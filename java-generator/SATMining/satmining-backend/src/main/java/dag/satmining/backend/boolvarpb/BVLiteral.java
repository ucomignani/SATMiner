/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.backend.boolvarpb;

import boolvar.model.Literal;

/**
 * 
 * @author ecoquery
 */
public final class BVLiteral implements
		dag.satmining.constraints.Literal<BVLiteral> {

	private final Literal _literal;

	public BVLiteral(Literal literal) {
		this._literal = literal;
	}

	@Override
	public boolean isPositive() {
		return _literal.getSign();
	}

	@Override
	public BVLiteral getOpposite() {
		return new BVLiteral(_literal.neg());
	}

	@Override
	public int getVariableId() {
		return _literal.getVariable().getId();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_literal == null) ? 0 : _literal.hashCode());
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
		BVLiteral other = (BVLiteral) obj;
		if (_literal == null) {
			if (other._literal != null)
				return false;
		} else if (!_literal.equals(other._literal))
			return false;
		return true;
	}

	Literal getLitImpl() {
		return _literal;
	}

	@Override
	public int toDimacs() {
		return toDimacs(_literal);
	}

	public static int toDimacs(Literal l) {
		return l.getSign() ? l.getVariable().getId() : -l.getVariable().getId();
	}
	
	@Override
	public String toString() {
		return _literal.toString();
	}
}
