package me.axieum.mcmod.mdc.event.minecraft;

import me.axieum.mcmod.mdc.util.DiscordUtils;
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
        final int dimensionId = event.getEntityLiving().dimension.getId();
        final String name = event.getEntityLiving().getName().getFormattedText();
        final String type = info.getFrame().getName();
        final String title = info.getTitle().getUnformattedComponentText();
        final String description = info.getDescription().getUnformattedComponentText();

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .addDateTime("DATETIME")
                .add("PLAYER", name)
                .add("TYPE", type)
                .add("TITLE", title)
                .add("DESCRIPTION", description);

        // Dispatch structured message
        DiscordUtils.sendMessagesFromMinecraft(formatter, "advancement", dimensionId);
    }
}
