package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

import java.time.Duration;

@Mod.EventBusSubscriber
public class EventServerStopped
{
    @SubscribeEvent
    public static void onServerStopped(FMLServerStoppedEvent event)
    {
        final DiscordClient discord = DiscordClient.getInstance();

        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = MDC.stoppingAt == 0 ? channel.getMessages().crashed : channel.getMessages().stopped;
            if (message == null || message.isEmpty()) continue;

            // Handle message substitutions
            message = new MessageFormatter(message)
                    .withDateTime("DATE")
                    .withDuration("UPTIME",
                                  Duration.ofMillis(System.currentTimeMillis() - MDC.startedAt))
                    .toString();

            // Send message
            discord.sendMessage(message, channel.id);
        }
    }
}
