package org.itmo.shop.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FieldError", propOrder = {"field", "message"})
public class FieldErrorXml {

    @XmlElement(required = true)
    private String field;

    @XmlElement(required = true)
    private String message;

    public FieldErrorXml() {
    }

    FieldErrorXml(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
