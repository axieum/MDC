package me.axieum.mcmod.mdc.event;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
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
        if (!DiscordClient.getInstance().isReady()) return;

        final JDA discord = DiscordClient.getInstance().getApi();
        for (ChannelConfig entry : Config.getChannels()) {
            // Fetch the started message format
            String message = entry.messages.started;
            if (message == null || message.isEmpty()) continue;

            // Fetch the Discord channel to send the message to
            TextChannel channel = discord.getTextChannelById(entry.id);
            if (channel == null) continue;

            // Handle message substitutions
            message = new MessageFormatter(message)
                    .withDateTime()
                    .add("{DURATION}", String.valueOf((MDC.startedAt - MDC.startingAt) / 1000))
                    .toString();

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
