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
        public MinecraftMessages minecraft;
        public DiscordMessages discord;

        /**
         * Retrieve the channel's Minecraft Messages instance or create a new instance
         * if it is not set.
         *
         * @return existing messages or new instance
         */
        public MinecraftMessages getMCMessages()
        {
            if (minecraft == null)
                minecraft = new MinecraftMessages();
            return minecraft;
        }

        /**
         * Retrieve the channel's Discord Messages instance or create a new instance
         * if it is not set.
         *
         * @return existing messages or new instance
         */
        public DiscordMessages getDiscordMessages()
        {
            if (discord == null)
                discord = new DiscordMessages();
            return discord;
        }

        /**
         * Minecraft Messages configuration schema.
         */
        public static class MinecraftMessages
        {
            public String join,
                    leave,
                    death,
                    advancement,
                    chat,
                    started,
                    stopping,
                    stopped,
                    crashed;
        }

        /**
         * Discord Messages configuration schema.
         */
        public static class DiscordMessages
        {
            public String chat, react, unreact, edit;
        }
    }
}
