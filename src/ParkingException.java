/**
 * Thrown when a parking action is not allowed (empty plate, duplicate, lot full, ...).
 * The GUI catches it and shows the title and message in a JOptionPane.
 */
public class ParkingException extends Exception {
    private final String title;
    private final boolean serious;      // true = error dialog, false = warning dialog

    public ParkingException(String title, String message, boolean serious) {
        super(message);
        this.title = title;
        this.serious = serious;
    }

    public String getTitle() {
        return title;
    }

    public boolean isSerious() {
        return serious;
    }
}
