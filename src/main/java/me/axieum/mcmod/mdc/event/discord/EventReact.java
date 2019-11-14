package me.axieum.mcmod.mdc.event.discord;

import com.vdurmont.emoji.EmojiParser;
import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.api.ChannelsConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.PlayerUtils;
import me.axieum.mcmod.mdc.util.StringUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;

public class EventReact implements EventListener
{
    @Override
    public void onEvent(@Nonnull GenericEvent e)
    {
        if (!(e instanceof GenericMessageReactionEvent)) return;
        final GenericMessageReactionEvent event = (GenericMessageReactionEvent) e;

        // Fetch reaction context/message and forward event
        event.getTextChannel()
             .retrieveMessageById(event.getMessageId())
             .queue(message -> onReactEvent(event, message));
    }

    /**
     * Handle reaction event with pre-fetched message context.
     *
     * @param event   message reaction event (add/remove)
     * @param context reaction context (i.e. message being reacted to)
     */
    private void onReactEvent(@Nonnull GenericMessageReactionEvent event, Message context)
    {
        // Fetch useful information about the reaction
        final boolean added = event instanceof MessageReactionAddEvent;
        final String reactor = event.getMember() != null ? event.getMember().getEffectiveName()
                                                         : event.getUser().getName();
        final String emote = event.getReactionEmote().getName();

        // Fetch useful information about the context of the reaction
        // TODO: May need to check if author is the bot, in which case the
        //       author may not represent the actual author. Perhaps, prefix
        //       the body with the non-bot author?
        final String author = event.getMember() != null ? event.getMember().getEffectiveName()
                                                        : context.getAuthor().getName();
        final String body = StringUtils.discordToMc(context.getContentDisplay());

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .add("AUTHOR", author)
                .add("REACTOR", reactor)
                // Should we translate unicode to names (e.g. to ':smiley:')
                .add("EMOTE", Config.EMOJI_TRANSLATION.get() ? EmojiParser.parseToAliases(emote) : emote)
                // Accept first argument of message format as max characters to show
                .add("MESSAGE", args -> {
                    try {
                        final int max = Integer.parseInt(args.get(2));
                        // Truncate message to maximum length
                        return body.length() > max ? body.substring(0, max).concat("...") : body;
                    } catch (Exception e) {
                        MDC.LOGGER.error("Unable to format '{}': {}", args.get(0), e.getMessage());
                        return "";
                    }
                });

        // Format and send messages
        for (ChannelsConfig.ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format (added/removed reaction)
            String message = added ? channel.getDiscordMessages().react
                                   : channel.getDiscordMessages().unreact;
            if (message == null || message.isEmpty()) continue;

            // Send message
            PlayerUtils.sendAllMessage(new StringTextComponent(formatter.apply(message)), channel.dimensions);
        }
    }
}
