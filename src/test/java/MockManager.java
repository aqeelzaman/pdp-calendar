import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModelInterface;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages multiple calendars and delegates operations to the active calendar.
 * Provides functionality for creating, editing, switching between calendars,
 * and copying events across calendars.
 */
public class MockManager implements CalendarManagerInterface {

  /**
   * Map of calendar names to their corresponding CalendarModel instances.
   */
  private final Map<String, MockModel> calendars;
  public Map<String, Object> parameters;

  /**
   * The name of the currently active calendar, or null if none is active.
   */
  private String activeCalendar;

  /**
   * Constructs a new CalendarManager with no calendars and no active calendar.
   */
  public MockManager() {
    calendars = new HashMap<>();
    activeCalendar = null;
  }

  @Override
  public MockModel getActiveCalendar() {
    checkActiveCalendar();
    return calendars.get(activeCalendar);
  }

  @Override
  public String getCalendarName() {
    checkActiveCalendar();
    return activeCalendar;
  }

  /**
   * Validates that there is an active calendar.
   *
   * @throws IllegalArgumentException if no calendar is currently active
   */
  private void checkActiveCalendar() {
    if (activeCalendar == null) {
      throw new IllegalArgumentException("There is no active calendar in use.");
    }
  }

  @Override
  public ZoneId getTimeZone() {
    return ZoneId.of("America/New_York");
  }

  @Override
  public CalendarModelInterface createCalendar(Map<String, Object> parameters) {
    String calendarName = (String) parameters.get("calname");
    ZoneId timezone = ZoneId.of(parameters.get("timezone").toString());
    if (calendarName == null || calendarName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (calendars.containsKey(calendarName)) {
      throw new IllegalArgumentException(
          "Calendar with name '" + calendarName + "' already exists");
    }
    calendars.put(calendarName, new MockModel());
    return null;
  }

  @Override
  public CalendarModelInterface editCalendar(Map<String, Object> parameters) {
    String calendarName = (String) parameters.get("calname");
    String propertyName = (String) parameters.get("property");
    Object value = parameters.get("value");

    validateCalendarExists(calendarName);
    validateCalendarProperty(propertyName, value);

    if ("timezone".equals(propertyName)) {
      updateCalendarTimezone(calendarName, (ZoneId) value);
    } else if ("name".equals(propertyName)) {
      renameCalendar(calendarName, (String) value);
    }

    return null;
  }

  /**
   * Updates the timezone of a calendar by creating a new instance with converted events.
   *
   * @param calendarName the name of the calendar to update
   * @param newTimezone  the new timezone
   */
  private void updateCalendarTimezone(String calendarName, ZoneId newTimezone) {
    MockModel currentCalendar = calendars.get(calendarName);
    MockModel updatedCalendar = currentCalendar.updateTimezone(newTimezone);
    calendars.put(calendarName, updatedCalendar);
  }

  /**
   * Renames a calendar and updates the active calendar reference if necessary.
   *
   * @param oldName the current name of the calendar
   * @param newName the new name for the calendar
   * @throws IllegalArgumentException if a calendar with the new name already exists
   */
  private void renameCalendar(String oldName, String newName) {
    if (calendars.containsKey(newName) && !newName.equals(oldName)) {
      throw new IllegalArgumentException("Calendar '" + newName + "' already exists");
    }

    MockModel calendar = calendars.remove(oldName);
    calendars.put(newName, calendar);

    if (oldName.equals(activeCalendar)) {
      activeCalendar = newName;
    }
  }

  /**
   * Validates that a calendar with the given name exists.
   *
   * @param calendarName the name of the calendar to validate
   * @throws IllegalArgumentException if the calendar does not exist
   */
  private void validateCalendarExists(String calendarName) {
    if (!calendars.containsKey(calendarName)) {
      throw new IllegalArgumentException("Calendar '" + calendarName + "' does not exist");
    }
  }

  /**
   * Validates that a calendar property name and value are valid.
   *
   * @param property the property name ("name" or "timezone")
   * @param value    the new value (String for name, ZoneId for timezone)
   * @throws IllegalArgumentException if property or value is null, invalid type,
   *                                  or empty (for name property)
   */
  private void validateCalendarProperty(String property, Object value) {
    if (property == null) {
      throw new IllegalArgumentException("Property cannot be null");
    }

    if (value == null) {
      throw new IllegalArgumentException("Value cannot be null");
    }

    if ("name".equals(property)) {
      if (!(value instanceof String)) {
        throw new IllegalArgumentException(
            "Calendar name must be a String, but received " + value.getClass().getSimpleName());
      }

      String newName = ((String) value).trim();
      if (newName.isEmpty()) {
        throw new IllegalArgumentException("Calendar name cannot be empty");
      }
    } else if ("timezone".equals(property)) {
      if (!(value instanceof ZoneId)) {
        throw new IllegalArgumentException(
            "Timezone must be a ZoneId, but received " + value.getClass().getSimpleName());
      }
    } else {
      throw new IllegalArgumentException(
          "Unknown property: '" + property + "'. Valid properties: name, timezone");
    }
  }

  @Override
  public boolean useCalendar(Map<String, Object> parameters) {
    String calendarName = (String) parameters.get("calname");
    if (calendars.containsKey(calendarName)) {
      activeCalendar = calendarName;
      return true;
    } else {
      throw new IllegalArgumentException(
          "Calendar with name '" + calendarName + "' does not exist");
    }
  }

  @Override
  public void copyEvent(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public void copyEventsOn(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public void copyEventsBetween(Map<String, Object> parameters) {
    this.parameters = parameters;
  }
}
