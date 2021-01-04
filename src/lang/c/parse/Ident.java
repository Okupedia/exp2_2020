package lang.c.parse;

import java.lang.*;
import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CType;

import java.io.PrintStream;

public class Ident extends CParseRule {
    //ident ::= IDENT
    CToken ident;
    public Ident(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return tk.getType() == CToken.TK_IDENT;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        var token = pcx.getTokenizer().getCurrentToken(pcx);
        if(token.getType() != CToken.TK_IDENT){
            pcx.fatalError("型がIDENTではありません");
        }
        ident = token;  //ここで綴りを保存しておく
        pcx.getTokenizer().getNextToken(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(ident != null){
            this.setCType(CType.getCType(CType.T_int_arr));
            this.setConstant(false);
        }
        var identString = ident.toExplainString();
        int setType = CType.T_int;
        boolean isConst = false;
        if(identString.startsWith("i_")) {
            setType = CType.T_int;
        }else if(identString.startsWith("ip_")){
            setType = CType.T_pint;
        }else if(identString.startsWith("ia_")){
            setType = CType.T_int_arr;
        }else if(identString.startsWith("ipa_")){
            setType = CType.T_pint_arr;
        }else if(identString.startsWith("c_")){
            setType = CType.T_int;
            isConst = true;
        }
        this.setCType(CType.getCType(setType));
        this.setConstant(isConst);
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; ident starts");
        if(ident != null){
            //MOV #ident, (R6)+
            o.println("\tMOV\t#" + ident.getText() + ", (R6)+"
            + "\t; Ident: 変数アドレスを積む<" + ident.toExplainString() + ">");
        }
        o.println(";;; ident completes");
    }
}
