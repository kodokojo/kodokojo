// Generated from io/kodokojo/commons/utils/DockerImageName.g4 by ANTLR 4.3
package io.kodokojo.commons.model;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DockerImageNameLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		DCOLON=1, SLASH=2, LETTER=3, NUMBER=4, SPECIAL=5, CRLF=6;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'"
	};
	public static final String[] ruleNames = {
		"DCOLON", "SLASH", "LETTER", "NUMBER", "SPECIAL", "CRLF"
	};


	public DockerImageNameLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "DockerImageName.g4"; }

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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\b\36\b\1\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3"+
		"\5\3\6\3\6\3\7\3\7\3\7\5\7\35\n\7\2\2\b\3\3\5\4\7\5\t\6\13\7\r\b\3\2\6"+
		"\4\2C\\c|\3\2\62;\4\2/\60aa\4\2\f\f\17\17\36\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\3\17\3\2\2\2\5\21\3\2"+
		"\2\2\7\23\3\2\2\2\t\25\3\2\2\2\13\27\3\2\2\2\r\34\3\2\2\2\17\20\7<\2\2"+
		"\20\4\3\2\2\2\21\22\7\61\2\2\22\6\3\2\2\2\23\24\t\2\2\2\24\b\3\2\2\2\25"+
		"\26\t\3\2\2\26\n\3\2\2\2\27\30\t\4\2\2\30\f\3\2\2\2\31\32\7\17\2\2\32"+
		"\35\7\f\2\2\33\35\t\5\2\2\34\31\3\2\2\2\34\33\3\2\2\2\35\16\3\2\2\2\4"+
		"\2\34\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}