import java.util.Date;

/** Everything we know about one parked vehicle (used for the ticket and the receipt). */
public class ParkingRecord {
    private final String ticketNo;
    private final Vehicle vehicle;
    private final String slot;
    private final Date timeIn;

    // filled in when the vehicle leaves
    private Date timeOut;
    private long minutes;
    private long hours;
    private long fee;

    public ParkingRecord(String ticketNo, Vehicle vehicle, String slot, Date timeIn) {
        this.ticketNo = ticketNo;
        this.vehicle = vehicle;
        this.slot = slot;
        this.timeIn = timeIn;
    }

    public void checkOut(Date timeOut, long minutes, long hours, long fee) {
        this.timeOut = timeOut;
        this.minutes = minutes;
        this.hours = hours;
        this.fee = fee;
    }

    public String getTicketNo() { return ticketNo; }
    public Vehicle getVehicle() { return vehicle; }
    public String getSlot() { return slot; }
    public Date getTimeIn() { return timeIn; }
    public Date getTimeOut() { return timeOut; }
    public long getMinutes() { return minutes; }
    public long getHours() { return hours; }
    public long getFee() { return fee; }
}
