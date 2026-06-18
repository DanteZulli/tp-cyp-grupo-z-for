package com.grupoz.interprete;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Analizador semantico. Recorre el AST (Abstract Syntax Tree) validando tipos,
 * redeclaraciones, variables no declaradas, operaciones invalidas y division
 * por cero.
 * No ejecuta el programa, solo acumula errores.
 */
public class SemanticAnalyzer extends LanguageBaseVisitor<Object> {

    private final SymbolTable symTable = new SymbolTable();
    @Getter
    private final List<String> errors = new ArrayList<>();

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public Object visitProgram(LanguageParser.ProgramContext ctx) {
        for (var stmt : ctx.statement())
            visit(stmt);
        return null;
    }

    @Override
    public Object visitBlock(LanguageParser.BlockContext ctx) {
        for (var stmt : ctx.statement())
            visit(stmt);
        return null;
    }

    @Override
    public Object visitVarDecl(LanguageParser.VarDeclContext ctx) {
        String name = ctx.ID().getText();
        SymbolTable.Type declType = SymbolTable.Type.fromString(ctx.type().getText());

        if (symTable.exists(name)) {
            errors.add("Variable '" + name + "' ya declarada (linea " + ctx.getStart().getLine() + ")");
            return null;
        }

        symTable.declare(name, declType, null, false);

        if (ctx.expr() != null) {
            SymbolTable.Type exprType = (SymbolTable.Type) visit(ctx.expr());
            if (exprType != declType) {
                errors.add("Error de tipo (linea " + ctx.getStart().getLine()
                        + "): no se puede asignar " + exprType + " a variable " + declType);
            } else {
                symTable.assign(name, null);
            }
        }
        return null;
    }

    @Override
    public Object visitAssignment(LanguageParser.AssignmentContext ctx) {
        String name = ctx.ID().getText();

        if (!symTable.exists(name)) {
            errors.add("Variable '" + name + "' no declarada (linea " + ctx.getStart().getLine() + ")");
            return null;
        }

        SymbolTable.Type varType = symTable.getType(name);
        SymbolTable.Type exprType = (SymbolTable.Type) visit(ctx.expr());

        if (exprType != varType) {
            errors.add("Error de tipo (linea " + ctx.getStart().getLine()
                    + "): no se puede asignar " + exprType + " a variable " + varType);
        }
        return null;
    }

    @Override
    public Object visitPrintStmt(LanguageParser.PrintStmtContext ctx) {
        visit(ctx.expr());
        return null;
    }

    @Override
    public Object visitIfStmt(LanguageParser.IfStmtContext ctx) {
        SymbolTable.Type condType = (SymbolTable.Type) visit(ctx.expr());
        if (condType != SymbolTable.Type.BOOL) {
            errors.add("Error de tipo (linea " + ctx.getStart().getLine()
                    + "): condicion if debe ser bool, no " + condType);
        }
        visit(ctx.block(0));
        if (ctx.block().size() > 1)
            visit(ctx.block(1));
        return null;
    }

    // ── Expresiones ───────────────────────────────────────────────────────────

    @Override
    public Object visitPrimaryExpr(LanguageParser.PrimaryExprContext ctx) {
        return visit(ctx.primary());
    }

    @Override
    public Object visitNotExpr(LanguageParser.NotExprContext ctx) {
        SymbolTable.Type t = (SymbolTable.Type) visit(ctx.expr());
        if (t != SymbolTable.Type.BOOL) {
            errors.add("Error de tipo (linea " + ctx.getStart().getLine()
                    + "): ! requiere bool, no " + t);
        }
        return SymbolTable.Type.BOOL;
    }

    @Override
    public Object visitUnaryMinusExpr(LanguageParser.UnaryMinusExprContext ctx) {
        SymbolTable.Type t = (SymbolTable.Type) visit(ctx.expr());
        if (!t.isNumeric()) {
            errors.add("Error de tipo (linea " + ctx.getStart().getLine()
                    + "): - requiere valor numerico, no " + t);
        }
        return t;
    }

