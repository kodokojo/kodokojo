// Generated from io/kodokojo/commons/utils/DockerImageName.g4 by ANTLR 4.3
package io.kodokojo.commons.model;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DockerImageNameParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		DCOLON=1, SLASH=2, LETTER=3, NUMBER=4, SPECIAL=5, CRLF=6;
	public static final String[] tokenNames = {
		"<INVALID>", "':'", "'/'", "LETTER", "NUMBER", "SPECIAL", "CRLF"
	};
	public static final int
		RULE_imageName = 0, RULE_repository = 1, RULE_namespace = 2, RULE_name = 3, 
		RULE_tag = 4;
	public static final String[] ruleNames = {
		"imageName", "repository", "namespace", "name", "tag"
	};

	@Override
	public String getGrammarFileName() { return "DockerImageName.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DockerImageNameParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ImageNameContext extends ParserRuleContext {
		public TerminalNode CRLF() { return getToken(DockerImageNameParser.CRLF, 0); }
		public NamespaceContext namespace() {
			return getRuleContext(NamespaceContext.class,0);
		}
		public TagContext tag() {
			return getRuleContext(TagContext.class,0);
		}
		public List<TerminalNode> SLASH() { return getTokens(DockerImageNameParser.SLASH); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode DCOLON() { return getToken(DockerImageNameParser.DCOLON, 0); }
		public TerminalNode SLASH(int i) {
			return getToken(DockerImageNameParser.SLASH, i);
		}
		public RepositoryContext repository() {
			return getRuleContext(RepositoryContext.class,0);
		}
		public ImageNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_imageName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DockerImageNameListener ) ((DockerImageNameListener)listener).enterImageName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DockerImageNameListener ) ((DockerImageNameListener)listener).exitImageName(this);
		}
	}

	public final ImageNameContext imageName() throws RecognitionException {
		ImageNameContext _localctx = new ImageNameContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_imageName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(13);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(10); repository();
				setState(11); match(SLASH);
				}
				break;
			}
			setState(18);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(15); namespace();
				setState(16); match(SLASH);
				}
				break;
			}
			setState(20); name();
			setState(23);
			_la = _input.LA(1);
			if (_la==DCOLON) {
				{
				setState(21); match(DCOLON);
				setState(22); tag();
				}
			}

			setState(25); match(CRLF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RepositoryContext extends ParserRuleContext {
		public TerminalNode NUMBER(int i) {
			return getToken(DockerImageNameParser.NUMBER, i);
		}
		public List<TerminalNode> SPECIAL() { return getTokens(DockerImageNameParser.SPECIAL); }
		public List<TerminalNode> LETTER() { return getTokens(DockerImageNameParser.LETTER); }
		public TerminalNode SPECIAL(int i) {
			return getToken(DockerImageNameParser.SPECIAL, i);
		}
		public TerminalNode LETTER(int i) {
			return getToken(DockerImageNameParser.LETTER, i);
		}
		public TerminalNode DCOLON() { return getToken(DockerImageNameParser.DCOLON, 0); }
		public List<TerminalNode> NUMBER() { return getTokens(DockerImageNameParser.NUMBER); }
		public RepositoryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_repository; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DockerImageNameListener ) ((DockerImageNameListener)listener).enterRepository(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DockerImageNameListener ) ((DockerImageNameListener)listener).exitRepository(this);
		}
	}

	public final RepositoryContext repository() throws RecognitionException {
		RepositoryContext _localctx = new RepositoryContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_repository);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(27);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LETTER) | (1L << NUMBER) | (1L << SPECIAL))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				}
				setState(30); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LETTER) | (1L << NUMBER) | (1L << SPECIAL))) != 0) );
			setState(32); match(DCOLON);
			setState(34); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(33); match(NUMBER);
				}
				}
				setState(36); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NamespaceContext extends ParserRuleContext {
		public TerminalNode NUMBER(int i) {
			return getToken(DockerImageNameParser.NUMBER, i);
		}
		public List<TerminalNode> SPECIAL() { return getTokens(DockerImageNameParser.SPECIAL); }
		public List<TerminalNode> LETTER() { return getTokens(DockerImageNameParser.LETTER); }
		public TerminalNode SPECIAL(int i) {
			return getToken(DockerImageNameParser.SPECIAL, i);
		}
		public TerminalNode LETTER(int i) {
			return getToken(DockerImageNameParser.LETTER, i);
		}
		public List<TerminalNode> NUMBER() { return getTokens(DockerImageNameParser.NUMBER); }
		public NamespaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namespace; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DockerImageNameListener ) ((DockerImageNameListener)listener).enterNamespace(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DockerImageNameListener ) ((DockerImageNameListener)listener).exitNamespace(this);
		}
	}

	public final NamespaceContext namespace() throws RecognitionException {
		NamespaceContext _localctx = new NamespaceContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_namespace);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(39); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(38);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LETTER) | (1L << NUMBER) | (1L << SPECIAL))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				}
				setState(41); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LETTER) | (1L << NUMBER) | (1L << SPECIAL))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NameContext extends ParserRuleContext {
		public TerminalNode NUMBER(int i) {
			return getToken(DockerImageNameParser.NUMBER, i);
		}
		public List<TerminalNode> SPECIAL() { return getTokens(DockerImageNameParser.SPECIAL); }
		public List<TerminalNode> LETTER() { return getTokens(DockerImageNameParser.LETTER); }
		public TerminalNode SPECIAL(int i) {
			return getToken(DockerImageNameParser.SPECIAL, i);
		}
		public TerminalNode LETTER(int i) {
			return getToken(DockerImageNameParser.LETTER, i);
		}
		public List<TerminalNode> NUMBER() { return getTokens(DockerImageNameParser.NUMBER); }
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DockerImageNameListener ) ((DockerImageNameListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DockerImageNameListener ) ((DockerImageNameListener)listener).exitName(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_name);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(44); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(43);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LETTER) | (1L << NUMBER) | (1L << SPECIAL))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				}
				setState(46); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LETTER) | (1L << NUMBER) | (1L << SPECIAL))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TagContext extends ParserRuleContext {
		public TerminalNode NUMBER(int i) {
			return getToken(DockerImageNameParser.NUMBER, i);
		}
		public List<TerminalNode> SPECIAL() { return getTokens(DockerImageNameParser.SPECIAL); }
		public List<TerminalNode> LETTER() { return getTokens(DockerImageNameParser.LETTER); }
		public TerminalNode SPECIAL(int i) {
			return getToken(DockerImageNameParser.SPECIAL, i);
		}
		public TerminalNode LETTER(int i) {
			return getToken(DockerImageNameParser.LETTER, i);
		}
		public List<TerminalNode> NUMBER() { return getTokens(DockerImageNameParser.NUMBER); }
		public TagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DockerImageNameListener ) ((DockerImageNameListener)listener).enterTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DockerImageNameListener ) ((DockerImageNameListener)listener).exitTag(this);
		}
	}

	public final TagContext tag() throws RecognitionException {
		TagContext _localctx = new TagContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_tag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(48);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LETTER) | (1L << NUMBER) | (1L << SPECIAL))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				}
				setState(51); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LETTER) | (1L << NUMBER) | (1L << SPECIAL))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\b8\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\2\5\2\20\n\2\3\2\3\2\3\2\5\2\25\n"+
		"\2\3\2\3\2\3\2\5\2\32\n\2\3\2\3\2\3\3\6\3\37\n\3\r\3\16\3 \3\3\3\3\6\3"+
		"%\n\3\r\3\16\3&\3\4\6\4*\n\4\r\4\16\4+\3\5\6\5/\n\5\r\5\16\5\60\3\6\6"+
		"\6\64\n\6\r\6\16\6\65\3\6\2\2\7\2\4\6\b\n\2\3\3\2\5\7:\2\17\3\2\2\2\4"+
		"\36\3\2\2\2\6)\3\2\2\2\b.\3\2\2\2\n\63\3\2\2\2\f\r\5\4\3\2\r\16\7\4\2"+
		"\2\16\20\3\2\2\2\17\f\3\2\2\2\17\20\3\2\2\2\20\24\3\2\2\2\21\22\5\6\4"+
		"\2\22\23\7\4\2\2\23\25\3\2\2\2\24\21\3\2\2\2\24\25\3\2\2\2\25\26\3\2\2"+
		"\2\26\31\5\b\5\2\27\30\7\3\2\2\30\32\5\n\6\2\31\27\3\2\2\2\31\32\3\2\2"+
		"\2\32\33\3\2\2\2\33\34\7\b\2\2\34\3\3\2\2\2\35\37\t\2\2\2\36\35\3\2\2"+
		"\2\37 \3\2\2\2 \36\3\2\2\2 !\3\2\2\2!\"\3\2\2\2\"$\7\3\2\2#%\7\6\2\2$"+
		"#\3\2\2\2%&\3\2\2\2&$\3\2\2\2&\'\3\2\2\2\'\5\3\2\2\2(*\t\2\2\2)(\3\2\2"+
		"\2*+\3\2\2\2+)\3\2\2\2+,\3\2\2\2,\7\3\2\2\2-/\t\2\2\2.-\3\2\2\2/\60\3"+
		"\2\2\2\60.\3\2\2\2\60\61\3\2\2\2\61\t\3\2\2\2\62\64\t\2\2\2\63\62\3\2"+
		"\2\2\64\65\3\2\2\2\65\63\3\2\2\2\65\66\3\2\2\2\66\13\3\2\2\2\n\17\24\31"+
		" &+\60\65";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}