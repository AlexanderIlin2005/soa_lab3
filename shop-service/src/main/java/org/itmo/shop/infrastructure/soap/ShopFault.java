package org.itmo.shop.infrastructure.soap;

import jakarta.xml.ws.WebFault;

import java.util.List;

@WebFault(name = "ShopFault", targetNamespace = ShopEndpoint.NAMESPACE)
public class ShopFault extends Exception {

    private final ShopFaultInfo faultInfo;

    public ShopFault(String message, ShopFaultInfo faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public ShopFault(String message, ShopFaultInfo faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public ShopFaultInfo getFaultInfo() {
        return faultInfo;
    }

    static ShopFault of(int status, String detail) {
        return of(status, detail, List.of());
    }

    static ShopFault of(int status, String detail, List<FieldErrorXml> errors) {
        return new ShopFault(detail, new ShopFaultInfo(status, ShopFaultInfo.title(status), detail, errors));
    }
}
