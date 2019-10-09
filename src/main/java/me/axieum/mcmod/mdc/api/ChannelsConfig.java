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
         * Retrieve the channel's messages instance or create a new instance
         * if it is not set.
         *
         * @return existing messages or new instance
         */
        public Messages getMessages()
        {
            if (messages == null)
                messages = new Messages();
            return messages;
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
                    stopped,
                    crashed;
        }
    }
}
