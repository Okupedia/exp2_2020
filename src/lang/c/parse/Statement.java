package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;

public class Statement extends CParseRule{
    //statement   ::= statementAssign
    CParseRule statementAssign;

    public Statement(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return StatementAssign.isFirst(tk);
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        statementAssign = new StatementAssign(pcx);
        statementAssign.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(statementAssign != null){
            statementAssign.semanticCheck(pcx);
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        final var printStream = pcx.getIOContext().getOutStream();
        printStream.println(";;; Statement starts");
        if (statementAssign != null) {
            statementAssign.codeGen(pcx);
        }
        printStream.println(";;; Statement completes");
    }
}
