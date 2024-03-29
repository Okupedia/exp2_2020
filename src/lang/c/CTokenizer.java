package lang.c;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import lang.*;

public class CTokenizer extends Tokenizer<CToken, CParseContext> {
	@SuppressWarnings("unused")
	private CTokenRule	rule;
	private int			lineNo, colNo;
	private char		backCh;
	private boolean		backChExist = false;
	private final int INT_MAX = 0xFFFF;

	public CTokenizer(CTokenRule rule) {
		this.rule = rule;
		lineNo = 1; colNo = 1;
	}

	private InputStream in;
	private PrintStream err;

	private char readChar() {
		char ch;
		if (backChExist) {
			ch = backCh;
			backChExist = false;
		} else {
			try {
				ch = (char) in.read();
			} catch (IOException e) {
				e.printStackTrace(err);
				ch = (char) -1;
			}
		}
		++colNo;
		if (ch == '\n')  { colNo = 1; ++lineNo; }
//		System.out.print("'"+ch+"'("+(int)ch+")");
		return ch;
	}
	private void backChar(char c) {
		backCh = c;
		backChExist = true;
		--colNo;
		if (c == '\n') { --lineNo; }
	}

	// 現在読み込まれているトークンを返す
	private CToken currentTk = null;
	public CToken getCurrentToken(CParseContext pctx) {
		return currentTk;
	}
	// 次のトークンを読んで返す
	public CToken getNextToken(CParseContext pctx) {
		in = pctx.getIOContext().getInStream();
		err = pctx.getIOContext().getErrStream();
		currentTk = readToken();
//		System.out.println("Token='" + currentTk.toString());
		return currentTk;
	}
	private CToken readToken() {
		CToken tk = null;
		char ch;
		int  startCol = colNo;
		StringBuffer text = new StringBuffer();

		//int state = 0;
		Status state = Status.INIT;
		boolean accept = false;
		while (!accept) {
			switch (state) {
				case INIT:					// 初期状態
					ch = readChar();
					if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
						break;
					} else if (ch == (char) -1) {	// EOF
						startCol = colNo - 1;
						state = Status.EOF;
						break;
					}/* else if (ch >= '0' && ch <= '9') {
						startCol = colNo - 1;
						text.append(ch);
						state = State.DECIMAL;
					} else if (ch == '+') {
						startCol = colNo - 1;
						text.append(ch);
						state = State.PLUS.EOF;
					} else {			// ヘンな文字を読んだ
						startCol = colNo - 1;
						text.append(ch);
						state = State.ILL;
					}*/

					startCol = colNo - 1;
					text.append(ch);
					state = switch(ch){
						case '+' -> Status.PLUS;
						case '-' -> Status.MINUS;
						case '/' -> Status.SLASH;
						case '0' -> Status.ZERO;
						case '&' -> Status.AND;
						case '*' -> Status.MULT;
						case '(' -> Status.LPAR;
						case ')' -> Status.RPAR;
						case '[' -> Status.LBRA;
						case ']' -> Status.RBRA;
						case '=' -> Status.ASSIGN;
						case ';' -> Status.SEMI;
						case '<' -> Status.LT;
						case '>' -> Status.GT;
						case '!' -> Status.NE;
						case '{' -> Status.LCBRA;
						case '}' -> Status.RCBRA;
						case '|' -> Status.OR;
						default -> {
							if (ch >= '1' && ch <= '9') {
								yield Status.DECIMAL;
							} else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
								yield Status.IDENT;
							} else {			// ヘンな文字を読んだ
								yield Status.ILL;
							}
						}
					};
					break;

				case EOF:					// EOFを読んだ
					tk = new CToken(CToken.TK_EOF, lineNo, startCol, "end_of_file");
					accept = true;
					break;
				case ILL:					// ヘンな文字を読んだ
					tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
					accept = true;
					break;
				case DECIMAL:					// 数（10進数）の開始
				ch = readChar();
					if (Character.isDigit(ch)) {
						text.append(ch);
					} else {
						backChar(ch);	// 数を表さない文字は戻す（読まなかったことにする）

						if(Integer.decode(text.toString()) > INT_MAX) {
							state = Status.ILL;
						} else {
							//backChar(ch);	// 数を表さない文字は戻す（読まなかったことにする）
							tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
							accept = true;
						}
					}
					break;
				case PLUS:					// +を読んだ
					tk = new CToken(CToken.TK_PLUS, lineNo, startCol, "+");
					accept = true;
					break;
				case MINUS:					// -を読んだ
					tk = new CToken(CToken.TK_MINUS, lineNo, startCol, "-");
					accept = true;
					break;
				case SLASH:					// /を読んだ
					//これがどんな意味の/かわからないからもう一文字読んで判断する必要がある
					ch = readChar();
					state = switch(ch){
						case '*' :
							yield Status.B_COMMENT;
						case '/' :
							yield Status.L_COMMENT;
						default:
							backChar(ch);
							yield Status.DIVIDE;
					};
					break;
				case L_COMMENT:				// //を読んだ
					ch = readChar();
					state = switch(ch){
						case '\n':						//改行したらコメントは終了して普通の文字列受け取り
							text.delete(0, text.length());
							yield Status.INIT;
						case (char) -1:					//EOFはファイルの終わり
							yield Status.EOF;
						default:
							yield Status.L_COMMENT;		//コメントはまだ続く
					};
					break;
				case B_COMMENT:			// /*を読んだ
					ch = readChar();
					state = switch(ch){
						case (char) -1:					//EOFはファイルの終わりだからおかしい
							yield Status.ILL;
						case '*':						// *だからコメントが続くかもしれないし次でおわるかもしれない
							yield Status.B_COMMENT_END;
						default:
							yield Status.B_COMMENT;		//コメントはまだ続く
					};
					break;
				case B_COMMENT_END:			// /*を読んだあとに*を読んだ
					ch = readChar();
					state = switch(ch){
						case '/':						// */となったのでコメントはここでおわり
							text.delete(0, text.length());
							yield Status.INIT;
						case (char) -1:					//EOFはファイルの終わりだからおかしい
							yield Status.ILL;
						case '*':						// **だからコメントが続くかもしれないし次でおわるかもしれない
							yield Status.B_COMMENT_END;
						default:
							yield Status.B_COMMENT;		//コメントはまだ続く
					};
					break;
				case DIVIDE:				//　割り算としての/を読んだ
					//ch = readChar();
					tk = new CToken(CToken.TK_SLASH, lineNo, startCol, "/");
					accept = true;
					break;
				case ZERO:
					ch = readChar();
					text.append(ch);
					if (ch >= '1' && ch <= '7') {
						state = Status.OCTAL;
					}else if(ch == 'x' || ch == 'X'){
						state = Status.X;
					}else{
						backChar(ch);
						tk = new CToken(CToken.TK_NUM, lineNo, startCol, "0");
						accept = true;
					}
					break;
				case OCTAL:		//8進数
					ch = readChar();
					if (ch >= '1' && ch <= '7') {
						text.append(ch);
					} else {
						// 数の終わり
						if(Integer.decode(text.toString()) > INT_MAX) {
							state = Status.ILL;
						}else if(ch >= '8'){
							text.append(ch);
							state = Status.ILL;
						}else {
							backChar(ch);	// 数を表さない文字は戻す（読まなかったことにする）
							tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
							accept = true;
						}
					}
					break;
				case X:
					ch = Character.toLowerCase(readChar());
					if (ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f' ) {
						text.append(ch);
						state = Status.HEXADECIMAL;
					} else {
						state = Status.ILL;
					}
					break;
				case HEXADECIMAL:
					ch = Character.toLowerCase(readChar());
					if (ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f' ) {
						text.append(ch);
					} else {
						// 数の終わり
						if(Integer.decode(text.toString()) > INT_MAX) {
							state = Status.ILL;
						}else if(ch >= 'g'){
							text.append(ch);
							state = Status.ILL;
						} else {
							backChar(ch);	// 数を表さない文字は戻す（読まなかったことにする）
							tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
							//System.out.println(tk.getText());
							accept = true;
						}
					}
					break;
				case AND:
					ch = readChar();
					tk = new CToken(CToken.TK_AND, lineNo, startCol, "&");
					if(ch == '&'){
						tk = new CToken(CToken.TK_DAND, lineNo, startCol, "&&");
					}else{
						backChar(ch);
					}
					accept = true;
					break;
				case MULT:				//　 掛け算
					//ch = readChar();
					tk = new CToken(CToken.TK_ASTERISK, lineNo, startCol, "*");
					accept = true;
					break;
				case LPAR:
					tk = new CToken(CToken.TK_LPAR, lineNo, startCol, "(");
					accept = true;
					break;
				case RPAR:
					tk = new CToken(CToken.TK_RPAR, lineNo, startCol, ")");
					accept = true;
					break;
				case IDENT:
					ch = readChar();
					if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
							|| (ch >= '0' && ch <= '9') || ch == '_') {
						text.append(ch);
					} else {
						backChar(ch);
						//tk = new CToken(CToken.TK_IDENT, lineNo, startCol, text.toString());
						String s = text.toString();
						Integer i = (Integer) rule.get(s);
						tk = new CToken(((i == null) ? CToken.TK_IDENT : i.intValue()), lineNo, startCol, s);
						accept = true;
					}
					break;
				case LBRA:
					tk = new CToken(CToken.TK_LBRA, lineNo, startCol, "[");
					accept = true;
					break;
				case RBRA:
					tk = new CToken(CToken.TK_RBRA, lineNo, startCol, "]");
					accept = true;
					break;
				case ASSIGN:
					ch = readChar();
					tk = new CToken(CToken.TK_ASSIGN, lineNo, startCol, "=");
					if(ch == '='){
						tk = new CToken(CToken.TK_EQ, lineNo, startCol, "==");
					}else{
						backChar(ch);
					}
					accept = true;
					break;
				case SEMI:
					tk = new CToken(CToken.TK_SEMI, lineNo, startCol, ";");
					accept = true;
					break;
				case LT:
					ch = readChar();
					tk = new CToken(CToken.TK_LT, lineNo, startCol, "<");
					if(ch == '='){
						tk = new CToken(CToken.TK_LE, lineNo, startCol, "<=");
					}else{
						backChar(ch);
					}
					accept = true;
					break;
				case GT:
					ch = readChar();
					tk = new CToken(CToken.TK_GT, lineNo, startCol, ">");
					if(ch == '='){
						tk = new CToken(CToken.TK_GE, lineNo, startCol, ">=");
					}else{
						backChar(ch);
					}
					accept = true;
					break;
				case NE:
					ch = readChar();
					tk = new CToken(CToken.TK_NOT, lineNo, startCol, "!");
					if(ch == '='){
						tk = new CToken(CToken.TK_NE, lineNo, startCol, "!=");
					}else{
						/*
						text.append(ch);
						state = Status.ILL;
						 */
						backChar(ch);
					}
					accept = true;
					break;
				case LCBRA:
					tk = new CToken(CToken.TK_LCBRA, lineNo, startCol, "{");
					accept = true;
					break;
				case RCBRA:
					tk = new CToken(CToken.TK_RCBRA, lineNo, startCol, "}");
					accept = true;
					break;
				case OR:
					ch = readChar();
					if(ch == '|'){
						tk = new CToken(CToken.TK_OR, lineNo, startCol, "||");
						accept = true;
					}else{
						text.append(ch);
						state = Status.ILL;
					}
					break;
			}
		}
		return tk;
	}

	private enum Status{
		INIT,
		EOF,
		ILL,
		DECIMAL,
		PLUS,
		MINUS,
		SLASH,
		L_COMMENT,		//行コメント
		B_COMMENT,		//ブロックコメント
		B_COMMENT_END,	//ブロックコメントが終わるかもしれない(*を受け取って/を待っている状態)
		DIVIDE,
		ZERO,
		OCTAL,			//8進数
		X,
		HEXADECIMAL,	//16進数
		AND,
		MULT,
		LPAR,			//(
		RPAR,			//)
		IDENT,			//IDENT
		LBRA,			//[
		RBRA,			//]
		ASSIGN,			//=
		SEMI,			//;
		LT,				//<
		GT,				//>
		NE,				//! (NOT もしくは NE)
		LCBRA,			//{
		RCBRA,			//}
		OR,				//||
	}
}
