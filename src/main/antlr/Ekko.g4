grammar Ekko;

NEWLINE: ([\r] | [\n])+;
WS: (' ' | '\t' | NEWLINE)+ -> channel(HIDDEN);

LET: 'let';
IN: 'in';

LPAREN: '(';
RPAREN: ')';
EQ: '=';
COLON: ':';
COMMA: ',';
BAR: '\\';
ARROW: '->';
GT: '>';
LT: '<';

// symbols
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

FORALL: TURNED_A | 'forall';

IDENT: ['a-zA-Z_]['a-zA-Z0-9_]*;
STRING: '"' (~["\r\n\\] | '\\' ~[\r\n])* '"';
INT: [0-9]+ ;
DECIMAL: INT '.' INT;

// This is not a lexer rule, but a parser rule, to avoid precedence problems.
symbol: SUM | SUB | TIMES | DIV | EQ | GT | LT | TURNED_A | INTERROGATION | AT | CIRCUMFLEX | EXCLAMATION | SIGN;
symbolIdent: symbol | symbol symbolIdent;

ident: IDENT | LPAREN symbolIdent RPAREN;
infixIdent: IDENT | symbolIdent;

pat: name=ident # PVar;

alt: name=ident pat* EQ value=exp # AInfer
   | name=ident COLON type=typ EQ value=exp # ATyped;

typ: name=ident                        # TVar
   | lhs=typ callee=infixIdent rhs=typ # TInfix
   | lhs=typ rhs=typ                   # TApp;

exp: LET alt (COMMA alt)* IN value=exp            # ELet
   | BAR param=pat ARROW value=exp                # EAbs
   | value=ident                                  # EVar
   | value=STRING                                 # EString
   | value=INT                                    # EInt
   | value=DECIMAL                                # EDecimal
   | lhs=exp callee=infixIdent rhs=exp            # EInfix
   | lhs=exp rhs=exp                              # EApp
   | LPAREN value=exp RPAREN                      # EGroup;
