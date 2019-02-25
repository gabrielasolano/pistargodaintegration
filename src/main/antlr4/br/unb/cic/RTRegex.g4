grammar RTRegex;

@rulecatch {
   catch (RecognitionException e) {
    throw e;
   }
}

rt:     expr NEWLINE                            # printExpr
  |     NEWLINE                                 # blank
  ;

expr:   t=('G'|'T') id							# gId
    |	'DM(' expr ')'							# gDecisionMaking
    | 	expr op=',' expr						# gDM
    ;
    
id:		FLOAT
	|	FLOAT X
	|	X
	;

FLOAT		: DIGIT+'.'?DIGIT* 	;
TASK		: 'T'				;
GOAL		: 'G'				;
X			: 'X'				;
NEWLINE 	: [\r\n]+           ;
WS          : [\t]+ -> skip 	;

fragment
DIGIT	: [0-9] ;