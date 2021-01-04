package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;

import java.io.PrintStream;

public class AddressToValue extends CParseRule {
    //addressToValue ::= primary
    CParseRule primary;

    public AddressToValue(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return Primary.isFirst(tk);
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        primary = new Primary(pcx);
        primary.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(primary != null) {
            primary.semanticCheck(pcx);
            this.setCType(primary.getCType());
            this.setConstant(primary.isConstant()); //false
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; addresstovalue starts");
        if (primary != null) {
            primary.codeGen(pcx);
        }
        o.println("\tMOV\t-(R6), R0\t; addressToValue:番地を取り出す");
        o.println("\tMOV\t(R0), (R6)+\t; addressToValue:番地を値に変えて積み直す");
        o.println(";;; addresstovalue completes");
    }
}
