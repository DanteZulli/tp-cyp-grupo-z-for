package com.grupoz.interprete;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Analizador semantico. Recorre el AST (Abstract Syntax Tree) validando tipos,
 * redeclaraciones, variables no declaradas, operaciones invalidas y division
 * por cero.
 * No ejecuta el programa, solo acumula errores (con humor argentino).
 */
public class SemanticAnalyzer extends LanguageBaseVisitor<Object> {

    private final SymbolTable symTable = new SymbolTable();
    @Getter
    private final List<String> errors = new ArrayList<>();

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public Object visitPrograma(LanguageParser.ProgramaContext ctx) {
        for (var stmt : ctx.sentencia())
            visit(stmt);
        return null;
    }

    @Override
    public Object visitBloque(LanguageParser.BloqueContext ctx) {
        for (var stmt : ctx.sentencia())
            visit(stmt);
        return null;
    }

    @Override
    public Object visitDeclaracionVariable(LanguageParser.DeclaracionVariableContext ctx) {
        String name = ctx.ID().getText();
        SymbolTable.Type declType = SymbolTable.Type.fromString(ctx.tipo().getText());

        if (symTable.exists(name)) {
            errors.add("Para par� para, ya declaraste '" + name + "' antes (linea "
                    + ctx.getStart().getLine() + "). No abuses.");
            return null;
        }

        symTable.declare(name, declType, null, false);

        if (ctx.expresion() != null) {
            SymbolTable.Type exprType = (SymbolTable.Type) visit(ctx.expresion());
            if (exprType != declType) {
                errors.add("Uh amigo, no pod�s asignar " + exprType + " a una variable "
                        + declType + "... no da. (linea " + ctx.getStart().getLine() + ")");
            } else {
                symTable.assign(name, null);
            }
        }
        return null;
    }

    @Override
    public Object visitAsignacion(LanguageParser.AsignacionContext ctx) {
        String name = ctx.ID().getText();

        if (!symTable.exists(name)) {
            errors.add("Che, la variable '" + name + "' no existe. �D�nde la viste? (linea "
                    + ctx.getStart().getLine() + ")");
            return null;
        }

        SymbolTable.Type varType = symTable.getType(name);
        SymbolTable.Type exprType = (SymbolTable.Type) visit(ctx.expresion());

        if (exprType != varType) {
            errors.add("Est�s mezclando peras con manzanas: " + exprType + " no es " + varType
                    + " (linea " + ctx.getStart().getLine() + ")");
        }
        return null;
    }

    @Override
    public Object visitMostrarStmt(LanguageParser.MostrarStmtContext ctx) {
        visit(ctx.expresion());
        return null;
    }

    @Override
    public Object visitSiStmt(LanguageParser.SiStmtContext ctx) {
        SymbolTable.Type condType = (SymbolTable.Type) visit(ctx.expresion());
        if (condType != SymbolTable.Type.BOOL) {
            errors.add("La condici�n del si tiene que ser booleana, no "
                    + condType + ". No te hagas el p�caro. (linea " + ctx.getStart().getLine() + ")");
        }
        visit(ctx.bloque(0));
        if (ctx.bloque().size() > 1)
            visit(ctx.bloque(1));
        return null;
    }

    @Override
    public Object visitParaStmt(LanguageParser.ParaStmtContext ctx) {
        visit(ctx.declaracionVariable());
        SymbolTable.Type condType = (SymbolTable.Type) visit(ctx.expresion());
        if (condType != SymbolTable.Type.BOOL) {
            errors.add("La condici�n del para tiene que ser booleana, no "
                    + condType + ". No te pases de vivo. (linea " + ctx.getStart().getLine() + ")");
        }
        visit(ctx.asignacion());
        visit(ctx.bloque());
        return null;
    }

    // ── Expresiones ───────────────────────────────────────────────────────────

    @Override
    public Object visitPrimariaExp(LanguageParser.PrimariaExpContext ctx) {
        return visit(ctx.primaria());
    }

    @Override
    public Object visitNoExp(LanguageParser.NoExpContext ctx) {
        SymbolTable.Type t = (SymbolTable.Type) visit(ctx.expresion());
        if (t != SymbolTable.Type.BOOL) {
            errors.add("no es solo para booleanos, no para " + t
                    + ". No seas rebelde. (linea " + ctx.getStart().getLine() + ")");
        }
        return SymbolTable.Type.BOOL;
    }

    @Override
    public Object visitMenosUnarioExp(LanguageParser.MenosUnarioExpContext ctx) {
        SymbolTable.Type t = (SymbolTable.Type) visit(ctx.expresion());
        if (!t.isNumeric()) {
            errors.add("- requiere un n�mero, no " + t
                    + ". No te hagas el astuto. (linea " + ctx.getStart().getLine() + ")");
        }
        return t;
    }

