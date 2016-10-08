grammar Graphflow;

cypherQuery : MATCH matchquery (',' matchquery)* RETURN returnquery;
matchquery : vertex LEFTEDGE? '-[:' edge ']-' RIGHTEDGE? vertex;
vertex  : '(' variable (':' type)? ')';

returnquery :  variable ( ',' variable )*;

edge : CHARS;
variable : CHARS;
type : CHARS;

LEFTEDGE: '<';
RIGHTEDGE: '>';
CHARS: [a-z]+;
MATCH : 'MATCH';
RETURN : 'RETURN';
WS  : [ \t\r\n]+ -> skip ;

/* MATCH
   (m:Person)-[:KNOWS]->(n:Person)-[:KNOWS]->(o:Person),
   (m)-[:KNOWS]->(p:Person)-[:KNOWS]->(o)
   return m,n,o,p

   MATCH (n) OPTIONAL MATCH (n)-[r1]-() DELETE n,r1

   MATCH (n) RETURN n

   MATCH (m)-[:label]->(m) RETURN m

   CREATE (A:Person {name:'A'})
   CREATE (B:Person {name:'B'})
   CREATE (C:Person {name:'C'})
   CREATE (D:Person {name:'D'})
   CREATE (E:Person {name:'E'})
   CREATE (F:Person {name:'F'})
   CREATE (G:Person {name:'G'})
   CREATE (H:Person {name:'H'})
   CREATE (I:Person {name:'I'})
   CREATE
     (A)-[:KNOWS {relation:'knows'}]->(B),
     (A)-[:KNOWS {relation:'knows'}]->(C),
     (B)-[:KNOWS {relation:'knows'}]->(D),
     (C)-[:KNOWS {relation:'knows'}]->(D),
     (C)-[:KNOWS {relation:'knows'}]->(I),
     (I)-[:KNOWS {relation:'knows'}]->(E),
     (E)-[:KNOWS {relation:'knows'}]->(F),
     (E)-[:KNOWS {relation:'knows'}]->(G),
     (F)-[:KNOWS {relation:'knows'}]->(H),
     (G)-[:KNOWS {relation:'knows'}]->(H)
   */
