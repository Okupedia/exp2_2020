package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CType;

import java.io.PrintStream;

public class PrimaryMult extends CParseRule {
    //primaryMult ::= MULT variable
    CParseRule variable;
    private CToken op;

    public PrimaryMult(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return tk.getType() == CToken.TK_ASTERISK;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        if(!Variable.isFirst(pcx.getTokenizer().getNextToken(pcx))){
            pcx.fatalError("*の後ろはvariable(ident)である必要があります");
        }
        op = pcx.getTokenizer().getCurrentToken(pcx);
        variable = new Variable(pcx);
        variable.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(variable != null){
            variable.semanticCheck(pcx);
            if(variable.getCType().getType() == CType.T_pint){
                this.setCType(CType.getCType(CType.T_int));
            }else if(variable.getCType().getType() == CType.T_int){
                pcx.fatalError("数値はデリファレンスできません");
            }
            this.setConstant(variable.isConstant());
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; primarymult starts");
        if (variable != null) {
            variable.codeGen(pcx);
        }
        o.println("\tMOV\t-(R6), R0\t; PrimaryMult:アドレスを取り出して内容を参照し、積む<"
                + op.toExplainString() + ">");
        o.println("\tMOV\t(R0), (R6)+\t; PrimaryMult:");
        o.println(";;; primarymult completes");
    }
}
