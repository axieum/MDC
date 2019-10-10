package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
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
        final String playerName = player.getName().getFormattedText();
        final String cause = event.getSource()
                                  .getDeathMessage(event.getEntityLiving())
                                  .getUnformattedComponentText()
                                  .trim();
        // Item player was holding
        String holding = PlayerUtils.getHeldItemName(player);
        // Position of player
        final double x = player.prevPosX, y = player.prevPosY, z = player.prevPosZ;
        // Dimension name player was in
        final String dimension = PlayerUtils.getDimensionName(player);

        // Format and send messages
        final DiscordClient discord = DiscordClient.getInstance();
        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the started message format
            String message = channel.getMessages().death;
            if (message == null || message.isEmpty()) continue;

            // Handle message substitutions
            message = new MessageFormatter(message)
                    .withDateTime("DATE")
                    .add("PLAYER", playerName)
                    .add("CAUSE", cause)
                    .addOptional("HOLDING", holding)
                    .add("DIMENSION", dimension)
                    .add("X", String.valueOf((int) x))
                    .add("Y", String.valueOf((int) y))
                    .add("Z", String.valueOf((int) z))
                    .toString();

            // Send message
            discord.sendMessage(message, channel.id);
        }
    }
}
