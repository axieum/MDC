package me.axieum.mcmod.mdc;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import me.axieum.mcmod.mdc.api.ChannelsConfig;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.api.CommandsConfig;
import me.axieum.mcmod.mdc.api.CommandsConfig.CommandConfig;
import me.axieum.mcmod.mdc.api.PresencesConfig;
import me.axieum.mcmod.mdc.api.PresencesConfig.PresenceConfig;
import net.dv8tion.jda.api.OnlineStatus;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class Config
{
    private static final Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;

    // [general]
    static {
        COMMON_BUILDER.comment("General configuration").push("general").pop();
    }

    // [general.bot]
    public static ConfigValue<String> BOT_TOKEN;
    public static EnumValue<OnlineStatus> BOT_STATUS_STARTING, BOT_STATUS_STARTED, BOT_STATUS_STOPPING,
            BOT_STATUS_STOPPED;
    public static LongValue BOT_PRESENCE_INTERVAL;
    private static PresencesConfig BOT_PRESENCES_TABLE;

    static {
        COMMON_BUILDER.comment("Discord bot configuration").push("general.bot");

        BOT_TOKEN = COMMON_BUILDER.comment("Discord bot token")
                                  .define("token", "");

        BOT_STATUS_STARTING = COMMON_BUILDER.comment("Bot status on server starting")
                                            .defineEnum("status.starting", OnlineStatus.IDLE);
        BOT_STATUS_STARTED = COMMON_BUILDER.comment("Bot status on server started")
                                           .defineEnum("status.started", OnlineStatus.ONLINE);
        BOT_STATUS_STOPPING = COMMON_BUILDER.comment("Bot status on server stopping")
                                            .defineEnum("status.stopping", OnlineStatus.DO_NOT_DISTURB);
        BOT_STATUS_STOPPED = COMMON_BUILDER.comment("Bot status on server stopped")
                                           .defineEnum("status.stopped", OnlineStatus.OFFLINE);

        BOT_PRESENCE_INTERVAL = COMMON_BUILDER.comment("Bot presence update interval (in seconds)")
                                              .defineInRange("presence_interval", 30, 12, Long.MAX_VALUE);

        COMMON_BUILDER.comment("Presence definitions")
                      .define("presences", new ArrayList<>());

        COMMON_BUILDER.pop(2);
    }

    // [general.chat]
    public static BooleanValue EMOJI_TRANSLATION;

    static {
        COMMON_BUILDER.comment("Chat configuration").push("general.chat");

        EMOJI_TRANSLATION = COMMON_BUILDER.comment("Should unicode emojis be translated to word form (i.e. ':emoji:')")
                                          .define("emoji_translation", true);

        COMMON_BUILDER.pop(2);
    }

    // [general.commands]
    public static ConfigValue<String> COMMAND_PREFIX, COMMAND_UNAUTHORISED;

    static {
        COMMON_BUILDER.comment("Command configuration").push("general.commands");

        COMMAND_PREFIX = COMMON_BUILDER.comment("Discord command prefix")
                                       .define("prefix", "!");

        COMMAND_UNAUTHORISED = COMMON_BUILDER.comment("Discord command unauthorised message")
                                             .define("unauthorised",
                                                     "**{{MENTION}}**, you were unauthorised to perform this action :no_good:");

        COMMON_BUILDER.pop(2);
    }

    // [general.commands.tps]
    public static BooleanValue COMMAND_TPS_ENABLED;
    public static ConfigValue<List<String>> COMMAND_TPS_ALIASES, COMMAND_TPS_PERMISSIONS;
    public static ConfigValue<List<Long>> COMMAND_TPS_CHANNELS;
    public static ConfigValue<List<Integer>> COMMAND_TPS_DIMENSIONS;

    static {
        COMMON_BUILDER.comment("TPS Discord command").push("general.commands.tps");

        COMMAND_TPS_ENABLED = COMMON_BUILDER.comment("Is the TPS command enabled for use?")
                                            .define("enabled", true);
        COMMAND_TPS_ALIASES = COMMON_BUILDER.comment("Command names/aliases to trigger on")
                                            .define("aliases", Arrays.asList("tps", "ticks"));
        COMMAND_TPS_PERMISSIONS = COMMON_BUILDER.comment("Permissions required to execute (blank for everyone)",
                                                         "Allowed Values: user:ID, user:Username#Tag, role:ID, Username#Tag")
                                                .define("permissions", new ArrayList<>());
        COMMAND_TPS_CHANNELS = COMMON_BUILDER.comment("Channels this command can be executed from (blank for all)")
                                             .define("channels", new ArrayList<>());
        COMMAND_TPS_DIMENSIONS = COMMON_BUILDER.comment("Minecraft dimension IDs to report TPS status (blank for all)")
                                               .define("dimensions", new ArrayList<>());

        COMMON_BUILDER.pop(3);
    }

    // [general.commands.uptime]
    public static BooleanValue COMMAND_UPTIME_ENABLED;
    public static ConfigValue<List<String>> COMMAND_UPTIME_ALIASES, COMMAND_UPTIME_PERMISSIONS;
    public static ConfigValue<List<Long>> COMMAND_UPTIME_CHANNELS;
    public static ConfigValue<String> COMMAND_UPTIME_FORMAT;

    static {
        COMMON_BUILDER.comment("Uptime Discord command").push("general.commands.uptime");

        COMMAND_UPTIME_ENABLED = COMMON_BUILDER.comment("Is the uptime command enabled for use?")
                                               .define("enabled", true);
        COMMAND_UPTIME_ALIASES = COMMON_BUILDER.comment("Command names/aliases to trigger on")
                                               .define("aliases", Arrays.asList("uptime"));
        COMMAND_UPTIME_PERMISSIONS = COMMON_BUILDER.comment("Permissions required to execute (blank for everyone)",
                                                            "Allowed Values: user:ID, user:Username#Tag, role:ID, Username#Tag")
                                                   .define("permissions", new ArrayList<>());
        COMMAND_UPTIME_CHANNELS = COMMON_BUILDER.comment("Channels this command can be executed from (blank for all)")
                                                .define("channels", new ArrayList<>());
        COMMAND_UPTIME_FORMAT = COMMON_BUILDER.comment("Message response format",
                                                       "Allowed Values: MENTION, UPTIME|format")
                                              .define("format",
                                                      "The server has been online for {{UPTIME}}");

        COMMON_BUILDER.pop(3);
    }

    // [[commands]]
    private static CommandsConfig COMMANDS_TABLE;

    static {
        COMMON_BUILDER.comment("Command configurations")
                      .define("commands", new ArrayList<>());
    }

    // [[channels]]
    private static ChannelsConfig CHANNELS_TABLE;

    static {
        COMMON_BUILDER.comment("Channel configurations")
                      .define("channels", new ArrayList<>());
    }

    // Publish configuration
    static {
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    /**
     * Retrieve presence configuration object instances.
     *
     * @return list of PresenceConfig instances (from config tables)
     */
    public static List<PresenceConfig> getPresences()
    {
        return BOT_PRESENCES_TABLE.presences != null ? BOT_PRESENCES_TABLE.presences
                                                     : Collections.emptyList();
    }

    /**
     * Retrieve channel configuration object instances.
     *
     * @return list of ChannelConfig instances (from config tables)
     */
    public static List<ChannelConfig> getChannels()
    {
        return CHANNELS_TABLE.channels != null ? CHANNELS_TABLE.channels
                                               : Collections.emptyList();
    }

    /**
     * Retrieve command configuration object instances.
     *
     * @return list of CommandConfig instances (from config tables)
     */
    public static List<CommandConfig> getCommands()
    {
        return COMMANDS_TABLE.commands != null ? COMMANDS_TABLE.commands
                                               : Collections.emptyList();
    }

    /**
     * Transform configuration data into respective objects.
     *
     * @param configData configuration instance
     */
    public static void transform(CommentedConfig configData)
    {
        BOT_PRESENCES_TABLE = new ObjectConverter().toObject(configData, PresencesConfig::new);
        CHANNELS_TABLE = new ObjectConverter().toObject(configData, ChannelsConfig::new);
        COMMANDS_TABLE = new ObjectConverter().toObject(configData, CommandsConfig::new);
    }

    /**
     * Load the configuration from file.
     *
     * @param spec configuration instance
     * @param path file to be used for loading and saving
     */
    public static void loadConfig(ForgeConfigSpec spec, Path path)
    {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                                                                  .sync()
                                                                  .autosave()
                                                                  .writingMode(WritingMode.REPLACE)
                                                                  .build();

        configData.load();
        spec.setConfig(configData);

        // Handle transformations
        transform(configData);
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfig.ConfigReloading event)
    {
        final ModConfig cfg = event.getConfig();

        // Is this our configuration being reloaded?
        if (!cfg.getModId().equals("mdc")) return;

        // Handle transformations
        transform(cfg.getConfigData());

        // Reconnect the Discord bot if already connected
        final DiscordClient discord = DiscordClient.getInstance();
        if (discord.isReady())
            discord.reconnect(BOT_TOKEN.get());
    }
}
