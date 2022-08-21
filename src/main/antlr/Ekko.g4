grammar Ekko;

NEWLINE: ([\r\n] | [\n])+;
WS: (' ' | '\t' | NEWLINE)+ -> channel(HIDDEN);

LET: 'let';
IN: 'in';

LPAREN: '(';
RPAREN: ')';
EQ: '=';
COLON: ',';

IDENT: ['a-zA-Z_]['a-zA-Z0-9_]*;
STRING: '"' (~["\r\n\\] | '\\' ~[\r\n])* '"';
INT: [0-9]+ ;
DECIMAL: INT '.' INT;

variable: name=IDENT EQ value=exp;
variableList: variable (COLON variable)*;

exp: LET names=variableList IN value=exp # ELet
   | value=IDENT                         # EVar
   | value=STRING                        # EStr
   | value=INT                           # EInt
   | value=DECIMAL                       # EDecimal
   | lhs=exp rhs=exp                     # EApp
   | LPAREN value=exp RPAREN             # EGroup;
