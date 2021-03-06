grammar LucenePushDown;
expr : (String)
|expr op=(AND|OR|NOT) expr
| LPAREN expr RPAREN
;

RPAREN : ')' ;
LPAREN : '(' ;
AND :'[AND]';
OR :'[OR]';
NOT :'not';

String :  '\'' | (~'\'' | '\'\'' )* '\'';

WS : [ \t\r\n]+ -> channel(HIDDEN) ;
