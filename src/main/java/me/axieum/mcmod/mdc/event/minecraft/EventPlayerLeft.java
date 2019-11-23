package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.util.DiscordUtils;
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
        final double x = player.posX, y = player.posY, z = player.posZ;
        final int dimensionId = player.dimension.getId();
        final String dimension = PlayerUtils.getDimensionName(player);
        final Duration elapsed = Duration.ofMillis(PlayerUtils.getSessionPlayTime(player));

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .add("PLAYER", name)
                .addDuration("ELAPSED", elapsed)
                .add("DIMENSION", dimension)
                .add("X", String.valueOf((int) x))
                .add("Y", String.valueOf((int) y))
                .add("Z", String.valueOf((int) z));

        // Dispatch structured message
        DiscordUtils.sendMessagesFromMinecraft(formatter, "leave", dimensionId);

        // We are done with the login time
        PlayerUtils.loginTimes.remove(player);
    }
}
