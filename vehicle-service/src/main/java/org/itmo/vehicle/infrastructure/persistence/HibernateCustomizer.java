package org.itmo.vehicle.infrastructure.persistence;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HibernateCustomizer implements HibernatePropertiesCustomizer {

    private static final String MAPPER = NoScanJsonFormatMapper.class.getName();

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.type.json_format_mapper", MAPPER);
        hibernateProperties.put("hibernate.type.xml_format_mapper", MAPPER);
    }
}
