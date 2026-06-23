# Int�rprete con ANTLR

**Licenciatura en Sistemas � Conceptos y Paradigmas de Lenguajes de Programaci�n � 2026**

**Equipo docente:** Ing. Elida Leoni � Lic. Tomas Silvestre

## Integrantes

- Mendieta, Gabriel � 41805105
- Zulli, Dante � 45630783

## Variante asignada

**Variante 2: for** � Iteraci�n con variable de control.

## Descripci�n del lenguaje

Lenguaje imperativo simple con tipado est�tico expl�cito y **keywords en espa�ol rioplatense**.
Soporta `entero`, `real`, `texto` y `boleano`.
Las variables se declaran con `variable nombre : tipo (= valor)?`.
Expresiones aritm�ticas (`+`, `-`, `*`, `/`), relacionales (`==`, `!=`, `<`, `>`, `<=`, `>=`)
y l�gicas (`no`, `y`, `o`).
Estructuras de control: `si-sino` y la variante diferencial `para`.
Salida por consola con `mostrar()`.
Comentarios de l�nea (`//`) y bloque (`/* */`).

## Decisiones de dise�o

- **Visitor pattern:** Se utiliza `LanguageBaseVisitor` para recorrer el AST (tal como exige la
  gu�a), separando la l�gica en dos visitors: `SemanticAnalyzer` (validaci�n) e `Interpreter`
  (ejecuci�n).
- **Pipeline en 3 fases:** parseo (ANTLR) � an�lisis sem�ntico � interpretaci�n. Si hay errores
  sem�nticos no se ejecuta.
- **Tabla de s�mbolos compartida:** `SymbolTable` es una clase independiente usada por ambos
  visitors, con tipado mediante `enum Type`.
- **Type checking por visitor:** cada visitor de expresi�n retorna `SymbolTable.Type`, lo que
  permite verificar compatibilidad sin necesidad de un sistema de tipos separado.
- **Coerci�n impl�cita:** al asignar un entero a una variable `real`, el `Interpreter` convierte
  el valor autom�ticamente.
- **Sintaxis en espa�ol argentino:** todos los keywords del lenguaje est�n en espa�ol
  rioplatense (`variable`, `entero`, `real`, `texto`, `boleano`, `si`, `sino`, `para`, `mostrar`,
  `verdad`, `falso`, `no`, `y`, `o`), para que programar se sienta m�s cercano.
- **Mensajes de error con humor argentino:** los errores sem�nticos tienen un tono informal
  rioplatense ("Che, la variable 'x' no existe", "No dividas por cero, crack"), para que
  programar sea menos frustrante.
- **Variable de control del `para` en scope global:** la variable declarada en el encabezado del
  `para` (`variable i : entero = 0`) queda disponible en la tabla de s�mbolos global, consistente
  con el resto del lenguaje que no implementa scoping por bloque.

## Compilaci�n y ejecuci�n

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

## Keywords en espa�ol

| ingl�s     | argentino   |
|------------|-------------|
| `var`      | `variable`  |
| `int`      | `entero`    |
| `float`    | `real`      |
| `string`   | `texto`     |
| `bool`     | `boleano`   |
| `if`       | `si`        |
| `else`     | `sino`      |
| `for`      | `para`      |
| `print`    | `mostrar`   |
| `true`     | `verdad`    |
| `false`    | `falso`     |
| `!`        | `no`        |
| `&&`       | `y`         |
| `\|\|`     | `o`        |
