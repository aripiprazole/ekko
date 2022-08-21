grammar Ekko;

NEWLINE: ([\r\n] | [\n])+;
WS: (' ' | '\t' | NEWLINE)+ -> channel(HIDDEN);

IDENT: ['a-zA-Z_]['a-zA-Z0-9_]*;
STRING: '"' (~["\r\n\\] | '\\' ~[\r\n])* '"';
INT: [0-9]+ ;
DECIMAL: INT '.' INT;

LPAREN: '(';
RPAREN: ')';

exp: IDENT             # EVar
   | STRING            # EStr
   | INT               # EInt
   | DECIMAL           # EDecimal
   | LPAREN exp RPAREN # EGroup
   | exp exp           # EApp;
