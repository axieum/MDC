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

@Mod.EventBusSubscriber
public class EventPlayerJoined
{
    @SubscribeEvent
    public static void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event)
    {
        final PlayerEntity player = event.getPlayer();

        // Fetch useful event information
        final String name = player.getName().getFormattedText();
        final double x = player.posX, y = player.posY, z = player.posZ;
        final int dimensionId = player.dimension.getId();
        final String dimension = PlayerUtils.getDimensionName(player);

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .add("PLAYER", name)
                .add("DIMENSION", dimension)
                .add("X", String.valueOf((int) x))
                .add("Y", String.valueOf((int) y))
                .add("Z", String.valueOf((int) z));

        // Format and send messages
        final DiscordClient discord = DiscordClient.getInstance();
        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getMCMessages().join;
            if (message == null || message.isEmpty()) continue;

            // Does this config entry listen to this dimension?
            if (!channel.listensToDimension(dimensionId)) continue;

            // Send message
            discord.sendMessage(formatter.apply(message), channel.id);
        }

        // Cache their login time (NB: for computing play time)
        PlayerUtils.loginTimes.put(player, System.currentTimeMillis());
    }
}
