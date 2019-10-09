package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EventPlayerDeath
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(LivingDeathEvent event)
    {
        // Is this a player dying?
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;

        // Fetch useful event information
        final PlayerEntity player = (PlayerEntity) event.getEntityLiving();

        // Player name and cause of death
        final String name = player.getName().getFormattedText();
        final String cause = event.getSource()
                                  .getDeathMessage(event.getEntityLiving())
                                  .getUnformattedComponentText()
                                  .trim();
        // Item player was holding
        final ItemStack itemInHand = player.getHeldItemMainhand();
        final String holding = itemInHand.isEmpty() ? "" : itemInHand.getDisplayName().getFormattedText();
        // Position of player
        final double x = player.prevPosX, y = player.prevPosY, z = player.prevPosZ;
        // Dimension name player was in
        // NB: Appears there is no way to get translated dimension name
        final ResourceLocation dimKey = DimensionType.getKey(player.dimension);
        final String dimension = dimKey != null ? strToTitle(dimKey.getPath()) : "";

        // Format and send messages
        final DiscordClient discord = DiscordClient.getInstance();
        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the started message format
            String message = channel.getMessages().death;
            if (message == null || message.isEmpty()) continue;

            // Handle message substitutions
            message = new MessageFormatter(message)
                    .withDateTime("DATE")
                    .add("NAME", name)
                    .add("CAUSE", cause)
                    .addOptional("HOLDING", holding)
                    .add("DIMENSION", dimension)
                    .add("POSITION", String.format("%.0f, %.0f, %.0f", x, y, z))
                    .withFormatted("POSITION", x, y, z)
                    .add("X", String.format("%.0f", x))
                    .add("Y", String.format("%.0f", y))
                    .add("Z", String.format("%.0f", z))
                    .toString();

            // Send message
            discord.sendMessage(message, channel.id);
        }
    }

    /**
     * Capitalize first letter in each word in a string.
     *
     * @param str        string
     * @param delimiters characters replaced with spaces
     * @return string as a title
     */
    private static String strToTitle(String str, char... delimiters)
    {
        // Replace all delimiters with spaces
        for (char delimiter : delimiters)
            str = str.replace(delimiter, ' ');

        char[] chars = str.toCharArray();
        boolean capitalizeNext = true;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ' ') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                chars[i] = Character.toTitleCase(chars[i]);
                capitalizeNext = false;
            }
        }

        return new String(chars);
    }
}
