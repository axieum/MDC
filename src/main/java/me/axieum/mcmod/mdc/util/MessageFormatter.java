package me.axieum.mcmod.mdc.util;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageFormatter
{
    private LinkedHashMap<Pattern, String> literals = new LinkedHashMap<>();
    private LinkedHashMap<Pattern, TokenReplacer> functional = new LinkedHashMap<>();

    /**
     * Constructs a new Message Formatter instance.
     */
    public MessageFormatter() {}

    /**
     * Adds common datetime replacements.
     *
     * @param token    token name
     * @param datetime datetime to use during replacements
     * @return this for chaining
     */
    public MessageFormatter withDateTime(String token, LocalDateTime datetime)
    {
        return add(token, groups -> datetime.format(DateTimeFormatter.ofPattern(groups.get(1))));
    }

    /**
     * Adds common datetime replacements with current datetime.
     *
     * @param token token name
     * @return this for chaining
     */
    public MessageFormatter withDateTime(String token)
    {
        return withDateTime(token, LocalDateTime.now());
    }

    /**
     * Adds common duration replacements.
     *
     * @param token    token name
     * @param duration duration to use during replacements
     * @return this for chaining
     */
    public MessageFormatter withDuration(String token, Duration duration)
    {
        final long millis = Math.abs(duration.toMillis());
        return add(token, groups -> DurationFormatUtils.formatDuration(millis, groups.get(1)));
    }

    /**
     * Adds common number replacements.
     *
     * @param token token name
     * @param args  arguments to be formatted into pattern
     * @return this for chaining
     */
    public MessageFormatter withFormatted(String token, Object... args)
    {
        return add(token, groups -> String.format(groups.get(1), args));
    }

    /**
     * Adds an optional literal replacement.
     *
     * @param token       token name
     * @param replacement string literal to replace match with
     * @return this for chaining
     */
    public MessageFormatter addOptional(String token, String replacement)
    {
        return add(token, groups ->
                replacement == null || replacement.isEmpty() ? groups.get(1)
                                                             : replacement);
    }

    /**
     * Adds an optional functional replacement.
     *
     * @param token       token name
     * @param replacement string literal to replace match with
     * @param fallback    fallback replacer if replacement missing
     * @return this for chaining
     */
    public MessageFormatter addOptional(String token, String replacement, TokenReplacer fallback)
    {
        return replacement == null || replacement.isEmpty() ? add(token, fallback)
                                                            : add(token, replacement);
    }

    /**
     * Adds an optional functional replacement.
     *
     * @param token       regex pattern to find
     * @param replacement string literal to replace match with
     * @param fallback    fallback replacer if replacement missing
     * @return this for chaining
     */
    public MessageFormatter addOptional(Pattern token, String replacement, TokenReplacer fallback)
    {
        return replacement == null || replacement.isEmpty() ? add(token, fallback)
                                                            : add(token, replacement);
    }

    /**
     * Add a new regex literal replacement.
     *
     * @param regex       regex pattern to find
     * @param replacement string literal to replace match with
     * @return this for chaining
     */
    public MessageFormatter add(Pattern regex, String replacement)
    {
        literals.put(regex, replacement);
        return this;
    }

    /**
     * Add a new token literal replacement.
     *
     * @param token       token name
     * @param replacement string literal to replace match with
     * @return this for chaining
     * @see MessageFormatter#add(Pattern, String)
     */
    public MessageFormatter add(String token, String replacement)
    {
        return add(Pattern.compile("\\{" + Pattern.quote(token) + "}"), replacement);
    }

    /**
     * Add a new regex functional replacement. The replacer will be called with
     * all matched groups, the first being the entire match.
     *
     * @param regex    regex pattern to find
     * @param replacer match replacer function
     * @return this for chaining
     */
    public MessageFormatter add(Pattern regex, TokenReplacer replacer)
    {
        functional.put(regex, replacer);
        return this;
    }

    /**
     * Add a new token functional replacement. The replacer will be called with
     * two groups: entire match and argument after the pipe (i.e.
     * "{DATE|argument}").
     *
     * @param token    token name (i.e. "DATE" -> "{DATE|format}")
     * @param replacer match replacer function
     * @return this for chaining
     * @see MessageFormatter#add(Pattern, TokenReplacer)
     */
    public MessageFormatter add(String token, TokenReplacer replacer)
    {
        return add(Pattern.compile("\\{" + Pattern.quote(token) + "\\|(.*?)}"), replacer);
    }

    /**
     * Apply this formatter to a given template.
     *
     * @param template string template to format
     * @return template formatted with rules
     */
    public String format(final String template)
    {
        if (template == null || template.isEmpty()) return "";

        // Handle literal replacements
        String message = template;
        for (Map.Entry<Pattern, String> entry : literals.entrySet())
            message = message.replaceAll(entry.getKey().pattern(), entry.getValue());

        // Handle functional replacements
        for (Map.Entry<Pattern, TokenReplacer> entry : functional.entrySet()) {
            Matcher matcher = entry.getKey().matcher(message);
            if (matcher.find()) {
                StringBuffer sb = new StringBuffer();
                do {
                    // Fetch regex groups
                    ArrayList<String> groups = new ArrayList<>();
                    for (int i = 0, j = matcher.groupCount(); i <= j; i++)
                        groups.add(matcher.group(i));
                    // Append the replacement supplied by the functional callback
                    matcher.appendReplacement(sb, entry.getValue().replace(groups));
                } while (matcher.find());
                matcher.appendTail(sb);
                message = sb.toString();
            }
        }

        // Post replacements
        message = message.replace("\\n", "\n");

        return message;
    }

    public interface TokenReplacer
    {
        /**
         * Handle replacement of a regex match.
         *
         * @param groups list of captured regex groups (first is entire regex match)
         * @return replacement for entire regex match
         */
        String replace(List<String> groups);
    }
}
