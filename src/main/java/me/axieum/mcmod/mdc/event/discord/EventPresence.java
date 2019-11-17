package me.axieum.mcmod.mdc.event.discord;

import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.MDC;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import javax.annotation.Nonnull;
import java.util.Timer;
import java.util.TimerTask;

public class EventPresence implements EventListener
{
    private static Timer timer;

    @Override
    public void onEvent(@Nonnull GenericEvent event)
    {
        final JDA jda = event.getJDA();

        if (event instanceof ReadyEvent)
            init(jda);
        else if (event instanceof ShutdownEvent)
            shutdown(jda);
    }

    /**
     * Initialises the Presence Update task.
     *
     * @param jda JDA discord api instance
     */
    public static void init(final JDA jda)
    {
        // Shutdown any existing timer
        shutdown(jda);

        // Do we have presence entries to update?
        if (Config.getPresences().size() < 1) {
            MDC.LOGGER.info("Skipping bot presence updates - none configured");
            return;
        }

        // Prepare our new timer
        timer = new Timer("Timer-MDC-Presence", true);

        // Register scheduled task (repeat on interval)
        final long interval = Config.BOT_PRESENCE_INTERVAL.get() * 1000;
        try {
            timer.schedule(new PresenceUpdateTask(jda), 0, interval);
        } catch (IllegalStateException e) {
            MDC.LOGGER.error("Unable to schedule bot presence updates", e);
        }
    }

    /**
     * Cancels the Presence Update task.
     *
     * @param jda JDA discord api instance
     */
    public static void shutdown(final JDA jda)
    {
        if (timer == null) return;
        timer.cancel();
        jda.getPresence().setActivity(null); // reset activity
    }

    public static class PresenceUpdateTask extends TimerTask
    {
        private final JDA discord;
        private int index = 0;

        /**
         * Constructs a new Presence Update Task with the given JDA api.
         *
         * @param jda JDA instance
         */
        public PresenceUpdateTask(JDA jda)
        {
            this.discord = jda;
        }

        @Override
        public void run()
        {
//            MDC.LOGGER.trace("Updating Discord bot presence");
            try {
                // Fetch and update this iteration's presence activity
                // NB: If the activity is null, this resets the presence
                discord.getPresence().setActivity(Config.getPresences().get(index).getActivity());
            } catch (Exception e) {
                MDC.LOGGER.warn("Unable to update bot presence: {}", e.getMessage());
            } finally {
                index = ++index % Config.getPresences().size(); // rotate presence index
            }
        }
    }
}
