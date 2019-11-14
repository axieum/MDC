package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.api.ChannelsConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.PlayerUtils;
import me.axieum.mcmod.mdc.util.StringUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EventPlayerChat
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerChat(ServerChatEvent event)
    {
        // Fetch useful information
        PlayerEntity player = event.getPlayer();
        final int dimensionId = player.dimension.getId();

        final String name = player.getName().getFormattedText();
        final String body = StringUtils.mcToDiscord(event.getMessage());
        final String dimension = PlayerUtils.getDimensionName(player);

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .add("PLAYER", name)
                .add("MESSAGE", body)
                .add("DIMENSION", dimension);

        // Format and send messages
        final DiscordClient discord = DiscordClient.getInstance();
        for (ChannelsConfig.ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getMCMessages().chat;
            if (message == null || message.isEmpty()) continue;

            // Does this config entry listen to this dimension?
            if (!channel.listensToDimension(dimensionId)) continue;

            // Send message
            discord.sendMessage(formatter.apply(message), channel.id);
        }
    }
}
