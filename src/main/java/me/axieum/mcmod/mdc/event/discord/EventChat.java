package me.axieum.mcmod.mdc.event.discord;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.api.ChannelsConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.StringUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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

        // On regular chat message event
        if (!event.getMessage().getContentRaw().isEmpty()) {
            // Attempt to run message as a command
            if (EventCommand.onCommandMessage(event))
                return;
            // Not a command, hence regular chat event
            onChatMessage(event);
        }

        // On attachments
        if (!event.getMessage().getAttachments().isEmpty())
            onAttachment(event);
    }

    /**
     * Handle a Discord chat message event.
     *
     * @param event Discord message received event
     */
    public static void onChatMessage(@Nonnull MessageReceivedEvent event)
    {
        // Fetch useful information
        final Member member = event.getMember();

        final String author = member != null ? member.getEffectiveName()
                                             : event.getAuthor().getName();
        final String body = StringUtils.discordToMc(event.getMessage().getContentDisplay().trim());

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .withDateTime("DATE")
                .add("AUTHOR", author)
                .add("MESSAGE", body);

        // Format and send messages
        for (ChannelsConfig.ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getDiscordMessages().chat;
            if (message == null || message.isEmpty()) continue;

            final StringTextComponent component = new StringTextComponent(formatter.format(message));

            // Send message
            for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
                player.sendMessage(component);
        }
    }

    /**
     * Handle a Discord message attachment(s) event.
     *
     * @param event Discord message received event
     */
    public static void onAttachment(@Nonnull MessageReceivedEvent event)
    {
        // Fetch useful information
        final Member member = event.getMember();

        final String author = member != null ? member.getEffectiveName()
                                             : event.getAuthor().getName();

        final List<String> attachmentUrls = event.getMessage()
                                                 .getAttachments()
                                                 .stream().map(Message.Attachment::getUrl)
                                                 .collect(Collectors.toList());

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .withDateTime("DATE")
                .add("AUTHOR", author)
                .addDelimited("ATTACHMENTS", attachmentUrls);

        // Format and send messages
        for (ChannelsConfig.ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getDiscordMessages().attachment;
            if (message == null || message.isEmpty()) continue;

            final ITextComponent component = ForgeHooks.newChatWithLinks(formatter.format(message));

            // Send message
            for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
                player.sendMessage(component);
        }
    }
}
