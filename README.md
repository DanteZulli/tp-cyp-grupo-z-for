# Intérprete con ANTLR

**Licenciatura en Sistemas  -  Conceptos y Paradigmas de Lenguajes de Programación  -  2026**

**Equipo docente:** Ing. Elida Leoni  -  Lic. Tomas Silvestre

## Integrantes

- Mendieta, Gabriel  -  41805105
- Zulli, Dante  -  45630783

## Variante asignada

**Variante 2: for**  -  Iteración con variable de control.

## Descripción del lenguaje

Lenguaje imperativo simple con tipado estático explícito y **keywords en español rioplatense**.
Soporta `entero`, `real`, `texto` y `boleano`.
Las variables se declaran con `variable nombre : tipo (= valor)?`.
Expresiones aritméticas (`+`, `-`, `*`, `/`), relacionales (`==`, `!=`, `<`, `>`, `<=`, `>=`)
y lógicas (`no`, `y`, `o`).
Estructuras de control: `si-sino` y la variante diferencial `para`.
Salida por consola con `mostrar()`.
Comentarios de línea (`//`) y bloque (`/* */`).

## Decisiones de diseño

- **Visitor pattern:** Se utiliza `LanguageBaseVisitor` para recorrer el AST (tal como exige la
  guía), separando la lógica en dos visitors: `SemanticAnalyzer` (validación) e `Interpreter`
  (ejecución).
- **Pipeline en 3 fases:** parseo (ANTLR) → análisis semántico → interpretación. Si hay errores
  semánticos no se ejecuta.
- **Tabla de símbolos compartida:** `SymbolTable` es una clase independiente usada por ambos
  visitors, con tipado mediante `enum Type`.
- **Type checking por visitor:** cada visitor de expresión retorna `SymbolTable.Type`, lo que
  permite verificar compatibilidad sin necesidad de un sistema de tipos separado.
- **Coerción implícita:** al asignar un entero a una variable `real`, el `Interpreter` convierte
  el valor automáticamente.
- **Sintaxis en español argentino:** todos los keywords del lenguaje están en español
  rioplatense (`variable`, `entero`, `real`, `texto`, `boleano`, `si`, `sino`, `para`, `mostrar`,
  `verdad`, `falso`, `no`, `y`, `o`), para que programar se sienta más cercano.
- **Mensajes de error con humor argentino:** los errores semánticos tienen un tono informal
  rioplatense ("Che, la variable 'x' no existe", "No dividas por cero, crack"), para que
  programar sea menos frustrante.
- **Variable de control del `para` en scope global:** la variable declarada en el encabezado del
  `para` (`variable i : entero = 0`) queda disponible en la tabla de símbolos global, consistente
  con el resto del lenguaje que no implementa scoping por bloque.

## Compilación y ejecución

```bash
mvn clean package
mvn exec:java -Dexec.args="<archivo>"
```

## Ejemplos

```lang
// Declaraciones y expresiones
variable x : entero = 10;
variable y : real = 3.14;
variable ok : boleano = verdad;

mostrar(x * 2);
mostrar(y / 2.0);

// Condicional si-sino
si (x > 5) {
    mostrar("mayor");
} sino {
    mostrar("menor o igual");
}

// Operadores relacionales y logicos
variable a : boleano = x == 10;
variable b : boleano = no a y ok;

// Iteracion para (Variante 2)
para (variable i : entero = 0; i < 5; i = i + 1) {
    mostrar(i);
}
```

## Keywords en español

| inglés     | argentino   |
|----------------|-------------|
| `var`         | `variable`  |
| `int`         | `entero`    |
| `float`       | `real`      |
| `string`      | `texto`     |
| `bool`        | `boleano`   |
| `if`          | `si`        |
| `else`        | `sino`      |
| `for`         | `para`      |
| `print`       | `mostrar`   |
| `true`        | `verdad`    |
| `false`       | `falso`     |
| `!`           | `no`        |
| `&&`          | `y`         |
| `\|\|`        | `o`         |
