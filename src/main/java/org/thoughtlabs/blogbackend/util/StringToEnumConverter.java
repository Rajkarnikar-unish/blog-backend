package org.thoughtlabs.blogbackend.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.thoughtlabs.blogbackend.models.EPostStatus;


@Component
public class StringToEnumConverter implements Converter<String, EPostStatus> {
    @Override
    public EPostStatus convert(String source) {
        try {
            return EPostStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
