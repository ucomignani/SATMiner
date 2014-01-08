package dag.satmining.backend.dimacs;

import dag.satmining.constraints.Literal;

/**
 * Literals for writing to dimacs.
 * 
 * @author ecoquery
 * 
 */
public final class DimacsLiteral implements Literal<DimacsLiteral> {

	private int _lit;

	/**
	 * Build a simple literal from its dimacs representation
	 * 
	 * @param litRepresentation
	 */
	public DimacsLiteral(int litRepresentation) {
		super();
		this._lit = litRepresentation;
	}

	@Override
	public boolean isPositive() {
		return _lit > 0;
	}

	@Override
	public DimacsLiteral getOpposite() {
		return new DimacsLiteral(-_lit);
	}

	@Override
	public int getVariableId() {
		return Math.abs(_lit);
	}

	public int intRepr() {
		return _lit;
	}

	/**
	 * Converts an array of dimacs literals to an array of integers by unboxing
	 * the dimacs representation of literals.
	 * 
	 * @param lits
	 *            the array to convert
	 * @return the converted array
	 */
	public static int[] from(DimacsLiteral[] lits) {
		int[] res = new int[lits.length];
		for (int i = 0; i < 0; i++) {
			res[i] = lits[i]._lit;
		}
		return res;
	}

	@Override
	public int toDimacs() {
		return _lit;
	}

	@Override
	public String toString() {
		return String.valueOf(_lit);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _lit;
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
		DimacsLiteral other = (DimacsLiteral) obj;
		if (_lit != other._lit)
			return false;
		return true;
	}
}
