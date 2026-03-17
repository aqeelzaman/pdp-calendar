package calendar.model.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.model.event.CalendarEvent;
import calendar.model.event.SingleEvent;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for IntervalTree class.
 * Tests AVL tree operations, interval queries, and tree balancing.
 */
public class IntervalTreeTest {

  private IntervalTree tree;

  /**
   * Instantiating a new Interval Tree.
   */
  @Before
  public void setUp() {
    tree = new IntervalTree();
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
    return new SingleEvent(params, null);
  }

  @Test
  public void testInsertSingleEvent() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event);

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsertNullEvent() {
    tree.insertSingleEvent(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsertDuplicateEvent() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event);
    tree.insertSingleEvent(event);
  }

  @Test
  public void testInsertMultipleEvents() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T14:00:00-05:00[America/New_York]",
        "2025-11-05T15:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-03T09:00:00-05:00[America/New_York]",
        "2025-11-03T10:00:00-05:00[America/New_York]"));

    assertEquals(3, tree.getAllEvents().size());
  }

  @Test
  public void testInsertAll() {
    List<CalendarEvent> events = new ArrayList<>();
    events.add(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));
    events.add(createEvent("Event 2",
        "2025-11-05T14:00:00-05:00[America/New_York]",
        "2025-11-05T15:00:00-05:00[America/New_York]"));

    tree.insertAll(events);

    assertEquals(2, tree.getAllEvents().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsertAllWithNull() {
    tree.insertAll(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsertAllWithDuplicate() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(event);

    tree.insertAll(events);
  }

  @Test
  public void testRemoveSingleEvent() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event);
    assertEquals(1, tree.getAllEvents().size());

    tree.remove(event);
    assertEquals(0, tree.getAllEvents().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveNullEvent() {
    tree.remove(null);
  }

  @Test
  public void testRemoveNonExistentEvent() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.remove(event);

    assertEquals(0, tree.getAllEvents().size());
  }

  @Test
  public void testRemoveAll() {
    List<CalendarEvent> events = new ArrayList<>();
    events.add(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));
    events.add(createEvent("Event 2",
        "2025-11-05T14:00:00-05:00[America/New_York]",
        "2025-11-05T15:00:00-05:00[America/New_York]"));

    tree.insertAll(events);
    assertEquals(2, tree.getAllEvents().size());

    tree.removeAll(events);
    assertEquals(0, tree.getAllEvents().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveAllWithNull() {
    tree.removeAll(null);
  }

  @Test
  public void testRemoveNodeWithNoChildren() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event);
    tree.remove(event);

    assertEquals(0, tree.getAllEvents().size());
  }

  @Test
  public void testRemoveNodeWithLeftChildOnly() {
    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    CalendarEvent leftChild = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");
    tree.insertSingleEvent(leftChild);

    tree.remove(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    assertEquals(1, tree.getAllEvents().size());
  }

  @Test
  public void testRemoveNodeWithRightChildOnly() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent rightChild = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");
    tree.insertSingleEvent(rightChild);

    tree.remove(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    assertEquals(1, tree.getAllEvents().size());
  }

  @Test
  public void testRemoveNodeWithTwoChildren() {
    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    tree.remove(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    assertEquals(2, tree.getAllEvents().size());
  }

  @Test
  public void testGetAllEventsInOrder() {
    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();

    assertEquals("Event 1", events.get(0).getSubject());
    assertEquals("Event 2", events.get(1).getSubject());
    assertEquals("Event 3", events.get(2).getSubject());
  }

  @Test
  public void testGetAllEventsEmptyTree() {
    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(0, events.size());
  }

  @Test
  public void testFindEventsInRange() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> found = tree.findEventsInRange(
        ZonedDateTime.parse("2025-11-05T00:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-11-08T00:00:00-05:00[America/New_York]"));

    assertEquals(1, found.size());
    assertEquals("Event 2", found.get(0).getSubject());
  }

  @Test
  public void testFindEventsInRangeOverlapping() {
    tree.insertSingleEvent(createEvent("Multi-day",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-06T15:00:00-05:00[America/New_York]"));

    List<CalendarEvent> found = tree.findEventsInRange(
        ZonedDateTime.parse("2025-11-05T00:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-11-05T23:59:59-05:00[America/New_York]"));

    assertEquals(1, found.size());
  }

  @Test
  public void testFindEventsInRangeEmpty() {
    tree.insertSingleEvent(createEvent("Event",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> found = tree.findEventsInRange(
        ZonedDateTime.parse("2025-11-10T00:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-11-11T00:00:00-05:00[America/New_York]"));

    assertEquals(0, found.size());
  }

  @Test
  public void testFindEventsInRangeEmptyTree() {
    List<CalendarEvent> found = tree.findEventsInRange(
        ZonedDateTime.parse("2025-11-04T00:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-11-05T00:00:00-05:00[America/New_York]"));

    assertEquals(0, found.size());
  }

  @Test
  public void testFindEventsByFields() {
    tree.insertSingleEvent(createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T12:00:00-05:00[America/New_York]"));

    List<CalendarEvent> found = tree.findEventsByFields("Meeting",
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    assertEquals(2, found.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindEventsByFieldsNullSubject() {
    tree.findEventsByFields(null,
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindEventsByFieldsNullStartTime() {
    tree.findEventsByFields("Meeting", null);
  }

  @Test
  public void testFindEventsByFieldsNotFound() {
    tree.insertSingleEvent(createEvent("Event",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> found = tree.findEventsByFields("NonExistent",
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    assertEquals(0, found.size());
  }

  @Test
  public void testFindEventsByFieldsNavigatesLeft() {
    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> found = tree.findEventsByFields("Event 1",
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"));

    assertEquals(1, found.size());
    assertEquals("Event 1", found.get(0).getSubject());
  }

  @Test
  public void testFindEventsByFieldsNavigatesRight() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> found = tree.findEventsByFields("Event 2",
        ZonedDateTime.parse("2025-11-05T10:00:00-05:00[America/New_York]"));

    assertEquals(1, found.size());
    assertEquals("Event 2", found.get(0).getSubject());
  }

  @Test
  public void testFindEvent() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event);

    CalendarEvent found = tree.findEvent(event);
    assertNotNull(found);
    assertEquals("Meeting", found.getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindEventNull() {
    tree.findEvent(null);
  }

  @Test
  public void testFindEventNotFound() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    CalendarEvent found = tree.findEvent(event);
    assertNull(found);
  }

  @Test
  public void testAlreadyExistsTrue() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event);

    assertTrue(tree.alreadyExists(event));
  }

  @Test
  public void testAlreadyExistsFalse() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    assertFalse(tree.alreadyExists(event));
  }

  @Test
  public void testAlreadyExistsListTrue() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event);

    List<CalendarEvent> events = new ArrayList<>();
    events.add(createEvent("New Event",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));
    events.add(event);

    assertTrue(tree.alreadyExists(events));
  }

  @Test
  public void testAlreadyExistsListFalse() {
    List<CalendarEvent> events = new ArrayList<>();
    events.add(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));
    events.add(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    assertFalse(tree.alreadyExists(events));
  }

  @Test
  public void testIsBusyDuringEvent() {
    tree.insertSingleEvent(createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    assertTrue(tree.isBusy(
        ZonedDateTime.parse("2025-11-04T10:30:00-05:00[America/New_York]")));
  }

  @Test
  public void testIsBusyAtStartTime() {
    tree.insertSingleEvent(createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    assertTrue(tree.isBusy(
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testIsBusyAtEndTime() {
    tree.insertSingleEvent(createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    assertFalse(tree.isBusy(
        ZonedDateTime.parse("2025-11-04T11:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testIsBusyBeforeEvent() {
    tree.insertSingleEvent(createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    assertFalse(tree.isBusy(
        ZonedDateTime.parse("2025-11-04T09:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testIsBusyAfterEvent() {
    tree.insertSingleEvent(createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    assertFalse(tree.isBusy(
        ZonedDateTime.parse("2025-11-04T12:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testIsBusyEmptyTree() {
    assertFalse(tree.isBusy(
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testIsBusySearchesLeftSubtree() {
    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T15:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    assertTrue(tree.isBusy(
        ZonedDateTime.parse("2025-11-04T10:30:00-05:00[America/New_York]")));
  }

  @Test
  public void testIsBusySearchesRightSubtree() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T15:00:00-05:00[America/New_York]"));

    assertTrue(tree.isBusy(
        ZonedDateTime.parse("2025-11-05T12:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testIsBusyUsesMaxEndTimeOptimization() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    assertFalse(tree.isBusy(
        ZonedDateTime.parse("2025-11-03T10:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testIsBusyHelperTraversesLeftSubtree() {
    IntervalTree tree = new IntervalTree();
    CalendarEvent root = createEvent("root", "2024-05-01T10:00:00Z", "2024-05-01T15:00:00Z");
    tree.insertSingleEvent(root);
    CalendarEvent left = createEvent("left", "2024-05-01T08:00:00Z", "2024-05-01T09:00:00Z");
    tree.insertSingleEvent(left);
    CalendarEvent right = createEvent("right", "2024-05-01T16:00:00Z", "2024-05-01T17:00:00Z");
    tree.insertSingleEvent(right);
    ZonedDateTime targetTime = ZonedDateTime.parse("2024-05-01T08:30:00Z");
    assertTrue(tree.isBusy(targetTime));
  }

  @Test
  public void testIsBusyHelperTraversesLeftSubtreeAndReturnsFalseWhenLeftNotBusy() {
    IntervalTree tree = new IntervalTree();
    CalendarEvent root = createEvent("root", "2024-05-01T10:00:00Z", "2024-05-01T15:00:00Z");
    tree.insertSingleEvent(root);
    CalendarEvent left = createEvent("left", "2024-05-01T08:00:00Z", "2024-05-01T09:00:00Z");
    tree.insertSingleEvent(left);
    ZonedDateTime targetTime = ZonedDateTime.parse("2024-05-01T07:59:00Z");
    assertFalse(tree.isBusy(targetTime));
  }

  @Test
  public void testReplaceEventInPlace() {
    CalendarEvent original = createEvent("Old Subject",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(original);

    CalendarEvent updated = createEvent("New Subject",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.replaceEventInPlace(original, updated);

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("New Subject", events.get(0).getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReplaceEventInPlaceNotFound() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    CalendarEvent newEvent = createEvent("Updated",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.replaceEventInPlace(event, newEvent);
  }

  @Test
  public void testBalancingLeftLeft() {
    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(3, events.size());
    assertEquals("Event 1", events.get(0).getSubject());
  }

  @Test
  public void testBalancingRightRight() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(3, events.size());
    assertEquals("Event 3", events.get(2).getSubject());
  }

  @Test
  public void testBalancingLeftRight() {
    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(3, events.size());
  }

  @Test
  public void testBalancingRightLeft() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(3, events.size());
  }

  @Test
  public void testMaxEndTimeUpdatedAfterInsertion() {
    tree.insertSingleEvent(createEvent("Short Event",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Long Event",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T18:00:00-05:00[America/New_York]"));

    ZonedDateTime maxEnd = tree.getMaxEnd();
    assertEquals(18, maxEnd.getHour());
  }

  @Test
  public void testGetBalanceNull() {
    assertEquals(0, tree.getBalance(null));
  }

  @Test
  public void testHeightNull() {
    assertEquals(0, tree.height(null));
  }

  @Test
  public void testFindEventsInRangeWithMultipleEvents() {
    for (int i = 3; i <= 10; i++) {
      tree.insertSingleEvent(createEvent("Event " + i,
          "2025-11-" + String.format("%02d", i) + "T10:00:00-05:00[America/New_York]",
          "2025-11-" + String.format("%02d", i) + "T11:00:00-05:00[America/New_York]"));
    }

    List<CalendarEvent> found = tree.findEventsInRange(
        ZonedDateTime.parse("2025-11-05T00:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-11-08T00:00:00-05:00[America/New_York]"));

    assertEquals(3, found.size());
  }

  @Test
  public void testInsertEventsWithSameStartTime() {
    tree.insertSingleEvent(createEvent("Meeting A",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Meeting B",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T12:00:00-05:00[America/New_York]"));

    assertEquals(2, tree.getAllEvents().size());
  }

  @Test
  public void testRemoveAndReinsert() {
    CalendarEvent event = createEvent("Meeting",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event);
    tree.remove(event);
    tree.insertSingleEvent(event);

    assertEquals(1, tree.getAllEvents().size());
  }

  @Test
  public void testMaxEndTimeAfterDeletion() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent longEvent = createEvent("Long Event",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T20:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(longEvent);

    assertEquals(20, tree.getMaxEnd().getHour());

    tree.remove(longEvent);

    assertEquals(11, tree.getMaxEnd().getHour());
  }

  @Test
  public void testFindEventsInRangeBoundaryInclusive() {
    tree.insertSingleEvent(createEvent("Event",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> found = tree.findEventsInRange(
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-11-04T11:00:00-05:00[America/New_York]"));

    assertEquals(1, found.size());
  }

  @Test
  public void testFindEventsInRangePartialOverlap() {
    tree.insertSingleEvent(createEvent("Event",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T12:00:00-05:00[America/New_York]"));

    List<CalendarEvent> found = tree.findEventsInRange(
        ZonedDateTime.parse("2025-11-04T11:00:00-05:00[America/New_York]"),
        ZonedDateTime.parse("2025-11-04T13:00:00-05:00[America/New_York]"));

    assertEquals(1, found.size());
  }

  @Test
  public void testBalanceNull() {
    assertNull(tree.balance(null));
  }

  @Test
  public void testComplexInsertionAndDeletion() {
    List<CalendarEvent> events = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      CalendarEvent e = createEvent("Event " + i,
          "2025-11-" + String.format("%02d", i + 3) + "T10:00:00-05:00[America/New_York]",
          "2025-11-" + String.format("%02d", i + 3) + "T11:00:00-05:00[America/New_York]");
      events.add(e);
      tree.insertSingleEvent(e);
    }

    assertEquals(10, tree.getAllEvents().size());

    for (int i = 0; i < 5; i++) {
      tree.remove(events.get(i));
    }

    assertEquals(5, tree.getAllEvents().size());
  }

  @Test
  public void testIsBusyWithMultipleOverlappingEvents() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T09:00:00-05:00[America/New_York]",
        "2025-11-04T12:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-04T11:00:00-05:00[America/New_York]",
        "2025-11-04T14:00:00-05:00[America/New_York]"));

    assertTrue(tree.isBusy(
        ZonedDateTime.parse("2025-11-04T10:00:00-05:00[America/New_York]")));

    assertTrue(tree.isBusy(
        ZonedDateTime.parse("2025-11-04T11:30:00-05:00[America/New_York]")));

    assertTrue(tree.isBusy(
        ZonedDateTime.parse("2025-11-04T13:00:00-05:00[America/New_York]")));
  }

  @Test
  public void testFindEventNavigatesLeftSubtree() {
    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    CalendarEvent leftEvent = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(leftEvent);

    CalendarEvent found = tree.findEvent(leftEvent);
    assertNotNull(found);
    assertEquals("Event 1", found.getSubject());
  }

  @Test
  public void testFindEventNavigatesRightSubtree() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    CalendarEvent rightEvent = createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(rightEvent);

    CalendarEvent found = tree.findEvent(rightEvent);
    assertNotNull(found);
    assertEquals("Event 2", found.getSubject());
  }

  @Test
  public void testInsertManyEventsPreservesBalance() {
    for (int i = 0; i < 100; i++) {
      tree.insertSingleEvent(createEvent("Event " + i,
          "2025-11-" + String.format("%02d", (i % 28) + 1) + "T10:00:00-05:00[America/New_York]",
          "2025-11-" + String.format("%02d", (i % 28) + 1) + "T11:00:00-05:00[America/New_York]"));
    }

    assertEquals(100, tree.getAllEvents().size());
  }

  @Test
  public void testAddEventsForLeftRightRotation() {
    CalendarEvent event1 = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-04T11:00:00-05:00[America/New_York]",
        "2025-11-04T12:00:00-05:00[America/New_York]");


    CalendarEvent event3 = createEvent("Event 3",
        "2025-11-04T09:30:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");

    CalendarEvent event4 = createEvent("Event 4",
        "2025-11-04T09:45:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");

    CalendarEvent event5 = createEvent("Event 5",
        "2025-11-04T09:50:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event1);
    tree.insertSingleEvent(event2);
    tree.insertSingleEvent(event3);
    tree.insertSingleEvent(event4);
    tree.insertSingleEvent(event5);
    List<CalendarEvent> events = new ArrayList<>();
    events.add(event3);
    events.add(event4);
    events.add(event5);
    events.add(event1);
    events.add(event2);

    CalendarEvent found = tree.findEvent(event5);
    assertNotNull(found);
    assertEquals("Event 5", found.getSubject());
    assertEquals(events, tree.getAllEvents());
  }

  @Test
  public void testAddEventsForRightRightRotation() {
    CalendarEvent event1 = createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]");

    CalendarEvent event2 = createEvent("Event 2",
        "2025-11-04T09:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");


    CalendarEvent event3 = createEvent("Event 3",
        "2025-11-04T08:30:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");

    tree.insertSingleEvent(event1);
    tree.insertSingleEvent(event2);
    tree.insertSingleEvent(event3);
    List<CalendarEvent> events = new ArrayList<>();
    events.add(event3);
    events.add(event2);
    events.add(event1);
    CalendarEvent found = tree.findEvent(event3);
    assertNotNull(found);
    assertEquals("Event 3", found.getSubject());
    assertEquals(events, tree.getAllEvents());
  }

  @Test
  public void testBalancingLeftRightCase() {
    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(3, events.size());
    assertEquals("Event 1", events.get(0).getSubject());
    assertEquals("Event 2", events.get(1).getSubject());
    assertEquals("Event 3", events.get(2).getSubject());
  }

  @Test
  public void testBalancingRightLeftCase() {
    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(3, events.size());
    assertEquals("Event 1", events.get(0).getSubject());
    assertEquals("Event 2", events.get(1).getSubject());
    assertEquals("Event 3", events.get(2).getSubject());
  }

  @Test
  public void testLeftRightRotationThroughDeletion() {
    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 4",
        "2025-11-07T10:00:00-05:00[America/New_York]",
        "2025-11-07T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 1.5",
        "2025-11-04T14:00:00-05:00[America/New_York]",
        "2025-11-04T15:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    tree.remove(createEvent("Event 4",
        "2025-11-07T10:00:00-05:00[America/New_York]",
        "2025-11-07T11:00:00-05:00[America/New_York]"));

    assertEquals(4, tree.getAllEvents().size());
  }

  @Test
  public void testRightLeftRotationThroughDeletion() {
    tree.insertSingleEvent(createEvent("Event 2",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 4",
        "2025-11-07T10:00:00-05:00[America/New_York]",
        "2025-11-07T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 0",
        "2025-11-03T10:00:00-05:00[America/New_York]",
        "2025-11-03T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 3",
        "2025-11-06T10:00:00-05:00[America/New_York]",
        "2025-11-06T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 1",
        "2025-11-04T10:00:00-05:00[America/New_York]",
        "2025-11-04T11:00:00-05:00[America/New_York]"));

    tree.remove(createEvent("Event 0",
        "2025-11-03T10:00:00-05:00[America/New_York]",
        "2025-11-03T11:00:00-05:00[America/New_York]"));

    assertEquals(4, tree.getAllEvents().size());
  }

  @Test
  public void testLeftRightRotation() {
    tree.insertSingleEvent(createEvent("Event 30",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 10",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 20",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    assertEquals(3, tree.getAllEvents().size());
  }

  @Test
  public void testRightLeftRotation() {
    tree.insertSingleEvent(createEvent("Event 10",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 30",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event 20",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    assertEquals(3, tree.getAllEvents().size());
  }

  @Test
  public void testLeftRightRotationComplex() {
    tree.insertSingleEvent(createEvent("E50",
        "2025-11-25T10:00:00-05:00[America/New_York]",
        "2025-11-25T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E20",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E60",
        "2025-11-28T10:00:00-05:00[America/New_York]",
        "2025-11-28T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E10",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E15",
        "2025-11-12T10:00:00-05:00[America/New_York]",
        "2025-11-12T11:00:00-05:00[America/New_York]"));

    assertEquals(5, tree.getAllEvents().size());
  }

  @Test
  public void testRightLeftRotationComplex() {
    tree.insertSingleEvent(createEvent("E20",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E10",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E50",
        "2025-11-25T10:00:00-05:00[America/New_York]",
        "2025-11-25T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E60",
        "2025-11-28T10:00:00-05:00[America/New_York]",
        "2025-11-28T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E55",
        "2025-11-27T10:00:00-05:00[America/New_York]",
        "2025-11-27T11:00:00-05:00[America/New_York]"));

    assertEquals(5, tree.getAllEvents().size());
  }

  @Test
  public void testIsBusyDoesNotSearchLeftWhenMaxEndTimeBeforeQueryTime() {
    tree.insertSingleEvent(createEvent(
        "Early Morning Meeting",
        "2025-01-15T08:00:00-05:00[America/New_York]",
        "2025-01-15T09:00:00-05:00[America/New_York]"
    ));

    tree.insertSingleEvent(createEvent(
        "Mid Morning Meeting",
        "2025-01-15T10:00:00-05:00[America/New_York]",
        "2025-01-15T11:00:00-05:00[America/New_York]"
    ));

    tree.insertSingleEvent(createEvent(
        "Late Morning Meeting",
        "2025-01-15T11:30:00-05:00[America/New_York]",
        "2025-01-15T12:30:00-05:00[America/New_York]"
    ));

    ZonedDateTime queryTime = ZonedDateTime.parse("2025-01-15T13:00:00-05:00[America/New_York]");

    assertFalse(tree.isBusy(queryTime));
  }

  @Test
  public void testIsBusySearchesLeftSubtreeWhenMaxEndTimeAfterQueryTime() {
    tree.insertSingleEvent(createEvent(
        "Morning Event",
        "2025-01-15T09:00:00-05:00[America/New_York]",
        "2025-01-15T10:00:00-05:00[America/New_York]"
    ));

    tree.insertSingleEvent(createEvent(
        "Noon Event",
        "2025-01-15T12:00:00-05:00[America/New_York]",
        "2025-01-15T13:00:00-05:00[America/New_York]"
    ));

    tree.insertSingleEvent(createEvent(
        "Afternoon Event",
        "2025-01-15T14:00:00-05:00[America/New_York]",
        "2025-01-15T15:00:00-05:00[America/New_York]"
    ));

    ZonedDateTime queryTime = ZonedDateTime.parse("2025-01-15T09:30:00-05:00[America/New_York]");

    assertTrue(tree.isBusy(queryTime));
  }

  @Test
  public void testIsBusySkipsLeftSubtreeWhenAllEventsEndBeforeQueryTime() {
    tree.insertSingleEvent(createEvent(
        "Event A",
        "2025-01-15T08:00:00-05:00[America/New_York]",
        "2025-01-15T09:00:00-05:00[America/New_York]"
    ));

    tree.insertSingleEvent(createEvent(
        "Event B",
        "2025-01-15T09:30:00-05:00[America/New_York]",
        "2025-01-15T10:30:00-05:00[America/New_York]"
    ));

    tree.insertSingleEvent(createEvent(
        "Event C",
        "2025-01-15T11:00:00-05:00[America/New_York]",
        "2025-01-15T12:00:00-05:00[America/New_York]"
    ));

    ZonedDateTime queryTime = ZonedDateTime.parse("2025-01-15T14:00:00-05:00[America/New_York]");

    assertFalse(tree.isBusy(queryTime));
  }

  @Test
  public void testPublicMethodsHandleNullGracefully() {
    tree.updateMaxEnd(null);
    tree.updateHeight(null);
    assertNull(tree.balance(null));
    assertEquals(0, tree.getBalance(null));
    assertEquals(0, tree.height(null));
  }

  @Test
  public void testLeftRightRotationMaintainsOrder() {
    tree.insertSingleEvent(createEvent("Event C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(3, events.size());
    assertEquals("Event A", events.get(0).getSubject());
    assertEquals("Event B", events.get(1).getSubject());
    assertEquals("Event C", events.get(2).getSubject());
  }

  @Test
  public void testRightLeftRotationMaintainsOrder() {
    tree.insertSingleEvent(createEvent("Event A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(3, events.size());
    assertEquals("Event A", events.get(0).getSubject());
    assertEquals("Event B", events.get(1).getSubject());
    assertEquals("Event C", events.get(2).getSubject());
  }

  @Test
  public void testLeftLeftRotationMaintainsOrder() {
    tree.insertSingleEvent(createEvent("Event C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(3, events.size());
    assertEquals("Event A", events.get(0).getSubject());
    assertEquals("Event B", events.get(1).getSubject());
    assertEquals("Event C", events.get(2).getSubject());
  }

  @Test
  public void testRightRightRotationMaintainsOrder() {
    tree.insertSingleEvent(createEvent("Event A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Event C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(3, events.size());
    assertEquals("Event A", events.get(0).getSubject());
    assertEquals("Event B", events.get(1).getSubject());
    assertEquals("Event C", events.get(2).getSubject());
  }

  @Test
  public void testMultipleRotationsMaintainOrder() {
    int[] days = {15, 10, 25, 5, 12, 20, 30, 3, 7, 11, 14};

    for (int day : days) {
      tree.insertSingleEvent(createEvent("Event " + day,
          "2025-11-" + String.format("%02d", day) + "T10:00:00-05:00[America/New_York]",
          "2025-11-" + String.format("%02d", day) + "T11:00:00-05:00[America/New_York]"));
    }

    List<CalendarEvent> events = tree.getAllEvents();
    assertEquals(days.length, events.size());

    for (int i = 1; i < events.size(); i++) {
      assertTrue(events.get(i - 1).getStartDateTime()
          .isBefore(events.get(i).getStartDateTime()));
    }
  }

  @Test
  public void testDeletionsWithRotationsMaintainOrder() {
    List<CalendarEvent> events = new ArrayList<>();
    for (int i = 1; i <= 15; i++) {
      CalendarEvent event = createEvent("Event " + i,
          "2025-11-" + String.format("%02d", i) + "T10:00:00-05:00[America/New_York]",
          "2025-11-" + String.format("%02d", i) + "T11:00:00-05:00[America/New_York]");
      events.add(event);
      tree.insertSingleEvent(event);
    }

    tree.remove(events.get(7));
    tree.remove(events.get(3));
    tree.remove(events.get(11));

    List<CalendarEvent> remaining = tree.getAllEvents();
    assertEquals(12, remaining.size());

    for (int i = 1; i < remaining.size(); i++) {
      assertTrue(remaining.get(i - 1).getStartDateTime()
          .isBefore(remaining.get(i).getStartDateTime()));
    }
  }

  @Test
  public void testRotateLeftUpdatesHeights() {
    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    int leftHeight = root.left.height;
    int rightHeight = root.right.height;
    int expectedHeight = 1 + Math.max(leftHeight, rightHeight);

    assertEquals(expectedHeight, root.height);

    if (root.left != null) {
      int leftLeftHeight = root.left.left != null ? root.left.left.height : 0;
      int leftRightHeight = root.left.right != null ? root.left.right.height : 0;
      assertEquals(1 + Math.max(leftLeftHeight, leftRightHeight), root.left.height);
    }

    if (root.right != null) {
      int rightLeftHeight = root.right.left != null ? root.right.left.height : 0;
      int rightRightHeight = root.right.right != null ? root.right.right.height : 0;
      assertEquals(1 + Math.max(rightLeftHeight, rightRightHeight), root.right.height);
    }
  }

  @Test
  public void testRotateRightUpdatesHeights() {
    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    int leftHeight = root.left != null ? root.left.height : 0;
    int rightHeight = root.right != null ? root.right.height : 0;
    int expectedHeight = 1 + Math.max(leftHeight, rightHeight);

    assertEquals(expectedHeight, root.height);

    if (root.left != null) {
      int leftLeftHeight = root.left.left != null ? root.left.left.height : 0;
      int leftRightHeight = root.left.right != null ? root.left.right.height : 0;
      assertEquals(1 + Math.max(leftLeftHeight, leftRightHeight), root.left.height);
    }

    if (root.right != null) {
      int rightLeftHeight = root.right.left != null ? root.right.left.height : 0;
      int rightRightHeight = root.right.right != null ? root.right.right.height : 0;
      assertEquals(1 + Math.max(rightLeftHeight, rightRightHeight), root.right.height);
    }
  }

  @Test
  public void testRotateLeftUpdatesMaxEndTime() {
    tree.insertSingleEvent(createEvent("Short",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Medium",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T15:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Long",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T22:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    ZonedDateTime rootMaxEnd = root.event.getEndDateTime();
    if (root.left != null && root.left.maxEndTime.isAfter(rootMaxEnd)) {
      rootMaxEnd = root.left.maxEndTime;
    }
    if (root.right != null && root.right.maxEndTime.isAfter(rootMaxEnd)) {
      rootMaxEnd = root.right.maxEndTime;
    }

    assertEquals(rootMaxEnd, root.maxEndTime);
  }

  @Test
  public void testRotateRightUpdatesMaxEndTime() {
    tree.insertSingleEvent(createEvent("Long",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T22:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Medium",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T15:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("Short",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    ZonedDateTime rootMaxEnd = root.event.getEndDateTime();
    if (root.left != null && root.left.maxEndTime.isAfter(rootMaxEnd)) {
      rootMaxEnd = root.left.maxEndTime;
    }
    if (root.right != null && root.right.maxEndTime.isAfter(rootMaxEnd)) {
      rootMaxEnd = root.right.maxEndTime;
    }

    assertEquals(rootMaxEnd, root.maxEndTime);
  }

  @Test
  public void testLeftRightRotationUpdatesAllProperties() {
    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T20:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T15:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();
    assertEquals("B", root.event.getSubject());
    assertEquals(2, root.height);
    assertEquals(20, root.maxEndTime.getHour());
    assertEquals(30, root.maxEndTime.getDayOfMonth());
  }

  @Test
  public void testRightLeftRotationUpdatesAllProperties() {
    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T20:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T15:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();
    assertEquals("B", root.event.getSubject());
    assertEquals(2, root.height);
    assertEquals(20, root.maxEndTime.getHour());
    assertEquals(30, root.maxEndTime.getDayOfMonth());
  }

  @Test
  public void testRotateLeftUpdatesHeightsCorrectly() {
    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("D",
        "2025-11-25T10:00:00-05:00[America/New_York]",
        "2025-11-25T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();
    assertEquals("B", root.event.getSubject());
    assertEquals(3, root.height);
    assertEquals(1, root.left.height);
    assertEquals(2, root.right.height);
    assertEquals(1, root.right.left.height);
    assertEquals(1, root.right.right.height);
  }

  @Test
  public void testRotateRightUpdatesHeightsCorrectly() {
    tree.insertSingleEvent(createEvent("E",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("D",
        "2025-11-25T10:00:00-05:00[America/New_York]",
        "2025-11-25T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();
    assertEquals("D", root.event.getSubject());
    assertEquals(3, root.height);
    assertEquals(2, root.left.height);
    assertEquals(1, root.right.height);
    assertEquals(1, root.left.left.height);
    assertEquals(1, root.left.right.height);
  }

  @Test
  public void testRotateLeftUpdatesMaxEndTimeCorrectly() {
    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T12:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T18:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T14:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("D",
        "2025-11-25T10:00:00-05:00[America/New_York]",
        "2025-11-25T16:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T22:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();
    assertEquals(22, root.maxEndTime.getHour());
    assertEquals(12, root.left.maxEndTime.getHour());
    assertEquals(22, root.right.maxEndTime.getHour());
    assertEquals(14, root.right.left.maxEndTime.getHour());
    assertEquals(22, root.right.right.maxEndTime.getHour());
  }

  @Test
  public void testRotateRightUpdatesMaxEndTimeCorrectly() {
    tree.insertSingleEvent(createEvent("E",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T22:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("D",
        "2025-11-25T10:00:00-05:00[America/New_York]",
        "2025-11-25T16:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T14:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T18:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T12:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();
    assertEquals(22, root.maxEndTime.getHour());
    assertEquals(14, root.left.maxEndTime.getHour());
    assertEquals(22, root.right.maxEndTime.getHour());
    assertEquals(12, root.left.left.maxEndTime.getHour());
    assertEquals(14, root.left.right.maxEndTime.getHour());
  }

  @Test
  public void testLeftRightRotationUpdatesHeightsAndMaxEnd() {
    tree.insertSingleEvent(createEvent("E",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T20:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T12:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("D",
        "2025-11-25T10:00:00-05:00[America/New_York]",
        "2025-11-25T16:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T14:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();
    assertEquals("B", root.event.getSubject());
    assertEquals(3, root.height);
    assertEquals(1, root.left.height);
    assertEquals(2, root.right.height);

    assertEquals(11, root.maxEndTime.getHour());
    assertEquals(12, root.left.maxEndTime.getHour());
    assertEquals(11, root.right.maxEndTime.getHour());
    assertEquals(14, root.right.left.maxEndTime.getHour());
    assertEquals(11, root.right.right.maxEndTime.getHour());
  }

  @Test
  public void testRightLeftRotationUpdatesHeightsAndMaxEnd() {
    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("D",
        "2025-11-25T10:00:00-05:00[America/New_York]",
        "2025-11-25T20:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T12:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T16:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T14:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();
    assertEquals("D", root.event.getSubject());
    assertEquals(3, root.height);
    assertEquals(2, root.left.height);
    assertEquals(1, root.right.height);

    assertEquals(12, root.maxEndTime.getHour());
    assertEquals(14, root.left.maxEndTime.getHour());
    assertEquals(12, root.right.maxEndTime.getHour());
  }

  @Test
  public void testRotateLeftUpdatesYnodeHeightCorrectly() {
    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    assertEquals("B", root.event.getSubject());
    assertEquals(2, root.height);
    assertEquals(1, root.left.height);
    assertEquals(1, root.right.height);
  }

  @Test
  public void testRotateLeftUpdatesYnodeMaxEndCorrectly() {
    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T15:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T12:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T20:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    assertEquals("B", root.event.getSubject());
    assertEquals(20, root.maxEndTime.getHour());
    assertEquals(30, root.maxEndTime.getDayOfMonth());
    assertEquals(15, root.left.maxEndTime.getHour());
    assertEquals(20, root.right.maxEndTime.getHour());
  }

  @Test
  public void testRotateRightUpdatesXnodeHeightCorrectly() {
    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    assertEquals("B", root.event.getSubject());
    assertEquals(2, root.height);
    assertEquals(1, root.left.height);
    assertEquals(1, root.right.height);
  }

  @Test
  public void testRotateRightUpdatesXnodeMaxEndCorrectly() {
    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T20:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T12:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T15:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    assertEquals("B", root.event.getSubject());
    assertEquals(20, root.maxEndTime.getHour());
    assertEquals(30, root.maxEndTime.getDayOfMonth());
    assertEquals(15, root.left.maxEndTime.getHour());
    assertEquals(20, root.right.maxEndTime.getHour());
  }

  @Test
  public void testRotateLeftUpdatesNewRootHeight() {
    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    assertEquals("B", root.event.getSubject());
    assertEquals(2, root.height);
    assertEquals(1, root.left.height);
    assertEquals(1, root.right.height);
  }

  @Test
  public void testRotateRightUpdatesOldRootHeight() {
    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    assertEquals("B", root.event.getSubject());
    assertEquals(2, root.height);
    assertEquals(1, root.left.height);
    assertEquals(1, root.right.height);
  }

  @Test
  public void testInsertIntoSubtreeLeftRightRotation() {
    CalendarEvent eventC = createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]");
    CalendarEvent eventA = createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    IntervalTreeNode nodeC = new IntervalTreeNode(eventC);
    IntervalTreeNode nodeA = new IntervalTreeNode(eventA);

    nodeC.left = nodeA;
    nodeC.height = 2;
    nodeA.height = 1;

    CalendarEvent newEvent = createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T18:00:00-05:00[America/New_York]");

    IntervalTreeNode balancedRoot = tree.insertIntoSubtree(nodeC, newEvent);

    assertEquals("B", balancedRoot.event.getSubject());
    assertEquals(2, balancedRoot.height);
    assertEquals("A", balancedRoot.left.event.getSubject());
    assertEquals(1, balancedRoot.left.height);
    assertEquals("C", balancedRoot.right.event.getSubject());
    assertEquals(1, balancedRoot.right.height);
    assertEquals(11, balancedRoot.maxEndTime.getHour());
  }

  @Test
  public void testInsertIntoSubtreeRightLeftRotation() {
    CalendarEvent eventA = createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");
    CalendarEvent eventC = createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]");

    IntervalTreeNode nodeA = new IntervalTreeNode(eventA);
    IntervalTreeNode nodeC = new IntervalTreeNode(eventC);

    nodeA.right = nodeC;
    nodeA.height = 2;
    nodeC.height = 1;

    CalendarEvent newEvent = createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T18:00:00-05:00[America/New_York]");

    IntervalTreeNode balancedRoot = tree.insertIntoSubtree(nodeA, newEvent);

    assertEquals("B", balancedRoot.event.getSubject());
    assertEquals(2, balancedRoot.height);
    assertEquals("A", balancedRoot.left.event.getSubject());
    assertEquals(1, balancedRoot.left.height);
    assertEquals("C", balancedRoot.right.event.getSubject());
    assertEquals(1, balancedRoot.right.height);
    assertEquals(11, balancedRoot.maxEndTime.getHour());
  }

  @Test
  public void testInsertIntoSubtreeWithComplexMaxEndTimes() {
    CalendarEvent eventB = createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T23:00:00-05:00[America/New_York]");
    CalendarEvent eventA = createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");
    CalendarEvent eventC = createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]");

    IntervalTreeNode root = new IntervalTreeNode(eventB);
    IntervalTreeNode nodeA = new IntervalTreeNode(eventA);
    IntervalTreeNode nodeC = new IntervalTreeNode(eventC);

    root.left = nodeA;
    root.right = nodeC;
    root.height = 2;
    nodeA.height = 1;
    nodeC.height = 1;
    root.maxEndTime = eventB.getEndDateTime();
    nodeA.maxEndTime = eventA.getEndDateTime();
    nodeC.maxEndTime = eventC.getEndDateTime();

    CalendarEvent newEvent = createEvent("D",
        "2025-11-25T10:00:00-05:00[America/New_York]",
        "2025-11-25T20:00:00-05:00[America/New_York]");

    IntervalTreeNode balancedRoot = tree.insertIntoSubtree(root, newEvent);

    assertEquals("B", balancedRoot.event.getSubject());
    assertEquals(3, balancedRoot.height);
    assertEquals(20, balancedRoot.maxEndTime.getHour());
    assertEquals(25, balancedRoot.maxEndTime.getDayOfMonth());
    assertEquals(2, balancedRoot.right.height);
    assertEquals(20, balancedRoot.right.maxEndTime.getHour());
  }

  @Test
  public void testInsertIntoSubtreeWithNullNodeCreatesNewNode() {
    CalendarEvent event = createEvent("New Event",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T11:00:00-05:00[America/New_York]");

    IntervalTreeNode newNode = tree.insertIntoSubtree(null, event);

    assertNotNull(newNode);
    assertEquals("New Event", newNode.event.getSubject());
    assertEquals(1, newNode.height);
    assertNull(newNode.left);
    assertNull(newNode.right);
  }

  @Test
  public void testInsertIntoSubtreeGoesLeft() {
    CalendarEvent rootEvent = createEvent("Root",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]");

    IntervalTreeNode root = new IntervalTreeNode(rootEvent);

    CalendarEvent earlierEvent = createEvent("Earlier",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    IntervalTreeNode result = tree.insertIntoSubtree(root, earlierEvent);

    assertEquals("Root", result.event.getSubject());
    assertNotNull(result.left);
    assertEquals("Earlier", result.left.event.getSubject());
    assertNull(result.right);
  }

  @Test
  public void testInsertIntoSubtreeGoesRight() {
    CalendarEvent rootEvent = createEvent("Root",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    IntervalTreeNode root = new IntervalTreeNode(rootEvent);

    CalendarEvent laterEvent = createEvent("Later",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]");

    IntervalTreeNode result = tree.insertIntoSubtree(root, laterEvent);

    assertEquals("Root", result.event.getSubject());
    assertNull(result.left);
    assertNotNull(result.right);
    assertEquals("Later", result.right.event.getSubject());
  }

  @Test
  public void testRotateLeftUpdatesNewRootMaxEnd() {
    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T15:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T12:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T20:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    assertEquals("B", root.event.getSubject());
    assertEquals(ZonedDateTime.parse("2025-11-30T20:00:00-05:00[America/New_York]"),
        root.maxEndTime);
    assertEquals(ZonedDateTime.parse("2025-11-10T15:00:00-05:00[America/New_York]"),
        root.left.maxEndTime);
    assertEquals(ZonedDateTime.parse("2025-11-30T20:00:00-05:00[America/New_York]"),
        root.right.maxEndTime);
  }

  @Test
  public void testRotateRightUpdatesOldRootMaxEnd() {
    tree.insertSingleEvent(createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T20:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T12:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T15:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();

    assertEquals("B", root.event.getSubject());
    assertEquals(ZonedDateTime.parse("2025-11-30T20:00:00-05:00[America/New_York]"),
        root.maxEndTime);
    assertEquals(ZonedDateTime.parse("2025-11-10T15:00:00-05:00[America/New_York]"),
        root.left.maxEndTime);
    assertEquals(ZonedDateTime.parse("2025-11-30T20:00:00-05:00[America/New_York]"),
        root.right.maxEndTime);
  }

  @Test
  public void testInsertIntoSubtreeUpdatesMaxEndTimes() {
    CalendarEvent eventA = createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T15:00:00-05:00[America/New_York]");
    CalendarEvent eventB = createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T12:00:00-05:00[America/New_York]");

    IntervalTreeNode nodeA = new IntervalTreeNode(eventA);
    IntervalTreeNode nodeB = new IntervalTreeNode(eventB);

    nodeA.right = nodeB;
    nodeA.height = 2;
    nodeB.height = 1;
    nodeA.maxEndTime = eventB.getEndDateTime();
    nodeB.maxEndTime = eventB.getEndDateTime();

    CalendarEvent newEvent = createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T22:00:00-05:00[America/New_York]");

    IntervalTreeNode balancedRoot = tree.insertIntoSubtree(nodeA, newEvent);

    assertEquals("B", balancedRoot.event.getSubject());
    assertEquals(ZonedDateTime.parse("2025-11-20T22:00:00-05:00[America/New_York]"),
        balancedRoot.maxEndTime);
    assertEquals(ZonedDateTime.parse("2025-11-10T15:00:00-05:00[America/New_York]"),
        balancedRoot.left.maxEndTime);
    assertEquals(ZonedDateTime.parse("2025-11-20T22:00:00-05:00[America/New_York]"),
        balancedRoot.right.maxEndTime);
  }

  @Test
  public void testComplexTreeUpdatesAllNodesCorrectly() {
    tree.insertSingleEvent(createEvent("D",
        "2025-11-13T10:00:00-05:00[America/New_York]",
        "2025-11-13T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("B",
        "2025-11-11T10:00:00-05:00[America/New_York]",
        "2025-11-11T18:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("F",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("C",
        "2025-11-12T10:00:00-05:00[America/New_York]",
        "2025-11-12T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("E",
        "2025-11-14T10:00:00-05:00[America/New_York]",
        "2025-11-14T11:00:00-05:00[America/New_York]"));

    tree.insertSingleEvent(createEvent("G",
        "2025-11-16T10:00:00-05:00[America/New_York]",
        "2025-11-16T22:00:00-05:00[America/New_York]"));

    IntervalTreeNode root = tree.getRoot();
    assertEquals("D", root.event.getSubject());
    assertEquals(3, root.height);

    assertEquals("B", root.left.event.getSubject());
    assertEquals(2, root.left.height);
    assertEquals(ZonedDateTime.parse("2025-11-12T11:00:00-05:00[America/New_York]"),
        root.left.maxEndTime);

    assertEquals("F", root.right.event.getSubject());
    assertEquals(2, root.right.height);
    assertEquals(ZonedDateTime.parse("2025-11-16T22:00:00-05:00[America/New_York]"),
        root.right.maxEndTime);

    assertEquals(ZonedDateTime.parse("2025-11-16T22:00:00-05:00[America/New_York]"),
        root.maxEndTime);
  }

  @Test
  public void testInsertIntoSubtreeBalancesLeftHeavyTree() {
    CalendarEvent eventB = createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T11:00:00-05:00[America/New_York]");
    CalendarEvent eventA = createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    IntervalTreeNode root = new IntervalTreeNode(eventB);
    IntervalTreeNode nodeA = new IntervalTreeNode(eventA);

    root.left = nodeA;
    root.height = 2;
    nodeA.height = 1;
    root.maxEndTime = eventB.getEndDateTime();
    nodeA.maxEndTime = eventA.getEndDateTime();

    CalendarEvent newEvent = createEvent("A0",
        "2025-11-05T10:00:00-05:00[America/New_York]",
        "2025-11-05T11:00:00-05:00[America/New_York]");

    IntervalTreeNode balancedRoot = tree.insertIntoSubtree(root, newEvent);

    assertEquals("A", balancedRoot.event.getSubject());
    assertEquals(2, balancedRoot.height);
    assertEquals("A0", balancedRoot.left.event.getSubject());
    assertEquals(1, balancedRoot.left.height);
    assertEquals("B", balancedRoot.right.event.getSubject());
    assertEquals(1, balancedRoot.right.height);
  }

  @Test
  public void testInsertIntoSubtreeBalancesRightHeavyTree() {
    CalendarEvent eventA = createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");
    CalendarEvent eventB = createEvent("B",
        "2025-11-15T10:00:00-05:00[America/New_York]",
        "2025-11-15T11:00:00-05:00[America/New_York]");

    IntervalTreeNode root = new IntervalTreeNode(eventA);
    IntervalTreeNode nodeB = new IntervalTreeNode(eventB);

    root.right = nodeB;
    root.height = 2;
    nodeB.height = 1;
    root.maxEndTime = eventB.getEndDateTime();
    nodeB.maxEndTime = eventB.getEndDateTime();

    CalendarEvent newEvent = createEvent("C",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T11:00:00-05:00[America/New_York]");

    IntervalTreeNode balancedRoot = tree.insertIntoSubtree(root, newEvent);

    assertEquals("B", balancedRoot.event.getSubject());
    assertEquals(2, balancedRoot.height);
    assertEquals("A", balancedRoot.left.event.getSubject());
    assertEquals(1, balancedRoot.left.height);
    assertEquals("C", balancedRoot.right.event.getSubject());
    assertEquals(1, balancedRoot.right.height);
  }

  @Test
  public void testInsertIntoSubtreeTriggersLeftRightRotation() {
    CalendarEvent eventC = createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]");
    CalendarEvent eventA = createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");

    IntervalTreeNode root = new IntervalTreeNode(eventC);
    IntervalTreeNode nodeA = new IntervalTreeNode(eventA);

    root.left = nodeA;
    root.height = 2;
    nodeA.height = 1;
    root.maxEndTime = eventC.getEndDateTime();
    nodeA.maxEndTime = eventA.getEndDateTime();

    CalendarEvent eventB = createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T15:00:00-05:00[America/New_York]");

    IntervalTreeNode balancedRoot = tree.insertIntoSubtree(root, eventB);

    assertEquals("B", balancedRoot.event.getSubject());
    assertEquals(2, balancedRoot.height);
    assertEquals("A", balancedRoot.left.event.getSubject());
    assertEquals("C", balancedRoot.right.event.getSubject());
    assertEquals(ZonedDateTime.parse("2025-11-30T11:00:00-05:00[America/New_York]"),
        balancedRoot.maxEndTime);
    assertEquals(ZonedDateTime.parse("2025-11-10T11:00:00-05:00[America/New_York]"),
        balancedRoot.left.maxEndTime);
    assertEquals(ZonedDateTime.parse("2025-11-30T11:00:00-05:00[America/New_York]"),
        balancedRoot.right.maxEndTime);
  }

  @Test
  public void testInsertIntoSubtreeTriggersRightLeftRotation() {
    CalendarEvent eventA = createEvent("A",
        "2025-11-10T10:00:00-05:00[America/New_York]",
        "2025-11-10T11:00:00-05:00[America/New_York]");
    CalendarEvent eventC = createEvent("C",
        "2025-11-30T10:00:00-05:00[America/New_York]",
        "2025-11-30T11:00:00-05:00[America/New_York]");

    IntervalTreeNode root = new IntervalTreeNode(eventA);
    IntervalTreeNode nodeC = new IntervalTreeNode(eventC);

    root.right = nodeC;
    root.height = 2;
    nodeC.height = 1;
    root.maxEndTime = eventC.getEndDateTime();
    nodeC.maxEndTime = eventC.getEndDateTime();

    CalendarEvent eventB = createEvent("B",
        "2025-11-20T10:00:00-05:00[America/New_York]",
        "2025-11-20T15:00:00-05:00[America/New_York]");

    IntervalTreeNode balancedRoot = tree.insertIntoSubtree(root, eventB);

    assertEquals("B", balancedRoot.event.getSubject());
    assertEquals(2, balancedRoot.height);
    assertEquals("A", balancedRoot.left.event.getSubject());
    assertEquals("C", balancedRoot.right.event.getSubject());
    assertEquals(ZonedDateTime.parse("2025-11-30T11:00:00-05:00[America/New_York]"),
        balancedRoot.maxEndTime);
  }

  @Test
  public void testBalanceLeftLeftRotationWhenLeftBalanceZero() {
    CalendarEvent a = createEvent("a", "2024-05-01T10:00:00Z", "2024-05-01T11:00:00Z");
    CalendarEvent b = createEvent("b", "2024-05-01T09:00:00Z", "2024-05-01T10:00:00Z");
    CalendarEvent c = createEvent("c", "2024-05-01T08:00:00Z", "2024-05-01T09:00:00Z");

    IntervalTreeNode root = new IntervalTreeNode(a);
    root.left = new IntervalTreeNode(b);
    root.left.left = new IntervalTreeNode(c);
    root.height = 3;
    root.left.height = 2;
    root.left.left.height = 1;
    IntervalTree tree = new IntervalTree();
    IntervalTreeNode balanced = tree.balance(root);
    assertEquals("b", balanced.event.getSubject());
  }

  @Test
  public void testBalanceLeftHeavyChildBalanceZeroSingleRotation() {
    CalendarEvent a = createEvent("A", "2024-05-01T10:00:00Z", "2024-05-01T11:00:00Z");
    CalendarEvent b = createEvent("B", "2024-05-01T09:00:00Z", "2024-05-01T10:00:00Z");
    CalendarEvent c = createEvent("C", "2024-05-01T08:00:00Z", "2024-05-01T09:00:00Z");
    CalendarEvent d = createEvent("D", "2024-05-01T09:30:00Z", "2024-05-01T09:45:00Z");

    IntervalTreeNode nodeA = new IntervalTreeNode(a);
    IntervalTreeNode nodeB = new IntervalTreeNode(b);
    IntervalTreeNode nodeC = new IntervalTreeNode(c);
    IntervalTreeNode nodeD = new IntervalTreeNode(d);

    nodeA.left = nodeB;
    nodeB.left = nodeC;
    nodeB.right = nodeD;

    IntervalTree tree = new IntervalTree();
    tree.updateHeight(nodeC);
    tree.updateHeight(nodeD);
    tree.updateHeight(nodeB);
    tree.updateHeight(nodeA);

    IntervalTreeNode newRoot = tree.balance(nodeA);

    assertEquals("B", newRoot.event.getSubject());
    assertEquals("C", newRoot.left.event.getSubject());
    assertEquals("A", newRoot.right.event.getSubject());
    assertEquals("D", newRoot.right.left.event.getSubject());
  }

  @Test
  public void testBalanceRightHeavyChildBalanceZeroSingleRotation() {
    CalendarEvent a = createEvent("A", "2024-05-01T10:00:00Z",
        "2024-05-01T11:00:00Z");
    CalendarEvent b = createEvent("B", "2024-05-01T11:00:00Z",
        "2024-05-01T12:00:00Z");
    CalendarEvent c = createEvent("C", "2024-05-01T12:00:00Z",
        "2024-05-01T13:00:00Z");
    CalendarEvent d = createEvent("D", "2024-05-01T11:30:00Z",
        "2024-05-01T11:45:00Z");

    IntervalTreeNode nodeA = new IntervalTreeNode(a);
    IntervalTreeNode nodeB = new IntervalTreeNode(b);
    IntervalTreeNode nodeC = new IntervalTreeNode(c);
    IntervalTreeNode nodeD = new IntervalTreeNode(d);

    nodeA.right = nodeB;
    nodeB.left = nodeD;
    nodeB.right = nodeC;

    IntervalTree tree = new IntervalTree();
    tree.updateHeight(nodeD);
    tree.updateHeight(nodeC);
    tree.updateHeight(nodeB);
    tree.updateHeight(nodeA);

    IntervalTreeNode newRoot = tree.balance(nodeA);

    assertEquals("B", newRoot.event.getSubject());
    assertEquals("A", newRoot.left.event.getSubject());
    assertEquals("C", newRoot.right.event.getSubject());
    assertEquals("D", newRoot.left.right.event.getSubject());
  }

  @Test
  public void testRotateRightUpdatesHeightAndMaxEnd() {
    CalendarEvent a = createEvent("A", "2024-05-01T10:00:00Z",
        "2024-05-01T15:00:00Z");
    CalendarEvent b = createEvent("B", "2024-05-01T12:00:00Z",
        "2024-05-01T14:00:00Z");
    CalendarEvent c = createEvent("C", "2024-05-01T08:00:00Z",
        "2024-05-01T11:00:00Z");
    CalendarEvent d = createEvent("D", "2024-05-01T09:00:00Z",
        "2024-05-01T16:00:00Z");

    IntervalTreeNode x = new IntervalTreeNode(a);
    IntervalTreeNode y = new IntervalTreeNode(b);
    IntervalTreeNode z = new IntervalTreeNode(c);
    IntervalTreeNode w = new IntervalTreeNode(d);

    y.left = x;
    x.left = z;
    x.right = w;

    z.height = 1;
    w.height = 1;
    x.height = 2;
    y.height = 3;
    x.maxEndTime = w.getMaxEndTime().isAfter(a.getEndDateTime())
        ? w.getMaxEndTime() : a.getEndDateTime();
    y.maxEndTime = b.getEndDateTime();

    IntervalTree tree = new IntervalTree();
    IntervalTreeNode newRoot = tree.rotateRight(y);

    assertEquals(x, newRoot);
    assertEquals(3, newRoot.height);
    assertEquals(ZonedDateTime.parse("2024-05-01T16:00:00Z"), y.getMaxEndTime());
  }

  @Test
  public void testRotateLeftUpdatesHeightAndMaxEnd() {
    CalendarEvent a = createEvent("A", "2024-05-01T10:00:00Z",
        "2024-05-01T14:00:00Z");
    CalendarEvent b = createEvent("B", "2024-05-01T12:00:00Z",
        "2024-05-01T16:00:00Z");
    CalendarEvent c = createEvent("C", "2024-05-01T15:00:00Z",
        "2024-05-01T17:00:00Z");
    CalendarEvent d = createEvent("D", "2024-05-01T13:00:00Z",
        "2024-05-01T18:00:00Z");

    IntervalTreeNode x = new IntervalTreeNode(a);
    IntervalTreeNode y = new IntervalTreeNode(b);
    IntervalTreeNode z = new IntervalTreeNode(c);
    IntervalTreeNode w = new IntervalTreeNode(d);

    x.right = y;
    y.left = z;
    y.right = w;

    z.height = 1;
    w.height = 1;
    y.height = 2;
    x.height = 3;
    y.maxEndTime = w.getMaxEndTime();
    x.maxEndTime = a.getEndDateTime();

    IntervalTree tree = new IntervalTree();
    IntervalTreeNode newRoot = tree.rotateLeft(x);

    assertEquals(y, newRoot);
    assertEquals(3, newRoot.height);
    assertEquals(ZonedDateTime.parse("2024-05-01T18:00:00Z"), y.getMaxEndTime());
  }

  @Test
  public void testRotateLeftUpdatesMaxEndCorrectly() {
    CalendarEvent a = createEvent("A", "2024-05-01T10:00:00Z", "2024-05-01T14:00:00Z");
    CalendarEvent b = createEvent("B", "2024-05-01T12:00:00Z", "2024-05-01T16:00:00Z");
    CalendarEvent c = createEvent("C", "2024-05-01T15:00:00Z", "2024-05-01T18:00:00Z");

    IntervalTreeNode x = new IntervalTreeNode(a);
    IntervalTreeNode y = new IntervalTreeNode(b);
    IntervalTreeNode z = new IntervalTreeNode(c);

    x.right = y;
    y.left = z;

    z.height = 1;
    y.height = 2;
    x.height = 3;

    z.maxEndTime = c.getEndDateTime();
    y.maxEndTime = b.getEndDateTime();
    x.maxEndTime = a.getEndDateTime();

    IntervalTree tree = new IntervalTree();
    IntervalTreeNode newRoot = tree.rotateLeft(x);

    assertEquals(y, newRoot);
    assertEquals(ZonedDateTime.parse("2024-05-01T18:00:00Z"), newRoot.getMaxEndTime());
  }

}