package me.axieum.mcmod.mdc.util;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.MDC;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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
     * Returns a dimension's current average TPS.
     *
     * @return current average ticks per second for dimension
     */
    public static double getAverageTPS(DimensionType dimension)
    {
        return Math.min(1000f / getAverageTPSTime(dimension), 20);
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
     * Returns a dimension's current average TPS time.
     *
     * @return current average tick time for dimension
     */
    public static double getAverageTPSTime(DimensionType dimension)
    {
        final long[] tickArray = ServerLifecycleHooks.getCurrentServer().getTickTime(dimension);
        return tickArray == null ? 0 : LongStream.of(tickArray).average().orElse(0) * 1e-6d;
    }

    /**
     * Get dimension name in title format.
     *
     * @param dimension dimension to derive name from
     * @return dimension friendly title
     */
    public static String getDimensionName(DimensionType dimension)
    {
        // NB: Appears there is no way to get translated dimension name
        final ResourceLocation dimKey = DimensionType.getKey(dimension);
        return dimKey != null ? StringUtils.strToTitle(dimKey.getPath(), '_')
                              : "";
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

    /**
     * Fetches the newest crash report from the configured directory.
     *
     * @return newest, optional crash report file
     */
    public static Optional<File> getLatestCrashReport()
    {
        // Use configured crash report directory, else fallback
        String crashReportDir = Config.CRASH_REPORT_DIRECTORY.get();
        if (crashReportDir == null || crashReportDir.isEmpty())
            crashReportDir = "crash-reports";

        System.out.println("Fetching latest crash report...");

        final File crashReportFolder = ServerLifecycleHooks.getCurrentServer().getFile(crashReportDir);

        try (Stream<Path> reports = Files.list(crashReportFolder.toPath())) {
            return reports.filter(f -> !Files.isDirectory(f))
                          .map(Path::toFile)
                          .max(Comparator.comparingLong(File::lastModified));
        } catch (IOException e) {
            MDC.LOGGER.error("Unable to get the latest crash report!", e);
            return Optional.empty();
        }
    }
}
