package org.itmo.shop.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import org.itmo.shop.domain.Vehicle;

import java.time.format.DateTimeFormatter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Vehicle", propOrder = {"id", "name", "coordinates", "creationDate", "enginePower",
        "numberOfWheels", "mileage", "type", "fuelType"})
public class VehicleXml {

    @XmlElement(required = true)
    private int id;

    @XmlElement(required = true)
    private String name;

    @XmlElement(required = true)
    private CoordinatesXml coordinates;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private String creationDate;

    @XmlElement(required = true)
    private float enginePower;

    private Integer numberOfWheels;

    private Double mileage;

    private String type;

    private String fuelType;

    public VehicleXml() {
    }

    static VehicleXml from(Vehicle vehicle) {
        VehicleXml xml = new VehicleXml();
        xml.id = vehicle.id();
        xml.name = vehicle.name();
        xml.coordinates = new CoordinatesXml(vehicle.coordinates().x(), vehicle.coordinates().y());
        xml.creationDate = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(vehicle.creationDate());
        xml.enginePower = vehicle.enginePower();
        xml.numberOfWheels = vehicle.numberOfWheels();
        xml.mileage = vehicle.mileage();
        xml.type = vehicle.type() == null ? null : vehicle.type().name();
        xml.fuelType = vehicle.fuelType() == null ? null : vehicle.fuelType().name();
        return xml;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CoordinatesXml getCoordinates() {
        return coordinates;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public float getEnginePower() {
        return enginePower;
    }

    public Integer getNumberOfWheels() {
        return numberOfWheels;
    }

    public Double getMileage() {
        return mileage;
    }

    public String getType() {
        return type;
    }

    public String getFuelType() {
        return fuelType;
    }
}
