package org.itmo.shop.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShopFaultInfo", propOrder = {"status", "title", "detail", "errors"})
public class ShopFaultInfo {

    @XmlElement(required = true)
    private int status;

    @XmlElement(required = true)
    private String title;

    @XmlElement(required = true)
    private String detail;

    @XmlElement(name = "error")
    private List<FieldErrorXml> errors = new ArrayList<>();

    public ShopFaultInfo() {
    }

    ShopFaultInfo(int status, String title, String detail, List<FieldErrorXml> errors) {
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.errors = new ArrayList<>(errors);
    }

    static String title(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            default -> "Internal Server Error";
        };
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public List<FieldErrorXml> getErrors() {
        return errors;
    }
}
