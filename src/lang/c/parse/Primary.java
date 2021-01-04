package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;

import java.io.PrintStream;

public class Primary extends CParseRule {
    //primary ::= primaryMult | variable
    private CParseRule nextParseRule;
    private boolean isPrimayMult;

    public Primary(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return PrimaryMult.isFirst(tk) | Variable.isFirst(tk);
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        if(pcx.getTokenizer().getCurrentToken(pcx).getType() == CToken.TK_ASTERISK){
           isPrimayMult = true;
           nextParseRule = new PrimaryMult(pcx);
        }else{
            nextParseRule = new Variable(pcx);
        }
        nextParseRule.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(nextParseRule != null){
            nextParseRule.semanticCheck(pcx);
            this.setCType(nextParseRule.getCType());
            //this.setConstant(nextParseRule.isConstant());
            this.setConstant(false);
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; primary starts");
        if (nextParseRule != null) {
            nextParseRule.codeGen(pcx);
        }
        o.println(";;; primary completes");
    }
}
