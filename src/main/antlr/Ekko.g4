grammar Ekko;

NEWLINE: ([\r] | [\n])+;
WS: (' ' | '\t' | NEWLINE)+ -> channel(HIDDEN);

LET: 'let';
IN: 'in';

LPAREN: '(';
RPAREN: ')';
EQ: '=';
COLON: ',';
BAR: '\\';
ARROW: '->';
GT: '>';
LT: '<';

// symbols
SYMBOL: SUM | SUB | TIMES | DIV | EQ | GT | LT | TURNED_A | INTERROGATION | AT | CIRCUMFLEX | EXCLAMATION | SIGN;
TURNED_A: '∀';
AT: '@';
AMPERSAND: '&';
CIRCUMFLEX: '^';
EXCLAMATION: '!';
SIGN: '$';
INTERROGATION: '?';
SUM: '+';
SUB: '-';
TIMES: '*';
DIV: '/';

IDENT: ['a-zA-Z_]['a-zA-Z0-9_]*;
STRING: '"' (~["\r\n\\] | '\\' ~[\r\n])* '"';
INT: [0-9]+ ;
DECIMAL: INT '.' INT;

symbolIdent: SYMBOL | SYMBOL symbolIdent;

ident: IDENT | LPAREN symbolIdent RPAREN;
infixIdent: IDENT | symbolIdent;

pat: name=ident # PVar;

alt: name=ident pat* EQ value=exp;

exp: LET alt (COLON alt)* IN value=exp # ELet
   | BAR param=pat ARROW value=exp     # EAbs
   | value=ident                       # EVar
   | value=STRING                      # EString
   | value=INT                         # EInt
   | value=DECIMAL                     # EDecimal
   | lhs=exp callee=infixIdent rhs=exp # EInfix
   | lhs=exp rhs=exp                   # EApp
   | LPAREN value=exp RPAREN           # EGroup;
