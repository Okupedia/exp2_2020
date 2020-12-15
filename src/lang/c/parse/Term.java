package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Term extends CParseRule {
	// term ::= factor {termMult | termDiv}
	private CParseRule factor;

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
			factor = switch(tk.getType()){
				case CToken.TK_ASTERISK -> new TermMult(pcx);
				case CToken.TK_SLASH -> new TermDiv(pcx);
				default -> {
					pcx.fatalError(tk.toExplainString() + "これは予期していない動作です");
					yield new TermMult(pcx);
				}
			};
			tk = pcx.getTokenizer().getNextToken(pcx);
			factor.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (factor != null) {
			factor.semanticCheck(pcx);
			this.setCType(factor.getCType());		// factor の型をそのままコピー
			this.setConstant(factor.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; term starts");
		if (factor != null) { factor.codeGen(pcx); }
		o.println(";;; term completes");
	}
}
