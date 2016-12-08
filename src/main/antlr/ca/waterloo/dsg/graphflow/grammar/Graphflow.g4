grammar Graphflow;

graphflow : whitespace? statement whitespace? ( ';'  whitespace?)? ;

statement : query ;

query : matchQuery
       | continuousMatchQuery
       | createQuery
       | deleteQuery
       | shortestPathQuery
       ;

matchQuery : MATCH whitespace matchPattern ;
continuousMatchQuery : CONTINUOUS whitespace matchQuery whitespace userOperation whitespace operationLocation;
createQuery : CREATE whitespace createPattern ;
deleteQuery : DELETE whitespace deletePattern ;
shortestPathQuery: SHORTEST whitespace PATH whitespace pathPattern ;

matchPattern: variableEdge ( whitespace? ',' whitespace? variableEdge )* ;
deletePattern : digitsEdgeWithOptionalType ( whitespace? ',' whitespace? digitsEdgeWithOptionalType )* ;
createPattern : digitsEdgeWithType ( whitespace? ',' whitespace? digitsEdgeWithType )* ;
pathPattern: '(' whitespace? Digits whitespace? ',' whitespace? Digits whitespace? ')' ;

digitsEdgeWithOptionalType : digitsVertex (DASH edgeType)? DASH RIGHT_ARROWHEAD digitsVertex ;
digitsEdgeWithType : digitsVertexWithType (DASH edgeType) DASH RIGHT_ARROWHEAD digitsVertexWithType ;
variableEdge : variableVertex (DASH edgeType)? DASH RIGHT_ARROWHEAD variableVertex ;

digitsVertex : '(' whitespace? Digits whitespace? ')' ;
digitsVertexWithType : '(' whitespace? Digits whitespace? type  whitespace? ')' ;
variableVertex : '(' whitespace? variable whitespace? type? whitespace? ')' ;

edgeType : '[' whitespace? type whitespace? ']' ;

type : ':' variable ;

userOperation : FILE ;
operationLocation: variable;

variable : Characters ( Digits | Characters | UNDERSCORE | DASH | DOT )* ;

Digits : (Digit)+ ;

MATCH : M A T C H ;

CONTINUOUS : C O N T I N U O U S ;

FILE : F I L E ;

CREATE : C R E A T E ;

DELETE : D E L E T E;

SHORTEST: S H O R T E S T ;
PATH: P A T H ;

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
