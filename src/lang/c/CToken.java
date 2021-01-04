package lang.c;

import lang.SimpleToken;

public class CToken extends SimpleToken {
	//ここに識別してほしい文字を登録する
	public static final int TK_PLUS			= 2;	// +
	public static final int TK_MINUS		= 3;	// -
	public static final int TK_ASTERISK		= 4;	// *
	public static final int TK_SLASH		= 5;	// /
	public static final int TK_AND			= 6;	// &
	public static final int TK_LPAR			= 7;	// (
	public static final int TK_RPAR			= 8;	// )
	public static final int TK_IDENT		= 9;
	public static final int TK_LBRA			= 10;	// [
	public static final int TK_RBRA			= 11;	// ]

	public CToken(int type, int lineNo, int colNo, String s) {
		super(type, lineNo, colNo, s);
	}
}
