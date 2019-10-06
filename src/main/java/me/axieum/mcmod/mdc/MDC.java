package me.axieum.mcmod.mdc;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("mdc")
@Mod.EventBusSubscriber
public class MDC
{
    public static final Logger LOGGER = LogManager.getLogger();

    // Server start time in milliseconds - used in computing uptime
    private static long startedAt;

    public MDC()
    {
        // Register configuration
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("mdc-common.toml"));
    }

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event)
    {
        // Prepare the Discord client and connect the bot
        DiscordClient.getInstance().connect(Config.BOT_TOKEN.get());
    }

    @SubscribeEvent
    public static void onServerStarted(FMLServerStartedEvent event)
    {
        startedAt = System.currentTimeMillis();

        // TODO: Broadcast server started messages
    }

    @SubscribeEvent
    public static void onServerStopping(FMLServerStoppingEvent event)
    {
        // TODO: Broadcast server stopping messages

        // Disconnect the Discord bot
        DiscordClient.getInstance().disconnect();
    }

    @SubscribeEvent
    public static void onServerStopped(FMLServerStoppedEvent event)
    {
        // TODO: Broadcast server stopped messages
    }
}
