package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.api.ChannelsConfig.ChannelConfig;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import net.minecraft.advancements.DisplayInfo;
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
        final String name = event.getEntityLiving().getName().getFormattedText();
        final String type = info.getFrame().getName();
        final String title = info.getTitle().getUnformattedComponentText();
        final String description = info.getDescription().getUnformattedComponentText();

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
                    .add("TYPE", type)
                    .add("TITLE", title)
                    .add("DESCRIPTION", description)
                    .toString();

            // Send message
            discord.sendMessage(message, channel.id);
        }
    }
}
