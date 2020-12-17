package me.axieum.mcmod.mdc.event.discord;

import com.github.difflib.text.DiffRowGenerator;
import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.PlayerUtils;
import me.axieum.mcmod.mdc.util.StringUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;
import java.util.*;

public class EventEdit implements EventListener
{
    // Diff generator for message edit
    private static final DiffRowGenerator DIFF_GENERATOR = DiffRowGenerator.create()
                                                                           .showInlineDiffs(true)
                                                                           .mergeOriginalRevised(true)
                                                                           .inlineDiffByWord(true)
                                                                           .oldTag(f -> f ? "\u00A7c~~" : "~~\u00A7r")
                                                                           .newTag(f -> f ? "\u00A7a" : "\u00A7r")
                                                                           .build();

    // Channel message history cache
    private static final int HISTORY_SIZE = 16;
    private static HashMap<Long, LinkedCircularHashMap<Long, Message>> histories = new HashMap<>();

    @Override
    public void onEvent(@Nonnull GenericEvent e)
    {
        // Off-load events to handle message histories cache
        if (e instanceof MessageReceivedEvent) onMessageReceivedEvent((MessageReceivedEvent) e);
        else if (e instanceof MessageDeleteEvent) onMessageDeletedEvent((MessageDeleteEvent) e);
        else if (e instanceof ReadyEvent) populateHistories(e);

        if (!(e instanceof MessageUpdateEvent)) return;
        final MessageUpdateEvent event = (MessageUpdateEvent) e;

        // Should we handle this chat event?
        // NB: bots or ourselves
        if (event.getAuthor().isBot() ||
            event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId()))
            return;

        // If we have the old message, forward to the message update event
        Optional.ofNullable(histories.get(event.getTextChannel().getIdLong()))
                .ifPresent(history -> {
                    final Message message = history.get(event.getMessageIdLong());
                    if (message != null) {
                        onMessageUpdatedEvent(event, message); // forward event
                        history.put(event.getMessageIdLong(), event.getMessage()); // update history
                    }
                });
    }

    /**
     * Handles the update event of a message.
     *
     * @param event   message update event
     * @param context old message before update
     */
    private void onMessageUpdatedEvent(@Nonnull MessageUpdateEvent event, @Nonnull Message context)
    {
        // Fetch useful information
        final String author = event.getMember() != null ? event.getMember().getEffectiveName()
                                                        : event.getAuthor().getName();
        final String bodyNew = event.getMessage().getContentDisplay();
        final String bodyOld = context.getContentDisplay();

        // Produce a diff between old and new message if we can
        final String bodyDiff;
        try {
            String diffOld = context.getContentStripped().trim();
            String diffNew = event.getMessage().getContentStripped().trim();

            bodyDiff = DIFF_GENERATOR.generateDiffRows(Arrays.asList(diffOld), Arrays.asList(diffNew))
                                     .get(0)
                                     .getOldLine();
        } catch (Exception e) {
            MDC.LOGGER.error("Unable to produce diff for a Discord message update: {}", e.getMessage());
            return;
        }

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .add("AUTHOR", author)
                .add("OLD", StringUtils.discordToMc(bodyOld))
                .add("NEW", StringUtils.discordToMc(bodyNew))
                .add("DIFFERENCE", StringUtils.discordToMc(bodyDiff));

        // Dispatch structured message
        PlayerUtils.sendMessagesFromDiscord(formatter, "edit", event.getTextChannel().getIdLong(), false);
    }

    /**
     * Handles preparing the message histories.
     *
     * @param event generic JDA event
     */
    private static void populateHistories(@Nonnull GenericEvent event)
    {
        final JDA jda = event.getJDA();
        final List<ChannelConfig> channels = Config.getChannels();

        MDC.LOGGER.info("Tracking message edits for last {} messages across {} channels",
                        HISTORY_SIZE,
                        channels.size());

        // Attempt to fetch messages from only configured channels
        for (ChannelConfig channelConfig : channels) {
            TextChannel channel = jda.getTextChannelById(channelConfig.id);
            if (channel == null) continue;

            // Do we already have a history for this channel?
            histories.putIfAbsent(channel.getIdLong(), new LinkedCircularHashMap<>(HISTORY_SIZE));

            MDC.LOGGER.debug("Eager-loading last {} messages from the channel: '{}'", HISTORY_SIZE, channel.getName());
            final LinkedCircularHashMap<Long, Message> history = histories.get(channel.getIdLong());
            channel.getHistory()
                   .retrievePast(HISTORY_SIZE)
                   .queue(messages -> {
                       Collections.reverse(messages); // add to map oldest to newest
                       for (Message message : messages)
                           history.put(message.getIdLong(), message);
                   });
        }
    }

    /**
     * Pushes a received message to the local cache in order to track message
     * history.
     *
     * @param event message received event
     */
    private static void onMessageReceivedEvent(@Nonnull MessageReceivedEvent event)
    {
        final long channelId = event.getChannel().getIdLong();

        // Do we have a history for this channel yet?
        histories.putIfAbsent(channelId, new LinkedCircularHashMap<>(HISTORY_SIZE));

        // Push this message onto the history stack
        histories.get(channelId).put(event.getMessageIdLong(), event.getMessage());
    }

    /**
     * Pops a deleted message from the local cache.
     *
     * @param event message deletion event
     */
    private static void onMessageDeletedEvent(@Nonnull MessageDeleteEvent event)
    {
        // Remove this message from the history via its message ID if exists
        Optional.ofNullable(histories.get(event.getChannel().getIdLong()))
                .ifPresent(history -> history.remove(event.getMessageIdLong()));
    }

    /**
     * A {@link LinkedHashMap} with a maximum capacity that removes eldest
     * entries to make room for newer ones.
     *
     * @param <K> the type of keys maintained by this map
     * @param <V> the type of mapped values
     */
    private static class LinkedCircularHashMap<K, V> extends LinkedHashMap<K, V>
    {
        private final int capacity;

        /**
         * Constructs a new Linked Circular Hash Map with a maximum capacity.
         *
         * @param capacity maximum capacity
         */
        private LinkedCircularHashMap(int capacity)
        {
            super(capacity, 1.0f);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest)
        {
            return size() > capacity;
        }
    }
}
