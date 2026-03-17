# Calendar Application - User Guide

## Running the Application

### GUI Mode (Default)

To launch the graphical user interface, run:

java -jar build/libs/calendar.jar

Or double-click the JAR file in your file explorer.

### Interactive Text Mode

To run in interactive mode where you can type commands one at a time:

java -jar build/libs/calendar.jar --mode interactive

Then enter commands, example:
-> create calendar --name Work --timezone America/New_York
-> use calendar --name Work
-> create event Team Meeting from 11/18/2025T14:00 to 11/18/2025T15:00
-> exit

### Headless/Script Mode

To execute a script file containing commands:

java -jar build/libs/calendar.jar --mode headless path/to/script.txt

Example script file (`script.txt`):
```
create calendar --name Work --timezone America/New_York
use calendar --name Work
create event Team Meeting from 11/18/2025T14:00 to 11/18/2025T15:00
create event Daily Standup from 11/18/2025T09:00 to 11/18/2025T09:15 repeats MO,TU,WE,TH,FR for 20 times
print events on 11/18/2025
```

---

## Using the GUI

### Getting Started

When you launch the GUI, you'll see:
- **Calendar grid** showing the current month
- **Calendar name** at the top (default: "Default Calendar")
- **Timezone** at the top (default: "(America/New_York)")
- **Navigation controls** to move between months
- **Today button** to move back to today's date
- **Sidebar** with action buttons


### Creating a New Calendar

1. Click the **"New Calendar"** button in the sidebar
2. Enter a calendar name
3. Select a timezone from the dropdown or type one (e.g., "America/New_York")
4. Click **"Create"**

The new calendar appears in the calendar switcher dropdown.


### Editing an Existing Calendar

1. Click the **"Edit Calendar"** button in the sidebar
2. Select the calendar you want to edit from the dropdown
3. Choose what to change:
   - **New Name**: Enter a new name for the calendar (leave empty to keep current)
   - **New Timezone**: Select or type a new timezone (leave empty to keep current)
4. Click **"Save"**

**Note:** You must provide at least one new value (name or timezone)

### Switching Between Calendars

- Use the **calendar dropdown** at the top of the window
- Select the calendar you want to view
- The calendar name label will update with the calendar's color

Each calendar has a unique color to help you distinguish between them.


### Viewing Events

#### Month View
- The calendar grid shows all days of the current month
- Events appear as bullet points under each day
- If there are more than 3 events, you'll see "- +N more"
- Today's date is highlighted in light yellow

#### Viewing Events for a Specific Day
1. **Click on any day** in the calendar grid
2. A dialog opens showing all events for that day
3. Each event displays:
   - Start and end times
   - Event subject
   - "(Recurring)" label if part of a series

#### Viewing All Events
1. Click **"View All Events"** in the sidebar
2. A table shows all events in the current calendar
3. Columns: Date, Time, Subject, Description, Location, Status
4. "(R)" indicates recurring events

#### Viewing Event Details
1. From the day events dialog, **double-click any event**
2. A detailed view opens showing:
   - Event subject (title)
   - Full date and day of week
   - Complete time range
   - Timezone information
   - Description (if provided)
   - Location (physical/online)
   - Status (public/private)
   - Event type (single or recurring)
   - Series ID (for recurring events)
3. Click **"Close"** to return to the event list


### Creating Events

#### Creating a Single Event
1. Click **"Create Event"** in the sidebar (or click on a day)
2. Fill in the event details:
   - **Subject*** (required): Event name
   - **Date*** (required): Event date (MM/DD/YYYY)
   - **All-day event**: Check if event is 8:00 AM - 5:00 PM
   - **Start Time*** (required if not all-day): Event start time
   - **End Time*** (required if not all-day): Event end time
   - **Description**: Optional event details
   - **Location**: "physical" or "online"
   - **Status**: "public" or "private"
3. Click **"Create"**

#### Creating a Recurring Event
1. Follow steps 1-2 above
2. Check **"Recurring event"**
3. Select weekdays when event repeats (e.g., Mon, Wed, Fri)
4. Choose termination:
   - **For N occurrences**: Specify number of times to repeat
   - **Until date**: Specify end date
5. Click **"Create"**

### All-Day Events
- Automatically set to 8:00 AM - 5:00 PM
- Perfect for holidays, birthdays, vacation days


### Editing Events

1. **Option A:** Click a day → Click event in list → Click "Edit Selected"
2. **Option B:** View All Events → Select event → Click "Edit Selected"
3. If the event is recurring, choose:
   - **"This event only"**: Edit just this occurrence
   - **"All events in series"**: Edit all occurrences
   - **"This and following events"**: Edit from this date forward
4. Select property to edit:
   - subject, description, location, status, start, end
5. Enter new value
6. Click **"Save"**

### Event Series Management
- Edit single occurrence: Changes only that instance
- Edit series: Changes all occurrences
- Edit from date: Splits series into two

**Important:** If you change the start time of a single occurrence in a series, it will be removed from the series and become a standalone event.


### Copying Events

#### Copy Event to Another Calendar
1. Click a day to view events
2. Select the event you want to copy
3. Click **"Copy Selected"**
4. Choose:
   - **Target Calendar**: Destination calendar
   - **Target Date**: New date for the event
   - **Target Time**: New start time
5. Click **"Copy"**

#### Copy Events On A Particulr Day To Another Calendar
1. Click a day to view events
2. Click **"Copy Shown Events"**
3. Choose:
   - **Target Calendar**: Destination calendar
   - **Target Date**: New date for the event
4. Click **"Copy All"**

#### Copy Event Between Two Dates to Another Calendar
1. On the side panel, click **"Copy Events"**
4. Choose:
   - **Start Date**: The start date of event selection
   - **End Date**: The end date of event selection
   - **Target Calendar**: Destination calendar
   - **Target Start Date**: New start date for the events
5. Click **"Copy Events"**

The events are duplicated to the target calendar with the new date/time, adjusted for the target calendar's timezone.


### Navigating the Calendar

- **Previous/Next buttons**: Move backward/forward one month
- **Today button**: Return to current month
- **Calendar grid**: Click any day to view/create events


### Exporting Calendar

1. Click **"Export Calendar"** in the sidebar
2. Choose save location
3. Enter filename (will auto-add .csv if not specified)
4. Click **"Save"**

Exports all events in the current calendar to CSV format.


### Exiting the Application

1. Click **"Exit"** in the sidebar
2. Confirm you want to exit
3. The application will close

Alternatively, you can close the window using the standard window close button (X).

---