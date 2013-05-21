package org.vaadin.addons.rangedatefield;

import java.util.Collection;

public class RangeDateFieldUtils {
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
