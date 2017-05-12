grammar Graphflow;

graphflow : whitespace? statement whitespace? ( SEMICOLON whitespace?)? EOF ;

statement : query ;

query : matchQuery
       | continuousMatchQuery
       | explainMatchQuery
       | explainContinuousMatchQuery
       | createQuery
       | deleteQuery
       | shortestPathQuery
       | durabilityQuery ;

matchQuery : MATCH whitespace matchPattern (whitespace whereClause)? (whitespace returnClause)?;
continuousMatchQuery : CONTINUOUSLY whitespace MATCH whitespace matchPattern
                       (whitespace whereClause)? whitespace (fileSink | udfCall) ;
explainMatchQuery : EXPLAIN whitespace matchQuery ;
explainContinuousMatchQuery : EXPLAIN whitespace continuousMatchQuery ;
createQuery : CREATE whitespace (createEdgePattern | createVertexPattern) ;
deleteQuery : DELETE whitespace deletePattern ;
shortestPathQuery: SHORTEST whitespace PATH whitespace pathPattern ;
durabilityQuery: ( LOAD whitespace FROM  | SAVE whitespace TO ) whitespace DIR whitespace stringLiteral ;

matchPattern: variableEdge ( whitespace? COMMA whitespace? variableEdge )* ;
deletePattern : digitsEdgeWithOptionalType ( whitespace? COMMA whitespace? digitsEdgeWithOptionalType )* ;
createEdgePattern : digitsEdgeWithTypeAndProperties
                    ( whitespace? COMMA whitespace? digitsEdgeWithTypeAndProperties )* ;
createVertexPattern : digitsVertexWithTypeAndProperties
                      ( whitespace? COMMA whitespace? digitsVertexWithTypeAndProperties)* ;
pathPattern: OPEN_ROUND_BRACKET whitespace? Digits whitespace? COMMA whitespace?
             Digits whitespace? CLOSE_ROUND_BRACKET ;

returnClause : RETURN whitespace (variable | variableWithProperty | aggregationPattern )
               ( whitespace? COMMA whitespace? (variable | variableWithProperty | aggregationPattern ) )* ;
aggregationPattern : ( aggregationFunction OPEN_ROUND_BRACKET whitespace? ( variable | variableWithProperty )
                     whitespace? CLOSE_ROUND_BRACKET )
                   | countStarPattern;
aggregationFunction : ( AVG | MAX | MIN | SUM ) ;
countStarPattern :  COUNT OPEN_ROUND_BRACKET whitespace? STAR whitespace? CLOSE_ROUND_BRACKET ;
whereClause : WHERE whitespace predicates ;
predicates : predicate ( whitespace AND whitespace predicate )* ;
predicate : operand whitespace? operator whitespace? operand ;
operand : literal | variableWithProperty ;
variableEdge : variableVertex (DASH edgeVariable)? DASH GREATER_THAN variableVertex ;
digitsEdgeWithOptionalType : digitsVertex (DASH edgeType)? DASH GREATER_THAN digitsVertex ;
digitsEdgeWithTypeAndProperties : digitsVertexWithTypeAndProperties (DASH edgeTypeAndOptionalProperties)
                                  DASH GREATER_THAN digitsVertexWithTypeAndProperties ;

digitsVertex : OPEN_ROUND_BRACKET whitespace? Digits whitespace? CLOSE_ROUND_BRACKET ;
digitsVertexWithTypeAndProperties : OPEN_ROUND_BRACKET whitespace? Digits whitespace? COLON type whitespace?
                                    properties? whitespace? CLOSE_ROUND_BRACKET ;
variableVertex : OPEN_ROUND_BRACKET whitespace? variable (whitespace? COLON type)? (whitespace? properties)?
                 whitespace? CLOSE_ROUND_BRACKET ;

edgeType : OPEN_SQUARE_BRACKET whitespace? COLON type whitespace? CLOSE_SQUARE_BRACKET ;
edgeTypeAndOptionalProperties : OPEN_SQUARE_BRACKET whitespace? COLON type whitespace? properties?
                                whitespace? CLOSE_SQUARE_BRACKET ;
edgeVariable : OPEN_SQUARE_BRACKET whitespace? variable (whitespace? COLON type)? whitespace? properties
               whitespace? CLOSE_SQUARE_BRACKET
             | OPEN_SQUARE_BRACKET whitespace? variable? (whitespace? COLON type)? whitespace? CLOSE_SQUARE_BRACKET ;
