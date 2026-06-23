package com.grupoz.interprete;

/**
 * Interprete del lenguaje. Recorre el AST (Abstract Syntax Tree)
 * que genera ANTLR y ejecuta cada instruccion:
 * declaraciones, asignaciones, expresiones, mostrar, si-sino y para.
 * Utiliza SymbolTable para almacenar y recuperar valores.
 */
public class Interpreter extends LanguageBaseVisitor<Object> {

    private final SymbolTable symTable = new SymbolTable();

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
        SymbolTable.Type type = SymbolTable.Type.fromString(ctx.tipo().getText());
        Object value = null;
        boolean initialized = false;
        if (ctx.expresion() != null) {
            value = visit(ctx.expresion());
            initialized = true;
        }
        symTable.declare(name, type, value, initialized);
        return null;
    }

    @Override
    public Object visitAsignacion(LanguageParser.AsignacionContext ctx) {
        String name = ctx.ID().getText();
        Object value = visit(ctx.expresion());
        SymbolTable.Type targetType = symTable.getType(name);
        if (value instanceof Integer && targetType == SymbolTable.Type.FLOAT) {
            value = ((Integer) value).doubleValue();
        }
        symTable.assign(name, value);
        return null;
    }

    @Override
    public Object visitMostrarStmt(LanguageParser.MostrarStmtContext ctx) {
        System.out.println(visit(ctx.expresion()));
        return null;
    }

    @Override
    public Object visitSiStmt(LanguageParser.SiStmtContext ctx) {
        boolean condition = (boolean) visit(ctx.expresion());
        if (condition) {
            visit(ctx.bloque(0));
        } else if (ctx.bloque().size() > 1) {
            visit(ctx.bloque(1));
        }
        return null;
    }

    @Override
    public Object visitParaStmt(LanguageParser.ParaStmtContext ctx) {
        visit(ctx.declaracionVariable());
        while ((boolean) visit(ctx.expresion())) {
            visit(ctx.bloque());
            visit(ctx.asignacion());
        }
        return null;
    }

    // ── Expresiones ───────────────────────────────────────────────────────────

    @Override
    public Object visitPrimariaExp(LanguageParser.PrimariaExpContext ctx) {
        return visit(ctx.primaria());
    }

    @Override
    public Object visitNoExp(LanguageParser.NoExpContext ctx) {
        return !(boolean) visit(ctx.expresion());
    }

    @Override
    public Object visitMenosUnarioExp(LanguageParser.MenosUnarioExpContext ctx) {
        Object val = visit(ctx.expresion());
        return val instanceof Integer ? -(int) val : -(double) val;
    }

    @Override
    public Object visitMulDivExp(LanguageParser.MulDivExpContext ctx) {
        Object left = visit(ctx.expresion(0));
        Object right = visit(ctx.expresion(1));
        boolean isFloat = left instanceof Double || right instanceof Double;
        if (isFloat) {
            return ctx.op.getText().equals("*")
                    ? toDouble(left) * toDouble(right)
                    : toDouble(left) / toDouble(right);
        }
        return ctx.op.getText().equals("*")
                ? (int) left * (int) right
                : (int) left / (int) right;
    }

    @Override
    public Object visitSumaRestExp(LanguageParser.SumaRestExpContext ctx) {
        Object left = visit(ctx.expresion(0));
        Object right = visit(ctx.expresion(1));
        boolean isFloat = left instanceof Double || right instanceof Double;
        if (isFloat) {
            return ctx.op.getText().equals("+")
                    ? toDouble(left) + toDouble(right)
                    : toDouble(left) - toDouble(right);
        }
        return ctx.op.getText().equals("+")
                ? (int) left + (int) right
                : (int) left - (int) right;
    }

    @Override
    public Object visitRelacionalExp(LanguageParser.RelacionalExpContext ctx) {
        Object left = visit(ctx.expresion(0));
        Object right = visit(ctx.expresion(1));
        String op = ctx.op.getText();
        if (left instanceof String) {
            return op.equals("==") ? left.equals(right) : !left.equals(right);
        }
        double l = toDouble(left), r = toDouble(right);
        return switch (op) {
            case "<" -> l < r;
            case ">" -> l > r;
            case "<=" -> l <= r;
            case ">=" -> l >= r;
            case "==" -> l == r;
            case "!=" -> l != r;
            default -> false;
        };
    }

    @Override
    public Object visitYExp(LanguageParser.YExpContext ctx) {
        return (boolean) visit(ctx.expresion(0)) && (boolean) visit(ctx.expresion(1));
    }

    @Override
    public Object visitOExp(LanguageParser.OExpContext ctx) {
        return (boolean) visit(ctx.expresion(0)) || (boolean) visit(ctx.expresion(1));
    }

    // ── Primaria ──────────────────────────────────────────────────────────────

    @Override
    public Object visitEnteroLiteral(LanguageParser.EnteroLiteralContext ctx) {
        return Integer.parseInt(ctx.ENTERO().getText());
    }

    @Override
    public Object visitRealLiteral(LanguageParser.RealLiteralContext ctx) {
        return Double.parseDouble(ctx.REAL().getText());
    }

    @Override
    public Object visitTextoLiteral(LanguageParser.TextoLiteralContext ctx) {
        String raw = ctx.TEXTO().getText();
        return raw.substring(1, raw.length() - 1);
    }

    @Override
    public Object visitVerdadLiteral(LanguageParser.VerdadLiteralContext ctx) {
        return true;
    }

    @Override
    public Object visitFalsoLiteral(LanguageParser.FalsoLiteralContext ctx) {
        return false;
    }

    @Override
    public Object visitIdRef(LanguageParser.IdRefContext ctx) {
        return symTable.getValue(ctx.ID().getText());
    }

    @Override
    public Object visitParenExp(LanguageParser.ParenExpContext ctx) {
        return visit(ctx.expresion());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double toDouble(Object val) {
        return val instanceof Integer ? ((Integer) val).doubleValue() : (double) val;
    }
}
