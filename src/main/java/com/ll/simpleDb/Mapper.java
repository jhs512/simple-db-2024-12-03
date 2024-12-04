package com.ll.simpleDb;

import java.lang.reflect.Field;
import java.util.Map;


/**
  주어진 `Map<String, Object>` 데이터를 지정된 클래스 타입의 객체로 변환합니다.

 // @param data 매핑할 데이터 (키: 필드 이름, 값: 필드 값)
 // @param clazz 변환할 대상 클래스 타입
 // @param <T> 대상 객체의 타입
 // @return 변환된 객체
 // @throws RuntimeException 매핑 과정에서 예외가 발생할 경우
 */

public class Mapper {
    public static <T> T map(Map<String, Object> data, Class<T> clazz) {
        try {
            // 대상 객체의 인스턴스를 생성합니다.
            T instance = clazz.getDeclaredConstructor().newInstance();

            // Map의 각 엔트리를 처리합니다.
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                // 대상 클래스에서 필드를 찾습니다.
                Field field = getField(clazz, fieldName);
                if (field != null) {
                    field.setAccessible(true); // private 필드도 접근 가능하도록 설정
                    // 필드 타입에 맞게 값을 변환하여 객체에 설정합니다.
                    field.set(instance, convertValue(value, field.getType()));
                }
            }

            return instance;
        } catch (Exception e) {
            // 매핑 중 예외가 발생하면 런타임 예외를 던집니다.
            throw new RuntimeException("Failed to map data to " + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * 클래스에서 주어진 이름의 필드를 찾습니다.
     *
     * - 현재 클래스에서 찾을 수 없으면 슈퍼클래스에서 재귀적으로 탐색합니다.
     *
     * @param clazz 필드를 찾을 클래스
     * @param fieldName 찾을 필드 이름
     * @return 발견된 `Field` 객체 또는 `null`
     */

    private static Field getField(Class<?> clazz, String fieldName) {
        try {
            // 클래스의 선언된 필드 중 이름이 일치하는 필드를 반환합니다.
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 필드가 현재 클래스에 없을 경우 슈퍼클래스를 탐색합니다.
            if (clazz.getSuperclass() != null) {
                return getField(clazz.getSuperclass(), fieldName);
            }
            return null; // 필드를 찾지 못한 경우
        }
    }

    /**
     * 주어진 값을 대상 필드의 타입에 맞게 변환합니다.
     *
     * @param value 변환할 값
     * @param targetType 대상 필드 타입
     * @return 변환된 값
     * @throws IllegalArgumentException 변환할 수 없는 타입의 경우
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null; // 값이 null인 경우 변환 없이 반환
        }

        // 값이 이미 대상 타입에 호환된다면 그대로 반환
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // 숫자 타입 변환
        if (targetType == Long.class || targetType == long.class) {
            return ((Number) value).longValue();
        } else if (targetType == Integer.class || targetType == int.class) {
            return ((Number) value).intValue();
        } else if (targetType == Double.class || targetType == double.class) {
            return ((Number) value).doubleValue();
        }

        // Boolean 변환
        else if (targetType == Boolean.class || targetType == boolean.class) {
            return value instanceof Number ? ((Number) value).intValue() != 0 : Boolean.parseBoolean(value.toString());
        }

        // 문자열 변환
        else if (targetType == String.class) {
            return value.toString();
        }

        // LocalDateTime 변환
        else if (targetType == java.time.LocalDateTime.class) {
            return java.sql.Timestamp.valueOf(value.toString()).toLocalDateTime();
        }

        // 지원되지 않는 타입의 경우 예외 발생
        throw new IllegalArgumentException("Cannot convert value of type " + value.getClass() + " to " + targetType);
    }
}
