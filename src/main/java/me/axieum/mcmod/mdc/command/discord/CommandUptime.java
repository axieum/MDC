package me.axieum.mcmod.mdc.command.discord;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.api.DiscordCommand;
import me.axieum.mcmod.mdc.util.DiscordUtils;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import me.axieum.mcmod.mdc.util.ServerUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class CommandUptime implements DiscordCommand
{
    @Override
    public List<String> getNames()
    {
        return Config.COMMAND_UPTIME_ALIASES.get();
    }

    @Override
    public boolean isAuthorised(Member executor, TextChannel channel)
    {
        // Does the member have permissions? Empty means no check
        List<String> permissions = Config.COMMAND_UPTIME_PERMISSIONS.get();
        return (permissions == null || permissions.isEmpty()) ||
               DiscordUtils.checkAnyPermission(executor, permissions);
    }

    @Override
    public boolean shouldIgnore(Member executor, TextChannel channel)
    {
        // Can the command be executed from this channel?
        List<Long> channels = Config.COMMAND_UPTIME_CHANNELS.get();
        return channels != null && !channels.isEmpty() && !channels.contains(channel.getIdLong());
    }

    @Override
    public void execute(Member executor, TextChannel channel, List<String> args)
    {
        final String format = Config.COMMAND_UPTIME_FORMAT.get();
        if (format == null || format.isEmpty()) return;

        // Prepare formatter
        final MessageFormatter formatter = new MessageFormatter()
                .add("MENTION", executor.getAsMention())
                .addDuration("UPTIME", ServerUtils.getUptime());

        // Send it!
        channel.sendMessage(formatter.apply(format)).queue();
    }
}
