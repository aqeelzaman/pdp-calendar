import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.TestBuilders.EventBuilder;
import calendar.model.event.CalendarEvent;
import calendar.model.event.SeriesLinkedList;
import calendar.model.event.SingleEvent;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for SeriesLinkedList class.
 * Tests doubly-linked list operations for managing series events.
 */
public class SeriesLinkedListTest {

  private SeriesLinkedList list;
  private String seriesUid;

  /**
   * Sets up a fresh SeriesLinkedList before each test.
   */
  @Before
  public void setUp() {
    seriesUid = "test-series-uid";
    list = new SeriesLinkedList(seriesUid);
  }

  /**
   * Creates an event with custom parameters.
   *
   * @param subject  the event subject.
   * @param startStr the start datetime string.
   * @param endStr   the end datetime string.
   * @return a new CalendarEvent.
   */
  private CalendarEvent createEvent(String subject, String startStr, String endStr) {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", subject);
    params.put("start", ZonedDateTime.parse(startStr));
    params.put("end", ZonedDateTime.parse(endStr));
    return new SingleEvent(params, seriesUid);
  }

  @Test
  public void testConstructor() {
    assertEquals(seriesUid, list.getSeriesUid());
    assertEquals(0, list.size());
    assertTrue(list.isEmpty());
    assertNull(list.getFirstEvent());
  }

  @Test
  public void testAddSingleEventToEmptyList() {
    CalendarEvent event = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    list.addSingleEvent(event);

    assertEquals(1, list.size());
    assertFalse(list.isEmpty());
    assertEquals(event, list.getFirstEvent());
  }

  @Test
  public void testAddSingleEventMaintainsOrder() {
    list.addSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = list.getAllEvents();
    assertEquals("Event 1", events.get(0).getSubject());
    assertEquals("Event 2", events.get(1).getSubject());
    assertEquals("Event 3", events.get(2).getSubject());
  }

  @Test
  public void testAddSingleEventAtBeginning() {
    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    assertEquals("Event 1", list.getFirstEvent().getSubject());
  }

