package me.axieum.mcmod.mdc;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class DiscordClient extends ListenerAdapter
{
    private static DiscordClient instance;
    private JDA jda;

    // JDA client ready status - has it connected yet?
    private boolean ready;

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
    public JDA getJda()
    {
        return jda;
    }

    /**
     * Check whether the JDA instance is ready.
     *
     * @return true if the JDA instance is built
     */
    public boolean isReady()
    {
        return ready && jda != null;
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
        if (jda != null) {
            MDC.LOGGER.debug("Discord bot is already connected!");
            return false;
        }

        // Build a new JDA instance and hence connect
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .setStatus(OnlineStatus.IDLE)
                    .addEventListeners(this)
                    .build(); // asynchronous - need to listen for ready event

            MDC.LOGGER.debug("Discord bot connecting...");
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
        if (jda == null) {
            MDC.LOGGER.debug("Discord bot is already disconnected!");
            return false;
        }

        // Shutdown the JDA instance and hence delete its reference
        jda.shutdown();
        jda = null;
        ready = false;
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
        if (jda != null)
            disconnect();

        MDC.LOGGER.debug("Discord bot reconnecting...");

        return connect(token);
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

        MDC.LOGGER.info("Logged into Discord as {}", jda.getSelfUser().getAsTag());
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        ready = true;
    }
}
