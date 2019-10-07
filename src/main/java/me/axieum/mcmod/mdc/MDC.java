package me.axieum.mcmod.mdc;

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

    // Server timing in milliseconds - used in computing uptime and startup duration
    public static long startingAt = 0, startedAt = 0;

    // Server crashes can be detected if the stopping event was invoked
    private static boolean isStopping;

    public MDC()
    {
        // Register configuration
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("mdc-common.toml"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerAboutToStart(FMLServerAboutToStartEvent event)
    {
        startingAt = System.currentTimeMillis(); // accuracy with highest priority
    }

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event)
    {
        // Prepare the Discord client and connect the bot
        DiscordClient.getInstance().connect(Config.BOT_TOKEN.get());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerStarted(FMLServerStartedEvent event)
    {
        startedAt = System.currentTimeMillis(); // accuracy with highest priority
    }

    @SubscribeEvent
    public static void onServerStopping(FMLServerStoppingEvent event)
    {
        isStopping = true;

        // TODO: Broadcast server stopping messages
    }

    @SubscribeEvent
    public static void onServerStopped(FMLServerStoppedEvent event)
    {
        // TODO: Broadcast server stopped/crashed messages
        if (!isStopping)
            LOGGER.debug("Discord bot detected a server crash!");

        // Disconnect the Discord bot
        DiscordClient.getInstance().disconnect();
    }
}
