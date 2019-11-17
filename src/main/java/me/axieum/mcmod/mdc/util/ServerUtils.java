package me.axieum.mcmod.mdc.util;

import me.axieum.mcmod.mdc.MDC;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.time.Duration;
import java.util.stream.LongStream;

public class ServerUtils
{
    /**
     * Returns the server's startup duration.
     *
     * @return duration of server booting
     */
    public static Duration getStartupTime()
    {
        return MDC.startedAt != 0 ? Duration.ofMillis(MDC.startedAt - MDC.startingAt)
                                  : Duration.ZERO;
    }

    /**
     * Returns the current server's uptime.
     *
     * @return current duration of server being online or zero if not started
     */
    public static Duration getUptime()
    {
        return MDC.startedAt != 0 ? Duration.ofMillis(System.currentTimeMillis() - MDC.startedAt)
                                  : Duration.ZERO;
    }

    /**
     * Returns the server's current average TPS.
     *
     * @return current average ticks per second
     */
    public static double getAverageTPS()
    {
        return Math.min(1000f / getAverageTPSTime(), 20);
    }

    /**
     * Returns the server's current average TPS time.
     *
     * @return current average tick time
     */
    public static double getAverageTPSTime()
    {
        double meanTPS = LongStream.of(ServerLifecycleHooks.getCurrentServer().tickTimeArray)
                                   .average()
                                   .orElse(0);
        return meanTPS * 1e-6d;
    }

    /**
     * Returns the current player count.
     *
     * @return number of currently logged in players
     */
    public static int getPlayerCount()
    {
        return ServerLifecycleHooks.getCurrentServer().getCurrentPlayerCount();
    }

    /**
     * Returns the maximum player count.
     *
     * @return maximum number of logged in players at any given time
     */
    public static int getMaxPlayerCount()
    {
        return ServerLifecycleHooks.getCurrentServer().getMaxPlayers();
    }

    /**
     * Returns the server's MOTD.
     *
     * @return server MOTD
     */
    public static String getMOTD()
    {
        // TODO: Probably strip formatting, and simplify for Discord
        return ServerLifecycleHooks.getCurrentServer().getMOTD();
    }

    /**
     * Returns the server's world name.
     *
     * @return server world name
     */
    public static String getWorldName()
    {
        return ServerLifecycleHooks.getCurrentServer().getWorldName();
    }
}
