package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CType;

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
        tk = pcx.getTokenizer().getNextToken(pcx);
        factor = new UnsignedFactor(pcx);
        factor.parse(pcx);
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (factor != null) {
            factor.semanticCheck(pcx);
            setCType(factor.getCType());		// unsignedFactorの型をそのままコピー
            setConstant(factor.isConstant());	// 常に定数
            //TODO:ポインタにはつけない
            if(factor.getCType().getType()== CType.T_pint){
                pcx.fatalError("ポインタに符号(-)はつけられません");
            }
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; factor starts");
        if (factor != null) {
            factor.codeGen(pcx);
        }
        o.println("\tMOV\t#0, R0\t; MinusFactor:減算するための0をレジスタにいれておく");
        o.println("\tSUB\t-(R6), R0\t; MinusFactor:負値にするために0との減算をする");
        o.println("\tMOV\tR0, (R6)+\t; MinusFactor:スタックに積み直す");
        o.println(";;; factor completes");
    }
}