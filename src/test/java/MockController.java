import calendar.controller.text.CalendarController;
import calendar.controller.text.CommandParser;
import calendar.model.CalendarModel;
import calendar.view.text.CalendarView;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that acts as a mock for the CalendarController to test the Runner.
 */
public class MockController extends CalendarController {
  private final List<String> commands = new ArrayList<>();

  /**
   * Constructor calls the super class to initialize the calendar controller.
   */
  public MockController() {
    super(new CalendarModel(), new CalendarView(), new CommandParser());
  }

  /**
   * Adds the inputs to a commands list.
   *
   * @param input input command string
   */
  @Override
  public void processCommand(String input) {
    commands.add(input);
  }

  /**
   * Returns the inputs given by test code.
   *
   * @return a list of string commands
   */
  public List<String> getCommands() {
    return commands;
  }
}

