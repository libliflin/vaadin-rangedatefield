package org.vaadin.addons.rangedatefield.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.VCalendarPanel;
import com.vaadin.terminal.gwt.client.ui.VPopupCalendar;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * RangeDateField extends standard Vaadin DateField and allows you to restrict date selection to a specific set of dates.
 * You can also set custom CSS styles to specific dates this way customizing the Look&Feel of the calendar.
 */
public class VRangeDateField extends VPopupCalendar {
    public static final String DATE_TIME_FORMAT_PATTERN = "yyyy.MM.dd";
    protected static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat(DATE_TIME_FORMAT_PATTERN);
    public static final String VALID_FROM_DATE_PARAM = "validFromDate";
    public static final String VALID_TO_DATE_PARAM = "validToDate";
    public static final String EXCLUDE_DATES_PARAM = "excludeDates";
    public static final String EXCLUDE_WEEKENDS_PARAM = "excludeWeekends";
    public static final String DATE_STYLE_NAMES_PARAM = "dateStyleNames";

    protected VCalendarPanel calendarPanel;
    protected FlexTable days;

    private Date validFromDate;
    private Date validToDate;
    private Date[] excludeDates;
    private Map<String, List<String>> oldDateStyleNames;
    private Map<String, List<String>> dateStyleNames;
    private boolean excludeWeekends = false;

