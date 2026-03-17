import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.TestBuilders;
import calendar.model.event.CalendarEvent;
import calendar.model.event.SeriesEvent;
import calendar.model.event.SingleEvent;
import calendar.model.event.Weekday;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Test suite for CalendarEvent interface and its implementations.
 * Tests SingleEvent, SeriesEvent, and the Weekday enum.
 */
public class CalendarEventTest {

  /**
   * Creates a basic event parameter map for testing.
   *
   * @return map containing required event parameters.
   */
  private Map<String, Object> createParams() {
    return createParams("Test Event", "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");
  }

  /**
   * Creates an event parameter map with custom values.
   *
   * @param subject  the event subject.
   * @param startStr the start datetime string.
   * @param endStr   the end datetime string.
   * @return map containing event parameters.
   */
  private Map<String, Object> createParams(String subject, String startStr, String endStr) {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", subject);
    params.put("start", ZonedDateTime.parse(startStr));
    params.put("end", ZonedDateTime.parse(endStr));
    return params;
  }

  /**
   * Creates parameters for a series event.
   *
   * @return map containing series event parameters.
   */
  private Map<String, Object> createSeriesParams() {
    Map<String, Object> params = createParams();
    params.put("weekdays", "MTF");
    params.put("ndays", 6);
    return params;
  }

  @Test
  public void testSingleEventCreation() {
    CalendarEvent event = new SingleEvent(createParams(), null);

    assertEquals("Test Event", event.getSubject());
    assertNotNull(event.getStartDateTime());
    assertNotNull(event.getEndDateTime());
    assertNull(event.getSeriesUid());
    assertFalse(event.isPartOfSeries());
  }

  @Test
  public void testSingleEventWithSeriesUid() {
    CalendarEvent event = new SingleEvent(createParams(), "series-123");

    assertEquals("series-123", event.getSeriesUid());
    assertTrue(event.isPartOfSeries());
  }

  @Test
  public void testSingleEventWithAllOptionalFields() {
    Map<String, Object> params = createParams();
    params.put("description", "Important meeting");
    params.put("location", "physical");
    params.put("status", "private");

    CalendarEvent event = new SingleEvent(params, null);

    assertEquals("Important meeting", event.getDescription());
    assertEquals("physical", event.getLocation());
    assertEquals("private", event.getStatus());
  }

