package org.itmo.vehicle.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;

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