    public VRangeDateField() {
        super();
        validFromDate = null;
        validToDate = null;
        excludeDates = null;
        dateStyleNames = null;
        calendarPanel = getCalendar();
        oldDateStyleNames = null;

        final ClickHandler dayClickHandler = getDayClickHandler();
        setDayClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                Date currentMonth = getCurrentMonth();
                if (clickEvent.getSource() instanceof Widget && ((InlineHTML) clickEvent.getSource()).getHTML() != null) {
                    //Integer day = Integer.parseInt(((InlineHTML)clickEvent.getSource()).getHTML());
                    Widget day = (Widget) clickEvent.getSource();
                    if (isValidDate(day)) {
                        dayClickHandler.onClick(clickEvent);
                    }
                }
            }
        });
    }

    /**
     * Checks if date1 is on the same day as date2
     * @param date1
     * @param date2
     * @return
     */
    protected boolean isSameDay(Date date1, Date date2) {
        return CalendarUtil.isSameDate(date1, date2);
    }

    /**
     * Checks if date should be disabled in the calendar
     * @param date
     * @return
     */
    protected boolean isExcluded(Date date) {
        if (date == null) return false;
        for (Date e : excludeDates) {
            if (e != null && isSameDay(e, date)) {
                //VConsole.log("Excluded date: " + date + ", exclusion criteria: " + e);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks is the day the widget represents is not disabled in the calendar
     * @param day
     * @return
     */
    protected boolean isValidDate(Widget day) {
        Date dayDate = getDayDate(day);
        return dayDate != null &&
                (!excludeWeekends || dayDate.getDay() != 0 && dayDate.getDay() != 6) &&
                (validFromDate == null || validFromDate.before(dayDate) || isSameDay(validFromDate, dayDate)) &&
                (validToDate == null || validToDate.after(dayDate) || isSameDay(validToDate, dayDate)) &&
                (excludeDates == null || !isExcluded(dayDate));
        //VConsole.log("Is valid date: " + dayDate + " RESULT: "+valid);
    }

    @Override
    public void openCalendarPanel() {
        super.openCalendarPanel();
        days = getDays();
        initHandlers();
        renderStyles();
    }

    /**
     * @param day
     * @return  A list of custom CSS styles assigned to a date
     */
    protected List<String> getDateStyles(Widget day) {
        Date date = getDayDate(day);
        return dateStyleNames != null ? dateStyleNames.get(DATE_TIME_FORMAT.format(date)) : null;
    }

    /**
     * @param day
     * @return  A list of custom CSS styles that were assigned to a specific date before styles update
     * (if custom styles are changed on the fly then this is used to remove previous CSS styles before applying
     * the new ones).
     */
    protected List<String> getOldDateStyles(Widget day) {
        Date date = getDayDate(day);
        return oldDateStyleNames != null ? oldDateStyleNames.get(DATE_TIME_FORMAT.format(date)) : null;
    }

    /**
     * Adds custom CSS style names to calendar days
     */
    protected void renderStyles() {
        if (days != null) {
            for (int r = 0; r < days.getRowCount(); r++) {
                for (int c = 0; c < days.getCellCount(r); c++) {
                    Widget d = days.getWidget(r, c);
                    if (d != null && d instanceof InlineHTML && ((InlineHTML) d).getHTML() != null) {
                        if (!isValidDate(d)) {
                            d.addStyleDependentName("disabled-day");
                        } else {
                            // if no longer disabled remove disabled style
                            d.removeStyleDependentName("disabled-day");
                        }
                        List<String> oldStyles = getOldDateStyles(d);
                        if (oldStyles != null && oldStyles.size() > 0) {
                            for (String style : oldStyles) {
                                d.removeStyleName(style);
                            }
                        }

                        List<String> styles = getDateStyles(d);
                        if (styles != null && styles.size() > 0) {
                            for (String style : styles) {
                                d.addStyleName(style);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Initializes mouse event handlers
     */
    protected void initHandlers() {
        MouseDownHandler handler = new MouseDownHandler() {
            public void onMouseDown(MouseDownEvent mouseDownEvent) {
                if (!isEnabled() || isReadonly()) {
                    return;
                }
                renderStyles();
            }
        };
        if (getPrevYearButton() != null)
            getPrevYearButton().addMouseDownHandler(handler);
        if (getNextYearButton() != null)
            getNextYearButton().addMouseDownHandler(handler);
        if (getPrevMonthButton() != null)
            getPrevMonthButton().addMouseDownHandler(handler);
        if (getNextMonthButton() != null)
            getNextMonthButton().addMouseDownHandler(handler);
    }

    /**
     * @return  Private property VPopupCalendar.calendar
     */
    public native VCalendarPanel getCalendar() /*-{
        return this.@com.vaadin.terminal.gwt.client.ui.VPopupCalendar::calendar;
    }-*/;

    /**
     * @return  Private property VCalendarPanel.days
     */
    public native FlexTable getDays() /*-{
        var calPan = this.@org.vaadin.addons.rangedatefield.client.VRangeDateField::calendarPanel;
        return calPan.@com.vaadin.terminal.gwt.client.ui.VCalendarPanel::days;
    }-*/;

    /**
     * @return  Private property VCalendarPanel.dayClickHandler
     */
    public native ClickHandler getDayClickHandler() /*-{
        var calPan = this.@org.vaadin.addons.rangedatefield.client.VRangeDateField::calendarPanel;
        return calPan.@com.vaadin.terminal.gwt.client.ui.VCalendarPanel::dayClickHandler;
    }-*/;

    /**
     * Sets a new ClickHandler to private VCalendarPanel.dayClickHandler
     * @param clickHandler
     */
    public native void setDayClickHandler(ClickHandler clickHandler) /*-{
        var calPan = this.@org.vaadin.addons.rangedatefield.client.VRangeDateField::calendarPanel;
        calPan.@com.vaadin.terminal.gwt.client.ui.VCalendarPanel::dayClickHandler = clickHandler;
    }-*/;

    /**
     * @return  Private property VCalendarPanel.prevYear
     */
    public native Button getPrevYearButton() /*-{
        var calPan = this.@org.vaadin.addons.rangedatefield.client.VRangeDateField::calendarPanel;
        return calPan.@com.vaadin.terminal.gwt.client.ui.VCalendarPanel::prevYear;
    }-*/;

    /**
     * @return  Private property VCalendarPanel.nextYear
     */
    public native Button getNextYearButton() /*-{
        var calPan = this.@org.vaadin.addons.rangedatefield.client.VRangeDateField::calendarPanel;
        return calPan.@com.vaadin.terminal.gwt.client.ui.VCalendarPanel::nextYear;
    }-*/;

    /**
     * @return  Private property VCalendarPanel.prevMonth
     */
    public native Button getPrevMonthButton() /*-{
        var calPan = this.@org.vaadin.addons.rangedatefield.client.VRangeDateField::calendarPanel;
        return calPan.@com.vaadin.terminal.gwt.client.ui.VCalendarPanel::prevMonth;
    }-*/;

    /**
     * @return  Private property VCalendarPanel.nextMonth
     */
    public native Button getNextMonthButton() /*-{
        var calPan = this.@org.vaadin.addons.rangedatefield.client.VRangeDateField::calendarPanel;
        return calPan.@com.vaadin.terminal.gwt.client.ui.VCalendarPanel::nextMonth;
    }-*/;

    /**
     * @return  Private property VCalendarPanel.displayedMonth
     */
    public native Date getCurrentMonth() /*-{
        var calPan = this.@org.vaadin.addons.rangedatefield.client.VRangeDateField::calendarPanel;
        return calPan.@com.vaadin.terminal.gwt.client.ui.VCalendarPanel::displayedMonth;
    }-*/;

    /**
     * Calls a private method of VCalendarPanel.Day.getDate
     * @return
     */
    public native Date getDayDate(Widget day) /*-{
        return day.@com.vaadin.terminal.gwt.client.ui.VCalendarPanel.Day::getDate()();
    }-*/;

    /**
     * Called whenever an update is received from the server
     */
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);
        VConsole.log("Updating from UIDL");
        if (uidl.hasAttribute(VALID_FROM_DATE_PARAM)) {
            String validFromDateString = uidl.getStringAttribute(VALID_FROM_DATE_PARAM);
            validFromDate = DATE_TIME_FORMAT.parse(validFromDateString);
        } else {
            validFromDate = null;
        }
        if (uidl.hasAttribute(VALID_TO_DATE_PARAM)) {
            String validToDateString = uidl.getStringAttribute(VALID_TO_DATE_PARAM);
            validToDate = DATE_TIME_FORMAT.parse(validToDateString);
        } else {
            validToDate = null;
        }
        if (uidl.hasAttribute(EXCLUDE_DATES_PARAM)) {
            String[] excludeDatesString = uidl.getStringArrayAttribute(EXCLUDE_DATES_PARAM);
            List<Date> result = new LinkedList<Date>();
            for (String date : excludeDatesString) {
                result.add(DATE_TIME_FORMAT.parse(date));
            }
            excludeDates = result.toArray(new Date[result.size()]);
        } else {
            excludeDates = null;
        }
        if (uidl.hasAttribute(EXCLUDE_WEEKENDS_PARAM)) {
            excludeWeekends = uidl.getBooleanAttribute(EXCLUDE_WEEKENDS_PARAM);
        } else {
            excludeWeekends = false;
        }
        if (uidl.hasAttribute(DATE_STYLE_NAMES_PARAM)) {
            String[] dateStyles = uidl.getStringArrayAttribute(DATE_STYLE_NAMES_PARAM);
            oldDateStyleNames = dateStyleNames;
            dateStyleNames = new HashMap<String, List<String>>();
            for (String dateStyle : dateStyles) {
                String[] s1 = dateStyle.split(":");
                if (s1.length == 2) {
                    try {
                        String date = s1[0];
                        List<String> styles = new LinkedList<String>();
                        for (String style : s1[1].split(",")) {
                            styles.add(style);
                        }
                        dateStyleNames.put(date, styles);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            dateStyleNames = null;
        }
        initHandlers();
        renderStyles();
    }
}