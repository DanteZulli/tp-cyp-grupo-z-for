grammar Language;

// ─── Parser Rules ────────────────────────────────────────────────────────────

program
    : statement* EOF
    ;

statement
    : varDecl ';'
    | assignment ';'
    | printStmt ';'
    ;

varDecl
    : 'var' ID ':' type ('=' expr)?
    ;

assignment
    : ID '=' expr
    ;

printStmt
    : 'print' '(' expr ')'
    ;

type
    : 'int'
    | 'float'
    | 'string'
    | 'bool'
    ;

expr
    : primary                                             # primaryExpr
    | '!' expr                                            # notExpr
    | '-' expr                                            # unaryMinusExpr
    | expr op=('*'|'/') expr                              # mulDivExpr
    | expr op=('+'|'-') expr                              # addSubExpr
    | expr op=('=='|'!='|'<'|'>'|'<='|'>=') expr          # relationalExpr
    | expr '&&' expr                                      # andExpr
    | expr '||' expr                                      # orExpr
    ;

primary
    : INT                                                 # intLiteral
    | FLOAT                                               # floatLiteral
    | STRING                                              # stringLiteral
    | 'true'                                              # trueLiteral
    | 'false'                                             # falseLiteral
    | ID                                                  # idRef
    | '(' expr ')'                                        # parenExpr
    ;

// ─── Lexer Rules ─────────────────────────────────────────────────────────────

FLOAT  : [0-9]+ '.' [0-9]+ ;
INT    : [0-9]+ ;
STRING : '"' (~["\r\n])* '"' ;
ID     : [a-zA-Z_][a-zA-Z0-9_]* ;

LINE_COMMENT  : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
WS            : [ \t\r\n]+ -> skip ;
