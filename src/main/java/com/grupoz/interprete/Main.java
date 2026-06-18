package com.grupoz.interprete;

import java.io.IOException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Main {

    private static final String EXTENSION = "lang";
    private static final String DIRBASE = "src/test/resources/";

    public static void main(String[] args) throws IOException {
        String files[] = args.length==0? new String[]{ "test." + EXTENSION } : args;
        System.out.println("Dirbase: " + DIRBASE);
        for (String file : files){
            System.out.println("START: " + file);

            CharStream in = CharStreams.fromFileName(DIRBASE + file);
            LanguageLexer lexer = new LanguageLexer(in);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            LanguageParser parser = new LanguageParser(tokens);
            LanguageParser.ProgramContext tree = parser.program();

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

            Interpreter interpreter = new Interpreter();
            interpreter.visit(tree);

            System.out.println("FINISH: " + file);
        }
    }
}
