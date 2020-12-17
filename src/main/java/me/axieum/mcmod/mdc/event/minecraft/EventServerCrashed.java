package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.util.DiscordUtils;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.ServerUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

import java.io.File;
import java.util.Optional;

@Mod.EventBusSubscriber
public class EventServerCrashed
{
    @SubscribeEvent
    public static void onServerCrashed(FMLServerStoppedEvent event)
    {
        // Skipped 'stopping' lifecycle event (despite this being 'stopped')
        if (MDC.stoppingAt == 0) onServerCrashed();
    }

    /**
     * Handles the server crashing.
     */
    public static void onServerCrashed()
    {
        final DiscordClient discord = DiscordClient.getInstance();

        // Set the Bot status
        discord.setBotStatus(Config.BOT_STATUS_CRASHED.get());

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .addDuration("UPTIME", ServerUtils.getUptime());

        // Dispatch structured message
        DiscordUtils.sendMessagesFromMinecraft(formatter, "crashed", null);

        // Attach latest crash-report if generated and channel configured
        final TextChannel crashReportChannel = discord.getApi().getTextChannelById(Config.CRASH_REPORT_CHANNEL.get());
        if (crashReportChannel == null) {
            System.out.println("Invalid crash report channel!");
            return;
        }

        Optional<File> report = ServerUtils.getLatestCrashReport();
        if (report.isPresent())
            System.out.println("No report");
        report.ifPresent(file -> {
            try {
                crashReportChannel.sendFile(file)
                                  .complete(false); // Send now and do not queue
            } catch (RateLimitedException e) {
                MDC.LOGGER.warn("Unable to attach crash-report to channel: {}", e.getMessage());
            }
        });
    }
}
