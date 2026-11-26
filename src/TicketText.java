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
    private final String title;
    private final String footer;
    private int width = 40; // default, updated dynamically

    BorderDecorator(TicketText inner, String title, String footer) {
        super(inner, null);
        this.title = title;
        this.footer = footer;
    }

    /** Sets the line width (call before render). */
    void setWidth(int width) {
        this.width = Math.max(20, width);
    }

    public String render() {
        String line = repeat("=", width) + "\n";
        String sep = repeat("-", width) + "\n";
        return line + center("CRA2 PARKING LOT") + center(title) + line
                + inner.render()
                + sep
                + wrapFooter(footer) + "\n"
                + line;
    }

    private String center(String text) {
        int pad = Math.max(0, (width - text.length()) / 2);
        return repeat(" ", pad) + text + "\n";
    }

    private String wrapFooter(String text) {
        if (text.length() <= width) return text;
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        int lineLen = 0;
        for (String w : words) {
            if (lineLen + w.length() + 1 > width) {
                sb.append("\n");
                lineLen = 0;
            } else if (lineLen > 0) {
                sb.append(" ");
                lineLen++;
            }
            sb.append(w);
            lineLen += w.length();
        }
        return sb.toString();
    }

    private String repeat(String s, int n) {
        return new String(new char[n]).replace("\0", s);
    }
}

/** Builds the two kinds of printout by wrapping decorators. */
class Tickets {
    static TicketText ticketFor(ParkingRecord record, int width) {
        TicketText ticket = new BasicTicket(record);
        ticket = new TimeInDecorator(ticket, record);
        BorderDecorator bd = new BorderDecorator(ticket, "PARKING TICKET", " Keep this ticket. Present it\n when leaving the parking lot.");
        bd.setWidth(width);
        return bd;
    }

    static TicketText receiptFor(ParkingRecord record, int width) {
        TicketText receipt = new BasicTicket(record);
        receipt = new TimeInDecorator(receipt, record);
        receipt = new TimeOutDecorator(receipt, record);
        receipt = new FeeDecorator(receipt, record);
        BorderDecorator bd = new BorderDecorator(receipt, "PARKING RECEIPT", "   Thank you! Drive safely.");
        bd.setWidth(width);
        return bd;
    }
}
