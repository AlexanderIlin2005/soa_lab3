package org.itmo.vehicle.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.itmo.vehicle.domain.*;
import org.itmo.vehicle.domain.query.Page;
import org.itmo.vehicle.domain.query.PageRequest;
import org.itmo.vehicle.domain.query.VehicleQuery;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaVehicleRepository implements VehicleRepository {

    private static final String MAX_NAME_SQL =
            "SELECT * FROM soa_vehicle ORDER BY name COLLATE \"C\" DESC, id LIMIT 1";

    @PersistenceContext
    private EntityManager entityManager;

    private final Clock clock;

    public JpaVehicleRepository(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Vehicle add(ZonedDateTime creationDate, VehicleDetails details) {
        VehicleEntity entity = new VehicleEntity(creationDate.toOffsetDateTime());
        VehicleEntityMapper.copyDetails(details, entity);
        entityManager.persist(entity);
        entityManager.flush();
        return new Vehicle(entity.getId(), creationDate, details);
    }

    @Override
    public Optional<Vehicle> findById(int id) {
        return Optional.ofNullable(entityManager.find(VehicleEntity.class, id)).map(this::toDomain);
    }

    @Override
    public void update(Vehicle vehicle) {
        VehicleEntity entity = entityManager.find(VehicleEntity.class, vehicle.getId());
        if (entity == null) throw new IllegalStateException("No row for " + vehicle);
        VehicleEntityMapper.copyDetails(vehicle.getDetails(), entity);
    }

    @Override
    public boolean remove(int id) {
        int removed = entityManager.createQuery("DELETE FROM VehicleEntity v WHERE v.id = :id")
                .setParameter("id", id).executeUpdate();
        return removed > 0;
    }

    @Override
    public Page<Vehicle> find(VehicleQuery query) {
        PageRequest request = query.page();
        long total = count(query);
        if (request.offset() >= total) return Page.empty(request, total);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<VehicleEntity> select = builder.createQuery(VehicleEntity.class);
        Root<VehicleEntity> root = select.from(VehicleEntity.class);
        CriteriaTranslator translator = new CriteriaTranslator(builder, root);
        select.select(root)
                .where(translator.predicates(query.criteria()))
                .orderBy(translator.orders(query.effectiveSort()));
        List<Vehicle> items = entityManager.createQuery(select)
                .setFirstResult(Math.toIntExact(request.offset()))
                .setMaxResults(request.size())
                .getResultStream().map(this::toDomain).toList();
        return new Page<>(items, request.page(), request.size(), total);
    }

    private long count(VehicleQuery query) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> count = builder.createQuery(Long.class);
        Root<VehicleEntity> root = count.from(VehicleEntity.class);
        count.select(builder.count(root))
                .where(new CriteriaTranslator(builder, root).predicates(query.criteria()));
        return entityManager.createQuery(count).getSingleResult();
    }

    @Override
    public EnginePowerStatistics enginePowerStatistics() {
        Object[] row = entityManager
                .createQuery("SELECT SUM(v.enginePower), COUNT(v) FROM VehicleEntity v", Object[].class)
                .getSingleResult();
        double sum = row[0] == null ? 0.0 : ((Number) row[0]).doubleValue();
        return new EnginePowerStatistics(sum, ((Number) row[1]).longValue());
    }

    @Override
    public WheelsStatistics wheelsStatistics() {
        Object[] row = entityManager
                .createQuery("SELECT AVG(v.numberOfWheels), COUNT(v.numberOfWheels) FROM VehicleEntity v",
                        Object[].class)
                .getSingleResult();
        Double average = row[0] == null ? null : ((Number) row[0]).doubleValue();
        return new WheelsStatistics(average, ((Number) row[1]).longValue());
    }

    @Override
    public Optional<Vehicle> findWithMaxName() {
        List<?> rows = entityManager.createNativeQuery(MAX_NAME_SQL, VehicleEntity.class).getResultList();
        return rows.stream().findFirst().map(row -> toDomain((VehicleEntity) row));
    }

    private Vehicle toDomain(VehicleEntity entity) {
        return VehicleEntityMapper.toDomain(entity, clock.getZone());
    }
}
