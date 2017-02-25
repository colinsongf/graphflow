grammar Graphflow;

graphflow : whitespace? statement whitespace? ( ';'  whitespace?)? ;

statement : query ;

query : matchQuery
       | continuousMatchQuery
       | createQuery
       | deleteQuery
       | shortestPathQuery
       ;

matchQuery : MATCH whitespace matchPattern whitespace? returnClause? ;
continuousMatchQuery : CONTINUOUS whitespace matchQuery whitespace userOperation whitespace operationLocation;
createQuery : CREATE whitespace (createEdgePattern | createVertexPattern) ;
deleteQuery : DELETE whitespace deletePattern ;
shortestPathQuery: SHORTEST whitespace PATH whitespace pathPattern ;

matchPattern: variableEdge ( whitespace? ',' whitespace? variableEdge )* ;
deletePattern : digitsEdgeWithOptionalType ( whitespace? ',' whitespace? digitsEdgeWithOptionalType )* ;
createEdgePattern : digitsEdgeWithTypeAndProperties ( whitespace? ',' whitespace? digitsEdgeWithTypeAndProperties )* ;
createVertexPattern : digitsVertexWithTypeAndProperties ( whitespace ? ',' whitespace? digitsVertexWithTypeAndProperties)* ;
pathPattern: '(' whitespace? Digits whitespace? ',' whitespace? Digits whitespace? ')' ;
returnClause : RETURN whitespace (variable | variableWithProperty) ( whitespace? ',' whitespace? (variable | variableWithProperty) )* ;

variableEdge : variableVertex (DASH edgeOptionalTypeAndOptionalProperties)? DASH RIGHT_ARROWHEAD variableVertex ;
digitsEdgeWithOptionalType : digitsVertex (DASH edgeType)? DASH RIGHT_ARROWHEAD digitsVertex ;
digitsEdgeWithTypeAndProperties : digitsVertexWithTypeAndProperties (DASH edgeTypeAndOptionalProperties) DASH RIGHT_ARROWHEAD
                                  digitsVertexWithTypeAndProperties ;

digitsVertex : '(' whitespace? Digits whitespace? ')' ;
digitsVertexWithTypeAndProperties : '(' whitespace? Digits whitespace? ':' type whitespace? properties? whitespace? ')';
variableVertex : '(' whitespace? variable whitespace? ')' ;

edgeType : '[' whitespace? ':' type whitespace? ']' ;
edgeTypeAndOptionalProperties : '[' whitespace? ':' type whitespace? properties? whitespace? ']' ;
edgeOptionalTypeAndOptionalProperties : '[' whitespace? variable? (':' type)? whitespace? properties? whitespace? ']' ;

type : variable ;
properties : '{' whitespace? ( property ( whitespace? ',' whitespace? property )* )? whitespace? '}' ;
property : key whitespace? ':' whitespace? dataType whitespace? '=' whitespace? value ;
key : variable ;
value : ( Digits | Characters | UNDERSCORE | DASH | DOT )+ ;

userOperation : FILE ;
operationLocation: variable;

variableWithProperty : variable DOT variable;
variable : ( Digits | Characters | UNDERSCORE | DASH )+ ;
dataType : ( INT | DOUBLE | BOOLEAN | STRING ) ;

INT: I N T ;
DOUBLE: D O U B L E ;
BOOLEAN: B O O L E A N ;
STRING: S T R I N G;

Digits : (Digit)+ ;

MATCH : M A T C H ;

CONTINUOUS : C O N T I N U O U S ;

FILE : F I L E ;

CREATE : C R E A T E ;

DELETE : D E L E T E;

SHORTEST: S H O R T E S T ;
PATH: P A T H ;

RETURN: R E T U R N ;

whitespace : (WHITESPACE)+ ;

Characters : ( [a-z] | [A-Z] )+ ;

WHITESPACE : SPACE
           | TAB
           | CR
           | LF
           | VT
           | '\n'
           | Comment
           ;

Comment : ( '/*' ( Comment_0 | ( '*' Comment_1 ) )* '*/' )
        | ( '//' Comment_2 CR? ( LF | EOF ) )
        ;

RIGHT_ARROWHEAD : '>' ;

DASH : '-' ;

UNDERSCORE : '_' ;

DOT : '.' ;

fragment Comment_1 : [\u0000-.0-\uFFFF] ;

fragment Comment_0 : [\u0000-)+-\uFFFF] ;

fragment Comment_2 : [\u0000-\t\u000B-\f\u000E-\uFFFF] ;

fragment SPACE : [ ] ;

fragment TAB : [\t] ;

fragment CR : [\r] ;

fragment LF : [\n] ;

fragment VT : [\u000B] ;

fragment A : ('a'|'A') ;
fragment B : ('b'|'B') ;
fragment C : ('c'|'C') ;
fragment D : ('d'|'D') ;
fragment E : ('e'|'E') ;
fragment F : ('f'|'F') ;
fragment G : ('g'|'G') ;
fragment H : ('h'|'H') ;
fragment I : ('i'|'I') ;
fragment J : ('j'|'J') ;
fragment K : ('k'|'K') ;
fragment L : ('l'|'L') ;
fragment M : ('m'|'M') ;
fragment N : ('n'|'N') ;
fragment O : ('o'|'O') ;
fragment P : ('p'|'P') ;
fragment Q : ('q'|'Q') ;
fragment R : ('r'|'R') ;
fragment S : ('s'|'S') ;
fragment T : ('t'|'T') ;
fragment U : ('u'|'U') ;
fragment V : ('v'|'V') ;
fragment W : ('w'|'W') ;
fragment X : ('x'|'X') ;
fragment Y : ('y'|'Y') ;
fragment Z : ('z'|'Z') ;

fragment Digit : '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' ;
