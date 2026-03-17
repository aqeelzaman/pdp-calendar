package calendar.model;

import static calendar.model.TestBuilders.EventBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.model.event.CalendarEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test suite for the CalendarModel class.
 * Tests all core functionality including event creation, editing, querying,
 * and series management operations.
 */
public class CalendarModelTest {

  private CalendarModel model;
  private ZoneId timezone;

  /**
   * Sets up a fresh CalendarModel instance before each test.
   */
  @Before
  public void setUp() {
    timezone = ZoneId.of("America/New_York");
    model = new CalendarModel(timezone);
  }

  @Test
  public void testCreateSingleEvent() {
    Map<String, Object> params = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build();

    model.createSingleEvent(params);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test
  public void testCreateMultipleSingleEvents() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting 1")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Meeting 2")
        .start("2025-11-05T14:00:00-05:00[America/New_York]")
        .end("2025-11-05T15:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(2, events.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateDuplicateSingleEvent() {
    Map<String, Object> params = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build();

    model.createSingleEvent(params);
    model.createSingleEvent(params);
  }

  @Test
  public void testCreateMultiDaySingleEvent() {
    Map<String, Object> params = new EventBuilder()
        .subject("Conference")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-06T17:00:00-05:00[America/New_York]")
        .build();

    model.createSingleEvent(params);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
  }