  @Test
  public void testSingleEventGetHashMap() {
    Map<String, Object> params = createParams();
    params.put("description", "Test");

    CalendarEvent event = new SingleEvent(params, null);
    Map<String, Object> map = event.getHashMap();

    assertEquals("Test Event", map.get("subject"));
    assertEquals("Test", map.get("description"));
    assertNotNull(map.get("start"));
    assertNotNull(map.get("end"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSingleEventInvalidLocation() {
    Map<String, Object> params = createParams();
    params.put("location", "invalid");

    new SingleEvent(params, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSingleEventInvalidStatus() {
    Map<String, Object> params = createParams();
    params.put("status", "confidential");

    new SingleEvent(params, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullParams() {
    new SingleEvent(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullSubject() {
    Map<String, Object> params = createParams();
    params.put("subject", null);

    new SingleEvent(params, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEmptySubject() {
    Map<String, Object> params = createParams();
    params.put("subject", "");

    new SingleEvent(params, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithWhitespaceSubject() {
    Map<String, Object> params = createParams();
    params.put("subject", "   ");

    new SingleEvent(params, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullStartDateTime() {
    Map<String, Object> params = createParams();
    params.put("start", null);

    new SingleEvent(params, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullEndDateTime() {
    Map<String, Object> params = createParams();
    params.put("end", null);

    new SingleEvent(params, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithStartAfterEnd() {
    Map<String, Object> params = createParams("Meeting",
        "2025-11-04T15:00:00-05:00[America/New_York]",
        "2025-11-04T10:00:00-05:00[America/New_York]");

    new SingleEvent(params, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithStartEqualsEnd() {
    Map<String, Object> params = createParams("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T10:00:00-05:00[America/New_York]");

    new SingleEvent(params, null);
  }

  @Test
  public void testConstructorWithValidLocationPhysical() {
    Map<String, Object> params = createParams();
    params.put("location", "physical");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals("physical", event.getLocation());
  }

  @Test
  public void testConstructorWithValidLocationOnline() {
    Map<String, Object> params = createParams();
    params.put("location", "online");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals("online", event.getLocation());
  }

  @Test
  public void testConstructorWithValidLocationMixedCase() {
    Map<String, Object> params = createParams();
    params.put("location", "Physical");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals("Physical", event.getLocation());
  }

  @Test
  public void testConstructorWithNullLocation() {
    Map<String, Object> params = createParams();
    params.put("location", null);

    CalendarEvent event = new SingleEvent(params, null);
    assertNull(event.getLocation());
  }

  @Test
  public void testConstructorWithMissingLocation() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    assertNull(event.getLocation());
  }

  @Test
  public void testConstructorWithValidStatusPrivate() {
    Map<String, Object> params = createParams();
    params.put("status", "private");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals("private", event.getStatus());
  }

  @Test
  public void testConstructorWithValidStatusPublic() {
    Map<String, Object> params = createParams();
    params.put("status", "public");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals("public", event.getStatus());
  }

  @Test
  public void testConstructorWithValidStatusMixedCase() {
    Map<String, Object> params = createParams();
    params.put("status", "Private");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals("Private", event.getStatus());
  }

  @Test
  public void testConstructorWithNullStatus() {
    Map<String, Object> params = createParams();
    params.put("status", null);

    CalendarEvent event = new SingleEvent(params, null);
    assertNull(event.getStatus());
  }

  @Test
  public void testGetSubject() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    assertEquals("Test Event", event.getSubject());
  }

  @Test
  public void testGetStartDateTime() {
    Map<String, Object> params = createParams();
    ZonedDateTime expectedStart = (ZonedDateTime) params.get("start");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals(expectedStart, event.getStartDateTime());
  }

  @Test
  public void testGetEndDateTime() {
    Map<String, Object> params = createParams();
    ZonedDateTime expectedEnd = (ZonedDateTime) params.get("end");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals(expectedEnd, event.getEndDateTime());
  }

  @Test
  public void testGetDescriptionPresent() {
    Map<String, Object> params = createParams();
    params.put("description", "Important meeting");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals("Important meeting", event.getDescription());
  }

  @Test
  public void testGetDescriptionMissing() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    assertNull(event.getDescription());
  }

  @Test
  public void testGetLocationPresent() {
    Map<String, Object> params = createParams();
    params.put("location", "physical");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals("physical", event.getLocation());
  }

  @Test
  public void testGetLocationMissing() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    assertNull(event.getLocation());
  }

  @Test
  public void testGetStatusPresent() {
    Map<String, Object> params = createParams();
    params.put("status", "private");

    CalendarEvent event = new SingleEvent(params, null);
    assertEquals("private", event.getStatus());
  }

  @Test
  public void testGetStatusMissing() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    assertNull(event.getStatus());
  }

  @Test
  public void testGetSeriesUidNull() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    assertNull(event.getSeriesUid());
  }

  @Test
  public void testGetSeriesUidPresent() {
    CalendarEvent event = new SingleEvent(createParams(), "series-123");
    assertEquals("series-123", event.getSeriesUid());
  }

  @Test
  public void testGetHashMapContainsAllFields() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    Map<String, Object> map = event.getHashMap();

    assertTrue(map.containsKey("subject"));
    assertTrue(map.containsKey("start"));
    assertTrue(map.containsKey("end"));
    assertTrue(map.containsKey("description"));
    assertTrue(map.containsKey("location"));
    assertTrue(map.containsKey("status"));
  }

  @Test
  public void testGetHashMapWithNullOptionalFields() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    Map<String, Object> map = event.getHashMap();

    assertNull(map.get("description"));
    assertNull(map.get("location"));
    assertNull(map.get("status"));
  }

  @Test
  public void testIsPartOfSeriesFalse() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    assertFalse(event.isPartOfSeries());
  }

  @Test
  public void testIsPartOfSeriesTrue() {
    CalendarEvent event = new SingleEvent(createParams(), "series-uid");
    assertTrue(event.isPartOfSeries());
  }

  @Test
  public void testSeriesEventCreation() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());

    assertEquals("Test Event", series.getSubject());
    assertNotNull(series.getSeriesUid());
    assertTrue(series.isPartOfSeries());
  }

  @Test
  public void testSeriesEventGeneratesCorrectNumberOfEvents() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());
    List<CalendarEvent> events = series.getEventsInSeries();

    assertEquals(6, events.size());
  }

  @Test
  public void testSeriesEventAllEventsHaveSameSeriesUid() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());
    List<CalendarEvent> events = series.getEventsInSeries();
    String seriesUid = series.getSeriesUid();

    for (CalendarEvent event : events) {
      assertEquals(seriesUid, event.getSeriesUid());
    }
  }

  @Test
  public void testSeriesEventAllEventsHaveSameSubject() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());
    List<CalendarEvent> events = series.getEventsInSeries();

    for (CalendarEvent event : events) {
      assertEquals("Test Event", event.getSubject());
    }
  }

  @Test
  public void testSeriesEventCorrectDaysOfWeek() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());
    List<CalendarEvent> events = series.getEventsInSeries();

    for (CalendarEvent event : events) {
      DayOfWeek day = event.getStartDateTime().getDayOfWeek();
      assertTrue(day == DayOfWeek.MONDAY
          || day == DayOfWeek.TUESDAY
          || day == DayOfWeek.FRIDAY);
    }
  }

  @Test
  public void testSeriesEventWithUntilDate() {
    Map<String, Object> params = createParams("Weekly Meeting",
        "2025-11-03T10:00:00-05:00[America/New_York]",
        "2025-11-03T11:00:00-05:00[America/New_York]");
    params.put("weekdays", "M");
    params.put("untildate", LocalDate.parse("2025-11-24"));

    SeriesEvent series = new SeriesEvent(params);
    List<CalendarEvent> events = series.getEventsInSeries();

    assertEquals(4, events.size());
  }

  @Test
  public void testSeriesEventGetDaysOfWeek() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());
    HashSet<DayOfWeek> days = series.getDaysOfWeek();

    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testSeriesEventGetRepeatCount() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());
    assertEquals(6, series.getRepeatCount());
  }

  @Test
  public void testSeriesEventGetRepeatUntilNull() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());
    assertNull(series.getRepeatUntil());
  }

  @Test
  public void testSeriesEventGetRepeatUntilPresent() {
    Map<String, Object> params = createParams();
    params.put("weekdays", "M");
    LocalDate until = LocalDate.parse("2025-11-24");
    params.put("untildate", until);

    SeriesEvent series = new SeriesEvent(params);

    assertEquals(until, series.getRepeatUntil());
    assertEquals(0, series.getRepeatCount());
  }

  @Test
  public void testSeriesEventGetEventsList() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());

    assertNotNull(series.getEventsList());
    assertEquals(6, series.getEventsList().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSeriesEventMultiDaySpan() {
    Map<String, Object> params = createParams("Invalid",
        "2025-11-04T23:00:00-05:00[America/New_York]",
        "2025-11-05T01:00:00-05:00[America/New_York]");
    params.put("weekdays", "MTF");
    params.put("ndays", 3);

    new SeriesEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSeriesEventInvalidRepeatTimes() {
    Map<String, Object> params = createParams("Invalid",
        "2025-11-04T23:00:00-05:00[America/New_York]",
        "2025-11-04T23:30:00-05:00[America/New_York]");
    params.put("weekdays", "MTF");
    params.put("ndays", -3);

    new SeriesEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSeriesEventInvalidRepeatNever() {
    Map<String, Object> params = createParams("Invalid",
        "2025-11-04T23:00:00-05:00[America/New_York]",
        "2025-11-04T23:30:00-05:00[America/New_York]");
    params.put("weekdays", "MTF");
    params.put("ndays", 0);

    new SeriesEvent(params);
  }

  @Test
  public void testSeriesEventValidForSingleEvent() {
    Map<String, Object> params = createParams("Invalid",
        "2025-11-04T23:00:00-05:00[America/New_York]",
        "2025-11-04T23:30:00-05:00[America/New_York]");
    params.put("weekdays", "MTF");
    params.put("ndays", 1);

    SeriesEvent single = new SeriesEvent(params);
    List<CalendarEvent> events = single.getEventsInSeries();
    assertEquals(1, events.size());
    assertEquals(events.get(0).getSeriesUid(), single.getSeriesUid());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSeriesEventMissingWeekdays() {
    Map<String, Object> params = createParams();
    params.put("ndays", 5);

    new SeriesEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSeriesEventInvalidWeekdayCode() {
    Map<String, Object> params = createParams();
    params.put("weekdays", "XYZ");
    params.put("ndays", 5);

    new SeriesEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSeriesEventEmptyWeekdayCode() {
    Map<String, Object> params = createParams();
    params.put("weekdays", "");
    params.put("ndays", 5);

    new SeriesEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSeriesEventZeroRepeatCount() {
    Map<String, Object> params = createParams();
    params.put("weekdays", "M");
    params.put("ndays", 0);

    new SeriesEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSeriesEventNegativeRepeatCount() {
    Map<String, Object> params = createParams();
    params.put("weekdays", "M");
    params.put("ndays", -5);

    new SeriesEvent(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSeriesEventUntilDateBeforeStart() {
    Map<String, Object> params = createParams();
    params.put("weekdays", "M");
    params.put("untildate", LocalDate.parse("2025-11-01"));

    new SeriesEvent(params);
  }

  @Test
  public void testSeriesEventAllDayWeeks() {
    Map<String, Object> params = createParams();
    params.put("weekdays", "MTWRFSU");
    params.put("ndays", 7);

    SeriesEvent series = new SeriesEvent(params);

    assertEquals(7, series.getEventsInSeries().size());
  }

  @Test
  public void testSeriesEventEventsInChronologicalOrder() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());
    List<CalendarEvent> events = series.getEventsInSeries();

    for (int i = 0; i < events.size() - 1; i++) {
      assertTrue(events.get(i).getStartDateTime()
          .isBefore(events.get(i + 1).getStartDateTime()));
    }
  }

  @Test
  public void testSeriesEventAllEventsSameTimeOfDay() {
    SeriesEvent series = new SeriesEvent(createSeriesParams());
    List<CalendarEvent> events = series.getEventsInSeries();

    for (CalendarEvent event : events) {
      assertEquals(10, event.getStartDateTime().getHour());
      assertEquals(11, event.getEndDateTime().getHour());
    }
  }

  @Test
  public void testSeriesEventWithDescription() {
    Map<String, Object> params = createSeriesParams();
    params.put("description", "Daily standup");

    SeriesEvent series = new SeriesEvent(params);
    List<CalendarEvent> events = series.getEventsInSeries();

    for (CalendarEvent event : events) {
      assertEquals("Daily standup", event.getDescription());
    }
  }

  @Test
  public void testSeriesEventWithLocation() {
    Map<String, Object> params = createSeriesParams();
    params.put("location", "online");

    SeriesEvent series = new SeriesEvent(params);
    List<CalendarEvent> events = series.getEventsInSeries();

    for (CalendarEvent event : events) {
      assertEquals("online", event.getLocation());
    }
  }

  @Test
  public void testSeriesEventWithStatus() {
    Map<String, Object> params = createSeriesParams();
    params.put("status", "public");

    SeriesEvent series = new SeriesEvent(params);
    List<CalendarEvent> events = series.getEventsInSeries();

    for (CalendarEvent event : events) {
      assertEquals("public", event.getStatus());
    }
  }

  @Test
  public void testEqualsSameSubjectStartEnd() {
    CalendarEvent event1 = new SingleEvent(createParams(), null);
    CalendarEvent event2 = new SingleEvent(createParams(), null);

    assertTrue(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentSubject() {
    Map<String, Object> params1 = createParams();
    Map<String, Object> params2 = createParams("Different",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    CalendarEvent event1 = new SingleEvent(params1, null);
    CalendarEvent event2 = new SingleEvent(params2, null);

    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentStart() {
    Map<String, Object> params1 = createParams();
    Map<String, Object> params2 = createParams("Test Event",
        "2025-11-04T14:00:00-05:00[America/New_York]",
        "2025-11-04T15:00:00-05:00[America/New_York]");

    CalendarEvent event1 = new SingleEvent(params1, null);
    CalendarEvent event2 = new SingleEvent(params2, null);

    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsDifferentEnd() {
    Map<String, Object> params1 = createParams();
    Map<String, Object> params2 = createParams("Test Event",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T15:00:00-05:00[America/New_York]");

    CalendarEvent event1 = new SingleEvent(params1, null);
    CalendarEvent event2 = new SingleEvent(params2, null);

    assertFalse(event1.equals(event2));
  }

  @Test
  public void testEqualsSingleEventAndSeriesEvent() {
    CalendarEvent single = new SingleEvent(createParams(), null);
    SeriesEvent series = new SeriesEvent(createSeriesParams());

    assertTrue(single.equals(series));
  }

  @Test
  public void testEqualsIgnoresOptionalFields() {
    Map<String, Object> params1 = createParams();
    params1.put("description", "Desc 1");
    params1.put("location", "physical");

    Map<String, Object> params2 = createParams();
    params2.put("description", "Desc 2");
    params2.put("location", "online");

    CalendarEvent event1 = new SingleEvent(params1, null);
    CalendarEvent event2 = new SingleEvent(params2, null);

    assertEquals(event1, event2);
  }

  @Test
  public void testEqualsSameObject() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    assertEquals(event, event);
  }


  @Test
  public void testEqualsWithNull() {
    CalendarEvent event = new SingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build(), null);

    assertFalse(event.equals(null));
  }

  @Test
  public void testEqualsWithDifferentType() {
    CalendarEvent event = new SingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build(), null);

    assertFalse(event.equals("Not a CalendarEvent"));
  }

  @Test
  public void testEqualsWithInteger() {
    CalendarEvent event = new SingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build(), null);

    assertFalse(event.equals(123));
  }

  @Test
  public void testEqualsWithObject() {
    CalendarEvent event = new SingleEvent(new TestBuilders.EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build(), null);

    assertFalse(event.equals(new Object()));
  }

  @Test
  public void testHashCodeConsistency() {
    CalendarEvent event = new SingleEvent(createParams(), null);
    assertEquals(event.hashCode(), event.hashCode());
  }

  @Test
  public void testHashCodeEqualEvents() {
    CalendarEvent event1 = new SingleEvent(createParams(), null);
    CalendarEvent event2 = new SingleEvent(createParams(), null);

    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testHashCodeDifferentSubjects() {
    Map<String, Object> params1 = createParams();
    Map<String, Object> params2 = createParams("Different",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    CalendarEvent event1 = new SingleEvent(params1, null);
    CalendarEvent event2 = new SingleEvent(params2, null);

    assertNotEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testHashCodeIgnoresDescription() {
    Map<String, Object> params1 = createParams();
    params1.put("description", "Desc 1");

    Map<String, Object> params2 = createParams();
    params2.put("description", "Desc 2");

    CalendarEvent event1 = new SingleEvent(params1, null);
    CalendarEvent event2 = new SingleEvent(params2, null);

    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testEqualsSymmetric() {
    CalendarEvent event1 = new SingleEvent(createParams(), null);
    CalendarEvent event2 = new SingleEvent(createParams(), null);

    assertEquals(event1, event2);
    assertEquals(event2, event1);
  }

  @Test
  public void testWeekdayToDayOfWeekMonday() {
    assertEquals(DayOfWeek.MONDAY, Weekday.toDayOfWeek('M'));
  }

  @Test
  public void testWeekdayToDayOfWeekTuesday() {
    assertEquals(DayOfWeek.TUESDAY, Weekday.toDayOfWeek('T'));
  }

  @Test
  public void testWeekdayToDayOfWeekWednesday() {
    assertEquals(DayOfWeek.WEDNESDAY, Weekday.toDayOfWeek('W'));
  }

  @Test
  public void testWeekdayToDayOfWeekThursday() {
    assertEquals(DayOfWeek.THURSDAY, Weekday.toDayOfWeek('R'));
  }

  @Test
  public void testWeekdayToDayOfWeekFriday() {
    assertEquals(DayOfWeek.FRIDAY, Weekday.toDayOfWeek('F'));
  }

  @Test
  public void testWeekdayToDayOfWeekSaturday() {
    assertEquals(DayOfWeek.SATURDAY, Weekday.toDayOfWeek('S'));
  }

  @Test
  public void testWeekdayToDayOfWeekSunday() {
    assertEquals(DayOfWeek.SUNDAY, Weekday.toDayOfWeek('U'));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWeekdayToDayOfWeekInvalid() {
    Weekday.toDayOfWeek('X');
  }

  @Test
  public void testWeekdayParseDaysOfWeek() {
    HashSet<DayOfWeek> days = Weekday.parseDaysOfWeek("MTF");

    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testWeekdayParseDaysOfWeekAllDays() {
    HashSet<DayOfWeek> days = Weekday.parseDaysOfWeek("MTWRFSU");

    assertEquals(7, days.size());
  }

  @Test
  public void testWeekdayParseDaysOfWeekLowercase() {
    HashSet<DayOfWeek> days = Weekday.parseDaysOfWeek("mtf");

    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
  }

  @Test
  public void testWeekdayParseDaysOfWeekMixedCase() {
    HashSet<DayOfWeek> days = Weekday.parseDaysOfWeek("MtF");

    assertEquals(3, days.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWeekdayParseDaysOfWeekInvalidChar() {
    Weekday.parseDaysOfWeek("MXF");
  }

  @Test
  public void testWeekdayParseDaysOfWeekDuplicates() {
    HashSet<DayOfWeek> days = Weekday.parseDaysOfWeek("MMM");

    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
  }

  @Test
  public void testWeekdayEnumValues() {
    Weekday[] values = Weekday.values();
    assertEquals(7, values.length);
  }

  @Test
  public void testWeekdayGetCode() {
    assertEquals('M', Weekday.MONDAY.getCode());
    assertEquals('T', Weekday.TUESDAY.getCode());
    assertEquals('W', Weekday.WEDNESDAY.getCode());
    assertEquals('R', Weekday.THURSDAY.getCode());
    assertEquals('F', Weekday.FRIDAY.getCode());
    assertEquals('S', Weekday.SATURDAY.getCode());
    assertEquals('U', Weekday.SUNDAY.getCode());
  }

  @Test
  public void testWeekdayGetDayOfWeek() {
    assertEquals(DayOfWeek.MONDAY, Weekday.MONDAY.getDayOfWeek());
    assertEquals(DayOfWeek.TUESDAY, Weekday.TUESDAY.getDayOfWeek());
    assertEquals(DayOfWeek.WEDNESDAY, Weekday.WEDNESDAY.getDayOfWeek());
    assertEquals(DayOfWeek.THURSDAY, Weekday.THURSDAY.getDayOfWeek());
    assertEquals(DayOfWeek.FRIDAY, Weekday.FRIDAY.getDayOfWeek());
    assertEquals(DayOfWeek.SATURDAY, Weekday.SATURDAY.getDayOfWeek());
    assertEquals(DayOfWeek.SUNDAY, Weekday.SUNDAY.getDayOfWeek());
  }

  @Test
  public void testWeekdayParseEmptyString() {
    HashSet<DayOfWeek> days = Weekday.parseDaysOfWeek("");
    assertTrue(days.isEmpty());
  }

  @Test
  public void testWeekdayParseSingleDay() {
    HashSet<DayOfWeek> days = Weekday.parseDaysOfWeek("W");

    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
  }

  @Test
  public void testSeriesEventSkipsDaysNotInPattern() {
    Map<String, Object> params = createParams("Weekly",
        "2025-11-03T10:00:00-05:00[America/New_York]",
        "2025-11-03T11:00:00-05:00[America/New_York]");
    params.put("weekdays", "M");
    params.put("ndays", 3);

    SeriesEvent series = new SeriesEvent(params);
    List<CalendarEvent> events = series.getEventsInSeries();

    assertEquals(3, events.size());
    for (CalendarEvent event : events) {
      assertEquals(DayOfWeek.MONDAY, event.getStartDateTime().getDayOfWeek());
    }
  }

  @Test
  public void testSeriesEventUntilDateInclusive() {
    Map<String, Object> params = createParams("Meetings",
        "2025-11-03T10:00:00-05:00[America/New_York]",
        "2025-11-03T11:00:00-05:00[America/New_York]");
    params.put("weekdays", "M");
    params.put("untildate", LocalDate.parse("2025-11-10"));

    SeriesEvent series = new SeriesEvent(params);
    List<CalendarEvent> events = series.getEventsInSeries();

    assertEquals(2, events.size());
  }

  @Test
  public void testSingleEventMultiDayAllowed() {
    Map<String, Object> params = createParams("Conference",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-06T15:00:00-05:00[America/New_York]");

    CalendarEvent event = new SingleEvent(params, null);

    assertEquals(4, event.getStartDateTime().getDayOfMonth());
    assertEquals(6, event.getEndDateTime().getDayOfMonth());
  }

  @Test
  public void testPolymorphicBehavior() {
    CalendarEvent single = new SingleEvent(createParams(), null);
    CalendarEvent series = new SeriesEvent(createSeriesParams());

    assertNotNull(single.getSubject());
    assertNotNull(series.getSubject());

    assertNull(single.getSeriesUid());
    assertNotNull(series.getSeriesUid());
  }

}