package org.itmo.shop.infrastructure.vehicleservice;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import org.itmo.shop.domain.Coordinates;
import org.itmo.shop.domain.FuelType;
import org.itmo.shop.domain.Vehicle;
import org.itmo.shop.domain.VehicleCatalog;
import org.itmo.shop.domain.VehicleCatalogException;
import org.itmo.shop.domain.VehicleNotFoundException;
import org.itmo.shop.domain.VehicleType;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HttpVehicleCatalog implements VehicleCatalog {

    static final int PAGE_SIZE = 100;

    private final String baseUrl;
    private final HttpClient http;
    private final Duration readTimeout;
    private final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withNullValues(true));

    public HttpVehicleCatalog(String baseUrl, Duration connectTimeout, Duration readTimeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
        this.readTimeout = readTimeout;
    }

    @Override
    public Optional<Vehicle> findById(int id) {
        String action = "read vehicle " + id;
        HttpResponse<String> response = send(action, request("/vehicles/" + id).GET());
        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        return Optional.of(toDomain(read(action, response, VehicleJson.class)));
    }

    @Override
    public Vehicle replace(Vehicle vehicle) {
        String action = "update vehicle " + vehicle.id();
        String body = jsonb.toJson(VehicleUpdateJson.from(vehicle));
        HttpResponse<String> response = send(action, request("/vehicles/" + vehicle.id())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body)));
        if (response.statusCode() == 404) {
            throw new VehicleNotFoundException(vehicle.id());
        }
        return toDomain(read(action, response, VehicleJson.class));
    }

    @Override
    public List<Vehicle> findByType(VehicleType type) {
        List<Vehicle> found = new ArrayList<>();
        int lastId = 0;
        List<VehicleJson> page;
        do {
            page = pageAfter(type, lastId);
            for (VehicleJson item : page) {
                Vehicle vehicle = toDomain(item);
                if (vehicle.id() <= lastId) {
                    throw new VehicleCatalogException("Could not search vehicles of type " + type
                            + ": the vehicle service returned them out of order.", null);
                }
                found.add(vehicle);
                lastId = vehicle.id();
            }
        } while (page.size() == PAGE_SIZE);
        return found;
    }

    private List<VehicleJson> pageAfter(VehicleType type, int lastId) {
        String action = "search vehicles of type " + type;
        String query = "?filter=" + encode("type:eq:" + type.name()) + "&filter=" + encode("id:gt:" + lastId)
                + "&sort=id&page=1&size=" + PAGE_SIZE;
        VehiclePageJson page = read(action, send(action, request("/vehicles" + query).GET()), VehiclePageJson.class);
        if (page.items == null) {
            throw new VehicleCatalogException("Could not " + action
                    + ": the vehicle service returned a response the shop cannot read.", null);
        }
        return page.items;
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(readTimeout)
                .header("Accept", "application/json");
    }

    private HttpResponse<String> send(String action, HttpRequest.Builder request) {
        try {
            return http.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpConnectTimeoutException | ConnectException e) {
            throw failure(action, "the vehicle service is unreachable", e);
        } catch (HttpTimeoutException e) {
            throw failure(action, "the vehicle service did not answer in time", e);
        } catch (IOException e) {
            throw failure(action, "the vehicle service is unreachable", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw failure(action, "the request was interrupted", e);
        }
    }

    private <T> T read(String action, HttpResponse<String> response, Class<T> type) {
        if (response.statusCode() != 200) {
            throw failure(action, "the vehicle service answered with status " + response.statusCode(), null);
        }
        try {
            T value = jsonb.fromJson(response.body(), type);
            if (value == null) {
                throw new JsonbException("empty body");
            }
            return value;
        } catch (JsonbException e) {
            throw failure(action, "the vehicle service returned a response the shop cannot read", e);
        }
    }

    private static VehicleCatalogException failure(String action, String reason, Throwable cause) {
        return new VehicleCatalogException("Could not " + action + ": " + reason + ".", cause);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Vehicle toDomain(VehicleJson json) {
        CoordinatesJson coordinates = required(json.coordinates, "coordinates");
        return new Vehicle(
                required(json.id, "id"),
                required(json.name, "name"),
                new Coordinates(required(coordinates.x, "coordinates.x"), required(coordinates.y, "coordinates.y")),
                OffsetDateTime.parse(required(json.creationDate, "creationDate")),
                required(json.enginePower, "enginePower"),
                json.numberOfWheels,
                json.mileage,
                json.type == null ? null : VehicleType.valueOf(json.type),
                json.fuelType == null ? null : FuelType.valueOf(json.fuelType));
    }

    private static <T> T required(T value, String field) {
        if (value == null) {
            throw new VehicleCatalogException("The vehicle service returned a vehicle without " + field + ".", null);
        }
        return value;
    }

    public static class VehiclePageJson {
        public List<VehicleJson> items;
    }

    public static class VehicleJson {
        public Integer id;
        public String name;
        public CoordinatesJson coordinates;
        public String creationDate;
        public Float enginePower;
        public Integer numberOfWheels;
        public Double mileage;
        public String type;
        public String fuelType;
    }

    public static class CoordinatesJson {
        public Integer x;
        public Integer y;

        public CoordinatesJson() {
        }

        CoordinatesJson(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class VehicleUpdateJson {
        public String name;
        public CoordinatesJson coordinates;
        public float enginePower;
        public Integer numberOfWheels;
        public Double mileage;
        public String type;
        public String fuelType;

        static VehicleUpdateJson from(Vehicle vehicle) {
            VehicleUpdateJson json = new VehicleUpdateJson();
            json.name = vehicle.name();
            json.coordinates = new CoordinatesJson(vehicle.coordinates().x(), vehicle.coordinates().y());
            json.enginePower = vehicle.enginePower();
            json.numberOfWheels = vehicle.numberOfWheels();
            json.mileage = vehicle.mileage();
            json.type = vehicle.type() == null ? null : vehicle.type().name();
            json.fuelType = vehicle.fuelType() == null ? null : vehicle.fuelType().name();
            return json;
        }
    }
}
