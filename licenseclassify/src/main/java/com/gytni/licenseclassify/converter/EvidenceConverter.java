package com.gytni.licenseclassify.converter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gytni.licenseclassify.model.Evidence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class EvidenceConverter implements AttributeConverter<List<Evidence>, String> {
    
    @Override
    public String convertToDatabaseColumn(List<Evidence> attribute) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Can not convert to string", e);
        }
        return null;
    }

    @Override
    public List<Evidence> convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            return mapper.readValue(dbData, new TypeReference<List<Evidence>>() {});
        } catch (JsonProcessingException e) {
            log.error("Can not convert to Evidence, JSON parsing error", e);
        }
        return null;
    }

}
