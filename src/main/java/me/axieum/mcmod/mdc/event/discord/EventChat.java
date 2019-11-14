package me.axieum.mcmod.mdc.event.discord;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.api.ChannelsConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.PlayerUtils;
import me.axieum.mcmod.mdc.util.StringUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class EventChat implements EventListener
{
    @Override
    public void onEvent(@Nonnull GenericEvent e)
    {
        if (!(e instanceof MessageReceivedEvent)) return;
        final MessageReceivedEvent event = (MessageReceivedEvent) e;

        // Should we handle this chat event?
        // NB: bots or ourselves
        if (event.getAuthor().isBot() ||
            event.getAuthor().getId().equals(DiscordClient.getInstance().getApi().getSelfUser().getId()))
            return;

        // On regular chat message event, attempt treat as a command, else chat
        if (!event.getMessage().getContentRaw().isEmpty())
            if (!EventCommand.onCommandMessage(event))
                onChatMessage(event);

        // On attachments
        if (!event.getMessage().getAttachments().isEmpty())
            onAttachment(event);
    }

    /**
     * Handle a Discord chat message event.
     *
     * @param event Discord message received event
     */
    private static void onChatMessage(@Nonnull MessageReceivedEvent event)
    {
        // Fetch useful information
        final String author = event.getMember() != null ? event.getMember().getEffectiveName()
                                                        : event.getAuthor().getName();
        final String body = StringUtils.discordToMc(event.getMessage().getContentDisplay());

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .add("AUTHOR", author)
                .add("MESSAGE", body);

        // Format and send messages
        for (ChannelsConfig.ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getDiscordMessages().chat;
            if (message == null || message.isEmpty()) continue;

            // Send message
            PlayerUtils.sendAllMessage(new StringTextComponent(formatter.apply(message)), channel.dimensions);
        }
    }

    /**
     * Handle a Discord message attachment(s) event.
     *
     * @param event Discord message received event
     */
    private static void onAttachment(@Nonnull MessageReceivedEvent event)
    {
        // Fetch useful information
        final String author = event.getMember() != null ? event.getMember().getEffectiveName()
                                                        : event.getAuthor().getName();

        final List<String> attachmentUrls = event.getMessage()
                                                 .getAttachments()
                                                 .stream().map(Message.Attachment::getUrl)
                                                 .collect(Collectors.toList());

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .add("AUTHOR", author)
                .add("ATTACHMENT_URLS", String.join(",", attachmentUrls));

        // Format and send messages
        for (ChannelsConfig.ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getDiscordMessages().attachment;
            if (message == null || message.isEmpty()) continue;

            // Send message
            PlayerUtils.sendAllMessage(ForgeHooks.newChatWithLinks(formatter.apply(message)), channel.dimensions);
        }
    }
}
