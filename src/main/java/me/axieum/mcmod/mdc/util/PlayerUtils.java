package me.axieum.mcmod.mdc.util;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.List;

public class PlayerUtils
{
    public static HashMap<PlayerEntity, Long> loginTimes = new HashMap<>();

    /**
     * Sends all structured messages defined in the configuration file to
     * Minecraft from a Discord channel. Performs channel checks.
     *
     * @param formatter Message Formatter instance
     * @param key       config channel message key/property
     * @param channelId Discord Guild channel ID in context
     * @param links     true if URLs should be formatted
     */
    public static void sendMessagesFromDiscord(MessageFormatter formatter, String key, Long channelId, boolean links)
    {
        for (ChannelConfig channel : Config.getChannels()) {
            // Does this config entry listen to this channel?
            if (channelId != null && channel.id != channelId) continue;

            // Fetch the message format
            String message = channel.getDiscordMessages().valueOf(key);
            if (message == null || message.isEmpty()) continue;

            // Send message
            if (links)
                PlayerUtils.sendAllMessage(ForgeHooks.newChatWithLinks(formatter.apply(message)), channel.dimensions);
            else
                PlayerUtils.sendAllMessage(new StringTextComponent(formatter.apply(message)), channel.dimensions);
        }
    }

    /**
     * Send a message to all online players.
     *
     * @param component text component to be sent
     */
    public static void sendAllMessage(ITextComponent component)
    {
        ServerLifecycleHooks.getCurrentServer()
                            .getPlayerList().getPlayers()
                            .forEach(player -> player.sendMessage(component));

        // Inform server/console
        ServerLifecycleHooks.getCurrentServer().sendMessage(component);
    }

    /**
     * Send a message to all online players in the given dimension(s).
     *
     * @param component    text component to be sent
     * @param dimensionIds dimension(s) ids players must be in to receive message
     */
    public static void sendAllMessage(ITextComponent component, List<Integer> dimensionIds)
    {
        if (dimensionIds != null && !dimensionIds.isEmpty()) {
            ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()
                                .stream()
                                .filter(player -> dimensionIds.contains(player.dimension.getId()))
                                .forEach(player -> player.sendMessage(component));

            // Inform server/console
            ServerLifecycleHooks.getCurrentServer().sendMessage(component);
        } else {
            // If there are no dimensions specified, send to all players
            sendAllMessage(component);
        }
    }

    /**
     * Get dimension name of player in title format.
     *
     * @param player player in dimension
     * @return dimension friendly title
     */
    public static String getDimensionName(PlayerEntity player)
    {
        // NB: Appears there is no way to get translated dimension name
        final ResourceLocation dimKey = DimensionType.getKey(player.dimension);
        return dimKey != null ? StringUtils.strToTitle(dimKey.getPath(), '_') : "";
    }

    /**
     * Get held item name of player.
     *
     * @param player player holding item
     * @return held item's name or blank
     */
    public static String getHeldItemName(PlayerEntity player)
    {
        final ItemStack itemInHand = player.getHeldItemMainhand();
        return itemInHand.isEmpty() ? "" : itemInHand.getDisplayName().getFormattedText();
    }

    /**
     * Compute duration in milliseconds since the player logged in.
     *
     * @param player player
     * @return milliseconds since login or 0 if unable to find
     */
    public static long getSessionPlayTime(PlayerEntity player)
    {
        final Long playTime = loginTimes.get(player);
        return playTime != null ? System.currentTimeMillis() - playTime : 0;
    }
}
