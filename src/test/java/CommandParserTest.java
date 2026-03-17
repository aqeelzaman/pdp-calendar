import static org.junit.Assert.assertEquals;

import calendar.controller.text.CommandParser;
import calendar.controller.text.commandfactory.CommandInterface;
import calendar.controller.text.commandfactory.CopyEvent;
import calendar.controller.text.commandfactory.CopyEventsBetween;
import calendar.controller.text.commandfactory.CopyEventsOn;
import calendar.controller.text.commandfactory.CreateAllDayEvent;
import calendar.controller.text.commandfactory.CreateAllDayEventSeriesFor;
import calendar.controller.text.commandfactory.CreateAllDayEventSeriesUntil;
import calendar.controller.text.commandfactory.CreateCalendar;
import calendar.controller.text.commandfactory.CreateEventSeriesFor;
import calendar.controller.text.commandfactory.CreateEventSeriesUntil;
import calendar.controller.text.commandfactory.CreateSingleEvent;
import calendar.controller.text.commandfactory.EasterEgg;
import calendar.controller.text.commandfactory.EditCalendar;
import calendar.controller.text.commandfactory.EditEvents;
import calendar.controller.text.commandfactory.EditSeries;
import calendar.controller.text.commandfactory.EditSingleEvent;
import calendar.controller.text.commandfactory.ExportCalendar;
import calendar.controller.text.commandfactory.Node;
import calendar.controller.text.commandfactory.PrintEventsFrom;
import calendar.controller.text.commandfactory.PrintEventsOn;
import calendar.controller.text.commandfactory.ShowStatus;
import calendar.controller.text.commandfactory.UseCalendar;
import calendar.model.CalendarManager;
import calendar.model.CalendarManagerInterface;
import calendar.model.CalendarModel;
import calendar.model.CalendarModelInterface;
import calendar.view.CalendarViewInterface;
import calendar.view.text.CalendarView;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing the command parser class with different user inputs.
 */
public class CommandParserTest {
  private CalendarModelInterface model;
  private CalendarManagerInterface calendar;
  private CalendarViewInterface view;
  private CommandParser parser;
  private CommandInterface command = null;
  private LocalDateTime start;
  private LocalDateTime end;
  private LocalDate date;
  private String subject;
  private String inputString = null;
  private Map<String, Object> parameters;

  /**
   * Setup to initialize the common variables.
   */
  @Before
  public void setUp() {
    this.calendar = new CalendarManager();
    this.model = new CalendarModel();
    this.view = new CalendarView();
    this.parser = new CommandParser();
    this.start = LocalDateTime.of(2025, 7, 1, 9, 0);
    this.end = LocalDateTime.of(2025, 7, 1, 10, 0);
    this.date = LocalDate.of(2025, 8, 1);
    this.subject = "\"Team Meeting\"";

    this.parameters = new HashMap<>();
    this.parameters.put("calname", "Cal A");
    this.parameters.put("timezone", "America/New_York");
    this.calendar.createCalendar(this.parameters);
    this.calendar.useCalendar(this.parameters);
  }

  @Test
  public void testParserCreate() {

    inputString = "create event " + subject + " from " + start + " to " + end;
    command = parser.parse(inputString, calendar, view);
    assertEquals(CreateSingleEvent.class, command.getClass());

    inputString =
        "create event " + subject + " from " + start + " to " + end + " repeats SMT until " + date;
    command = parser.parse(inputString, calendar, view);
    assertEquals(CreateEventSeriesUntil.class, command.getClass());

    inputString =
        "create event " + subject + " from " + start + " to " + end + " repeats SMT for 10 times";
    command = parser.parse(inputString, calendar, view);
    assertEquals(CreateEventSeriesFor.class, command.getClass());

    inputString = "create event " + subject + " on " + date;
    command = parser.parse(inputString, calendar, view);
    assertEquals(CreateAllDayEvent.class, command.getClass());

    inputString = "create event " + subject + " on " + date
        + " repeats SMT until " + date.plusDays(10);
    command = parser.parse(inputString, calendar, view);
    assertEquals(CreateAllDayEventSeriesUntil.class, command.getClass());

    inputString = "create event " + subject + " on " + date + " repeats SMT for 20 times";
    command = parser.parse(inputString, calendar, view);
    assertEquals(CreateAllDayEventSeriesFor.class, command.getClass());
  }

