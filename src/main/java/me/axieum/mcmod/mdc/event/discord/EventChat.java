package me.axieum.mcmod.mdc.event.discord;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.api.ChannelsConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.StringUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

public class EventChat implements EventListener
{
    @Override
    public void onEvent(@Nonnull GenericEvent e)
    {
        if (!(e instanceof MessageReceivedEvent)) return;

        final MessageReceivedEvent event = (MessageReceivedEvent) e;
        if (event.getAuthor().isBot()) return; // ignore bots
        if (event.getAuthor().getId().equals(
                DiscordClient.getInstance().getApi().getSelfUser().getId()))
            return; // ignore self

        // Fetch useful information
        final Member member = event.getMember();

        final String author = member != null ? member.getEffectiveName()
                                             : event.getAuthor().getName();
        final String body = StringUtils.discordToMc(event.getMessage().getContentDisplay().trim());

        // TODO: Handle commands

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
}
