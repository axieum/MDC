package me.axieum.mcmod.mdc;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import me.axieum.mcmod.mdc.api.ChannelsConfig;
import me.axieum.mcmod.mdc.api.CommandsConfig;
import me.axieum.mcmod.mdc.api.PresencesConfig;
import net.dv8tion.jda.api.OnlineStatus;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;

    private static final String CATEGORY_GENERAL = "general";
    public static ForgeConfigSpec.ConfigValue<String> BOT_TOKEN, COMMAND_PREFIX, COMMAND_UNAUTHORISED;
    public static ForgeConfigSpec.EnumValue<OnlineStatus> BOT_STATUS_STARTING, BOT_STATUS_STARTED, BOT_STATUS_STOPPING,
            BOT_STATUS_STOPPED;
    public static ForgeConfigSpec.LongValue BOT_PRESENCE_INTERVAL;
    private static PresencesConfig BOT_PRESENCES_TABLE;
    public static ForgeConfigSpec.BooleanValue EMOJI_TRANSLATION;

    private static final String CATEGORY_COMMANDS = "commands";
    private static CommandsConfig COMMANDS_TABLE;

    private static final String CATEGORY_CHANNEL = "channels";
    private static ChannelsConfig CHANNELS_TABLE;

    // Define configuration schema
    static {
        // GENERAL
        COMMON_BUILDER.comment("General configuration").push(CATEGORY_GENERAL);

        BOT_TOKEN = COMMON_BUILDER.comment("Discord bot token")
                                  .define("bot.token", "");

        BOT_STATUS_STARTING = COMMON_BUILDER.comment("Bot status on server starting")
                                            .defineEnum("bot.status.starting", OnlineStatus.IDLE);
        BOT_STATUS_STARTED = COMMON_BUILDER.comment("Bot status on server started")
                                           .defineEnum("bot.status.started", OnlineStatus.ONLINE);
        BOT_STATUS_STOPPING = COMMON_BUILDER.comment("Bot status on server stopping")
                                            .defineEnum("bot.status.stopping", OnlineStatus.DO_NOT_DISTURB);
        BOT_STATUS_STOPPED = COMMON_BUILDER.comment("Bot status on server stopped")
                                           .defineEnum("bot.status.stopped", OnlineStatus.OFFLINE);

        BOT_PRESENCE_INTERVAL = COMMON_BUILDER.comment("Bot presence update interval (in seconds)")
                                              .defineInRange("bot.presence_interval", 30, 12, Long.MAX_VALUE);

        COMMON_BUILDER.comment("Presence definitions")
                      .define("bot.presences", new ArrayList<>()); // presences table

        EMOJI_TRANSLATION = COMMON_BUILDER.comment("Should unicode emojis be translated to word form (i.e. ':emoji:')")
                                          .define("chat.emoji_translation", true);

        COMMAND_PREFIX = COMMON_BUILDER.comment("Discord command prefix")
                                       .define("commands.prefix", "!");

        COMMAND_UNAUTHORISED = COMMON_BUILDER.comment("Discord command unauthorised message")
                                             .define("commands.unauthorised",
                                                     "{{MENTION}}, you were unauthorised to perform this action :no_good:");

        COMMON_BUILDER.pop();

        // COMMANDS
        COMMON_BUILDER.comment("Command configurations") // commands table
                      .define(CATEGORY_COMMANDS, new ArrayList<>());

        // CHANNELS
        COMMON_BUILDER.comment("Channel configurations") // channels table
                      .define(CATEGORY_CHANNEL, new ArrayList<>());

        // Publish config
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    /**
     * Retrieve presence configuration object instances.
     *
     * @return list of PresenceConfig instances (from config tables)
     */
    public static List<PresencesConfig.PresenceConfig> getPresences()
    {
        return BOT_PRESENCES_TABLE.presences != null ? BOT_PRESENCES_TABLE.presences
                                                     : Collections.emptyList();
    }

    /**
     * Retrieve channel configuration object instances.
     *
     * @return list of ChannelConfig instances (from config tables)
     */
    public static List<ChannelsConfig.ChannelConfig> getChannels()
    {
        return CHANNELS_TABLE.channels != null ? CHANNELS_TABLE.channels
                                               : Collections.emptyList();
    }

    /**
     * Retrieve command configuration object instances.
     *
     * @return list of CommandConfig instances (from config tables)
     */
    public static List<CommandsConfig.CommandConfig> getCommands()
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
