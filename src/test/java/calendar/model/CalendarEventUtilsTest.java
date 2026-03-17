package calendar.model;

import static calendar.model.CalendarEventUtils.convertToTimezone;
import static calendar.model.CalendarEventUtils.createTempEvent;
import static calendar.model.CalendarEventUtils.isMultiDayEvent;
import static calendar.model.CalendarEventUtils.isSingleDaySeries;
import static calendar.model.CalendarEventUtils.isTimeProperty;
import static calendar.model.CalendarEventUtils.preProcess;
import static calendar.model.CalendarEventUtils.setAllDayTimes;
import static calendar.model.CalendarEventUtils.validateEditProperty;
import static calendar.model.CalendarEventUtils.withUpdatedProperty;
import static calendar.model.CalendarEventUtils.withUpdatedTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.event.CalendarEvent;
import calendar.model.event.SingleEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Test suite for CalendarEventUtils utility class.
 * Tests all utility methods for event manipulation, validation, and conversion.
 */
public class CalendarEventUtilsTest {

  /**
   * Creates a basic event parameter map for testing.
   * Creates an event on November 4, 2025 from 10:00 to 22:00 EST.
   *
   * @return map containing subject, start, and end parameters.
   */
  private Map<String, Object> createBasicEventParams() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Test Event");
    params.put("start", ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));
    params.put("end", ZonedDateTime.parse("2025-11-04T22:00:00-05:00[America/New_York]"));
    return params;
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testConstructorThrowsException() {
    new CalendarEventUtils();
  }

  @Test
  public void testWithUpdatedPropertySubject() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), null);

    CalendarEvent updated = withUpdatedProperty(original, "subject", "New Subject", null);

    assertEquals("New Subject", updated.getSubject());
    assertEquals(original.getStartDateTime(), updated.getStartDateTime());
    assertEquals(original.getEndDateTime(), updated.getEndDateTime());
  }

  @Test
  public void testWithUpdatedPropertyLocation() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), null);

    CalendarEvent updated = withUpdatedProperty(original, "location", "online", null);

    assertEquals("online", updated.getLocation());
    assertEquals("Test Event", updated.getSubject());
  }

  @Test
  public void testWithUpdatedPropertyPreservesSeriesUid() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), "series-123");

    CalendarEvent updated = withUpdatedProperty(original, "subject", "New", "series-123");

    assertEquals("series-123", updated.getSeriesUid());
  }

  @Test
  public void testWithUpdatedPropertyChangesSeriesUid() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), "old-uid");

    CalendarEvent updated = withUpdatedProperty(original, "subject", "New", "new-uid");

    assertEquals("new-uid", updated.getSeriesUid());
  }

  @Test
  public void testWithUpdatedPropertyRemovesFromSeries() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), "series-123");

    CalendarEvent updated = withUpdatedProperty(original, "subject", "New", null);

    assertNull(updated.getSeriesUid());
  }

  @Test
  public void testCreateTempEvent() {
    ZonedDateTime start = ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]");
    ZonedDateTime end = ZonedDateTime.parse("2025-11-04T11:00:00-05:00[America/New_York]");

    SingleEvent temp = createTempEvent("Meeting", start, end);

    assertEquals("Meeting", temp.getSubject());
    assertEquals(start, temp.getStartDateTime());
    assertEquals(end, temp.getEndDateTime());
    assertNull(temp.getSeriesUid());
  }

  @Test
  public void testWithUpdatedTimeStart() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), "series-123");
    ZonedDateTime newTime = ZonedDateTime.parse("2025-11-04T09:00:00-05:00[America/New_York]");

    CalendarEvent updated = withUpdatedTime(original, "start", newTime, "series-123");

    assertEquals(9, updated.getStartDateTime().getHour());
    assertEquals(4, updated.getStartDateTime().getDayOfMonth());
    assertEquals(22, updated.getEndDateTime().getHour());
  }

  @Test
  public void testWithUpdatedTimeEnd() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), "series-123");
    ZonedDateTime newTime = ZonedDateTime.parse("2025-11-04T12:00:00-05:00[America/New_York]");

    CalendarEvent updated = withUpdatedTime(original, "end", newTime, "series-123");

    assertEquals(10, updated.getStartDateTime().getHour());
    assertEquals(12, updated.getEndDateTime().getHour());
    assertEquals(4, updated.getEndDateTime().getDayOfMonth());
  }

  @Test
  public void testWithUpdatedTimePreservesDate() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), null);
    ZonedDateTime newTime = ZonedDateTime.parse("2025-11-10T14:00:00-05:00[America/New_York]");

    CalendarEvent updated = withUpdatedTime(original, "start", newTime, null);

    assertEquals(14, updated.getStartDateTime().getHour());
    assertEquals(4, updated.getStartDateTime().getDayOfMonth());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithUpdatedTimeStartAfterEnd() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), null);
    ZonedDateTime newTime = ZonedDateTime.parse("2025-11-04T23:00:00-05:00[America/New_York]");

    withUpdatedTime(original, "start", newTime, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithUpdatedTimeEndBeforeStart() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), null);
    ZonedDateTime newTime = ZonedDateTime.parse("2025-11-04T09:00:00-05:00[America/New_York]");

    withUpdatedTime(original, "end", newTime, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithUpdatedTimeStartEqualsEnd() {
    CalendarEvent original = new SingleEvent(createBasicEventParams(), null);
    ZonedDateTime newTime = ZonedDateTime.parse("2025-11-04T22:00:00-05:00[America/New_York]");

    withUpdatedTime(original, "start", newTime, null);
  }

  @Test
  public void testIsTimePropertyStart() {
    assertTrue(isTimeProperty("start"));
  }

  @Test
  public void testIsTimePropertyEnd() {
    assertTrue(isTimeProperty("end"));
  }

  @Test
  public void testIsTimePropertySubject() {
    assertFalse(isTimeProperty("subject"));
  }

  @Test
  public void testIsTimePropertyLocation() {
    assertFalse(isTimeProperty("location"));
  }

  @Test
  public void testIsSingleDaySeriesWithNdaysOne() {
    Map<String, Object> params = new HashMap<>();
    params.put("ndays", 1);

    assertTrue(isSingleDaySeries(params));
  }

  @Test
  public void testIsSingleDaySeriesWithNdaysMultiple() {
    Map<String, Object> params = new HashMap<>();
    params.put("ndays", 5);

    assertFalse(isSingleDaySeries(params));
  }

  @Test
  public void testIsSingleDaySeriesWithUntilDateSameDay() {
    Map<String, Object> params = new HashMap<>();
    params.put("start", ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));
    params.put("untildate", LocalDate.parse("2025-11-04"));

    assertTrue(isSingleDaySeries(params));
  }

  @Test
  public void testIsSingleDaySeriesWithUntilDateDifferentDay() {
    Map<String, Object> params = new HashMap<>();
    params.put("start", ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));
    params.put("untildate", LocalDate.parse("2025-11-05"));

    assertFalse(isSingleDaySeries(params));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIsSingleDaySeriesWithoutNdaysOrUntildate() {
    Map<String, Object> params = new HashMap<>();
    params.put("start", ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    isSingleDaySeries(params);
  }

  @Test
  public void testIsMultiDayEventSingleDay() {
    CalendarEvent event = new SingleEvent(createBasicEventParams(), null);

    assertFalse(isMultiDayEvent(event));
  }

  @Test
  public void testIsMultiDayEventMultipleDays() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Conference");
    params.put("start", ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));
    params.put("end", ZonedDateTime.parse("2025-11-06T17:00:00-05:00[America/New_York]"));

    CalendarEvent event = new SingleEvent(params, null);

    assertTrue(isMultiDayEvent(event));
  }

  @Test
  public void testIsMultiDayEventCrossesMidnight() {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", "Late Night");
    params.put("start", ZonedDateTime.parse("2025-11-04T23:00:00-05:00[America/New_York]"));
    params.put("end", ZonedDateTime.parse("2025-11-05T01:00:00-05:00[America/New_York]"));

    CalendarEvent event = new SingleEvent(params, null);

    assertTrue(isMultiDayEvent(event));
  }

  @Test
  public void testSetAllDayTimes() {
    Map<String, Object> params = new HashMap<>();
    params.put("ondate", LocalDate.parse("2025-11-04"));

    setAllDayTimes(params, ZoneId.of("America/New_York"));

    ZonedDateTime start = (ZonedDateTime) params.get("start");
    ZonedDateTime end = (ZonedDateTime) params.get("end");

    assertEquals(8, start.getHour());
    assertEquals(0, start.getMinute());
    assertEquals(17, end.getHour());
    assertEquals(0, end.getMinute());
    assertEquals(LocalDate.of(2025, 11, 4), start.toLocalDate());
    assertEquals(LocalDate.of(2025, 11, 4), end.toLocalDate());
  }

  @Test
  public void testValidateEditPropertyStartValid() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "start");
    params.put("value", ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    validateEditProperty(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEditPropertyMissingProperty() {
    Map<String, Object> params = new HashMap<>();
    params.put("value", "some value");

    validateEditProperty(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEditPropertyMissingValue() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "subject");

    validateEditProperty(params);
  }

  @Test
  public void testValidateEditPropertySubjectValid() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "subject");
    params.put("value", "Valid Subject");

    validateEditProperty(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEditPropertySubjectEmpty() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "subject");
    params.put("value", "");

    validateEditProperty(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEditPropertySubjectWhitespace() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "subject");
    params.put("value", "   ");

    validateEditProperty(params);
  }

  @Test
  public void testValidateEditPropertyLocationValid() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "location");
    params.put("value", "Building A");

    validateEditProperty(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEditPropertyLocationEmpty() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "location");
    params.put("value", "");

    validateEditProperty(params);
  }

  @Test
  public void testValidateEditPropertyDescriptionValid() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "description");
    params.put("value", "Important meeting");

    validateEditProperty(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateEditPropertyUnknownProperty() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "color");
    params.put("value", "blue");

    validateEditProperty(params);
  }

  @Test
  public void testConvertToTimezoneStart() {
    Map<String, Object> params = new HashMap<>();
    params.put("start", ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    convertToTimezone(params, ZoneId.of("America/Los_Angeles"));

    ZonedDateTime converted = (ZonedDateTime) params.get("start");
    assertEquals(7, converted.getHour());
    assertEquals(ZoneId.of("America/Los_Angeles"), converted.getZone());
  }

  @Test
  public void testConvertToTimezoneEnd() {
    Map<String, Object> params = new HashMap<>();
    params.put("end", ZonedDateTime.parse("2025-11-04T15:00:00-05:00[America/New_York]"));

    convertToTimezone(params, ZoneId.of("America/Chicago"));

    ZonedDateTime converted = (ZonedDateTime) params.get("end");
    assertEquals(14, converted.getHour());
    assertEquals(ZoneId.of("America/Chicago"), converted.getZone());
  }

  @Test
  public void testConvertToTimezoneValue() {
    Map<String, Object> params = new HashMap<>();
    params.put("value", ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    convertToTimezone(params, ZoneId.of("America/Denver"));

    ZonedDateTime converted = (ZonedDateTime) params.get("value");
    assertEquals(8, converted.getHour());
    assertEquals(ZoneId.of("America/Denver"), converted.getZone());
  }

  @Test
  public void testConvertToTimezoneValueNotDateTime() {
    Map<String, Object> params = new HashMap<>();
    params.put("value", "just a string");

    convertToTimezone(params, ZoneId.of("UTC"));

    assertEquals("just a string", params.get("value"));
  }

  @Test
  public void testConvertToTimezoneNullValues() {
    Map<String, Object> params = new HashMap<>();
    params.put("start", null);
    params.put("end", null);

    convertToTimezone(params, ZoneId.of("UTC"));

    assertNull(params.get("start"));
    assertNull(params.get("end"));
  }

  @Test
  public void testConvertToTimezoneMultipleFields() {
    Map<String, Object> params = new HashMap<>();
    params.put("start", ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));
    params.put("end", ZonedDateTime.parse("2025-11-04T11:00:00-05:00[America/New_York]"));
    params.put("subject", "Meeting");

    convertToTimezone(params, ZoneId.of("America/Los_Angeles"));

    ZonedDateTime start = (ZonedDateTime) params.get("start");
    ZonedDateTime end = (ZonedDateTime) params.get("end");

    assertEquals(7, start.getHour());
    assertEquals(8, end.getHour());
    assertEquals("Meeting", params.get("subject"));
  }

  @Test
  public void testPreProcessValid() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "subject");
    params.put("value", "New Subject");
    params.put("start", ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    preProcess(params, ZoneId.of("America/Chicago"));

    ZonedDateTime start = (ZonedDateTime) params.get("start");
    assertEquals(9, start.getHour());
    assertEquals(ZoneId.of("America/Chicago"), start.getZone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPreProcessInvalidProperty() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "invalid");
    params.put("value", "something");

    preProcess(params, ZoneId.of("UTC"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPreProcessMissingValue() {
    Map<String, Object> params = new HashMap<>();
    params.put("property", "subject");

    preProcess(params, ZoneId.of("UTC"));
  }
}