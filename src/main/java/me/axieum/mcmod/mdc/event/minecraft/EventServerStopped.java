package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.ServerUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

@Mod.EventBusSubscriber
public class EventServerStopped
{
    @SubscribeEvent
    public static void onServerStopped(FMLServerStoppedEvent event)
    {
        final DiscordClient discord = DiscordClient.getInstance();

        // Set the Bot status
        discord.setBotStatus(Config.BOT_STATUS_STOPPED.get());

        final boolean crashed = MDC.stoppingAt == 0;

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .addDuration("UPTIME", ServerUtils.getUptime());

        // Format and send messages
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
