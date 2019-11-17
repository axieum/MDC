package me.axieum.mcmod.mdc.util;

import com.vdurmont.emoji.EmojiParser;
import me.axieum.mcmod.mdc.Config;
import me.axieum.mcmod.mdc.DiscordClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.regex.Pattern;

public class StringUtils
{
    private static final MessageFormatter FORMATTER_DC_MC, FORMATTER_MC_DC;

    // Prepare formatters
    static {
        FORMATTER_DC_MC = new MessageFormatter()
                // Translate italics, bold, strikethrough and underline to Minecraft codes
                .add(Pattern.compile("__(.+?)__"), g -> "\u00A7n" + g.get(1) + "\u00A7r")
                .add(Pattern.compile("_(.+?)_"), g -> "\u00A7o" + g.get(1) + "\u00A7r")
                .add(Pattern.compile("\\*\\*(.+?)\\*\\*"), g -> "\u00A7l" + g.get(1) + "\u00A7r")
                .add(Pattern.compile("\\*(.+?)\\*"), g -> "\u00A7o" + g.get(1) + "\u00A7r")
                .add(Pattern.compile("~~(.+?)~~"), g -> "\u00A7m" + g.get(1) + "\u00A7r")
                // TODO: Turn spoilers into "on hover" tooltip - rather than immediately in chat
                .add(Pattern.compile("\\|\\|(.+?)\\|\\|"), g -> "\u00A7k" + g.get(1) + "\u00A7r")
                // TODO: Turn code block into "on hover" tooltip - rather than spam chat?
                .add(Pattern.compile("```(\\w*)\\n(.*?)\\n?```"),
                     g -> "(" + g.get(1) + ") \u00A79" + g.get(2) + "\u00A7r")
                .add(Pattern.compile("```(.*?)```"), g -> "\u00A77" + g.get(1) + "\u00A7r")
                .add(Pattern.compile("`(.*?)`"), g -> "\u00A78" + g.get(1) + "\u00A7r");

        FORMATTER_MC_DC = new MessageFormatter()
                // Escape special characters
                .add(Pattern.compile("\\\\n"), "")
                // Translate italics, bold, strikethrough and underline to markdown
                .add(Pattern.compile("(?<=[\u00A7]n)(.+?)(?=\\s?[\u00A7]r|$)"), g -> "__" + g.get(1) + "__")
                .add(Pattern.compile("(?<=[\u00A7]o)(.+?)(?=\\s?[\u00A7]r|$)"), g -> "_" + g.get(1) + "_")
                .add(Pattern.compile("(?<=[\u00A7]l)(.+?)(?=\\s?[\u00A7]r|$)"), g -> "**" + g.get(1) + "**")
                .add(Pattern.compile("(?<=[\u00A7]m)(.+?)(?=\\s?[\u00A7]r|$)"), g -> "~~" + g.get(1) + "~~")
                // Handle @mentions
                .addReplacement("@everyone", "@_everyone_") // suppress @everyone
                .addReplacement("@here", "@_here_") // suppress @here
                .add(Pattern.compile("@([^\\s]+)"), groups -> {
                    // Attempt to match mention to a Discord member from all Guilds
                    // NB: We have to use Guilds as nicknames only exist of Members
                    //     which in turn only exist in Guilds.
                    final JDA discord = DiscordClient.getInstance().getApi();
                    if (discord == null) return groups.get(0); // No Discord api, no change

                    final String lookup = groups.get(1);
                    Member member = discord.getGuilds()
                                           .stream()
                                           .flatMap(guild -> guild.getMembersByEffectiveName(lookup, true).stream())
                                           .findFirst().orElse(null);
                    // If we found a member, mention them, else don't touch it
                    return member != null ? member.getAsMention() : groups.get(0);
                })
                // Handle #channels
                .add(Pattern.compile("#([^\\s]+)"), groups -> {
                    // Attempt to match a channel to a Discord channel
                    final JDA discord = DiscordClient.getInstance().getApi();
                    if (discord == null) return groups.get(0); // No Discord api, no change

                    List<TextChannel> channels = discord.getTextChannelsByName(groups.get(1), true);
                    if (channels.size() < 1) return groups.get(0); // no channels, don't change anything
                    return channels.get(0).getAsMention(); // try to mention first channel
                })
                // Strip left over formatting codes and return
                .add(Pattern.compile("[\u00A7][0-9a-fk-or]"), "");
    }

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
        return FORMATTER_MC_DC.apply(message);
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
        String formatted = FORMATTER_DC_MC.apply(message);

        // Handle emoji -> words
        return Config.EMOJI_TRANSLATION.get() ? EmojiParser.parseToAliases(formatted) : formatted;
    }
}
