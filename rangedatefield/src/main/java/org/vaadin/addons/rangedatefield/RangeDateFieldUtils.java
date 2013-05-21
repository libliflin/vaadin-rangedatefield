package org.vaadin.addons.rangedatefield;

import java.util.Collection;

public class RangeDateFieldUtils {
    /**
     * Joins a list of Strings using a specified delimeter
     * @param list  A list of strings to join
     * @param delimiter
     * @return  A joined list using a provided delimiter
     */
    public static String join(Collection<String> list, String delimiter) {
        if (list == null) return null;
        StringBuilder result = new StringBuilder();
        for (String str : list) {
            if (result.length() > 0) {
                result.append(delimiter);
            }
            result.append(str);
        }
        return result.toString();
    }
}
