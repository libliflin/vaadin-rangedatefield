package org.vaadin.addons.rangedatefield;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.DateField;
import org.apache.commons.lang.time.DateUtils;
import org.vaadin.addons.rangedatefield.client.VRangeDateField;

import java.text.SimpleDateFormat;
import java.util.*;

@ClientWidget(VRangeDateField.class)
public class RangeDateField extends DateField {
   private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(VRangeDateField.DATE_TIME_FORMAT_PATTERN);
   private Date validFromDate = null;
   private Date validToDate = null;
   private Date[] excludeDates = null;
   private Boolean excludeWeekends = null;
   protected Map<Date, List<String>> dateStyleNames = null;
   Calendar calendar = Calendar.getInstance();

   public RangeDateField() {}

   public RangeDateField(Property dataSource) throws IllegalArgumentException {
      super(dataSource);
   }

   public RangeDateField(String caption, Date value) {
      super(caption, value);
   }

   public RangeDateField(String caption, Property dataSource) {
      super(caption, dataSource);
   }

   public RangeDateField(String caption) {
      super(caption);
   }

   public Date getValidFromDate() {
      return validFromDate;
   }

   public void setValidFromDate(Date validFromDate) {
      this.validFromDate = validFromDate;
      requestRepaint();
   }

   public Date getValidToDate() {
      return validToDate;
   }

   public void setValidToDate(Date validToDate) {
      this.validToDate = validToDate;
      requestRepaint();
   }

   public Date[] getExcludeDates() {
      return excludeDates;
   }

   public void setExcludeDates(Date[] excludeDates) {
      this.excludeDates = excludeDates;
      requestRepaint();
   }

   public Boolean getExcludeWeekends() {
      return excludeWeekends;
   }

   public void setExcludeWeekends(Boolean excludeWeekends) {
      this.excludeWeekends = excludeWeekends;
      requestRepaint();
   }

   protected boolean containsStyle(Date date, String styleName) {
      List<String> styles = dateStyleNames.get(date);
      if (styles != null) {
         return styles.contains(styleName);
      }
      return false;
   }

   protected void addStyle(Date date, String style) {
      List<String> styles = dateStyleNames.get(date);
      if (styles == null) {
         styles = new LinkedList<String>();
         dateStyleNames.put(date, styles);
      }
      styles.add(style);
   }

   public void addDateStyleName(Date date, String styleName) {
      if (date == null || styleName == null) return;
      if (dateStyleNames == null) dateStyleNames = new HashMap<Date, List<String>>();
      if (!containsStyle(date, styleName)) {
         addStyle(date, styleName);
         requestRepaint();
      }
   }
   public void clearDateStyleNames() {
      dateStyleNames.clear();
   }

   public void removeDateStyleNames(Date date) {
      List<String> styles = dateStyleNames.get(date);
      if (styles != null) {
         styles.clear();
      }
      requestRepaint();
   }

   public void removeDateStyleName(Date date, String style) {
      List<String> styles = dateStyleNames.get(date);
      if (styles != null) {
         styles.remove(style);
      }
      requestRepaint();
   }

   public boolean isValid() {
      return isValidDate();
   }

   protected boolean isDateBefore(Date date1, Date date2) {
      return DateUtils.isSameDay(date1, date2) || date1.before(date2);
   }

   protected boolean isDateAfter(Date date1, Date date2) {
      return DateUtils.isSameDay(date1, date2) || date1.after(date2);
   }

   protected boolean isDateExcluded(Date date) {
      if (excludeDates != null) {
         for (Date excludeDate : excludeDates) {
            if (DateUtils.isSameDay(date, excludeDate)) {
               return true;
            }
         }
      }
      return false;
   }

   protected boolean isDateWeekend(Date date) {
      calendar.setTime(date);
      int day = calendar.get(Calendar.DAY_OF_WEEK);
      return Calendar.SATURDAY == day || Calendar.SUNDAY == day;
   }

   protected boolean isValidDate() {
      Date date = (Date) getValue();
      if (date != null) {
         if (validFromDate != null && !isDateAfter(date, validFromDate)) {
            return false;
         }
         if (validToDate != null && !isDateBefore(date, validToDate)) {
            return false;
         }
         if (isDateExcluded(date)) {
            return false;
         }
         if (Boolean.TRUE.equals(excludeWeekends)) {
            return !isDateWeekend(date);
         }
      }
      return true;
   }

   @Override
   public void validate() throws Validator.InvalidValueException {
      if (!isValidDate()) {
         throw new Validator.InvalidValueException("Invalid value!");
      }
      super.validate();
   }

   public void paintContent(PaintTarget target) throws PaintException {
      super.paintContent(target);
      if (getValidFromDate() != null) {
         target.addAttribute(VRangeDateField.VALID_FROM_DATE_PARAM, DATE_TIME_FORMAT.format(getValidFromDate()));
      }
      if (getValidToDate() != null) {
         target.addAttribute(VRangeDateField.VALID_TO_DATE_PARAM, DATE_TIME_FORMAT.format(getValidToDate()));
      }
      if (getExcludeDates() != null) {
         List<String> result = new LinkedList<String>();
         for (Date date : getExcludeDates()) {
            result.add(DATE_TIME_FORMAT.format(date));
         }
         target.addAttribute(VRangeDateField.EXCLUDE_DATES_PARAM, result.toArray(new String[result.size()]));
      }
      if (excludeWeekends != null) {
         target.addAttribute(VRangeDateField.EXCLUDE_WEEKENDS_PARAM, excludeWeekends);
      }

      List<String> dateStyles = new LinkedList<String>();
      if (dateStyleNames != null && dateStyleNames.size() > 0) {
         for (Date date : dateStyleNames.keySet()) {
            Collection<String> styles = dateStyleNames.get(date);
            dateStyles.add(DATE_TIME_FORMAT.format(date) + ":" + RangeDateFieldUtils.join(styles, ","));
         }
      }
      if (dateStyles != null && dateStyles.size() > 0) {
         target.addAttribute(VRangeDateField.DATE_STYLE_NAMES_PARAM, dateStyles.toArray(new String[dateStyles.size()]));
      }
   }
}