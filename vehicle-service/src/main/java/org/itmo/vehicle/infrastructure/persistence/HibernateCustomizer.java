package org.itmo.vehicle.infrastructure.persistence;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Программно применяет наш FormatMapper к Hibernate ДО того, как
 * Spring Boot начнёт создавать EntityManagerFactory.
 *
 * Через application.yml свойство hibernate.type.xml_format_mapper
 * игнорируется Spring Boot (это известная особенность Boot 3.x).
 * Через HibernatePropertiesCustomizer — применяется гарантированно.
 *
 * Наш NoScanJsonFormatMapper не вызывает ObjectMapper.findModules(),
 * поэтому ServiceLoader не ищет Payara-модуль
 * JakartaXmlBindAnnotationModule, и конфликт classloader'ов не возникает.
 */
@Component
public class HibernateCustomizer implements HibernatePropertiesCustomizer {

    private static final String MAPPER = NoScanJsonFormatMapper.class.getName();

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.type.json_format_mapper", MAPPER);
        hibernateProperties.put("hibernate.type.xml_format_mapper", MAPPER);
    }
}
