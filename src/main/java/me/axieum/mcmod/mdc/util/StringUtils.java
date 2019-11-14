package me.axieum.mcmod.mdc.util;

import com.vdurmont.emoji.EmojiParser;
import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.regex.Pattern;

public class StringUtils
{
    /**
     * Capitalize first letter in each word in a string.
     *
     * @param str        string
     * @param delimiters characters replaced with spaces
     * @return string as a title
     */
    public static String strToTitle(String str, char... delimiters)
    {
        // Replace all delimiters with spaces
        for (char delimiter : delimiters)
            str = str.replace(delimiter, ' ');

        char[] chars = str.toCharArray();
        boolean capitalizeNext = true;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ' ') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                chars[i] = Character.toTitleCase(chars[i]);
                capitalizeNext = false;
            }
        }

        return new String(chars);
    }

    /**
     * Translate a Minecraft formatted message to Discord formatting.
     *
     * @param message message (e.g. with colour codes, etc.)
     * @return message ready for Discord
     */
    public static String mcToDiscord(String message)
    {
        return new MessageFormatter()
                // Translate italics and bold to markdown
                .add(Pattern.compile("(?<=[\u00A7]o)(.+?)(?=\\s?[\u00A7]r|$)"), "_$1_")
                .add(Pattern.compile("(?<=[\u00A7]l)(.+?)(?=\\s?[\u00A7]r|$)"), "**$1**")
                // Handle @mentions
                .add(Pattern.compile("@([A-Za-z0-9\\-_()\\[\\]]+)"), groups -> {
                    // Attempt to match mention to a Discord user
                    List<User> users = DiscordClient.getInstance().getApi().getUsersByName(groups.get(1), true);
                    if (users.size() < 1) return groups.get(0); // no users, don't change anything
                    return users.get(0).getAsMention(); // try to mention first user
                })
                // Handle #channels
                .add(Pattern.compile("#([A-Za-z0-9\\-_]+)"), groups -> {
                    // Attempt to match a channel to a Discord channel
                    List<TextChannel> channels = DiscordClient.getInstance()
                                                              .getApi()
                                                              .getTextChannelsByName(groups.get(1), true);
                    if (channels.size() < 1) return groups.get(0); // no channels, don't change anything
                    return channels.get(0).getAsMention(); // try to mention first channel
                })
                // Strip left over formatting codes and return
                .add(Pattern.compile("[\u00A7][0-9a-fk-or]"), "")
                .apply(message);
    }

    /**
     * Translate a Discord formatted message to Minecraft formatting.
     *
     * @param message message (e.g. with markdown formatting)
     * @return message ready for Minecraft
     */
    public static String discordToMc(String message)
    {
        // Handle markdown formatting
        String formatted = new MessageFormatter()
                // Translate italics and bold to Minecraft codes
                .add(Pattern.compile("_(.+?)_"), "\u00A7o$1\u00A7r")
                .add(Pattern.compile("\\*\\*(.+?)\\*\\*"), "\u00A7l$1\u00A7r")
                .add(Pattern.compile("\\*(.+?)\\*"), "\u00A7o$1\u00A7r")
                // TODO: Turn spoilers into "on hover" tooltip - rather than immediately in chat
                .add(Pattern.compile("\\|\\|(.+?)\\|\\|"), "\u00A7k$1\u00A7r")
                // TODO: Turn code block into "on hover" tooltip - rather than spam chat?
                .add(Pattern.compile("```(\\w*)\\n(.*?)\\n?```"), "($1) \u00A79$2\u00A7r")
                .add(Pattern.compile("```(.*?)```"), "\u00A77$1\u00A7r")
                .add(Pattern.compile("`(.*?)`"), "\u00A78$1\u00A7r")
                .apply(message);

        // Handle emoji -> words
        return Config.EMOJI_TRANSLATION.get() ? EmojiParser.parseToAliases(formatted) : formatted;
    }
}
