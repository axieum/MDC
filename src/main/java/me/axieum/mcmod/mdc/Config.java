package me.axieum.mcmod.mdc;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import me.axieum.mcmod.mdc.api.ChannelsConfig;
import me.axieum.mcmod.mdc.api.CommandsConfig;
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
    public static ForgeConfigSpec.ConfigValue<String> BOT_TOKEN;

    private static final String CATEGORY_COMMANDS = "commands";
    private static CommandsConfig COMMANDS_TABLE;

    private static final String CATEGORY_CHANNEL = "channels";
    private static ChannelsConfig CHANNELS_TABLE;

    // Define configuration schema
    static {
        // GENERAL
        COMMON_BUILDER.comment("General configuration").push(CATEGORY_GENERAL);

        BOT_TOKEN = COMMON_BUILDER.comment("Discord Bot Token")
                                  .define("bot.token", "");
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
        if (!cfg.getModId().equals("mdc"))
            return;

        // Handle transformations
        transform(cfg.getConfigData());

        // Did the bot token update?
        final String token = BOT_TOKEN.get();
        final DiscordClient dc = DiscordClient.getInstance();
        if (dc.isReady() && !dc.getApi().getToken().equals(token))
            dc.reconnect(token);
    }
}
