package com.grupoz.interprete;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public enum Type {
        INT, FLOAT, STRING, BOOL;

        public static Type fromString(String s) {
            return switch (s) {
                case "int" -> INT;
                case "float" -> FLOAT;
                case "string" -> STRING;
                case "bool" -> BOOL;
                default -> throw new IllegalArgumentException("Unknown type: " + s);
            };
        }

        public boolean isNumeric() {
            return this == INT || this == FLOAT;
        }
    }

    public static class Entry {
        public final Type type;
        public Object value;
        public boolean initialized;

        public Entry(Type type, Object value, boolean initialized) {
            this.type = type;
            this.value = value;
            this.initialized = initialized;
        }
    }

    private final Map<String, Entry> symbols = new HashMap<>();

    public void declare(String name, Type type, Object value, boolean initialized) {
        symbols.put(name, new Entry(type, value, initialized));
    }

    public boolean exists(String name) {
        return symbols.containsKey(name);
    }

    public Type getType(String name) {
        Entry e = symbols.get(name);
        return e != null ? e.type : null;
    }

    public Object getValue(String name) {
        Entry e = symbols.get(name);
        return e != null ? e.value : null;
    }

    public void assign(String name, Object value) {
        Entry e = symbols.get(name);
        if (e != null) {
            e.value = value;
            e.initialized = true;
        }
    }

    public Entry get(String name) {
        return symbols.get(name);
    }
}
