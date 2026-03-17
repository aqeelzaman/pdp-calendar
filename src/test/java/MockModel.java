import calendar.model.CalendarModelInterface;
import calendar.model.event.CalendarEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Class that acts as a mock for the CalendarModel to test the Controller.
 */
public class MockModel implements CalendarModelInterface {

  public Map<String, Object> parameters;
  public ZonedDateTime dateTime;
  public boolean busy = false;
  public ZoneId timezone;

  /**
   * Returns the parameters sent by the controller to the model.
   *
   * @return a map of command names to its given values
   */
  public Map<String, Object> getParameters() {
    return parameters;
  }

  @Override
  public void createAllDayEvent(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public void createAllDayEventSeries(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public void createEventSeries(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public void createSingleEvent(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public void editEvents(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public List<CalendarEvent> getAllEvents() {
    return List.of();
  }

  @Override
  public List<CalendarEvent> getEventsOn(LocalDate date) {
    return List.of();
  }

  @Override
  public List<CalendarEvent> getEventsInRange(ZonedDateTime startDateTime,
                                              ZonedDateTime endDateTime) {
    return List.of();
  }

  @Override
  public void editSeries(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public void editSingleEvent(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  @Override
  public boolean checkAvailability(ZonedDateTime dateTime) {
    this.dateTime = dateTime;
    return this.busy;
  }

  @Override
  public ZoneId getTimezone() {
    return null;
  }

  /**
   * Sets availability of the calendar to test status command.
   *
   * @param busy true if busy
   */
  public void setAvailability(boolean busy) {
    this.busy = busy;
  }

  /**
   * updates the timezone.
   *
   * @param timezone timezone in ZoneId
   * @return the model itself
   */
  public MockModel updateTimezone(ZoneId timezone) {
    this.timezone = timezone;
    return this;
  }

  @Override
  public void copyEvent(CalendarModelInterface target, Map<String, Object> parameters) {
    return;
  }

  @Override
  public void copyEventsOn(CalendarModelInterface target, Map<String, Object> parameters) {
    return;
  }

  @Override
  public void copyEventsBetween(CalendarModelInterface target, Map<String, Object> parameters) {
    return;
  }
}
