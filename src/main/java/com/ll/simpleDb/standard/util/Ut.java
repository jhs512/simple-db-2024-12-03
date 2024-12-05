package com.ll.simpleDb.standard.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

import java.util.Map;

public class Ut {
    public static class mapper {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper() {{
            registerModule(new JavaTimeModule());
            setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        }};

        @SneakyThrows
        public static <T> T mapToObj(Map<String, Object> map, Class<T> cls) {
            return OBJECT_MAPPER.convertValue(map, cls);
        }
    }
}
