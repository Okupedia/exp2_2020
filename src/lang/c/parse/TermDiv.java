package lang.c.parse;

        import lang.*;
        import lang.c.*;

        import java.io.PrintStream;

public class TermDiv extends CParseRule {
    //termDiv ::= DIV factor
    private CParseRule factor;
    private CToken op;

    public TermDiv(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_SLASH;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        factor = new Factor(pcx);
        factor.parse(pcx);
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (factor != null) {
            factor.semanticCheck(pcx);
            this.setCType(factor.getCType());		// factor の型をそのままコピー
            this.setConstant(factor.isConstant());
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; termDiv starts");
        if (factor != null) {
            factor.codeGen(pcx);
            o.println("\tJSR\tDIV\t; TermDiv:計算サブルーチンを呼ぶ");
            o.println("\tSUB\t#2, R6\t; TermDiv:スタックから計算した値を消す");
            o.println("\tMOV\tR0, (R6)+\t; TermDiv:計結果をスタックに乗せる");
        }
        o.println(";;; termDiv completes");
    }
}