package dag.satmining.constraints;

public enum Ineq {

	EQ, LEQ, GEQ;

	public String toString() {
		switch (this) {
		case EQ:
			return "=";
		case LEQ:
			return "<=";
		case GEQ:
			return ">=";
		default:
			throw new Error("Bug in Ineq: unknown case");
		}
	}

	public Ineq op() {
		switch (this) {
		case EQ:
			return EQ;
		case LEQ:
			return GEQ;
		case GEQ:
			return LEQ;
		default:
			throw new Error("Bug in Ineq: unknown case");
		}
	}
}
