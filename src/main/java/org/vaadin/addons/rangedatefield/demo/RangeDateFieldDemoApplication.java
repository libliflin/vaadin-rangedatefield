package org.vaadin.addons.rangedatefield.demo;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.*;
import org.vaadin.addons.rangedatefield.RangeDateField;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RangeDateFieldDemoApplication extends Application {
   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
   RangeDateField rangeDateField;
   DateField fromDate;
   DateField toDate;

   @Override
   public void init() {
      Window window = new Window();
      setMainWindow(window);
      window.setContent(createContent());
   }

   private ComponentContainer createContent() {
      VerticalLayout layout = new VerticalLayout();
      layout.setMargin(true);
      layout.setSpacing(true);

      // FROM DATE
      fromDate = new DateField();
      fromDate.setResolution(DateField.RESOLUTION_DAY);
      fromDate.setImmediate(true);
      fromDate.addListener(new Property.ValueChangeListener() {
         @Override
         public void valueChange(Property.ValueChangeEvent event) {
            rangeDateField.setValidFromDate((Date)event.getProperty().getValue());
         }
      });
      layout.addComponent(new Label("Valid from:"));
      layout.addComponent(fromDate);

      // TO DATE
      toDate = new DateField();
      toDate.setResolution(DateField.RESOLUTION_DAY);
      toDate.setImmediate(true);
      toDate.addListener(new Property.ValueChangeListener() {
         @Override
         public void valueChange(Property.ValueChangeEvent event) {
            rangeDateField.setValidToDate((Date)event.getProperty().getValue());
         }
      });
      layout.addComponent(new Label("Valid to:"));
      layout.addComponent(toDate);

      // Exclude weekends
      CheckBox excludeWeekends = new CheckBox();
      excludeWeekends.setImmediate(true);
      excludeWeekends.addListener(new Property.ValueChangeListener() {
         @Override
         public void valueChange(Property.ValueChangeEvent event) {
            rangeDateField.setExcludeWeekends((Boolean)event.getProperty().getValue());
         }
      });
      layout.addComponent(new Label("Exclude weekends:"));
      layout.addComponent(excludeWeekends);

      TextField excludeDates = new TextField();
      excludeDates.setWidth("300px");
      excludeDates.setImmediate(true);
      excludeDates.addValidator(new RegexpValidator("([0-9]{4}-[0-9]{2}-[0-9]{2})(,[0-9]{4}-[0-9]{2}-[0-9]{2})*",
              "Incorrect date list format (should be: \"2013-05-20,2013-06-17\")"));
      excludeDates.addListener(new Property.ValueChangeListener() {
         @Override
         public void valueChange(Property.ValueChangeEvent event) {
            String dates = (String) event.getProperty().getValue();
            if (dates != null && dates.length() > 0) {
               String[] dateArray = dates.split(",");
               List<Date> dateList = new LinkedList<Date>();
               for (String str : dateArray) {
                  try {
                     Date date = dateFormat.parse(str);
                     dateList.add(date);
                  } catch (ParseException e) {
                     e.printStackTrace();
                     throw new RuntimeException(e);
                  }
               }
               rangeDateField.setExcludeDates(dateList.toArray(new Date[dateList.size()]));
            }
         }
      });
      layout.addComponent(new Label("Exclude dates:"));
      layout.addComponent(excludeDates);

      final DateField styleDate = new DateField();
      styleDate.setResolution(DateField.RESOLUTION_DAY);
      final TextField styleName = new TextField();
      styleName.addValidator(new RegexpValidator("[a-zA-Z\\-]*", "Incorrect style name!"));
      HorizontalLayout styleLayout = new HorizontalLayout();
      styleLayout.setSpacing(true);
      styleLayout.addComponent(styleDate);
      styleLayout.addComponent(styleName);
      styleLayout.addComponent(new Button("Add", new Button.ClickListener() {
         @Override
         public void buttonClick(Button.ClickEvent clickEvent) {
            Date date = (Date) styleDate.getValue();
            String style = (String) styleName.getValue();
            if (date != null && style != null && style.length() > 0) {
               rangeDateField.addDateStyleName(date, style);
            }
         }
      }));
      styleLayout.addComponent(new Button("Remove", new Button.ClickListener() {
         @Override
         public void buttonClick(Button.ClickEvent clickEvent) {
            Date date = (Date) styleDate.getValue();
            String style = (String) styleName.getValue();
            if (date != null && style != null && style.length() > 0) {
               rangeDateField.removeDateStyleName(date, style);
            }
         }
      }));
      layout.addComponent(new Label("Add/remove custom style to a specific day (try styles busy-day, free-day or birthday):"));
      layout.addComponent(styleLayout);

      // Range Date Field
      rangeDateField = new RangeDateField();
      rangeDateField.setResolution(DateField.RESOLUTION_DAY);
      rangeDateField.setImmediate(true);
      layout.addComponent(new Label("Range date field:"));
      layout.addComponent(rangeDateField);
      return layout;
   }
}
