import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import calendar.controller.exporter.CsvExporter;
import calendar.controller.exporter.ExporterInterface;
import calendar.controller.exporter.IcsExporter;
import calendar.controller.text.CalendarController;
import calendar.controller.text.CommandParser;
import calendar.model.CalendarManager;
import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModelInterface;
import calendar.view.text.CalendarView;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Calendar controller.
 */
public class ControllerTest {
  private MockManager calendar;
  private MockModel model;
  private MockView view;
  private MockView2 view2;
  private CommandParser parser;
  private CalendarController controller;

  private String subject;
  private ZoneId zoneId;
  private LocalDateTime start;
  private LocalDateTime end;
  private LocalDate untildate;
  private LocalDate ondate;
  private LocalDate date;
  private String weekdays;
  private int ndays;
  private String property;
  private String value;
  private String filename;
  private String location;
  private String description;
  private String status;
  private String calname;
  private ZoneId timezone;

  private String inputString = null;
  private String message = null;
  private Map<String, Object> parameters = new HashMap<>();
  private Map<String, Object> testParameters = new HashMap<>();

  /**
   * Checks if two csv files are the same.
   * This is to validate the export functionality.
   *
   * @param reference First file path
   * @param filePath  Second file path
   * @return true if both are same
   * @throws IOException if exception found with file operations
   */
  public static boolean compareCsvFiles(String[] reference, String filePath) throws IOException {
    try (BufferedReader br1 = new BufferedReader(new FileReader(filePath))) {
      String line1;
      String line2;
      int lineNumber = 0;
      while ((line1 = br1.readLine()) != null && (line2 = reference[lineNumber]) != null) {
        if (!line1.equals(line2)) {
          return false;
        }
        lineNumber++;
      }
      return br1.readLine() == null && lineNumber == reference.length;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Setup to initialize the common variables.
   */
  @Before
  public void setUp() {
    this.model = new MockModel();
    this.view = new MockView();
    this.view2 = new MockView2();
    this.parser = new CommandParser();
    this.calendar = new MockManager();

    this.subject = "\"Team Meeting\"";
    this.zoneId = ZoneId.of("America/New_York");
    this.start = LocalDateTime.of(2025, 7, 1, 9, 0);
    this.end = LocalDateTime.of(2025, 7, 1, 10, 0);
    this.untildate = LocalDate.of(2025, 8, 1);
    this.ondate = LocalDate.of(2025, 8, 1);
    this.date = LocalDate.of(2025, 8, 1);
    this.weekdays = "MTW";
    this.ndays = 10;
    this.property = "subject";
    this.value = "\"New Team Meeting\"";
    this.filename = "calFromTest.csv";
    this.location = "physical";
    this.description = "\"PDP team meeting to discuss assignment\"";
    this.status = "public";
    this.calname = "Cal A";
    this.timezone = ZoneId.of("America/New_York");

    this.parameters.put("subject", subject);
    this.parameters.put("start", start.atZone(zoneId));
    this.parameters.put("end", end.atZone(zoneId));
    this.parameters.put("date", date);
    this.parameters.put("untildate", untildate);
    this.parameters.put("ondate", ondate);
    this.parameters.put("weekdays", weekdays);
    this.parameters.put("ndays", ndays);
    this.parameters.put("property", property);
    this.parameters.put("value", value);
    this.parameters.put("filename", filename);
    this.parameters.put("location", location);
    this.parameters.put("description", description);
    this.parameters.put("status", status);
    this.parameters.put("calname", calname);
    this.parameters.put("timezone", timezone);

    this.calendar.createCalendar(this.parameters);
    this.calendar.useCalendar(this.parameters);
    this.controller = new CalendarController(calendar, view, parser);
  }

  @Test
  public void testA1() {

    inputString = "create event " + subject + " from " + start + " to " + end
        + " location " + location + " status " + status + " description " + description;

    controller.processCommand(inputString);
    MockModel model = calendar.getActiveCalendar();
    testParameters = model.getParameters();

    this.message = "Using calendar Cal A: Successfully created a single event called Team Meeting "
        + "(2025-07-01T09:00-04:00[America/New_York] -> 2025-07-01T10:00-04:00[America/New_York]).";
    assertEquals(this.message, view.getMessage());

    for (Map.Entry<String, Object> entry : testParameters.entrySet()) {
      assertEquals(entry.getValue().toString(),
          parameters.get(entry.getKey()).toString().replaceAll("\"", ""));
    }
  }

  @Test
  public void testA2() {

    inputString =
        "create event " + subject + " from " + start + " to " + end + " repeats " + weekdays
            + " until " + untildate;

    controller.processCommand(inputString);
    MockModel model = calendar.getActiveCalendar();
    testParameters = model.getParameters();

    this.message = "Using calendar Cal A: Successfully created an event series called Team Meeting "
        + "(2025-07-01T09:00-04:00[America/New_York] -> 2025-07-01T10:00-04:00[America/New_York])"
        + " on every MTW days recurring until 2025-08-01.";
    assertEquals(this.message, view.getMessage());

    for (Map.Entry<String, Object> entry : testParameters.entrySet()) {
      assertEquals(entry.getValue().toString(),
          parameters.get(entry.getKey()).toString().replaceAll("\"", ""));
    }
  }

  @Test
  public void testA3() {

    inputString =
        "create event " + subject + " from " + start + " to " + end + " repeats " + weekdays
            + " for " + ndays + " times";

    controller.processCommand(inputString);
    MockModel model = calendar.getActiveCalendar();
    testParameters = model.getParameters();

    this.message = "Using calendar Cal A: Successfully created an event series called Team Meeting "
        + "(2025-07-01T09:00-04:00[America/New_York] -> 2025-07-01T10:00-04:00[America/New_York])"
        + " on every MTW days recurring 10 times.";
    assertEquals(this.message, view.getMessage());

    for (Map.Entry<String, Object> entry : testParameters.entrySet()) {
      assertEquals(entry.getValue().toString(),
          parameters.get(entry.getKey()).toString().replaceAll("\"", ""));
    }
  }

  @Test
  public void testA4() {

    inputString = "create event " + subject + " on " + ondate;

    controller.processCommand(inputString);
    MockModel model = calendar.getActiveCalendar();
    testParameters = model.getParameters();

    this.message =
        "Using calendar Cal A: Successfully created an all day event called Team Meeting "
            + "on 2025-08-01.";
    assertEquals(this.message, view.getMessage());

    for (Map.Entry<String, Object> entry : testParameters.entrySet()) {
      assertEquals(entry.getValue().toString(),
          parameters.get(entry.getKey()).toString().replaceAll("\"", ""));
    }
  }

  @Test
  public void testA5() {

    inputString =
        "create event " + subject + " on " + ondate + " repeats " + weekdays
            + " until " + untildate;

    controller.processCommand(inputString);
    MockModel model = calendar.getActiveCalendar();
    testParameters = model.getParameters();

    this.message =
        "Using calendar Cal A: Successfully created an all day event series called Team Meeting "
            + "starting from 2025-08-01 on every MTW days recurring until 2025-08-01.";
    assertEquals(this.message, view.getMessage());

    for (Map.Entry<String, Object> entry : testParameters.entrySet()) {
      assertEquals(entry.getValue().toString(),
          parameters.get(entry.getKey()).toString().replaceAll("\"", ""));
    }
  }

  @Test
  public void testA6() {

    inputString =
        "create event " + subject + " on " + ondate + " repeats " + weekdays + " for " + ndays
            + " times";

    controller.processCommand(inputString);
    MockModel model = calendar.getActiveCalendar();
    testParameters = model.getParameters();

    this.message =
        "Using calendar Cal A: Successfully created an all day event series called Team Meeting "
            + "starting from 2025-08-01 on every MTW days "
            + "recurring 10 times.";
    assertEquals(this.message, view.getMessage());

    for (Map.Entry<String, Object> entry : testParameters.entrySet()) {
      assertEquals(entry.getValue().toString(),
          parameters.get(entry.getKey()).toString().replaceAll("\"", ""));
    }
  }

  @Test
  public void testA7A() {

    inputString = "create event " + subject;

    controller.processCommand(inputString);
    assertEquals("Incomplete command input: Missing tokens. Expected: create "
        + "event <event name> from <start date time> to <end date time>", view.getMessage());
  }

  @Test
  public void testA7B() {

    inputString = "create something";

    controller.processCommand(inputString);
    assertEquals("Invalid item in input: something. Expected: "
        + "create (calendar | event)...", view.getMessage());
  }

  @Test
  public void testA7C() {

    inputString = "edit something";

    controller.processCommand(inputString);
    assertEquals("Invalid item in input: something. Expected: "
        + "edit (calendar | event | events | series)...", view.getMessage());
  }

  @Test
  public void testA7D() {

    inputString = "copy something";

    controller.processCommand(inputString);
    assertEquals("Invalid item in input: something. Expected: "
        + "copy (event | events)...", view.getMessage());
  }

  @Test
  public void testA7E() {

    inputString = "print something";

    controller.processCommand(inputString);
    assertEquals("Invalid item in input: something. Expected: "
        + "print events on <date>", view.getMessage());
  }

  @Test
  public void testA7F() {

    inputString = "use something";

    controller.processCommand(inputString);
    assertEquals("Invalid item in input: something. Expected: "
        + "use calendar --name <calendar name>", view.getMessage());
  }

  @Test
  public void testA7G() {

    inputString = "show something";

    controller.processCommand(inputString);
    assertEquals("Invalid item in input: something. Expected: "
        + "show status on <start date time>", view.getMessage());
  }

  @Test
  public void testA7I() {

    inputString = "show status something";

    controller.processCommand(inputString);
    assertEquals("Invalid item in input: something. Expected: "
        + "show status on <start date time>", view.getMessage());
  }

  @Test
  public void test7J() {

    inputString = "export something";

    controller.processCommand(inputString);
    assertEquals("Invalid item in input: something. Expected: "
        + "export cal <filename>", view.getMessage());
  }

  @Test
  public void test7K() {

    inputString = "use calendar something";

    controller.processCommand(inputString);
    assertEquals("Invalid item in input: something. Expected: "
        + "use calendar --name <calendar name>", view.getMessage());
  }

  @Test
  public void testA8() {

    String wrongndays = "two";
    inputString =
        "create event " + subject + " on " + ondate + " repeats " + weekdays + " for " + wrongndays;

    controller.processCommand(inputString);
    assertEquals("Invalid integer value format: two", view.getMessage());
  }

  @Test
  public void testA9() {

    String wrongondate = "Jan/1/2025";
    inputString = "create event " + subject + " from " + wrongondate + " to " + end;

    controller.processCommand(inputString);
    assertEquals("Invalid date time value format: Jan/1/2025", view.getMessage());
  }

  @Test
  public void testA10() {

    inputString = "create event " + subject + " on " + ondate + " location physical";

    controller.processCommand(inputString);

    this.message =
        "Using calendar Cal A: Successfully created an all day event called Team Meeting on "
            + "2025-08-01.";
    assertEquals(this.message, view.getMessage());

    inputString = "create event " + subject + " on " + ondate + " halloween";

    controller.processCommand(inputString);

    this.message = "Optional parameter is not set as optional property: halloween";
    assertEquals(this.message, view.getMessage());
  }

  @Test
  public void testA11() {

    inputString = "happy";
    controller.processCommand(inputString);
    this.message = "Easter Egg called!";
    assertEquals(this.message, view.getMessage());
  }

  @Test
  public void testA12() {
    controller.processCommand(inputString);
  }

  @Test
  public void testB1() {

    inputString = "edit event " + property + " " + subject + " from " + start + " to " + end
        + " with " + value;
    controller.processCommand(inputString);
    MockModel model = calendar.getActiveCalendar();
    testParameters = model.getParameters();

    for (Map.Entry<String, Object> entry : testParameters.entrySet()) {
      assertEquals(entry.getValue().toString(),
          parameters.get(entry.getKey()).toString().replaceAll("\"", ""));
    }
  }

  @Test
  public void testB2() {

    inputString =
        "edit events " + property + " " + subject + " from " + start + " with " + value;

    controller.processCommand(inputString);
    MockModel model = calendar.getActiveCalendar();
    testParameters = model.getParameters();

    for (Map.Entry<String, Object> entry : testParameters.entrySet()) {
      assertEquals(entry.getValue().toString(),
          parameters.get(entry.getKey()).toString().replaceAll("\"", ""));
    }
  }

  @Test
  public void testB3() {

    inputString =
        "edit series " + property + " " + subject + " from " + start + " with " + value;

    controller.processCommand(inputString);
    MockModel model = calendar.getActiveCalendar();
    testParameters = model.getParameters();

    for (Map.Entry<String, Object> entry : testParameters.entrySet()) {
      assertEquals(entry.getValue().toString(),
          parameters.get(entry.getKey()).toString().replaceAll("\"", ""));
    }
  }

  @Test
  public void testC1() {

    inputString = "show status on " + start;

    MockModel model = calendar.getActiveCalendar();
    model.setAvailability(true);
    controller.processCommand(inputString);
    this.message = "Using calendar Cal A: Available on " + this.parameters.get("start") + ".";
    assertEquals(this.message, view.getMessage());

    model.setAvailability(false);
    controller.processCommand(inputString);
    this.message = "Using calendar Cal A: Busy on " + this.parameters.get("start") + ".";
    assertEquals(this.message, view.getMessage());
  }

  @Test
  public void testC2() {
    try {
      controller = new CalendarController((CalendarManagerInterface) null, view, parser);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController((CalendarModelInterface) null, view, parser);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Model, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController(calendar, null, parser);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController(model, null, parser);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Model, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController(calendar, view, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController(model, view, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Model, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController((CalendarManagerInterface) null, null, parser);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController((CalendarModelInterface) null, null, parser);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Model, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController((CalendarManagerInterface) null, view, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController((CalendarModelInterface) null, view, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Model, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController(calendar, null, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController(model, null, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Model, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController((CalendarManagerInterface) null, null, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      controller = new CalendarController((CalendarModelInterface) null, null, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Model, View, or Parser cannot be null when creating a calendar controller.",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    controller = new CalendarController(model, view, parser);
    assertEquals(CalendarController.class, controller.getClass());
    controller = new CalendarController(calendar, view, parser);
    assertEquals(CalendarController.class, controller.getClass());
  }

  @Test
  public void testD1A() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    this.controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    inputString =
        "create event " + subject + " from " + (start.plusDays(1)) + " to " + (end.plusDays(1));
    controller.processCommand(inputString);
    inputString =
        "create event " + subject + " from " + (start.plusDays(2)) + " to " + (end.plusDays(2));
    controller.processCommand(inputString);
    inputString =
        "create event " + subject + " from " + (start.plusDays(5)) + " to " + (end.plusDays(5));
    controller.processCommand(inputString);

    inputString = "print events on " + start.toLocalDate();
    controller.processCommand(inputString);
    String expected = "- Team Meeting starting on 2025-07-01 at 09:00,"
        + " ending on 2025-07-01 at 10:00";
    assertEquals(expected, view.getMessage());

    inputString = "print events on " + start.toLocalDate().plusDays(5);
    controller.processCommand(inputString);
    expected = "- Team Meeting starting on 2025-07-06 at 09:00,"
        + " ending on 2025-07-06 at 10:00";
    assertEquals(expected, view.getMessage());

    inputString = "print events on " + start.toLocalDate().plusDays(3);
    controller.processCommand(inputString);
    expected = "No events found on 2025-07-04.";
    assertEquals(expected, view.getMessage());

    inputString = "print events from " + start + " to " + start.plusDays(5);
    controller.processCommand(inputString);
    expected = "- Team Meeting starting on 2025-07-03 at 09:00,"
        + " ending on 2025-07-03 at 10:00";
    assertEquals(expected, view.getMessage());

    inputString = "print events from " + start.plusDays(4) + " to " + start.plusDays(10);
    controller.processCommand(inputString);
    expected = "- Team Meeting starting on 2025-07-06 at 09:00, ending on 2025-07-06 at 10:00";
    assertEquals(expected, view.getMessage());
  }

  @Test
  public void testD1B() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    MockView2 view = new MockView2();
    this.controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    inputString =
        "create event " + subject + " from " + (start.plusDays(1)) + " to " + (end.plusDays(1));
    controller.processCommand(inputString);
    inputString =
        "create event " + subject + " from " + (start.plusDays(2)) + " to " + (end.plusDays(2));
    controller.processCommand(inputString);
    inputString =
        "create event " + subject + " from " + (start.plusDays(5)) + " to " + (end.plusDays(5));
    controller.processCommand(inputString);

    inputString = "print events on " + start.toLocalDate();
    controller.processCommand(inputString);
    String expected = "Using calendar Cal A:";
    assertEquals(expected, view.getMessageArray().get(4));
    expected = "- Team Meeting starting on 2025-07-01 at 09:00,"
        + " ending on 2025-07-01 at 10:00";
    assertEquals(expected, view.getMessage());
  }

  @Test
  public void testD1C() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    MockView2 view = new MockView2();
    this.controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    inputString =
        "create event " + subject + " from " + (start.plusDays(1)) + " to " + (end.plusDays(1));
    controller.processCommand(inputString);
    inputString =
        "create event " + subject + " from " + (start.plusDays(2)) + " to " + (end.plusDays(2));
    controller.processCommand(inputString);
    inputString =
        "create event " + subject + " from " + (start.plusDays(5)) + " to " + (end.plusDays(5));
    controller.processCommand(inputString);

    inputString = "print events from " + start.minusDays(5) + " to " + start.plusDays(10);
    controller.processCommand(inputString);
    String expected = "Using calendar Cal A:";
    assertEquals(expected, view.getMessageArray().get(4));
    expected = "- Team Meeting starting on 2025-07-06 at 09:00,"
        + " ending on 2025-07-06 at 10:00";
    assertEquals(expected, view.getMessage());
  }

  @Test
  public void testD2() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    this.controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start.plusDays(2) + " to " + end;
    controller.processCommand(inputString);
    assertEquals("CreateSingleEvent command: Start time must be before end time",
        view.getMessage());

    inputString = "create event " + subject + " from " + start.plusDays(2) + " to " + end
        + " repeats ABC " + " for " + ndays + " times";
    controller.processCommand(inputString);
    assertEquals("CreateEventSeriesFor command: Series events cannot span multiple days. "
        + "Start and end must be on the same day.", view.getMessage());

    inputString = "create event " + subject + " from " + start + " to " + end
        + " repeats MAZ " + " for " + ndays + " times";
    controller.processCommand(inputString);
    assertEquals("CreateEventSeriesFor command: Invalid day of week: A", view.getMessage());

    inputString = "create event " + subject + " from " + start + " to " + end
        + " repeats MTW " + " for " + (ndays * -2) + " times";
    controller.processCommand(inputString);
    assertEquals("CreateEventSeriesFor command: Repeat count must be at least 1. "
            + "Cannot create series with count: -20",
        view.getMessage());

    inputString = "create event " + subject + " from " + start + " to " + end
        + " repeats MTW " + " until " + date.minusDays(35);
    controller.processCommand(inputString);
    assertEquals("CreateEventSeriesUntil command: Repeat until date must be "
        + "after the Start date. Until date: 2025-06-27, "
        + "Start date: 2025-07-01T09:00-04:00[America/New_York]", view.getMessage());

    inputString = "create event " + subject + " on " + date;
    controller.processCommand(inputString);
    inputString = "create event " + subject + " on " + date;
    controller.processCommand(inputString);
    assertEquals("CreateAllDayEvent command: Duplicate event detected.",
        view.getMessage());

    inputString = "create event " + subject + " on " + date + " repeats MTWF for "
        + ndays + " times";
    controller.processCommand(inputString);
    assertEquals("CreateAllDayEventSeriesFor command: Duplicate event detected.",
        view.getMessage());

    inputString = "create event " + subject + " on " + date + " repeats MTW until "
        + date.minusDays(1);
    controller.processCommand(inputString);
    assertEquals("CreateAllDayEventSeriesUntil command: Repeat until date "
        + "must be after the Start date. Until date:"
        + " 2025-07-31, "
        + "Start date: 2025-08-01T08:00-04:00[America/New_York]", view.getMessage());
  }

  @Test
  public void testD3() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    this.controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    inputString = "create event " + subject + " from " + start.plusDays(3)
        + " to " + end.plusDays(1);
    controller.processCommand(inputString);
    inputString = "create event " + subject + " from " + start.plusDays(6)
        + " to " + end.plusDays(6);
    controller.processCommand(inputString);

    inputString = "edit event start " + subject + " from " + start + " to " + end
        + " with " + start.plusDays(2);
    controller.processCommand(inputString);
    assertEquals("EditSingleEvent command: Start time must be before end time",
        view.getMessage());

    inputString = "edit events start " + subject + " from " + start.plusDays(2)
        + " with " + start.plusDays(3);
    controller.processCommand(inputString);
    assertEquals("EditEvents command: Event not found", view.getMessage());

    inputString = "edit series start " + subject + " from " + start
        + " with " + start.plusDays(3);
    controller.processCommand(inputString);
    assertEquals("EditSeries command: Start time must be before end time", view.getMessage());
  }

  @Test
  public void testD4A() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    this.controller = new CalendarController(calendar, view, parser);

    inputString = "print events from " + start + " to " + end;
    controller.processCommand(inputString);
    assertEquals("No events found between 2025-07-01T09:00-04:00[America/New_York]"
        + " and 2025-07-01T10:00-04:00[America/New_York].", view.getMessage());

    inputString = "print events on " + ondate;
    controller.processCommand(inputString);
    assertEquals("No events found on 2025-08-01.",
        view.getMessage());
  }

  @Test
  public void testD4B() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    this.controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    message = "Using calendar Cal A: Successfully created a single event called "
        + "Team Meeting (2025-07-01T09:00-04:00[America/New_York] -> "
        + "2025-07-01T10:00-04:00[America/New_York]).";
    assertEquals(message, view.getMessage());
    inputString = "print events from " + start.minusHours(2) + " to " + end.plusHours(2);
    controller.processCommand(inputString);
    message = "- Team Meeting starting on 2025-07-01 at 09:00, ending on 2025-07-01 at 10:00";
    assertEquals(message, view.getMessage());
    inputString = "edit calendar --name \"Cal A\" --property timezone \"America/Los_Angeles\"";
    controller.processCommand(inputString);
    message = "Successfully edited calendar Cal A and changed property timezone "
        + "to America/Los_Angeles.";
    assertEquals(message, view.getMessage());
    inputString = "print events from " + start.minusDays(2) + " to " + end.plusDays(2);
    controller.processCommand(inputString);
    message = "- Team Meeting starting on 2025-07-01 at 06:00, ending on 2025-07-01 at 07:00";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testD5() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    this.controller = new CalendarController(calendar, view, parser);

    inputString = "export cal " + filename;
    controller.processCommand(inputString);
    assertEquals("No events exist in calendar Cal A.", view.getMessage());

    inputString = "create event \"Team Meeting 1\" from " + start + " to " + end
        + " description \"First team meeting with everyone, get laptops!\"";
    controller.processCommand(inputString);
    inputString = "create event \"Team Meeting 2\" from " + start.plusDays(3)
        + " to " + end.plusDays(3) + " status public";
    controller.processCommand(inputString);
    inputString = "create event \"Team Meeting 3\" from " + start.plusDays(6)
        + " to " + end.plusDays(6) + " location physical";
    controller.processCommand(inputString);
    inputString = "create event \"Team Meeting 4\" from " + start + " to " + end
        + " description \"Last team meeting with everyone\n order pizza!\"";
    controller.processCommand(inputString);

    inputString = "export cal " + filename;
    controller.processCommand(inputString);
    assertEquals("Exported events in calendar Cal A.", view.getMessage());

    String referenceString = "Subject,Start date,Start time,End date,End time,Description,"
        + "Location,Status" + System.lineSeparator()
        + "Team Meeting 1,2025-07-01,09:00,2025-07-01,10:00,"
        + "\"First team meeting with everyone, get laptops!\",null,null" + System.lineSeparator()
        + "Team Meeting 4,2025-07-01,09:00,2025-07-01,10:00,\"Last team meeting with everyone\n"
        + " order pizza!\",null,null" + System.lineSeparator()
        + "Team Meeting 2,2025-07-04,09:00,2025-07-04,10:00,null,null,public"
        + System.lineSeparator()
        + "Team Meeting 3,2025-07-07,09:00,2025-07-07,10:00,null,physical,null";

    String[] reference = referenceString.split(System.lineSeparator());

    try {
      assertTrue(compareCsvFiles(reference, "calFromTest.csv"));
    } catch (Exception e) {
      assert false;
    }

    inputString = "export cal ./folderNotExists/" + filename;
    controller.processCommand(inputString);
    assertEquals("ExportCalendar command: Unsupported file extension:"
            + " /folderNotExists/calFromTest",
        view.getMessage());
  }

  @Test
  public void testD6() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    this.controller = new CalendarController(calendar, view, parser);

    inputString = "export cal calFromTest2.ics";
    controller.processCommand(inputString);
    assertEquals("No events exist in calendar Cal A.", view.getMessage());

    inputString = "create event \"Assignment 1\" from " + start + " to " + end
        + " description \"First assignment with everyone, get laptops!\"";
    controller.processCommand(inputString);
    inputString = "create event \"Assignment 2\" from " + start.plusDays(3)
        + " to " + end.plusDays(3) + " status public";
    controller.processCommand(inputString);
    inputString = "create event \"Assignment 3\" from " + start.plusDays(6)
        + " to " + end.plusDays(6) + " location physical";
    controller.processCommand(inputString);
    inputString = "create event \"Assignment 4\" from " + start + " to " + end
        + " description \"Last assignment with everyone\n order calzones!\"";
    controller.processCommand(inputString);

    inputString = "export cal calFromTest2.ics";
    controller.processCommand(inputString);
    assertEquals("Exported events in calendar Cal A.", view.getMessage());

    String referenceString = "BEGIN:VCALENDAR" + System.lineSeparator()
        + "PRODID:-//MAZIV//Calendar App//EN" + System.lineSeparator()
        + "VERSION:2.0" + System.lineSeparator()
        + "CALSCALE:GREGORIAN" + System.lineSeparator()
        + "X-WR-CALNAME:Cal A" + System.lineSeparator()
        + "X-WR-TIMEZONE:America/New_York" + System.lineSeparator()
        + "BEGIN:VEVENT" + System.lineSeparator()
        + "DTSTART:20250701T130000Z" + System.lineSeparator()
        + "DTEND:20250701T140000Z" + System.lineSeparator()
        + "UID:Assignment 1-2025-07-012025-07-01" + System.lineSeparator()
        + "SUMMARY:Assignment 1" + System.lineSeparator()
        + "END:VEVENT" + System.lineSeparator()
        + "BEGIN:VEVENT" + System.lineSeparator()
        + "DTSTART:20250701T130000Z" + System.lineSeparator()
        + "DTEND:20250701T140000Z" + System.lineSeparator()
        + "UID:Assignment 4-2025-07-012025-07-01" + System.lineSeparator()
        + "SUMMARY:Assignment 4" + System.lineSeparator()
        + "END:VEVENT" + System.lineSeparator()
        + "BEGIN:VEVENT" + System.lineSeparator()
        + "DTSTART:20250704T130000Z" + System.lineSeparator()
        + "DTEND:20250704T140000Z" + System.lineSeparator()
        + "UID:Assignment 2-2025-07-042025-07-04" + System.lineSeparator()
        + "SUMMARY:Assignment 2" + System.lineSeparator()
        + "END:VEVENT" + System.lineSeparator()
        + "BEGIN:VEVENT" + System.lineSeparator()
        + "DTSTART:20250707T130000Z" + System.lineSeparator()
        + "DTEND:20250707T140000Z" + System.lineSeparator()
        + "UID:Assignment 3-2025-07-072025-07-07" + System.lineSeparator()
        + "SUMMARY:Assignment 3" + System.lineSeparator()
        + "END:VEVENT" + System.lineSeparator()
        + "END:VCALENDAR" + System.lineSeparator();

    String[] reference = referenceString.split(System.lineSeparator());

    try {
      assertTrue(compareCsvFiles(reference, "calFromTest2.ics"));
    } catch (Exception e) {
      assert false;
    }
  }

  @Test
  public void testD7() {
    ExporterInterface exporter = new CsvExporter("");
    try {
      exporter.exportHelper(new ArrayList<>());
      assert false;
    } catch (Exception e) {
      assert true;
    }

    exporter = new IcsExporter("", calendar);
    try {
      exporter.exportHelper(new ArrayList<>());
      assert false;
    } catch (Exception e) {
      assert true;
    }
  }

  @Test
  public void testE1() {
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end
        + " location physical";

    controller.processCommand(inputString);
    assertEquals("Using calendar Cal A: Successfully created a single event called Team Meeting "
        + "(2025-07-01T09:00-04:00[America/New_York] -> "
        + "2025-07-01T10:00-04:00[America/New_York]).", view.getMessage());
  }

  @Test
  public void testE2() {
    CalendarManager calendar = new CalendarManager();
    controller = new CalendarController(calendar, view, parser);
    inputString = "create calendar --name \"Cal A\" --timezone \"Asia/Tokyo\"";
    controller.processCommand(inputString);
    assertEquals("Successfully created calendar Cal A in timezone Asia/Tokyo",
        view.getMessage());
    inputString = "use calendar --name \"Cal A\"";
    controller.processCommand(inputString);
    assertEquals("Set active calendar to Cal A", view.getMessage());
    inputString = "create event " + subject + " on " + ondate;
    controller.processCommand(inputString);
    controller.processCommand(inputString);

    message = "CreateAllDayEvent command: Duplicate event detected.";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testE3() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    controller.processCommand(inputString);

    message = "CreateSingleEvent command: Duplicate event detected.";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testE4() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end
        + " repeats MTW for " + ndays + " times";
    controller.processCommand(inputString);
    controller.processCommand(inputString);

    message = "CreateEventSeriesFor command: Duplicate event detected.";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testE5() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end
        + " repeats MTW until " + untildate;
    controller.processCommand(inputString);
    controller.processCommand(inputString);

    message = "CreateEventSeriesUntil command: Duplicate event detected.";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testE6() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " on " + ondate
        + " repeats MTW for " + ndays + " times";
    ;
    controller.processCommand(inputString);
    controller.processCommand(inputString);

    message = "CreateAllDayEventSeriesFor command: Duplicate event detected.";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testE7() {
    CalendarManager calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " on " + ondate
        + " repeats MTW until " + untildate;
    controller.processCommand(inputString);
    controller.processCommand(inputString);

    message = "CreateAllDayEventSeriesUntil command: Duplicate event detected.";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testE8() {
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    inputString = "create event " + subject + " from " + start.plusDays(1)
        + " to " + end.plusDays(1);
    controller.processCommand(inputString);
    inputString = "edit event subject " + subject + " from " + start
        + " to " + end + " with " + start;
    controller.processCommand(inputString);

    message = "Using calendar Cal A: Successfully edited an event called Team Meeting "
        + "(2025-07-01T09:00-04:00[America/New_York] -> "
        + "2025-07-01T10:00-04:00[America/New_York]) and changed subject to 2025-07-01T09:00.";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testE9() {
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end
        + " repeats MTW for " + ndays + " times";
    controller.processCommand(inputString);
    inputString = "edit series subject " + subject + " from " + start + " with " + start;
    controller.processCommand(inputString);

    message =
        "Using calendar Cal A: Successfully edited a series called Team Meeting starting from "
            + "2025-07-01T09:00-04:00[America/New_York] and changed subject to 2025-07-01T09:00.";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testE10() {
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end
        + " repeats MTW for " + ndays + " times";
    controller.processCommand(inputString);
    inputString = "edit events subject " + subject + " from " + start + " with " + start;
    controller.processCommand(inputString);

    message =
        "Using calendar Cal A: Successfully edited all events called Team Meeting starting from "
            + "2025-07-01T09:00-04:00[America/New_York] and changed subject to 2025-07-01T09:00.";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testF1() {
    controller = new CalendarController(calendar, view, parser);
    inputString = "create calendar --name \"Cal 1\" --timezone \"America/New_York\"";
    controller.processCommand(inputString);
    message = "Successfully created calendar Cal 1 in timezone America/New_York";
    assertEquals(message, view.getMessage());

    inputString = "use calendar --name \"Cal 1\"";
    controller.processCommand(inputString);
    message = "Set active calendar to Cal 1";
    assertEquals(message, view.getMessage());

    inputString = "create calendar --name \"Cal 1\" --timezone \"America/New_York\"";
    controller.processCommand(inputString);
    message = "CreateCalendar command: Calendar with name 'Cal 1' already exists";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testF2() {
    CalendarManagerInterface calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    inputString = "copy event " + subject + " on " + start + " --target \""
        + calname + "\" to " + start.plusDays(1);
    controller.processCommand(inputString);
    message = "Copied event Team Meeting from Cal A on 2025-07-01T09:00-04:00[America/New_York]"
        + " to Cal A on 2025-07-02T09:00 of its' timezone.";
    assertEquals(message, view.getMessage());

    inputString = "copy event " + subject + " on " + start.plusDays(50) + " --target \""
        + calname + "\" to " + start.plusDays(1);
    controller.processCommand(inputString);
    message = "CopyEvent command: Event not found";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testF3() {
    CalendarManagerInterface calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    inputString = "copy events between " + date.minusDays(33) + " and " + date
        + " --target \"" + calname + "\" to " + date.plusDays(2);
    controller.processCommand(inputString);
    message = "Copied events between 2025-06-29 and 2025-08-01 from Cal A to Cal A "
        + "on 2025-08-03 of its' timezone.";
    assertEquals(message, view.getMessage());

    inputString = "copy events between " + date.plusDays(50) + " and " + date.plusDays(60)
        + " --target \"" + calname + "\" to " + date.plusDays(20);
    controller.processCommand(inputString);
    message = "CopyEventsBetween command: No events found between 2025-09-20 and 2025-09-30";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testF3A() {
    CalendarManagerInterface calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    parameters.put("calname", "Cal B");
    calendar.createCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    inputString = "copy events between " + date.minusDays(33) + " and " + date
        + " --target \"Cal B\" to " + date.plusDays(2);
    controller.processCommand(inputString);
    message = "Copied events between 2025-06-29 and 2025-08-01 from Cal A to Cal B "
        + "on 2025-08-03 of its' timezone.";
    assertEquals(message, view.getMessage());

    inputString = "copy events between " + date.plusDays(50) + " and " + date.plusDays(60)
        + " --target \"" + calname + "\" to " + date.plusDays(20);
    controller.processCommand(inputString);
    message = "CopyEventsBetween command: No events found between 2025-09-20 and 2025-09-30";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testF4() {
    CalendarManagerInterface calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    inputString = "copy events on " + date.minusDays(31) + " --target \""
        + calname + "\" to " + date.plusDays(1);
    controller.processCommand(inputString);
    message = "Copied events on 2025-07-01 from Cal A to Cal A on 2025-08-02 of its' timezone.";
    assertEquals(message, view.getMessage());

    inputString = "copy events on " + date + " --target \"" + calname + "\" to "
        + date.plusDays(1);
    controller.processCommand(inputString);
    message = "CopyEventsOn command: No events found on date: 2025-08-01";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testF5() {
    CalendarManagerInterface calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "create event " + subject + " from " + start + " to " + end;
    controller.processCommand(inputString);
    inputString = "edit calendar --name \"" + calname + "\" --property name \"Cal B\"";
    controller.processCommand(inputString);
    message = "Successfully edited calendar Cal A and changed property name to Cal B.";
    assertEquals(message, view.getMessage());
    inputString = "edit calendar --name \"Cal B\" --property timezone \"America/Los_Angeles\"";
    controller.processCommand(inputString);
    message = "Successfully edited calendar Cal B and changed property timezone"
        + " to America/Los_Angeles.";
    assertEquals(message, view.getMessage());

    inputString = "edit calendar --name \"" + calname + "\" --property name \"Cal B\"";
    controller.processCommand(inputString);
    message = "EditCalendar command: Calendar 'Cal A' does not exist";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testF6() {
    CalendarManagerInterface calendar = new CalendarManager();
    calendar.createCalendar(parameters);
    calendar.useCalendar(parameters);
    controller = new CalendarController(calendar, view, parser);
    inputString = "use calendar --name ABC";
    controller.processCommand(inputString);
    message = "UseCalendar command: Calendar with name 'ABC' does not exist";
    assertEquals(message, view.getMessage());
  }

  @Test
  public void testG1() {
    controller = new CalendarController(calendar, view2, parser);
    String simulatedInput =
        System.lineSeparator() + "create" + System.lineSeparator() + "" + System.lineSeparator()
            + "exit";
    InputStream testIn = new ByteArrayInputStream(simulatedInput.getBytes());
    System.setIn(testIn);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    PrintStream testOut = new PrintStream(outStream);
    System.setOut(testOut);
    controller.interactiveMode();
    assertEquals("-> -> -> -> ", outStream.toString());
    assertTrue(view2.getMessageArray().get(0).contains("Enter command"));
    assertTrue(view2.getMessageArray().get(1).contains("Missing token"));
    assertTrue(view2.getMessageArray().get(2).contains("Exit command encountered. "
        + "Terminating interactive"));
    System.setIn(System.in);
    System.setOut(System.out);
  }

  @Test
  public void testG2() throws IOException {
    controller = new CalendarController(calendar, view2, parser);

    File temp = File.createTempFile("cmd", ".txt");
    temp.deleteOnExit();

    String fileContent = System.lineSeparator() + "create" + System.lineSeparator() + ""
        + System.lineSeparator() + "exit";
    Files.write(temp.toPath(), fileContent.getBytes());

    controller.headlessMode(temp.getAbsolutePath());

    assertTrue(view2.getMessageArray().get(0).contains("Missing token"));
    assertTrue(view2.getMessageArray().get(1).contains("Exit command encountered. "
        + "Terminating headless"));
  }


  @Test
  public void testG3() throws IOException {
    controller = new CalendarController(calendar, view2, parser);

    File temp = File.createTempFile("cmd", ".txt");
    temp.deleteOnExit();

    String fileContent = "create";
    Files.write(temp.toPath(), fileContent.getBytes());

    controller.headlessMode("Path doesn't exist");
    assertTrue(view2.getMessageArray().get(0).contains("Path doesn't exist"));

    controller.headlessMode(temp.getAbsolutePath());
    assertTrue(view2.getMessageArray().get(1).contains("Missing token"));
    assertTrue(view2.getMessageArray().get(2).contains("No exit command"));
  }

  @Test
  public void testG4() {
    controller = new CalendarController(calendar, view2, parser);
    controller.guiMode();
    assertTrue(view2.getMessageArray().get(0).contains("GUI Mode not supported"));

    controller.showUsageAndExit();
    assertTrue(view2.getMessageArray().get(1).contains("Invalid command-line arguments"));
  }

  @Test
  public void testZ1() {
    inputString = "create event " + subject + " from " + start + " to " + end
        + " location " + location + " status " + status + " description " + description;

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    PrintStream testOut = new PrintStream(outStream);

    try {
      System.setOut(testOut);
      CalendarView view = new CalendarView();
      this.controller = new CalendarController(calendar, view, parser);
      controller.processCommand(inputString);

      String output = outStream.toString();
      assertTrue(output.contains("Using calendar Cal A: Successfully created a single event"));

    } catch (Exception e) {
      assert false;
    }
    System.setOut(System.out);
    System.setErr(System.err);
  }

  @Test
  public void testZ2() {
    inputString = "create event " + subject + " from " + start + " to " + end
        + " location " + location + " status " + status + " description " + description;

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    PrintStream testOut = new PrintStream(outStream);

    try {
      System.setErr(testOut);
      CalendarView view = new CalendarView();
      CalendarManager calendar = new CalendarManager();
      calendar.createCalendar(parameters);
      calendar.useCalendar(parameters);
      this.controller = new CalendarController(calendar, view, parser);
      controller.processCommand(inputString);
      controller.processCommand(inputString);

      String output = outStream.toString();
      assertTrue(output.contains("Duplicate event"));
    } catch (Exception e) {
      assert false;
    }
    System.setOut(System.out);
    System.setErr(System.err);
  }

  @Test
  public void testZ3() {
    inputString = "create event " + subject + " from " + start + " to " + end
        + " location " + location + " status " + status + " description " + description;

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    PrintStream testOut = new PrintStream(outStream);

    try {
      System.setErr(testOut);
      CalendarView view = new CalendarView(System.out, System.err);
      CalendarManager calendar = new CalendarManager();
      calendar.createCalendar(parameters);
      calendar.useCalendar(parameters);
      this.controller = new CalendarController(calendar, view, parser);
      controller.processCommand(inputString);
      controller.processCommand(inputString);

      String output = outStream.toString();
      assertTrue(output.contains("Duplicate event"));
    } catch (Exception e) {
      assert false;
    }
    System.setOut(System.out);
    System.setErr(System.err);
  }
}