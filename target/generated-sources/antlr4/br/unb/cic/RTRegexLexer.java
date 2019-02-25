// Generated from br/unb/cic/RTRegex.g4 by ANTLR 4.3
package br.unb.cic;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class RTRegexLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__2=1, T__1=2, T__0=3, FLOAT=4, TASK=5, GOAL=6, X=7, NEWLINE=8, WS=9;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'"
	};
	public static final String[] ruleNames = {
		"T__2", "T__1", "T__0", "FLOAT", "TASK", "GOAL", "X", "NEWLINE", "WS", 
		"DIGIT"
	};


	public RTRegexLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "RTRegex.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\13A\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\3\2\3\2\3\2\3\2\3\3\3\3\3\4\3\4\3\5\6\5!\n\5\r\5\16\5\"\3\5\5\5&\n"+
		"\5\3\5\7\5)\n\5\f\5\16\5,\13\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\6\t\65\n\t"+
		"\r\t\16\t\66\3\n\6\n:\n\n\r\n\16\n;\3\n\3\n\3\13\3\13\2\2\f\3\3\5\4\7"+
		"\5\t\6\13\7\r\b\17\t\21\n\23\13\25\2\3\2\5\4\2\f\f\17\17\3\2\13\13\3\2"+
		"\62;D\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r"+
		"\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\3\27\3\2\2\2\5\33\3\2"+
		"\2\2\7\35\3\2\2\2\t \3\2\2\2\13-\3\2\2\2\r/\3\2\2\2\17\61\3\2\2\2\21\64"+
		"\3\2\2\2\239\3\2\2\2\25?\3\2\2\2\27\30\7F\2\2\30\31\7O\2\2\31\32\7*\2"+
		"\2\32\4\3\2\2\2\33\34\7+\2\2\34\6\3\2\2\2\35\36\7.\2\2\36\b\3\2\2\2\37"+
		"!\5\25\13\2 \37\3\2\2\2!\"\3\2\2\2\" \3\2\2\2\"#\3\2\2\2#%\3\2\2\2$&\7"+
		"\60\2\2%$\3\2\2\2%&\3\2\2\2&*\3\2\2\2\')\5\25\13\2(\'\3\2\2\2),\3\2\2"+
		"\2*(\3\2\2\2*+\3\2\2\2+\n\3\2\2\2,*\3\2\2\2-.\7V\2\2.\f\3\2\2\2/\60\7"+
		"I\2\2\60\16\3\2\2\2\61\62\7Z\2\2\62\20\3\2\2\2\63\65\t\2\2\2\64\63\3\2"+
		"\2\2\65\66\3\2\2\2\66\64\3\2\2\2\66\67\3\2\2\2\67\22\3\2\2\28:\t\3\2\2"+
		"98\3\2\2\2:;\3\2\2\2;9\3\2\2\2;<\3\2\2\2<=\3\2\2\2=>\b\n\2\2>\24\3\2\2"+
		"\2?@\t\4\2\2@\26\3\2\2\2\b\2\"%*\66;\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}