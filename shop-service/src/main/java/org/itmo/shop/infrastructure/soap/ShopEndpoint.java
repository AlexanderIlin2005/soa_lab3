package org.itmo.shop.infrastructure.soap;

import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import org.itmo.shop.application.ShopService;
import org.itmo.shop.domain.VehicleCatalogException;
import org.itmo.shop.domain.VehicleNotFoundException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebService(name = "ShopPort", serviceName = "ShopService", portName = "ShopPort",
        targetNamespace = ShopEndpoint.NAMESPACE)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL,
        parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public class ShopEndpoint {

    public static final String NAMESPACE = "http://itmo.org/soa/shop";

    private static final Logger LOG = Logger.getLogger(ShopEndpoint.class.getName());

    @Inject
    private ShopService shop;

    @WebMethod(operationName = "searchByType")
    @WebResult(name = "vehicle")
    public List<VehicleXml> searchByType(@WebParam(name = "type") String type) throws ShopFault {
        return handle(() -> shop.searchByType(RequestParameters.vehicleType(type)).stream()
                .map(VehicleXml::from)
                .toList());
    }

    @WebMethod(operationName = "fixDistance")
    @WebResult(name = "vehicle")
    public VehicleXml fixDistance(@WebParam(name = "vehicleId") String vehicleId) throws ShopFault {
        return handle(() -> VehicleXml.from(shop.fixDistance(RequestParameters.vehicleId(vehicleId))));
    }

    private static <T> T handle(Operation<T> operation) throws ShopFault {
        try {
            return operation.run();
        } catch (VehicleNotFoundException e) {
            throw ShopFault.of(404, e.getMessage());
        } catch (VehicleCatalogException e) {
            LOG.log(Level.WARNING, e.getMessage(), e.getCause());
            throw ShopFault.of(500, e.getMessage());
        } catch (ShopFault e) {
            throw e;
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Unexpected error", e);
            throw ShopFault.of(500, "Internal service error.");
        }
    }

    @FunctionalInterface
    private interface Operation<T> {
        T run() throws ShopFault;
    }
}
