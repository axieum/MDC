package me.axieum.mcmod.mdc.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.List;

public class PlayerUtils
{
    public static HashMap<PlayerEntity, Long> loginTimes = new HashMap<>();

    /**
     * Send a message to all online players.
     *
     * @param component text component to be sent
     */
    public static void sendAllMessage(ITextComponent component)
    {
        for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
            player.sendMessage(component);
    }

    /**
     * Send a message to all online players in the given dimension(s).
     *
     * @param component    text component to be sent
     * @param dimensionIds dimension(s) ids players must be in to receive message
     */
    public static void sendAllMessage(ITextComponent component, List<Integer> dimensionIds)
    {
        if (dimensionIds != null && !dimensionIds.isEmpty())
            for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                final int playerDimId = player.dimension.getId();
                if (dimensionIds.stream().anyMatch(dimensionId -> playerDimId == dimensionId))
                    player.sendMessage(component);
            }
        else
            sendAllMessage(component);
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
        return dimKey != null ? StringUtils.strToTitle(dimKey.getPath(), '_')
                              : "";
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
     * @return milliseconds since login
     */
    public static long getSessionPlayTime(PlayerEntity player)
    {
        // Do we have their login time? This shouldn't ever happen.
        if (loginTimes.get(player) == null)
            return 0;

        return System.currentTimeMillis() - loginTimes.get(player);
    }
}