  @Test
  public void testParserEdit() {

    inputString = "edit event subject " + subject + " from " + start + " to " + end
        + " with \"New Team Meeting\"";
    command = parser.parse(inputString, calendar, view);
    assertEquals(EditSingleEvent.class, command.getClass());

    inputString = "edit event end " + subject + " from " + start + " to " + end
        + " with " + end.plusHours(2);
    command = parser.parse(inputString, calendar, view);
    assertEquals(EditSingleEvent.class, command.getClass());

    inputString =
        "edit events subject " + subject + " from " + start + " with \"New Team Meeting\"";
    command = parser.parse(inputString, calendar, view);
    assertEquals(EditEvents.class, command.getClass());

    inputString =
        "edit series subject " + subject + " from " + start + " with \"New Team Meeting\"";
    command = parser.parse(inputString, calendar, view);
    assertEquals(EditSeries.class, command.getClass());

    inputString =
        "edit series start " + subject + " from " + start + " with " + start.plusHours(2);
    command = parser.parse(inputString, calendar, view);
    assertEquals(EditSeries.class, command.getClass());
  }

  @Test
  public void testParserPrint() {

    inputString = "print events from " + start + " to " + end;
    command = parser.parse(inputString, calendar, view);
    assertEquals(PrintEventsFrom.class, command.getClass());

    inputString = "print events on " + date;
    command = parser.parse(inputString, calendar, view);
    assertEquals(PrintEventsOn.class, command.getClass());
  }

  @Test
  public void testParserMiscellaneous() {

    inputString = "show status on " + start;
    command = parser.parse(inputString, calendar, view);
    assertEquals(ShowStatus.class, command.getClass());

    inputString = "export cal someFileName.csv";
    command = parser.parse(inputString, calendar, view);
    assertEquals(ExportCalendar.class, command.getClass());
  }

