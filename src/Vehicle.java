/**
 * PRODUCT of the Factory Method pattern.
 * Every vehicle type extends this class and supplies its own type name, hourly rate
 * and Philippine plate number rule.
 */
public abstract class Vehicle {
    private final String plate;          // stored without spaces, example: ABC1234

    protected Vehicle(String plate) {
        this.plate = plate.replaceAll("[\\s-]", "").toUpperCase();
    }

    /**
     * The plate as it is shown. Plates written as letters then digits (or digits then letters)
     * get a space in between, like ABC 1234 or 123 ABC. Mixed plates such as GA660C stay as they are.
     */
    public String getPlate() {
        if (plate.matches("[A-Z]+[0-9]+")) {
            int split = 0;
            while (Character.isLetter(plate.charAt(split))) split++;
            return plate.substring(0, split) + " " + plate.substring(split);
        }
        if (plate.matches("[0-9]+[A-Z]+")) {
            int split = 0;
            while (Character.isDigit(plate.charAt(split))) split++;
            return plate.substring(0, split) + " " + plate.substring(split);
        }
        return plate;
    }

    // ----- plate checker -----
    /**
     * A plate is valid when it has exactly the required number of letters and digits,
     * in any order (so ABC123 and GA660C are both valid motorcycle plates).
     */
    public boolean hasValidPlate() {
        return hasOnlyLettersAndDigits()
                && countLetters() == getRequiredLetters()
                && countDigits() == getRequiredDigits();
    }

    public boolean hasOnlyLettersAndDigits() {
        return plate.matches("[A-Z0-9]*");
    }

    public int countLetters() {
        int count = 0;
        for (char c : plate.toCharArray()) {
            if (c >= 'A' && c <= 'Z') count++;
        }
        return count;
    }

    public int countDigits() {
        int count = 0;
        for (char c : plate.toCharArray()) {
            if (c >= '0' && c <= '9') count++;
        }
        return count;
    }

    public int countCharacters() {
        return plate.length();
    }

    // ----- supplied by each vehicle type -----
    public abstract String getType();

    public abstract int getHourlyRate();          // in PHP

    public abstract int getRequiredLetters();     // how many letters the plate must have

    public abstract int getRequiredDigits();      // how many digits the plate must have

    public abstract String getPlateHint();        // explains the rule to the user
}

class Car extends Vehicle {
    Car(String plate) { super(plate); }
    public String getType() { return "Car"; }
    public int getHourlyRate() { return 40; }
    public int getRequiredLetters() { return 3; }
    public int getRequiredDigits() { return 4; }
    public String getPlateHint() { return "Car plate: 3 letters + 4 digits (7 characters, any order), e.g. ABC 1234"; }
}

class Motorcycle extends Vehicle {
    Motorcycle(String plate) { super(plate); }
    public String getType() { return "Motorcycle"; }
    public int getHourlyRate() { return 20; }
    public int getRequiredLetters() { return 3; }
    public int getRequiredDigits() { return 3; }
    public String getPlateHint() { return "Motorcycle plate: 3 letters + 3 digits (6 characters, any order), e.g. ABC 123 or GA660C"; }
}

class SuvVan extends Vehicle {
    SuvVan(String plate) { super(plate); }
    public String getType() { return "SUV / Van"; }
    public int getHourlyRate() { return 50; }
    public int getRequiredLetters() { return 3; }
    public int getRequiredDigits() { return 4; }
    public String getPlateHint() { return "SUV / Van plate: 3 letters + 4 digits (7 characters, any order), e.g. ABC 1234"; }
}

class Truck extends Vehicle {
    Truck(String plate) { super(plate); }
    public String getType() { return "Truck"; }
    public int getHourlyRate() { return 80; }
    public int getRequiredLetters() { return 3; }
    public int getRequiredDigits() { return 4; }
    public String getPlateHint() { return "Truck plate: 3 letters + 4 digits (7 characters, any order), e.g. ABC 1234"; }
}
