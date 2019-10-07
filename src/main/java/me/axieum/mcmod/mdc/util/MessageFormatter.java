package me.axieum.mcmod.mdc.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageFormatter
{
    private final String template;
    private HashMap<Pattern, String> literals = new HashMap<>();
    private HashMap<Pattern, TokenReplacer> functional = new HashMap<>();

    /**
     * Constructs a new Message Formatter instance.
     *
     * @param template string to replace tokens
     */
    public MessageFormatter(String template)
    {
        this.template = template;
    }

    /**
     * Adds common datetime replacements.
     *
     * @param datetime datetime to use during replacements
     * @return this for chaining
     */
    public MessageFormatter withDateTime(LocalDateTime datetime)
    {
        add(Pattern.compile("\\{(DATE|TIME)\\|(.+?)}"),
            groups -> groups.size() < 3 ? datetime.toString()
                                        : datetime.format(DateTimeFormatter.ofPattern(groups.get(2))));

        return this;
    }

    /**
     * Adds common datetime replacements with current datetime.
     *
     * @return this for chaining
     */
    public MessageFormatter withDateTime()
    {
        return withDateTime(LocalDateTime.now());
    }

    /**
     * Add a new token literal replacement.
     *
     * @param regex       regex pattern to find
     * @param replacement string literal to replace token with
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
     * @param token       string literal to find
     * @param replacement string literal to replace token with
     * @return this for chaining
     * @see MessageFormatter#add(Pattern, String)
     */
    public MessageFormatter add(String token, String replacement)
    {
        return add(Pattern.compile(Pattern.quote(token)), replacement);
    }

    /**
     * Add a new token functional replacement.
     *
     * @param regex    regex pattern to find
     * @param replacer token replacer function
     * @return this for chaining
     */
    public MessageFormatter add(Pattern regex, TokenReplacer replacer)
    {
        functional.put(regex, replacer);
        return this;
    }

    /**
     * Add a new token functional replacement.
     *
     * @param token    string literal to find
     * @param replacer token replacer function
     * @return this for chaining
     * @see MessageFormatter#add(Pattern, TokenReplacer)
     */
    public MessageFormatter add(String token, TokenReplacer replacer)
    {
        return add(Pattern.compile(Pattern.quote(token)), replacer);
    }

    @Override
    public String toString()
    {
        if (template == null || template.isEmpty()) return "";
        if (literals.isEmpty() || functional.isEmpty()) return template;

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

        return message;
    }

    public interface TokenReplacer
    {
        String replace(List<String> groups);
    }
}