  @Test
  public void testCreateEventSeries() {
    Map<String, Object> params = new EventBuilder()
        .subject("Daily Standup")
        .start("2025-11-04T09:00:00-05:00[America/New_York]")
        .end("2025-11-04T09:30:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(6)
        .build();

    model.createEventSeries(params);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(6, events.size());

    for (CalendarEvent event : events) {
      assertEquals("Daily Standup", event.getSubject());
      assertNotNull(event.getSeriesUid());
    }
  }

  @Test
  public void testCreateSeriesWithSingleDay() {
    Map<String, Object> params = new EventBuilder()
        .subject("One Time Event")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(1)
        .build();

    model.createEventSeries(params);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertNull(events.get(0).getSeriesUid());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesWithMultiDayEvent() {
    Map<String, Object> params = new EventBuilder()
        .subject("Invalid Series")
        .start("2025-11-04T23:00:00-05:00[America/New_York]")
        .end("2025-11-05T01:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(3)
        .build();

    model.createEventSeries(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesWithNoWeekdayValue() {
    Map<String, Object> params = new EventBuilder()
        .subject("Invalid Series")
        .start("2025-11-04T23:00:00-05:00[America/New_York]")
        .end("2025-11-05T01:00:00-05:00[America/New_York]")
        .ndays(3)
        .build();

    model.createEventSeries(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesWithEmptyWeekdayValue() {
    Map<String, Object> params = new EventBuilder()
        .subject("Invalid Series")
        .start("2025-11-04T23:00:00-05:00[America/New_York]")
        .end("2025-11-05T01:00:00-05:00[America/New_York]")
        .weekdays("    ")
        .ndays(3)
        .build();

    model.createEventSeries(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesWithIncorrectWeekdayValue() {
    Map<String, Object> params = new EventBuilder()
        .subject("Invalid Series")
        .start("2025-11-04T23:00:00-05:00[America/New_York]")
        .end("2025-11-05T01:00:00-05:00[America/New_York]")
        .weekdays("XYZ")
        .ndays(3)
        .build();

    model.createEventSeries(params);
  }

  @Test
  public void testEditSingleEventSubject() {
    model.createSingleEvent(new EventBuilder()
        .subject("Old Subject")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Old Subject")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .property("subject")
        .value("New Subject")
        .build();

    model.editSingleEvent(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("New Subject", events.get(0).getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSingleEventStartTimeInvalid() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-04T14:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams);
  }

  @Test
  public void testEditSingleEventStartTimeValid() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T14:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T14:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-04T11:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(11, events.get(0).getStartDateTime().getHour());
    assertEquals(14, events.get(0).getEndDateTime().getHour());
  }

  @Test
  public void testEditSingleEventEndTime() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .property("end")
        .valueAsDateTime("2025-11-04T12:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(12, events.get(0).getEndDateTime().getHour());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSingleEventCreatesDuplicate() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T14:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T14:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-04T10:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams);

    Map<String, Object> editParams2 = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .property("end")
        .valueAsDateTime("2025-11-04T11:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditNonExistentEvent() {
    Map<String, Object> editParams = new EventBuilder()
        .subject("NonExistent")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .property("subject")
        .value("New Name")
        .build();

    model.editSingleEvent(editParams);
  }

  @Test
  public void testEditEntireSeriesSubject() {
    model.createEventSeries(new EventBuilder()
        .subject("Old Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(6)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Old Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("New Series")
        .build();

    model.editSeries(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(6, events.size());
    for (CalendarEvent event : events) {
      assertEquals("New Series", event.getSubject());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesWithMultipleCandidates() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T12:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated Meeting")
        .build();

    model.editSeries(editParams);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesToMultiDay() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("end")
        .valueAsDateTime("2025-11-05T11:00:00-05:00[America/New_York]")
        .build();

    model.editSeries(editParams);
  }

  @Test
  public void testEditEventsFromDateSubject() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(6)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-07T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated Series")
        .build();

    model.editEvents(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(6, events.size());

    int oldCount = 0;
    int newCount = 0;
    for (CalendarEvent event : events) {
      if (event.getSubject().equals("Series")) {
        oldCount++;
      }
      if (event.getSubject().equals("Updated Series")) {
        newCount++;
      }
    }

    assertEquals(1, oldCount);
    assertEquals(5, newCount);
  }

  @Test
  public void testEditEventsStartTimeSplitsSeries() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(6)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-11T10:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-11T14:00:00-05:00[America/New_York]")
        .build();

    model.editEvents(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(6, events.size());

    int morning = 0;
    int afternoon = 0;
    for (CalendarEvent event : events) {
      if (event.getStartDateTime().getHour() == 10) {
        morning++;
      }
      if (event.getStartDateTime().getHour() == 14) {
        afternoon++;
      }
    }

    assertEquals(3, morning);
    assertEquals(3, afternoon);
  }

  @Test
  public void testGetAllEvents() {
    model.createSingleEvent(new EventBuilder()
        .subject("Event 1")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Event 2")
        .start("2025-11-05T14:00:00-05:00[America/New_York]")
        .end("2025-11-05T15:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(2, events.size());
  }

  @Test
  public void testGetEventsOn() {
    model.createSingleEvent(new EventBuilder()
        .subject("Event 1")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Event 2")
        .start("2025-11-05T14:00:00-05:00[America/New_York]")
        .end("2025-11-05T15:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> events = model
        .getEventsOn(LocalDate.of(2025, 11, 4));
    assertEquals(1, events.size());
    assertEquals("Event 1", events.get(0).getSubject());
  }

  @Test
  public void testGetEventsInRange() {
    model.createSingleEvent(new EventBuilder()
        .subject("Event 1")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Event 2")
        .start("2025-11-06T14:00:00-05:00[America/New_York]")
        .end("2025-11-06T15:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Event 3")
        .start("2025-11-10T14:00:00-05:00[America/New_York]")
        .end("2025-11-10T15:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> events = model.getEventsInRange(
        ZonedDateTime.parse("2025-11-05T00:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-11-08T00:00:00-05:00[America/New_York]"));

    assertEquals(1, events.size());
    assertEquals("Event 2", events.get(0).getSubject());
  }

  @Test
  public void testCheckAvailability() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    assertFalse(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T10:30:00-05:00[America/New_York]")));

    assertFalse(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]")));

    assertTrue(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T11:00:00-05:00[America/New_York]")));

    assertTrue(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T09:00:00-05:00[America/New_York]")));

    assertTrue(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T12:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testEditSingleEventInSeries() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Modified Event")
        .build();

    model.editSingleEvent(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(3, events.size());

    int seriesCount = 0;
    int modifiedCount = 0;
    for (CalendarEvent event : events) {
      if (event.getSubject().equals("Series")) {
        seriesCount++;
      }
      if (event.getSubject().equals("Modified Event")) {
        modifiedCount++;
      }
    }

    assertEquals(2, seriesCount);
    assertEquals(1, modifiedCount);
  }

  @Test
  public void testEditEntireSeriesStartTime() {
    model.createEventSeries(new EventBuilder()
        .subject("Daily Standup")
        .start("2025-11-04T09:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(6)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Daily Standup")
        .start("2025-11-04T09:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-04T10:00:00-05:00[America/New_York]")
        .build();

    model.editSeries(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(6, events.size());

    for (CalendarEvent event : events) {
      assertEquals(10, event.getStartDateTime().getHour());
      assertEquals(11, event.getEndDateTime().getHour());
    }
  }

  @Test
  public void testCreateEventSeriesUntilDate() {
    Map<String, Object> params = new EventBuilder()
        .subject("Weekly Meeting")
        .start("2025-11-03T14:00:00-05:00[America/New_York]")
        .end("2025-11-03T15:00:00-05:00[America/New_York]")
        .weekdays("M")
        .untildate("2025-11-24")
        .build();

    model.createEventSeries(params);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(4, events.size());

    for (CalendarEvent event : events) {
      assertEquals(java.time.DayOfWeek.MONDAY, event.getStartDateTime().getDayOfWeek());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditStartTimeRemovesFromSeriesInvalid() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-04T14:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams);
  }

  @Test
  public void testEditStartTimeRemovesFromSeriesValid() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-04T12:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(3, events.size());

    int inSeries = 0;
    int standalone = 0;
    for (CalendarEvent event : events) {
      if (event.getSeriesUid() != null) {
        inSeries++;
      }
      if (event.getSeriesUid() == null) {
        standalone++;
      }
    }

    assertEquals(2, inSeries);
    assertEquals(1, standalone);

    for (CalendarEvent event : events) {
      if (event.getSeriesUid() == null) {
        assertEquals(12, event.getStartDateTime().getHour());
      }
    }
  }

  @Test
  public void testCreateAllDaySeriesUntilDate() {
    Map<String, Object> params = new EventBuilder()
        .subject("Vacation")
        .ondate("2025-11-03")
        .weekdays("MTWRF")
        .untildate("2025-11-24")
        .build();

    model.createAllDayEventSeries(params);
    assertEquals(16, model.getAllEvents().size());
  }

  @Test
  public void testCreateSeriesWithOneOccurrence() {
    Map<String, Object> params = new EventBuilder()
        .subject("One Time")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(1)
        .build();

    model.createEventSeries(params);
    assertEquals(1, model.getAllEvents().size());
    assertNull(model.getAllEvents().get(0).getSeriesUid());
  }

  @Test
  public void testCreateEventWithValidLocation() {
    Map<String, Object> params = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .location("physical")
        .build();

    model.createSingleEvent(params);
    assertEquals("physical", model.getAllEvents().get(0).getLocation());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithInvalidLocation() {
    Map<String, Object> params = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .location("invalid_location")
        .build();

    model.createSingleEvent(params);
  }

  @Test
  public void testCreateEventWithValidStatus() {
    Map<String, Object> params = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .status("private")
        .build();

    model.createSingleEvent(params);
    assertEquals("private", model.getAllEvents().get(0).getStatus());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithInvalidStatus() {
    Map<String, Object> params = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .status("confidential")
        .build();

    model.createSingleEvent(params);
  }

  @Test
  public void testEditEventLocationValid() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .property("location")
        .value("online")
        .build();

    model.editSingleEvent(editParams);
    assertEquals("online", model.getAllEvents().get(0).getLocation());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventLocationInvalid() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .property("location")
        .value("zoom")
        .build();

    model.editSingleEvent(editParams);
  }

  @Test
  public void testMultiDayEventAvailability() {
    model.createSingleEvent(new EventBuilder()
        .subject("Conference")
        .start("2025-11-04T08:00:00-05:00[America/New_York]")
        .end("2025-11-06T17:00:00-05:00[America/New_York]")
        .build());

    assertFalse(model.checkAvailability(
        ZonedDateTime.parse("2025-11-05T14:00:00-05:00[America/New_York]")));

    assertTrue(model.checkAvailability(
        ZonedDateTime.parse("2025-11-03T14:00:00-05:00[America/New_York]")));

    assertTrue(model.checkAvailability(
        ZonedDateTime.parse("2025-11-07T14:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testCreateSeriesWithNdays() {
    Map<String, Object> params = new EventBuilder()
        .subject("Standup")
        .start("2025-11-03T09:00:00-05:00[America/New_York]")
        .end("2025-11-03T09:30:00-05:00[America/New_York]")
        .weekdays("MTW")
        .ndays(9)
        .build();

    model.createEventSeries(params);
    assertEquals(9, model.getAllEvents().size());
  }

  @Test
  public void testCreateSeriesWithUntilDate() {
    Map<String, Object> params = new EventBuilder()
        .subject("Weekly Sync")
        .start("2025-11-03T14:00:00-05:00[America/New_York]")
        .end("2025-11-03T15:00:00-05:00[America/New_York]")
        .weekdays("M")
        .untildate("2025-11-24")
        .build();

    model.createEventSeries(params);
    assertEquals(4, model.getAllEvents().size());
  }

  @Test
  public void testCreateAllDayEvent() {
    Map<String, Object> params = new EventBuilder()
        .subject("Holiday")
        .ondate("2025-11-04")
        .build();

    model.createAllDayEvent(params);

    CalendarEvent event = model.getAllEvents().get(0);
    assertEquals(8, event.getStartDateTime().getHour());
    assertEquals(17, event.getEndDateTime().getHour());
  }

  @Test
  public void testCreateSeriesWithSameStartAndEndDate() {
    Map<String, Object> params = new EventBuilder()
        .subject("Same Day")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .untildate("2025-11-04")
        .build();

    model.createEventSeries(params);
    assertEquals(1, model.getAllEvents().size());
    assertNull(model.getAllEvents().get(0).getSeriesUid());
  }

  @Test
  public void testEditLongSeriesLastThird() {
    model.createEventSeries(new EventBuilder()
        .subject("Daily Meeting")
        .start("2025-11-03T10:00:00-05:00[America/New_York]")
        .end("2025-11-03T14:00:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(30)
        .build());

    assertEquals(30, model.getAllEvents().size());

    List<CalendarEvent> allEvents = model.getAllEvents();
    CalendarEvent eventAt20 = allEvents.get(20);
    ZonedDateTime newStart = eventAt20.getStartDateTime().plusHours(2);

    Map<String, Object> editParams = new EventBuilder()
        .subject("Daily Meeting")
        .start(eventAt20.getStartDateTime().toString())
        .property("start")
        .valueAsDateTime(newStart.toString())
        .build();

    model.editEvents(editParams);

    List<CalendarEvent> updated = model.getAllEvents();
    assertEquals(30, updated.size());

    int morningMeetings = 0;
    int afternoonMeetings = 0;
    for (CalendarEvent event : updated) {
      if (event.getStartDateTime().getHour() == 10) {
        morningMeetings++;
      }
      if (event.getStartDateTime().getHour() == 12) {
        afternoonMeetings++;
      }
    }

    assertEquals(20, morningMeetings);
    assertEquals(10, afternoonMeetings);

    Set<String> seriesUids = new java.util.HashSet<>();
    for (CalendarEvent event : updated) {
      if (event.getSeriesUid() != null) {
        seriesUids.add(event.getSeriesUid());
      }
    }
    assertEquals(2, seriesUids.size());
  }

  @Test
  public void testEditLongSeriesMiddleThird() {
    model.createEventSeries(new EventBuilder()
        .subject("Daily Standup")
        .start("2025-11-03T09:00:00-05:00[America/New_York]")
        .end("2025-11-03T10:30:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(30)
        .build());

    List<CalendarEvent> allEvents = model.getAllEvents();
    assertEquals(30, allEvents.size());

    CalendarEvent eventAt10 = allEvents.get(10);

    Map<String, Object> editParams = new EventBuilder()
        .subject("Daily Standup")
        .start(eventAt10.getStartDateTime().toString())
        .property("start")
        .valueAsDateTime(eventAt10.getStartDateTime().withHour(10).toString())
        .build();

    model.editEvents(editParams);

    List<CalendarEvent> updated = model.getAllEvents();
    assertEquals(30, updated.size());

    int at9am = 0;
    int at10am = 0;
    for (CalendarEvent event : updated) {
      if (event.getStartDateTime().getHour() == 9) {
        at9am++;
      }
      if (event.getStartDateTime().getHour() == 10) {
        at10am++;
      }
    }

    assertEquals("First 10 events (0-9) at 09:00", 10, at9am);
    assertEquals("Last 20 events (10-29) at 10:00", 20, at10am);
  }

  @Test
  public void testCountSeriesAfterSubjectEdits() {
    model.createEventSeries(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-03T10:00:00-05:00[America/New_York]")
        .end("2025-11-03T11:00:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(15)
        .build());

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(15, events.size());

    CalendarEvent event5 = events.get(5);

    model.editEvents(new EventBuilder()
        .subject("Meeting")
        .start(event5.getStartDateTime().toString())
        .property("subject")
        .value("Meeting Part 2")
        .build());

    events = model.getAllEvents();

    CalendarEvent eventPart2 = null;
    for (CalendarEvent e : events) {
      if (e.getSubject().equals("Meeting Part 2")) {
        eventPart2 = e;
        break;
      }
    }

    assertNotNull("Should have found 'Meeting Part 2'", eventPart2);

    model.editEvents(new EventBuilder()
        .subject("Meeting Part 2")
        .start(eventPart2.getStartDateTime().toString())
        .property("subject")
        .value("Meeting Part 3")
        .build());

    Map<String, Integer> seriesCounts = new HashMap<>();
    for (CalendarEvent event : model.getAllEvents()) {
      if (event.getSeriesUid() != null) {
        seriesCounts.put(event.getSeriesUid(),
            seriesCounts.getOrDefault(event.getSeriesUid(), 0) + 1);
      }
    }

    int meetingCount = 0;
    int part2Count = 0;
    int part3Count = 0;

    for (CalendarEvent event : model.getAllEvents()) {
      if (event.getSubject().equals("Meeting")) {
        meetingCount++;
      }
      if (event.getSubject().equals("Meeting Part 2")) {
        part2Count++;
      }
      if (event.getSubject().equals("Meeting Part 3")) {
        part3Count++;
      }
    }

    assertTrue("Should have original meetings", meetingCount > 0);
    assertTrue("Should have Part 2 meetings", part2Count > 0);
    assertTrue("Should have Part 3 meetings", part3Count > 0);
    assertEquals(15, meetingCount + part2Count + part3Count);
  }

  @Test
  public void testSplitLongSeriesMultipleTimes() {
    model.createEventSeries(new EventBuilder()
        .subject("Daily Work")
        .start("2025-11-03T09:00:00-05:00[America/New_York]")
        .end("2025-11-03T17:00:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(30)
        .build());

    List<CalendarEvent> allEvents = model.getAllEvents();
    assertEquals(30, allEvents.size());

    for (CalendarEvent event : allEvents) {
      assertEquals(9, event.getStartDateTime().getHour());
    }

    CalendarEvent event20 = allEvents.get(20);

    model.editEvents(new EventBuilder()
        .subject("Daily Work")
        .start(event20.getStartDateTime().toString())
        .property("start")
        .valueAsDateTime(event20.getStartDateTime().withHour(14).toString())
        .build());

    allEvents = model.getAllEvents();
    assertEquals(30, allEvents.size());

    int at9am = 0;
    int at14pm = 0;
    for (CalendarEvent event : allEvents) {
      if (event.getStartDateTime().getHour() == 9) {
        at9am++;
      }
      if (event.getStartDateTime().getHour() == 14) {
        at14pm++;
      }
    }

    assertEquals("First 20 events should be at 09:00", 20, at9am);
    assertEquals("Last 10 events should be at 14:00", 10, at14pm);

    CalendarEvent event10 = null;
    int count = 0;
    for (CalendarEvent event : allEvents) {
      if (event.getStartDateTime().getHour() == 9) {
        if (count == 10) {
          event10 = event;
          break;
        }
        count++;
      }
    }

    assertNotNull("Should find event at position 10", event10);

    model.editEvents(new EventBuilder()
        .subject("Daily Work")
        .start(event10.getStartDateTime().toString())
        .property("start")
        .valueAsDateTime(event10.getStartDateTime().withHour(12).toString())
        .build());

    allEvents = model.getAllEvents();
    assertEquals(30, allEvents.size());

    at9am = 0;
    int at12pm = 0;
    at14pm = 0;

    for (CalendarEvent event : allEvents) {
      int hour = event.getStartDateTime().getHour();
      if (hour == 9) {
        at9am++;
      } else if (hour == 12) {
        at12pm++;
      } else if (hour == 14) {
        at14pm++;
      }
    }

    assertEquals("First 10 events (0-9) should be at 09:00", 10, at9am);
    assertEquals("Middle 10 events (10-19) should be at 12:00", 10, at12pm);
    assertEquals("Last 10 events (20-29) should be at 14:00", 10, at14pm);

    Set<String> seriesUids = new java.util.HashSet<>();
    for (CalendarEvent event : allEvents) {
      if (event.getSeriesUid() != null) {
        seriesUids.add(event.getSeriesUid());
      }
    }

    assertEquals("Should have 3 separate series after 2 splits", 3, seriesUids.size());

    Map<String, Integer> seriesCounts = new HashMap<>();
    for (CalendarEvent event : allEvents) {
      if (event.getSeriesUid() != null) {
        seriesCounts.put(event.getSeriesUid(),
            seriesCounts.getOrDefault(event.getSeriesUid(), 0) + 1);
      }
    }

    for (Integer i : seriesCounts.values()) {
      assertEquals("Each series should have 10 events", 10, i.intValue());
    }
  }

  @Test
  public void testEditStartTimeToDifferentDayChangesDate() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-03T10:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams);

    CalendarEvent updated = model.getAllEvents().get(0);
    assertEquals(3, updated.getStartDateTime().getDayOfMonth());
    assertEquals(4, updated.getEndDateTime().getDayOfMonth());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesEventToDifferentDayInvalid() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-05T10:00:00-05:00[America/New_York]")
        .build();

    model.editSeries(editParams);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditWithMultipleCandidatesFails() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T12:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated Meeting")
        .build();

    model.editSeries(editParams);
  }

  @Test
  public void testQueryEventsOnSpecificDate() {
    model.createSingleEvent(new EventBuilder()
        .subject("Event 1")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Event 2")
        .start("2025-11-04T14:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Event 3")
        .start("2025-11-05T10:00:00-05:00[America/New_York]")
        .end("2025-11-05T11:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> eventsOnNov4 = model.getEventsOn(LocalDate.of(2025, 11, 4));
    assertEquals(2, eventsOnNov4.size());
  }

  @Test
  public void testQueryEventsInRange() {
    model.createSingleEvent(new EventBuilder()
        .subject("Event 1")
        .start("2025-11-03T10:00:00-05:00[America/New_York]")
        .end("2025-11-03T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Event 2")
        .start("2025-11-05T10:00:00-05:00[America/New_York]")
        .end("2025-11-05T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Event 3")
        .start("2025-11-07T10:00:00-05:00[America/New_York]")
        .end("2025-11-07T11:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Event 4")
        .start("2025-11-10T10:00:00-05:00[America/New_York]")
        .end("2025-11-10T11:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> eventsInRange = model.getEventsInRange(
        ZonedDateTime.parse("2025-11-04T00:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-11-08T00:00:00-05:00[America/New_York]"));

    assertEquals(2, eventsInRange.size());
  }

  @Test
  public void testCheckBusyAtSpecificTime() {
    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    assertFalse(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T10:30:00-05:00[America/New_York]")));

    assertFalse(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]")));

    assertTrue(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T11:00:00-05:00[America/New_York]")));

    assertTrue(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T09:00:00-05:00[America/New_York]")));

    assertTrue(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T12:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testCreateAllDaySeriesNdays() {
    Map<String, Object> params = new EventBuilder()
        .subject("Training")
        .ondate("2025-11-03")
        .weekdays("MTW")
        .ndays(6)
        .build();

    model.createAllDayEventSeries(params);
    assertEquals(6, model.getAllEvents().size());

    for (CalendarEvent event : model.getAllEvents()) {
      assertEquals(8, event.getStartDateTime().getHour());
      assertEquals(17, event.getEndDateTime().getHour());
    }
  }

  @Test
  public void testMultipleEventsAvailability() {
    model.createSingleEvent(new EventBuilder()
        .subject("Morning Meeting")
        .start("2025-11-04T09:00:00-05:00[America/New_York]")
        .end("2025-11-04T10:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Afternoon Meeting")
        .start("2025-11-04T14:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .build());

    assertFalse(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T09:30:00-05:00[America/New_York]")));

    assertTrue(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T12:00:00-05:00[America/New_York]")));

    assertFalse(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T14:30:00-05:00[America/New_York]")));
  }

  @Test
  public void testEditLocationWrongValueException() {
    try {
      model.createSingleEvent(new EventBuilder()
          .subject("Morning Meeting")
          .start("2025-11-04T09:00:00-05:00[America/New_York]")
          .end("2025-11-04T11:00:00-05:00[America/New_York]")
          .build());

      model.editSingleEvent(new EventBuilder()
          .subject("Morning Meeting")
          .start("2025-11-04T09:00:00-05:00[America/New_York]")
          .end("2025-11-04T11:00:00-05:00[America/New_York]")
          .property("location")
          .value("At Home")
          .build());
    } catch (IllegalArgumentException e) {
      assertEquals("Location must be either 'physical' or 'online'", e.getMessage());
    }
  }

  @Test
  public void testEditStatusWrongValueException() {
    try {
      model.createSingleEvent(new EventBuilder()
          .subject("Morning Meeting")
          .start("2025-11-04T09:00:00-05:00[America/New_York]")
          .end("2025-11-04T11:00:00-05:00[America/New_York]")
          .build());

      model.editSingleEvent(new EventBuilder()
          .subject("Morning Meeting")
          .start("2025-11-04T09:00:00-05:00[America/New_York]")
          .end("2025-11-04T11:00:00-05:00[America/New_York]")
          .property("status")
          .value("internal")
          .build());
    } catch (IllegalArgumentException e) {
      assertEquals("Status must be either 'private' or 'public'", e.getMessage());
    }
  }

  @Test
  public void testEditStatusEmptyValueException() {
    try {
      model.createSingleEvent(new EventBuilder()
          .subject("Morning Meeting")
          .start("2025-11-04T09:00:00-05:00[America/New_York]")
          .end("2025-11-04T11:00:00-05:00[America/New_York]")
          .build());

      model.editSingleEvent(new EventBuilder()
          .subject("Morning Meeting")
          .start("2025-11-04T09:00:00-05:00[America/New_York]")
          .end("2025-11-04T11:00:00-05:00[America/New_York]")
          .property("status")
          .value("")
          .build());
    } catch (IllegalArgumentException e) {
      assertEquals("Status cannot be empty or contain only whitespace."
          + " Please provide a valid status value.", e.getMessage());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateSeriesWithoutRepetitionPattern() {
    Map<String, Object> params = new EventBuilder()
        .subject("Invalid Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .build();

    model.createEventSeries(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesDuplicateWithinNewEvents() {
    model.createEventSeries(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(3)
        .build());

    model.createEventSeries(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T09:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T09:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-04T10:00:00-05:00[America/New_York]")
        .build();

    model.editSeries(editParams);
  }

  @Test
  public void testEditSeriesDoesNotConflictWithItself() {
    model.createEventSeries(new EventBuilder()
        .subject("Daily Standup")
        .start("2025-11-04T09:00:00-05:00[America/New_York]")
        .end("2025-11-04T09:30:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(6)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Daily Standup")
        .start("2025-11-04T09:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Morning Standup")
        .build();

    model.editSeries(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    for (CalendarEvent event : events) {
      assertEquals("Morning Standup", event.getSubject());
    }
  }

  @Test
  public void testEditSeriesValidWhenReplacingOwnEvents() {
    model.createEventSeries(new EventBuilder()
        .subject("Team Sync")
        .start("2025-11-03T14:00:00-05:00[America/New_York]")
        .end("2025-11-03T15:00:00-05:00[America/New_York]")
        .weekdays("MW")
        .ndays(4)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Team Sync")
        .start("2025-11-03T14:00:00-05:00[America/New_York]")
        .property("location")
        .value("online")
        .build();

    model.editSeries(editParams);

    for (CalendarEvent event : model.getAllEvents()) {
      assertEquals("online", event.getLocation());
    }
  }

  @Test
  public void testEditSingleEventEndTimeStaysInSeries() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .property("end")
        .valueAsDateTime("2025-11-04T16:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    int inSeries = 0;
    for (CalendarEvent e : events) {
      if (e.getSeriesUid() != null) {
        inSeries++;
      }
    }
    assertEquals(3, inSeries);
  }

  @Test
  public void testEditSingleEventLastInSeriesRemovesSeriesUid() {
    model.createEventSeries(new EventBuilder()
        .subject("One Event Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(1)
        .build());

    model.createEventSeries(new EventBuilder()
        .subject("Short Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .weekdays("TF")
        .ndays(2)
        .build());

    List<CalendarEvent> events = model.getAllEvents();

    Map<String, Object> edit1 = new EventBuilder()
        .subject("Short Series")
        .start(events.get(0).getStartDateTime().toString())
        .end(events.get(0).getEndDateTime().toString())
        .property("start")
        .valueAsDateTime(events.get(0).getStartDateTime().withHour(12).toString())
        .build();

    model.editSingleEvent(edit1);

    events = model.getAllEvents();

    CalendarEvent lastInSeries = null;
    for (CalendarEvent e : events) {
      if (e.getSeriesUid() != null) {
        lastInSeries = e;
        break;
      }
    }

    assertNotNull(lastInSeries);

    Map<String, Object> edit2 = new EventBuilder()
        .subject("Short Series")
        .start(lastInSeries.getStartDateTime().toString())
        .end(lastInSeries.getEndDateTime().toString())
        .property("start")
        .valueAsDateTime(lastInSeries.getStartDateTime().withHour(14).toString())
        .build();

    model.editSingleEvent(edit2);

    events = model.getAllEvents();
    for (CalendarEvent e : events) {
      assertNull("All events should be standalone", e.getSeriesUid());
    }
  }

  @Test
  public void testCreateAllDayEventSeriesWithOneOccurrence() {
    Map<String, Object> params = new EventBuilder()
        .subject("Single All Day Event")
        .ondate("2025-11-04")
        .weekdays("T")
        .ndays(1)
        .build();

    model.createAllDayEventSeries(params);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertNull(events.get(0).getSeriesUid());
    assertEquals(8, events.get(0).getStartDateTime().getHour());
    assertEquals(17, events.get(0).getEndDateTime().getHour());
  }

  @Test
  public void testCreateAllDayEventSeriesWithSameStartAndUntilDate() {
    Map<String, Object> params = new EventBuilder()
        .subject("Single All Day")
        .ondate("2025-11-04")
        .weekdays("T")
        .untildate("2025-11-04")
        .build();

    model.createAllDayEventSeries(params);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertNull(events.get(0).getSeriesUid());
    assertEquals(8, events.get(0).getStartDateTime().getHour());
    assertEquals(17, events.get(0).getEndDateTime().getHour());
  }

  @Test
  public void testEditSeriesOnStandaloneEvent() {
    model.createSingleEvent(new EventBuilder()
        .subject("Standalone Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Standalone Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated Meeting")
        .build();

    model.editSeries(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("Updated Meeting", events.get(0).getSubject());
  }

  @Test
  public void testEditEventsOnStandaloneEvent() {
    model.createSingleEvent(new EventBuilder()
        .subject("Solo Event")
        .start("2025-11-04T14:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Solo Event")
        .start("2025-11-04T14:00:00-05:00[America/New_York]")
        .property("location")
        .value("online")
        .build();

    model.editEvents(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("online", events.get(0).getLocation());
  }

  @Test
  public void testCreateSingleEventWithTimezoneConversion() {
    CalendarModel pstModel = new CalendarModel(ZoneId.of("America/Los_Angeles"));

    Map<String, Object> params = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build();

    pstModel.createSingleEvent(params);

    List<CalendarEvent> events = pstModel.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(7, events.get(0).getStartDateTime().getHour());
    assertEquals(ZoneId.of("America/Los_Angeles"),
        events.get(0).getStartDateTime().getZone());
  }

  @Test
  public void testCreateEventSeriesWithTimezoneConversion() {
    CalendarModel utcModel = new CalendarModel(ZoneId.of("UTC"));

    Map<String, Object> params = new EventBuilder()
        .subject("Daily Standup")
        .start("2025-11-04T09:00:00-05:00[America/New_York]")
        .end("2025-11-04T09:30:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(3)
        .build();

    utcModel.createEventSeries(params);

    List<CalendarEvent> events = utcModel.getAllEvents();
    assertEquals(3, events.size());
    assertEquals(14, events.get(0).getStartDateTime().getHour());
    assertEquals(ZoneId.of("UTC"), events.get(0).getStartDateTime().getZone());
  }

  @Test
  public void testCreateAllDayEventWithTimezoneConversion() {
    CalendarModel chicagoModel = new CalendarModel(ZoneId.of("America/Chicago"));

    Map<String, Object> params = new EventBuilder()
        .subject("Holiday")
        .ondate("2025-11-04")
        .build();

    chicagoModel.createAllDayEvent(params);

    List<CalendarEvent> events = chicagoModel.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(ZoneId.of("America/Chicago"), events.get(0).getStartDateTime().getZone());
  }

  @Test
  public void testCreateAllDayEventSeriesWithTimezoneConversion() {
    CalendarModel denverModel = new CalendarModel(ZoneId.of("America/Denver"));

    Map<String, Object> params = new EventBuilder()
        .subject("Training")
        .ondate("2025-11-03")
        .weekdays("MTW")
        .ndays(3)
        .build();

    denverModel.createAllDayEventSeries(params);

    List<CalendarEvent> events = denverModel.getAllEvents();
    for (CalendarEvent event : events) {
      assertEquals(ZoneId.of("America/Denver"), event.getStartDateTime().getZone());
    }
  }

  @Test
  public void testEditSingleEventWithTimezoneConversion() {
    CalendarModel pstModel = new CalendarModel(ZoneId.of("America/Los_Angeles"));

    pstModel.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-08:00[America/Los_Angeles]")
        .end("2025-11-04T11:00:00-08:00[America/Los_Angeles]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T13:00:00-05:00[America/New_York]")
        .end("2025-11-04T14:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated Meeting")
        .build();

    pstModel.editSingleEvent(editParams);

    List<CalendarEvent> events = pstModel.getAllEvents();
    assertEquals("Updated Meeting", events.get(0).getSubject());
    assertEquals(ZoneId.of("America/Los_Angeles"),
        events.get(0).getStartDateTime().getZone());
  }

  @Test
  public void testEditSeriesWithTimezoneConversion() {
    CalendarModel utcModel = new CalendarModel(ZoneId.of("UTC"));

    utcModel.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00+00:00[UTC]")
        .end("2025-11-04T11:00:00+00:00[UTC]")
        .weekdays("T")
        .ndays(2)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T05:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated Series")
        .build();

    utcModel.editSeries(editParams);

    List<CalendarEvent> events = utcModel.getAllEvents();
    for (CalendarEvent event : events) {
      assertEquals("Updated Series", event.getSubject());
      assertEquals(ZoneId.of("UTC"), event.getStartDateTime().getZone());
    }
  }

  @Test
  public void testEditEventsWithTimezoneConversion() {
    CalendarModel chicagoModel = new CalendarModel(ZoneId.of("America/Chicago"));

    chicagoModel.createEventSeries(new EventBuilder()
        .subject("Meetings")
        .start("2025-11-04T10:00:00-06:00[America/Chicago]")
        .end("2025-11-04T11:00:00-06:00[America/Chicago]")
        .weekdays("TF")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meetings")
        .start("2025-11-04T11:00:00-05:00[America/New_York]")
        .property("location")
        .value("online")
        .build();

    chicagoModel.editEvents(editParams);

    List<CalendarEvent> events = chicagoModel.getAllEvents();
    for (CalendarEvent event : events) {
      assertEquals(ZoneId.of("America/Chicago"), event.getStartDateTime().getZone());
    }
  }

  @Test
  public void testPreProcessWithValueAsDateTime() {
    CalendarModel pstModel = new CalendarModel(ZoneId.of("America/Los_Angeles"));

    pstModel.createSingleEvent(new EventBuilder()
        .subject("Event")
        .start("2025-11-04T10:00:00-08:00[America/Los_Angeles]")
        .end("2025-11-04T15:00:00-08:00[America/Los_Angeles]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Event")
        .start("2025-11-04T13:00:00-05:00[America/New_York]")
        .end("2025-11-04T18:00:00-05:00[America/New_York]")
        .property("end")
        .valueAsDateTime("2025-11-04T19:00:00-05:00[America/New_York]")
        .build();

    pstModel.editSingleEvent(editParams);

    List<CalendarEvent> events = pstModel.getAllEvents();
    assertEquals(16, events.get(0).getEndDateTime().getHour());
    assertEquals(ZoneId.of("America/Los_Angeles"), events.get(0).getEndDateTime().getZone());
  }

  @Test
  public void testTimezonePreservesInstantAcrossZones() {
    CalendarModel estModel = new CalendarModel(ZoneId.of("America/New_York"));
    CalendarModel pstModel = new CalendarModel(ZoneId.of("America/Los_Angeles"));

    Map<String, Object> params = new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T15:00:00-05:00[America/New_York]")
        .end("2025-11-04T16:00:00-05:00[America/New_York]")
        .build();

    estModel.createSingleEvent(new HashMap<>(params));
    pstModel.createSingleEvent(new HashMap<>(params));

    CalendarEvent estEvent = estModel.getAllEvents().get(0);
    CalendarEvent pstEvent = pstModel.getAllEvents().get(0);

    assertEquals(15, estEvent.getStartDateTime().getHour());
    assertEquals(12, pstEvent.getStartDateTime().getHour());

    assertTrue(estEvent.getStartDateTime().toInstant()
        .equals(pstEvent.getStartDateTime().toInstant()));
  }

  @Test
  public void testGetTimezone() {
    CalendarModel defaultModel = new CalendarModel();
    assertEquals(ZoneId.of("America/New_York"), defaultModel.getTimezone());

    CalendarModel pstModel = new CalendarModel(ZoneId.of("America/Los_Angeles"));
    assertEquals(ZoneId.of("America/Los_Angeles"), pstModel.getTimezone());
  }

  @Test
  public void testCreateSeriesWithUntilDateTimezoneConversion() {
    CalendarModel utcModel = new CalendarModel(ZoneId.of("UTC"));

    Map<String, Object> params = new EventBuilder()
        .subject("Weekly")
        .start("2025-11-03T09:00:00-05:00[America/New_York]")
        .end("2025-11-03T10:00:00-05:00[America/New_York]")
        .weekdays("M")
        .untildate("2025-11-24")
        .build();

    utcModel.createEventSeries(params);

    List<CalendarEvent> events = utcModel.getAllEvents();
    assertEquals(4, events.size());
    for (CalendarEvent event : events) {
      assertEquals(ZoneId.of("UTC"), event.getStartDateTime().getZone());
    }
  }

  @Test
  public void testEditSeriesStartTimeWithTimezoneConversion() {
    CalendarModel chicagoModel = new CalendarModel(ZoneId.of("America/Chicago"));

    chicagoModel.createEventSeries(new EventBuilder()
        .subject("Standup")
        .start("2025-11-04T09:00:00-06:00[America/Chicago]")
        .end("2025-11-04T11:30:00-06:00[America/Chicago]")
        .weekdays("TF")
        .ndays(2)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Standup")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-04T11:00:00-05:00[America/New_York]")
        .build();

    chicagoModel.editSeries(editParams);

    List<CalendarEvent> events = chicagoModel.getAllEvents();
    for (CalendarEvent event : events) {
      assertEquals(10, event.getStartDateTime().getHour());
      assertEquals(ZoneId.of("America/Chicago"), event.getStartDateTime().getZone());
    }
  }

  @Test
  public void testEditEventsWithStartTimeSplitAndTimezone() {
    CalendarModel denverModel = new CalendarModel(ZoneId.of("America/Denver"));

    denverModel.createEventSeries(new EventBuilder()
        .subject("Work")
        .start("2025-11-04T08:00:00-07:00[America/Denver]")
        .end("2025-11-04T17:00:00-07:00[America/Denver]")
        .weekdays("TF")
        .ndays(4)
        .build());

    List<CalendarEvent> allEvents = denverModel.getAllEvents();
    CalendarEvent secondEvent = allEvents.get(1);

    Map<String, Object> editParams = new EventBuilder()
        .subject("Work")
        .start(secondEvent.getStartDateTime().toString())
        .property("start")
        .valueAsDateTime(secondEvent.getStartDateTime().withHour(9).toString())
        .build();

    denverModel.editEvents(editParams);

    List<CalendarEvent> events = denverModel.getAllEvents();
    assertEquals(4, events.size());

    int at8am = 0;
    int at9am = 0;
    for (CalendarEvent event : events) {
      if (event.getStartDateTime().getHour() == 8) {
        at8am++;
      }
      if (event.getStartDateTime().getHour() == 9) {
        at9am++;
      }
      assertEquals(ZoneId.of("America/Denver"), event.getStartDateTime().getZone());
    }

    assertEquals(1, at8am);
    assertEquals(3, at9am);
  }

  @Test
  public void testConvertToTimezoneWithNullValues() {
    CalendarModel model = new CalendarModel(ZoneId.of("UTC"));

    Map<String, Object> params = new EventBuilder()
        .subject("Event")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build();
    params.put("description", null);
    params.put("location", null);

    model.createSingleEvent(params);

    assertEquals(1, model.getAllEvents().size());
  }

  @Test
  public void testPreProcessValidatesBeforeConverting() {
    CalendarModel model = new CalendarModel(ZoneId.of("UTC"));

    model.createSingleEvent(new EventBuilder()
        .subject("Event")
        .start("2025-11-04T10:00:00+00:00[UTC]")
        .end("2025-11-04T11:00:00+00:00[UTC]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Event")
        .start("2025-11-04T10:00:00+00:00[UTC]")
        .end("2025-11-04T11:00:00+00:00[UTC]")
        .property("invalid_property")
        .value("some value")
        .build();

    try {
      model.editSingleEvent(editParams);
      fail("Should have thrown validation error");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Unknown property"));
    }
  }

  @Test
  public void testMultipleTimezonesInSameCalendar() {
    CalendarModel model = new CalendarModel(ZoneId.of("America/New_York"));

    model.createSingleEvent(new EventBuilder()
        .subject("Meeting 1")
        .start("2025-11-04T10:00:00-08:00[America/Los_Angeles]")
        .end("2025-11-04T11:00:00-08:00[America/Los_Angeles]")
        .build());

    model.createSingleEvent(new EventBuilder()
        .subject("Meeting 2")
        .start("2025-11-04T15:00:00+00:00[UTC]")
        .end("2025-11-04T16:00:00+00:00[UTC]")
        .build());

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(2, events.size());

    for (CalendarEvent event : events) {
      assertEquals(ZoneId.of("America/New_York"), event.getStartDateTime().getZone());
    }
  }

  @Test
  public void testGetEventsOnWithCalendarTimezone() {
    CalendarModel pstModel = new CalendarModel(ZoneId.of("America/Los_Angeles"));

    pstModel.createSingleEvent(new EventBuilder()
        .subject("Event")
        .start("2025-11-04T10:00:00-08:00[America/Los_Angeles]")
        .end("2025-11-04T11:00:00-08:00[America/Los_Angeles]")
        .build());

    List<CalendarEvent> events = pstModel
        .getEventsOn(LocalDate.of(2025, 11, 4));
    assertEquals(1, events.size());
  }

  @Test
  public void testCheckAvailabilityUsesCalendarTimezone() {
    CalendarModel model = new CalendarModel(ZoneId.of("America/New_York"));

    model.createSingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    assertFalse(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T15:00:00+00:00[UTC]")));

    assertTrue(model.checkAvailability(
        ZonedDateTime.parse("2025-11-04T16:00:00+00:00[UTC]")));
  }

  @Test
  public void testCreateSingleDaySeriesBecomesEventWithTimezone() {
    CalendarModel pstModel = new CalendarModel(ZoneId.of("America/Los_Angeles"));

    Map<String, Object> params = new EventBuilder()
        .subject("One Time")
        .start("2025-11-04T13:00:00-05:00[America/New_York]")
        .end("2025-11-04T14:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(1)
        .build();

    pstModel.createEventSeries(params);

    List<CalendarEvent> events = pstModel.getAllEvents();
    assertEquals(1, events.size());
    assertNull(events.get(0).getSeriesUid());
    assertEquals(10, events.get(0).getStartDateTime().getHour());
    assertEquals(ZoneId.of("America/Los_Angeles"),
        events.get(0).getStartDateTime().getZone());
  }

  @Test
  public void testCreateAllDaySeriesSingleDayWithTimezone() {
    CalendarModel denverModel = new CalendarModel(ZoneId.of("America/Denver"));

    Map<String, Object> params = new EventBuilder()
        .subject("All Day")
        .ondate("2025-11-04")
        .weekdays("T")
        .ndays(1)
        .build();

    denverModel.createAllDayEventSeries(params);

    List<CalendarEvent> events = denverModel.getAllEvents();
    assertEquals(1, events.size());
    assertNull(events.get(0).getSeriesUid());
    assertEquals(ZoneId.of("America/Denver"), events.get(0).getStartDateTime().getZone());
    assertEquals(8, events.get(0).getStartDateTime().getHour());
    assertEquals(17, events.get(0).getEndDateTime().getHour());
  }

  @Test
  public void testCreateSeriesWithRepeatCountOne() {
    Map<String, Object> params = new EventBuilder()
        .subject("One Time")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(1)
        .build();

    model.createEventSeries(params);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(1, events.size());
    assertNull(events.get(0).getSeriesUid());
  }

  @Test
  public void testCreateEventSeriesReturnValue() {
    Map<String, Object> params = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("MTF")
        .ndays(3)
        .build();

    model.createEventSeries(params);
    assertEquals(3, model.getAllEvents().size());
  }

  @Test
  public void testCreateAllDayEventConvertsTimezone() {
    CalendarModel pstModel = new CalendarModel(ZoneId.of("America/Los_Angeles"));

    Map<String, Object> params = new EventBuilder()
        .subject("Holiday")
        .ondate("2025-11-04")
        .build();

    pstModel.createAllDayEvent(params);

    CalendarEvent event = pstModel.getAllEvents().get(0);
    assertEquals(ZoneId.of("America/Los_Angeles"), event.getStartDateTime().getZone());
  }

  @Test
  public void testCreateAllDayEventReturnValue() {
    Map<String, Object> params = new EventBuilder()
        .subject("Holiday")
        .ondate("2025-11-04")
        .build();

    model.createAllDayEvent(params);
    assertEquals(1, model.getAllEvents().size());
  }

  @Test
  public void testEditEventsPreProcessValidates() {
    model.createSingleEvent(new EventBuilder()
        .subject("Event")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Event")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("invalid_prop")
        .value("value")
        .build();

    try {
      model.editEvents(editParams);
      fail("Should throw validation error");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Unknown property"));
    }
  }

  @Test
  public void testEditEventsOnStandaloneReturnValue() {
    model.createSingleEvent(new EventBuilder()
        .subject("Solo")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Solo")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated")
        .build();

    model.editEvents(editParams);

    assertEquals("Updated", model.getAllEvents().get(0).getSubject());
  }

  @Test
  public void testEditEventsSeriesReturnValue() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("location")
        .value("online")
        .build();

    model.editEvents(editParams);

    assertEquals("online", model.getAllEvents().get(0).getLocation());
  }

  @Test
  public void testEditEntireSeriesValidationPreventsConflict() {
    model.createSingleEvent(new EventBuilder()
        .subject("Conflict")
        .start("2025-11-07T10:00:00-05:00[America/New_York]")
        .end("2025-11-07T11:00:00-05:00[America/New_York]")
        .build());

    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("TF")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Conflict")
        .build();

    try {
      model.editSeries(editParams);
      fail("Should throw duplicate error");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("duplicate"));
    }
  }

  @Test
  public void testApplySeriesEditReplacesSeriesList() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated")
        .build();

    model.editSeries(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(3, events.size());
    for (CalendarEvent e : events) {
      assertEquals("Updated", e.getSubject());
    }
  }

  @Test
  public void testTreeOperationsTimePropertyRemovesAndReinserts() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("end")
        .valueAsDateTime("2025-11-04T12:00:00-05:00[America/New_York]")
        .build();

    model.editSeries(editParams);

    for (CalendarEvent e : model.getAllEvents()) {
      assertEquals(12, e.getEndDateTime().getHour());
    }
  }

  @Test
  public void testTreeOperationsNonTimePropertyReplacesInPlace() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("description")
        .value("Important")
        .build();

    model.editSeries(editParams);

    for (CalendarEvent e : model.getAllEvents()) {
      assertEquals("Important", e.getDescription());
    }
  }

  @Test
  public void testApplySeriesFromUpdatesLinkedList() {
    model.createEventSeries(new EventBuilder()
        .subject("Meetings")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("TF")
        .ndays(4)
        .build());

    List<CalendarEvent> events = model.getAllEvents();
    CalendarEvent secondEvent = events.get(1);

    Map<String, Object> editParams = new EventBuilder()
        .subject("Meetings")
        .start(secondEvent.getStartDateTime().toString())
        .property("status")
        .value("public")
        .build();

    model.editEvents(editParams);

    events = model.getAllEvents();
    assertNull(events.get(0).getStatus());
    assertEquals("public", events.get(1).getStatus());
    assertEquals("public", events.get(2).getStatus());
    assertEquals("public", events.get(3).getStatus());
  }

  @Test
  public void testApplySingleTimeEditRemovesFromSeries() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-11-04T12:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    int inSeries = 0;
    int standalone = 0;
    for (CalendarEvent e : events) {
      if (e.getSeriesUid() != null) {
        inSeries++;
      } else {
        standalone++;
      }
    }

    assertEquals(2, inSeries);
    assertEquals(1, standalone);
  }

  @Test
  public void testApplySingleTimeEditKeepsInSeriesWhenEditingEnd() {
    model.createEventSeries(new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(3)
        .build());

    Map<String, Object> editParams = new EventBuilder()
        .subject("Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .property("end")
        .valueAsDateTime("2025-11-04T16:00:00-05:00[America/New_York]")
        .build();

    model.editSingleEvent(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    for (CalendarEvent e : events) {
      assertNotNull(e.getSeriesUid());
    }

    CalendarEvent editedEvent = null;
    for (CalendarEvent e : events) {
      if (e.getEndDateTime().getHour() == 16) {
        editedEvent = e;
        break;
      }
    }
    assertNotNull(editedEvent);
    assertNotNull(editedEvent.getSeriesUid());
  }

  @Test
  public void testApplySingleTimeEditEmptiesSeriesMap() {
    model.createEventSeries(new EventBuilder()
        .subject("Two Event Series")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T15:00:00-05:00[America/New_York]")
        .weekdays("TF")
        .ndays(2)
        .build());

    List<CalendarEvent> events = model.getAllEvents();

    Map<String, Object> edit1 = new EventBuilder()
        .subject("Two Event Series")
        .start(events.get(0).getStartDateTime().toString())
        .end(events.get(0).getEndDateTime().toString())
        .property("start")
        .valueAsDateTime(events.get(0).getStartDateTime().withHour(12).toString())
        .build();

    model.editSingleEvent(edit1);

    events = model.getAllEvents();
    CalendarEvent lastInSeries = null;
    for (CalendarEvent e : events) {
      if (e.getSeriesUid() != null) {
        lastInSeries = e;
        break;
      }
    }

    Map<String, Object> edit2 = new EventBuilder()
        .subject("Two Event Series")
        .start(lastInSeries.getStartDateTime().toString())
        .end(lastInSeries.getEndDateTime().toString())
        .property("start")
        .valueAsDateTime(lastInSeries.getStartDateTime().withHour(14).toString())
        .build();

    model.editSingleEvent(edit2);

    events = model.getAllEvents();
    for (CalendarEvent e : events) {
      assertNull(e.getSeriesUid());
    }
  }

  @Test
  public void testTreeOperationsConditionalBothBranches() {
    model.createEventSeries(new EventBuilder()
        .subject("Time Edit")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("T")
        .ndays(2)
        .build());

    model.editSeries(new EventBuilder()
        .subject("Time Edit")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .property("end")
        .valueAsDateTime("2025-11-04T12:00:00-05:00[America/New_York]")
        .build());

    for (CalendarEvent e : model.getAllEvents()) {
      if (e.getSubject().equals("Time Edit")) {
        assertEquals(12, e.getEndDateTime().getHour());
      }
    }

    model.createEventSeries(new EventBuilder()
        .subject("Non-Time Edit")
        .start("2025-11-07T10:00:00-05:00[America/New_York]")
        .end("2025-11-07T11:00:00-05:00[America/New_York]")
        .weekdays("F")
        .ndays(2)
        .build());

    model.editSeries(new EventBuilder()
        .subject("Non-Time Edit")
        .start("2025-11-07T10:00:00-05:00[America/New_York]")
        .property("location")
        .value("online")
        .build());

    int withLocation = 0;
    for (CalendarEvent e : model.getAllEvents()) {
      if ("online".equals(e.getLocation())) {
        withLocation++;
      }
    }
    assertEquals(2, withLocation);
  }

  @Test
  public void testApplySeriesFromForNonTimeProperty() {
    model.createEventSeries(new EventBuilder()
        .subject("Work")
        .start("2025-11-04T10:00:00-05:00[America/New_York]")
        .end("2025-11-04T11:00:00-05:00[America/New_York]")
        .weekdays("TF")
        .ndays(4)
        .build());

    List<CalendarEvent> events = model.getAllEvents();

    model.editEvents(new EventBuilder()
        .subject("Work")
        .start(events.get(2).getStartDateTime().toString())
        .property("description")
        .value("Late shift")
        .build());

    events = model.getAllEvents();
    assertNull(events.get(0).getDescription());
    assertNull(events.get(1).getDescription());
    assertEquals("Late shift", events.get(2).getDescription());
    assertEquals("Late shift", events.get(3).getDescription());
  }

  @Test
  public void testEditEventsFromStartRemovesOriginalSeriesWhenEmpty() {
    model.createEventSeries(new EventBuilder()
        .subject("Daily Standup")
        .start("2025-01-13T09:00:00-05:00[America/New_York]")
        .end("2025-01-13T10:30:00-05:00[America/New_York]")
        .weekdays("MWF")
        .ndays(7)
        .build());

    List<CalendarEvent> initialEvents = model.getAllEvents();
    assertEquals(7, initialEvents.size());
    String originalSeriesUid = initialEvents.get(0).getSeriesUid();
    assertNotNull(originalSeriesUid);

    model.editEvents(new EventBuilder()
        .subject("Daily Standup")
        .start("2025-01-13T09:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-01-13T10:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> afterEdit = model.getAllEvents();
    assertEquals(7, afterEdit.size());

    String newSeriesUid = afterEdit.get(0).getSeriesUid();
    assertNotNull(newSeriesUid);
    assertNotEquals(originalSeriesUid, newSeriesUid);

    for (CalendarEvent event : afterEdit) {
      assertEquals(newSeriesUid, event.getSeriesUid());
      assertEquals(10, event.getStartDateTime().getHour());
    }
  }

  @Test
  public void testEditEventsFromMiddleKeepsOriginalSeries() {
    model.createEventSeries(new EventBuilder()
        .subject("Team Sync")
        .start("2025-01-13T14:00:00-05:00[America/New_York]")
        .end("2025-01-13T17:00:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(7)
        .build());

    List<CalendarEvent> initialEvents = model.getAllEvents();
    assertEquals(7, initialEvents.size());
    String originalSeriesUid = initialEvents.get(0).getSeriesUid();

    model.editEvents(new EventBuilder()
        .subject("Team Sync")
        .start("2025-01-15T14:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-01-15T16:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> afterEdit = model.getAllEvents();
    assertEquals(7, afterEdit.size());

    List<CalendarEvent> mondayEvents = model
        .getEventsOn(LocalDate.of(2025, 1, 13));
    assertEquals(originalSeriesUid, mondayEvents.get(0).getSeriesUid());
    assertEquals(14, mondayEvents.get(0).getStartDateTime().getHour());

    List<CalendarEvent> tuesdayEvents = model
        .getEventsOn(LocalDate.of(2025, 1, 14));
    assertEquals(originalSeriesUid, tuesdayEvents.get(0).getSeriesUid());

    List<CalendarEvent> wednesdayEvents = model
        .getEventsOn(LocalDate.of(2025, 1, 15));
    String newSeriesUid = wednesdayEvents.get(0).getSeriesUid();
    assertNotEquals(originalSeriesUid, newSeriesUid);
    assertEquals(16, wednesdayEvents.get(0).getStartDateTime().getHour());
    int oldSeriesCount = 0;
    int newSeriesCount = 0;
    for (CalendarEvent event : afterEdit) {
      if (event.getSeriesUid().equals(newSeriesUid)) {
        newSeriesCount++;
      } else if (event.getSeriesUid().equals(originalSeriesUid)) {
        oldSeriesCount++;
      }
    }
    assertEquals(2, oldSeriesCount);
    assertEquals(5, newSeriesCount);
  }

  @Test
  public void testEditEventsFromStartWithThreeEventSeries() {
    model.createEventSeries(new EventBuilder()
        .subject("Short Series")
        .start("2025-01-13T11:00:00-05:00[America/New_York]")
        .end("2025-01-13T15:30:00-05:00[America/New_York]")
        .weekdays("MW")
        .ndays(3)
        .build());

    List<CalendarEvent> initialEvents = model.getAllEvents();
    assertEquals(3, initialEvents.size());
    String originalSeriesUid = initialEvents.get(0).getSeriesUid();

    model.editEvents(new EventBuilder()
        .subject("Short Series")
        .start("2025-01-13T11:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-01-13T13:00:00-05:00[America/New_York]")
        .build());

    List<CalendarEvent> afterEdit = model.getAllEvents();
    assertEquals(3, afterEdit.size());

    for (CalendarEvent event : afterEdit) {
      assertNotEquals(originalSeriesUid, event.getSeriesUid());
      assertEquals(13, event.getStartDateTime().getHour());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesEventNotFound() {
    model.createEventSeries(new TestBuilders.EventBuilder()
        .subject("Team Meeting")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .end("2025-01-13T11:00:00-05:00[America/New_York]")
        .weekdays("MWF")
        .ndays(7)
        .build());

    model.editSeries(new TestBuilders.EventBuilder()
        .subject("Nonexistent Meeting")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated Meeting")
        .build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsEventNotFound() {
    model.createEventSeries(new TestBuilders.EventBuilder()
        .subject("Daily Standup")
        .start("2025-01-13T09:00:00-05:00[America/New_York]")
        .end("2025-01-13T09:30:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(7)
        .build());

    model.editEvents(new TestBuilders.EventBuilder()
        .subject("Daily Standup")
        .start("2025-01-13T14:00:00-05:00[America/New_York]")
        .property("subject")
        .value("New Standup")
        .build());
  }

  @Test
  public void testEditSeriesEventNotFoundMessage() {
    model.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("One Time Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build());

    try {
      model.editSeries(new TestBuilders.EventBuilder()
          .subject("Wrong Subject")
          .start("2025-01-15T10:00:00-05:00[America/New_York]")
          .property("subject")
          .value("Updated")
          .build());
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Event not found", e.getMessage());
    }
  }

  @Test
  public void testEditSingleEventValidationPreventsDuplicate() {
    model.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .build());

    model.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T14:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .build());

    try {
      model.editSingleEvent(new TestBuilders.EventBuilder()
          .subject("Meeting")
          .start("2025-01-15T14:00:00-05:00[America/New_York]")
          .end("2025-01-15T15:00:00-05:00[America/New_York]")
          .property("start")
          .valueAsDateTime("2025-01-15T10:00:00-05:00[America/New_York]")
          .build());
      fail("Should throw duplicate error");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("already exists"));
      assertTrue(e.getMessage().contains("Meeting"));
    }
  }

  @Test
  public void testEditSingleEventNonTimePropertyKeepsInSeries() {
    model.createEventSeries(new TestBuilders.EventBuilder()
        .subject("Series Event")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .weekdays("MWF")
        .ndays(3)
        .build());

    List<CalendarEvent> before = model.getAllEvents();

    model.editSingleEvent(new TestBuilders.EventBuilder()
        .subject("Series Event")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .property("description")
        .value("Updated description")
        .build());

    List<CalendarEvent> after = model.getAllEvents();
    assertEquals(3, after.size());

    String originalSeriesUid = before.get(0).getSeriesUid();
    CalendarEvent editedEvent = after.get(0);
    assertEquals("Updated description", editedEvent.getDescription());
    assertEquals(originalSeriesUid, editedEvent.getSeriesUid());

    assertEquals(originalSeriesUid, after.get(1).getSeriesUid());
    assertEquals(originalSeriesUid, after.get(2).getSeriesUid());
    assertNull(after.get(1).getDescription());
    assertNull(after.get(2).getDescription());
  }

  @Test
  public void testEditSingleEventTimePropertyRemovesFromSeries() {
    model.createEventSeries(new TestBuilders.EventBuilder()
        .subject("Series Event")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .weekdays("MWF")
        .ndays(3)
        .build());

    List<CalendarEvent> before = model.getAllEvents();
    model.editSingleEvent(new TestBuilders.EventBuilder()
        .subject("Series Event")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .property("start")
        .valueAsDateTime("2025-01-15T12:00:00-05:00[America/New_York]")
        .build());
    List<CalendarEvent> after = model.getAllEvents();
    String originalSeriesUid = before.get(0).getSeriesUid();
    assertEquals(originalSeriesUid, after.get(1).getSeriesUid());
    assertEquals(originalSeriesUid, after.get(2).getSeriesUid());
    assertEquals(3, after.size());
    CalendarEvent editedEvent = after.get(0);
    assertEquals(12, editedEvent.getStartDateTime().getHour());
    assertNull(editedEvent.getSeriesUid());
  }

  @Test
  public void testEditSingleEventEndTimeKeepsInSeries() {
    model.createEventSeries(new TestBuilders.EventBuilder()
        .subject("Series Event")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .weekdays("MWF")
        .ndays(3)
        .build());

    List<CalendarEvent> before = model.getAllEvents();
    model.editSingleEvent(new TestBuilders.EventBuilder()
        .subject("Series Event")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .property("end")
        .valueAsDateTime("2025-01-15T16:00:00-05:00[America/New_York]")
        .build());
    List<CalendarEvent> after = model.getAllEvents();
    String originalSeriesUid = before.get(0).getSeriesUid();
    for (CalendarEvent event : after) {
      assertEquals(originalSeriesUid, event.getSeriesUid());
    }

    CalendarEvent editedEvent = after.get(0);
    assertEquals(16, editedEvent.getEndDateTime().getHour());
  }

  @Test
  public void testEditEventsConvertsParametersToCalendarTimezone() {
    model.createEventSeries(new TestBuilders.EventBuilder()
        .subject("Daily Standup")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .end("2025-01-13T11:00:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(7)
        .build());

    List<CalendarEvent> eventsBefore = model.getAllEvents();
    assertEquals(7, eventsBefore.size());

    Map<String, Object> editParams = new TestBuilders.EventBuilder()
        .subject("Daily Standup")
        .start("2025-01-15T09:00:00-06:00[America/Chicago]")
        .property("description")
        .value("Updated standup")
        .build();

    model.editEvents(editParams);

    List<CalendarEvent> eventsAfter = model.getAllEvents();
    assertEquals(7, eventsAfter.size());

    CalendarEvent wednesdayEvent = eventsAfter.get(2);
    assertEquals("Updated standup", wednesdayEvent.getDescription());
    assertEquals(ZoneId.of("America/New_York"), wednesdayEvent.getStartDateTime().getZone());
    assertEquals(10, wednesdayEvent.getStartDateTime().getHour());

    String originalSeriesUid = eventsBefore.get(0).getSeriesUid();
    for (int i = 0; i < 2; i++) {
      assertNull(eventsAfter.get(i).getDescription());
      assertEquals(originalSeriesUid, eventsAfter.get(i).getSeriesUid());
    }

    for (int i = 2; i < 7; i++) {
      assertEquals("Updated standup", eventsAfter.get(i).getDescription());
    }
  }

  @Test
  public void testEditEventsWithTimezoneConversionOnStandaloneEvent() {
    model.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Project Review")
        .start("2025-01-15T14:00:00-05:00[America/New_York]")
        .end("2025-01-15T16:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> editParams = new TestBuilders.EventBuilder()
        .subject("Project Review")
        .start("2025-01-15T13:00:00-06:00[America/Chicago]")
        .property("location")
        .value("online")
        .build();

    model.editEvents(editParams);

    CalendarEvent updated = model.getAllEvents().get(0);
    assertEquals("online", updated.getLocation());
    assertEquals(ZoneId.of("America/New_York"), updated.getStartDateTime().getZone());
    assertEquals(14, updated.getStartDateTime().getHour());
  }

  @Test
  public void testEditEventsWithDifferentTimezoneStartProperty() {
    model.createEventSeries(new TestBuilders.EventBuilder()
        .subject("Team Sync")
        .start("2025-01-13T15:00:00-05:00[America/New_York]")
        .end("2025-01-13T23:00:00-05:00[America/New_York]")
        .weekdays("MWF")
        .ndays(7)
        .build());

    Map<String, Object> editParams = new TestBuilders.EventBuilder()
        .subject("Team Sync")
        .start("2025-01-15T14:00:00-06:00[America/Chicago]")
        .property("start")
        .valueAsDateTime("2025-01-15T16:00:00-06:00[America/Chicago]")
        .build();

    model.editEvents(editParams);

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals(7, events.size());

    for (CalendarEvent event : events) {
      assertEquals(ZoneId.of("America/New_York"), event.getStartDateTime().getZone());
    }

    int at15 = 0;
    int at17 = 0;
    for (CalendarEvent event : events) {
      int hour = event.getStartDateTime().getHour();
      if (hour == 15) {
        at15++;
      } else if (hour == 17) {
        at17++;
      }
    }

    assertEquals(1, at15);
    assertEquals(6, at17);
  }

  @Test
  public void testCopyEventsOnMaintainsSeriesRelationships() {
    CalendarModel source = new CalendarModel(ZoneId.of("America/New_York"));

    source.createEventSeries(new TestBuilders.EventBuilder()
        .subject("Weekly Meeting")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .end("2025-01-13T11:00:00-05:00[America/New_York]")
        .weekdays("MWF")
        .ndays(7)
        .build());

    List<CalendarEvent> sourceEvents = source
        .getEventsOn(LocalDate.of(2025, 1, 15));
    assertEquals(1, sourceEvents.size());
    String originalSeriesUid = sourceEvents.get(0).getSeriesUid();
    assertNotNull(originalSeriesUid);

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("sourcedate", LocalDate.of(2025, 1, 15));
    copyParams.put("targetdate", LocalDate.of(2025, 2, 1));

    CalendarModel target = new CalendarModel(ZoneId.of("America/New_York"));
    source.copyEventsOn(target, copyParams);

    List<CalendarEvent> copiedEvents = target
        .getEventsOn(LocalDate.of(2025, 2, 1));
    assertEquals(1, copiedEvents.size());
    String copiedSeriesUid = copiedEvents.get(0).getSeriesUid();
    assertNotNull(copiedSeriesUid);

    target.editSeries(new TestBuilders.EventBuilder()
        .subject("Weekly Meeting")
        .start("2025-02-01T10:00:00-05:00[America/New_York]")
        .property("subject")
        .value("Updated Weekly Meeting")
        .build());

    List<CalendarEvent> afterEdit = target.getAllEvents();
    assertEquals(1, afterEdit.size());
    assertEquals("Updated Weekly Meeting", afterEdit.get(0).getSubject());
  }

  @Test
  public void testCopyEventsBetweenMaintainsMultipleSeriesRelationships() {
    CalendarModel source = new CalendarModel(ZoneId.of("America/New_York"));

    source.createEventSeries(new TestBuilders.EventBuilder()
        .subject("Standup")
        .start("2025-01-13T09:00:00-05:00[America/New_York]")
        .end("2025-01-13T09:30:00-05:00[America/New_York]")
        .weekdays("MTWRF")
        .ndays(7)
        .build());

    source.createEventSeries(new TestBuilders.EventBuilder()
        .subject("Team Sync")
        .start("2025-01-13T14:00:00-05:00[America/New_York]")
        .end("2025-01-13T15:00:00-05:00[America/New_York]")
        .weekdays("MW")
        .ndays(7)
        .build());

    assertEquals(14, source.getAllEvents().size());

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("startdate", LocalDate.of(2025, 1, 13));
    copyParams.put("enddate", LocalDate.of(2025, 1, 17));
    copyParams.put("targetdate", LocalDate.of(2025, 3, 1));

    CalendarModel target = new CalendarModel(ZoneId.of("America/New_York"));
    source.copyEventsBetween(target, copyParams);

    List<CalendarEvent> copiedEvents = target.getAllEvents();
    copiedEvents.sort(Comparator.comparing(CalendarEvent::getStartDateTime));
    assertEquals(7, copiedEvents.size());

    String standupSeriesUid = null;
    String syncSeriesUid = null;

    for (CalendarEvent event : copiedEvents) {
      assertNotNull(event.getSeriesUid());
      if (event.getSubject().equals("Standup")) {
        if (standupSeriesUid == null) {
          standupSeriesUid = event.getSeriesUid();
        } else {
          assertEquals(standupSeriesUid, event.getSeriesUid());
        }
      } else if (event.getSubject().equals("Team Sync")) {
        if (syncSeriesUid == null) {
          syncSeriesUid = event.getSeriesUid();
        } else {
          assertEquals(syncSeriesUid, event.getSeriesUid());
        }
      }
    }

    assertNotNull(standupSeriesUid);
    assertNotNull(syncSeriesUid);
    assertNotEquals(standupSeriesUid, syncSeriesUid);

    target.editSeries(new TestBuilders.EventBuilder()
        .subject("Standup")
        .start("2025-03-03T09:00:00-05:00[America/New_York]")
        .property("location")
        .value("online")
        .build());

    int standupWithLocation = 0;
    int syncWithoutLocation = 0;

    for (CalendarEvent event : target.getAllEvents()) {
      if (event.getSubject().equals("Standup")) {
        assertEquals("online", event.getLocation());
        standupWithLocation++;
      } else if (event.getSubject().equals("Team Sync")) {
        assertNull(event.getLocation());
        syncWithoutLocation++;
      }
    }

    assertEquals(5, standupWithLocation);
    assertEquals(2, syncWithoutLocation);
  }

  @Test
  public void testCopyEventsOnValidationPreventsInvalidState() {
    CalendarModel source = new CalendarModel(ZoneId.of("America/New_York"));
    CalendarModel target = new CalendarModel(ZoneId.of("America/New_York"));

    source.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build());

    source.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Review")
        .start("2025-01-15T14:00:00-05:00[America/New_York]")
        .end("2025-01-15T15:00:00-05:00[America/New_York]")
        .build());

    target.createSingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-02-10T10:00:00-05:00[America/New_York]")
        .end("2025-02-10T11:00:00-05:00[America/New_York]")
        .build());

    Map<String, Object> copyParams = new HashMap<>();
    copyParams.put("sourcedate", LocalDate.of(2025, 1, 15));
    copyParams.put("targetdate", LocalDate.of(2025, 2, 10));

    try {
      source.copyEventsOn(target, copyParams);
      fail("Should throw duplicate error");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("duplicate")
          || e.getMessage().contains("already exists"));
    }

    assertEquals(1, target.getAllEvents().size());
  }

}