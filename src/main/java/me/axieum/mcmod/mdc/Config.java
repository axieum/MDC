package me.axieum.mcmod.mdc;

import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import me.axieum.mcmod.mdc.api.ChannelConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config
{
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;

    private static final String CATEGORY_GENERAL = "general";
    public static ForgeConfigSpec.ConfigValue<String> BOT_TOKEN;

    private static final String CATEGORY_CHANNEL = "channel";
    public static ForgeConfigSpec.ConfigValue<List<List<ChannelConfig>>> CHANNELS;

    // Define configuration schema
    static {
        // GENERAL
        COMMON_BUILDER.comment("General configuration").push(CATEGORY_GENERAL);

        BOT_TOKEN = COMMON_BUILDER.comment("Discord Bot Token")
                                  .worldRestart()
                                  .define("bot.token", "");

        COMMON_BUILDER.pop();

        // CHANNELS
        CHANNELS = COMMON_BUILDER.comment("Channel configurations")
                                 .define(CATEGORY_CHANNEL, new ArrayList<>());

        // Publish config
        COMMON_CONFIG = COMMON_BUILDER.build();
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

        ChannelsConfig ccs = new ObjectConverter().toObject(configData, ChannelsConfig::new);
        // TODO: Figure out how to put this `ccs.channels` list into the CHANNELS ConfigValue
        ccs.channels.forEach(c -> MDC.LOGGER.debug(c.toString()));
    }

    public static class ChannelsConfig
    {
        public List<ChannelConfig> channels;

        public ChannelsConfig() {}
    }
}
