package org.evrete.showcase.town.model;

import org.evrete.api.Type;
import org.evrete.api.TypeField;
import org.evrete.api.TypeWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Entity {
    public final String type;
    private final Map<String, Boolean> flags = new HashMap<>();
    private final Map<String, Integer> integers = new HashMap<>();
    private final Map<String, Double> doubles = new HashMap<>();
    private final Map<String, Entity> properties = new HashMap<>();

    public Entity(String type) {
        this.type = type;
    }

    public Entity(LocationType type) {
        this.type = type.name();
    }

    public int getNumber(String name, int defaultValue) {
        return integers.getOrDefault(name, defaultValue);
    }

    public double getDouble(String name, double defaultValue) {
        return doubles.getOrDefault(name, defaultValue);
    }

    public int getNumber(String name) {
        return Objects.requireNonNull(integers.get(name));
    }

    public double getDouble(String name) {
        return Objects.requireNonNull(doubles.get(name));
    }

    public boolean getFlag(String name, boolean defaultValue) {
        return flags.getOrDefault(name, defaultValue);
    }

    public Entity getProperty(String name) {
        return properties.get(name);
    }

    public Entity set(String name, boolean flag) {
        this.flags.put(name, flag);
        return this;
    }

    public Entity set(String name, int i) {
        this.integers.put(name, i);
        return this;
    }

    public Entity set(String name, double d) {
        this.doubles.put(name, d);
        return this;
    }

    public Entity set(String name, Entity property) {
        if (property != null && property.getClass().isPrimitive()) {
            throw new IllegalArgumentException("Primitive types are not allowed");
        } else {
            if (property == null) {
                this.properties.remove(name);
            } else {
                this.properties.put(name, property);
            }
        }
        return this;
    }


    @Override
    public String toString() {
        return "{type='" + type + '\'' +
                (flags.isEmpty() ? "" : ", flags=" + flags) +
                (integers.isEmpty() ? "" : ", integers=" + integers) +
                (doubles.isEmpty() ? "" : ", doubles=" + doubles) +
                (properties.isEmpty() ? "" : ", props=" + properties) +
                '}';
    }

    public static class EntityKnowledgeType extends TypeWrapper<Entity> {
        public EntityKnowledgeType(Type<Entity> delegate) {
            super(delegate);
        }

        @Override
        public TypeField getField(String name) {
            TypeField field = super.getField(name);
            if (field != null) return field;


            String[] parts = name.split("\\.");
            if (parts.length != 2) {
                return null;
            }

            String key = parts[1];
            switch (parts[0]) {
                case "flags":
                    return declareBooleanField(name, entity -> entity.getFlag(key, false));
                case "integers":
                    return declareIntField(name, entity -> entity.getNumber(key, 0));
                case "doubles":
                    return declareDoubleField(name, entity -> entity.getDouble(key, 0.0));
                case "properties":
                    return declareField(name, Entity.class, entity -> entity.getProperty(key));
                default:
                    throw new IllegalStateException("Unknown prefix: " + parts[0]);

            }
        }
    }

}
