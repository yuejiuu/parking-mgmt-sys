# CRA2 Parking Management System

**Group 1**

```
Aguanta, Job Marco Angelus G.
Baran, Antonio Ymanuel A.
Pesirla, Adrian Laurence R.
Villaflor, Inigo Dominique
Villarta, John Lhendon P.
```

A parking lot management system built with Java Swing and the IntelliJ GUI Designer. Each vehicle is assigned a slot and receives a numbered parking ticket on entry, and the lot can be viewed on a color-coded slot map.

> **Status: work in progress (about half done).** Parking, ticketing, the slot map, navigation and error handling are working. Exit, payment and parking fees are still being built.

## Features

### Navigation
- **Entry** page: the vehicle entry form and the parking ticket.
- **Slot Map** page: a 4x4 map of all 16 parking slots.
- The selected page's button is highlighted, and you can switch between the two pages at any time.
- **Exit** is shown but disabled ("Coming soon") until the exit feature is finished.

### Vehicle entry
- Enter a plate number and choose the vehicle type (Car, Motorcycle, SUV / Van, Truck).
- The vehicle is automatically assigned the first free slot, and the slot (e.g. `B2`) is shown right away.
- Press **Enter** or click **Park Vehicle** to park.

### Parking ticket
- A ticket is printed in the ticket area for every vehicle parked.
- It includes the ticket number (T-0001, T-0002, ...), plate number, vehicle type, slot and time in.

### Slot map
- Free slots are **green** and taken slots are **red**, and a taken slot shows the plate number parked in it.
- Click any slot to see its details (plate, vehicle type, time in).

### Error handling
Errors and warnings are shown with `JOptionPane` dialogs:
- Empty plate number
- Invalid plate format (4 to 10 letters or numbers, e.g. `ABC 1234`)
- Vehicle already parked (duplicate plate)
- Parking lot full

### Design
- Navy header with a live date and clock.
- Color-coded buttons, cards, slot map and a paper-style ticket area.

## How it works

```
Vehicle Enters -> Plate Validation -> Slot Assignment -> Ticket Printed -> Slot Map Updated
```

## Planned features
- Exit and check-out of parked vehicles
- Parking fee computed from the hours parked (rounded up, minimum 1 hour) multiplied by the hourly rate
- Payment and receipt
- Saving data to a file or database
- Reports
