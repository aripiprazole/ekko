grammar Ekko;

NEWLINE: ([\r\n] | [\n])+;
WS: (' ' | '\t' | NEWLINE)+ -> channel(HIDDEN);

IDENT: ['a-zA-Z_]['a-zA-Z0-9_]*;
STRING: '"' (~["\r\n\\] | '\\' ~[\r\n])* '"';
INT: [0-9]+ ;
DECIMAL: INT '.' INT;

LPAREN: '(';
RPAREN: ')';

exp: value=IDENT             # EVar
   | value=STRING            # EStr
   | value=INT               # EInt
   | value=DECIMAL           # EDecimal
   | LPAREN value=exp RPAREN # EGroup
   | lhs=exp rhs=exp         # EApp;
