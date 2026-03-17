# **Calendar Application**

## **Design Patterns Used**

1. **Model-View-Controller (MVC)**: Complete separation between business logic, presentation, and control flow

2. **Callback/Observer Pattern**: Features interface enables view to notify controller without tight coupling

3. **Command Pattern**: Text mode uses command pattern for extensible command processing with command factory

4. **Strategy Pattern**: Export uses different strategies (CsvExporter, IcsExporter) selected at runtime based on file extension

## **Design Changes**

### => **Architecture Changes**

1. **Input Handling Moved from Runner to Controller**

   Moved `interactiveMode()` and `headlessMode()` methods from `CalendarRunner` to `CalendarController`.<br/><br/>
   **Justification**: The Runner should only handle application initialization and routing. 
    Input handling belongs in the controller layer as per MVC principles.<br/><br/>

2. **New GUI Controller Package Created**

   Created `calendar.controller.gui` package containing `CalendarGuiController` and `Features` interface.<br/><br/>
   **Justification**: Separates GUI-specific controller logic from text-based controller logic, 
   allowing both interfaces to coexist without conflicts.<br/><br/>

3. **Features Interface Implementation (Callback Pattern)**

   Created `Features` interface extending `CalendarControllerInterface` with GUI-specific callback methods.<br/><br/>
   **Justification**: The view can invoke callbacks through this interface without knowing controller internals, following the Observer pattern and enabling loose coupling.<br/><br/>

4. **New GUI View Package Created**

   Created `calendar.view.gui` package with `CalendarGuiView` interface, `CalendarGuiViewImpl` main view, and 8 specialized dialog classes.<br/><br/>
   **Justification**: Houses all Swing-based GUI components separately from text views to allow independent development of each interface type.<br/><br/>

5. **Dialog-Based User Interaction System**

   Created 8 specialized dialog classes: `CreateCalendarDialog`, `EditCalendarDialog`, `CreateEventDialog`, `EditEventDialog`, `DayEventsDialog`, `AllEventsDialog`, `CopyEventDialog`, `EventDetailsDialog`.<br/><br/>
   **Justification**: Each dialog handles its own validation, data collection, and user feedback. Follows Single Responsibility Principle and makes the codebase more maintainable than a monolithic view class.<br/><br/>

6. **Exception Handling Consolidation**

   Controller methods throw exceptions instead of calling view.showError(). View layer catches exceptions and displays error dialogs.<br/><br/>
   **Justification**: Previously controller was calling show message functions in view. Now controller focuses on business logic while view handles all user feedback.

### => **No Changes to Model Layer**

`CalendarModel`, `CalendarManager`, Event classes (`SingleEvent`, `SeriesEvent`), and `IntervalTree` remain completely unchanged.<br/><br/>
**Justification**: The existing model architecture was well-designed and supports all required operations. Adding GUI is purely a presentation layer change requiring no business logic modifications.

## **Features Status**

### => **Fully Working Features**

#### **Text-Based Mode (Interactive & Headless)**
-  Create calendars with custom timezones
-  Switch between calendars
-  Edit calendar properties (name, timezone)
-  Create single events with all properties
-  Create recurring event series (weekday-based with N occurrences or until date)
-  Create all-day events (single and series)
-  Edit single event
-  Edit entire series
-  Edit series from specific date onwards
-  View events by date, by range, check availability
-  Copy events between calendars (single, on date, by range)
-  Export calendars in CSV and ICS formats

#### **GUI Mode**

**Calendar Management:**
-  Visual month-view calendar display with grid layout
-  Create multiple calendars with custom timezone selection
-  Edit calendar name and/or timezone through dialog
-  Switch between calendars using dropdown selector
-  Color-coded calendar display (6 distinct colors cycle through calendars)
-  Default calendar automatically created on startup
-  Calendar timezone displayed in header

**Event Creation:**
-  Single events with complete property support (subject, start, end, description, location, status)
-  Recurring events on specific weekdays (UMTWRFS)
-  Recurring events with N occurrences or until date options
-  All-day events (automatically set to 8am-5pm)
-  All-day recurring event series
-  Form validation with descriptive error messages
-  Pre-selected date when creating event from day cell click
-  Automatic end time suggestion (1 hour after start time)

**Event Viewing:**
-  Month view with interactive day cells
-  Day cells show up to 3 events with "+N more" indicator
-  Click day cells to view all events for that specific date
-  "All Events" view displaying entire calendar in table format
-  Event details dialog showing complete event information
-  Visual indicators for recurring events (R suffix)
-  Today's date highlighted in yellow
-  Time displayed in 12-hour format (h:mm a)

**Event Editing:**
-  Edit single event instance (removes from series if editing start time)
-  Edit entire series (all events updated)
-  Edit series from specific date onwards (splits series if editing start time)
-  Edit any property: subject, description, location, status, start, end
-  Property selection dropdown in edit dialog
-  Validation preventing invalid edits (duplicates, multi-day series)
-  Double-click event in list to view details before editing

**Event Copying:**
-  Copy single events to other calendars with new date/time
-  Copy all events from specific date to another date
-  Copy all events in date range to new starting date
-  Target calendar dropdown selector
-  Date and time picker for target datetime
-  Series relationships maintained in target calendar

**Calendar Export:**
-  Export to CSV format
-  Export to ICS format
-  File chooser dialog for filename/location selection
-  Automatic .csv extension appending if no extension provided
-  Validation preventing export of empty calendars

**Navigation:**
-  Previous month button
-  Next month button
-  Today button (returns to current month)
-  Month/year label display
-  Calendar automatically refreshes after operations