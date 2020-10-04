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
						default -> {
							if (ch >= '0' && ch <= '9') {
								yield Status.DECIMAL;
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
						// 数の終わり
						backChar(ch);	// 数を表さない文字は戻す（読まなかったことにする）
						tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
						accept = true;
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
					tk = new CToken(CToken.TK_SLASH, lineNo, startCol, "/");
					//TODO:ここのトークンはSLASHでいいの？
					accept = true;
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
		DIVIDE
	}
}
