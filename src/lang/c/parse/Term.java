package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

import java.util.ArrayList;

public class Term extends CParseRule {
	// term ::= factor {termMult | termDiv}
	private CParseRule factor;
	private ArrayList<CParseRule> mulDivList = new ArrayList<>();//ダイアモンド演算子

	public Term(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return Factor.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		factor = new Factor(pcx);
		factor.parse(pcx);
		CToken tk = pcx.getTokenizer().getCurrentToken(pcx);
		while(TermMult.isFirst(tk) || TermDiv.isFirst(tk) ){
			final var mulDiv = switch(tk.getType()){
				case CToken.TK_ASTERISK -> new TermMult(pcx);
				case CToken.TK_SLASH -> new TermDiv(pcx);
				default -> {
					pcx.fatalError(tk.toExplainString() + "これは予期していない動作です");
					yield new TermMult(pcx);
				}
			};
			tk = pcx.getTokenizer().getNextToken(pcx);
			mulDiv.parse(pcx);
			mulDivList.add(mulDiv);
			tk = pcx.getTokenizer().getCurrentToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (factor != null) {
			factor.semanticCheck(pcx);
			this.setCType(factor.getCType());		// factor の型をそのままコピー
			this.setConstant(factor.isConstant());
		}

		// 複数の時は左右の型を見て、結果の型を決定する
		if(mulDivList.size() == 0){
			factor.semanticCheck(pcx);
			this.setCType(factor.getCType());		// factor の型をそのままコピー
			this.setConstant(factor.isConstant());
		}else{
			var left = factor;
			for(var mulDiv : mulDivList){// 拡張for
				mulDiv.semanticCheck(pcx);
				var right = mulDiv;

				//乗除算ではポインタを扱わない
				var leftType = left.getCType().getType();
				var rightType = right.getCType().getType();
				if((leftType & rightType) != 1){// どちらもポインタじゃないことを確認
					pcx.fatalError("ポインタを含む乗除算はできません");
				}
				this.setCType(CType.getCType(CType.T_int));

				this.setConstant(left.isConstant()&right.isConstant());

				left = mulDiv;
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; term starts");
		if (factor != null) { factor.codeGen(pcx); }
		for(var mulDiv : mulDivList){
			mulDiv.codeGen(pcx);
		}
		o.println(";;; term completes");
	}
}