  @Test
  public void testAddSingleEventAtEnd() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = list.getAllEvents();
    assertEquals("Event 2", events.get(1).getSubject());
  }

  @Test
  public void testAddSingleEventInMiddle() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = list.getAllEvents();
    assertEquals("Event 2", events.get(1).getSubject());
  }

  @Test
  public void testAddAllToEmptyList() {
    List<CalendarEvent> events = new ArrayList<>();
    events.add(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));
    events.add(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    list.addAll(events);

    assertEquals(2, list.size());
    assertEquals("Event 1", list.getFirstEvent().getSubject());
  }

  @Test
  public void testAddAllWithNullList() {
    list.addAll(null);
    assertEquals(0, list.size());
  }

  @Test
  public void testAddAllWithEmptyList() {
    list.addAll(new ArrayList<>());
    assertEquals(0, list.size());
  }

  @Test
  public void testGetAllEventsEmptyList() {
    List<CalendarEvent> events = list.getAllEvents();
    assertEquals(0, events.size());
  }

  @Test
  public void testGetAllEvents() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = list.getAllEvents();
    assertEquals(2, events.size());
  }

  @Test
  public void testRemoveSingleEventFromMiddle() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    list.removeSingleEvent("Event 2",
        ZonedDateTime.parse("2025-11-05T10:00:00-05:00[America/New_York]"));

    assertEquals(2, list.size());
    List<CalendarEvent> events = list.getAllEvents();
    assertEquals("Event 1", events.get(0).getSubject());
    assertEquals("Event 3", events.get(1).getSubject());
  }

  @Test
  public void testRemoveSingleEventFromHead() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    list.removeSingleEvent("Event 1",
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    assertEquals(1, list.size());
    assertEquals("Event 2", list.getFirstEvent().getSubject());
  }

  @Test
  public void testRemoveSingleEventFromTail() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    list.removeSingleEvent("Event 2",
        ZonedDateTime.parse("2025-11-05T10:00:00-05:00[America/New_York]"));

    assertEquals(1, list.size());
    assertEquals("Event 1", list.getFirstEvent().getSubject());
  }

  @Test
  public void testRemoveSingleEventNotFound() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.removeSingleEvent("NonExistent",
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    assertEquals(1, list.size());
  }

  @Test
  public void testRemoveSingleEventLastOne() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.removeSingleEvent("Event 1",
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    assertEquals(0, list.size());
    assertTrue(list.isEmpty());
    assertNull(list.getFirstEvent());
  }

  @Test
  public void testGetEventsFrom() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event2);

    list.addSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> eventsFrom = list.getEventsFrom(event2);

    assertEquals(2, eventsFrom.size());
    assertEquals("Event 2", eventsFrom.get(0).getSubject());
    assertEquals("Event 3", eventsFrom.get(1).getSubject());
  }

  @Test
  public void testGetEventsFromFirstEvent() {
    CalendarEvent event1 = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event1);

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> eventsFrom = list.getEventsFrom(event1);

    assertEquals(2, eventsFrom.size());
  }

  @Test
  public void testGetEventsFromLastEvent() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event2);

    List<CalendarEvent> eventsFrom = list.getEventsFrom(event2);

    assertEquals(1, eventsFrom.size());
    assertEquals("Event 2", eventsFrom.get(0).getSubject());
  }

  @Test
  public void testGetEventsFromNotFound() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent nonExistent = createEvent("NonExistent",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    List<CalendarEvent> eventsFrom = list.getEventsFrom(nonExistent);

    assertEquals(0, eventsFrom.size());
  }

  @Test
  public void testGetEventsFromWithEndEvent() {
    CalendarEvent event1 = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event1);

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event2);

    list.addSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> eventsRange = list.getEventsFrom(event1, event2);

    assertEquals(2, eventsRange.size());
    assertEquals("Event 1", eventsRange.get(0).getSubject());
    assertEquals("Event 2", eventsRange.get(1).getSubject());
  }

  @Test
  public void testGetEventsFromSameStartAndEnd() {
    CalendarEvent event = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event);

    List<CalendarEvent> eventsRange = list.getEventsFrom(event, event);

    assertEquals(1, eventsRange.size());
  }

  @Test
  public void testGetEventsFromEndEventNotFound() {
    CalendarEvent event1 = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event1);

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    CalendarEvent nonExistent = createEvent("NonExistent",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    List<CalendarEvent> eventsRange = list.getEventsFrom(event1, nonExistent);

    assertEquals(2, eventsRange.size());
  }

  @Test
  public void testUpdateSeriesFromFirstEvent() {
    CalendarEvent event1 = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event1);

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    list.updateSeries(event1, "subject", "Updated");

    List<CalendarEvent> events = list.getAllEvents();
    assertEquals("Updated", events.get(0).getSubject());
    assertEquals("Updated", events.get(1).getSubject());
  }

  @Test
  public void testUpdateSeriesFromMiddleEvent() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event2);

    list.addSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    list.updateSeries(event2, "location", "online");

    List<CalendarEvent> events = list.getAllEvents();
    assertNull(events.get(0).getLocation());
    assertEquals("online", events.get(1).getLocation());
    assertEquals("online", events.get(2).getLocation());
  }

  @Test
  public void testUpdateSeriesEventNotFound() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent nonExistent = createEvent("NonExistent",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    list.updateSeries(nonExistent, "subject", "Updated");

    assertEquals("Event 1", list.getFirstEvent().getSubject());
  }

  @Test
  public void testSplitUpdateFromMiddle() {
    CalendarEvent event1 = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T17:00:00-05:00[America/New_York]");
    list.addSingleEvent(event1);

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T16:00:00-05:00[America/New_York]");
    list.addSingleEvent(event2);

    list.addSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T15:00:00-05:00[America/New_York]"));

    SeriesLinkedList newSeries = list.splitUpdateFrom(event2, "new-uid",
        ZonedDateTime.parse("2025-11-05T14:00:00-05:00[America/New_York]"));

    assertNotNull(newSeries);
    assertEquals(2, newSeries.size());
    assertEquals(1, list.size());
    assertEquals("Event 1", list.getFirstEvent().getSubject());
  }

  @Test
  public void testSplitUpdateFromHead() {
    CalendarEvent event1 = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T15:00:00-05:00[America/New_York]");
    list.addSingleEvent(event1);

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T15:00:00-05:00[America/New_York]"));

    SeriesLinkedList newSeries = list.splitUpdateFrom(event1, "new-uid",
        ZonedDateTime.parse("2025-11-04T14:00:00-05:00[America/New_York]"));

    assertNotNull(newSeries);
    assertEquals(2, newSeries.size());
    assertEquals(0, list.size());
    assertTrue(list.isEmpty());
  }

  @Test
  public void testSplitUpdateFromTail() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T15:00:00-05:00[America/New_York]"));

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T15:00:00-05:00[America/New_York]");
    list.addSingleEvent(event2);

    SeriesLinkedList newSeries = list.splitUpdateFrom(event2, "new-uid",
        ZonedDateTime.parse("2025-11-05T14:00:00-05:00[America/New_York]"));

    assertNotNull(newSeries);
    assertEquals(1, newSeries.size());
    assertEquals(1, list.size());
  }

  @Test
  public void testSplitUpdateFromNotFound() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent nonExistent = createEvent("NonExistent",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    SeriesLinkedList newSeries = list.splitUpdateFrom(nonExistent, "new-uid",
        ZonedDateTime.parse("2025-11-10T14:00:00-05:00[America/New_York]"));

    assertNull(newSeries);
    assertEquals(1, list.size());
  }

  @Test
  public void testSplitUpdateFromUpdatesStartTimes() {
    CalendarEvent event1 = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event1);

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event2);

    SeriesLinkedList newSeries = list.splitUpdateFrom(event1, "new-uid",
        ZonedDateTime.parse("2025-11-04T10:30:00-05:00[America/New_York]"));

    List<CalendarEvent> newEvents = newSeries.getAllEvents();
    for (CalendarEvent event : newEvents) {
      assertEquals(10, event.getStartDateTime().getHour());
      assertEquals(30, event.getStartDateTime().getMinute());
    }
  }

  @Test
  public void testSplitUpdateFromUpdatesSeriesUid() {
    CalendarEvent event1 = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T17:00:00-05:00[America/New_York]");
    list.addSingleEvent(event1);

    SeriesLinkedList newSeries = list.splitUpdateFrom(event1, "new-uid",
        ZonedDateTime.parse("2025-11-04T14:00:00-05:00[America/New_York]"));

    List<CalendarEvent> newEvents = newSeries.getAllEvents();
    for (CalendarEvent event : newEvents) {
      assertEquals("new-uid", event.getSeriesUid());
    }
  }

  @Test
  public void testReplaceEvent() {
    CalendarEvent original = createEvent("Original",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(original);

    CalendarEvent replacement = createEvent("Replaced",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    list.replaceEvent(original, replacement);

    assertEquals("Replaced", list.getFirstEvent().getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReplaceEventNotFound() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent nonExistent = createEvent("NonExistent",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    CalendarEvent replacement = createEvent("Replaced",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    list.replaceEvent(nonExistent, replacement);
  }

  @Test
  public void testReplaceEventInMiddle() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event2);

    list.addSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    CalendarEvent replacement = createEvent("Replaced 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");

    list.replaceEvent(event2, replacement);

    List<CalendarEvent> events = list.getAllEvents();
    assertEquals("Replaced 2", events.get(1).getSubject());
  }

  @Test
  public void testSize() {
    assertEquals(0, list.size());

    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));
    assertEquals(1, list.size());

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));
    assertEquals(2, list.size());
  }

  @Test
  public void testIsEmpty() {
    assertTrue(list.isEmpty());

    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));
    assertFalse(list.isEmpty());

    list.removeSingleEvent("Event 1",
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));
    assertTrue(list.isEmpty());
  }

  @Test
  public void testGetSeriesUid() {
    assertEquals(seriesUid, list.getSeriesUid());
  }

  @Test
  public void testGetFirstEventNull() {
    assertNull(list.getFirstEvent());
  }

  @Test
  public void testGetFirstEventPresent() {
    CalendarEvent event = createEvent("First",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event);

    assertEquals("First", list.getFirstEvent().getSubject());
  }

  @Test
  public void testAddSingleEventUpdatesSize() {
    for (int i = 0; i < 5; i++) {
      list.addSingleEvent(createEvent("Event " + i,
          "2025-11-" + String.format("%02d", i + 4) + "T10:00:00-05:00[America/New_York]",
          "2025-11-" + String.format("%02d", i + 4) + "T11:00:00-05:00[America/New_York]"));
      assertEquals(i + 1, list.size());
    }
  }

  @Test
  public void testRemoveSingleEventUpdatesSize() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    assertEquals(2, list.size());

    list.removeSingleEvent("Event 1",
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    assertEquals(1, list.size());
  }

  @Test
  public void testSplitUpdateFromUpdatesOriginalSize() {
    list.addSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");
    list.addSingleEvent(event2);

    list.addSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    assertEquals(3, list.size());

    SeriesLinkedList newSeries = list.splitUpdateFrom(event2, "new-uid",
        ZonedDateTime.parse("2025-11-05T10:30:00-05:00[America/New_York]"));

    assertEquals(1, list.size());
    assertEquals(2, newSeries.size());
  }

  @Test
  public void testComplexAddRemoveSequence() {
    list.addSingleEvent(createEvent("Event A",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event C",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    list.addSingleEvent(createEvent("Event B",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    assertEquals(3, list.size());

    list.removeSingleEvent("Event B",
        ZonedDateTime.parse("2025-11-05T10:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = list.getAllEvents();
    assertEquals(2, events.size());
    assertEquals("Event A", events.get(0).getSubject());
    assertEquals("Event C", events.get(1).getSubject());
  }

  @Test
  public void testAddSingleEventBeforeHeadUpdatesSize() {
    SeriesLinkedList list = new SeriesLinkedList("series-123");

    CalendarEvent event1 = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build(), "series-123");

    list.addSingleEvent(event1);
    assertEquals(1, list.size());

    CalendarEvent event2 = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T09:00:00-05:00[America/New_York]")
        .end("2025-01-15T09:30:00-05:00[America/New_York]")
        .build(), "series-123");

    list.addSingleEvent(event2);
    assertEquals(2, list.size());

    List<CalendarEvent> events = list.getAllEvents();
    assertEquals(2, events.size());
    assertEquals("2025-01-15T09:00", events.get(0).getStartDateTime()
        .toLocalDateTime().toString().substring(0, 16));
    assertEquals("2025-01-15T10:00", events.get(1).getStartDateTime()
        .toLocalDateTime().toString().substring(0, 16));
  }

  @Test
  public void testAddMultipleEventsBeforeHeadMaintainsSize() {
    SeriesLinkedList list = new SeriesLinkedList("series-123");

    CalendarEvent event1 = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T15:00:00-05:00[America/New_York]")
        .end("2025-01-15T16:00:00-05:00[America/New_York]")
        .build(), "series-123");

    list.addSingleEvent(event1);
    assertEquals(1, list.size());

    CalendarEvent event2 = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build(), "series-123");

    list.addSingleEvent(event2);
    assertEquals(2, list.size());

    CalendarEvent event3 = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T08:00:00-05:00[America/New_York]")
        .end("2025-01-15T09:00:00-05:00[America/New_York]")
        .build(), "series-123");

    list.addSingleEvent(event3);
    assertEquals(3, list.size());

    List<CalendarEvent> events = list.getAllEvents();
    assertEquals(3, events.size());
  }

  @Test
  public void testGetEventsFromWithStartEventNotInList() {
    SeriesLinkedList list = new SeriesLinkedList("series-123");

    CalendarEvent event1 = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-13T10:00:00-05:00[America/New_York]")
        .end("2025-01-13T11:00:00-05:00[America/New_York]")
        .build(), "series-123");

    CalendarEvent event2 = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build(), "series-123");

    list.addSingleEvent(event1);
    list.addSingleEvent(event2);

    CalendarEvent nonExistentEvent = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-20T10:00:00-05:00[America/New_York]")
        .end("2025-01-20T11:00:00-05:00[America/New_York]")
        .build(), "series-123");

    List<CalendarEvent> result = list.getEventsFrom(nonExistentEvent, null);

    assertEquals(0, result.size());
    result.add(nonExistentEvent);
    assertEquals(1, result.size());
  }

  @Test
  public void testGetEventsFromEmptyListReturnsNewArrayList() {
    SeriesLinkedList emptyList = new SeriesLinkedList("empty-series");

    CalendarEvent event = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build(), "empty-series");

    List<CalendarEvent> result = emptyList.getEventsFrom(event, null);

    assertEquals(0, result.size());
    result.add(event);
    assertEquals(1, result.size());
  }

  @Test
  public void testGetEventsFromWithStartBeforeAllEvents() {
    SeriesLinkedList list = new SeriesLinkedList("series-123");

    CalendarEvent event1 = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-15T10:00:00-05:00[America/New_York]")
        .end("2025-01-15T11:00:00-05:00[America/New_York]")
        .build(), "series-123");

    CalendarEvent event2 = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-17T10:00:00-05:00[America/New_York]")
        .end("2025-01-17T11:00:00-05:00[America/New_York]")
        .build(), "series-123");

    list.addSingleEvent(event1);
    list.addSingleEvent(event2);

    CalendarEvent earlierEvent = new SingleEvent(new EventBuilder()
        .subject("Meeting")
        .start("2025-01-10T10:00:00-05:00[America/New_York]")
        .end("2025-01-10T11:00:00-05:00[America/New_York]")
        .build(), "series-123");

    List<CalendarEvent> result = list.getEventsFrom(earlierEvent, null);

    assertEquals(0, result.size());
    result.add(earlierEvent);
    result.add(event1);
    assertEquals(2, result.size());
  }
}