    @Override
    public Object visitMulDivExpr(LanguageParser.MulDivExprContext ctx) {
        SymbolTable.Type left = (SymbolTable.Type) visit(ctx.expr(0));
        SymbolTable.Type right = (SymbolTable.Type) visit(ctx.expr(1));

        if (!left.isNumeric() || !right.isNumeric()) {
            errors.add("Error de tipo (linea " + ctx.getStart().getLine()
                    + "): */ requiere operandos numericos");
            return SymbolTable.Type.INT;
        }

        if (ctx.op.getText().equals("/")) {
            LanguageParser.ExprContext rightExpr = ctx.expr(1);
            if (rightExpr instanceof LanguageParser.PrimaryExprContext primCtx) {
                LanguageParser.PrimaryContext prim = primCtx.primary();
                if (prim instanceof LanguageParser.IntLiteralContext intCtx) {
                    if (Integer.parseInt(intCtx.INT().getText()) == 0) {
                        errors.add("Division por cero (linea " + ctx.getStart().getLine() + ")");
                    }
                } else if (prim instanceof LanguageParser.FloatLiteralContext flCtx) {
                    if (Double.parseDouble(flCtx.FLOAT().getText()) == 0.0) {
                        errors.add("Division por cero (linea " + ctx.getStart().getLine() + ")");
                    }
                }
            }
        }

        return left == SymbolTable.Type.FLOAT || right == SymbolTable.Type.FLOAT
                ? SymbolTable.Type.FLOAT
                : SymbolTable.Type.INT;
    }

    @Override
    public Object visitAddSubExpr(LanguageParser.AddSubExprContext ctx) {
        SymbolTable.Type left = (SymbolTable.Type) visit(ctx.expr(0));
        SymbolTable.Type right = (SymbolTable.Type) visit(ctx.expr(1));

        if (!left.isNumeric() || !right.isNumeric()) {
            errors.add("Error de tipo (linea " + ctx.getStart().getLine()
                    + "): +- requiere operandos numericos");
            return SymbolTable.Type.INT;
        }

        return left == SymbolTable.Type.FLOAT || right == SymbolTable.Type.FLOAT
                ? SymbolTable.Type.FLOAT
                : SymbolTable.Type.INT;
    }

    @Override
    public Object visitRelationalExpr(LanguageParser.RelationalExprContext ctx) {
        SymbolTable.Type left = (SymbolTable.Type) visit(ctx.expr(0));
        SymbolTable.Type right = (SymbolTable.Type) visit(ctx.expr(1));

        if (left == SymbolTable.Type.STRING || right == SymbolTable.Type.STRING) {
            String op = ctx.op.getText();
            if (!op.equals("==") && !op.equals("!=")) {
                errors.add("Error de tipo (linea " + ctx.getStart().getLine()
                        + "): no se puede usar " + op + " con strings");
            }
        } else if (!left.isNumeric() || !right.isNumeric()) {
            errors.add("Error de tipo (linea " + ctx.getStart().getLine()
                    + "): operadores relacionales requieren tipos comparables");
        }

        return SymbolTable.Type.BOOL;
    }

    @Override
    public Object visitAndExpr(LanguageParser.AndExprContext ctx) {
        checkBooleanOp(ctx.expr(0), ctx.expr(1), ctx.getStart().getLine());
        return SymbolTable.Type.BOOL;
    }

    @Override
    public Object visitOrExpr(LanguageParser.OrExprContext ctx) {
        checkBooleanOp(ctx.expr(0), ctx.expr(1), ctx.getStart().getLine());
        return SymbolTable.Type.BOOL;
    }

    private void checkBooleanOp(LanguageParser.ExprContext left, LanguageParser.ExprContext right, int line) {
        SymbolTable.Type lt = (SymbolTable.Type) visit(left);
        SymbolTable.Type rt = (SymbolTable.Type) visit(right);
        if (lt != SymbolTable.Type.BOOL || rt != SymbolTable.Type.BOOL) {
            errors.add("Error de tipo (linea " + line + "): operadores logicos requieren bool");
        }
    }

    // ── Primary ───────────────────────────────────────────────────────────────

    @Override
    public Object visitIntLiteral(LanguageParser.IntLiteralContext ctx) {
        return SymbolTable.Type.INT;
    }

    @Override
    public Object visitFloatLiteral(LanguageParser.FloatLiteralContext ctx) {
        return SymbolTable.Type.FLOAT;
    }

    @Override
    public Object visitStringLiteral(LanguageParser.StringLiteralContext ctx) {
        return SymbolTable.Type.STRING;
    }

    @Override
    public Object visitTrueLiteral(LanguageParser.TrueLiteralContext ctx) {
        return SymbolTable.Type.BOOL;
    }

    @Override
    public Object visitFalseLiteral(LanguageParser.FalseLiteralContext ctx) {
        return SymbolTable.Type.BOOL;
    }

    @Override
    public Object visitIdRef(LanguageParser.IdRefContext ctx) {
        String name = ctx.ID().getText();
        if (!symTable.exists(name)) {
            errors.add("Variable '" + name + "' no declarada (linea " + ctx.getStart().getLine() + ")");
            return SymbolTable.Type.INT;
        }
        return symTable.getType(name);
    }

    @Override
    public Object visitParenExpr(LanguageParser.ParenExprContext ctx) {
        return visit(ctx.expr());
    }
}
