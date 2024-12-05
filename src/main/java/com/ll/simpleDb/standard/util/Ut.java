package com.ll.simpleDb.standard.util;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Map;

public class Ut {
    public static class mapper {

        @SneakyThrows
        public static <T> T mapToObj(Map<String, Object> map, Class<T> cls) {
            T obj = cls.newInstance();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                Field declaredField = cls.getDeclaredField(key);
                declaredField.setAccessible(true);
                declaredField.set(obj, value);
            }

            return obj;
        }
    }
}
