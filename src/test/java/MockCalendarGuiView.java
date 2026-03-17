import calendar.controller.gui.Features;
import calendar.view.gui.CalendarGuiView;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of CalendarGuiView for testing purposes.
 * Tracks method invocations and stores messages to verify controller behavior.
 */
public class MockCalendarGuiView implements CalendarGuiView {

  String lastMessage = "";
  String lastError = "";
  boolean guiDisplayed = false;
  int refreshCount = 0;

  private Features features;
  private final List<String> displayedMessages;
  private final List<String> errorMessages;
  private boolean calendarListUpdated;

  /**
   * Constructs a new mock view with empty state.
   */
  public MockCalendarGuiView() {
    this.displayedMessages = new ArrayList<>();
    this.errorMessages = new ArrayList<>();
    this.guiDisplayed = false;
    this.calendarListUpdated = false;
    this.refreshCount = 0;
  }

  @Override
  public void setFeatures(Features features) {
    this.features = features;
  }

  @Override
  public void displayGui() {
    this.guiDisplayed = true;
  }

  @Override
  public void refreshCalendarView() {
    this.refreshCount++;
  }

  @Override
  public void updateCalendarList() {
    this.calendarListUpdated = true;
    this.refreshCount++;
  }

  @Override
  public void display(String message) {
  }

  @Override
  public void displayError(String message) {
    this.lastError = message;
    this.errorMessages.add(message);
  }

  @Override
  public void showMessage(String message) {
    this.lastMessage = message;
    this.displayedMessages.add(message);
  }

  @Override
  public void showError(String errorMessage) {
    this.lastError = errorMessage;
    this.errorMessages.add(errorMessage);
  }

  @Override
  public boolean showConfirmation(String message) {
    return true;
  }

  /**
   * Returns a copy of all messages displayed to the user.
   *
   * @return list of displayed messages
   */
  public List<String> getDisplayedMessages() {
    return new ArrayList<>(displayedMessages);
  }

  /**
   * Returns a copy of all error messages shown to the user.
   *
   * @return list of error messages
   */
  public List<String> getErrors() {
    return new ArrayList<>(errorMessages);
  }

  /**
   * Checks whether the GUI was displayed.
   *
   * @return true if displayGui was called
   */
  public boolean wasGuiDisplayed() {
    return guiDisplayed;
  }

  /**
   * Checks whether the calendar view was refreshed at least once.
   *
   * @return true if refreshCalendarView was called
   */
  public boolean wasCalendarViewRefreshed() {
    return refreshCount > 0;
  }

  /**
   * Checks whether the calendar list was updated.
   *
   * @return true if updateCalendarList was called
   */
  public boolean wasCalendarListUpdated() {
    return calendarListUpdated;
  }

  /**
   * Returns the features controller set for this view.
   *
   * @return the features object
   */
  public Features getFeatures() {
    return features;
  }

  /**
   * Resets all tracking state to initial values.
   */
  public void reset() {
    lastMessage = "";
    lastError = "";
    displayedMessages.clear();
    errorMessages.clear();
    guiDisplayed = false;
    refreshCount = 0;
    calendarListUpdated = false;
  }
}