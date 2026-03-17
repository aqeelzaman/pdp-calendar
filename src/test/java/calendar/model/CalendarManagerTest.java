package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.model.TestBuilders.CalendarBuilder;
import calendar.model.TestBuilders.CopyBuilder;
import calendar.model.TestBuilders.EventBuilder;
import calendar.model.event.CalendarEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for CalendarManager class.
 * Tests calendar management operations including creation, editing, switching,
 * and copying events across calendars.
 */
public class CalendarManagerTest {

  private CalendarManager manager;
  private Map<String, Object> params;

  /**
   * Setting up the testing variables before running each test.
   */
  @Before
  public void setUp() {
    manager = new CalendarManager();
    params = new HashMap<>();
  }

  private boolean safeCall(Supplier<Boolean> methodCall) {
    try {
      return methodCall.get();
    } catch (Exception e) {
      return false;
    }
  }

  @Test
  public void testConstructorInitializesWithNoActiveCalendar() {
    try {
      manager.getActiveCalendar();
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("There is no active calendar in use.", e.getMessage());
    }
  }

  @Test
  public void testCreateCalendarSuccess() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));

    assertEquals(CalendarModel.class, manager.createCalendar(params).getClass());
    manager.useCalendar(new CalendarBuilder()
        .name("Work")
        .buildUse());
    assertEquals("Work", manager.getCalendarName());
    assertEquals(ZoneId.of("America/New_York"), manager.getTimeZone());
  }

  @Test
  public void testCreateCalendarWithDifferentTimezone() {
    params.put("calname", "Tokyo");
    params.put("timezone", ZoneId.of("Asia/Tokyo"));

    assertEquals(CalendarModel.class, manager.createCalendar(params).getClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarNullName() {
    params.put("calname", null);
    params.put("timezone", ZoneId.of("America/New_York"));

    manager.createCalendar(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarEmptyName() {
    params.put("calname", "");
    params.put("timezone", ZoneId.of("America/New_York"));

    manager.createCalendar(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarWhitespaceName() {
    params.put("calname", "   ");
    params.put("timezone", ZoneId.of("America/New_York"));

    manager.createCalendar(params);
  }

  @Test
  public void testCreateCalendarDuplicateNameThrowsException() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    try {
      manager.createCalendar(params);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar with name 'Work' already exists", e.getMessage());
    }
  }

  @Test
  public void testUseCalendarSuccess() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    assertTrue(manager.useCalendar(params));
    assertEquals("Work", manager.getCalendarName());
  }

  @Test
  public void testUseCalendarSwitchesBetweenCalendars() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.put("calname", "Personal");
    params.put("timezone", ZoneId.of("America/Los_Angeles"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    manager.useCalendar(params);
    assertEquals("Work", manager.getCalendarName());

    params.put("calname", "Personal");
    manager.useCalendar(params);
    assertEquals("Personal", manager.getCalendarName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUseCalendarNonExistentCalendar() {
    params.put("calname", "NonExistent");
    manager.useCalendar(params);
  }

  @Test
  public void testGetActiveCalendarReturnsCorrectCalendar() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    manager.useCalendar(params);

    CalendarModelInterface calendar = manager.getActiveCalendar();
    assertNotNull(calendar);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetActiveCalendarNoActiveCalendar() {
    manager.getActiveCalendar();
  }

  @Test
  public void testGetCalendarNameReturnsCorrectName() {
    params.put("calname", "MyCalendar");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "MyCalendar");
    manager.useCalendar(params);

    assertEquals("MyCalendar", manager.getCalendarName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetCalendarNameNoActiveCalendar() {
    manager.getCalendarName();
  }

  @Test
  public void testEditCalendarRenameName() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "name");
    params.put("value", "Office");

    assertEquals(CalendarModel.class, manager.editCalendar(params).getClass());

    params.clear();
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/Los_Angeles"));
    manager.createCalendar(params);

    manager.useCalendar(new CalendarBuilder()
        .name("Office")
        .buildUse());
    assertEquals("Office", manager.getCalendarName());
    assertEquals(ZoneId.of("America/New_York"), manager.getTimeZone());
  }

  @Test
  public void testEditCalendarRenameActiveCalendarUpdatesActiveReference() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    manager.useCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "name");
    params.put("value", "Office");
    manager.editCalendar(params);

    assertEquals("Office", manager.getCalendarName());
  }

  @Test
  public void testEditCalendarRenameToSameName() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "name");
    params.put("value", "Work");

    assertEquals(CalendarModel.class, manager.editCalendar(params).getClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarRenameToExistingName() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.put("calname", "Personal");
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "name");
    params.put("value", "Personal");

    manager.editCalendar(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarEmptyNewName() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "name");
    params.put("value", "");

    manager.editCalendar(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarWhitespaceNewName() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "name");
    params.put("value", "   ");

    manager.editCalendar(params);
  }

  @Test
  public void testEditCalendarChangeTimezone() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "timezone");
    params.put("value", ZoneId.of("America/Los_Angeles"));

    assertEquals(CalendarModel.class, manager.editCalendar(params).getClass());
    manager.useCalendar(new CalendarBuilder()
        .name("Work")
        .buildUse());
    assertEquals("Work", manager.getCalendarName());
    assertEquals(ZoneId.of("America/Los_Angeles"), manager.getTimeZone());
  }

  @Test
  public void testEditCalendarChangeTimezoneConvertsAllEvents() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Work")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Work")
        .buildUse());

    CalendarModelInterface calendar = manager.getActiveCalendar();
    calendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Morning Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build());

    calendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Afternoon Meeting")
        .start("2025-01-15T14:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .build());

    calendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Evening Meeting")
        .start("2025-01-15T18:00:00-05:00[America/New_York]")
        .end("2025-01-15T19:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> eventsBeforeChange = calendar.getAllEvents();
    assertEquals(3, eventsBeforeChange.size());
    for (CalendarEvent event : eventsBeforeChange) {
      assertEquals(ZoneId.of("America/New_York"), event.getStartDateTime().getZone());
      assertEquals(ZoneId.of("America/New_York"), event.getEndDateTime().getZone());
    }

    manager.editCalendar(new TestBuilders.CalendarBuilder()
        .name("Work")
        .property("timezone")
        .value(ZoneId.of("America/Los_Angeles"))
        .buildEdit());

    CalendarModelInterface updatedCalendar = manager.getActiveCalendar();
    assertEquals(ZoneId.of("America/Los_Angeles"), updatedCalendar.getTimezone());

    List<CalendarEvent> eventsAfterChange = updatedCalendar.getAllEvents();
    assertEquals(3, eventsAfterChange.size());

    for (CalendarEvent event : eventsAfterChange) {
      assertEquals(ZoneId.of("America/Los_Angeles"), event.getStartDateTime().getZone());
      assertEquals(ZoneId.of("America/Los_Angeles"), event.getEndDateTime().getZone());
    }

    assertEquals(7, eventsAfterChange.get(0).getStartDateTime().getHour());
    assertEquals(11, eventsAfterChange.get(1).getStartDateTime().getHour());
    assertEquals(15, eventsAfterChange.get(2).getStartDateTime().getHour());
  }

  @Test
  public void testEditCalendarChangeTimezonePreservesAbsoluteMoment() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Work")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Work")
        .buildUse());

    CalendarModelInterface calendar = manager.getActiveCalendar();
    calendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build());

    ZonedDateTime originalStart = calendar.getAllEvents().get(0).getStartDateTime();
    long originalEpoch = originalStart.toInstant().toEpochMilli();

    manager.editCalendar(new TestBuilders.CalendarBuilder()
        .name("Work")
        .property("timezone")
        .value(ZoneId.of("Europe/London"))
        .buildEdit());

    CalendarModelInterface updatedCalendar = manager.getActiveCalendar();
    ZonedDateTime newStart = updatedCalendar.getAllEvents().get(0).getStartDateTime();
    long newEpoch = newStart.toInstant().toEpochMilli();

    assertEquals(originalEpoch, newEpoch);
    assertEquals(ZoneId.of("Europe/London"), newStart.getZone());
    assertEquals(15, newStart.getHour());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNonExistentCalendar() {
    params.put("calname", "NonExistent");
    params.put("property", "name");
    params.put("value", "NewName");

    manager.editCalendar(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNullProperty() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", null);
    params.put("value", "NewName");

    manager.editCalendar(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNullValue() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "name");
    params.put("value", null);

    manager.editCalendar(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidProperty() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "invalid");
    params.put("value", "something");

    manager.editCalendar(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarWrongTypeForName() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "name");
    params.put("value", 123);

    manager.editCalendar(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarWrongTypeForTimezone() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    params.put("property", "timezone");
    params.put("value", "America/New_York");

    manager.editCalendar(params);
  }

  @Test
  public void testCopyEventsBetweenWithinSameCalendar() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Work")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Work")
        .buildUse());

    CalendarModelInterface workCalendar = manager.getActiveCalendar();

    workCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Kickoff Meeting")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T10:30:00-05:00[America/New_York]")
        .location("physical")
        .description("Project kickoff")
        .build());

    workCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Design Review")
        .start("2025-01-16T11:00:00-05:00[America/New_York]")
        .end("2025-01-16T12:30:00-05:00[America/New_York]")
        .location("online")
        .build());

    workCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Implementation Sprint")
        .start("2025-01-16T14:00:00-05:00[America/New_York]")
        .end("2025-01-17T18:00:00-05:00[America/New_York]")
        .status("public")
        .build());

    workCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Code Review")
        .start("2025-01-17T10:00:00-05:00[America/New_York]")
        .end("2025-01-17T11:00:00-05:00[America/New_York]")
        .location("online")
        .build());

    assertEquals(4, workCalendar.getAllEvents().size());

    manager.copyEventsBetween(new TestBuilders.CopyBuilder()
        .target("Work")
        .startdate(LocalDate.of(2025, 1, 15))
        .enddate(LocalDate.of(2025, 1, 17))
        .targetdate(LocalDate.of(2025, 3, 1))
        .buildCopyEventsBetween());

    List<CalendarEvent> allEvents = workCalendar.getAllEvents();
    allEvents.sort(Comparator.comparing(CalendarEvent::getStartDateTime));

    assertEquals(8, allEvents.size());

    assertEquals("Kickoff Meeting", allEvents.get(4).getSubject());
    assertEquals(LocalDate.of(2025, 3, 1),
        allEvents.get(4).getStartDateTime().toLocalDate());
    assertEquals(9, allEvents.get(4).getStartDateTime().getHour());
    assertEquals(10, allEvents.get(4).getEndDateTime().getHour());
    assertEquals(30, allEvents.get(4).getEndDateTime().getMinute());
    assertEquals("physical", allEvents.get(4).getLocation());
    assertEquals("Project kickoff", allEvents.get(4).getDescription());

    assertEquals("Design Review", allEvents.get(5).getSubject());
    assertEquals(LocalDate.of(2025, 3, 2),
        allEvents.get(5).getStartDateTime().toLocalDate());
    assertEquals(11, allEvents.get(5).getStartDateTime().getHour());
    assertEquals("online", allEvents.get(5).getLocation());

    assertEquals("Implementation Sprint", allEvents.get(6).getSubject());
    assertEquals(LocalDate.of(2025, 3, 2),
        allEvents.get(6).getStartDateTime().toLocalDate());
    assertEquals(14, allEvents.get(6).getStartDateTime().getHour());
    assertEquals(LocalDate.of(2025, 3, 3),
        allEvents.get(6).getEndDateTime().toLocalDate());
    assertEquals("public", allEvents.get(6).getStatus());

    assertEquals("Code Review", allEvents.get(7).getSubject());
    assertEquals(LocalDate.of(2025, 3, 3),
        allEvents.get(7).getStartDateTime().toLocalDate());
    assertEquals(10, allEvents.get(7).getStartDateTime().getHour());
    assertEquals("online", allEvents.get(7).getLocation());

    List<CalendarEvent> originalEvents = workCalendar.getEventsInRange(
        ZonedDateTime.parse("2025-01-15T00:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-01-18T00:00:00-05:00[America/New_York]"));
    assertEquals(4, originalEvents.size());

    List<CalendarEvent> copiedEvents = workCalendar.getEventsInRange(
        ZonedDateTime.parse("2025-03-01T00:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-03-04T00:00:00-05:00[America/New_York]"));
    assertEquals(4, copiedEvents.size());
  }

  @Test
  public void testCopyEventToTargetCalendarWithVerification() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .location("physical")
        .description("Important client meeting")
        .build());

    assertEquals(1, sourceCalendar.getAllEvents().size());

    manager.copyEvent(new TestBuilders.CopyBuilder()
        .target("Target")
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .targetstart("2025-01-20T14:00:00-05:00[America/New_York]")
        .buildCopyEvent());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar.getAllEvents();

    assertEquals(1, copiedEvents.size());

    CalendarEvent copiedEvent = copiedEvents.get(0);
    assertEquals("Meeting", copiedEvent.getSubject());
    assertEquals(ZonedDateTime.parse("2025-01-20T14:00:00-05:00[America/New_York]"),
        copiedEvent.getStartDateTime());
    assertEquals(ZonedDateTime.parse("2025-01-20T15:00:00-05:00[America/New_York]"),
        copiedEvent.getEndDateTime());
    assertEquals("physical", copiedEvent.getLocation());
    assertEquals("Important client meeting", copiedEvent.getDescription());

    assertEquals(1, sourceCalendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventPreservesDuration() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("America/Los_Angeles")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Long Meeting")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T12:30:00-05:00[America/New_York]")
        .build());

    manager.copyEvent(new TestBuilders.CopyBuilder()
        .target("Target")
        .subject("Long Meeting")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .targetstart("2025-02-01T10:00:00-08:00[America/Los_Angeles]")
        .buildCopyEvent());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    CalendarEvent copiedEvent = targetCalendar.getAllEvents().get(0);

    assertEquals(ZonedDateTime.parse("2025-02-01T10:00:00-08:00[America/Los_Angeles]"),
        copiedEvent.getStartDateTime());
    assertEquals(ZonedDateTime.parse("2025-02-01T13:30:00-08:00[America/Los_Angeles]"),
        copiedEvent.getEndDateTime());

    long durationMinutes = java.time.Duration.between(
        copiedEvent.getStartDateTime(),
        copiedEvent.getEndDateTime()).toMinutes();
    assertEquals(210, durationMinutes);
  }

  @Test
  public void testCopyEventDoesNotModifySource() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Original Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build());

    CalendarEvent originalEvent = sourceCalendar.getAllEvents().get(0);
    ZonedDateTime originalStart = originalEvent.getStartDateTime();
    ZonedDateTime originalEnd = originalEvent.getEndDateTime();

    manager.copyEvent(new TestBuilders.CopyBuilder()
        .target("Target")
        .subject("Original Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .targetstart("2025-02-01T14:00:00-05:00[America/New_York]")
        .buildCopyEvent());

    CalendarEvent sourceEventAfterCopy = sourceCalendar.getAllEvents().get(0);
    assertEquals(originalStart, sourceEventAfterCopy.getStartDateTime());
    assertEquals(originalEnd, sourceEventAfterCopy.getEndDateTime());
    assertEquals("Original Meeting", sourceEventAfterCopy.getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsBetweenNoActiveCalendar() {
    params.put("target", "Target");
    params.put("startdate", LocalDate.of(2025, 1, 15));
    params.put("enddate", LocalDate.of(2025, 1, 20));
    params.put("targetdate", LocalDate.of(2025, 2, 1));

    manager.copyEventsBetween(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventTargetDoesNotExist() {
    params.put("calname", "Source");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Source");
    manager.useCalendar(params);

    params.clear();
    params.put("target", "NonExistent");
    params.put("subject", "Meeting");
    params.put("start", ZonedDateTime.now());
    params.put("targetstart", ZonedDateTime.now().plusDays(1));

    manager.copyEvent(params);
  }

  @Test
  public void testCopyEventsOnCopiesToTargetDateWithVerification() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Morning Meeting")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T10:00:00-05:00[America/New_York]")
        .location("online")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Lunch")
        .start("2025-01-15T12:00:00-05:00[America/New_York]")
        .end("2025-01-15T13:00:00-05:00[America/New_York]")
        .location("online")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Afternoon Sync")
        .start("2025-01-15T15:00:00-05:00[America/New_York]")
        .end("2025-01-15T16:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> sourceEvents = sourceCalendar
        .getEventsOn(LocalDate.of(2025, 1, 15));
    assertEquals(3, sourceEvents.size());

    manager.copyEventsOn(new TestBuilders.CopyBuilder()
        .target("Target")
        .sourcedate(LocalDate.of(2025, 1, 15))
        .targetdate(LocalDate.of(2025, 1, 20))
        .buildCopyEventsOn());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar
        .getEventsOn(LocalDate.of(2025, 1, 20));

    assertEquals(3, copiedEvents.size());

    assertEquals("Morning Meeting", copiedEvents.get(0).getSubject());
    assertEquals(LocalDate.of(2025, 1, 20),
        copiedEvents.get(0).getStartDateTime().toLocalDate());
    assertEquals(9, copiedEvents.get(0).getStartDateTime().getHour());
    assertEquals(10, copiedEvents.get(0).getEndDateTime().getHour());
    assertEquals("online", copiedEvents.get(0).getLocation());

    assertEquals("Lunch", copiedEvents.get(1).getSubject());
    assertEquals(LocalDate.of(2025, 1, 20),
        copiedEvents.get(1).getStartDateTime().toLocalDate());
    assertEquals(12, copiedEvents.get(1).getStartDateTime().getHour());
    assertEquals("online", copiedEvents.get(1).getLocation());

    assertEquals("Afternoon Sync", copiedEvents.get(2).getSubject());
    assertEquals(15, copiedEvents.get(2).getStartDateTime().getHour());

    List<CalendarEvent> sourceStillHasEvents = sourceCalendar
        .getEventsOn(LocalDate.of(2025, 1, 15));
    assertEquals(3, sourceStillHasEvents.size());

    List<CalendarEvent> targetDoesNotHaveSourceDate = targetCalendar
        .getEventsOn(LocalDate.of(2025, 1, 15));
    assertEquals(0, targetDoesNotHaveSourceDate.size());
  }

  @Test
  public void testCopyMultipleEventsOnDayAcrossTimezones() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Work")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Personal")
        .timezone("Asia/Tokyo")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Work")
        .buildUse());

    CalendarModelInterface workCalendar = manager.getActiveCalendar();

    workCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Team Planning")
        .start("2025-01-15T11:00:00-05:00[America/New_York]")
        .end("2025-01-15T12:00:00-05:00[America/New_York]")
        .location("physical")
        .description("Q1 planning session")
        .build());

    workCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Lunch Break")
        .start("2025-01-15T12:30:00-05:00[America/New_York]")
        .end("2025-01-15T13:30:00-05:00[America/New_York]")
        .build());

    workCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Client Meeting")
        .start("2025-01-15T15:00:00-05:00[America/New_York]")
        .end("2025-01-15T16:30:00-05:00[America/New_York]")
        .location("online")
        .status("public")
        .build());

    workCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Different Day Event")
        .start("2025-01-16T10:00:00-05:00[America/New_York]")
        .end("2025-01-16T11:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> sourceEventsOnJan15 = workCalendar
        .getEventsOn(LocalDate.of(2025, 1, 15));
    assertEquals(3, sourceEventsOnJan15.size());

    manager.copyEventsOn(new TestBuilders.CopyBuilder()
        .target("Personal")
        .sourcedate(LocalDate.of(2025, 1, 15))
        .targetdate(LocalDate.of(2025, 2, 10))
        .buildCopyEventsOn());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Personal")
        .buildUse());

    CalendarModelInterface personalCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = personalCalendar
        .getEventsOn(LocalDate.of(2025, 2, 10));
    copiedEvents.sort(Comparator.comparing(CalendarEvent::getStartDateTime));

    assertEquals(3, copiedEvents.size());

    assertEquals("Team Planning", copiedEvents.get(0).getSubject());
    assertEquals(LocalDate.of(2025, 2, 10),
        copiedEvents.get(0).getStartDateTime().toLocalDate());
    assertEquals(ZoneId.of("Asia/Tokyo"), copiedEvents.get(0).getStartDateTime().getZone());
    assertEquals(1, copiedEvents.get(0).getStartDateTime().getHour());
    assertEquals(2, copiedEvents.get(0).getEndDateTime().getHour());
    assertEquals("physical", copiedEvents.get(0).getLocation());
    assertEquals("Q1 planning session", copiedEvents.get(0).getDescription());

    assertEquals("Lunch Break", copiedEvents.get(1).getSubject());
    assertEquals(LocalDate.of(2025, 2, 10),
        copiedEvents.get(1).getStartDateTime().toLocalDate());
    assertEquals(2, copiedEvents.get(1).getStartDateTime().getHour());
    assertEquals(30, copiedEvents.get(1).getStartDateTime().getMinute());

    assertEquals("Client Meeting", copiedEvents.get(2).getSubject());
    assertEquals(LocalDate.of(2025, 2, 10),
        copiedEvents.get(2).getStartDateTime().toLocalDate());
    assertEquals(5, copiedEvents.get(2).getStartDateTime().getHour());
    assertEquals(6, copiedEvents.get(2).getEndDateTime().getHour());
    assertEquals(30, copiedEvents.get(2).getEndDateTime().getMinute());
    assertEquals("online", copiedEvents.get(2).getLocation());
    assertEquals("public", copiedEvents.get(2).getStatus());

    List<CalendarEvent> eventsNotOnSourceDate = personalCalendar
        .getEventsOn(LocalDate.of(2025, 1, 15));
    assertEquals(0, eventsNotOnSourceDate.size());

    assertEquals(4, workCalendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventsOnAcrossTimezonesConvertsCorrectly() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("Asia/Tokyo")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Early Meeting")
        .start("2025-01-15T08:00:00-05:00[America/New_York]")
        .end("2025-01-15T09:00:00-05:00[America/New_York]")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Late Meeting")
        .start("2025-01-15T20:00:00-05:00[America/New_York]")
        .end("2025-01-15T21:00:00-05:00[America/New_York]")
        .build());

    manager.copyEventsOn(new TestBuilders.CopyBuilder()
        .target("Target")
        .sourcedate(LocalDate.of(2025, 1, 15))
        .targetdate(LocalDate.of(2025, 1, 20))
        .buildCopyEventsOn());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar.getAllEvents();

    assertEquals(2, copiedEvents.size());

    assertEquals("Late Meeting", copiedEvents.get(0).getSubject());
    assertEquals(ZoneId.of("Asia/Tokyo"), copiedEvents.get(0).getStartDateTime().getZone());
    assertEquals(10, copiedEvents.get(0).getStartDateTime().getHour());

    assertEquals("Early Meeting", copiedEvents.get(1).getSubject());
    assertEquals(ZoneId.of("Asia/Tokyo"), copiedEvents.get(1).getStartDateTime().getZone());
    assertEquals(22, copiedEvents.get(1).getStartDateTime().getHour());
    assertEquals(LocalDate.of(2025, 1, 20),
        copiedEvents.get(1).getStartDateTime().toLocalDate());
  }

  @Test
  public void testCopyEventsOnThrowsErrorWhenDuplicateExists() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Team Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .location("online")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Design Review")
        .start("2025-01-15T14:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .build());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();

    targetCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Team Meeting")
        .start("2025-02-10T10:00:00-05:00[America/New_York]")
        .end("2025-02-10T11:00:00-05:00[America/New_York]")
        .build());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    try {
      manager.copyEventsOn(new TestBuilders.CopyBuilder()
          .target("Target")
          .sourcedate(LocalDate.of(2025, 1, 15))
          .targetdate(LocalDate.of(2025, 2, 10))
          .buildCopyEventsOn());
      fail("Should throw duplicate error");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("duplicate") || e.getMessage().contains("already exists"));
    }
  }

  @Test
  public void testCopyEventsBetweenThrowsErrorWhenDuplicateExists() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Morning Sync")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T10:00:00-05:00[America/New_York]")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Status Update")
        .start("2025-01-16T11:00:00-05:00[America/New_York]")
        .end("2025-01-16T12:00:00-05:00[America/New_York]")
        .location("physical")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Planning Session")
        .start("2025-01-17T14:00:00-05:00[America/New_York]")
        .end("2025-01-17T15:30:00-05:00[America/New_York]")
        .build());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();

    targetCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Status Update")
        .start("2025-03-02T11:00:00-05:00[America/New_York]")
        .end("2025-03-02T12:00:00-05:00[America/New_York]")
        .build());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    try {
      manager.copyEventsBetween(new TestBuilders.CopyBuilder()
          .target("Target")
          .startdate(LocalDate.of(2025, 1, 15))
          .enddate(LocalDate.of(2025, 1, 17))
          .targetdate(LocalDate.of(2025, 3, 1))
          .buildCopyEventsBetween());
      fail("Should throw duplicate error");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("duplicate") || e.getMessage().contains("already exists"));
    }
  }

  @Test
  public void testCopyEventsOnThrowsErrorWhenMultipleDuplicates() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting A")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T10:00:00-05:00[America/New_York]")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting B")
        .start("2025-01-15T11:00:00-05:00[America/New_York]")
        .end("2025-01-15T12:00:00-05:00[America/New_York]")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting C")
        .start("2025-01-15T14:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .build());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();

    targetCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting A")
        .start("2025-02-20T09:00:00-05:00[America/New_York]")
        .end("2025-02-20T10:00:00-05:00[America/New_York]")
        .build());

    targetCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting B")
        .start("2025-02-20T11:00:00-05:00[America/New_York]")
        .end("2025-02-20T12:00:00-05:00[America/New_York]")
        .build());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    try {
      manager.copyEventsOn(new TestBuilders.CopyBuilder()
          .target("Target")
          .sourcedate(LocalDate.of(2025, 1, 15))
          .targetdate(LocalDate.of(2025, 2, 20))
          .buildCopyEventsOn());
      fail("Should throw duplicate error");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("duplicate")
          || e.getMessage().contains("already exists"));
    }
  }

  @Test
  public void testCopyEventsBetweenCopiesDateRangeWithVerification() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Day 1 Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .location("physical")
        .description("First day event")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Day 2 Meeting")
        .start("2025-01-16T10:00:00-05:00[America/New_York]")
        .end("2025-01-16T11:00:00-05:00[America/New_York]")
        .location("online")
        .description("Second day event")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Day 3 Meeting")
        .start("2025-01-17T14:00:00-05:00[America/New_York]")
        .end("2025-01-17T15:00:00-05:00[America/New_York]")
        .location("physical")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Outside Range")
        .start("2025-01-18T10:00:00-05:00[America/New_York]")
        .end("2025-01-18T11:00:00-05:00[America/New_York]")
        .build());

    assertEquals(4, sourceCalendar.getAllEvents().size());

    manager.copyEventsBetween(new TestBuilders.CopyBuilder()
        .target("Target")
        .startdate(LocalDate.of(2025, 1, 15))
        .enddate(LocalDate.of(2025, 1, 17))
        .targetdate(LocalDate.of(2025, 2, 1))
        .buildCopyEventsBetween());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar.getAllEvents();

    assertEquals(3, copiedEvents.size());

    assertEquals("Day 1 Meeting", copiedEvents.get(0).getSubject());
    assertEquals(LocalDate.of(2025, 2, 1), copiedEvents.get(0).getStartDateTime().toLocalDate());
    assertEquals(10, copiedEvents.get(0).getStartDateTime().getHour());
    assertEquals("physical", copiedEvents.get(0).getLocation());
    assertEquals("First day event", copiedEvents.get(0).getDescription());

    assertEquals("Day 2 Meeting", copiedEvents.get(1).getSubject());
    assertEquals(LocalDate.of(2025, 2, 2), copiedEvents.get(1).getStartDateTime().toLocalDate());
    assertEquals(10, copiedEvents.get(1).getStartDateTime().getHour());
    assertEquals("online", copiedEvents.get(1).getLocation());

    assertEquals("Day 3 Meeting", copiedEvents.get(2).getSubject());
    assertEquals(LocalDate.of(2025, 2, 3), copiedEvents.get(2).getStartDateTime().toLocalDate());
    assertEquals(14, copiedEvents.get(2).getStartDateTime().getHour());
    assertEquals("physical", copiedEvents.get(2).getLocation());

    assertEquals(4, sourceCalendar.getAllEvents().size());
  }

  @Test
  public void testCopyEventsBetweenAcrossTimezonesShiftsCorrectly() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("Europe/London")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Morning Standup")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T09:30:00-05:00[America/New_York]")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Evening Review")
        .start("2025-01-16T18:00:00-05:00[America/New_York]")
        .end("2025-01-16T19:00:00-05:00[America/New_York]")
        .build());

    manager.copyEventsBetween(new TestBuilders.CopyBuilder()
        .target("Target")
        .startdate(LocalDate.of(2025, 1, 15))
        .enddate(LocalDate.of(2025, 1, 16))
        .targetdate(LocalDate.of(2025, 3, 1))
        .buildCopyEventsBetween());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar.getAllEvents();

    assertEquals(2, copiedEvents.size());

    assertEquals("Morning Standup", copiedEvents.get(0).getSubject());
    assertEquals(LocalDate.of(2025, 3, 1), copiedEvents.get(0).getStartDateTime().toLocalDate());
    assertEquals(ZoneId.of("Europe/London"), copiedEvents.get(0).getStartDateTime().getZone());
    assertEquals(14, copiedEvents.get(0).getStartDateTime().getHour());

    assertEquals("Evening Review", copiedEvents.get(1).getSubject());
    assertEquals(LocalDate.of(2025, 3, 2), copiedEvents.get(1).getStartDateTime().toLocalDate());
    assertEquals(ZoneId.of("Europe/London"), copiedEvents.get(1).getStartDateTime().getZone());
    assertEquals(23, copiedEvents.get(1).getStartDateTime().getHour());

    long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(
        LocalDate.of(2025, 1, 15),
        copiedEvents.get(0).getStartDateTime().toLocalDate());
    assertEquals(45, dayOffset);
  }

  @Test
  public void testCopyEventsBetweenOnlyIncludesEventsInRange() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Before Range")
        .start("2025-01-14T10:00:00-05:00[America/New_York]")
        .end("2025-01-14T11:00:00-05:00[America/New_York]")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("In Range 1")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("In Range 2")
        .start("2025-01-16T10:00:00-05:00[America/New_York]")
        .end("2025-01-16T11:00:00-05:00[America/New_York]")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("After Range")
        .start("2025-01-18T10:00:00-05:00[America/New_York]")
        .end("2025-01-18T11:00:00-05:00[America/New_York]")
        .build());

    manager.copyEventsBetween(new TestBuilders.CopyBuilder()
        .target("Target")
        .startdate(LocalDate.of(2025, 1, 15))
        .enddate(LocalDate.of(2025, 1, 16))
        .targetdate(LocalDate.of(2025, 2, 1))
        .buildCopyEventsBetween());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    assertEquals(2, targetCalendar.getAllEvents().size());

    List<String> subjects = new ArrayList<>();
    for (CalendarEvent event : targetCalendar.getAllEvents()) {
      subjects.add(event.getSubject());
    }

    assertTrue(subjects.contains("In Range 1"));
    assertTrue(subjects.contains("In Range 2"));
    assertFalse(subjects.contains("Before Range"));
    assertFalse(subjects.contains("After Range"));
  }

  @Test
  public void testCopyEventsBetweenWithMultipleEvents() {
    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new TestBuilders.CalendarBuilder()
        .name("Destination")
        .timezone("America/Chicago")
        .buildCreate());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Team Retrospective")
        .start("2025-01-14T14:00:00-05:00[America/New_York]")
        .end("2025-01-14T18:00:00-05:00[America/New_York]")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Product Launch")
        .start("2025-01-14T22:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .location("physical")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Daily Standup")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T10:00:00-05:00[America/New_York]")
        .location("online")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Engineering Review")
        .start("2025-01-15T13:00:00-05:00[America/New_York]")
        .end("2025-01-17T17:00:00-05:00[America/New_York]")
        .description("Architecture planning")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Sprint Planning")
        .start("2025-01-16T10:00:00-05:00[America/New_York]")
        .end("2025-01-16T13:00:00-05:00[America/New_York]")
        .status("private")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Client Presentation")
        .start("2025-01-17T19:00:00-05:00[America/New_York]")
        .end("2025-01-18T09:00:00-05:00[America/New_York]")
        .location("physical")
        .build());

    sourceCalendar.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Budget Review")
        .start("2025-01-19T11:00:00-05:00[America/New_York]")
        .end("2025-01-19T14:00:00-05:00[America/New_York]")
        .build());

    assertEquals(7, sourceCalendar.getAllEvents().size());

    manager.copyEventsBetween(new TestBuilders.CopyBuilder()
        .target("Destination")
        .startdate(LocalDate.of(2025, 1, 15))
        .enddate(LocalDate.of(2025, 1, 17))
        .targetdate(LocalDate.of(2025, 3, 1))
        .buildCopyEventsBetween());

    manager.useCalendar(new TestBuilders.CalendarBuilder()
        .name("Destination")
        .buildUse());

    CalendarModelInterface destCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = destCalendar.getAllEvents();
    copiedEvents.sort(Comparator.comparing(CalendarEvent::getStartDateTime));

    assertEquals(5, copiedEvents.size());

    assertEquals("Product Launch", copiedEvents.get(0).getSubject());
    assertEquals(LocalDate.of(2025, 2, 28), copiedEvents.get(0).getStartDateTime().toLocalDate());
    assertEquals(21, copiedEvents.get(0).getStartDateTime().getHour());
    assertEquals(LocalDate.of(2025, 3, 1), copiedEvents.get(0).getEndDateTime().toLocalDate());
    assertEquals("physical", copiedEvents.get(0).getLocation());

    assertEquals("Daily Standup", copiedEvents.get(1).getSubject());
    assertEquals(LocalDate.of(2025, 3, 1), copiedEvents.get(1).getStartDateTime().toLocalDate());
    assertEquals(8, copiedEvents.get(1).getStartDateTime().getHour());
    assertEquals("online", copiedEvents.get(1).getLocation());

    assertEquals("Engineering Review", copiedEvents.get(2).getSubject());
    assertEquals(LocalDate.of(2025, 3, 1), copiedEvents.get(2).getStartDateTime().toLocalDate());
    assertEquals(12, copiedEvents.get(2).getStartDateTime().getHour());
    assertEquals(LocalDate.of(2025, 3, 3), copiedEvents.get(2).getEndDateTime().toLocalDate());
    assertEquals("Architecture planning", copiedEvents.get(2).getDescription());

    assertEquals("Sprint Planning", copiedEvents.get(3).getSubject());
    assertEquals(LocalDate.of(2025, 3, 2), copiedEvents.get(3).getStartDateTime().toLocalDate());
    assertEquals(9, copiedEvents.get(3).getStartDateTime().getHour());
    assertEquals("private", copiedEvents.get(3).getStatus());

    assertEquals("Client Presentation", copiedEvents.get(4).getSubject());
    assertEquals(LocalDate.of(2025, 3, 3), copiedEvents.get(4).getStartDateTime().toLocalDate());
    assertEquals(18, copiedEvents.get(4).getStartDateTime().getHour());
    assertEquals(LocalDate.of(2025, 3, 4), copiedEvents.get(4).getEndDateTime().toLocalDate());
    assertEquals("physical", copiedEvents.get(4).getLocation());

    assertEquals(7, sourceCalendar.getAllEvents().size());
  }

  @Test
  public void testCompleteWorkflow() {
    params.put("calname", "MyCalendar");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "MyCalendar");
    manager.useCalendar(params);

    assertEquals("MyCalendar", manager.getCalendarName());
    assertNotNull(manager.getActiveCalendar());

    params.clear();
    params.put("calname", "MyCalendar");
    params.put("property", "name");
    params.put("value", "WorkCalendar");
    manager.editCalendar(params);

    assertEquals("WorkCalendar", manager.getCalendarName());

    params.clear();
    params.put("calname", "WorkCalendar");
    params.put("property", "timezone");
    params.put("value", ZoneId.of("America/Los_Angeles"));
    manager.editCalendar(params);

    assertEquals("WorkCalendar", manager.getCalendarName());
  }

  @Test
  public void testMultipleCalendarsWorkflow() {
    params.put("calname", "Work");
    params.put("timezone", ZoneId.of("America/New_York"));
    manager.createCalendar(params);

    params.put("calname", "Personal");
    params.put("timezone", ZoneId.of("America/Los_Angeles"));
    manager.createCalendar(params);

    params.put("calname", "Travel");
    params.put("timezone", ZoneId.of("Europe/London"));
    manager.createCalendar(params);

    params.clear();
    params.put("calname", "Work");
    manager.useCalendar(params);
    assertEquals("Work", manager.getCalendarName());
    assertEquals(ZoneId.of("America/New_York"), manager.getTimeZone());

    params.put("calname", "Personal");
    manager.useCalendar(params);
    assertEquals("Personal", manager.getCalendarName());
    assertEquals(ZoneId.of("America/Los_Angeles"), manager.getTimeZone());

    params.put("calname", "Travel");
    manager.useCalendar(params);
    assertEquals("Travel", manager.getCalendarName());

    assertEquals(ZoneId.of("Europe/London"), manager.getTimeZone());
  }

  @Test
  public void testEditCalendarChangeTimezoneWithSeriesEvents() {
    manager.createCalendar(new CalendarBuilder()
        .name("Work")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new CalendarBuilder()
        .name("Work")
        .buildUse());

    CalendarModelInterface calendar = manager.getActiveCalendar();
    calendar.createEventSeries(new EventBuilder()
        .subject("Weekly Standup")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .end("2025-01-13T11:00:00-05:00[America/New_York]")
        .weekdays("MW")
        .ndays(10)
        .build());

    List<CalendarEvent> eventsBeforeChange = calendar.getEventsOn(LocalDate.of(2025, 1,
        13));
    assertEquals(1, eventsBeforeChange.size());
    assertEquals(10, eventsBeforeChange.get(0).getStartDateTime().getHour());

    manager.editCalendar(new CalendarBuilder()
        .name("Work")
        .property("timezone")
        .value(ZoneId.of("America/Los_Angeles"))
        .buildEdit());

    CalendarModelInterface updatedCalendar = manager.getActiveCalendar();
    List<CalendarEvent> eventsAfterChange = updatedCalendar.getEventsOn(LocalDate.of(2025,
        1, 13));
    assertEquals(1, eventsAfterChange.size());
    assertEquals(7, eventsAfterChange.get(0).getStartDateTime().getHour());
    assertEquals(ZoneId.of("America/Los_Angeles"),
        eventsAfterChange.get(0).getStartDateTime().getZone());
  }

  @Test
  public void testEditCalendarChangeTimezonePreservesSeriesRelationship() {
    manager.createCalendar(new CalendarBuilder()
        .name("Work")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new CalendarBuilder()
        .name("Work")
        .buildUse());

    CalendarModelInterface calendar = manager.getActiveCalendar();
    calendar.createEventSeries(new EventBuilder()
        .subject("Team Sync")
        .start("2025-01-13T14:00:00-05:00[America/New_York]")
        .end("2025-01-13T15:00:00-05:00[America/New_York]")
        .weekdays("MWF")
        .ndays(7)
        .build());

    String originalSeriesUid = calendar.getEventsOn(LocalDate.of(2025, 1,
            13))
        .get(0).getSeriesUid();
    assertNotNull(originalSeriesUid);

    manager.editCalendar(new CalendarBuilder()
        .name("Work")
        .property("timezone")
        .value(ZoneId.of("Europe/London"))
        .buildEdit());

    CalendarModelInterface updatedCalendar = manager.getActiveCalendar();
    List<CalendarEvent> mondayEvents = updatedCalendar.getEventsOn(LocalDate.of(2025,
        1, 13));
    List<CalendarEvent> wednesdayEvents = updatedCalendar.getEventsOn(LocalDate.of(2025,
        1, 15));
    List<CalendarEvent> fridayEvents = updatedCalendar.getEventsOn(LocalDate.of(2025,
        1, 17));

    assertEquals(1, mondayEvents.size());
    assertEquals(1, wednesdayEvents.size());
    assertEquals(1, fridayEvents.size());

    String newSeriesUid = mondayEvents.get(0).getSeriesUid();
    assertNotNull(newSeriesUid);
    assertEquals(newSeriesUid, wednesdayEvents.get(0).getSeriesUid());
    assertEquals(newSeriesUid, fridayEvents.get(0).getSeriesUid());
  }

  @Test
  public void testEditCalendarChangeTimezonePreservesSeriesRelationshipAndAllowsEditing() {
    manager.createCalendar(new CalendarBuilder()
        .name("Work")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new CalendarBuilder()
        .name("Work")
        .buildUse());

    CalendarModelInterface calendar = manager.getActiveCalendar();

    calendar.createEventSeries(new EventBuilder()
        .subject("Team Sync")
        .description("Initial Description")
        .start("2025-01-13T14:00:00-05:00[America/New_York]")
        .end("2025-01-13T15:00:00-05:00[America/New_York]")
        .weekdays("MWF")
        .ndays(7)
        .build());

    String originalSeriesUid = calendar
        .getEventsOn(LocalDate.of(2025, 1, 13))
        .get(0).getSeriesUid();
    assertNotNull(originalSeriesUid);

    manager.editCalendar(new CalendarBuilder()
        .name("Work")
        .property("timezone")
        .value(ZoneId.of("Europe/London"))
        .buildEdit());

    CalendarModelInterface updatedCalendar = manager.getActiveCalendar();

    List<CalendarEvent> mondayEvent = updatedCalendar
        .getEventsOn(LocalDate.of(2025, 1, 13));
    List<CalendarEvent> wednesdayEvent = updatedCalendar
        .getEventsOn(LocalDate.of(2025, 1, 15));
    List<CalendarEvent> fridayEvent = updatedCalendar
        .getEventsOn(LocalDate.of(2025, 1, 17));

    String newSeriesUid = mondayEvent.get(0).getSeriesUid();
    assertNotNull(newSeriesUid);
    assertEquals(newSeriesUid, wednesdayEvent.get(0).getSeriesUid());
    assertEquals(newSeriesUid, fridayEvent.get(0).getSeriesUid());

    updatedCalendar.editSeries(new EventBuilder()
        .subject("Team Sync")
        .start("2025-01-13T14:00:00-05:00[America/New_York]")
        .property("description")
        .value("Updated Description")
        .build());

    List<CalendarEvent> allEvents = updatedCalendar.getAllEvents();
    assertEquals(7, allEvents.size());

    for (CalendarEvent event : allEvents) {
      assertEquals("All series events should share the updated description after edit",
          event.getDescription(),
          "Updated Description");
      assertEquals("Series UID must remain consistent across timezone change and edit",
          event.getSeriesUid(), newSeriesUid
      );
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarChangeTimezoneCreatesMultiDaySeriesEvent() {
    manager.createCalendar(new CalendarBuilder()
        .name("Work")
        .timezone("America/New_York")
        .buildCreate());

    manager.useCalendar(new CalendarBuilder()
        .name("Work")
        .buildUse());

    CalendarModelInterface calendar = manager.getActiveCalendar();
    calendar.createEventSeries(new EventBuilder()
        .subject("Late Meeting")
        .start("2025-01-13T09:30:00-05:00[America/New_York]")
        .end("2025-01-13T10:30:00-05:00[America/New_York]")
        .weekdays("M")
        .ndays(7)
        .build());

    manager.editCalendar(new CalendarBuilder()
        .name("Work")
        .property("timezone")
        .value(ZoneId.of("Asia/Tokyo"))
        .buildEdit());
  }

  @Test
  public void testCopySeriesEventToCalendarWithDifferentTimezone() {
    manager.createCalendar(new CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new CalendarBuilder()
        .name("Target")
        .timezone("America/Los_Angeles")
        .buildCreate());

    manager.useCalendar(new CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createEventSeries(new EventBuilder()
        .subject("Standup")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .end("2025-01-13T10:30:00-05:00[America/New_York]")
        .weekdays("MW")
        .ndays(10)
        .build());

    manager.copyEvent(new CopyBuilder()
        .target("Target")
        .subject("Standup")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .targetstart("2025-01-20T14:00:00-08:00[America/Los_Angeles]")
        .buildCopyEvent());

    manager.useCalendar(new CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar.getEventsOn(LocalDate.of(2025, 1,
        20));

    assertEquals(1, copiedEvents.size());
    assertEquals(14, copiedEvents.get(0).getStartDateTime().getHour());
    assertEquals(ZoneId.of("America/Los_Angeles"),
        copiedEvents.get(0).getStartDateTime().getZone());
  }

  @Test
  public void testCopyEventsOnWithSeriesAcrossTimezones() {
    manager.createCalendar(new CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new CalendarBuilder()
        .name("Target")
        .timezone("Asia/Tokyo")
        .buildCreate());

    manager.useCalendar(new CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createEventSeries(new EventBuilder()
        .subject("Daily Standup")
        .start("2025-01-13T09:00:00-05:00[America/New_York]")
        .end("2025-01-13T09:30:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(7)
        .build());

    List<CalendarEvent> sourceEvents = sourceCalendar.getEventsOn(LocalDate.of(2025, 1,
        15));
    assertEquals(1, sourceEvents.size());
    assertEquals(9, sourceEvents.get(0).getStartDateTime().getHour());
    String originalSeriesUid = sourceEvents.get(0).getSeriesUid();
    assertNotNull(originalSeriesUid);

    manager.copyEventsOn(new CopyBuilder()
        .target("Target")
        .sourcedate(LocalDate.of(2025, 1, 15))
        .targetdate(LocalDate.of(2025, 1, 20))
        .buildCopyEventsOn());

    manager.useCalendar(new CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar.getEventsOn(LocalDate.of(2025, 1,
        20));

    assertEquals(1, copiedEvents.size());
    assertEquals(23, copiedEvents.get(0).getStartDateTime().getHour());
    assertEquals(ZoneId.of("Asia/Tokyo"),
        copiedEvents.get(0).getStartDateTime().getZone());

    String copiedSeriesUid = copiedEvents.get(0).getSeriesUid();
    assertNotNull(copiedSeriesUid);
  }

  @Test
  public void testCopyEventsBetweenWithSeriesAcrossTimezones() {
    manager.createCalendar(new CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new CalendarBuilder()
        .name("Target")
        .timezone("Europe/London")
        .buildCreate());

    manager.useCalendar(new CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createEventSeries(new EventBuilder()
        .subject("Team Meeting")
        .start("2025-01-13T15:00:00-05:00[America/New_York]")
        .end("2025-01-13T16:00:00-05:00[America/New_York]")
        .weekdays("MWF")
        .ndays(14)
        .build());

    ZonedDateTime rangeStart =
        LocalDate.of(2025, 1, 13)
            .atStartOfDay(ZoneId.of("America/New_York"));
    ZonedDateTime rangeEnd = LocalDate.of(2025, 1, 18)
        .atStartOfDay(ZoneId.of("America/New_York"));
    List<CalendarEvent> sourceEvents = sourceCalendar.getEventsInRange(rangeStart, rangeEnd);
    assertEquals(3, sourceEvents.size());

    manager.copyEventsBetween(new CopyBuilder()
        .target("Target")
        .startdate(LocalDate.of(2025, 1, 13))
        .enddate(LocalDate.of(2025, 1, 17))
        .targetdate(LocalDate.of(2025, 2, 1))
        .buildCopyEventsBetween());

    manager.useCalendar(new CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    ZonedDateTime targetRangeStart =
        LocalDate.of(2025, 2, 1)
            .atStartOfDay(ZoneId.of("Europe/London"));
    ZonedDateTime targetRangeEnd =
        LocalDate.of(2025, 2, 6)
            .atStartOfDay(ZoneId.of("Europe/London"));
    List<CalendarEvent> copiedEvents =
        targetCalendar.getEventsInRange(targetRangeStart, targetRangeEnd);

    assertEquals(3, copiedEvents.size());
    assertEquals(LocalDate.of(2025, 2, 1),
        copiedEvents.get(0).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 2, 3),
        copiedEvents.get(1).getStartDateTime().toLocalDate());
    assertEquals(LocalDate.of(2025, 2, 5),
        copiedEvents.get(2).getStartDateTime().toLocalDate());
    assertEquals(20, copiedEvents.get(0).getStartDateTime().getHour());

    String seriesUid = copiedEvents.get(0).getSeriesUid();
    assertNotNull(seriesUid);
    assertEquals(seriesUid, copiedEvents.get(1).getSeriesUid());
    assertEquals(seriesUid, copiedEvents.get(2).getSeriesUid());
  }

  @Test
  public void testCopyMultipleSeriesEventsPreservesIndependentSeries() {
    manager.createCalendar(new CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new CalendarBuilder()
        .name("Target")
        .timezone("America/Los_Angeles")
        .buildCreate());

    manager.useCalendar(new CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createEventSeries(new EventBuilder()
        .subject("Series A")
        .start("2025-01-13T09:00:00-05:00[America/New_York]")
        .end("2025-01-13T10:00:00-05:00[America/New_York]")
        .weekdays("MW")
        .ndays(7)
        .build());

    sourceCalendar.createEventSeries(new EventBuilder()
        .subject("Series B")
        .start("2025-01-13T14:00:00-05:00[America/New_York]")
        .end("2025-01-13T15:00:00-05:00[America/New_York]")
        .weekdays("MW")
        .ndays(7)
        .build());

    manager.copyEventsOn(new CopyBuilder()
        .target("Target")
        .sourcedate(LocalDate.of(2025, 1, 13))
        .targetdate(LocalDate.of(2025, 1, 20))
        .buildCopyEventsOn());

    manager.useCalendar(new CalendarBuilder()
        .name("Target")
        .buildUse());

    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar.getEventsOn(LocalDate.of(2025, 1,
        20));

    assertEquals(2, copiedEvents.size());

    String seriesA = copiedEvents.get(0).getSeriesUid();
    String seriesB = copiedEvents.get(1).getSeriesUid();

    assertNotNull(seriesA);
    assertNotNull(seriesB);
    assertNotEquals(seriesA, seriesB);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsBetweenWithSeriesCreatesMultiDayEvent() {
    manager.createCalendar(new CalendarBuilder()
        .name("Source")
        .timezone("America/New_York")
        .buildCreate());

    manager.createCalendar(new CalendarBuilder()
        .name("Target")
        .timezone("Pacific/Auckland")
        .buildCreate());

    manager.useCalendar(new CalendarBuilder()
        .name("Source")
        .buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createEventSeries(new EventBuilder()
        .subject("Late Night Meeting")
        .start("2025-01-13T05:30:00-05:00[America/New_York]")
        .end("2025-01-13T06:30:00-05:00[America/New_York]")
        .weekdays("MW")
        .ndays(7)
        .build());

    manager.copyEventsBetween(new CopyBuilder()
        .target("Target")
        .startdate(LocalDate.of(2025, 1, 13))
        .enddate(LocalDate.of(2025, 1, 15))
        .targetdate(LocalDate.of(2025, 2, 1))
        .buildCopyEventsBetween());
  }

  @Test
  public void testCopyEventActuallyCopiesToTarget() {
    manager.createCalendar(new CalendarBuilder().name("Source")
        .timezone("America/New_York").buildCreate());
    manager.createCalendar(new CalendarBuilder().name("Target")
        .timezone("America/Los_Angeles").buildCreate());

    manager.useCalendar(new CalendarBuilder().name("Source").buildUse());
    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();

    sourceCalendar.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-10T10:00:00-05:00[America/New_York]")
        .end("2025-01-10T11:00:00-05:00[America/New_York]")
        .build()
    );

    manager.copyEvent(new CopyBuilder()
        .target("Target")
        .subject("Meeting")
        .start("2025-01-10T10:00:00-05:00[America/New_York]")
        .targetstart("2025-01-11T07:00:00-08:00[America/Los_Angeles]")
        .buildCopyEvent()
    );

    manager.useCalendar(new CalendarBuilder().name("Target").buildUse());
    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar
        .getEventsOn(LocalDate.of(2025, 1, 11));

    assertEquals(1, copiedEvents.size());
    assertEquals("Meeting", copiedEvents.get(0).getSubject());
    assertEquals(7, copiedEvents.get(0).getStartDateTime().getHour());
    assertEquals(ZoneId.of("America/Los_Angeles"),
        copiedEvents.get(0).getStartDateTime().getZone());
  }

  @Test
  public void testCopyEventsOnActuallyCopiesToTarget() {
    manager.createCalendar(new CalendarBuilder().name("Source")
        .timezone("America/New_York").buildCreate());
    manager.createCalendar(new CalendarBuilder().name("Target")
        .timezone("Asia/Tokyo").buildCreate());

    manager.useCalendar(new CalendarBuilder().name("Source").buildUse());
    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();

    sourceCalendar.createSingleEvent(new EventBuilder()
        .subject("Daily Standup")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T09:30:00-05:00[America/New_York]")
        .build()
    );

    manager.copyEventsOn(new CopyBuilder()
        .target("Target")
        .sourcedate(LocalDate.of(2025, 1, 15))
        .targetdate(LocalDate.of(2025, 1, 20))
        .buildCopyEventsOn()
    );

    manager.useCalendar(new CalendarBuilder().name("Target").buildUse());
    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar
        .getEventsOn(LocalDate.of(2025, 1, 20));

    assertEquals(1, copiedEvents.size());
    assertEquals("Daily Standup", copiedEvents.get(0).getSubject());
    assertEquals(23, copiedEvents.get(0).getStartDateTime().getHour());
    assertEquals(ZoneId.of("Asia/Tokyo"), copiedEvents.get(0).getStartDateTime().getZone());
  }

  @Test
  public void testCopyEventsBetweenActuallyCopiesToTarget() {
    manager.createCalendar(new CalendarBuilder().name("Source")
        .timezone("America/New_York").buildCreate());
    manager.createCalendar(new CalendarBuilder().name("Target")
        .timezone("Europe/London").buildCreate());

    manager.useCalendar(new CalendarBuilder().name("Source").buildUse());
    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();

    sourceCalendar.createSingleEvent(new EventBuilder()
        .subject("Day 1 Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build()
    );

    sourceCalendar.createSingleEvent(new EventBuilder()
        .subject("Day 2 Meeting")
        .start("2025-01-16T10:00:00-05:00[America/New_York]")
        .end("2025-01-16T11:00:00-05:00[America/New_York]")
        .build()
    );

    manager.copyEventsBetween(new CopyBuilder()
        .target("Target")
        .startdate(LocalDate.of(2025, 1, 15))
        .enddate(LocalDate.of(2025, 1, 16))
        .targetdate(LocalDate.of(2025, 2, 1))
        .buildCopyEventsBetween()
    );

    manager.useCalendar(new CalendarBuilder().name("Target").buildUse());
    CalendarModelInterface targetCalendar = manager.getActiveCalendar();
    List<CalendarEvent> copiedEvents = targetCalendar.getEventsInRange(
        LocalDate.of(2025, 2, 1)
            .atStartOfDay(ZoneId.of("Europe/London")),
        LocalDate.of(2025, 2, 3)
            .atStartOfDay(ZoneId.of("Europe/London"))
    );

    assertEquals(2, copiedEvents.size());
    assertEquals("Day 1 Meeting", copiedEvents.get(0).getSubject());
    assertEquals("Day 2 Meeting", copiedEvents.get(1).getSubject());
    assertEquals(15, copiedEvents.get(0).getStartDateTime().getHour());
  }

  @Test
  public void testCopyEventFailsWithoutActiveCalendar() {
    manager.createCalendar(new CalendarBuilder().name("Target")
        .timezone("America/Los_Angeles").buildCreate());

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      manager.copyEvent(new CopyBuilder()
          .target("Target")
          .subject("Meeting")
          .start("2025-01-10T10:00:00-05:00[America/New_York]")
          .targetstart("2025-01-11T07:00:00-08:00[America/Los_Angeles]")
          .buildCopyEvent()
      );
    });

    assertTrue(exception.getMessage().contains("There is no active calendar in use."));
  }

  @Test
  public void testCopyEventFailsIfTargetCalendarDoesNotExist() {
    manager.createCalendar(new CalendarBuilder().name("Source")
        .timezone("America/New_York").buildCreate());
    manager.useCalendar(new CalendarBuilder().name("Source").buildUse());

    CalendarModelInterface sourceCalendar = manager.getActiveCalendar();
    sourceCalendar.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-10T10:00:00-05:00[America/New_York]")
        .end("2025-01-10T11:00:00-05:00[America/New_York]")
        .build()
    );

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      manager.copyEvent(new CopyBuilder()
          .target("NonExistent")
          .subject("Meeting")
          .start("2025-01-10T10:00:00-05:00[America/New_York]")
          .targetstart("2025-01-11T07:00:00-08:00[America/Los_Angeles]")
          .buildCopyEvent()
      );
    });

    assertTrue(exception.getMessage().contains("Calendar 'NonExistent' does not exist"));
  }

  @Test
  public void testCopyEventFailsIfSourceEventDoesNotExist() {
    manager.createCalendar(new CalendarBuilder().name("Source")
        .timezone("America/New_York").buildCreate());
    manager.createCalendar(new CalendarBuilder().name("Target")
        .timezone("America/Los_Angeles").buildCreate());
    manager.useCalendar(new CalendarBuilder().name("Source").buildUse());

    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      manager.copyEvent(new CopyBuilder()
          .target("Target")
          .subject("NonExistentEvent")
          .start("2025-01-10T10:00:00-05:00[America/New_York]")
          .targetstart("2025-01-11T07:00:00-08:00[America/Los_Angeles]")
          .buildCopyEvent()
      );
    });

    assertTrue(exception.getMessage().contains("Event not found"));
  }

  @Test
  public void testCopyEventFailsIfEventAlreadyExists() {
    manager.createCalendar(new CalendarBuilder().name("Source")
        .timezone("America/New_York").buildCreate());
    manager.createCalendar(new CalendarBuilder().name("Target")
        .timezone("America/Los_Angeles").buildCreate());

    manager.useCalendar(new CalendarBuilder().name("Source").buildUse());
    CalendarModelInterface source = manager.getActiveCalendar();

    source.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-10T10:00:00-05:00[America/New_York]")
        .end("2025-01-10T11:00:00-05:00[America/New_York]")
        .build()
    );

    manager.useCalendar(new CalendarBuilder().name("Target").buildUse());
    CalendarModelInterface target = manager.getActiveCalendar();
    target.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-10T07:00:00-08:00[America/Los_Angeles]")
        .end("2025-01-10T08:00:00-08:00[America/Los_Angeles]")
        .build()
    );

    manager.useCalendar(new CalendarBuilder().name("Source").buildUse());
    Map<String, Object> params = new CopyBuilder()
        .target("Target")
        .subject("Meeting")
        .start("2025-01-10T10:00:00-05:00[America/New_York]")
        .targetstart("2025-01-10T07:00:00-08:00[America/Los_Angeles]")
        .buildCopyEvent();

    assertThrows(IllegalArgumentException.class, () -> manager.copyEvent(params));
  }

  @Test
  public void testCopySeriesEventFailsIfConflictInTarget() {
    manager.createCalendar(new CalendarBuilder().name("Source")
        .timezone("America/New_York").buildCreate());
    manager.createCalendar(new CalendarBuilder().name("Target")
        .timezone("Europe/London").buildCreate());

    manager.useCalendar(new CalendarBuilder().name("Source").buildUse());
    CalendarModelInterface source = manager.getActiveCalendar();

    source.createSingleEvent(new EventBuilder()
        .subject("Weekly Sync")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T10:00:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(5)
        .untildate("2025-01-19")
        .build()
    );

    manager.useCalendar(new CalendarBuilder().name("Target").buildUse());
    CalendarModelInterface target = manager.getActiveCalendar();

    target.createSingleEvent(new EventBuilder()
        .subject("Weekly Sync")
        .start("2025-01-22T14:00:00+00:00[Europe/London]")
        .end("2025-01-22T15:00:00+00:00[Europe/London]")
        .build()
    );

    manager.useCalendar(new CalendarBuilder().name("Source").buildUse());
    Map<String, Object> params = new CopyBuilder()
        .target("Target")
        .startdate(LocalDate.of(2025, 1, 15))
        .enddate(LocalDate.of(2025, 1, 19))
        .targetdate(LocalDate.of(2025, 1, 22))
        .buildCopyEventsBetween();

    assertThrows(IllegalArgumentException.class, () -> manager.copyEventsBetween(params));
  }

}