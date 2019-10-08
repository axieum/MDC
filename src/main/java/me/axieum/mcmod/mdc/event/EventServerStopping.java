package me.axieum.mcmod.mdc.event;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import java.time.Duration;

@Mod.EventBusSubscriber
public class EventServerStopping
{
    @SubscribeEvent
    public static void onServerStopping(FMLServerStoppingEvent event)
    {
        final DiscordClient discord = DiscordClient.getInstance();

        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the started message format
            String message = channel.messages.stopping;
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
