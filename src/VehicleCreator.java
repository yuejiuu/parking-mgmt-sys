/**
 * CREATOR of the Factory Method pattern (creational).
 * Each subclass decides which Vehicle to create, so adding a new vehicle type
 * only needs one new Vehicle class and one new creator.
 */
public abstract class VehicleCreator {

    /** The factory method. */
    public abstract Vehicle createVehicle(String plate);

    /** Text shown in the Vehicle Type dropdown. */
    public abstract String getTypeName();
}

class CarCreator extends VehicleCreator {
    public Vehicle createVehicle(String plate) { return new Car(plate); }
    public String getTypeName() { return "Car"; }
}

class MotorcycleCreator extends VehicleCreator {
    public Vehicle createVehicle(String plate) { return new Motorcycle(plate); }
    public String getTypeName() { return "Motorcycle"; }
}

class SuvVanCreator extends VehicleCreator {
    public Vehicle createVehicle(String plate) { return new SuvVan(plate); }
    public String getTypeName() { return "SUV / Van"; }
}

class TruckCreator extends VehicleCreator {
    public Vehicle createVehicle(String plate) { return new Truck(plate); }
    public String getTypeName() { return "Truck"; }
}
