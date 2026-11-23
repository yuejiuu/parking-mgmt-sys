/** Keeps track of which plate is parked in which slot (subsystem used by the Facade). */
public class SlotManager {
    private static final int COLUMNS = 4;
    private final String[] plates;          // null = free slot

    public SlotManager(int size) {
        plates = new String[size];
    }

    public int size() {
        return plates.length;
    }

    /** Slot names run across the rows: A1 B1 C1 D1 / A2 B2 C2 D2 ... */
    public String nameOf(int index) {
        return "" + (char) ('A' + index % COLUMNS) + (index / COLUMNS + 1);
    }

    public String getPlate(int index) {
        return plates[index];
    }

    /** Puts the plate in the first free slot. Returns the slot index, or -1 if the lot is full. */
    public int assignFirstFree(String plate) {
        for (int i = 0; i < plates.length; i++) {
            if (plates[i] == null) {
                plates[i] = plate;
                return i;
            }
        }
        return -1;
    }

    /** Frees the slot that holds this plate. Returns the slot index, or -1 if not found. */
    public int release(String plate) {
        for (int i = 0; i < plates.length; i++) {
            if (plate.equals(plates[i])) {
                plates[i] = null;
                return i;
            }
        }
        return -1;
    }
}
