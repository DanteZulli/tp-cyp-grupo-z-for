grammar Language;

// ─── Parser Rules ────────────────────────────────────────────────────────────

programa: sentencia* EOF;

sentencia:
	declaracionVariable ';'
	| asignacion ';'
	| mostrarStmt ';'
	| siStmt
	| paraStmt;

declaracionVariable: 'variable' ID ':' tipo ('=' expresion)?;

asignacion: ID '=' expresion;

mostrarStmt: 'mostrar' '(' expresion ')';

bloque: '{' sentencia* '}';

siStmt: 'si' '(' expresion ')' bloque ('sino' bloque)?;

paraStmt: 'para' '(' declaracionVariable ';' expresion ';' asignacion ')' bloque;

tipo: 'entero' | 'real' | 'texto' | 'boleano';

expresion:
	primaria													# primariaExp
	| 'no' expresion											# noExp
	| '-' expresion												# menosUnarioExp
	| expresion op = ('*' | '/') expresion						# mulDivExp
	| expresion op = ('+' | '-') expresion						# sumaRestExp
	| expresion op = ('==' | '!=' | '<' | '>' | '<=' | '>=') expresion	# relacionalExp
	| expresion 'y' expresion									# yExp
	| expresion 'o' expresion									# oExp;

primaria:
	ENTERO				# enteroLiteral
	| REAL				# realLiteral
	| TEXTO				# textoLiteral
	| 'verdad'			# verdadLiteral
	| 'falso'			# falsoLiteral
	| ID				# idRef
	| '(' expresion ')'	# parenExp;

// ─── Lexer Rules ─────────────────────────────────────────────────────────────

REAL: [0-9]+ '.' [0-9]+;
ENTERO: [0-9]+;
TEXTO: '"' (~["\r\n])* '"';
ID: [a-zA-Z_][a-zA-Z0-9_]*;

LINE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t\r\n]+ -> skip;
