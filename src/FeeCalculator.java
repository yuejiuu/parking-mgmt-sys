import java.util.Date;

/** Computes the parking fee (subsystem used by the Facade). */
public class FeeCalculator {

    public long minutesParked(Date in, Date out) {
        return Math.max(1, (out.getTime() - in.getTime()) / 60000);
    }

    /** Hours are rounded up, with a minimum of 1 hour. */
    public long billedHours(long minutes) {
        return Math.max(1, (minutes + 59) / 60);
    }

    public long fee(long hours, int hourlyRate) {
        return hours * hourlyRate;
    }
}
