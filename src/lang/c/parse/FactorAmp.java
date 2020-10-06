package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class FactorAmp extends CParseRule {
    // factorAmp ::= AMD number
    private CParseRule num;
    public FactorAmp (CParseContext pcx) { }


    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_AND;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);
        tk = ct.getNextToken(pcx);


        if (tk.getType() == CToken.TK_NUM) {
            num = new Number(pcx);
        } else {
            pcx.fatalError(tk.toExplainString() + "&の後ろはNumberです");
        }
        num.parse(pcx);
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {   // 意味のチェック
        if(num != null){
            num.semanticCheck(pcx);
            if (num.getCType().getType() != CToken.TK_NUM) {
                pcx.fatalError(num + " はアドレス値として不適です");
            }
            this.setCType(CType.getCType(CType.T_pint));    //*int
            this.setConstant(true);
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {         //code generator
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; factorAmp starts");
        num.codeGen(pcx);
        o.println(";;; factorAmp completes");
    }
}
