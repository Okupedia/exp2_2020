package lang.c.parse;

import lang.FatalErrorException;
import lang.c.*;
import java.io.PrintStream;

public class Variable extends CParseRule{
    //variable ::= ident [array]
    CParseRule ident;
    CParseRule array;

    public Variable(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return Ident.isFirst(tk);
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        ident = new Ident(pcx);
        ident.parse(pcx);
        var tk = pcx.getTokenizer().getCurrentToken(pcx);
        if(tk.getType() == CToken.TK_LBRA){
            array = new Array(pcx);
            array.parse(pcx);
        }
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(ident != null){
            ident.semanticCheck(pcx);
            var isIntArr = (ident.getCType().getType() == CType.T_int_arr);
            var isPIntArr = (ident.getCType().getType() == CType.T_pint_arr);
            /*
            if(array != null){
                if(!isIntArr && !isPIntArr){
                    pcx.fatalError("Identの型が配列型ではないため不適切です");
                }
                array.semanticCheck(pcx);
                if(isIntArr){
                    this.setCType(CType.getCType(CType.T_int));
                }else if(isPIntArr){
                    this.setCType(CType.getCType(CType.T_pint));
                }
            }else{
                if(isIntArr||isPIntArr){
                    pcx.fatalError("Arrayにインデックスが指定されていません");
                }
                this.setCType(ident.getCType());
            }
             */
            if(isIntArr||isPIntArr){
                if(array == null) {
                    pcx.fatalError("Arrayにインデックスが指定されていません");
                }
                array.semanticCheck(pcx);
                if(isIntArr){
                    this.setCType(CType.getCType(CType.T_int));
                }else if(isPIntArr){
                    this.setCType(CType.getCType(CType.T_pint));
                }
            }
            this.setCType(ident.getCType());
            this.setConstant(ident.isConstant());
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; variable starts");
        if(ident != null){
            ident.codeGen(pcx);
        }
        if(array != null){
            array.codeGen(pcx);
        }
        o.println(";;; variable completes");
    }
}
