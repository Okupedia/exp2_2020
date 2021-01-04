package lang.c.parse;

import lang.FatalErrorException;
import lang.c.*;

import java.io.PrintStream;

public class Array extends CParseRule{
    CParseRule expression;
    //array ::= LBRA expression RBRA
    public Array(CParseContext pcx) {}

    public static boolean isFirst(CToken tk){
        return tk.getType() == CToken.TK_LBRA;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        var tokenizer = pcx.getTokenizer();
        tokenizer.getNextToken(pcx);
        expression = new Expression(pcx);
        expression.parse(pcx);
        var token = tokenizer.getCurrentToken(pcx);
        if(token.getType() != CToken.TK_RBRA){
            pcx.fatalError("[]が閉じていません");
        }
        tokenizer.getNextToken(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(expression != null){
            expression.semanticCheck(pcx);
            if(!expression.getCType().isCType(CType.T_int)){
                pcx.fatalError("配列のインデックスはintである必要があります");
            }
            this.setCType(expression.getCType());
            this.setConstant(expression.isConstant());
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; array starts");
        if (expression != null) {
            expression.codeGen(pcx);
        }
        o.println("\tMOV\t-(R6), R0\t; Array:");
        o.println("\tADD\t-(R6), R0\t; Array:配列が表す番地を計算");
        o.println("\tMOV\tR0, (R6)+\t; Array:積む");
        o.println(";;; array completes");
    }
}
