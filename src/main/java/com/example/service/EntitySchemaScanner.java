package com.example.service;


import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EntitySchemaScanner {

    public static String scanSchema(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

        StringBuilder schema = new StringBuilder("Database Schema:\n");

        entities.forEach(entity -> {
            schema.append("- ")
                  .append(entity.getSimpleName())
                  .append("(");
            Field[] fields = entity.getDeclaredFields();
            String fieldNames = Arrays.stream(fields)
                                      .map(Field::getName)
                                      .collect(Collectors.joining(", "));
            schema.append(fieldNames).append(")\n");

            schema.append(")\n");
        });

        return schema.toString();
    }
}

