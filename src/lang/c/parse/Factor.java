package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Factor extends CParseRule {
	//factor ::= plusFactor | minusFactor | unsignedFactor
	private CParseRule factor;

	public Factor(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return PlusFactor.isFirst(tk) || MinusFactor.isFirst(tk) || UnsignedFactor.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CToken tk = pcx.getTokenizer().getCurrentToken(pcx);
		factor = switch (tk.getType()){
			case CToken.TK_PLUS -> new PlusFactor(pcx);
			case CToken.TK_MINUS -> new MinusFactor(pcx);
			default -> new UnsignedFactor(pcx);
		};
		factor.parse(pcx);
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
		o.println(";;; factor starts");
		if (factor != null) { factor.codeGen(pcx); }
		o.println(";;; factor completes");
	}
}