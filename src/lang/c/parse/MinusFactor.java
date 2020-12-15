package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;

import java.io.PrintStream;

public class MinusFactor extends CParseRule {
    //plusFactor ::= MINUS unsignedFactor
    private CParseRule factor;

    public MinusFactor(CParseContext pcx){

    }

    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_MINUS;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CToken tk = pcx.getTokenizer().getCurrentToken(pcx);
        factor = new UnsignedFactor(pcx);
        factor.parse(pcx);
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (factor != null) {
            factor.semanticCheck(pcx);
            setCType(factor.getCType());		// number の型をそのままコピー
            setConstant(factor.isConstant());	// number は常に定数
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; factor starts");
        if (factor != null) { factor.codeGen(pcx); }
        o.println(";;; factor completes");
    }
}