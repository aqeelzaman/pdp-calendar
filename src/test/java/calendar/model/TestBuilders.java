package calendar.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test utility class providing builder patterns for creating parameter maps
 * used in calendar operations. Contains separate builders for calendar management,
 * event creation, and copy operations.
 */
public class TestBuilders {

  /**
   * Builder for calendar management parameters.
   * Supports creating, editing, and switching between calendars.
   */
  public static class CalendarBuilder {
    private String calname;
    private ZoneId timezone;
    private String property;
    private Object value;

    /**
     * Sets the calendar name.
     *
     * @param calname the name of the calendar
     * @return this builder for method chaining
     */
    public CalendarBuilder name(String calname) {
      this.calname = calname;
      return this;
    }

    /**
     * Sets the timezone using a timezone string.
     *
     * @param timezone the timezone ID (e.g., "America/New_York")
     * @return this builder for method chaining
     */
    public CalendarBuilder timezone(String timezone) {
      this.timezone = ZoneId.of(timezone);
      return this;
    }

    /**
     * Sets the property to edit.
     *
     * @param property the property name ("name" or "timezone")
     * @return this builder for method chaining
     */
    public CalendarBuilder property(String property) {
      this.property = property;
      return this;
    }

    /**
     * Sets the new value for the property being edited.
     *
     * @param value the new value (String for name, ZoneId for timezone)
     * @return this builder for method chaining
     */
    public CalendarBuilder value(Object value) {
      this.value = value;
      return this;
    }

    /**
     * Builds parameters for creating a calendar.
     *
     * @return map containing "calname" and "timezone"
     */
    public Map<String, Object> buildCreate() {
      Map<String, Object> params = new HashMap<>();
      params.put("calname", calname);
      params.put("timezone", timezone);
      return params;
    }

    /**
     * Builds parameters for editing a calendar.
     *
     * @return map containing "calname", "property", and "value"
     */
    public Map<String, Object> buildEdit() {
      Map<String, Object> params = new HashMap<>();
      params.put("calname", calname);
      params.put("property", property);
      params.put("value", value);
      return params;
    }

    /**
     * Builds parameters for switching to a calendar.
     *
     * @return map containing "calname"
     */
    public Map<String, Object> buildUse() {
      Map<String, Object> params = new HashMap<>();
      params.put("calname", calname);
      return params;
    }
  }

  /**
   * Builder for event creation parameters.
   * Supports single events, all-day events, and event series.
   */
  public static class EventBuilder {
    private String subject;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private LocalDate ondate;
    private String location;
    private String status;
    private String description;
    private LocalDate untildate;
    private int ndays;
    private String weekdays;
    private String property;
    private Object value;

    /**
     * Sets the event subject.
     *
     * @param subject the event subject
     * @return this builder for method chaining
     */
    public EventBuilder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the event start time from an ISO-8601 string.
     *
     * @param startStr the start time string (e.g., "2025-01-15T09:00:00-05:00[America/New_York]")
     * @return this builder for method chaining
     */
    public EventBuilder start(String startStr) {
      this.start = ZonedDateTime.parse(startStr);
      return this;
    }

    /**
     * Sets the event end time from an ISO-8601 string.
     *
     * @param endStr the end time string (e.g., "2025-01-15T10:00:00-05:00[America/New_York]")
     * @return this builder for method chaining
     */
    public EventBuilder end(String endStr) {
      this.end = ZonedDateTime.parse(endStr);
      return this;
    }

    /**
     * Sets the date for an all-day event.
     *
     * @param dateStr the date string in YYYY-MM-DD format
     * @return this builder for method chaining
     */
    public EventBuilder ondate(String dateStr) {
      this.ondate = LocalDate.parse(dateStr);
      return this;
    }

