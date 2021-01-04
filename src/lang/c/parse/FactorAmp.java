package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class FactorAmp extends CParseRule {
    // factorAmp ::= AMD number
    //->factorAmp ::= AMP (number | primary)
    private CParseRule numPrim;
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
            numPrim = new Number(pcx);
        } else if (tk.getType() == CToken.TK_IDENT) {
            numPrim = new Primary(pcx);
        } else {
            pcx.fatalError(tk.toExplainString() + "&の後ろはNumberかPrimaryです");
        }
        numPrim.parse(pcx);
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {   // 意味のチェック
        if(numPrim != null){
            if (numPrim instanceof Primary) {
                if (((Primary) numPrim).isPrimayMult) {
                    pcx.fatalError("&の後ろに*は付けられません");
                }
            }
            numPrim.semanticCheck(pcx);
            int setType = switch(numPrim.getCType().getType()){
                case CType.T_int        -> CType.T_pint;
                case CType.T_int_arr    -> CType.T_pint_arr;
                default -> {
                    pcx.fatalError("ポインタに&はつけられません");
                    yield CType.T_err;
                }
            };
            this.setCType(CType.getCType(setType));
            this.setConstant(true);
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {         //code generator
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; factorAmp starts");
        numPrim.codeGen(pcx);
        o.println(";;; factorAmp completes");
    }
}
