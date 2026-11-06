package org.itmo.shop.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Coordinates", propOrder = {"x", "y"})
public class CoordinatesXml {

    @XmlElement(required = true)
    private int x;

    @XmlElement(required = true)
    private int y;

    public CoordinatesXml() {
    }

    CoordinatesXml(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
