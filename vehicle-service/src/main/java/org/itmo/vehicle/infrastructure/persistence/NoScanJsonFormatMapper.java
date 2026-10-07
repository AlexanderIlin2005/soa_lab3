package org.itmo.vehicle.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;

/**
 * Обёртка над {@link JacksonJsonFormatMapper}, которая НЕ вызывает
 * {@code ObjectMapper.findAndRegisterModules()}.
 *
 * Hibernate 6.6 при создании FormatMapper по умолчанию строит
 * JacksonJsonFormatMapper через конструктор без аргументов, который
 * вызывает findAndRegisterModules(). В classpath Payara есть
 * jackson-module-jakarta-xmlbind-annotations.jar, чей
 * JakartaXmlBindAnnotationModule загружен ДРУГИМ classloader'ом
 * и не наследуется от Module из WAR. Это даёт
 * "com.fasterxml.jackson.databind.Module: ... not a subtype".
 *
 * Наш маппер сам создаёт ObjectMapper и регистрирует только
 * JavaTimeModule — ServiceLoader не вызывается.
 */
public class NoScanJsonFormatMapper implements FormatMapper {

    private final JacksonJsonFormatMapper delegate;

    public NoScanJsonFormatMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        this.delegate = new JacksonJsonFormatMapper(mapper);
    }

    @Override
    public <T> T fromString(CharSequence charSequence, JavaType<T> type, WrapperOptions options) {
        return delegate.fromString(charSequence, type, options);
    }

    @Override
    public <T> String toString(T value, JavaType<T> type, WrapperOptions options) {
        return delegate.toString(value, type, options);
    }
}
