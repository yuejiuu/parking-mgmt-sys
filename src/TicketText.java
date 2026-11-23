import java.text.SimpleDateFormat;

/**
 * DECORATOR pattern (structural).
 * A ticket starts as a BasicTicket and is wrapped by decorators that each add more lines.
 * The ticket and the receipt are built from the same pieces:
 *
 *   ticket  = Border( TimeIn( Basic ) )
 *   receipt = Border( Fee( TimeOut( TimeIn( Basic ) ) ) )
 */
public interface TicketText {
    String render();
}

/** The plain ticket: ticket number, plate, type and slot. */
class BasicTicket implements TicketText {
    private final ParkingRecord record;

    BasicTicket(ParkingRecord record) {
        this.record = record;
    }

    public String render() {
        return "Ticket No : " + record.getTicketNo() + "\n"
                + "Plate     : " + record.getVehicle().getPlate() + "\n"
                + "Type      : " + record.getVehicle().getType() + "\n"
                + "Slot      : " + record.getSlot() + "\n";
    }
}

/** Base class of all decorators: it holds the ticket that it wraps. */
abstract class TicketDecorator implements TicketText {
    protected final TicketText inner;
    protected final ParkingRecord record;
    protected final SimpleDateFormat time = new SimpleDateFormat("hh:mm a");

    TicketDecorator(TicketText inner, ParkingRecord record) {
        this.inner = inner;
        this.record = record;
    }
}

class TimeInDecorator extends TicketDecorator {
    TimeInDecorator(TicketText inner, ParkingRecord record) {
        super(inner, record);
    }

    public String render() {
        return inner.render() + "Time In   : " + time.format(record.getTimeIn()) + "\n";
    }
}

class TimeOutDecorator extends TicketDecorator {
    TimeOutDecorator(TicketText inner, ParkingRecord record) {
        super(inner, record);
    }

    public String render() {
        return inner.render()
                + "Time Out  : " + time.format(record.getTimeOut()) + "\n"
                + "Duration  : " + record.getMinutes() + " min\n";
    }
}

class FeeDecorator extends TicketDecorator {
    FeeDecorator(TicketText inner, ParkingRecord record) {
        super(inner, record);
    }

    public String render() {
        return inner.render()
                + "------------------------------\n"
                + "Billed    : " + record.getHours() + " hr x PHP " + record.getVehicle().getHourlyRate() + "\n"
                + "TOTAL     : PHP " + record.getFee() + "\n";
    }
}

/** Adds the header (lot name and title) and the footer message. */
class BorderDecorator extends TicketDecorator {
    private static final String LINE = "==============================\n";
    private final String title;
    private final String footer;

    BorderDecorator(TicketText inner, String title, String footer) {
        super(inner, null);
        this.title = title;
        this.footer = footer;
    }

    public String render() {
        return LINE + center("CRA2 PARKING LOT") + center(title) + LINE
                + inner.render()
                + "------------------------------\n"
                + footer + "\n"
                + LINE;
    }

    private String center(String text) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < (30 - text.length()) / 2; i++) {
            spaces.append(' ');
        }
        return spaces + text + "\n";
    }
}

/** Builds the two kinds of printout by wrapping decorators. */
class Tickets {
    static TicketText ticketFor(ParkingRecord record) {
        TicketText ticket = new BasicTicket(record);
        ticket = new TimeInDecorator(ticket, record);
        return new BorderDecorator(ticket, "PARKING TICKET", " Keep this ticket. Present it\n when leaving the parking lot.");
    }

    static TicketText receiptFor(ParkingRecord record) {
        TicketText receipt = new BasicTicket(record);
        receipt = new TimeInDecorator(receipt, record);
        receipt = new TimeOutDecorator(receipt, record);
        receipt = new FeeDecorator(receipt, record);
        return new BorderDecorator(receipt, "PARKING RECEIPT", "   Thank you! Drive safely.");
    }
}
