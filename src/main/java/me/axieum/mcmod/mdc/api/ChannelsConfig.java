package me.axieum.mcmod.mdc.api;

import java.util.Arrays;
import java.util.List;

/**
 * Channels configuration schema wrapper.
 */
public class ChannelsConfig
{
    public List<ChannelConfig> channels;

    public ChannelsConfig() {}

    /**
     * Channel configuration schema.
     */
    public static class ChannelConfig
    {
        public long id;
        public List<Integer> dimensions;
        public Messages messages;

        public ChannelConfig() {}

        @Override
        public String toString()
        {
            return String.format("ChannelConfig {id: %d, dimensions: %s, messages: %s}",
                                 id,
                                 Arrays.toString(dimensions.toArray()),
                                 messages.toString());
        }

        /**
         * Messages configuration schema.
         */
        public static class Messages
        {
            public String join,
                    leave,
                    death,
                    advancement,
                    started,
                    stopping,
                    stopped;

            public Messages() {}

            @Override
            public String toString()
            {
                return String.format(
                        "Messages {join %s, leave: %s, death: %s, advancement: %s, started: %s, stopping: %s, stopped: %s}",
                        join,
                        leave,
                        death,
                        advancement,
                        started,
                        stopping,
                        stopped);
            }
        }
    }
}
