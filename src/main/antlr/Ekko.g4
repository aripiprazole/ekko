grammar Ekko;

NEWLINE: ([\r\n] | [\n])+;
WS: (' ' | '\t' | NEWLINE)+ -> channel(HIDDEN);

LET: 'let';
IN: 'in';

LPAREN: '(';
RPAREN: ')';
EQ: '=';
COLON: ',';
BAR: '\\';
ARROW: '->';

IDENT: ['a-zA-Z_]['a-zA-Z0-9_]*;
STRING: '"' (~["\r\n\\] | '\\' ~[\r\n])* '"';
INT: [0-9]+ ;
DECIMAL: INT '.' INT;

pat: name=IDENT # PVar;

alt: name=IDENT pat* EQ value=exp;

exp: LET alt (COLON alt)* IN value=exp # ELet
   | BAR param=pat ARROW value=exp     # EAbs
   | value=IDENT                       # EVar
   | value=STRING                      # EString
   | value=INT                         # EInt
   | value=DECIMAL                     # EDecimal
   | lhs=exp rhs=exp                   # EApp
   | LPAREN value=exp RPAREN           # EGroup;
