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
        final boolean crashed = MDC.stoppingAt == 0;

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .addDuration("UPTIME",
                             Duration.ofMillis(System.currentTimeMillis() - MDC.startedAt));

        // Format and send messages
        final DiscordClient discord = DiscordClient.getInstance();
        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format - whether crashed or not
            String message = crashed ? channel.getMCMessages().crashed
                                     : channel.getMCMessages().stopped;
            if (message == null || message.isEmpty()) continue;

            // Send message
            discord.sendMessage(formatter.apply(message), channel.id);
        }
    }
}
