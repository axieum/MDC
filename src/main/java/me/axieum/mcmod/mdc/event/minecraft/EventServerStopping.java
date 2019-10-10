package me.axieum.mcmod.mdc.event.minecraft;

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
        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .withDateTime("DATE")
                .withDuration("UPTIME",
                              Duration.ofMillis(System.currentTimeMillis() - MDC.startedAt));

        // Format and send messages
        final DiscordClient discord = DiscordClient.getInstance();
        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getMessages().stopping;
            if (message == null || message.isEmpty()) continue;

            // Send message
            discord.sendMessage(formatter.format(message), channel.id);
        }
    }
}
