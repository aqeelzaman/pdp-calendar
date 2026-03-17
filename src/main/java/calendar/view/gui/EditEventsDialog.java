package calendar.view.gui;

import calendar.controller.gui.Features;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for editing all events with a given name starting from a specific date.
 */
class EditEventsDialog extends JDialog {

  private final Features features;
  private boolean modified = false;
  private final JPanel formPanel;

  private JTextField eventNameField;
  private JSpinner startDateSpinner;
  private JComboBox<String> propertyCombo;
  private JPanel valuePanel;
  private JTextField valueField;
  private JSpinner dateSpinner;
  private JSpinner timeSpinner;

  /**
   * Constructor to initialize the edit events dialogue box.
   *
   * @param parent   the caller JFrame who called this dialogue box
   * @param features the interface defining all callbacks
   */
  public EditEventsDialog(JFrame parent, Features features) {
    super(parent, "Edit Events From Date", true);
    this.features = features;

    setSize(450, 400);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    formPanel = new JPanel();
    formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
    initializeUi();
  }

  /**
   * Create the UI for edit event dialogue box.
   */
  private void initializeUi() {
    JLabel titleLabel = new JLabel("Edit all events with same name from a date");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(titleLabel);
    formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

    eventName();
    startFrom();
    propertySelection();
    newValue();
    noteLabel();

    add(formPanel, BorderLayout.CENTER);
    buttons();
  }

  /**
   * Panel to show the event name to search for.
   */
  private void eventName() {
    JLabel nameLabel = new JLabel("Event Name:*");
    nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(nameLabel);

    eventNameField = new JTextField(20);
    eventNameField.setMaximumSize(new Dimension(300, 25));
    eventNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(eventNameField);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  /**
   * Panel to show the start date of events to edit.
   */
  private void startFrom() {
    JLabel dateLabel = new JLabel("Start From Date:*");
    dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(dateLabel);

    javax.swing.SpinnerDateModel dateModel = new javax.swing.SpinnerDateModel(
        new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
    startDateSpinner = new JSpinner(dateModel);
    JSpinner.DateEditor dateEditor =
        new JSpinner.DateEditor(startDateSpinner, "MM/dd/yyyy");
    startDateSpinner.setEditor(dateEditor);
    startDateSpinner.setMaximumSize(new Dimension(150, 25));
    startDateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

    formPanel.add(startDateSpinner);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  /**
   * Panel that shows the property to edit.
   */
  private void propertySelection() {
    JLabel propertyLabel = new JLabel("Property to edit:*");
    propertyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(propertyLabel);

    propertyCombo = new JComboBox<>(new String[] {
        "--select property--", "subject", "description", "location", "status", "start", "end"
    });
    propertyCombo.setMaximumSize(new Dimension(200, 25));
    propertyCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

    propertyCombo.addActionListener(e -> {
      String selected = (String) propertyCombo.getSelectedItem();
      updateValuePanel(selected);
    });

    formPanel.add(propertyCombo);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  /**
   * Panel to show the new value of the property.
   */
  private void newValue() {
    JLabel valueLabel = new JLabel("New value:*");
    valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(valueLabel);

    valuePanel = new JPanel();
    valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.X_AXIS));
    valuePanel.setMaximumSize(new Dimension(400, 30));
    valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    formPanel.add(valuePanel);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  /**
   * Updates the value panel based on selected property.
   */
  private void updateValuePanel(String property) {
    valuePanel.removeAll();

    if ("start".equals(property) || "end".equals(property)) {
      java.util.Calendar cal = java.util.Calendar.getInstance();

      javax.swing.SpinnerDateModel dateModel = new javax.swing.SpinnerDateModel(
          cal.getTime(), null, null, java.util.Calendar.DAY_OF_MONTH);
      dateSpinner = new JSpinner(dateModel);
      javax.swing.JSpinner.DateEditor dateEditor =
          new javax.swing.JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy");
      dateSpinner.setEditor(dateEditor);
      dateSpinner.setMaximumSize(new Dimension(120, 25));

      javax.swing.SpinnerDateModel timeModel = new javax.swing.SpinnerDateModel(
          cal.getTime(), null, null, java.util.Calendar.MINUTE);
      timeSpinner = new JSpinner(timeModel);
      javax.swing.JSpinner.DateEditor timeEditor =
          new javax.swing.JSpinner.DateEditor(timeSpinner, "HH:mm");
      timeSpinner.setEditor(timeEditor);
      timeSpinner.setMaximumSize(new Dimension(80, 25));

      valuePanel.add(dateSpinner);
      valuePanel.add(Box.createRigidArea(new Dimension(5, 0)));
      valuePanel.add(timeSpinner);
    } else if (!"--select property--".equals(property)) {
      valueField = new JTextField(20);
      valueField.setMaximumSize(new Dimension(300, 25));
      valuePanel.add(valueField);
    }

    valuePanel.revalidate();
    valuePanel.repaint();
  }

  /**
   * Panel with note about the operation.
   */
  private void noteLabel() {
    JLabel noteLabel = new JLabel(
        "<html><i>This will edit the selected property for all events<br>"
            + "with the given name starting from the specified date onwards</i></html>");
    noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(noteLabel);
  }

  /**
   * Panel to add save and cancel buttons.
   */
  private void buttons() {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> saveEdits());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * Action method that calls the edit feature.
   */
  private void saveEdits() {
    String eventName = eventNameField.getText().trim();
    String property = (String) propertyCombo.getSelectedItem();

    if (eventName.isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Event name is required", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (property == null || "--select property--".equals(property)) {
      JOptionPane.showMessageDialog(this,
          "Property selection is required", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      java.util.Date dateValue = (java.util.Date) startDateSpinner.getValue();

      java.time.LocalDate date = dateValue.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

      ZonedDateTime startDateTime = date.atStartOfDay(
          java.time.ZoneId.of(features.getCurrentCalendarTimezone()));

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("subject", eventName);
      parameters.put("start", startDateTime);
      parameters.put("property", property);
      parameters.put("value", getConvertedValue(property));

      features.editEventsWithName(parameters);

      modified = true;
      JOptionPane.showMessageDialog(this,
          "Events updated successfully!", "Success",
          JOptionPane.INFORMATION_MESSAGE);
      dispose();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
          "Error updating events: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Convert the value to the correct datatype for editing.
   *
   * @param property the property to edit
   * @return the value which is converted to the datatype
   */
  private Object getConvertedValue(String property) {
    if (property.equals("start") || property.equals("end")) {
      if (dateSpinner == null || timeSpinner == null) {
        throw new IllegalArgumentException("Date/time pickers not initialized");
      }

      java.util.Date dateValue = (java.util.Date) dateSpinner.getValue();
      java.util.Date timeValue = (java.util.Date) timeSpinner.getValue();

      java.time.LocalDate date = dateValue.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
      java.time.LocalTime time = timeValue.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalTime();

      return ZonedDateTime.of(date, time,
          java.time.ZoneId.of(features.getCurrentCalendarTimezone()));
    } else {
      if (valueField == null) {
        throw new IllegalArgumentException("Value field not initialized");
      }
      String valueText = valueField.getText().trim();
      if (valueText.isEmpty()) {
        throw new IllegalArgumentException("Value cannot be empty");
      }
      return valueText;
    }
  }

  /**
   * Method to see if the property is modified.
   */
  public boolean wasModified() {
    return modified;
  }
}