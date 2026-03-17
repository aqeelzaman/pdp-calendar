import calendar.view.CalendarViewInterface;
import java.util.ArrayList;

/**
 * Class that acts as a mock for the CalendarView to test the Controller.
 */
public class MockView2 implements CalendarViewInterface {
  public ArrayList<String> messageArray = new ArrayList<>();
  public String message;

  /**
   * Gives back the message stored by the view given by the controller.
   *
   * @return a string message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Gives back all the messages stored by the view given by the controller.
   *
   * @return a string message array
   */
  public ArrayList<String> getMessageArray() {
    return messageArray;
  }

  @Override
  public void display(String message) {
    this.messageArray.add(message);
    this.message = message;
  }

  @Override
  public void displayError(String message) {
    this.messageArray.add(message);
    this.message = message;
  }
}
