package me.axieum.mcmod.mdc;

import me.axieum.mcmod.mdc.command.discord.CommandTPS;
import me.axieum.mcmod.mdc.command.discord.CommandUptime;
import me.axieum.mcmod.mdc.event.discord.EventChat;
import me.axieum.mcmod.mdc.event.discord.EventEdit;
import me.axieum.mcmod.mdc.event.discord.EventPresence;
import me.axieum.mcmod.mdc.event.discord.EventReact;
import me.axieum.mcmod.mdc.event.minecraft.EventServerCrashed;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.*;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("mdc")
@Mod.EventBusSubscriber
public class MDC
{
    public static final Logger LOGGER = LogManager.getLogger();

    // Timings in milliseconds - used in computing uptime and startup/shutdown duration
    public static long startingAt = 0, startedAt = 0, stoppingAt = 0, stoppedAt = 0;

    public MDC()
    {
        // Register configuration
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("mdc-common.toml"));

        // Register Discord event listeners
        DiscordClient.getInstance().addEventListeners(new EventChat(),
                                                      new EventEdit(),
                                                      new EventReact(),
                                                      new EventPresence());

        // Register Discord commands (not config level)
        if (Config.COMMAND_TPS_ENABLED.get())
            DiscordClient.getInstance().addCommands(new CommandTPS());
        if (Config.COMMAND_UPTIME_ENABLED.get())
            DiscordClient.getInstance().addCommands(new CommandUptime());

        // Manually trigger the crash handler should a forceful shutdown occur
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Skipped 'stopped' lifecycle event
            if (MDC.stoppedAt == 0) {
                try {
                    EventServerCrashed.onServerCrashed();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerAboutToStart(FMLServerAboutToStartEvent event)
    {
        startingAt = System.currentTimeMillis(); // accuracy with the highest priority
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onServerStarting(FMLServerStartingEvent event)
    {
        // Prepare the Discord client and connect the bot
        DiscordClient.getInstance().connect(Config.BOT_TOKEN.get(), Config.BOT_STATUS_STARTING.get());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStarted(FMLServerStartedEvent event)
    {
        startedAt = System.currentTimeMillis(); // accuracy with the highest priority
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStopping(FMLServerStoppingEvent event)
    {
        stoppingAt = System.currentTimeMillis(); // accuracy with the highest priority
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStopped(FMLServerStoppedEvent event)
    {
        stoppedAt = System.currentTimeMillis(); // accuracy with the lowest priority

        // Disconnect the Discord bot
//        DiscordClient.getInstance().disconnect();
    }
}
