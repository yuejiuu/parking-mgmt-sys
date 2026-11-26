import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * FACADE pattern (structural).
 * The GUI talks only to this class. It hides the slot manager, the fee calculator,
 * the validation rules and the ticket numbering behind a few simple methods.
 */
public class ParkingFacade {
    private final SlotManager slots = new SlotManager(16);
    private final FeeCalculator fees = new FeeCalculator();
    private final Map<String, ParkingRecord> parked = new HashMap<>();
    private int ticketCount = 0;
    private long timeOffsetMillis = 0L;

    /** Parks the vehicle in the first free slot, or throws a ParkingException. */
    public ParkingRecord park(Vehicle vehicle) throws ParkingException {
        String plate = vehicle.getPlate();

        if (plate.isEmpty()) {
            throw new ParkingException("Missing plate number", "Please enter the plate number.", false);
        }
        if (!vehicle.hasValidPlate()) {
            throw new ParkingException("Invalid plate number", invalidPlateMessage(vehicle), false);
        }
        if (parked.containsKey(plate)) {
            throw new ParkingException("Duplicate vehicle",
                    plate + " is already parked in slot " + parked.get(plate).getSlot() + ".", false);
        }
        int index = slots.assignFirstFree(plate);
        if (index == -1) {
            throw new ParkingException("No slots available", "Sorry, the parking lot is full.", true);
        }

        ticketCount++;
        String ticketNo = String.format("T-%04d", ticketCount);
        ParkingRecord record = new ParkingRecord(ticketNo, vehicle, slots.nameOf(index), getCurrentTime());
        parked.put(plate, record);
        return record;
    }

    /** Explains what is wrong, in terms of the number of letters and digits. */
    private String invalidPlateMessage(Vehicle vehicle) {
        String message = "\"" + vehicle.getPlate() + "\" is not a valid " + vehicle.getType() + " plate number.\n\n"
                + "Required: " + vehicle.getPlateHint() + "\n"
                + "You entered: " + vehicle.countLetters() + " letters and " + vehicle.countDigits()
                + " digits (" + vehicle.countCharacters() + " characters).";
        if (!vehicle.hasOnlyLettersAndDigits()) {
            message += "\nOnly letters and numbers are allowed.";
        }
        return message;
    }

    /** Frees the slot, computes the fee and returns the finished record. */
    public ParkingRecord checkOut(String plate) throws ParkingException {
        ParkingRecord record = parked.get(plate);
        if (record == null) {
            throw new ParkingException("Not found", "No parked vehicle with plate " + plate + ".", true);
        }
        Date now = getCurrentTime();
        long minutes = fees.minutesParked(record.getTimeIn(), now);
        long hours = fees.billedHours(minutes);
        long fee = fees.fee(hours, record.getVehicle().getHourlyRate());

        record.checkOut(now, minutes, hours, fee);
        slots.release(plate);
        parked.remove(plate);
        return record;
    }

    public ParkingRecord getRecord(String plate) {
        return parked.get(plate);
    }

    /** Plates of all parked vehicles, sorted A-Z. */
    public String[] getParkedPlates() {
        return new TreeSet<>(parked.keySet()).toArray(new String[0]);
    }

    public int getSlotCount() {
        return slots.size();
    }

    public String getSlotName(int index) {
        return slots.nameOf(index);
    }

    public String getSlotPlate(int index) {
        return slots.getPlate(index);
    }

    /** Returns the current time, including any simulated offset for fee testing. */
    public Date getCurrentTime() {
        return new Date(System.currentTimeMillis() + timeOffsetMillis);
    }

    /** Shifts the simulated clock forward (or backward) by the given number of minutes. */
    public void advanceTimeMinutes(long minutes) {
        timeOffsetMillis += minutes * 60_000L;
    }

    /** Resets the simulated clock back to real system time. */
    public void resetTime() {
        timeOffsetMillis = 0L;
    }

    /** Returns the current simulated offset in whole minutes. */
    public long getTimeOffsetMinutes() {
        return timeOffsetMillis / 60_000L;
    }
}
