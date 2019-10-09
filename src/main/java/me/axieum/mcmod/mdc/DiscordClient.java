package me.axieum.mcmod.mdc;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class DiscordClient extends ListenerAdapter
{
    private static DiscordClient instance;
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
     * @param token Discord Bot token
     * @return true if the bot successfully connected
     */
    public boolean connect(String token)
    {
        // Are we already connected?
        if (api != null) {
            MDC.LOGGER.debug("Discord bot is already connected!");
            return false;
        }

        // Build a new JDA instance and hence connect
        try {
            api = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setStatus(OnlineStatus.ONLINE)
                    .addEventListeners(this)
                    .build();

            MDC.LOGGER.debug("Discord bot connecting...");
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
            MDC.LOGGER.debug("Discord bot is already disconnected!");
            return false;
        }

        // Shutdown the JDA instance and hence delete its reference
        api.shutdown();
        api = null;
        MDC.LOGGER.debug("Discord bot disconnected");
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
        // Do we need to disconnect?
        if (api != null)
            disconnect();

        MDC.LOGGER.debug("Discord bot reconnecting...");

        return connect(token);
    }

    /**
     * Send a message to a specific channel(s).
     *
     * @param message  message to be sent
     * @param channels channel(s) to broadcast message to
     */
    public void sendMessage(String message, TextChannel... channels)
    {
        if (message.isEmpty() || !isReady()) return;
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
        if (message.isEmpty() || !isReady()) return;
        for (long channelId : channelIds)
            sendMessage(message, api.getTextChannelById(channelId));
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

        MDC.LOGGER.info("Logged into Discord as {}", api.getSelfUser().getAsTag());
    }
}