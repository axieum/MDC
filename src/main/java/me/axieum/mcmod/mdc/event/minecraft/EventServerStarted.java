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
        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .addDuration("ELAPSED", Duration.ofMillis(MDC.startedAt - MDC.startingAt));

        // Format and send messages
        final DiscordClient discord = DiscordClient.getInstance();
        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getMCMessages().started;
            if (message == null || message.isEmpty()) continue;

            // Send message
            discord.sendMessage(formatter.apply(message), channel.id);
        }
    }
}
