package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;

public class StatementAssign extends CParseRule{
    //pstatementAssign    ::= primary ASSIGN expression SEMI
    CParseRule primary, expression;

    public StatementAssign(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return Primary.isFirst(tk);
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        var tokenizer = pcx.getTokenizer();
        //primary
        primary = new Primary(pcx);
        primary.parse(pcx);
        // ASSIGN
        if(tokenizer.getCurrentToken(pcx).getType() != CToken.TK_ASSIGN){
            pcx.fatalError("primaryの後には=が必要です");
        }
        //expression
        tokenizer.getNextToken(pcx);
        expression = new Expression(pcx);
        expression.parse(pcx);
        //SEMI
        if(tokenizer.getCurrentToken(pcx).getType() != CToken.TK_SEMI){
            pcx.fatalError(";が足りません");
        }

        tokenizer.getNextToken(pcx);
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        if(primary != null){
            primary.semanticCheck(pcx);
        }
        if(expression != null){
            expression.semanticCheck(pcx);
        }
        final var leftType = primary.getCType();
        final var rightType = expression.getCType();
        if (leftType.getType() != rightType.getType()) {
            pcx.fatalError(String.format("左辺の型[%s]と右辺の型[%s]が一致しません\n",
                    leftType.toString(), rightType.toString()));
        }
        if (primary.isConstant()) {
            pcx.fatalError("primaryがconstant(定数)なので値を代入することはできません");
        }
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        final var printStream = pcx.getIOContext().getOutStream();
        printStream.println(";;; StatementAssign starts");
        if (primary != null) {
            primary.codeGen(pcx);
        }
        if (expression != null) {
            expression.codeGen(pcx);
        }
        printStream.println("\tMOV\t-(R6), R1\t; StatementAssign: 右辺の値を取り出す");
        printStream.println("\tMOV\t-(R6), R0\t; StatementAssign: 左辺のアドレスを取り出す");
        printStream.println("\tMOV\t   R1, (R0)\t; StatementAssign: 変数に値を代入する");
        printStream.println(";;; StatementAssign completes");
    }
}
