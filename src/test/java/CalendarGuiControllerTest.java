import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.controller.gui.CalendarGuiController;
import calendar.model.CalendarManager;
import calendar.model.CalendarManagerInterface;
import calendar.model.TestBuilders;
import calendar.model.event.CalendarEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for CalendarGuiController.
 * Verifies controller behavior in managing calendars, events, and GUI interactions.
 */
public class CalendarGuiControllerTest {

  private CalendarGuiController controller;
  private MockCalendarGuiView mockView;
  private CalendarManagerInterface manager;

  /**
   * Initializes test fixtures before each test method.
   * Creates a fresh controller, mock view, and calendar manager for isolated testing.
   */
  @Before
  public void setUp() {
    manager = new CalendarManager();
    mockView = new MockCalendarGuiView();
    controller = new CalendarGuiController(mockView, manager);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullView() {
    new CalendarGuiController(null, manager);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullManager() {
    new CalendarGuiController(mockView, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorBothNull() {
    new CalendarGuiController(null, null);
  }

  @Test
  public void testDefaultCalendarCreatedOnStartup() {
    List<String> calendars = controller.getCalendarNames();
    assertTrue(calendars.contains("Default Calendar"));
    assertEquals("Default Calendar", controller.getCurrentCalendarName());
  }

  @Test
  public void testCreateCalendarWithTimezone() {
    controller.createCalendar("Work Calendar", "America/New_York");

    List<String> calendarNames = controller.getCalendarNames();
    assertTrue(calendarNames.contains("Work Calendar"));
    assertTrue(calendarNames.contains("Default Calendar"));
    assertEquals(2, calendarNames.size());
  }

  @Test
  public void testCreateMultipleCalendars() {
    controller.createCalendar("Work", "America/New_York");
    controller.createCalendar("Personal", "Europe/London");
    controller.createCalendar("Travel", "Asia/Tokyo");

    List<String> calendars = controller.getCalendarNames();
    assertEquals(4, calendars.size());
    assertTrue(calendars.contains("Work"));
    assertTrue(calendars.contains("Personal"));
    assertTrue(calendars.contains("Travel"));
  }

  @Test
  public void testSwitchCalendar() {
    controller.createCalendar("Work", "America/New_York");
    controller.switchCalendar("Work");

    assertEquals("Work", controller.getCurrentCalendarName());
    assertEquals("America/New_York", controller.getCurrentCalendarTimezone());
    assertTrue(mockView.refreshCount > 0);
  }

  @Test
  public void testSwitchBetweenMultipleCalendars() {
    controller.createCalendar("Work", "America/New_York");
    controller.createCalendar("Personal", "Europe/London");

    controller.switchCalendar("Work");
    assertEquals("Work", controller.getCurrentCalendarName());

    controller.switchCalendar("Personal");
    assertEquals("Personal", controller.getCurrentCalendarName());
    assertEquals("Europe/London", controller.getCurrentCalendarTimezone());
  }

  @Test
  public void testEditCalendarName() {
    controller.createCalendar("Test", "America/New_York");
    controller.editCalendar("Test", "Updated", null);

    List<String> calendars = controller.getCalendarNames();
    assertTrue(calendars.contains("Updated"));
    assertFalse(calendars.contains("Test"));
  }

  @Test
  public void testEditCalendarTimezone() {
    controller.createCalendar("Test", "America/New_York");
    controller.switchCalendar("Test");
    controller.editCalendar("Test", null, "America/Los_Angeles");

    assertEquals("America/Los_Angeles", controller.getCurrentCalendarTimezone());
  }

  @Test
  public void testEditCalendarNameAndTimezone() {
    controller.createCalendar("Test", "America/New_York");
    controller.editCalendar("Test", "Updated", "Europe/London");

    List<String> calendars = controller.getCalendarNames();
    assertTrue(calendars.contains("Updated"));

    controller.switchCalendar("Updated");
    assertEquals("Europe/London", controller.getCurrentCalendarTimezone());
  }

  @Test
  public void testEditActiveCalendarUpdatesReference() {
    controller.createCalendar("Active", "America/New_York");
    controller.switchCalendar("Active");

    controller.editCalendar("Active", "Renamed", null);

    assertEquals("Renamed", controller.getCurrentCalendarName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNonExistent() {
    controller.editCalendar("NonExistent", "NewName",
        "America/New_York");
  }

  @Test
  public void testEditCalendarOnlyName() {
    controller.createCalendar("Original", "America/New_York");
    controller.editCalendar("Original", "Updated", null);

    List<String> calendars = controller.getCalendarNames();
    assertTrue(calendars.contains("Updated"));
    assertFalse(calendars.contains("Original"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarWithExistingName() {
    controller.createCalendar("Original", "America/New_York");
    controller.createCalendar("Updated", "America/New_York");
    controller.editCalendar("Original", "Updated", null);
  }

  @Test
  public void testEditCalendarOnlyTimezone() {
    controller.createCalendar("Test", "America/New_York");
    controller.switchCalendar("Test");

    controller.editCalendar("Test", null, "Europe/London");

    assertEquals("Europe/London", controller.getCurrentCalendarTimezone());
    assertEquals("Test", controller.getCurrentCalendarName());
  }

  @Test
  public void testEditCalendarEmptyName() {
    controller.createCalendar("Original", "America/New_York");
    String originalTimezone = "America/New_York";

    controller.editCalendar("Original", "", "Europe/London");

    List<String> calendars = controller.getCalendarNames();
    assertTrue(calendars.contains("Original"));
    controller.switchCalendar("Original");
    assertEquals("Europe/London", controller.getCurrentCalendarTimezone());
  }

  @Test
  public void testEditCalendarEmptyTimezone() {
    controller.createCalendar("Original", "America/New_York");

    controller.editCalendar("Original", "Updated", "");

    List<String> calendars = controller.getCalendarNames();
    assertTrue(calendars.contains("Updated"));
    assertFalse(calendars.contains("Original"));
  }

  @Test
  public void testEditCalendarInvalidTimezone() {
    controller.createCalendar("Original", "America/New_York");

    try {
      controller.editCalendar("Original", null, "Invalid");
      fail("Should throw exception");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid timezone: Invalid", e.getMessage());
    }
  }

  @Test
  public void testEditCalendarWhitespaceName() {
    controller.createCalendar("Original", "America/New_York");

    controller.editCalendar("Original", "   ", "Europe/London");

    List<String> calendars = controller.getCalendarNames();
    assertTrue(calendars.contains("Original"));
    controller.switchCalendar("Original");
    assertEquals("Europe/London", controller.getCurrentCalendarTimezone());
  }

  @Test
  public void testEditCalendarWhitespaceTimezone() {
    controller.createCalendar("Original", "America/New_York");

    controller.editCalendar("Original", "Updated", "   ");

    List<String> calendars = controller.getCalendarNames();
    assertTrue(calendars.contains("Updated"));
    assertFalse(calendars.contains("Original"));
  }

  @Test
  public void testEditCalendarBothNull() {
    controller.createCalendar("Test", "America/New_York");

    controller.editCalendar("Test", null, null);

    List<String> calendars = controller.getCalendarNames();
    assertTrue(calendars.contains("Test"));
  }

  @Test
  public void testCreateSingleEventBasic() {
    Map<String, Object> params = new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build();

    controller.createSingleEvent(params);

    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test
  public void testCreateSingleEventDetailed() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Team Meeting");
    params.put("start", ZonedDateTime.of(2025, 12, 15, 10, 0,
        0, 0,
        ZoneId.of("America/New_York")));
    params.put("end", ZonedDateTime.of(2025, 12, 15, 11, 0,
        0, 0,
        ZoneId.of("America/New_York")));
    params.put("description", "Weekly team sync");
    params.put("location", "physical");

    controller.createSingleEvent(params);

    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("Team Meeting", events.get(0).getSubject());
    assertTrue(mockView.refreshCount > 0);
  }

  @Test
  public void testCreateEventSeries() {
    Map<String, Object> params = new TestBuilders.EventBuilder()
        .subject("Daily Standup")
        .start("2025-01-13T09:00:00-05:00[America/New_York]")
        .end("2025-01-13T09:30:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(7)
        .build();

    controller.createEventSeries(params);

    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(7, events.size());
  }

  @Test
  public void testCreateAllDayEvent() {
    Map<String, Object> params = new TestBuilders.EventBuilder()
        .subject("Holiday")
        .ondate("2025-01-15")
        .build();

    controller.createAllDayEvent(params);

    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(1, events.size());
  }

  @Test
  public void testCreateAllDayEventSeries() {
    Map<String, Object> params = new TestBuilders.EventBuilder()
        .subject("Training")
        .ondate("2025-01-13")
        .weekdays("MW")
        .ndays(3)
        .build();

    controller.createAllDayEventSeries(params);

    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(3, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSingleEventWithInvalidParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Test");

    controller.createSingleEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSingleEventDuplicate() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Duplicate");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 1, 15, 11, 0,
        0, 0, ZoneId.systemDefault()));

    controller.createSingleEvent(params);
    controller.createSingleEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventSeriesWithInvalidParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Series");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));

    controller.createEventSeries(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventSeriesMultiDay() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Invalid Series");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 22, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 1, 16, 2, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("weekdays", "M");
    params.put("ndays", 3);

    controller.createEventSeries(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateAllDayEventWithInvalidParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Invalid All Day");

    controller.createAllDayEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateAllDayEventSeriesWithInvalidParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Invalid Series");
    params.put("ondate", LocalDate.of(2025, 1, 15));

    controller.createAllDayEventSeries(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateAllDayEventSeriesInvalidWeekdays() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Invalid Series");
    params.put("ondate", LocalDate.of(2025, 1, 15));
    params.put("weekdays", "XYZ");
    params.put("ndays", 3);

    controller.createAllDayEventSeries(params);
  }

  @Test
  public void testEditSingleEventProperty() {
    Map<String, Object> createParams = new HashMap<>();
    createParams.put("subject", "Project Review");
    createParams.put("start", ZonedDateTime.of(2025, 12, 20, 14,
        0, 0, 0,
        ZoneId.systemDefault()));
    createParams.put("end", ZonedDateTime.of(2025, 12, 20, 15,
        0, 0, 0,
        ZoneId.systemDefault()));

    controller.createSingleEvent(createParams);
    List<CalendarEvent> eventsBefore = controller.getAllEvents();
    assertNull(eventsBefore.get(0).getLocation());

    Map<String, Object> editParams = new HashMap<>();
    editParams.put("subject", "Project Review");
    editParams.put("start", ZonedDateTime.of(2025, 12, 20, 14,
        0, 0, 0,
        ZoneId.systemDefault()));
    editParams.put("end", ZonedDateTime.of(2025, 12, 20, 15,
        0, 0, 0,
        ZoneId.systemDefault()));
    editParams.put("property", "location");
    editParams.put("value", "online");

    controller.editSingleEvent(editParams);

    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("online", events.get(0).getLocation());
  }

  @Test
  public void testEditSingleEventSubject() {
    Map<String, Object> createParams = new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build();

    controller.createSingleEvent(createParams);

    Map<String, Object> editParams = new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated Meeting")
        .build();

    controller.editSingleEvent(editParams);

    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals("Updated Meeting", events.get(0).getSubject());
  }

  @Test
  public void testEditEntireSeries() {
    Map<String, Object> createParams = new TestBuilders.EventBuilder()
        .subject("Series")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .end("2025-01-13T11:00:00-05:00[America/New_York]")
        .weekdays("MW")
        .ndays(3)
        .build();

    controller.createEventSeries(createParams);

    Map<String, Object> editParams = new TestBuilders.EventBuilder()
        .subject("Series")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated Series")
        .build();

    controller.editSeries(editParams);

    List<CalendarEvent> events = controller.getAllEvents();
    for (CalendarEvent event : events) {
      assertEquals("Updated Series", event.getSubject());
    }
  }

  @Test
  public void testEditSeriesFromSpecificDate() {
    Map<String, Object> createParams = new TestBuilders.EventBuilder()
        .subject("Series")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .end("2025-01-13T11:00:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(7)
        .build();

    controller.createEventSeries(createParams);

    Map<String, Object> editParams = new TestBuilders.EventBuilder()
        .subject("Series")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .property("location")
        .value("online")
        .build();

    controller.editEvents(editParams);

    List<CalendarEvent> events = controller.getAllEvents();
    int withLocation = 0;
    for (CalendarEvent event : events) {
      if ("online".equals(event.getLocation())) {
        withLocation++;
      }
    }
    assertEquals(5, withLocation);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSingleEventNotFound() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Nonexistent");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 1, 15, 11, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("property", "location");
    params.put("value", "online");

    controller.editSingleEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSingleEventMissingParameters() {
    Map<String, Object> createParams = new HashMap<>();
    createParams.put("subject", "Meeting");
    createParams.put("start", ZonedDateTime.of(2025, 1, 15, 10,
        0, 0, 0, ZoneId.systemDefault()));
    createParams.put("end", ZonedDateTime.of(2025, 1, 15, 11,
        0, 0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(createParams);

    Map<String, Object> editParams = new HashMap<>();
    editParams.put("subject", "Meeting");
    editParams.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));

    controller.editSingleEvent(editParams);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesNotFound() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Nonexistent Series");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("property", "description");
    params.put("value", "Updated");

    controller.editSeries(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesMissingProperty() {
    Map<String, Object> createParams = new HashMap<>();
    createParams.put("subject", "Series");
    createParams.put("start", ZonedDateTime.of(2025, 1, 13, 10,
        0, 0, 0, ZoneId.systemDefault()));
    createParams.put("end", ZonedDateTime.of(2025, 1, 13, 11,
        0, 0, 0, ZoneId.systemDefault()));
    createParams.put("weekdays", "MW");
    createParams.put("ndays", 3);
    controller.createEventSeries(createParams);

    Map<String, Object> editParams = new HashMap<>();
    editParams.put("subject", "Series");
    editParams.put("start", ZonedDateTime.of(2025, 1, 13, 10,
        0, 0, 0, ZoneId.systemDefault()));
    editParams.put("value", "Updated");

    controller.editSeries(editParams);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsNotFound() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Nonexistent");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("property", "location");
    params.put("value", "physical");

    controller.editEvents(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsWithNameNotValid() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Event 1");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 1, 15, 12, 0,
        0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(params);
    params.put("property", "start");
    params.put("value", ZonedDateTime.of(2025, 1, 15, 14, 0,
        0, 0, ZoneId.systemDefault()));

    controller.editEventsWithName(params);
  }


  @Test
  public void testEditEventsWithNameNotFound() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Event 1");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 1, 15, 12, 0,
        0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(params);
    params.put("subject", "Event 2");
    params.put("property", "start");
    params.put("value", ZonedDateTime.of(2025, 1, 15, 9, 0,
        0, 0, ZoneId.systemDefault()));

    controller.editEventsWithName(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsInvalidProperty() {
    Map<String, Object> createParams = new HashMap<>();
    createParams.put("subject", "Training");
    createParams.put("start", ZonedDateTime.of(2025, 1, 13, 10,
        0, 0, 0, ZoneId.systemDefault()));
    createParams.put("end", ZonedDateTime.of(2025, 1, 13, 11,
        0, 0, 0, ZoneId.systemDefault()));
    createParams.put("weekdays", "M");
    createParams.put("ndays", 3);
    controller.createEventSeries(createParams);

    Map<String, Object> editParams = new HashMap<>();
    editParams.put("subject", "Training");
    editParams.put("start", ZonedDateTime.of(2025, 1, 13, 10,
        0, 0, 0, ZoneId.systemDefault()));
    editParams.put("property", "invalidProperty");
    editParams.put("value", "something");

    controller.editEvents(editParams);
  }

  @Test
  public void testViewEventsOnSpecificDate() {
    Map<String, Object> params1 = new HashMap<>();
    params1.put("subject", "Christmas Event 1");
    params1.put("start", ZonedDateTime.of(2025, 12, 25, 9,
        0, 0, 0,
        ZoneId.systemDefault()));
    params1.put("end", ZonedDateTime.of(2025, 12, 25, 10,
        0, 0, 0,
        ZoneId.systemDefault()));
    params1.put("description", "Morning celebration");
    params1.put("location", "physical");

    Map<String, Object> params2 = new HashMap<>();
    params2.put("subject", "Christmas Event 2");
    params2.put("start", ZonedDateTime.of(2025, 12, 25, 14,
        0, 0, 0,
        ZoneId.systemDefault()));
    params2.put("end", ZonedDateTime.of(2025, 12, 25, 15,
        0, 0, 0,
        ZoneId.systemDefault()));
    params2.put("description", "Afternoon dinner");
    params2.put("location", "online");

    controller.createSingleEvent(params1);
    controller.createSingleEvent(params2);

    LocalDate testDate = LocalDate.of(2025, 12, 25);
    List<CalendarEvent> eventsOnDate = controller.getEventsOnDate(testDate);

    assertEquals(2, eventsOnDate.size());

    assertEquals("Christmas Event 1", eventsOnDate.get(0).getSubject());
    assertEquals("Christmas Event 2", eventsOnDate.get(1).getSubject());

    assertEquals("Morning celebration", eventsOnDate.get(0).getDescription());
    assertEquals("Afternoon dinner", eventsOnDate.get(1).getDescription());

    assertEquals("physical", eventsOnDate.get(0).getLocation());
    assertEquals("online", eventsOnDate.get(1).getLocation());

    assertEquals(testDate, eventsOnDate.get(0).getStartDateTime().toLocalDate());
    assertEquals(testDate, eventsOnDate.get(1).getStartDateTime().toLocalDate());
  }

  @Test
  public void testGetEventsOnDate() {
    Map<String, Object> params1 = new TestBuilders.EventBuilder()
        .subject("Event 1")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build();

    Map<String, Object> params2 = new TestBuilders.EventBuilder()
        .subject("Event 2")
        .start("2025-01-15T14:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .build();

    Map<String, Object> params3 = new TestBuilders.EventBuilder()
        .subject("Event 3")
        .start("2025-01-16T10:00:00-05:00[America/New_York]")
        .end("2025-01-16T11:00:00-05:00[America/New_York]")
        .build();

    controller.createSingleEvent(params1);
    controller.createSingleEvent(params2);
    controller.createSingleEvent(params3);

    List<CalendarEvent> events = controller.getEventsOnDate(LocalDate.of(2025, 1,
        15));
    assertEquals(2, events.size());
  }

  @Test
  public void testGetEventsInRange() {
    Map<String, Object> params1 = new TestBuilders.EventBuilder()
        .subject("Event 1")
        .start("2025-01-10T10:00:00-05:00[America/New_York]")
        .end("2025-01-10T11:00:00-05:00[America/New_York]")
        .build();

    Map<String, Object> params2 = new TestBuilders.EventBuilder()
        .subject("Event 2")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build();

    Map<String, Object> params3 = new TestBuilders.EventBuilder()
        .subject("Event 3")
        .start("2025-01-20T10:00:00-05:00[America/New_York]")
        .end("2025-01-20T11:00:00-05:00[America/New_York]")
        .build();

    controller.createSingleEvent(params1);
    controller.createSingleEvent(params2);
    controller.createSingleEvent(params3);

    List<CalendarEvent> events = controller.getEventsInRange(
        LocalDate.of(2025, 1, 12),
        LocalDate.of(2025, 1, 18));

    assertEquals(1, events.size());
    assertEquals("Event 2", events.get(0).getSubject());
  }

  @Test
  public void testSelectDifferentCalendar() {

    controller.createCalendar("Personal", "America/Los_Angeles");

    assertEquals("Default Calendar", controller.getCurrentCalendarName());
    controller.switchCalendar("Personal");

    assertEquals("Personal", controller.getCurrentCalendarName());
    assertTrue(mockView.refreshCount > 0);
  }

  @Test
  public void testViewDefaultCalendar() {
    String defaultName = controller.getCurrentCalendarName();
    assertEquals("Default Calendar", defaultName);

    List<String> calendarNames = controller.getCalendarNames();
    assertTrue(calendarNames.contains("Default Calendar"));
  }

  @Test
  public void testCreateRecurringEventSpecificWeekdays() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Weekly Standup");
    params.put("start", ZonedDateTime.of(2025, 12, 1, 9, 0,
        0, 0,
        ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 12, 1, 9, 30,
        0, 0,
        ZoneId.systemDefault()));
    params.put("weekdays", "MWF");
    params.put("ndays", 6);

    controller.createEventSeries(params);

    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(6, events.size());

    String seriesUid = events.get(0).getSeriesUid();

    for (CalendarEvent event : events) {
      DayOfWeek day = event.getStartDateTime().getDayOfWeek();
      assertEquals("Weekly Standup", event.getSubject());
      assertEquals(seriesUid, event.getSeriesUid());
      assertTrue(day == DayOfWeek.MONDAY || day == DayOfWeek.WEDNESDAY
          || day == DayOfWeek.FRIDAY);
    }
  }

  @Test
  public void testCreateRecurringEventOccurrences() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Daily Scrum");
    params.put("start", ZonedDateTime.of(2025, 12, 1, 10, 0,
        0, 0,
        ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 12, 1, 10, 15,
        0, 0,
        ZoneId.systemDefault()));
    params.put("weekdays", "MTWRF");
    params.put("ndays", 10);

    controller.createEventSeries(params);

    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(10, events.size());

    String seriesUid = events.get(0).getSeriesUid();
    assertNotNull(seriesUid);
    for (CalendarEvent event : events) {
      assertEquals("Daily Scrum", event.getSubject());
      assertEquals(seriesUid, event.getSeriesUid());
    }
  }

  @Test
  public void testEditMultipleEventsSameName() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Recurring Meeting");
    params.put("start", ZonedDateTime.of(2025, 12, 2, 15, 0,
        0, 0,
        ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 12, 2, 16, 0,
        0, 0,
        ZoneId.systemDefault()));
    params.put("weekdays", "T");
    params.put("ndays", 4);

    controller.createEventSeries(params);

    Map<String, Object> editParams = new HashMap<>();
    editParams.put("subject", "Recurring Meeting");
    editParams.put("start", ZonedDateTime.of(2025, 12, 2, 15,
        0, 0, 0,
        ZoneId.systemDefault()));
    editParams.put("property", "description");
    editParams.put("value", "Updated description for all");

    controller.editSeries(editParams);

    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(4, events.size());

    for (CalendarEvent event : events) {
      assertEquals("Updated description for all", event.getDescription());
    }
  }

  @Test
  public void testEditEventsFromSpecificTime() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Training Session");
    params.put("start", ZonedDateTime.of(2025, 12, 1, 13, 0,
        0, 0,
        ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 12, 1, 14, 0,
        0, 0,
        ZoneId.systemDefault()));
    params.put("weekdays", "M");
    params.put("ndays", 5);

    controller.createEventSeries(params);

    Map<String, Object> editParams = new HashMap<>();
    editParams.put("subject", "Training Session");
    editParams.put("start", ZonedDateTime.of(2025, 12, 15, 13,
        0, 0, 0,
        ZoneId.systemDefault()));
    editParams.put("property", "location");
    editParams.put("value", "physical");

    controller.editEvents(editParams);

    List<CalendarEvent> allEvents = controller.getAllEvents();
    assertEquals(5, allEvents.size());

    int withLocation = 0;
    for (CalendarEvent event : allEvents) {
      if ("physical".equals(event.getLocation())) {
        withLocation++;
      }
    }

    assertTrue(withLocation >= 3);

    for (int i = 0; i < allEvents.size(); i++) {
      LocalDate eventDate = allEvents.get(i).getStartDateTime().toLocalDate();
      if (eventDate.isBefore(LocalDate.of(2025, 12, 15))) {
        assertNull(allEvents.get(i).getLocation());
      } else {
        assertEquals("physical", allEvents.get(i).getLocation());
      }
    }
  }

  @Test
  public void testCopyEvent() {
    controller.createCalendar("Target", "America/New_York");

    Map<String, Object> eventParams = new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build();

    controller.createSingleEvent(eventParams);

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("subject", "Meeting");
    copyParams.put("start",
        ZonedDateTime.parse("2025-01-15T10:00:00-05:00[America/New_York]"));
    copyParams.put("target", "Target");
    copyParams.put("targetstart",
        ZonedDateTime.parse("2025-02-01T14:00:00-05:00[America/New_York]"));

    controller.copyEvent(copyParams);

    controller.switchCalendar("Target");
    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(1, events.size());
  }

  @Test
  public void testCopyEventsOnDate() {
    controller.createCalendar("Target", "America/New_York");

    Map<String, Object> event1 = new TestBuilders.EventBuilder()
        .subject("Event 1")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T10:00:00-05:00[America/New_York]")
        .build();

    Map<String, Object> event2 = new TestBuilders.EventBuilder()
        .subject("Event 2")
        .start("2025-01-15T14:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .build();

    controller.createSingleEvent(event1);
    controller.createSingleEvent(event2);

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("target", "Target");
    copyParams.put("sourcedate", LocalDate.of(2025, 1, 15));
    copyParams.put("targetdate", LocalDate.of(2025, 2, 1));

    controller.copyEventsOnDate(copyParams);

    controller.switchCalendar("Target");
    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(2, events.size());
  }

  @Test
  public void testCopyEventsInRange() {
    controller.createCalendar("Target", "America/New_York");

    Map<String, Object> event1 = new TestBuilders.EventBuilder()
        .subject("Event 1")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build();

    Map<String, Object> event2 = new TestBuilders.EventBuilder()
        .subject("Event 2")
        .start("2025-01-16T10:00:00-05:00[America/New_York]")
        .end("2025-01-16T11:00:00-05:00[America/New_York]")
        .build();

    controller.createSingleEvent(event1);
    controller.createSingleEvent(event2);

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("target", "Target");
    copyParams.put("startdate", LocalDate.of(2025, 1, 15));
    copyParams.put("enddate", LocalDate.of(2025, 1, 16));
    copyParams.put("targetdate", LocalDate.of(2025, 3, 1));

    controller.copyEventsInRange(copyParams);

    controller.switchCalendar("Target");
    List<CalendarEvent> events = controller.getAllEvents();
    assertEquals(2, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventNotFound() {
    controller.createCalendar("Target", "America/New_York");

    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Nonexistent");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("target", "Target");
    params.put("targetstart", ZonedDateTime.of(2025, 2, 1, 10,
        0, 0, 0, ZoneId.systemDefault()));

    controller.copyEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventMissingTarget() {
    Map<String, Object> eventParams = new HashMap<>();
    eventParams.put("subject", "Meeting");
    eventParams.put("start", ZonedDateTime.of(2025, 1, 15, 10,
        0, 0, 0, ZoneId.systemDefault()));
    eventParams.put("end", ZonedDateTime.of(2025, 1, 15, 11,
        0, 0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(eventParams);

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("subject", "Meeting");
    copyParams.put("start", ZonedDateTime.of(2025, 1, 15, 10,
        0, 0, 0, ZoneId.systemDefault()));
    copyParams.put("targetstart",
        ZonedDateTime.of(2025, 2, 1, 10, 0, 0,
            0, ZoneId.systemDefault()));

    controller.copyEvent(copyParams);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventTargetCalendarNotFound() {
    Map<String, Object> eventParams = new HashMap<>();
    eventParams.put("subject", "Meeting");
    eventParams.put("start", ZonedDateTime.of(2025, 1, 15, 10,
        0, 0, 0, ZoneId.systemDefault()));
    eventParams.put("end", ZonedDateTime.of(2025, 1, 15, 11,
        0, 0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(eventParams);

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("subject", "Meeting");
    copyParams.put("start", ZonedDateTime.of(2025, 1, 15, 10,
        0, 0, 0, ZoneId.systemDefault()));
    copyParams.put("target", "Nonexistent");
    copyParams.put("targetstart",
        ZonedDateTime.of(2025, 2, 1, 10, 0, 0,
            0, ZoneId.systemDefault()));

    controller.copyEvent(copyParams);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsOnDateNoEvents() {
    controller.createCalendar("Target", "America/New_York");

    Map<String, Object> params = new HashMap<>();
    params.put("target", "Target");
    params.put("sourcedate", LocalDate.of(2025, 1, 15));
    params.put("targetdate", LocalDate.of(2025, 2, 1));

    controller.copyEventsOnDate(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsOnDateMissingParameters() {
    controller.createCalendar("Target", "America/New_York");

    Map<String, Object> eventParams = new HashMap<>();
    eventParams.put("subject", "Event");
    eventParams.put("start", ZonedDateTime.of(2025, 1, 15, 10,
        0, 0, 0, ZoneId.systemDefault()));
    eventParams.put("end", ZonedDateTime.of(2025, 1, 15, 11,
        0, 0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(eventParams);

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("target", "Target");
    copyParams.put("sourcedate", LocalDate.of(2025, 1, 15));

    controller.copyEventsOnDate(copyParams);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsOnDateTargetNotFound() {
    Map<String, Object> eventParams = new HashMap<>();
    eventParams.put("subject", "Event");
    eventParams.put("start", ZonedDateTime.of(2025, 1, 15, 10,
        0, 0, 0, ZoneId.systemDefault()));
    eventParams.put("end", ZonedDateTime.of(2025, 1, 15, 11,
        0, 0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(eventParams);

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("target", "Nonexistent");
    copyParams.put("sourcedate", LocalDate.of(2025, 1, 15));
    copyParams.put("targetdate", LocalDate.of(2025, 2, 1));

    controller.copyEventsOnDate(copyParams);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsInRangeNoEvents() {
    controller.createCalendar("Target", "America/New_York");

    Map<String, Object> params = new HashMap<>();
    params.put("target", "Target");
    params.put("startdate", LocalDate.of(2025, 1, 15));
    params.put("enddate", LocalDate.of(2025, 1, 20));
    params.put("targetdate", LocalDate.of(2025, 2, 1));

    controller.copyEventsInRange(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsInRangeMissingParameters() {
    controller.createCalendar("Target", "America/New_York");

    Map<String, Object> eventParams = new HashMap<>();
    eventParams.put("subject", "Event");
    eventParams.put("start", ZonedDateTime.of(2025, 1, 15, 10,
        0, 0, 0, ZoneId.systemDefault()));
    eventParams.put("end", ZonedDateTime.of(2025, 1, 15, 11,
        0, 0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(eventParams);

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("target", "Target");
    copyParams.put("startdate", LocalDate.of(2025, 1, 15));

    controller.copyEventsInRange(copyParams);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsInRangeTargetNotFound() {
    Map<String, Object> eventParams = new HashMap<>();
    eventParams.put("subject", "Event");
    eventParams.put("start", ZonedDateTime.of(2025, 1, 15, 10,
        0, 0, 0, ZoneId.systemDefault()));
    eventParams.put("end", ZonedDateTime.of(2025, 1, 15, 11,
        0, 0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(eventParams);

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("target", "Nonexistent");
    copyParams.put("startdate", LocalDate.of(2025, 1, 15));
    copyParams.put("enddate", LocalDate.of(2025, 1, 20));
    copyParams.put("targetdate", LocalDate.of(2025, 2, 1));

    controller.copyEventsInRange(copyParams);
  }

  @Test
  public void testExportCalendarWithEvents() {
    Map<String, Object> params = new TestBuilders.EventBuilder()
        .subject("Event")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build();

    controller.createSingleEvent(params);

    try {
      controller.exportCalendar("test.csv");
    } catch (IllegalArgumentException e) {
      fail("Export should succeed with events");
    }
  }

  @Test
  public void testExportCalendarIcsFormat() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Event");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 1, 15, 11, 0,
        0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(params);

    try {
      controller.exportCalendar("test.ics");
    } catch (IllegalArgumentException e) {
      fail("Export should succeed with .ics extension");
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportCalendarUnsupportedExtension() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Event");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 1, 15, 11, 0,
        0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(params);

    controller.exportCalendar("calendar.txt");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportCalendarNoExtension() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Event");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 1, 15, 11, 0,
        0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(params);

    controller.exportCalendar("calendar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportCalendarInvalidExtension() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Event");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 1, 15, 11, 0,
        0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(params);

    controller.exportCalendar("calendar.pdf");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportCalendarXmlExtension() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Event");
    params.put("start", ZonedDateTime.of(2025, 1, 15, 10, 0,
        0, 0, ZoneId.systemDefault()));
    params.put("end", ZonedDateTime.of(2025, 1, 15, 11, 0,
        0, 0, ZoneId.systemDefault()));
    controller.createSingleEvent(params);

    controller.exportCalendar("calendar.xml");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportEmptyCalendar() {
    controller.exportCalendar("empty.csv");
  }

  @Test
  public void testGuiMode() {
    controller.guiMode();

    assertTrue(mockView.guiDisplayed);
    assertTrue(mockView.refreshCount > 0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testInteractiveModeShowsError() {
    controller.interactiveMode();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testHeadlessModeShowsError() {
    controller.headlessMode("test.txt");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testProcessCommandShowsError() {
    controller.processCommand("some command");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testShowUsageAndExit() {
    controller.showUsageAndExit();
  }

  @Test
  public void testCreateEventInDifferentCalendar() {
    controller.createCalendar("Work", "America/New_York");
    controller.switchCalendar("Work");

    Map<String, Object> params = new TestBuilders.EventBuilder()
        .subject("Work Event")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build();

    controller.createSingleEvent(params);

    controller.switchCalendar("Default Calendar");
    assertEquals(0, controller.getAllEvents().size());

    controller.switchCalendar("Work");
    assertEquals(1, controller.getAllEvents().size());
  }

  @Test
  public void testGetCurrentCalendarTimezone() {
    assertEquals(ZoneId.systemDefault().toString(),
        controller.getCurrentCalendarTimezone());

    controller.createCalendar("Tokyo", "Asia/Tokyo");
    controller.switchCalendar("Tokyo");

    assertEquals("Asia/Tokyo", controller.getCurrentCalendarTimezone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testErrorHandlingInvalidTimezone() {
    controller.createCalendar("Invalid", "Invalid/Timezone");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testErrorHandlingDuplicateCalendarName() {
    controller.createCalendar("Test", "America/New_York");
    controller.createCalendar("Test", "America/Los_Angeles");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testErrorHandlingSwitchToNonexistentCalendar() {
    controller.switchCalendar("Nonexistent");
  }

  @Test
  public void testViewRefreshCalls() {
    Map<String, Object> parameters = new HashMap<>();
    String subject = "\"Team Meeting\"";
    ZoneId zoneId = ZoneId.of("America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 7, 1, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 7, 1, 10, 0);
    LocalDate untildate = LocalDate.of(2025, 8, 1);
    LocalDate ondate = LocalDate.of(2025, 8, 1);
    LocalDate date = LocalDate.of(2025, 8, 1);
    String weekdays = "MTW";
    int ndays = 10;
    String property = "subject";
    String value = "\"New Team Meeting\"";
    String calname = "Cal A";
    ZoneId timezone = ZoneId.of("America/New_York");

    parameters.put("subject", subject);
    parameters.put("start", start.atZone(zoneId));
    parameters.put("end", end.atZone(zoneId));
    parameters.put("date", date);
    parameters.put("untildate", untildate);
    parameters.put("ondate", ondate);
    parameters.put("weekdays", weekdays);
    parameters.put("ndays", ndays);
    parameters.put("property", property);
    parameters.put("value", value);
    parameters.put("calname", calname);
    parameters.put("timezone", timezone);

    assertEquals(CalendarGuiController.class, mockView.getFeatures().getClass());

    controller.createCalendar("Tokyo", "Asia/Tokyo");
    assertEquals(1, mockView.refreshCount);

    controller.editCalendar("Default Calendar", "Calendar1", "Asia/Tokyo");
    assertEquals(2, mockView.refreshCount);

    controller.createEventSeries(parameters);
    assertEquals(3, mockView.refreshCount);

    controller.editSeries(parameters);
    assertEquals(4, mockView.refreshCount);

    controller.createAllDayEvent(parameters);
    assertEquals(5, mockView.refreshCount);

    controller.editSingleEvent(parameters);
    assertEquals(6, mockView.refreshCount);

    parameters.put("subject", "\"New Team Meeting\"");

    controller.editEvents(parameters);
    assertEquals(7, mockView.refreshCount);

    controller.editEventsWithName(parameters);
    assertEquals(8, mockView.refreshCount);

    controller.createAllDayEventSeries(parameters);
    assertEquals(9, mockView.refreshCount);

    controller.switchCalendar("Calendar1");
    assertEquals(10, mockView.refreshCount);
  }
}