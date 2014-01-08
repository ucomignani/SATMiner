/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dag.satmining.problem.rql.ast.intermediate.AtomicHolder;
import dag.satmining.problem.rql.ast.intermediate.BFormula;
import dag.satmining.problem.rql.ast.sql.Comparison;
import dag.satmining.problem.rql.ast.sql.SQLBooleanValue;
import dag.satmining.problem.rql.ast.sql.SQLValue;
import dag.satmining.utils.BoundedIntPrefixTree;

/**
 * 
 * @author ecoquery
 */
public final class BinOpAtom extends AtomicMiningExpression {

	private final Op _op;
	private final MiningValue _a;
	private final MiningValue _b;
	private final AttributeVariable[] _variables;
	private BoundedIntPrefixTree<AtomicHolder> _intermediateLiterals;

	private BinOpAtom(MiningValue a, MiningValue b, Op op) {
		this._a = a;
		this._b = b;
		this._op = op;
		Set<AttributeVariable> var = new HashSet<AttributeVariable>();
		var.addAll(a.getAttributeVariables());
		var.addAll(b.getAttributeVariables());
		this._variables = var.toArray(new AttributeVariable[var.size()]);
	}

	@Override
	public MiningExpression pushDown(Quantifier q, AttributeVariable av,
			SchemaVariable s) {
		if (_a.getAttributeVariables().contains(av)
				|| _b.getAttributeVariables().contains(av)) {
			return new AttributeQuantifier(q, av, s, this);
		} else {
			return this;
		}
	}

	@Override
	public MiningExpression pushDown() {
		return this;
	}

	@Override
	public Set<AttributeVariable> getFreeAttVariables() {
		Set<AttributeVariable> s = _a.getAttributeVariables();
		s.addAll(_b.getAttributeVariables());
		return s;
	}

	@Override
	public String toString() {
		return _op.applyTo(_a.toString(), _b.toString());
	}

	@Override
	public <E> E accept(Visitor<E> v) {
		return v.eq(_a, _b);
	}

	public enum Op {

		Eq;

		public String applyTo(String fst, String snd) {
			switch (this) {
			case Eq:
				return fst + " = " + snd;
			}
			return null;
		}
	}

	public static BinOpAtom eq(MiningValue a, MiningValue b) {
		return new BinOpAtom(a, b, Op.Eq);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BinOpAtom other = (BinOpAtom) obj;
		if (this._op != other._op) {
			return false;
		}
		if (this._a != other._a
				&& (this._a == null || !this._a.equals(other._a))) {
			return false;
		}
		if (this._b != other._b
				&& (this._b == null || !this._b.equals(other._b))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + (this._op != null ? this._op.hashCode() : 0);
		hash = 29 * hash + (this._a != null ? this._a.hashCode() : 0);
		hash = 29 * hash + (this._b != null ? this._b.hashCode() : 0);
		return hash;
	}

	@Override
	public void acceptPrefix(VoidVisitor v) {
		switch (_op) {
		case Eq:
			v.eq(this, _a, _b);
			break;
		}
	}

	@Override
	void registerSQLExpressions(Collection<AttributeConstant> atts,
			int maxAttId, SQLBinding binding, ASTDictionnary dict) {
		if (_intermediateLiterals == null) {
			_intermediateLiterals = new BoundedIntPrefixTree<AtomicHolder>(0,
					maxAttId + 1);
		}
		registerSQLExpressionsAndBuildBFormulas(atts, binding, 0,
				new int[_variables.length], new AttributeValuation(dict));
	}

	private void registerSQLExpressionsAndBuildBFormulas(
			Collection<AttributeConstant> atts, SQLBinding binding, int i,
			int[] valuation, AttributeValuation attVal) {
		if (i < _variables.length) {
			for (AttributeConstant att : atts) {
				valuation[i] = att.getId();
				attVal.set(_variables[i], att);
				registerSQLExpressionsAndBuildBFormulas(atts, binding, i+1, valuation, attVal);
			}
		} else {
			SQLValue a = _a.generateSQLExpression(attVal);
			SQLValue b = _b.generateSQLExpression(attVal);
			SQLBooleanValue cmp = null;
			switch (_op) {
			case Eq:
				cmp = Comparison.eq(a, b);
			}
			int exprIdx = binding.registerSQLStatement(cmp);
			AtomicHolder h = new AtomicHolder(exprIdx);
			_intermediateLiterals.put(valuation, h);
		}
	}

	@Override
	public BFormula buildIntermediateFormula(AttributeValuation attVal,
			Collection<AttributeConstant> atts, ForceAttributeSchema fas,
			Map<SchemaVariable, Map<AttributeConstant, Integer>> domain) {
		int[] interp = new int[_variables.length];
		for (int i = 0; i < interp.length; i++) {
			interp[i] = attVal.getInt(_variables[i]);
		}
		return _intermediateLiterals.get(interp);
	}
}
