package me.axieum.mcmod.mdc.util;

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
}
