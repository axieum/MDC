package me.axieum.mcmod.mdc.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;

import java.util.HashMap;

public class PlayerUtils
{
    public static HashMap<PlayerEntity, Long> loginTimes = new HashMap<>();

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
