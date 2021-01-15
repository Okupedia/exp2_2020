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
	public static final int TK_ASSIGN		= 12;	// =
	public static final int TK_SEMI			= 13;	// ;
	public static final int TK_LT			= 14;	// <
	public static final int TK_LE			= 15;	// <=
	public static final int TK_GT			= 16;	// >
	public static final int TK_GE			= 17;	// >=
	public static final int TK_EQ			= 18;	// ==
	public static final int TK_NE			= 19;	// !=
	public static final int TK_TRUE			= 20;	// true
	public static final int TK_FALSE		= 21;	// false
	public static final int TK_IF			= 22;	// if
	public static final int TK_ELSE			= 23;	// else
	public static final int TK_WHILE		= 24;	// while
	public static final int TK_OUTPUT		= 25;	// output
	public static final int TK_INPUT		= 26;	// input
	public static final int TK_LCBRA		= 27;	// {
	public static final int TK_RCBRA		= 28;	//  }
	public static final int TK_NOT			= 29;	// !
	public static final int TK_DAND			= 30;	// &&
	public static final int TK_OR			= 31;	// ||

	public CToken(int type, int lineNo, int colNo, String s) {
		super(type, lineNo, colNo, s);
	}
}
