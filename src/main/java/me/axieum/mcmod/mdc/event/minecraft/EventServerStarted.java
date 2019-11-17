package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.ServerUtils;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@Mod.EventBusSubscriber
public class EventServerStarted
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarted(FMLServerStartedEvent event)
    {
        final DiscordClient discord = DiscordClient.getInstance();

        // Set the Bot status
        discord.getApi().getPresence().setStatus(Config.BOT_STATUS_STARTED.get());

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .addDuration("ELAPSED", ServerUtils.getStartupTime());

        // Format and send messages
        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getMCMessages().started;
            if (message == null || message.isEmpty()) continue;

            // Send message
            discord.sendMessage(formatter.apply(message), channel.id);
        }
    }
}
