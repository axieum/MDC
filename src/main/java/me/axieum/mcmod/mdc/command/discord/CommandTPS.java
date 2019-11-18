package me.axieum.mcmod.mdc.command.discord;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.api.DiscordCommand;
import me.axieum.mcmod.mdc.util.DiscordUtils;
import me.axieum.mcmod.mdc.util.ServerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Collections;
import java.util.List;

public class CommandTPS implements DiscordCommand
{
    @Override
    public List<String> getNames()
    {
        List<String> aliases = Config.COMMAND_TPS_ALIASES.get();
        return aliases != null && !aliases.isEmpty() ? aliases
                                                     : Collections.emptyList();
    }

    @Override
    public boolean isAuthorised(Member executor, TextChannel channel)
    {
        // Does the member have permissions? Empty means no check
        List<String> permissions = Config.COMMAND_TPS_PERMISSIONS.get();
        return (permissions == null || permissions.isEmpty()) ||
               DiscordUtils.checkAnyPermission(executor, permissions);
    }

    @Override
    public boolean shouldIgnore(Member executor, TextChannel channel)
    {
        // Can the command be executed from this channel?
        List<Long> channels = Config.COMMAND_TPS_CHANNELS.get();
        return channels != null && !channels.isEmpty() && !channels.contains(channel.getIdLong());
    }

    @Override
    public void execute(Member executor, TextChannel channel, List<String> args)
    {
        // Build and then send an embed for pretty response
        final EmbedBuilder embed = new EmbedBuilder();

        // Add each world TPS entry (given it is wanted)
        final List<Integer> onlyDims = Config.COMMAND_TPS_DIMENSIONS.get();
        for (World world : ServerLifecycleHooks.getCurrentServer().getWorlds()) {
            // Do we want this dimension?
            final DimensionType dim = world.getDimension().getType();
            if (onlyDims != null && !onlyDims.isEmpty() && !onlyDims.contains(dim.getId()))
                continue;

            embed.addField(String.format("Dim %d | %s", dim.getId(), ServerUtils.getDimensionName(dim)),
                           String.format("%.2f TPS @ %.3fms",
                                         ServerUtils.getAverageTPS(dim),
                                         ServerUtils.getAverageTPSTime(dim)),
                           true);
        }

        // Add overall server TPS entry
        final double meanTPS = ServerUtils.getAverageTPS();
        final double meanTPSTime = ServerUtils.getAverageTPSTime();
        embed.addField("Overall", String.format("%.2f TPS @ %.3fms", meanTPS, meanTPSTime), false);

        // Set embed colour based on overall TPS status
        if (meanTPS > 19)
            embed.setColor(65280); // Green
        else if (meanTPS > 15)
            embed.setColor(16776960); // Yellow
        else if (meanTPS > 10)
            embed.setColor(16744448); // Orange
        else
            embed.setColor(16711680); // Red

        // Send it!
        channel.sendMessage(embed.build()).queue();
    }
}
