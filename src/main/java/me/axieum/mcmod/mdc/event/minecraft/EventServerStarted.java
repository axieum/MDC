package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

import java.time.Duration;

@Mod.EventBusSubscriber
public class EventServerStarted
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarted(FMLServerStartedEvent event)
    {
        final DiscordClient discord = DiscordClient.getInstance();

        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getMessages().started;
            if (message == null || message.isEmpty()) continue;

            // Handle message substitutions
            message = new MessageFormatter(message)
                    .withDateTime("DATE")
                    .withDuration("ELAPSED", Duration.ofMillis(MDC.startedAt - MDC.startingAt))
                    .toString();

            // Send message
            discord.sendMessage(message, channel.id);
        }
    }
}
