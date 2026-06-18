package com.grupoz.interprete;

import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Punto de entrada del interprete.
 * Orquesta las 3 fases de ejecucion: parseo, analisis semantico e
 * interpretacion.
 */
public class Main {

    private static final String EXTENSION = "lang";
    private static final String DIRBASE = "src/test/resources/";

    public static void main(String[] args) throws IOException {
        String files[] = args.length == 0 ? new String[] { "test." + EXTENSION } : args;
        System.out.println("Dirbase: " + DIRBASE);
        for (String file : files) {
            System.out.println("START: " + file);

            // ── Fase 1: Analisis lexico y sintactico (ANTLR) ──
            // ANTLR genera el lexer y parser a partir de Language.g4.
            // El lexer tokeniza el archivo y el parser construye el AST (Abstract Syntax
            // Tree).
            CharStream in = CharStreams.fromFileName(DIRBASE + file);
            LanguageLexer lexer = new LanguageLexer(in);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            LanguageParser parser = new LanguageParser(tokens);
            LanguageParser.ProgramContext tree = parser.program();

            // ── Fase 2: Analisis semantico ──
            // El SemanticAnalyzer recorre el AST y verifica tipos,
            // redeclaraciones, variables no declaradas y division por cero.
            // Si encuentra errores, los muestra y pasa al siguiente archivo.
            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.visit(tree);
            if (analyzer.hasErrors()) {
                System.out.println("Errores semanticos:");
                for (String err : analyzer.getErrors()) {
                    System.out.println("  " + err);
                }
                System.out.println("FINISH (con errores): " + file);
                continue;
            }

            // ── Fase 3: Interpretacion ──
            // El Interpreter recorre el AST y ejecuta cada instruccion:
            // declara variables, evalua expresiones, imprime resultados,
            // y controla el flujo con if-else.
            Interpreter interpreter = new Interpreter();
            interpreter.visit(tree);

            System.out.println("FINISH: " + file);
        }
    }
}
