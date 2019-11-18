package me.axieum.mcmod.mdc.api;

import me.axieum.mcmod.mdc.util.DiscordUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * Commands configuration schema wrapper.
 */
public class CommandsConfig
{
    public List<CommandConfig> commands;

    /**
     * Command configuration schema.
     */
    public static class CommandConfig
    {
        public String name, description, command;
        public boolean enabled = true, quiet = false;
        public List<String> aliases, permissions;
        public List<Long> channels;

        /**
         * Retrieve the command permissions.
         *
         * @return list of command permissions
         */
        public List<String> getPermissions()
        {
            if (permissions == null)
                permissions = new ArrayList<>();
            return permissions;
        }

        /**
         * Retrieve the command channels.
         *
         * @return list of command channels
         */
        public List<Long> getChannels()
        {
            if (channels == null)
                channels = new ArrayList<>();
            return channels;
        }

        /**
         * Get command name.
         *
         * @return command name
         */
        public String getName()
        {
            return name;
        }

        /**
         * Get command description.
         *
         * @return command description
         */
        public String getDescription()
        {
            return description;
        }

        /**
         * Retrieve the command aliases.
         *
         * @return list of command aliases
         */
        public List<String> getAliases()
        {
            if (aliases == null)
                aliases = new ArrayList<>();
            return aliases;
        }

        /**
         * Retrieve the command name and aliases.
         *
         * @return list of effective names
         */
        public List<String> getEffectiveNames()
        {
            List<String> aliases = new ArrayList<>(getAliases());
            aliases.add(name);
            return aliases;
        }

        /**
         * Get command to execute.
         *
         * @return command to execute
         */
        public String getCommand()
        {
            return command;
        }

        /**
         * Check if command is enabled.
         *
         * @return true if command is enabled
         */
        public boolean isEnabled()
        {
            return enabled;
        }

        /**
         * Check if command should output response.
         *
         * @return true if command is quiet
         */
        public boolean isQuiet()
        {
            return quiet;
        }

        /**
         * Check whether a user and channel combination should be ignored.
         *
         * @param member  member whom is executing
         * @param channel text channel command was issued from
         * @return true if the command should execute
         */
        public boolean shouldIgnore(Member member, TextChannel channel)
        {
            // Check channels
            if (getChannels().isEmpty()) return false;
            return !getChannels().contains(channel.getIdLong());
        }

        /**
         * Check whether a user in a channel is authorised to execute this
         * command.
         *
         * @param member  member whom is executing
         * @param channel text channel command was issued from
         * @return true if the command should execute
         */
        public boolean isAuthorised(Member member, TextChannel channel)
        {
            if (member == null || channel == null) return false;
            if (getPermissions().isEmpty()) return true;

            return DiscordUtils.checkAnyPermission(member, getPermissions());
        }
    }
}
