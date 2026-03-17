import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.junit.Before;
import org.junit.Test;
/*
/**
 * Tests to verify the CalendarRunner.
 *
public class RunnerTest {

  private MockController controller;
  private CalendarRunner runner;
  private ByteArrayInputStream testIn;
  private ByteArrayOutputStream testOut;
  private ByteArrayOutputStream errContent;

  /**
   * Setting up the runner and a mock controller.
   *
  @Before
  public void setUp() {
    this.controller = new MockController();
    this.runner = new CalendarRunner();
    this.testOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(testOut));
    this.errContent = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errContent));
  }

  @Test
  public void testA1() {
    String simulatedInput = "create event \"Meeting\"\nshow status\n    \nexit\n";
    testIn = new ByteArrayInputStream(simulatedInput.getBytes());
    System.setIn(testIn);

    runner.runInInteractiveMode(controller);

    assertEquals(2, controller.getCommands().size());
    assertEquals("create event \"Meeting\"", controller.getCommands().get(0));
    assertEquals("show status", controller.getCommands().get(1));

    String output = testOut.toString();
    assertTrue(output.contains("Enter command"));
    assertTrue(output.contains("Exit command encountered"));
  }

  @Test
  public void testA2() {

    File tempFile = null;
    try {
      tempFile = File.createTempFile("testCommands", ".txt");
    } catch (Exception e) {
      assert false;
    }

    try (PrintWriter writer = new PrintWriter(tempFile)) {
      writer.println("create event \"Demo\"");
      writer.println("show status");
      writer.println("   ");
      writer.println("exit  ");
    } catch (Exception e) {
      assert false;
    }

    runner.runInHeadlessMode(controller, "wrongPath.txt");

    String output = errContent.toString();
    assertTrue(output.contains("No such file or directory"));

    runner.runInHeadlessMode(controller, tempFile.getAbsolutePath());

    assertEquals(2, controller.getCommands().size());
    assertEquals("create event \"Demo\"", controller.getCommands().get(0));
    assertEquals("show status", controller.getCommands().get(1));

    output = testOut.toString();
    assertTrue(output.contains("Exit command encountered"));
  }


  @Test
  public void testA3() {

    File tempFile = null;
    try {
      tempFile = File.createTempFile("testCommands", ".txt");
    } catch (Exception e) {
      assert false;
    }

    try (PrintWriter writer = new PrintWriter(tempFile)) {
      writer.println("create event \"Demo\"");
      writer.println("show status");
      writer.println("   ");
    } catch (Exception e) {
      assert false;
    }

    runner.runInHeadlessMode(controller, tempFile.getAbsolutePath());

    assertEquals(2, controller.getCommands().size());
    assertEquals("create event \"Demo\"", controller.getCommands().get(0));
    assertEquals("show status", controller.getCommands().get(1));
  }

  @Test
  public void testA4() {
    String simulatedInput = "exit";
    testIn = new ByteArrayInputStream(simulatedInput.getBytes());
    System.setIn(testIn);
    runner.run(new String[] {"--mode", "interactive"});
    runner.run(new String[] {"--mode", "headless", "testCommands.txt"});

    runner.run(new String[] {"--mode"});
    runner.run(new String[] {"test"});
    runner.run(new String[] {"test", "testAgain"});
    runner.run(new String[] {"--mode", "headless"});
    runner.run(new String[] {"--mode", "something", "testCommands.txt"});
    runner.run(new String[] {"--mode", "something"});
  }
  *
}*/
