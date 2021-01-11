package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CType;

import java.io.PrintStream;


public class Condition extends CParseRule {
    //condition ::= TRUE | FALSE | expression
    // (conditionLT | conditionLE | conditionGT | conditionGE | conditionEQ | conditionNE)
    CParseRule nextCondition;
    Boolean trueOrFalse = null;

    public Condition(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return tk.getType() == CToken.TK_TRUE || tk.getType() == CToken.TK_FALSE || Expression.isFirst(tk);
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        var tokenizer = pcx.getTokenizer();
        var token = tokenizer.getCurrentToken(pcx);
        if(Expression.isFirst(token)){
            var expression = new Expression(pcx);
            expression.parse(pcx);

            token = tokenizer.getCurrentToken(pcx);
            nextCondition = switch(token.getType()){
                case CToken.TK_LT -> new ConditionLT(pcx, expression);
                case CToken.TK_LE -> new ConditionLE(pcx, expression);
                case CToken.TK_GT -> new ConditionGT(pcx, expression);
                case CToken.TK_GE -> new ConditionGE(pcx, expression);
                case CToken.TK_EQ -> new ConditionEQ(pcx, expression);
                case CToken.TK_NE -> new ConditionNE(pcx, expression);
                default -> {
                    pcx.fatalError("expressionの後ろは条件判定演算子である必要があります");
                    yield new ConditionLT(pcx, expression);
                }
            };
            nextCondition.parse(pcx);
        }else {
            if(token.getType() == CToken.TK_TRUE){
                trueOrFalse = true;
            }else{
                trueOrFalse = false;
            }
            tokenizer.getNextToken(pcx);
        }

    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(nextCondition != null){
            nextCondition.semanticCheck(pcx);
            this.setCType(nextCondition.getCType());
            this.setConstant(nextCondition.isConstant());
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; condition starts");
        if(nextCondition != null){
            nextCondition.codeGen(pcx);
        }
        if(trueOrFalse != null){
            if(trueOrFalse){
                // MOV #0x0001 (R6)+
                o.println("\tMOV\\t#0x0001, (R6)+\t; Condition: trueとして1を積む");
            }else{
                o.println("\tMOV\\t#0x0000, (R6)+\t; Condition: falseとして0を積む");
            }
        }
        o.println(";;; condition completes");
    }
}

class ConditionLT extends CParseRule{
    //conditionLT ::= LT expression //<

    private CParseRule left, right; //right = expression
    private int seq;

    public ConditionLT(CParseContext pcx, CParseRule left){
        this.left = left;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        if(!Expression.isFirst(pcx.getTokenizer().getNextToken(pcx))){
            pcx.fatalError("比較演算子の右辺にはexpressionが必要です");
        }
        right = new Expression(pcx);
        right.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(left != null && right != null){
            left.semanticCheck(pcx);
            right.semanticCheck(pcx);
            var leftType = left.getCType();
            var rightType = right.getCType();
            if (leftType.getType() != rightType.getType()) {
                pcx.fatalError("左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]が一致していないため比較ができません");
            }
        }
        this.setCType(CType.getCType(CType.T_bool));
        this.setConstant(true);
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; condition < (compare) starts");
        if (left != null && right != null) {
            left.codeGen(pcx);
            right.codeGen(pcx);
            int seq = pcx.getSeqId();
            o.println("\tMOV\t-(R6), R0\t; ConditionLT: 2数を取り出して, 比べる");
            o.println("\tMOV\t-(R6), R1\t; ConditionLT:");
            o.println("\tMOV\t#0x0001, R2\t; ConditionLT: set true");
            o.println("\tCMP\tR0, R1\t; ConditionLT: R1 < R0 = R1-R0 < 0");
            o.println("\tBRN\tLT" + seq + " ; ConditionLT:N=1ならseqの値をR7へ入れる");
            o.println("\tCLR\tR2\t\t; ConditionLT: set false");
            o.println("LT" + seq + ":\tMOV\tR2, (R6)+\t; ConditionLT:");
        }
        o.println(";;; condition < (compare) completes");
    }
}

class ConditionLE extends CParseRule{
    //conditionLE ::= LE expression //<=

    private CParseRule left, right; //right = expression
    private int seq;

