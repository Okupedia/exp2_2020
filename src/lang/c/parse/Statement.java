package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;

import java.util.ArrayList;
import java.io.PrintStream;

public class Statement extends CParseRule{
    //statement   ::= statementAssign
    //->statement   ::= statementAssign | statementIf | statementWhile | statementBlock | statementOutput | statementInput
    CParseRule statement;

    public Statement(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return StatementAssign.isFirst(tk)
                || StatementIf.isFirst(tk)
                || StatementWhile.isFirst(tk)
                || StatementBlock.isFirst(tk)
                || StatementOutput.isFirst(tk)
                || StatementInput.isFirst(tk);
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        statement = switch(pcx.getTokenizer().getCurrentToken(pcx).getType()){
            case CToken.TK_IF -> new StatementIf(pcx);
            case CToken.TK_WHILE-> new StatementWhile(pcx);
            case CToken.TK_LCBRA-> new StatementBlock(pcx);
            case CToken.TK_OUTPUT-> new StatementOutput(pcx);
            case CToken.TK_INPUT-> new StatementInput(pcx);
            default -> {
                yield new StatementAssign(pcx);
            }
        };
        statement.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(statement != null){
            statement.semanticCheck(pcx);
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        final var printStream = pcx.getIOContext().getOutStream();
        printStream.println(";;; Statement starts");
        if (statement != null) {
            statement.codeGen(pcx);
        }
        printStream.println(";;; Statement completes");
    }
}

class StatementAssign extends CParseRule{
    //statementAssign    ::= primary ASSIGN expression SEMI
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
            pcx.fatalError(tokenizer.getCurrentToken(pcx).toExplainString() + "primaryの後には=が必要です");
        }
        //expression
        tokenizer.getNextToken(pcx);
        expression = new Expression(pcx);
        expression.parse(pcx);
        //SEMI
        if(tokenizer.getCurrentToken(pcx).getType() != CToken.TK_SEMI){
            pcx.fatalError(tokenizer.getCurrentToken(pcx).toExplainString() +";が足りません");
        }

