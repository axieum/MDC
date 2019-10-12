package me.axieum.mcmod.mdc.event.discord;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.MDC;
import me.axieum.mcmod.mdc.api.CommandsConfig;
import me.axieum.mcmod.mdc.api.DiscordCommand;
import me.axieum.mcmod.mdc.command.MDCCommandSender;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventCommand
{
    public static boolean onCommandMessage(@Nonnull MessageReceivedEvent event)
    {
        // Fetch relevant information
        final Member member = event.getMember();
        final TextChannel channel = event.getTextChannel();
        final String body = event.getMessage().getContentRaw();

        final List<String> args = new ArrayList<>(Arrays.asList(body.split(" ")));
        final String prefix = args.remove(0);

        // Keep track of whether we handled a command or not
        boolean isCommand = false;

        // Attempt to match this command with its registered handler(s)
        for (DiscordCommand command : DiscordClient.getInstance().getCommands())
            if (command.getNames().stream().anyMatch(name -> name.equalsIgnoreCase(prefix)))
                if (command.isAuthorised(member, channel)) {
                    command.execute(member, channel, args);
                    isCommand = true;
                }

        // Attempt to match this command with a Minecraft proxy configuration entry(s)
        for (CommandsConfig.CommandConfig command : Config.getCommands())
            if (command.isEnabled())
                if (command.getEffectiveNames().stream().anyMatch(name -> name.equalsIgnoreCase(prefix)))
                    if (command.isAuthorised(member, channel)) {
                        proxyMinecraftCommand(new MDCCommandSender(member, channel),
                                              command.getCommand(),
                                              args);
                        isCommand = true;
                    }

        return isCommand;
    }

    /**
     * Proxy a Minecraft command and fetch the result of the command.
     *
     * @param sender  command sender Minecraft entity
     * @param command command template/format
     * @param args    command arguments (i.e. "/whitelist add" -> ["whitelist", "add"])
     */
    private static void proxyMinecraftCommand(ServerPlayerEntity sender, String command, List<String> args)
    {
        // Replace placeholders (i.e. "{{0}}" -> argument #1)
        for (int i = 0; i < args.size(); i++)
            command = command.replaceAll("\\{\\{" + i + "}}", args.get(i));

        // Replace "{{}}" with all arguments
        command = command.replaceAll("\\{\\{}}", String.join(" ", args));

        // Execute command
        if (ServerLifecycleHooks.getCurrentServer()
                                .getCommandManager()
                                .handleCommand(sender.getCommandSource(), command) == 0)
            MDC.LOGGER.error("Discord command proxy failed: '/{}'", command);
        MDC.LOGGER.error("Discord command proxied successfully: '/{}'", command);
    }
}
