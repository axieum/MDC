package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EventPlayerAdvancement
{
    @SubscribeEvent
    public static void onPlayerAdvancement(AdvancementEvent event)
    {
        final DisplayInfo info = event.getAdvancement().getDisplay();

        // Do we have advancement info, or should it even announce to chat?
        if (info == null || !info.shouldAnnounceToChat()) return;

        // Fetch useful event information
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();

        // Player name and advancement info
        final String name = event.getEntityLiving().getName().getFormattedText();
        final String title = info.getTitle().getUnformattedComponentText();
        final String description = info.getDescription().getUnformattedComponentText();
        // Advancement progress stats
        String total = "0", obtained = "0";
        double progress = 0;
        if (player.getServer() != null) {
            AdvancementManager advManager = player.getServer().getAdvancementManager();
            int advTotal = advManager.getAllAdvancements().size();
            int playerObtained = 1; // TODO: Get actual player advancement count
            total = String.valueOf(advTotal);
            obtained = String.valueOf(playerObtained);
            progress = playerObtained / (float) advTotal;
        }

        // Format and send messages
        final DiscordClient discord = DiscordClient.getInstance();
        for (ChannelConfig channel : Config.getChannels()) {
            // Fetch the started message format
            String message = channel.getMessages().advancement;
            if (message == null || message.isEmpty()) continue;

            // Handle message substitutions
            message = new MessageFormatter(message)
                    .withDateTime("DATE")
                    .add("PLAYER", name)
                    .add("TITLE", title)
                    .add("DESCRIPTION", description)
                    .add("OBTAINED", obtained)
                    .add("TOTAL", total)
                    .withFormatted("PROGRESS", progress)
                    .toString();

            // Send message
            discord.sendMessage(message, channel.id);
        }
    }
}
