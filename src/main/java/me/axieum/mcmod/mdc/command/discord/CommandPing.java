package me.axieum.mcmod.mdc.command.discord;

import me.axieum.mcmod.mdc.api.DiscordCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Arrays;
import java.util.List;

public class CommandPing implements DiscordCommand
{
    @Override
    public List<String> getNames()
    {
        return Arrays.asList("ping");
    }

    @Override
    public boolean isAuthorised(Member executor, TextChannel channel)
    {
        return true;
    }

    @Override
    public void execute(Member executor, TextChannel channel, List<String> args)
    {
        channel.sendMessage("pong!").queue();
    }
}
