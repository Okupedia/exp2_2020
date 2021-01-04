package lang.c.parse;

import lang.FatalErrorException;
import lang.Tokenizer;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;

import java.io.PrintStream;

public class UnsignedFactor extends CParseRule {
    //unsignedFactor ::= factorAmp | number | LPAR expression RPAR
    //->unsignedFactor ::= factorAmp | number | LPAR expression RPAR | addressToValue
    private CParseRule factor;

    public UnsignedFactor(CParseContext pcx){

    }

    public static boolean isFirst(CToken tk) {
        return FactorAmp.isFirst(tk)
                || Number.isFirst(tk)
                || tk.getType() == CToken.TK_LPAR
                || AddressToValue.isFirst(tk);
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CToken tk = pcx.getTokenizer().getCurrentToken(pcx);
        //System.out.println("this is UnsignedFactor. this token type is " + tk.getType());
        switch(tk.getType()){
            case CToken.TK_LPAR:
                tk = pcx.getTokenizer().getNextToken(pcx);// (は読み飛ばす
                System.out.println("this token is " + tk.getText());
                if(Expression.isFirst(tk)){
                    factor = new Expression(pcx);
                    factor.parse(pcx);
                    tk = pcx.getTokenizer().getCurrentToken(pcx);
                    if(tk.getType() != CToken.TK_RPAR){
                        // 括弧が閉じられていないとき
                        pcx.fatalError(tk.toExplainString() + "括弧が閉じられていません");
                    }
                    tk = pcx.getTokenizer().getNextToken(pcx);
                }else{
                    pcx.fatalError(tk.toExplainString() + "括弧の後ろはExpressionです");
                }
                break;
            case CToken.TK_AND:
                factor = new FactorAmp(pcx);
                factor.parse(pcx);
                break;
            case CToken.TK_NUM:
                factor = new Number(pcx);
                factor.parse(pcx);
                break;
            default:
                factor = new AddressToValue(pcx);
                factor.parse(pcx);
                break;
        }
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
        o.println(";;; unsigned factor starts");
        if (factor != null) { factor.codeGen(pcx); }
        o.println(";;; unsigned factor completes");
    }
}
