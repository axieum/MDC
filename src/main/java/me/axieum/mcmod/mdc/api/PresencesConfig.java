package me.axieum.mcmod.mdc.api;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.ServerUtils;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;

import java.util.List;

/**
 * Presences configuration schema wrapper.
 */
@Path("general.bot")
public class PresencesConfig
{
    public List<PresenceConfig> presences;

    /**
     * Presence configuration schema.
     */
    public static class PresenceConfig
    {
        public ActivityType type;
        public String name, url;

        // Prepare name formatter
        @PreserveNotNull
        private final static MessageFormatter FORMATTER = new MessageFormatter().addDateTime("DATETIME")
                                                                                .addDuration("UPTIME",
                                                                                             ServerUtils::getUptime)
                                                                                .add("PLAYER_COUNT",
                                                                                     () -> String.valueOf(ServerUtils.getPlayerCount()))
                                                                                .add("MAX_PLAYERS",
                                                                                     () -> String.valueOf(ServerUtils.getMaxPlayerCount()))
                                                                                .add("MOTD", ServerUtils::getMOTD)
                                                                                .add("TPS",
                                                                                     () -> String.format("%.2f",
                                                                                                         ServerUtils.getAverageTPS()))
                                                                                .add("TPS_TIME",
                                                                                     () -> String.format("%.0f",
                                                                                                         ServerUtils.getAverageTPSTime()))
                                                                                .add("WORLD",
                                                                                     ServerUtils::getWorldName);

        /**
         * Instantiates a new presence Activity from this config.
         *
         * @return new Discord Activity
         */
        public Activity getActivity()
        {
            try {
                return Activity.of(type, FORMATTER.apply(name), url);
            } catch (Exception e) {
                MDC.LOGGER.warn("Invalid presence entry: {}", e.getMessage());
                return null;
            }
        }
    }
}