  @Test
  public void testC1() {
    inputString = "   CREATE    event " + subject + " from " + start + " to " + end + "    ";
    command = parser.parse(inputString, calendar, view);
    assertEquals(CreateSingleEvent.class, command.getClass());

    try {
      inputString = "  create wrong input  ";
      command = parser.parse(inputString, calendar, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid item in input: wrong. Expected: create "
          + "(calendar | event)...", e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      inputString = "create event " + subject;
      command = parser.parse(inputString, calendar, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Incomplete command input: Missing tokens. Expected: create event "
          + "<event name> from <start date time> to <end date time>", e.getMessage());
    } catch (Exception e) {
      assert false;
    }
  }

  @Test
  public void testN1() {
    Map<String, Object> params = new HashMap<>();
    Node mynode = new Node("create", false);
    try {
      mynode.getExecutable(params, calendar, view);
      assert false;
    } catch (IllegalStateException e) {
      assertEquals("Command is null for this node: create", e.getMessage());
    } catch (Exception e) {
      assert false;
    }
  }

  @Test
  public void testA1() {
    Node myNode1 = new Node("create", false);
    Map<String, Object> parameters = new HashMap<>();

    try {
      myNode1.setCommand(CreateAllDayEvent::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateAllDayEvent", e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateAllDayEventSeriesFor::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateAllDayEventSeriesFor",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateAllDayEventSeriesUntil::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateAllDayEventSeriesUntil",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateEventSeriesFor::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateEventSeriesFor",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateEventSeriesUntil::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateEventSeriesUntil",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateSingleEvent::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateSingleEvent",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EditEvents::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EditEvents",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EditSeries::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EditSeries",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EditSingleEvent::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EditSingleEvent",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(ExportCalendar::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in ExportCalendar",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(PrintEventsFrom::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in PrintEventsFrom",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(PrintEventsOn::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in PrintEventsOn",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(ShowStatus::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in ShowStatus",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }
  }


  @Test
  public void testA2() {
    Node myNode1 = new Node("create", false);
    Map<String, Object> parameters = new HashMap<>();

    try {
      myNode1.setCommand(CreateAllDayEvent::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateAllDayEvent", e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateAllDayEventSeriesFor::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateAllDayEventSeriesFor",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateAllDayEventSeriesUntil::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateAllDayEventSeriesUntil",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateEventSeriesFor::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateEventSeriesFor",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateEventSeriesUntil::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateEventSeriesUntil",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateSingleEvent::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateSingleEvent",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EditEvents::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EditEvents",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EditSeries::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EditSeries",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EditSingleEvent::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EditSingleEvent",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateCalendar::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateCalendar",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CreateCalendar::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CreateCalendar",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(UseCalendar::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in UseCalendar",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(UseCalendar::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in UseCalendar",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EditCalendar::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EditCalendar",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EditCalendar::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EditCalendar",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CopyEvent::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CopyEvent",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CopyEvent::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CopyEvent",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CopyEventsOn::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CopyEventsOn",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CopyEventsOn::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CopyEventsOn",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CopyEventsBetween::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CopyEventsBetween",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(CopyEventsBetween::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in CopyEventsBetween",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }


    try {
      myNode1.setCommand(EditCalendar::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EditCalendar",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EditCalendar::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EditCalendar",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }
  }

  @Test
  public void testA3() {
    Node myNode1 = new Node("create", false);
    Map<String, Object> parameters = new HashMap<>();

    try {
      myNode1.setCommand(ExportCalendar::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in ExportCalendar",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(PrintEventsFrom::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in PrintEventsFrom",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(PrintEventsOn::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in PrintEventsOn",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(ShowStatus::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in ShowStatus",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EasterEgg::new);
      myNode1.getExecutable(parameters, calendar, null);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EasterEgg",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }

    try {
      myNode1.setCommand(EasterEgg::new);
      myNode1.getExecutable(parameters, null, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Calendar or View is non-existent in EasterEgg",
          e.getMessage());
    } catch (Exception e) {
      assert false;
    }
  }

  @Test
  public void testA4() {
    calendar = new CalendarManager();
    calendar.createCalendar(this.parameters);

    try {
      inputString = "create event event1 on 2025-01-01";
      command = parser.parse(inputString, calendar, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("There is no active calendar in use.", e.getMessage());
    } catch (Exception e) {
      assert false;
    }
  }

  @Test
  public void testA5() {
    calendar = new CalendarManager();
    parameters.put("timezone", "SomeTimezone");
    try {
      inputString = "create calendar --name calA --timezone SomeTimezone";
      command = parser.parse(inputString, calendar, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid time zone format: SomeTimezone", e.getMessage());
    } catch (Exception e) {
      assert false;
    }
  }

  @Test
  public void testA6() {
    try {
      inputString = "create event event1 on 2025-01-01T09:00";
      command = parser.parse(inputString, calendar, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid date value format: 2025-01-01T09:00", e.getMessage());
    } catch (Exception e) {
      assert false;
    }
  }

  @Test
  public void testA7() {
    calendar = new CalendarManager();
    calendar.createCalendar(this.parameters);
    try {
      command = new CreateAllDayEvent(parameters, calendar, view);
      assert false;
    } catch (IllegalArgumentException e) {
      assertEquals("There is no active calendar in use.", e.getMessage());
    }
  }

  @Test
  public void testA8() {
    String fileContent = "create calendar --name \"Cal A\" --timezone \"America/New_York\""
        + System.lineSeparator() + "use calendar --name \"Cal A\"" + System.lineSeparator()
        + "create event \"First Team Meeting\" from 2025-07-01T09:00 to 2025-07-01T12:00"
        + System.lineSeparator() + "create event \"Second Team Meeting Series\" from "
        + "2025-07-02T10:00 to 2025-07-02T12:00 repeats MWF for 3 times" + System.lineSeparator()
        + "create event \"Third Team Meeting Series\" from 2025-07-03T10:00 to "
        + "2025-07-03T15:00 repeats TW until 2025-07-10" + System.lineSeparator()
        + "create event \"First Exam\" on 2025-08-01" + System.lineSeparator()
        + "create event \"Second Exam Series\" on 2025-08-02 repeats MWF for 3 times"
        + System.lineSeparator() + "create event \"Third Exam Series\" on 2025-08-03 repeats"
        + " TW until 2025-08-10" + System.lineSeparator()
        + "edit event start \"First Team Meeting\" from 2025-07-01T09:00 to 2025-07-01T12:00"
        + " with 2025-07-01T10:00" + System.lineSeparator()
        + "print events from 2025-07-01T09:00 to 2025-07-01T12:00" + System.lineSeparator()
        + "print events on 2025-07-01" + System.lineSeparator()
        + "create calendar --name \"Cal B\" --timezone \"America/Los_Angeles\""
        + System.lineSeparator() + "copy event \"First Team Meeting\" on 2025-07-01T10:00"
        + " --target \"Cal B\" to 2025-07-23T09:00" + System.lineSeparator()
        + "copy events between 2025-08-01 and 2025-08-20 --target \"Cal B\" to 2025-06-01"
        + System.lineSeparator() + "copy events on 2025-07-01 --target \"Cal B\" to 2025-07-03"
        + System.lineSeparator() + "show status on 2025-07-01T09:00" + System.lineSeparator()
        + "edit calendar --name \"Cal B\" --property name \"Cal 2\"" + System.lineSeparator()
        + "edit calendar --name \"Cal 2\" --property timezone \"America/New_York\""
        + System.lineSeparator()
        + "export cal \"validCommandsCSV.csv\"" + System.lineSeparator()
        + "export cal \"validCommandsICS.ics\"" + System.lineSeparator();
    try (BufferedWriter writer = new BufferedWriter(new FileWriter("testA8.txt"))) {
      writer.write(fileContent);
      assert true;
    } catch (IOException e) {
      assert false;
    }

    MockView2 view = new MockView2();
    CalendarManagerInterface calendar = new CalendarManager();
    try (BufferedReader reader = new BufferedReader(new FileReader("testA8.txt"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        command = parser.parse(line, calendar, view);
        command.execute();
      }
    } catch (IOException e) {
      System.out.println("Error reading file: " + e.getMessage());
    }

    String[] expectedOutputStringArray = ("Successfully created calendar Cal A in timezone "
        + "America/New_York" + System.lineSeparator()
        + "Set active calendar to Cal A" + System.lineSeparator()
        + "Using calendar Cal A: Successfully created a single event called First Team Meeting"
        + " (2025-07-01T09:00-04:00[America/New_York] -> 2025-07-01T12:00-04:00"
        + "[America/New_York])." + System.lineSeparator()
        + "Using calendar Cal A: Successfully created an event series called Second Team "
        + "Meeting Series (2025-07-02T10:00-04:00[America/New_York] -> "
        + "2025-07-02T12:00-04:00[America/New_York]) on every MWF days recurring 3 "
        + "times." + System.lineSeparator()
        + "Using calendar Cal A: Successfully created an event series called "
        + "Third Team Meeting Series (2025-07-03T10:00-04:00[America/New_York] "
        + "-> 2025-07-03T15:00-04:00[America/New_York]) on every TW days recurring "
        + "until 2025-07-10." + System.lineSeparator()
        + "Using calendar Cal A: Successfully created an all day event called "
        + "First Exam on 2025-08-01." + System.lineSeparator()
        + "Using calendar Cal A: Successfully created an all day event series called"
        + " Second Exam Series starting from 2025-08-02 on every MWF days recurring "
        + "3 times." + System.lineSeparator()
        + "Using calendar Cal A: Successfully created an all day event series called"
        + " Third Exam Series starting from 2025-08-03 on every TW days recurring until "
        + "2025-08-10." + System.lineSeparator()
        + "Using calendar Cal A: Successfully edited an event called First Team Meeting "
        + "(2025-07-01T09:00-04:00[America/New_York] -> 2025-07-01T12:00-04:00"
        + "[America/New_York]) and changed start to " + "2025-07-01T10:00-04:00[America/New_York]."
        + System.lineSeparator() + "Using calendar Cal A:" + System.lineSeparator()
        + "- First Team Meeting starting on 2025-07-01 at 10:00, ending on 2025-07-01 at 12:00"
        + System.lineSeparator() + "Using calendar Cal A:" + System.lineSeparator()
        + "- First Team Meeting starting on 2025-07-01 at 10:00, ending on 2025-07-01 at 12:00"
        + System.lineSeparator()
        + "Successfully created calendar Cal B in timezone America/Los_Angeles"
        + System.lineSeparator()
        + "Copied event First Team Meeting from Cal A on 2025-07-01T10:00-04:00"
        + "[America/New_York] to Cal B on 2025-07-23T09:00 of its' timezone."
        + System.lineSeparator() + "Copied events between 2025-08-01 and 2025-08-20 "
        + "from Cal A to Cal B on 2025-06-01 of its' timezone." + System.lineSeparator()
        + "Copied events on 2025-07-01 from Cal A to Cal B on 2025-07-03 of its' timezone."
        + System.lineSeparator() + "Using calendar Cal A: Available on 2025-07-01T09:00-04:00"
        + "[America/New_York]." + System.lineSeparator()
        + "Successfully edited calendar Cal B and changed property name to Cal 2."
        + System.lineSeparator() + "Successfully edited calendar Cal 2 and changed property "
        + "timezone to America/New_York." + System.lineSeparator()
        + "Exported events in calendar Cal A." + System.lineSeparator()
        + "Exported events in calendar Cal A."
        + System.lineSeparator()).split(System.lineSeparator());

    ArrayList<String> expectedOutput = new ArrayList<>(Arrays.asList(expectedOutputStringArray));

    ArrayList<String> actualOutput = view.getMessageArray();

    for (int i = 0; i < expectedOutput.size(); i++) {
      String a = expectedOutput.get(i);
      String b = actualOutput.get(i);
      assertEquals(a, b);
    }
    assert true;
  }
}