        tokenizer.getNextToken(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
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
            pcx.fatalError("左辺がconstant(定数)なので値を代入することはできません");
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
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

class StatementIf extends CParseRule{
    //statementIf     ::= IF LPAR condition RPAR statement [ELSE statement]
    CParseRule condition, statement, elseStatement;
    public StatementIf(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return tk.getType() == CToken.TK_IF;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        var tokenizer = pcx.getTokenizer();
        var token = tokenizer.getNextToken(pcx); //ifの続きを見る

        if(token.getType() != CToken.TK_LPAR){
            pcx.fatalError(token.toExplainString( )+ "ifの後ろには(を続ける必要があります");
        }

        token = tokenizer.getNextToken(pcx);
        condition = new Condition(pcx);
        condition.parse(pcx);

        token = tokenizer.getCurrentToken(pcx);
        if(token.getType() != CToken.TK_RPAR){
            pcx.fatalError(token.toExplainString() + "()が閉じていません");
        }

        token = tokenizer.getNextToken(pcx);
        statement = new Statement(pcx);
        statement.parse(pcx);

        token = tokenizer.getCurrentToken(pcx);
        if(token.getType() == CToken.TK_ELSE){
            token = tokenizer.getNextToken(pcx);
            if(!Statement.isFirst(token)){
                pcx.fatalError(token.toExplainString() + "elseがあるので続きが必要です");
            }
            elseStatement = new Statement(pcx);
            elseStatement.parse(pcx);
        }

    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(condition != null){
            condition.semanticCheck(pcx);
        }
        if(statement != null){
            statement.semanticCheck(pcx);
        }
        if(elseStatement != null){
            elseStatement.semanticCheck(pcx);
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        final var seq = pcx.getSeqId();
        o.println(";;; StatementsIf Starts");
        if (condition != null) {
            condition.codeGen(pcx);
        }
        o.println("\tMOV\t-(R6), R0\t;StatementIF: スタックからconditionの結果を持ってくる");
        o.println("\tBRZ endIf" + seq + "\t;;; StatementIF:trueなら1なので Z=0の時は以下のtrueの処理をスキップする");
        if (statement != null) {
            statement.codeGen(pcx);
        }
        if (elseStatement != null) {
            o.println("\tJMP endElse" + seq + "\t;;; StatementIF:trueの処理が終了したのでfalseの処理をスキップする");
            o.println("endIf" + seq + ": \t\t;StatementIF: ラベル生成(trueの処理が終了 falseの処理が始まる)");
            elseStatement.codeGen(pcx);
            o.println("endElse" + seq + ": \t\t;StatementIF: ラベル生成(falseの処理が終了)");
        } else {
            o.println("endIf" + seq + ": \t\t;StatementIF: ラベル生成(trueの処理が終了)");
        }
        o.println(";;; StatementsIf Completes");
    }
}

class StatementWhile extends CParseRule{
    //statementWhile  ::= WHILE LPAR condition RPAR statement
    CParseRule condition, statement;

    public StatementWhile(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return tk.getType() == CToken.TK_WHILE;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        var tokenizer = pcx.getTokenizer();
        var token = tokenizer.getNextToken(pcx); //whileの続きを見る

        if(token.getType() != CToken.TK_LPAR){
            pcx.fatalError(token.toExplainString() + "ifの後ろには(を続ける必要があります");
        }

        token = tokenizer.getNextToken(pcx);
        condition = new Condition(pcx);
        condition.parse(pcx);

        token = tokenizer.getCurrentToken(pcx);
        if(token.getType() != CToken.TK_RPAR){
            pcx.fatalError(token.toExplainString() + "()が閉じていません");
        }

        token = tokenizer.getNextToken(pcx);
        statement = new Statement(pcx);
        statement.parse(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(condition != null){
            condition.semanticCheck(pcx);
        }
        if(statement != null){
            statement.semanticCheck(pcx);
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        final var seq = pcx.getSeqId();
        o.println(";;; StatementWhile Starts");
        o.println("while" + seq + ":\t;StatementWhile: ラベル生成(whileの処理が終わった後に戻ってくるためのラベル)");
        if (condition != null) {
            condition.codeGen(pcx);
        }
        o.println("\tMOV\t-(R6), R0\t;StatementWhile: スタックからconditionの結果を持ってくる");
        o.println("BRZ whileEnd" + seq + "\t;;; StatementWhile:z=0(false)の時はwhile内の処理をスキップ");
        if (statement != null) {
            statement.codeGen(pcx);
        }
        o.println("\tJMP while" + seq + ":\t;StatementWhile:一通り処理を実施したらもう一度条件判定からやり直す");
        o.println("whileEnd" + seq + ":\t;StatementWhile: ラベル生成(while文の終了地点)");
        o.println(";;; StatementWhile Completes");
    }
}

class StatementOutput extends CParseRule{
    //statementOutput ::= OUTPUT expression SEMI
    CParseRule expression;

    public StatementOutput(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return tk.getType() == CToken.TK_OUTPUT;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        var tokenizer = pcx.getTokenizer();
        var token = tokenizer.getNextToken(pcx); //outputの続きを見る

        expression = new Expression(pcx);
        expression.parse(pcx);

        token = tokenizer.getCurrentToken(pcx);
        if(token.getType() != CToken.TK_SEMI){
            pcx.fatalError(token.toExplainString() + ";がありません");
        }
        tokenizer.getNextToken(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(expression != null){
            expression.semanticCheck(pcx);
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        if (expression != null) {
            expression.codeGen(pcx);
            o.println("\tMOV\t#0xFFE0, R3\t; StatementIn:I/Oアドレスをレジスタにセット");
            o.println("\tMOV\t-(R6), (R3)\t; StatementIn: Expressionの値を書き込み");
        }
    }
}

class StatementInput extends CParseRule{
    //statementInput  ::= INPUT primary SEMI
    CParseRule primary;

    public StatementInput(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return tk.getType() == CToken.TK_INPUT;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        var tokenizer = pcx.getTokenizer();
        var token = tokenizer.getNextToken(pcx); //inputの続きを見る

        primary = new Primary(pcx);
        primary.parse(pcx);

        token = tokenizer.getCurrentToken(pcx);
        if(token.getType() != CToken.TK_SEMI){
            pcx.fatalError(token.toExplainString() + ";がありません");
        }
        tokenizer.getNextToken(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(primary != null){
            primary.semanticCheck(pcx);
            if(primary.isConstant()){
                pcx.fatalError("定数に対してのinputは不正です");
            }
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        if (primary != null) {
            primary.codeGen(pcx);
            o.println("\tMOV\t-(R6), R0; StatementIn: 変数のアドレスをスタックからpop");
            o.println("\tMOV\t#0xFFE0, R3\t; StatementIn:");
            o.println("\tMOV\t(R3), (R0)\t; StatementIn: Primaryの値を変数に書き込み");
        }
    }
}

class StatementBlock extends CParseRule{
    //statementBlock  ::= LCBRA {statement} RCBRA
    ArrayList<CParseRule> statementList = new ArrayList<>();

    public StatementBlock(CParseContext pcx){}

    public static boolean isFirst(CToken tk){
        return tk.getType() == CToken.TK_LCBRA;
    }

    @Override
    public void parse(CParseContext pcx) throws FatalErrorException {
        var tokenizer = pcx.getTokenizer();
        var token = tokenizer.getNextToken(pcx); //{続きを見る

        while(Statement.isFirst(token)){
            var statement = new Statement(pcx);
            statement.parse(pcx);
            statementList.add(statement);
            token = tokenizer.getCurrentToken(pcx);
        }

        token = tokenizer.getCurrentToken(pcx);
        if(token.getType() != CToken.TK_RCBRA){
            pcx.fatalError(token.toExplainString() + "{}が閉じていません");
        }
        tokenizer.getNextToken(pcx);
    }

    @Override
    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        for(var statement : statementList){
           statement.semanticCheck(pcx);
        }
    }

    @Override
    public void codeGen(CParseContext pcx) throws FatalErrorException {
        for(var statement : statementList){
            statement.codeGen(pcx);
        }
    }
}