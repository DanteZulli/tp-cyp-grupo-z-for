package com.grupoz.interprete;

public class Interpreter extends LanguageBaseVisitor<Object> {

    private final SymbolTable symTable = new SymbolTable();

    @Override
    public Object visitProgram(LanguageParser.ProgramContext ctx) {
        for (var stmt : ctx.statement()) visit(stmt);
        return null;
    }

    @Override
    public Object visitBlock(LanguageParser.BlockContext ctx) {
        for (var stmt : ctx.statement()) visit(stmt);
        return null;
    }

    @Override
    public Object visitVarDecl(LanguageParser.VarDeclContext ctx) {
        String name = ctx.ID().getText();
        SymbolTable.Type type = SymbolTable.Type.fromString(ctx.type().getText());
        Object value = null;
        boolean initialized = false;
        if (ctx.expr() != null) {
            value = visit(ctx.expr());
            initialized = true;
        }
        symTable.declare(name, type, value, initialized);
        return null;
    }

    @Override
    public Object visitAssignment(LanguageParser.AssignmentContext ctx) {
        String name = ctx.ID().getText();
        Object value = visit(ctx.expr());
        SymbolTable.Type targetType = symTable.getType(name);
        if (value instanceof Integer && targetType == SymbolTable.Type.FLOAT) {
            value = ((Integer) value).doubleValue();
        }
        symTable.assign(name, value);
        return null;
    }

    @Override
    public Object visitPrintStmt(LanguageParser.PrintStmtContext ctx) {
        System.out.println(visit(ctx.expr()));
        return null;
    }

    @Override
    public Object visitIfStmt(LanguageParser.IfStmtContext ctx) {
        boolean condition = (boolean) visit(ctx.expr());
        if (condition) {
            visit(ctx.block(0));
        } else if (ctx.block().size() > 1) {
            visit(ctx.block(1));
        }
        return null;
    }

    // ── Expresiones ───────────────────────────────────────────────────────────

    @Override
    public Object visitPrimaryExpr(LanguageParser.PrimaryExprContext ctx) {
        return visit(ctx.primary());
    }

    @Override
    public Object visitNotExpr(LanguageParser.NotExprContext ctx) {
        return !(boolean) visit(ctx.expr());
    }

    @Override
    public Object visitUnaryMinusExpr(LanguageParser.UnaryMinusExprContext ctx) {
        Object val = visit(ctx.expr());
        return val instanceof Integer ? -(int) val : -(double) val;
    }

    @Override
    public Object visitMulDivExpr(LanguageParser.MulDivExprContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        boolean isFloat = left instanceof Double || right instanceof Double;
        return ctx.op.getText().equals("*")
            ? (isFloat ? toDouble(left) * toDouble(right) : (int) left * (int) right)
            : (isFloat ? toDouble(left) / toDouble(right) : (int) left / (int) right);
    }

    @Override
    public Object visitAddSubExpr(LanguageParser.AddSubExprContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        boolean isFloat = left instanceof Double || right instanceof Double;
        return ctx.op.getText().equals("+")
            ? (isFloat ? toDouble(left) + toDouble(right) : (int) left + (int) right)
            : (isFloat ? toDouble(left) - toDouble(right) : (int) left - (int) right);
    }

    @Override
    public Object visitRelationalExpr(LanguageParser.RelationalExprContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        String op = ctx.op.getText();
        if (left instanceof String) {
            return op.equals("==") ? left.equals(right) : !left.equals(right);
        }
        double l = toDouble(left), r = toDouble(right);
        return switch (op) {
            case "<"  -> l < r;
            case ">"  -> l > r;
            case "<=" -> l <= r;
            case ">=" -> l >= r;
            case "==" -> l == r;
            case "!=" -> l != r;
            default   -> false;
        };
    }

    @Override
    public Object visitAndExpr(LanguageParser.AndExprContext ctx) {
        return (boolean) visit(ctx.expr(0)) && (boolean) visit(ctx.expr(1));
    }

    @Override
    public Object visitOrExpr(LanguageParser.OrExprContext ctx) {
        return (boolean) visit(ctx.expr(0)) || (boolean) visit(ctx.expr(1));
    }

    // ── Primary ───────────────────────────────────────────────────────────────

    @Override
    public Object visitIntLiteral(LanguageParser.IntLiteralContext ctx) {
        return Integer.parseInt(ctx.INT().getText());
    }

    @Override
    public Object visitFloatLiteral(LanguageParser.FloatLiteralContext ctx) {
        return Double.parseDouble(ctx.FLOAT().getText());
    }

    @Override
    public Object visitStringLiteral(LanguageParser.StringLiteralContext ctx) {
        String raw = ctx.STRING().getText();
        return raw.substring(1, raw.length() - 1);
    }

    @Override
    public Object visitTrueLiteral(LanguageParser.TrueLiteralContext ctx) {
        return true;
    }

    @Override
    public Object visitFalseLiteral(LanguageParser.FalseLiteralContext ctx) {
        return false;
    }

    @Override
    public Object visitIdRef(LanguageParser.IdRefContext ctx) {
        return symTable.getValue(ctx.ID().getText());
    }

    @Override
    public Object visitParenExpr(LanguageParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double toDouble(Object val) {
        return val instanceof Integer ? ((Integer) val).doubleValue() : (double) val;
    }
}
