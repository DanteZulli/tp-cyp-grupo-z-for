grammar Language;

// ─── Parser Rules ────────────────────────────────────────────────────────────

program
    : statement* EOF
    ;

statement
    : varDecl ';'
    | assignment ';'
    ;

// var nombre : tipo
// var nombre : tipo = literal
varDecl
    : 'var' ID ':' type ('=' literal)?
    ;

assignment
    : ID '=' literal
    ;

type
    : 'int'
    | 'float'
    | 'string'
    | 'bool'
    ;

literal
    : FLOAT
    | INT
    | STRING
    | 'true'
    | 'false'
    ;

// ─── Lexer Rules ─────────────────────────────────────────────────────────────

// FLOAT antes que INT para que 3.14 no matchee como INT '.' INT
FLOAT  : [0-9]+ '.' [0-9]+ ;
INT    : [0-9]+ ;
STRING : '"' (~["\r\n])* '"' ;
ID     : [a-zA-Z_][a-zA-Z0-9_]* ;

LINE_COMMENT  : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;

WS : [ \t\r\n]+ -> skip ;
