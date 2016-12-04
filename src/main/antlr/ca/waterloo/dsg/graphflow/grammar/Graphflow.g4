grammar Graphflow;

graphflow : whitespace? statement whitespace? ( ';'  whitespace?)? ;

statement : query ;

query : match
       | continuousMatch
       | create
       | delete
       | shortestPath
       ;

match : MATCH whitespace matchPattern ;

continuousMatch : CONTINUOUS whitespace match whitespace userOperation whitespace operationLocation;

create : CREATE whitespace createPattern ;

delete : DELETE whitespace deletePattern ;

shortestPath: SHORTEST_PATH whitespace pathPattern ;

matchPattern: variableExpression ( whitespace? ',' whitespace? variableExpression )* ;

deletePattern : digitsExpression ( whitespace? ',' whitespace? digitsExpression )* ;

createPattern : digitsExpression ( whitespace? ',' whitespace? digitsExpression )* ;

pathPattern: '(' whitespace? leftDigit whitespace? ',' whitespace? rightDigit whitespace? ')' ;

digitsExpression : '(' whitespace? leftDigit whitespace? ')' whitespace? dash rightArrowHead '(' whitespace? rightDigit whitespace? ')' ;

variableExpression: '(' whitespace? leftVariable whitespace? ')' whitespace? dash rightArrowHead '(' whitespace? rightVariable whitespace? ')' ;

userOperation : FILE ;

operationLocation : variable ;

leftDigit : Digits ;

rightDigit : Digits ;

leftVariable : variable ;

rightVariable : variable ;

variable : Digits
         | Characters
         ;

Digits : ( Digit )+ ;

Digit : '0'
      | '1'
      | '2'
      | '3'
      | '4'
      | '5'
      | '6'
      | '7'
      | '8'
      | '9'
      ;

Characters : ( Character )+ ;

Character : [a-z] ;

MATCH : ( 'M' | 'm' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'C' | 'c' ) ( 'H' | 'h' )  ;

CONTINUOUS : 'CONTINUOUS' | 'continuous' ;

FILE : 'FILE' | 'file' ;

CREATE : ( 'C' | 'c' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'E' | 'e' )  ;

DELETE : ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'T' | 't' ) ( 'E' | 'e' )  ;

SHORTEST_PATH: ( 'S' | 's' ) ( 'H' | 'h' ) ( 'O' | 'o' ) ( 'R' | 'r' ) ( 'T' | 't' ) ( 'E' | 'e' )
    ( 'S' | 's' ) ( 'T' | 't' ) ( '_' ) ('P' | 'p') ( 'A' | 'a' ) ( 'T' | 't' ) ('H' | 'h') ;

whitespace : ( WHITESPACE )+ ;

WHITESPACE : SPACE
           | TAB
           | LF
           | VT
           | FF
           | CR
           | FS
           | GS
           | RS
           | US
           | '\n'
           | Comment
           ;

Comment : ( '/*' ( Comment_0 | ( '*' Comment_1 ) )* '*/' )
        | ( '//' Comment_2 CR? ( LF | EOF ) )
        ;

rightArrowHead : '>' ;

dash : '-' ;

L_0X : ( '0' | '0' ) ( 'X' | 'x' )  ;

fragment FF : [\f] ;

fragment EscapedSymbolicName_0 : [\u0000-_a-\uFFFF] ;

fragment RS : [\u001E] ;

fragment Comment_1 : [\u0000-.0-\uFFFF] ;

fragment Comment_0 : [\u0000-)+-\uFFFF] ;

fragment StringLiteral_1 : [\u0000-&(-\[\]-\uFFFF] ;

fragment Comment_2 : [\u0000-\t\u000B-\f\u000E-\uFFFF] ;

fragment GS : [\u001D] ;

fragment FS : [\u001C] ;

fragment CR : [\r] ;

fragment SPACE : [ ] ;

fragment TAB : [\t] ;

fragment LF : [\n] ;

fragment VT : [\u000B] ;

fragment US : [\u001F] ;
