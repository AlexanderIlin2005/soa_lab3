package org.itmo.shop.infrastructure.soap;

import org.itmo.shop.domain.VehicleType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class RequestParameters {

    private RequestParameters() {
    }

    static VehicleType vehicleType(String value) throws ShopFault {
        try {
            return VehicleType.valueOf(value == null ? "" : value);
        } catch (IllegalArgumentException e) {
            String allowed = Arrays.stream(VehicleType.values()).map(Enum::name)
                    .collect(Collectors.joining(", ", "must be one of ", ""));
            throw invalid("type", "Parameter 'type' has an invalid value.", allowed);
        }
    }

    static int vehicleId(String value) throws ShopFault {
        int id;
        try {
            id = Integer.parseInt(value == null ? "" : value);
        } catch (NumberFormatException e) {
            throw invalid("vehicle-id", "Parameter 'vehicle-id' has an invalid value.", "must be a 32-bit integer");
        }
        if (id < 1) {
            throw invalid("vehicle-id", "The request violates constraints of the contract.",
                    "must be greater than or equal to 1");
        }
        return id;
    }

    private static ShopFault invalid(String field, String detail, String message) {
        return ShopFault.of(400, detail, List.of(new FieldErrorXml(field, message)));
    }
}
