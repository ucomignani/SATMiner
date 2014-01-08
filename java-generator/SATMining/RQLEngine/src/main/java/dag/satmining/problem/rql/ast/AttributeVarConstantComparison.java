package dag.satmining.problem.rql.ast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dag.satmining.problem.rql.ast.intermediate.BFormula;
import dag.satmining.problem.rql.ast.intermediate.Constant;

public class AttributeVarConstantComparison extends MiningExpression {

	private AttributeVariable _a;
	private AttributeConstant _b;
	
	AttributeVarConstantComparison(AttributeVariable a, AttributeConstant b) {
		this._a = a;
		this._b = b;
	}

	@Override
	void registerSQLExpressions(Collection<AttributeConstant> atts,
			int maxAttId, SQLBinding binding, ASTDictionnary dict) {
		// do nothing
	}

	@Override
	BFormula buildIntermediateFormula(AttributeValuation attVal,
			Collection<AttributeConstant> atts, ForceAttributeSchema fas,
			Map<SchemaVariable, Map<AttributeConstant, Integer>> domain) {
		return attVal.getAtt(_a).equals(_b) ? Constant.TRUE
				: Constant.FALSE;
	}

	@Override
	protected MiningExpression pushDown(Quantifier q, AttributeVariable av,
			SchemaVariable s) {
		return new AttributeQuantifier(q, av, s, this);
	}

	@Override
	public MiningExpression pushDown() {
		return this;
	}

	@Override
	public Set<AttributeVariable> getFreeAttVariables() {
		Set<AttributeVariable> s = new HashSet<AttributeVariable>();
		s.add(_a);
		return s;
	}

	@Override
	public String toString() {
		return _a.getName()+" = "+_b.getName();
	}

	@Override
	public <E> E accept(Visitor<E> v) {
		return v.attCmp(_a, _b);
	}

	@Override
	public void acceptPrefix(VoidVisitor v) {
		v.attCmp(this, _a, _b);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_a == null) ? 0 : _a.hashCode());
		result = prime * result + ((_b == null) ? 0 : _b.hashCode());
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
		AttributeVarConstantComparison other = (AttributeVarConstantComparison) obj;
		if (_a == null) {
			if (other._a != null)
				return false;
		} else if (!_a.equals(other._a))
			return false;
		if (_b == null) {
			if (other._b != null)
				return false;
		} else if (!_b.equals(other._b))
			return false;
		return true;
	}
	
	
	
}
