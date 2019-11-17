package me.axieum.mcmod.mdc;

import me.axieum.mcmod.mdc.api.DiscordCommand;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiscordClient extends ListenerAdapter
{
    private static DiscordClient instance;
    private static List<Object> listeners = new ArrayList<>();
    private static List<DiscordCommand> commands = new ArrayList<>();
    private JDA api;

    /**
     * Retrieve the static Discord Client instance.
     *
     * @return existing client else new instance
     */
    public static DiscordClient getInstance()
    {
        if (instance == null)
            instance = new DiscordClient();

        return instance;
    }

    /**
     * Retrieve the JDA instance in order to tap into it.
     *
     * @return JDA instance else null if none exists
     */
    public JDA getApi()
    {
        return api;
    }

    /**
     * Check whether the JDA instance is ready.
     *
     * @return true if the JDA instance is built
     */
    public boolean isReady()
    {
        return api != null;
    }

    /**
     * Connect to the Discord Bot with the provided token.
     *
     * @param token  Discord Bot token
     * @param status Initial Discord Bot presence status
     * @return true if the bot successfully connected
     */
    public boolean connect(String token, OnlineStatus status)
    {
        // Are we already connected?
        if (api != null) {
            MDC.LOGGER.info("Discord bot is already connected!");
            return false;
        }

        // Build a new JDA instance and hence connect
        try {
            api = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setStatus(status != null ? status : OnlineStatus.ONLINE)
                    .addEventListeners(this)
                    .addEventListeners(listeners.toArray())
                    .build();

            MDC.LOGGER.info("Discord bot connecting...");
            api.awaitReady(); // synchronous - easier to handle sending starting messages
            return true;
        } catch (Exception e) {
            MDC.LOGGER.error("Unable to connect to the Discord bot: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Disconnect the Discord Bot.
     *
     * @return true if the bot successfully disconnected
     */
    public boolean disconnect()
    {
        // Are we already disconnected?
        if (api == null) {
            MDC.LOGGER.info("Discord bot is already disconnected!");
            return false;
        }

        // Shutdown the JDA instance and hence delete its reference
        api.shutdown();
        api = null;
        MDC.LOGGER.info("Discord bot disconnected");
        return true;
    }

    /**
     * Reconnect the Discord Bot.
     *
     * @param token Discord Bot token
     * @return true if the bot successfully reconnected
     */
    public boolean reconnect(String token)
    {
        MDC.LOGGER.info("Discord bot reconnecting...");

        final OnlineStatus oldStatus = api.getPresence().getStatus();

        // Do we need to disconnect?
        if (api != null)
            disconnect();

        return connect(token, oldStatus);
    }

    /**
     * Add event listeners to the JDA api.
     *
     * @param listeners listener instances
     * @see JDA#addEventListener(Object...)
     */
    public void addEventListeners(Object... listeners)
    {
        DiscordClient.listeners.addAll(Arrays.asList(listeners));
        if (api != null)
            api.addEventListener(listeners);
    }

    /**
     * Add Discord command handlers.
     *
     * @param commands command instances
     */
    public void addCommands(DiscordCommand... commands)
    {
        DiscordClient.commands.addAll(Arrays.asList(commands));
    }

    /**
     * Remove Discord command handlers.
     *
     * @param commands command instances
     */
    public void removeCommands(DiscordCommand... commands)
    {
        DiscordClient.commands.removeAll(Arrays.asList(commands));
    }

    /**
     * Retrieve registered commands.
     *
     * @return immutable list of commands
     */
    public List<DiscordCommand> getCommands()
    {
        return Collections.unmodifiableList(commands);
    }

    /**
     * Send a message to a specific channel(s).
     *
     * @param message  message to be sent
     * @param channels channel(s) to broadcast message to
     */
    public void sendMessage(String message, TextChannel... channels)
    {
        if (!isReady() || message.isEmpty()) return;
        message = message.replaceAll("\\\\n", "\n"); // actual new-line
        for (TextChannel channel : channels) {
            if (channel == null) continue;
            try {
                channel.sendMessage(message).queue();
            } catch (Exception e) {
                MDC.LOGGER.warn("Unable to send message to channel with id {}: {}",
                                channel.getId(),
                                e.getMessage());
            }
        }
    }

    /**
     * Send a message to a specific channel(s).
     *
     * @param message    message to be sent
     * @param channelIds channel id(s) to broadcast message to
     * @see DiscordClient#sendMessage(String, TextChannel...)
     */
    public void sendMessage(String message, long... channelIds)
    {
        if (!isReady() || message.isEmpty()) return;
        // Map channel ids to their TextChannel entities
        TextChannel[] channels = Arrays.stream(channelIds)
                                       .mapToObj(id -> api.getTextChannelById(id))
                                       .toArray(TextChannel[]::new);
        sendMessage(message, channels);
    }

    /**
     * On ready event, mark the instance as ready.
     *
     * @param event JDA ready event
     */
    @Override
    public void onReady(@Nonnull ReadyEvent event)
    {
        super.onReady(event);

        // Register listeners
        final List<Object> registered = api.getRegisteredListeners();
        for (Object listener : listeners)
            if (!registered.contains(listener))
                api.addEventListener(listener);

        MDC.LOGGER.info("Logged into Discord as {}", api.getSelfUser().getAsTag());
    }
}
