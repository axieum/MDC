package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.Duration;

@Mod.EventBusSubscriber
public class EventPlayerLeft
{
    @SubscribeEvent
    public static void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
    {
        final PlayerEntity player = event.getPlayer();

        // Fetch useful event information
        final String name = player.getName().getFormattedText();
        // Position of player
        final double x = player.prevPosX, y = player.prevPosY, z = player.prevPosZ;
        // Dimension name player logged into
        final String dimension = PlayerUtils.getDimensionName(player);
        // Time spent logged in
        final Duration elapsed = Duration.ofMillis(PlayerUtils.getSessionPlayTime(player));

        // Format and send messages
        final DiscordClient discord = DiscordClient.getInstance();
        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getMessages().leave;
            if (message == null || message.isEmpty()) continue;

            // Handle message substitutions
            message = new MessageFormatter(message)
                    .withDateTime("DATE")
                    .add("PLAYER", name)
                    .withDuration("ELAPSED", elapsed)
                    .add("DIMENSION", dimension)
                    .add("X", String.valueOf((int) x))
                    .add("Y", String.valueOf((int) y))
                    .add("Z", String.valueOf((int) z))
                    .toString();

            // Send message
            discord.sendMessage(message, channel.id);
        }

        // We are done with the login time
        PlayerUtils.loginTimes.remove(player);
    }
}
