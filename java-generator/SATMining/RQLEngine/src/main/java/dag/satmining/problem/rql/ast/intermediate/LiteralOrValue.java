package dag.satmining.problem.rql.ast.intermediate;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

public final class LiteralOrValue {
	
	int _l;
	boolean _v;
	public static final LiteralOrValue TRUE = new LiteralOrValue(true);
	public static final LiteralOrValue FALSE = new LiteralOrValue(false);

	private LiteralOrValue(boolean b) {
		this._v = b;
		this._l = 0;
	}

	public LiteralOrValue(int l) {
		this._v = false;
		this._l = l;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _l;
		result = prime * result + (_v ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiteralOrValue other = (LiteralOrValue) obj;
		if (_l != other._l)
			return false;
		if (_v != other._v)
			return false;
		return true;
	}

	public LiteralOrValue neg() {
		if (_l == 0) {
			return (_v ? FALSE : TRUE);
		} else {
			return new LiteralOrValue(-_l);
		}
	}
	
	public <L extends Literal<L>> L getLiteral(MinimalClauseBuilder<L> b) {
		return b.fromDimacs(_l);
	}
	
	public static LiteralOrValue trueV() {
		return TRUE;
	}
	
	public static LiteralOrValue falseV() {
		return FALSE;
	}
}