    public ConditionLE(CParseContext pcx, CParseRule left){
        this.left = left;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        if(!Expression.isFirst(pcx.getTokenizer().getNextToken(pcx))){
            pcx.fatalError("比較演算子の右辺にはexpressionが必要です");
        }
        right = new Expression(pcx);
        right.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(left != null && right != null){
            left.semanticCheck(pcx);
            right.semanticCheck(pcx);
            var leftType = left.getCType();
            var rightType = right.getCType();
            if (leftType.getType() != rightType.getType()) {
                pcx.fatalError("左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]が一致していないため比較ができません");
            }
        }
        this.setCType(CType.getCType(CType.T_bool));
        this.setConstant(true);
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; condition <= (compare) starts");
        if (left != null && right != null) {
            left.codeGen(pcx);
            right.codeGen(pcx);
            int seq = pcx.getSeqId();
            o.println("\tMOV\t-(R6), R0\t; ConditionLE: 2数を取り出して, 比べる");
            o.println("\tMOV\t-(R6), R1\t; ConditionLE:");
            o.println("\tMOV\t#0x0001, R2\t; ConditionLE: set true");
            o.println("\tCMP\tR0, R1\t; ConditionLE: R1 <= R0 = R1-R0 <= 0");
            o.println("\tBRN\tLT" + seq + " ; ConditionLE:N=1(減算結果が負)ならseqの値をR7へ入れる");
            o.println("\tBRZ\tLT" + seq + " ; ConditionLE:Z=1(減算結果が0)ならseqの値をR7へ入れる");
            o.println("\tCLR\tR2\t\t; ConditionLE: set false");
            o.println("LT" + seq + ":\tMOV\tR2, (R6)+\t; ConditionLE:");
        }
        o.println(";;; condition <= (compare) completes");
    }
}

class ConditionGT extends CParseRule{

    //conditionGT ::= GT expression //>

    private CParseRule left, right; //right = expression
    private int seq;

    public ConditionGT(CParseContext pcx, CParseRule left){
        this.left = left;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        if(!Expression.isFirst(pcx.getTokenizer().getNextToken(pcx))){
            pcx.fatalError("比較演算子の右辺にはexpressionが必要です");
        }
        right = new Expression(pcx);
        right.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(left != null && right != null){
            left.semanticCheck(pcx);
            right.semanticCheck(pcx);
            var leftType = left.getCType();
            var rightType = right.getCType();
            if (leftType.getType() != rightType.getType()) {
                pcx.fatalError("左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]が一致していないため比較ができません");
            }
        }
        this.setCType(CType.getCType(CType.T_bool));
        this.setConstant(true);
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; condition < (compare) starts");
        if (left != null && right != null) {
            left.codeGen(pcx);
            right.codeGen(pcx);
            int seq = pcx.getSeqId();
            o.println("\tMOV\t-(R6), R0\t; ConditionGT: 2数を取り出して, 比べる");
            o.println("\tMOV\t-(R6), R1\t; ConditionGT:");
            o.println("\tMOV\t#0x0001, R2\t; ConditionGT: set true");
            o.println("\tCMP\tR1, R0\t; ConditionGT: R1 > R0 = R0 - R1 < 0");
            o.println("\tBRN\tGT" + seq + " ; ConditionGT:N=1ならseqの値をR7へ入れる");
            o.println("\tCLR\tR2\t\t; ConditionGT: set false");
            o.println("GT" + seq + ":\tMOV\tR2, (R6)+\t; ConditionGT:");
        }
        o.println(";;; condition < (compare) completes");
    }

}

class ConditionGE extends CParseRule{
    //conditionGE ::= GE expression //>=

    private CParseRule left, right; //right = expression
    private int seq;

