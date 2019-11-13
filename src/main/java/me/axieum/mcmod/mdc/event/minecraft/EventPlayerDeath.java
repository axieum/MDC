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
        final PlayerEntity player = (PlayerEntity) event.getEntityLiving();

        // Fetch useful event information
        final String name = player.getName().getFormattedText();
        final String holding = PlayerUtils.getHeldItemName(player);
        final double x = player.prevPosX, y = player.prevPosY, z = player.prevPosZ;
        final String dimension = PlayerUtils.getDimensionName(player);
        final String cause = event.getSource()
                                  .getDeathMessage(event.getEntityLiving())
                                  .getUnformattedComponentText()
                                  .trim();

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .withDateTime("DATE")
                .add("PLAYER", name)
                .add("CAUSE", cause)
                .addOptional("HOLDING", holding)
                .add("DIMENSION", dimension)
                .add("X", String.valueOf((int) x))
                .add("Y", String.valueOf((int) y))
                .add("Z", String.valueOf((int) z));

        // Format and send messages
        final DiscordClient discord = DiscordClient.getInstance();
        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the message format
            String message = channel.getMCMessages().death;
            if (message == null || message.isEmpty()) continue;

            // Send message
            discord.sendMessage(formatter.format(message), channel.id);
        }
    }
}