    @Override
    public Object visitMulDivExp(LanguageParser.MulDivExpContext ctx) {
        SymbolTable.Type left = (SymbolTable.Type) visit(ctx.expresion(0));
        SymbolTable.Type right = (SymbolTable.Type) visit(ctx.expresion(1));

        if (!left.isNumeric() || !right.isNumeric()) {
            errors.add("* y / solo funcionan con n�meros, y vos pasaste "
                    + left + " y " + right + ". No da. (linea " + ctx.getStart().getLine() + ")");
            return SymbolTable.Type.INT;
        }

        if (ctx.op.getText().equals("/")) {
            LanguageParser.ExpresionContext rightExpr = ctx.expresion(1);
            if (rightExpr instanceof LanguageParser.PrimariaExpContext primCtx) {
                LanguageParser.PrimariaContext prim = primCtx.primaria();
                if (prim instanceof LanguageParser.EnteroLiteralContext intCtx) {
                    if (Integer.parseInt(intCtx.ENTERO().getText()) == 0) {
                        errors.add("No dividas por cero, crack. No es f�sicamente posible. (linea "
                                + ctx.getStart().getLine() + ")");
                    }
                } else if (prim instanceof LanguageParser.RealLiteralContext flCtx) {
                    if (Double.parseDouble(flCtx.REAL().getText()) == 0.0) {
                        errors.add("No dividas por cero, crack. No es f�sicamente posible. (linea "
                                + ctx.getStart().getLine() + ")");
                    }
                }
            }
        }

        return left == SymbolTable.Type.FLOAT || right == SymbolTable.Type.FLOAT
                ? SymbolTable.Type.FLOAT
                : SymbolTable.Type.INT;
    }

    @Override
    public Object visitSumaRestExp(LanguageParser.SumaRestExpContext ctx) {
        SymbolTable.Type left = (SymbolTable.Type) visit(ctx.expresion(0));
        SymbolTable.Type right = (SymbolTable.Type) visit(ctx.expresion(1));

        if (!left.isNumeric() || !right.isNumeric()) {
            errors.add(left + " y " + right + " no se pueden sumar/restar. No da ni a palos. (linea "
                    + ctx.getStart().getLine() + ")");
            return SymbolTable.Type.INT;
        }

        return left == SymbolTable.Type.FLOAT || right == SymbolTable.Type.FLOAT
                ? SymbolTable.Type.FLOAT
                : SymbolTable.Type.INT;
    }

    @Override
    public Object visitRelacionalExp(LanguageParser.RelacionalExpContext ctx) {
        SymbolTable.Type left = (SymbolTable.Type) visit(ctx.expresion(0));
        SymbolTable.Type right = (SymbolTable.Type) visit(ctx.expresion(1));

        if (left == SymbolTable.Type.STRING || right == SymbolTable.Type.STRING) {
            String op = ctx.op.getText();
            if (!op.equals("==") && !op.equals("!=")) {
                errors.add("Con texto solo se puede usar == o !=, no "
                        + op + ". No inventes, loco. (linea " + ctx.getStart().getLine() + ")");
            }
        } else if (!left.isNumeric() || !right.isNumeric()) {
            errors.add("Estos tipos no se pueden comparar ni en pedo. (linea "
                    + ctx.getStart().getLine() + ")");
        }

        return SymbolTable.Type.BOOL;
    }

    @Override
    public Object visitYExp(LanguageParser.YExpContext ctx) {
        checkBooleanOp(ctx.expresion(0), ctx.expresion(1), ctx.getStart().getLine());
        return SymbolTable.Type.BOOL;
    }

    @Override
    public Object visitOExp(LanguageParser.OExpContext ctx) {
        checkBooleanOp(ctx.expresion(0), ctx.expresion(1), ctx.getStart().getLine());
        return SymbolTable.Type.BOOL;
    }

    private void checkBooleanOp(LanguageParser.ExpresionContext left, LanguageParser.ExpresionContext right, int line) {
        SymbolTable.Type lt = (SymbolTable.Type) visit(left);
        SymbolTable.Type rt = (SymbolTable.Type) visit(right);
        if (lt != SymbolTable.Type.BOOL || rt != SymbolTable.Type.BOOL) {
            errors.add("y y o son solo para booleanos. No da mezclar. (linea " + line + ")");
        }
    }

    // ── Primaria ──────────────────────────────────────────────────────────────

    @Override
    public Object visitEnteroLiteral(LanguageParser.EnteroLiteralContext ctx) {
        return SymbolTable.Type.INT;
    }

    @Override
    public Object visitRealLiteral(LanguageParser.RealLiteralContext ctx) {
        return SymbolTable.Type.FLOAT;
    }

    @Override
    public Object visitTextoLiteral(LanguageParser.TextoLiteralContext ctx) {
        return SymbolTable.Type.STRING;
    }

    @Override
    public Object visitVerdadLiteral(LanguageParser.VerdadLiteralContext ctx) {
        return SymbolTable.Type.BOOL;
    }

    @Override
    public Object visitFalsoLiteral(LanguageParser.FalsoLiteralContext ctx) {
        return SymbolTable.Type.BOOL;
    }

    @Override
    public Object visitIdRef(LanguageParser.IdRefContext ctx) {
        String name = ctx.ID().getText();
        if (!symTable.exists(name)) {
            errors.add("Che, la variable '" + name + "' no existe. �Est�s seguro de que la declaraste? (linea "
                    + ctx.getStart().getLine() + ")");
            return SymbolTable.Type.INT;
        }
        return symTable.getType(name);
    }

    @Override
    public Object visitParenExp(LanguageParser.ParenExpContext ctx) {
        return visit(ctx.expresion());
    }
}
