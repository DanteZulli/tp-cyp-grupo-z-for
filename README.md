# Intérprete con ANTLR

**Licenciatura en Sistemas — Conceptos y Paradigmas de Lenguajes de Programación — 2026**

**Equipo docente:** Ing. Elida Leoni — Lic. Tomas Silvestre

## Integrantes

- Mendieta, Gabriel — 41805105
- Zulli, Dante — 45630783

## Variante asignada

**Variante 2: for** — Iteración con variable de control.

## Descripción del lenguaje

Lenguaje imperativo simple con tipado estático explícito. Soporta `int`, `float`, `string` y `bool`. Las variables se declaran con `var nombre : tipo (= valor)?`. Expresiones aritméticas (`+`, `-`, `*`, `/`), relacionales (`==`, `!=`, `<`, `>`, `<=`, `>=`) y lógicas (`!`, `&&`, `||`). Estructuras de control: `if-else` y la variante diferencial `for`. Salida por consola con `print()`. Comentarios de línea (`//`) y bloque (`/* */`).

## Decisiones de diseño

- **Visitor pattern:** Se utiliza `LanguageBaseVisitor` para recorrer el AST (tal como exige la guía), separando la lógica en dos visitors: `SemanticAnalyzer` (validación) e `Interpreter` (ejecución).
- **Pipeline en 3 fases:** parseo (ANTLR) → análisis semántico → interpretación. Si hay errores semánticos no se ejecuta.
- **Tabla de símbolos compartida:** `SymbolTable` es una clase independiente usada por ambos visitors, con tipado mediante `enum Type`.
- **Type checking por visitor:** cada visitor de expresión retorna `SymbolTable.Type`, lo que permite verificar compatibilidad sin necesidad de un sistema de tipos separado.
- **Coerción implícita:** al asignar un entero a una variable `float`, el `Interpreter` convierte el valor automáticamente.
- **Sintaxis Pascal-like:** `var nombre : tipo = expr` para declaraciones, `print(...)` para salida.
- **Variable de control del for en scope global:** la variable declarada en el encabezado del `for` (`var i : int = 0`) queda disponible en la tabla de símbolos global, consistente con el resto del lenguaje que no implementa scoping por bloque.

## Compilación y ejecución

```bash
mvn clean package
mvn exec:java -Dexec.args="<archivo>"
```

## Ejemplos

```lang
// Declaraciones y expresiones
var x : int = 10;
var y : float = 3.14;
var ok : bool = true;

print(x * 2);
print(y / 2.0);

// Condicional if-else
if (x > 5) {
    print("mayor");
} else {
    print("menor o igual");
}

// Operadores relacionales y logicos
var a : bool = x == 10;
var b : bool = !a && ok;
```
