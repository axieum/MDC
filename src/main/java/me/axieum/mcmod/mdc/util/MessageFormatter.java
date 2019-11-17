package me.axieum.mcmod.mdc.util;

import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageFormatter
{
    private LinkedHashMap<Pattern, TokenReplacer> replacers = new LinkedHashMap<>();

    /**
     * Constructs a new Message Formatter instance.
     */
    public MessageFormatter() {}

    /**
     * Adds a date token.
     *
     * @param token token name to match
     * @param date  date to use for replacements
     * @return a reference to this object
     */
    public MessageFormatter addDate(String token, LocalDate date)
    {
        return add(token, match -> {
            try {
                return date.format(DateTimeFormatter.ofPattern(match.get(2)));
            } catch (Exception e) { return ""; }
        });
    }

    /**
     * Adds a date token using the current date.
     *
     * @param token token name to match
     * @return a reference to this object
     * @see #addDate(String, LocalDate)
     */
    public MessageFormatter addDate(String token)
    {
        return addDate(token, LocalDate.now());
    }

    /**
     * Adds a time token.
     *
     * @param token token name to match
     * @param time  time to use for replacements
     * @return a reference to this object
     */
    public MessageFormatter addTime(String token, LocalTime time)
    {
        return add(token, match -> {
            try {
                return time.format(DateTimeFormatter.ofPattern(match.get(2)));
            } catch (Exception e) { return ""; }
        });
    }

    /**
     * Adds a time token using the current time.
     *
     * @param token token name to match
     * @return a reference to this object
     * @see #addTime(String, LocalTime)
     */
    public MessageFormatter addTime(String token)
    {
        return addTime(token, LocalTime.now());
    }

    /**
     * Adds a date time token.
     *
     * @param token    token name to match
     * @param dateTime date time to use for replacements
     * @return a reference to this object
     */
    public MessageFormatter addDateTime(String token, LocalDateTime dateTime)
    {
        return add(token, match -> {
            try {
                return dateTime.format(DateTimeFormatter.ofPattern(match.get(2)));
            } catch (Exception e) { return ""; }
        });
    }

    /**
     * Adds a date time token using the current date and time.
     *
     * @param token token name to match
     * @return a reference to this object
     * @see #addDateTime(String, LocalDateTime)
     */
    public MessageFormatter addDateTime(String token)
    {
        return addDateTime(token, LocalDateTime.now());
    }

    /**
     * Adds a duration token.
     *
     * @param token    token name to match
     * @param duration duration to use for replacements
     * @return a reference to this object
     */
    public MessageFormatter addDuration(String token, Duration duration)
    {
        final long millis = Math.abs(duration.toMillis());
        return add(token, match ->
                match.size() > 2 ? DurationFormatUtils.formatDuration(millis, match.get(2))
                                 : DurationFormatUtils.formatDurationWords(millis, true, true));
    }

    /**
     * Adds a duration functional token.
     *
     * @param token    token name to match
     * @param supplier duration supplier to use for replacements
     * @return a reference to this object
     */
    public MessageFormatter addDuration(String token, Supplier<Duration> supplier)
    {
        return add(token, match -> {
            final long millis = Math.abs(supplier.get().toMillis());
            return match.size() > 2 ? DurationFormatUtils.formatDuration(millis, match.get(2))
                                    : DurationFormatUtils.formatDurationWords(millis, true, true);
        });
    }

    /**
     * Adds an optional token that uses a fallback replacement if the given
     * replacement is empty or {@code null}.
     *
     * @param token       token name to match
     * @param replacement replacement
     * @param fallback    default fallback if replacement is empty/{@code null}
     *                    and token does not specify its own fallback value
     * @return a reference to this object
     */
    public MessageFormatter addOptional(String token, @Nullable String replacement, @Nonnull String fallback)
    {
        if (replacement == null || replacement.isEmpty())
            return add(token, match -> match.size() > 2 ? match.get(2) : fallback);
        return add(token, match -> replacement);
    }

    /**
     * Adds a string format token.
     *
     * @param token token name to match
     * @param args  arguments referenced by the formatter
     * @return a reference to this object
     * @see String#format(String, Object...)
     */
    public MessageFormatter addFormatted(String token, Object... args)
    {
        return add(token, match -> {
            try {
                return String.format(match.get(2), args);
            } catch (IllegalFormatException | IndexOutOfBoundsException e) { return ""; }
        });
    }

    /**
     * Adds a string replacement.
     *
     * @param target      string to be replaced
     * @param replacement string to replace with
     * @return a reference to this object
     */
    public MessageFormatter addReplacement(String target, String replacement)
    {
        return add(Pattern.compile(Pattern.quote(target)), match -> replacement);
    }

    /**
     * Adds a new regex replacement.
     *
     * @param regex    regex pattern to match
     * @param replacer match replacer
     * @return a reference to this object
     */
    public MessageFormatter add(Pattern regex, TokenReplacer replacer)
    {
        replacers.put(regex, replacer);
        return this;
    }

    /**
     * Adds a new regex replacement.
     *
     * @param regex       regex pattern to match
     * @param replacement plaintext replacement
     * @return a reference to this object
     * @see MessageFormatter#add(Pattern, TokenReplacer)
     */
    public MessageFormatter add(Pattern regex, String replacement)
    {
        return add(regex, match -> replacement);
    }

    /**
     * Adds a new token replacement.
     *
     * @param token    token name to match
     * @param replacer match replacer with groups: all, token, arguments
     * @return a reference to this object
     * @see MessageFormatter#add(Pattern, TokenReplacer)
     */
    public MessageFormatter add(String token, TokenReplacer replacer)
    {
        return add(Pattern.compile("\\{\\{(" + Pattern.quote(token) + ")(?:\\|(.*?))?}}"), replacer);
    }

    /**
     * Adds a new token replacement.
     *
     * @param token       token name to match
     * @param replacement plaintext replacement
     * @return a reference to this object
     * @see MessageFormatter#add(String, TokenReplacer)
     */
    public MessageFormatter add(String token, String replacement)
    {
        return add(token, match -> replacement);
    }

    /**
     * Adds a new token functional replacement.
     *
     * @param token    token name to match
     * @param supplier plaintext replacement supplier
     * @return a reference to this object
     */
    public MessageFormatter add(String token, Supplier<String> supplier)
    {
        return add(token, match -> supplier.get());
    }

    /**
     * Applies this formatter to a given message template to obtain a new
     * formatted message.
     *
     * @param template message template to format
     * @return formatted message
     */
    public String apply(String template)
    {
        if (template == null || template.isEmpty()) return "";

        // Handle functional replacements
        String message = template;
        for (Map.Entry<Pattern, TokenReplacer> entry : replacers.entrySet()) {
            Pattern token = entry.getKey();
            TokenReplacer replacer = entry.getValue();

            Matcher matcher = token.matcher(message);
            if (matcher.find()) {
                StringBuffer sb = new StringBuffer();
                do {
                    // Fetch regex groups
                    final int groupCount = matcher.groupCount();
                    ArrayList<String> groups = new ArrayList<>(groupCount);
                    for (int i = 0; i <= groupCount; i++) {
                        String group = matcher.group(i);
                        if (group != null) groups.add(group);
                    }
                    // Append the replacement supplied by the functional callback
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacer.replace(groups)));
                } while (matcher.find());
                matcher.appendTail(sb);
                message = sb.toString();
            }
        }

        return message;
    }

    public interface TokenReplacer
    {
        /**
         * Computes the replacement text for a regex match.
         *
         * @param groups list of captured regex groups - first is entire match
         * @return replacement string for entire regex match
         */
        String replace(List<String> groups);
    }
}
