# CRA2 Parking Management System

**Group 1**

```
Aguanta, Job Marco Angelus G.
Baran, Antonio Ymanuel A.
Pesirla, Adrian Laurence R.
Villaflor, Inigo Dominique
Villarta, John Lhendon P.
```

A parking lot management system built with Java Swing and the IntelliJ GUI Designer. Each vehicle is checked, assigned a slot and given a numbered parking ticket on entry. When it leaves, the fee is computed and a receipt is printed. The whole lot can be viewed on a color-coded slot map.

> **Status:** parking, ticketing, check-out with fee and receipt, the slot map, navigation and error handling are working. Payment handling, saving data and reports are still planned.

## How to run

1. Open the project in IntelliJ IDEA.
2. Run the `clone` class (it holds the `main` method and the window). Its layout is in `src/clone.form`.

Run it from IntelliJ: the form is compiled by the IDE's GUI Designer, so building it with plain `javac` will not work.

## Features

### Navigation
- **Entry** page: the vehicle entry form and the ticket / receipt area.
- **Slot Map** page: a 4x4 map of all 16 parking slots.
- The selected page's button is highlighted, and you can switch between the two pages at any time.
- **Exit** (red button) checks a vehicle out.

### Vehicle entry
- Enter a plate number and choose the vehicle type (Car, Motorcycle, SUV / Van, Truck).
- The vehicle is automatically assigned the first free slot, and the slot (e.g. `B2`) is shown right away.
- Press **Enter** or click **Park Vehicle** to park.

### Plate number checker
Each vehicle type knows its own Philippine plate rule. Spaces and dashes are ignored, letters are made uppercase, and only letters and numbers are allowed.

| Vehicle type | Rule | Example |
|---|---|---|
| Car | 3 letters + 4 digits (7 characters, any order) | `ABC 1234` |
| SUV / Van | 3 letters + 4 digits (7 characters, any order) | `ABC 1234` |
| Truck | 3 letters + 4 digits (7 characters, any order) | `ABC 1234` |
| Motorcycle | 3 letters + 3 digits (6 characters, any order) | `ABC 123`, `GA660C` |

- **Live check:** the plate box turns light green when the plate is valid and light red when it is not. Hover over the box to see the rule for the chosen vehicle type.
- If a plate is rejected, the message says how many letters and digits were entered and what is required.

### Parking ticket
- A ticket is printed in the ticket area for every vehicle parked.
- It includes the ticket number (T-0001, T-0002, ...), plate number, vehicle type, slot and time in.

### Exit, fee and receipt
- Click **Exit**, pick the vehicle that is leaving from the list of parked plates (sorted A-Z) and confirm. You can also click a taken slot on the slot map and choose to check that vehicle out.
- The slot is freed and a receipt is printed on the Entry page with the time in, time out, duration, billed hours and the total.
- **Fee** = billed hours x hourly rate. Hours are rounded up with a minimum of 1 hour.

| Vehicle type | Rate per hour |
|---|---|
| Motorcycle | PHP 20 |
| Car | PHP 40 |
| SUV / Van | PHP 50 |
| Truck | PHP 80 |

### Slot map
- Slots are named across the rows: `A1 B1 C1 D1` / `A2 B2 C2 D2` / ... / `A4 B4 C4 D4`.
- Free slots are **green** and taken slots are **red**. A taken slot shows a small picture of the vehicle type and the plate number parked in it.
- Click any slot to see its details (plate, vehicle type, time in) or to check the vehicle out.

### Error handling
Errors and warnings are shown with `JOptionPane` dialogs:
- Empty plate number
- Invalid plate number for the chosen vehicle type
- Vehicle already parked (duplicate plate)
- Parking lot full
- Vehicle not found when checking out

### Design
- Navy header with a live date and clock.
- Color-coded buttons, cards, slot map and a paper-style ticket area.
- Vehicle pictures are drawn with Java2D (`VehicleIcon`), so the project needs no image files.

## How it works

```
Entry: Vehicle Enters -> Plate Validation -> Slot Assignment -> Ticket Printed -> Slot Map Updated
Exit:  Vehicle Selected -> Fee Computed -> Slot Freed -> Receipt Printed -> Slot Map Updated
```

## Design patterns

| Pattern | Type | Where | What it does |
|---|---|---|---|
| Factory Method | Creational | `VehicleCreator` and its subclasses | Creates the `Car`, `Motorcycle`, `SuvVan` or `Truck` object. Adding a new vehicle type needs one new `Vehicle` class and one new creator. |
| Facade | Structural | `ParkingFacade` | The only class the GUI talks to. It hides the slot manager, fee calculator, validation rules and ticket numbering. |
| Decorator | Structural | `TicketText` and its decorators | Builds the ticket and the receipt by wrapping a `BasicTicket` with time, fee and border decorators. |

The full class diagram is in `src/class_diagram.mermaid`.

## Project structure

| File | Role |
|---|---|
| `clone.java` / `clone.form` | Main window (GUI only) and the program entry point |
| `ParkingFacade.java` | Facade: park, check out and look up vehicles |
| `SlotManager.java` | Tracks which plate is in which slot |
| `FeeCalculator.java` | Minutes parked, billed hours and the fee |
| `ParkingRecord.java` | Everything about one parked vehicle (used by the ticket and receipt) |
| `ParkingException.java` | Error type that the GUI shows in a dialog |
| `Vehicle.java` | Vehicle base class, the plate checker, and the four vehicle types |
| `VehicleCreator.java` | Factory Method creators for each vehicle type |
| `TicketText.java` | Decorator classes for the ticket and receipt |
| `VehicleIcon.java` | The vehicle pictures on the slot map |

## Planned features
- Payment handling (the receipt shows the total, but no payment is recorded yet)
- Saving data to a file or database (everything is cleared when the program closes)
- Reports