variableWithProperty : variable DOT key;

type : variable ;
properties : OPEN_CURLY_BRACKET whitespace? ( property ( whitespace? COMMA whitespace? property )* )?
             whitespace? CLOSE_CURLY_BRACKET ;
property : key whitespace? COLON whitespace? literal ;
literal : numericalLiteral | booleanLiteral | stringLiteral  ;

fileSink : FILE whitespace stringLiteral;
udfCall : ACTION whitespace UDF whitespace functionName whitespace IN whitespace stringLiteral ;

operator : EQUAL_TO | NOT_EQUAL_TO | LESS_THAN | LESS_THAN_OR_EQUAL | GREATER_THAN | GREATER_THAN_OR_EQUAL ;

key : ( Characters | UNDERSCORE | keyword ) ( Digits | Characters | UNDERSCORE | keyword )* ;
functionName : ( Characters | Digits | UNDERSCORE | DASH | DOT | keyword )+ ;
variable : ( Characters | UNDERSCORE | DASH | keyword ) ( Digits | Characters | UNDERSCORE | DASH | keyword )* ;

keyword
    : MATCH
    | CONTINUOUSLY
    | EXPLAIN
    | CREATE
    | DELETE
    | SHORTEST
    | PATH
    | WHERE
    | RETURN
    | COUNT
    | AVG
    | MAX
    | MIN
    | SUM
    | ACTION
    | IN
    | UDF
    | JAR
    | LOAD
    | SAVE
    | FROM
    | TO
    | DIR
    | TRUE
    | FALSE
    | AND
    | OR
    | FILE ;

whitespace : ( SPACE | TAB | CARRIAGE_RETURN | LINE_FEED | FORM_FEED | Comment )+ ;
numericalLiteral : (DASH whitespace?)? ( integerLiteral | doubleLiteral ) ;
integerLiteral : Digits ;
doubleLiteral : Digits DOT Digits ;
booleanLiteral : TRUE | FALSE ;
stringLiteral : QuotedString ;

/*********** Lexer rules ***********/

Comment : '/*' .*? '*/'
        | '//' ~( '\n' | '\r' )*  '\r'? ( '\n' | EOF ) ;

fragment EscapedChar : TAB | CARRIAGE_RETURN | LINE_FEED | BACKSPACE | FORM_FEED | '\\' ( '"' | '\'' | '\\' ) ;
QuotedString : DOUBLE_QUOTE ( EscapedChar | ~( '\\' | '"' ) )* DOUBLE_QUOTE
             | SINGLE_QUOTE ( EscapedChar | ~( '\\' | '\'' ) )* SINGLE_QUOTE ;

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

SPACE : [ ] ;

TAB : [\t] ;

CARRIAGE_RETURN : [\r] ;

LINE_FEED : [\n] ;

FORM_FEED : [\f] ;

BACKSPACE : [\b] ;

VERTICAL_TAB : [\u000B] ;

STAR : '*' ;
DASH : '-' ;
UNDERSCORE : '_' ;
DOT : '.' ;
FORWARD_SLASH : '/' ;
BACKWARD_SLASH : '\\' ;
SEMICOLON: ';' ;
COLON : ':' ;
COMMA: ',' ;
SINGLE_QUOTE: '\'' ;
DOUBLE_QUOTE: '"' ;
UNARY_OR : '|' ;
OPEN_ROUND_BRACKET : '(' ;
CLOSE_ROUND_BRACKET : ')' ;
OPEN_CURLY_BRACKET : '{' ;
CLOSE_CURLY_BRACKET : '}' ;
OPEN_SQUARE_BRACKET : '[' ;
CLOSE_SQUARE_BRACKET : ']' ;
EQUAL_TO : '=' ;
NOT_EQUAL_TO : '<>' ;
LESS_THAN : '<' ;
GREATER_THAN : '>' ;
LESS_THAN_OR_EQUAL : '<=' ;
GREATER_THAN_OR_EQUAL : '>=' ;

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

fragment Character : A | B | C | D | E | F | G | H | I | J | K | L | M |
                     N | O | P | Q | R | S | T | U | V | W | X | Y | Z ;
Characters : ( Character )+ ;

fragment Digit : '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' ;
Digits : ( Digit )+ ;