    /**
     * Sets the event location.
     *
     * @param location the event location
     * @return this builder for method chaining
     */
    public EventBuilder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the event status.
     *
     * @param status the event status
     * @return this builder for method chaining
     */
    public EventBuilder status(String status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the event description.
     *
     * @param description the event description
     * @return this builder for method chaining
     */
    public EventBuilder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the weekdays for a series using single-character codes.
     *
     * @param weekdays weekday codes (M=Monday, T=Tuesday, W=Wednesday,
     *                 R=Thursday, F=Friday, S=Saturday, U=Sunday)
     * @return this builder for method chaining
     */
    public EventBuilder weekdays(String weekdays) {
      this.weekdays = weekdays;
      return this;
    }

    /**
     * Sets the number of days for a series.
     *
     * @param ndays the number of days the series spans
     * @return this builder for method chaining
     */
    public EventBuilder ndays(int ndays) {
      this.ndays = ndays;
      return this;
    }

    /**
     * Sets the end date for a series from an ISO-8601 string.
     *
     * @param untilStr the end date string
     * @return this builder for method chaining
     */
    public EventBuilder untildate(String untilStr) {
      this.untildate = LocalDate.parse(untilStr);
      return this;
    }

    /**
     * Sets the property to edit.
     *
     * @param property the property name
     * @return this builder for method chaining
     */
    public EventBuilder property(String property) {
      this.property = property;
      return this;
    }

    /**
     * Sets the new value for editing as a string.
     *
     * @param value the new value
     * @return this builder for method chaining
     */
    public EventBuilder value(String value) {
      this.value = value;
      return this;
    }

    /**
     * Sets the new value for editing as a ZonedDateTime.
     *
     * @param value the new datetime value as an ISO-8601 string
     * @return this builder for method chaining
     */
    public EventBuilder valueAsDateTime(String value) {
      this.value = ZonedDateTime.parse(value);
      return this;
    }

    /**
     * Builds the event parameters map.
     * Includes all set parameters, omitting null or default values.
     *
     * @return map containing event parameters
     */
    public Map<String, Object> build() {
      Map<String, Object> params = new HashMap<>();
      params.put("subject", subject);

      if (ondate != null) {
        params.put("ondate", ondate);
      } else {
        params.put("start", start);
        params.put("end", end);
      }

      if (location != null) {
        params.put("location", location);
      }
      if (status != null) {
        params.put("status", status);
      }
      if (description != null) {
        params.put("description", description);
      }
      if (weekdays != null) {
        params.put("weekdays", weekdays);
      }
      if (ndays != 0) {
        params.put("ndays", ndays);
      }
      if (untildate != null) {
        params.put("untildate", untildate);
      }
      if (property != null) {
        params.put("property", property);
      }
      if (value != null) {
        params.put("value", value);
      }

      return params;
    }
  }

  /**
   * Builder for copy operation parameters.
   * Supports copying single events, events on a date, and events in a date range.
   */
  public static class CopyBuilder {
    private String target;
    private String subject;
    private ZonedDateTime start;
    private ZonedDateTime targetstart;
    private LocalDate sourcedate;
    private LocalDate targetdate;
    private LocalDate startdate;
    private LocalDate enddate;

    /**
     * Sets the target calendar name.
     *
     * @param target the name of the target calendar
     * @return this builder for method chaining
     */
    public CopyBuilder target(String target) {
      this.target = target;
      return this;
    }

    /**
     * Sets the subject of the event to copy.
     *
     * @param subject the event subject
     * @return this builder for method chaining
     */
    public CopyBuilder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the start time of the event to copy from an ISO-8601 string.
     *
     * @param startStr the start time string
     * @return this builder for method chaining
     */
    public CopyBuilder start(String startStr) {
      this.start = ZonedDateTime.parse(startStr);
      return this;
    }

    /**
     * Sets the target start time for the copied event from an ISO-8601 string.
     *
     * @param targetstartStr the target start time string
     * @return this builder for method chaining
     */
    public CopyBuilder targetstart(String targetstartStr) {
      this.targetstart = ZonedDateTime.parse(targetstartStr);
      return this;
    }

    /**
     * Sets the source date for copying events.
     *
     * @param sourcedate the source date
     * @return this builder for method chaining
     */
    public CopyBuilder sourcedate(LocalDate sourcedate) {
      this.sourcedate = sourcedate;
      return this;
    }

    /**
     * Sets the target date for copying events.
     *
     * @param targetdate the target date
     * @return this builder for method chaining
     */
    public CopyBuilder targetdate(LocalDate targetdate) {
      this.targetdate = targetdate;
      return this;
    }

    /**
     * Sets the start date of the range for copying events.
     *
     * @param startdate the start date of the range
     * @return this builder for method chaining
     */
    public CopyBuilder startdate(LocalDate startdate) {
      this.startdate = startdate;
      return this;
    }

    /**
     * Sets the end date of the range for copying events.
     *
     * @param enddate the end date of the range
     * @return this builder for method chaining
     */
    public CopyBuilder enddate(LocalDate enddate) {
      this.enddate = enddate;
      return this;
    }

    /**
     * Builds parameters for copying a single event.
     *
     * @return map containing "target", "subject", "start", and "targetstart"
     */
    public Map<String, Object> buildCopyEvent() {
      Map<String, Object> params = new HashMap<>();
      params.put("target", target);
      params.put("subject", subject);
      params.put("start", start);
      params.put("targetstart", targetstart);
      return params;
    }

    /**
     * Builds parameters for copying all events on a specific date.
     *
     * @return map containing "target", "sourcedate", and "targetdate"
     */
    public Map<String, Object> buildCopyEventsOn() {
      Map<String, Object> params = new HashMap<>();
      params.put("target", target);
      params.put("sourcedate", sourcedate);
      params.put("targetdate", targetdate);
      return params;
    }

    /**
     * Builds parameters for copying events within a date range.
     *
     * @return map containing "target", "startdate", "enddate", and "targetdate"
     */
    public Map<String, Object> buildCopyEventsBetween() {
      Map<String, Object> params = new HashMap<>();
      params.put("target", target);
      params.put("startdate", startdate);
      params.put("enddate", enddate);
      params.put("targetdate", targetdate);
      return params;
    }
  }
}