package me.axieum.mcmod.mdc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;

@Mod.EventBusSubscriber
public class Config
{
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;

    private static final String CATEGORY_GENERAL = "general";
    public static ForgeConfigSpec.ConfigValue<String> BOT_TOKEN;

    // Define configuration schema
    static {
        // GENERAL
        COMMON_BUILDER.comment("General configuration").push(CATEGORY_GENERAL);

        BOT_TOKEN = COMMON_BUILDER.comment("Discord Bot Token")
                                  .worldRestart()
                                  .define("bot.token", "");

        COMMON_BUILDER.pop();

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
    }
}