    public ConditionGE(CParseContext pcx, CParseRule left){
        this.left = left;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        if(!Expression.isFirst(pcx.getTokenizer().getNextToken(pcx))){
            pcx.fatalError("比較演算子の右辺にはexpressionが必要です");
        }
        right = new Expression(pcx);
        right.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(left != null && right != null){
            left.semanticCheck(pcx);
            right.semanticCheck(pcx);
            var leftType = left.getCType();
            var rightType = right.getCType();
            if (leftType.getType() != rightType.getType()) {
                pcx.fatalError("左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]が一致していないため比較ができません");
            }
        }
        this.setCType(CType.getCType(CType.T_bool));
        this.setConstant(true);
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; condition >= (compare) starts");
        if (left != null && right != null) {
            left.codeGen(pcx);
            right.codeGen(pcx);
            int seq = pcx.getSeqId();
            o.println("\tMOV\t-(R6), R0\t; ConditionGE: 2数を取り出して, 比べる");
            o.println("\tMOV\t-(R6), R1\t; ConditionGE:");
            o.println("\tMOV\t#0x0001, R2\t; ConditionGE: set true");
            o.println("\tCMP\tR1, R0\t; ConditionGE: (R1 >= R0) = (R0 - R1 =< 0)");
            o.println("\tBRZ\tGE" + seq + " ; ConditionGE::N=1(減算結果が負)ならseqの値をR7へ入れる");
            o.println("\tBRN\tGE" + seq + " ; ConditionGE:Z=1(減算結果が0)ならseqの値をR7へ入れる");
            o.println("\tCLR\tR2\t\t; ConditionGE: set false");
            o.println("GE" + seq + ":\tMOV\tR2, (R6)+\t; ConditionGE:");
        }
        o.println(";;; condition >= (compare) completes");
    }
}

class ConditionEQ extends CParseRule{
    //conditionEQ ::= EQ expression // ==

    private CParseRule left, right; //right = expression
    private int seq;

    public ConditionEQ(CParseContext pcx, CParseRule left){
        this.left = left;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        if(!Expression.isFirst(pcx.getTokenizer().getNextToken(pcx))){
            pcx.fatalError("比較演算子の右辺にはexpressionが必要です");
        }
        right = new Expression(pcx);
        right.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(left != null && right != null){
            left.semanticCheck(pcx);
            right.semanticCheck(pcx);
            var leftType = left.getCType();
            var rightType = right.getCType();
            if (leftType.getType() != rightType.getType()) {
                pcx.fatalError("左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]が一致していないため比較ができません");
            }
        }
        this.setCType(CType.getCType(CType.T_bool));
        this.setConstant(true);
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; condition == (compare) starts");
        if (left != null && right != null) {
            left.codeGen(pcx);
            right.codeGen(pcx);
            int seq = pcx.getSeqId();
            o.println("\tMOV\t-(R6), R0\t; ConditionEQ: 2数を取り出して, 比べる");
            o.println("\tMOV\t-(R6), R1\t; ConditionEQ:");
            o.println("\tMOV\t#0x0001, R2\t; ConditionEQ: set true");
            o.println("\tCMP\tR0, R1\t; ConditionEQ: R1 == R0 = R1-R0 = 0");
            o.println("\tBRZ\tLT" + seq + " ; ConditionEQ: Z=1ならseqの値をR7へ入れる");
            o.println("\tCLR\tR2\t\t; ConditionEQ: set false");
            o.println("LT" + seq + ":\tMOV\tR2, (R6)+\t; ConditionEQ:");
        }
        o.println(";;; condition == (compare) completes");
    }
}

class ConditionNE extends CParseRule{
    //conditionNE ::= NE expression // !=

    private CParseRule left, right; //right = expression
    private int seq;

    public ConditionNE(CParseContext pcx, CParseRule left){
        this.left = left;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        if(!Expression.isFirst(pcx.getTokenizer().getNextToken(pcx))){
            pcx.fatalError("比較演算子の右辺にはexpressionが必要です");
        }
        right = new Expression(pcx);
        right.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(left != null && right != null){
            left.semanticCheck(pcx);
            right.semanticCheck(pcx);
            var leftType = left.getCType();
            var rightType = right.getCType();
            if (leftType.getType() != rightType.getType()) {
                pcx.fatalError("左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]が一致していないため比較ができません");
            }
        }
        this.setCType(CType.getCType(CType.T_bool));
        this.setConstant(true);
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; condition ==(compare) starts");
        if (left != null && right != null) {
            left.codeGen(pcx);
            right.codeGen(pcx);
            int seq = pcx.getSeqId();
            o.println("\tMOV\t-(R6), R0\t; ConditionNE: 2数を取り出して, 比べる");
            o.println("\tMOV\t-(R6), R1\t; ConditionNE:");
            o.println("\tCLR\tR2\t\t; ConditionNE: set false");
            o.println("\tCMP\tR1, R0\t; ConditionNE: R1 == R0 = R0 - R1 = 0");
            o.println("\tBRZ\tNE" + seq + " ; ConditionNE:");
            o.println("\tMOV\t#0x0001, R2\t; ConditionNE: set true");
            o.println("NE" + seq + ":\tMOV\tR2, (R6)+\t; ConditionNE:");
        }
        o.println(";;; condition == (compare) completes");
    }
}
