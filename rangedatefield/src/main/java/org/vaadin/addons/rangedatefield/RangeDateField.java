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

    public RangeDateField() {
    }

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

    /**
     * Set this property to restrict selecting dates that are before this date
     * @param validFromDate
     */
    public void setValidFromDate(Date validFromDate) {
        this.validFromDate = validFromDate;
        requestRepaint();
    }

    public Date getValidToDate() {
        return validToDate;
    }

    /**
     * Set this property to restrict selecting dates that are after this date
     * @param validToDate
     */
    public void setValidToDate(Date validToDate) {
        this.validToDate = validToDate;
        requestRepaint();
    }

    public Date[] getExcludeDates() {
        return excludeDates;
    }

    /**
     * Set this property to specify a list of dates that should be disabled (not selectable) in the calendar
     * @param excludeDates
     */
    public void setExcludeDates(Date[] excludeDates) {
        this.excludeDates = excludeDates;
        requestRepaint();
    }

    public Boolean getExcludeWeekends() {
        return excludeWeekends;
    }

    /**
     * If this property is set then all the weekend days are disabled in the calendar
     * @param excludeWeekends
     */
    public void setExcludeWeekends(Boolean excludeWeekends) {
        this.excludeWeekends = excludeWeekends;
        requestRepaint();
    }

    /**
     * Checks if there's a specific style for a date
     * @param date
     * @param styleName
     * @return  True if there's already a style with provided styleName for a specified date
     */
    protected boolean containsStyle(Date date, String styleName) {
        List<String> styles = dateStyleNames.get(date);
        if (styles != null) {
            return styles.contains(styleName);
        }
        return false;
    }

    /**
     * A method used to safely add a style to dateStyleNames map
     * @param date
     * @param style
     */
    protected void addStyle(Date date, String style) {
        List<String> styles = dateStyleNames.get(date);
        if (styles == null) {
            styles = new LinkedList<String>();
            dateStyleNames.put(date, styles);
        }
        styles.add(style);
    }

    /**
     * Adds a CSS styleName to a specific date cell
     * @param date     A date for which the specified CSS style class should be added
     * @param styleName     CSS style class
     */
    public void addDateStyleName(Date date, String styleName) {
        if (date == null || styleName == null) return;
        if (dateStyleNames == null) dateStyleNames = new HashMap<Date, List<String>>();
        if (!containsStyle(date, styleName)) {
            addStyle(date, styleName);
            requestRepaint();
        }
    }

    /**
     * A method to clear all date specific CSS style assignments
     */
    public void clearDateStyleNames() {
        dateStyleNames.clear();
    }

    /**
     * Removes all CSS styles for a specific date
     * @param date
     */
    public void removeDateStyleNames(Date date) {
        List<String> styles = dateStyleNames.get(date);
        if (styles != null) {
            styles.clear();
        }
        requestRepaint();
    }

    /**
     * Removes a specific CSS style for a specified date
     * @param date      A date for which a CSS style should be removed
     * @param style     CSS style to remove
     */
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

    /**
     * Checks if date1 is before or on the same day as date2
     * @param date1
     * @param date2
     * @return
     */
    protected boolean isDateBefore(Date date1, Date date2) {
        return DateUtils.isSameDay(date1, date2) || date1.before(date2);
    }

    /**
     * Checks if date1 is after or on the same day as date2
     * @param date1
     * @param date2
     * @return
     */
    protected boolean isDateAfter(Date date1, Date date2) {
        return DateUtils.isSameDay(date1, date2) || date1.after(date2);
    }

    /**
     * Checks if date is excluded from the calendar
     * @param date
     * @return
     */
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

    /**
     * Checks if the date is a weekend day
     * @param date
     * @return
     */
    protected boolean isDateWeekend(Date date) {
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return Calendar.SATURDAY == day || Calendar.SUNDAY == day;
    }

    /**
     * Checks if a date is valid (not disabled in the calendar)
     * @return
     */
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