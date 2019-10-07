package me.axieum.mcmod.mdc.api;

import java.util.List;

/**
 * Channels configuration schema wrapper.
 */
public class ChannelsConfig
{
    public List<ChannelConfig> channels;

    /**
     * Channel configuration schema.
     */
    public static class ChannelConfig
    {
        public long id;
        public List<Integer> dimensions;
        public Messages messages;

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
        }
    }
}
