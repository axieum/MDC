package me.axieum.mcmod.mdc.event;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;

public class EventServerStarted
{
    /**
     * Handle server started event given the Discord bot is ready.
     * NB: This event is triggered post-bot connecting.
     *
     * @see DiscordClient#onReady(ReadyEvent)
     */
    public static void invoke()
    {
        final JDA discord = DiscordClient.getInstance().getApi();

        if (discord == null) return;

        for (ChannelConfig entry : Config.getChannels()) {
            // Fetch the started message format
            String message = entry.messages.started;
            if (message == null || message.isEmpty()) continue;

            // Fetch the Discord channel to send the message to
            TextChannel channel = discord.getTextChannelById(entry.id);
            if (channel == null) continue;

            // TODO: Handle message substitutions

            // Send message
            try {
                channel.sendMessage(message).queue();
            } catch (Exception e) {
                MDC.LOGGER.error("Unable to send server started message to channel with id {}: {}",
                                 channel.getId(),
                                 e.getMessage());
            }
        }
    }
}
