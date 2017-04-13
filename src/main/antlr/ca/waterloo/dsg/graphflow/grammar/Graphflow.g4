grammar Graphflow;

graphflow : whitespace? statement whitespace? ( ';'  whitespace?)? ;

statement : query ;

query : matchQuery
       | continuousMatchQuery
       | explainMatchQuery
       | explainContinuousMatchQuery
       | createQuery
       | deleteQuery
       | shortestPathQuery
       | durabilityQuery
       ;

matchQuery : MATCH whitespace matchPattern (whitespace whereClause)? (whitespace returnClause)?;
continuousMatchQuery : CONTINUOUSLY whitespace MATCH whitespace matchPattern (whitespace whereClause)? whitespace (fileSink | udfCall) ;
explainMatchQuery : EXPLAIN whitespace matchQuery ;
explainContinuousMatchQuery : EXPLAIN whitespace continuousMatchQuery ;
createQuery : CREATE whitespace (createEdgePattern | createVertexPattern) ;
deleteQuery : DELETE whitespace deletePattern ;
shortestPathQuery: SHORTEST whitespace PATH whitespace pathPattern ;
durabilityQuery: ( LOAD whitespace FROM  | SAVE whitespace TO ) whitespace DIR whitespace
                    SINGLE_QUOTE filePath SINGLE_QUOTE ;

matchPattern: variableEdge ( whitespace? ',' whitespace? variableEdge )* ;
deletePattern : digitsEdgeWithOptionalType ( whitespace? ',' whitespace? digitsEdgeWithOptionalType )* ;
createEdgePattern : digitsEdgeWithTypeAndProperties ( whitespace? ',' whitespace? digitsEdgeWithTypeAndProperties )* ;
createVertexPattern : digitsVertexWithTypeAndProperties ( whitespace? ',' whitespace? digitsVertexWithTypeAndProperties)* ;
pathPattern: '(' whitespace? Digits whitespace? ',' whitespace? Digits whitespace? ')' ;

returnClause : RETURN whitespace (variable | variableWithProperty | aggregationPattern ) ( whitespace? ',' whitespace? (variable | variableWithProperty | aggregationPattern ) )* ;
aggregationPattern : ( aggregationFunction '(' whitespace? ( variable | variableWithProperty ) whitespace? ')' )
                    | countStarPattern;
aggregationFunction : ( AVG | MAX | MIN | SUM ) ;
countStarPattern :  COUNT '(' whitespace? '*' whitespace? ')' ;
whereClause : WHERE whitespace predicates ;
predicates : predicate ( whitespace AND whitespace predicate )* ;
predicate : operand whitespace? operator whitespace? operand ;
operand : literal | variableWithProperty ;
variableEdge : variableVertex (DASH edgeVariable)? DASH RIGHT_ARROWHEAD variableVertex ;
digitsEdgeWithOptionalType : digitsVertex (DASH edgeType)? DASH RIGHT_ARROWHEAD digitsVertex ;
digitsEdgeWithTypeAndProperties : digitsVertexWithTypeAndProperties (DASH edgeTypeAndOptionalProperties) DASH RIGHT_ARROWHEAD digitsVertexWithTypeAndProperties ;

digitsVertex : '(' whitespace? Digits whitespace? ')' ;
digitsVertexWithTypeAndProperties : '(' whitespace? Digits whitespace? ':' type whitespace? properties? whitespace? ')' ;
variableVertex : '(' whitespace? variable (whitespace? ':' type)? (whitespace? properties)? whitespace? ')' ;

edgeType : '[' whitespace? ':' type whitespace? ']' ;
edgeTypeAndOptionalProperties : '[' whitespace? ':' type whitespace? properties? whitespace? ']' ;
edgeVariable : '[' whitespace? variable (whitespace? ':' type)? whitespace? properties whitespace? ']' |
               '[' whitespace? variable? (whitespace? ':' type)? whitespace? ']' ;

type : variable ;
properties : '{' whitespace? ( property ( whitespace? ',' whitespace? property )* )? whitespace? '}' ;
property : key whitespace? ':' whitespace? literal ;
key : ( Characters | UNDERSCORE ) ( Digits | Characters | UNDERSCORE )* ;
literal : integerLiteral | doubleLiteral | booleanLiteral | stringLiteral  ;

integerLiteral : Digits ;
doubleLiteral : Digits DOT Digits ;
booleanLiteral : TRUE | FALSE ;
stringLiteral : '\'' value '\'';
value : ( Digits | Characters | UNDERSCORE | DASH | DOT )+ ;

fileSink : FILE whitespace SINGLE_QUOTE filePath SINGLE_QUOTE;
udfCall : ACTION whitespace UDF whitespace functionName whitespace IN whitespace SINGLE_QUOTE filePath DOT JAR SINGLE_QUOTE ;
filePath: ( Digits | Characters | UNDERSCORE | DASH | DOT | SLASH | SPACE )+ ;
functionName : ( Characters | Digits | UNDERSCORE | DASH | DOT )+ ;

operator : (equalTo | notEqualTo | lessThan | lessThanOrEqualTo | greaterThan | greaterThanOrEqualTo) ;
equalTo : '=' ;
notEqualTo : '<>' ;
lessThan : '<' ;
greaterThan : '>' ;
lessThanOrEqualTo : '<=' ;
greaterThanOrEqualTo : '>=' ;

variableWithProperty : variable DOT key;
variable : ( Characters | UNDERSCORE | DASH ) ( Digits | Characters | UNDERSCORE | DASH )* ;

MATCH : M A T C H ;
CONTINUOUSLY : C O N T I N U O U S L Y ;

EXPLAIN : E X P L A I N ;

CREATE : C R E A T E ;
DELETE : D E L E T E ;
SHORTEST : S H O R T E S T ;
PATH : P A T H ;
WHERE : W H E R E ;
RETURN : R E T U R N ;

COUNT : C O U N T ;
AVG : A V G ;
MAX : M A X ;
MIN : M I N ;
SUM : S U M ;

ACTION : A C T I O N ;
IN : I N ;
UDF : U D F ;
JAR : J A R ;
LOAD: L O A D ;
SAVE: S A V E ;
FROM: F R O M ;
TO: T O ;
DIR: D I R ;

TRUE : T R U E ;
FALSE : F A L S E ;

AND : A N D ;
OR : O R ;

FILE : F I L E ;

whitespace : (WHITESPACE)+ ;

Digits : (Digit)+ ;

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
SPACE : [ ] ;
SLASH : '/' ;
SINGLE_QUOTE: '\'' ;

fragment Comment_1 : [\u0000-.0-\uFFFF] ;

fragment Comment_0 : [\u0000-)+-\uFFFF] ;

fragment Comment_2 : [\u0000-\t\u000B-\f\u000E-\uFFFF] ;

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
