grammar Graphflow;

cypher : sp? statement sp? ( ';' sp? statement )* ( sp? ';' )? sp? ;

statement : query ;

query : match
       | create
       | delete
       ;

match : MATCH sp matchpattern ;

create : CREATE sp createpattern ;

delete : DELETE sp deletepattern ;

matchpattern: variableexpression ( sp? ',' sp? variableexpression )* ;

deletepattern : variableexpression ( sp? ',' sp? variableexpression )* ;

createpattern : digitsexpression ( sp? ',' sp? digitsexpression )* ;

digitsexpression : '(' sp? leftdigit sp? ')' sp? dash rightArrowHead '(' sp? rightdigit? sp? ')' ;

variableexpression: '(' sp? leftvariable sp? ')' sp? dash rightArrowHead '(' sp? rightvariable sp? ')'
                  | '(' sp? ')' sp? dash rightArrowHead '(' sp? rightvariable sp? ')'
                  | '(' sp? leftvariable sp? ')' sp? dash rightArrowHead '(' sp? ')'
                  ;

leftdigit : Digits ;

rightdigit : Digits ;

leftvariable : variable ;

rightvariable : variable ;

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

CREATE : ( 'C' | 'c' ) ( 'R' | 'r' ) ( 'E' | 'e' ) ( 'A' | 'a' ) ( 'T' | 't' ) ( 'E' | 'e' )  ;

DELETE : ( 'D' | 'd' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'T' | 't' ) ( 'E' | 'e' )  ;

sp : ( WHITESPACE )+ ;

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
