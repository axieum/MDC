package me.axieum.mcmod.mdc.event.discord;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.api.CommandsConfig;
import me.axieum.mcmod.mdc.api.DiscordCommand;
import me.axieum.mcmod.mdc.command.MDCCommandSender;
import me.axieum.mcmod.mdc.util.MessageFormatter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventCommand
{
    /**
     * Handle a Discord command message event.
     *
     * @param event Discord message received event
     * @return true if a command was executed
     */
    public static boolean onCommandMessage(@Nonnull MessageReceivedEvent event)
    {
        // Is this a command - starts with prefix?
        // NB: bailing out as soon as possible saves us from checking every
        // registered command's names
        final String body = event.getMessage().getContentRaw();
        if (!body.startsWith(Config.COMMAND_PREFIX.get())) return false;

        // Fetch relevant information
        final DiscordClient discord = DiscordClient.getInstance();
        final Member member = event.getMember();
        final TextChannel channel = event.getTextChannel();


        final List<String> args = new ArrayList<>(Arrays.asList(body.split(" ")));
        final String cmd = args.remove(0).substring(Config.COMMAND_PREFIX.get().length());

        final String unauthorisedMsg = new MessageFormatter()
                .add("MENTION", event.getAuthor().getAsMention())
                .add("COMMAND", body.trim())
                .format(Config.COMMAND_UNAUTHORISED.get());

        // Keep track of whether we handled a command or not
        boolean isCommand = false;

        // Attempt to match this command with its registered handler(s)
        for (DiscordCommand command : DiscordClient.getInstance().getCommands()) {
            if (command.getNames().stream().noneMatch(name -> name.equalsIgnoreCase(cmd)))
                continue;

            isCommand = true;
            if (command.isAuthorised(member, channel))
                command.execute(member, channel, args);
            else
                discord.sendMessage(unauthorisedMsg, channel);
        }

        // Attempt to match this command with a Minecraft proxy configuration entry(s)
        for (CommandsConfig.CommandConfig command : Config.getCommands()) {
            if (!command.isEnabled()) continue;
            if (command.shouldIgnore(member, channel)) continue;
            if (command.getEffectiveNames().stream().noneMatch(name -> name.equalsIgnoreCase(cmd)))
                continue;

            isCommand = true;
            if (command.isAuthorised(member, channel))
                proxyMinecraftCommand(member, channel, command.getCommand(), args);
            else
                discord.sendMessage(unauthorisedMsg, channel);
        }

        return isCommand;
    }

    /**
     * Proxy a Minecraft command and fetch the result of the command.
     *
     * @param member  Discord member whom is executing
     * @param channel Discord channel command issued from
     * @param command command template/format
     * @param args    command arguments (i.e. "/whitelist add" -> ["whitelist", "add"])
     */
    private static void proxyMinecraftCommand(Member member, TextChannel channel, String command, List<String> args)
    {
        // Replace placeholders (i.e. "{{0}}" -> argument #1)
        for (int i = 0; i < args.size(); i++)
            command = command.replaceAll("\\{\\{" + i + "}}", args.get(i));

        // Replace "{{}}" with all arguments
        command = command.replaceAll("\\{\\{}}", String.join(" ", args));

        // Replace all left over placeholders
        command = command.replaceAll("\\{\\{\\d*}}", "").trim();

        // Do we have permission to execute commands on the server?
        final MDCCommandSender sender = new MDCCommandSender(member, channel);
        if (!sender.hasPermissionLevel(4)) {
            MDC.LOGGER.warn("MDC (uuid={}) does not have sufficient permission level (4) to execute commands.",
                            sender.getUniqueID().toString());
            return;
        }

        // Execute command
        try {
            ServerLifecycleHooks.getCurrentServer()
                                .getCommandManager()
                                .getDispatcher()
                                .execute(command, sender.getCommandSource());
            MDC.LOGGER.error("Discord command proxied successfully: '/{}'", command);
        } catch (CommandSyntaxException e) {
            MDC.LOGGER.error("Discord command proxy failed for '/{}': {}", command, e.getRawMessage().getString());
            channel.sendMessage(":warning: " + e.getRawMessage().getString()).queue();
        }
    }
}
