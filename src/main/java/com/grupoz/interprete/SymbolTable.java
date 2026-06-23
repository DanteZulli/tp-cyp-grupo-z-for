package com.grupoz.interprete;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Tabla de simbolos del interprete.
 * Almacena variables con su tipo, valor y estado de inicializacion.
 * Es utilizada tanto por el analizador semantico como por el interprete.
 */
public class SymbolTable {

    public enum Type {
        INT, FLOAT, STRING, BOOL;

        public static Type fromString(String s) {
            return switch (s) {
                case "entero" -> INT;
                case "real" -> FLOAT;
                case "texto" -> STRING;
                case "boleano" -> BOOL;
                default -> throw new IllegalArgumentException("Tipo desconocido: " + s);
            };
        }

        public boolean isNumeric() {
            return this == INT || this == FLOAT;
        }
    }

    @Data
    @AllArgsConstructor
    public static class Entry {
        private final Type type;
        private Object value;
        private boolean initialized;
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
        return e != null ? e.getType() : null;
    }

    public Object getValue(String name) {
        Entry e = symbols.get(name);
        return e != null ? e.getValue() : null;
    }

    public void assign(String name, Object value) {
        Entry e = symbols.get(name);
        if (e != null) {
            e.setValue(value);
            e.setInitialized(true);
        }
    }

    public Entry get(String name) {
        return symbols.get(name);
    }
}
