import calendar.view.CalendarViewInterface;

/**
 * Class that acts as a mock for the CalendarView to test the Controller.
 */
public class MockView implements CalendarViewInterface {

  String message;

  /**
   * Gives back the message stored by the view given by the controller.
   *
   * @return a string message
   */
  public String getMessage() {
    return message;
  }

  @Override
  public void display(String message) {
    this.message = message;
  }

  @Override
  public void displayError(String message) {
    this.message = message;
  }
}
