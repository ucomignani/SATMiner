/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.problem.rql.ast.intermediate.BFormula;
import dag.satmining.problem.rql.ast.intermediate.BNeg;
import dag.satmining.problem.rql.ast.intermediate.LiteralOrValue;
import dag.satmining.problem.rql.ast.sql.BitSetFetcher;
import dag.satmining.problem.rql.ast.sql.SQLBooleanValue;

/**
 * 
 * @author ecoquery
 */
public class SQLBinding {

	private static final Logger LOG = LoggerFactory.getLogger(SQLBinding.class);

	private final MiningExpression _mainExpression;
	private final List<AttributeConstant> _attributes;
	private final Map<SchemaVariable, Map<AttributeConstant, Integer>> _domain;
	private final List<SQLBooleanValue> _selectStatements = new ArrayList<SQLBooleanValue>();
	private final ASTDictionnary _dict;

	public SQLBinding(MiningExpression e, List<AttributeConstant> atts,
			Map<SchemaVariable, Map<AttributeConstant, Integer>> providedDomain,
			ASTDictionnary dict) {
		this._mainExpression = e;
		this._attributes = atts;
		this._domain = providedDomain;
		this._dict = dict;
		int maxAttId = 0;
		for(AttributeConstant att : atts) {
			maxAttId = Math.max(maxAttId, att.getId());
		}
		e.registerSQLExpressions(_attributes, maxAttId, this, _dict);
	}

	public MiningExpression getMainExpression() {
		return _mainExpression;
	}

	// /**
	// * A visitor to add to toEvaluate the top formalas that have no bound
	// * variables
	// */
	// private class FormulaFinder implements MiningExpression.Visitor<Object> {
	//
	// @Override
	// public Object forall(AttributeVariable av, SchemaVariable sv,
	// MiningExpression e) {
	// if (BoundVariables.getFrom(e).isEmpty()) {
	// _toEvaluate.add(e);
	// return null;
	// } else {
	// return e.accept(this);
	// }
	// }
	//
	// @Override
	// public Object exists(AttributeVariable av, SchemaVariable sv,
	// MiningExpression e) {
	// return forall(av, sv, e); // same as forall
	// }
	//
	// @Override
	// public Object eq(MiningValue a, MiningValue b) {
	// _toEvaluate.add(MiningExpression.eq(a, b));
	// return null;
	// }
	//
	// @Override
	// public Object trueV() {
	// _toEvaluate.add(MiningExpression.tt());
	// return null;
	// }
	//
	// @Override
	// public Object and(MiningExpression a, MiningExpression b) {
	// // there are bound variables somewhere ...
	// if (BoundVariables.getFrom(a).isEmpty()) {
	// _toEvaluate.add(a);
	// return b.accept(this);
	// } else if (BoundVariables.getFrom(b).isEmpty()) {
	// _toEvaluate.add(b);
	// return a.accept(this);
	// } else {
	// a.accept(this);
	// return b.accept(this);
	// }
	// }
	//
	// @Override
	// public Object or(MiningExpression a, MiningExpression b) {
	// return and(a, b); // similar to and
	// }
	//
	// @Override
	// public Object neg(MiningExpression a) {
	// return a.accept(this);
	// }
	// }
	//
	// private void findFormulas(MiningExpression e) {
	// e.accept(new FormulaFinder());
	// }
	//
	// private Map<String, String> buildMap(int exprId, int[] valuation) {
	// HashMap<String, String> res = new HashMap<String, String>();
	// for (int i = 0; i < valuation.length; i++) {
	// res.put(_freeVariables[exprId][i].getName(),
	// _attributes.get(valuation[i]).getName());
	// }
	// return res;
	// }
	//
	// private void generateStatementsFor(MiningExpression e, int exprId,
	// int[] currentValuation, int varToSet, List<SQLBooleanValue> l) {
	// if (varToSet == _freeVariables[exprId].length) {
	// // valuation is complete
	// l.add(e.generateSQLExpression(buildMap(exprId, currentValuation)));
	// } else {
	// for (int i = 0; i < _attributes.size(); i++) {
	// currentValuation[varToSet] = i;
	// generateStatementsFor(e, exprId, currentValuation,
	// varToSet + 1, l);
	// }
	// }
	// }
	//
	// public int selectOffset(int exprId,
	// Map<AttributeVariable, Integer> valuation) {
	// int[] val = new int[_freeVariables[exprId].length];
	// for (int i = 0; i < val.length; i++) {
	// val[i] = valuation.get(_freeVariables[exprId][i]);
	// }
	// return selectOffset(exprId, val);
	// }
	//
	// private int selectOffset(int exprId, int[] valuation) {
	// int dec = 0;
	// for (int i = 0; i < valuation.length; i++) {
	// dec = dec * _attributes.size();
	// dec += valuation[i];
	// }
	// dec += _exprSQLPos[exprId];
	// return dec;
	// }
	//
	// private boolean getValue(int exprId, int[] valuation, BitSet bs) {
	// LOG.debug("Getting value for {}, {}",
	// new Object[] { exprId, Arrays.toString(valuation) });
	// return bs.get(selectOffset(exprId, valuation));
	// }

	// private class FormulaGenerator implements
	// MiningExpression.Visitor<LiteralOrValue[]> {
	//
	// private SATHandler _sh;
	// private ClauseFactory _cf;
	// private Map<AttributeVariable, Integer> _currentValuation;
	// private BitSet _currentValues;
	// private final ForceAttributeSchema[] _configurations;
	//
	// public FormulaGenerator(SATHandler sh,
	// ForceAttributeSchema[] configurations) {
	// this._sh = sh;
	// this._cf = sh.getClauseFactory();
	// this._currentValuation = new HashMap<AttributeVariable, Integer>();
	// this._configurations = configurations;
	// }
	//
	// public void setCurrentValues(BitSet bs) {
	// this._currentValues = bs;
	// }
	//
	// private LiteralOrValue[] process(MiningExpression e) {
	// LiteralOrValue[] res = _cache.get(e, _currentValuation,
	// _currentValues);
	// if (res == null) {
	// res = e.accept(this);
	// _cache.put(e, _currentValuation, _currentValues, res);
	// } else {
	// _nbCacheHits++;
	// }
	// return res;
	// }
	//
	// private List<Literal>[] terminalForall(AttributeVariable av,
	// SchemaVariable sv, MiningExpression e) {
	// @SuppressWarnings("unchecked")
	// List<Literal>[] result = new List[_configurations.length];
	// for (int cfg = 0; cfg < result.length; cfg++) {
	// ForceAttributeSchema conf = _configurations[cfg];
	// if (conf.isEnabled()) {
	// List<Literal> conj = new ArrayList<Literal>();
	// boolean hasAFalse = false;
	// int exprId = getId(e);
	// int holePosition = -1;
	// int[] valuation = new int[_freeVariables[exprId].length];
	// for (int i = 0; i < _freeVariables[exprId].length; i++) {
	// if (_freeVariables[exprId][i].equals(av)) {
	// holePosition = i;
	// } else {
	// valuation[i] = _currentValuation
	// .get(_freeVariables[exprId][i]);
	// }
	// }
	// for (int i = 0; i < _attributes.size(); i++) {
	// valuation[holePosition] = i;
	// if (!getValue(exprId, valuation, _currentValues)) {
	// if (conf.match(sv, _attributes.get(i), true)) {
	// hasAFalse = true;
	// break;
	// } else {
	// conj.add(_domain[sv.getIndex()][i]
	// .getOpposite());
	// }
	// }
	// }
	// result[cfg] = hasAFalse ? null : conj;
	// }
	// }
	// return result;
	// }
	//
	// private List<Literal>[] nonTerminalForall(AttributeVariable av,
	// SchemaVariable sv, MiningExpression e)
	// throws NoSolutionException {
	// @SuppressWarnings("unchecked")
	// List<Literal>[] result = new List[_configurations.length];
	// boolean[] keep = new boolean[result.length];
	// for (int cfg = 0; cfg < result.length; cfg++) {
	// if (_configurations[cfg].isEnabled()) {
	// result[cfg] = new ArrayList<Literal>();
	// keep[cfg] = true;
	// } else {
	// keep[cfg] = false;
	// }
	// }
	// for (int i = 0; i < _attributes.size(); i++) {
	// _currentValuation.put(av, i);
	// List<ForceAttributeSchema> toReEnable = new
	// ArrayList<ForceAttributeSchema>();
	// for (ForceAttributeSchema conf : _configurations) {
	// if (conf.isEnabled()
	// && conf.match(sv, _attributes.get(i), false)) {
	// conf.disable();
	// toReEnable.add(conf);
	// }
	// }
	// LiteralOrValue[] lv = process(e);
	// for (int cfg = 0; cfg < result.length; cfg++) {
	// ForceAttributeSchema conf = _configurations[cfg];
	// if (conf.isEnabled()) {
	// if (lv[cfg]._l != null) {
	// if (conf.match(sv, _attributes.get(i), true)) {
	// result[cfg].add(lv[cfg]._l);
	// } else {
	// Literal l = _cf.newLiteral(true);
	// _sh.addEquivToClause(l,
	// _cf.newClause(_domain[sv.getIndex()][i]
	// .getOpposite(), lv[cfg]._l));
	// result[cfg].add(l);
	// }
	// } else {
	// if (!lv[cfg]._v) {
	// if (conf.match(sv, _attributes.get(i), true)) {
	// keep[cfg] = false;
	// } else {
	// result[cfg].add(_domain[sv.getIndex()][i]
	// .getOpposite());
	// }
	// }
	// }
	// }
	// }
	// for (ForceAttributeSchema conf : toReEnable) {
	// conf.enable();
	// }
	// }
	// for (int cfg = 0; cfg < result.length; cfg++) {
	// if (!keep[cfg]) {
	// result[cfg] = null;
	// }
	// }
	// return result;
	// }
	//
	// @Override
	// public LiteralOrValue[] forall(AttributeVariable av, SchemaVariable sv,
	// MiningExpression e) {
	// try {
	// LOG.debug("Generating forall {}({}) {}", new Object[] { av, sv,
	// e });
	// LiteralOrValue[] result = new LiteralOrValue[_configurations.length];
	// List<Literal>[] conjunctions;
	// if (e.getBoundAttVariables().isEmpty()) {
	// conjunctions = terminalForall(av, sv, e);
	// } else {
	// conjunctions = nonTerminalForall(av, sv, e);
	// }
	// for (int cfg = 0; cfg < result.length; cfg++) {
	// if (conjunctions[cfg] == null) {
	// result[cfg] = LiteralOrValue.FALSE;
	// } else if (conjunctions[cfg].isEmpty()) {
	// result[cfg] = LiteralOrValue.TRUE;
	// } else {
	// Literal andL = _cf.newLiteral(true);
	// _sh.addEquivToLiteralConj(_cf.newReifiedConjunction(
	// conjunctions[cfg], andL));
	// result[cfg] = new LiteralOrValue(andL);
	// }
	// }
	// return result;
	// } catch (NoSolutionException ex) {
	// throw new RuntimeException(ex);
	// }
	// }
	//
	// private List<Literal>[] terminalExists(AttributeVariable av,
	// SchemaVariable sv, MiningExpression e) {
	// @SuppressWarnings("unchecked")
	// List<Literal>[] result = new List[_configurations.length];
	// for (int cfg = 0; cfg < result.length; cfg++) {
	// ForceAttributeSchema conf = _configurations[cfg];
	// if (conf.isEnabled()) {
	// List<Literal> clause = new ArrayList<Literal>();
	// int exprId = getId(e);
	// int holePosition = -1;
	// int[] valuation = new int[_freeVariables[exprId].length];
	// for (int i = 0; i < _freeVariables[exprId].length; i++) {
	// if (_freeVariables[exprId][i].equals(av)) {
	// holePosition = i;
	// } else {
	// valuation[i] = _currentValuation
	// .get(_freeVariables[exprId][i]);
	// }
	// }
	// for (int i = 0; i < _attributes.size(); i++) {
	// valuation[holePosition] = i;
	// if (getValue(exprId, valuation, _currentValues)
	// && !(conf.match(sv, _attributes.get(i), false))) {
	// clause.add(_domain[sv.getIndex()][i]);
	// }
	// }
	// result[cfg] = clause;
	// }
	// }
	// return result;
	// }
	//
	// private List<Literal>[] nonTerminalExists(AttributeVariable av,
	// SchemaVariable sv, MiningExpression e)
	// throws NoSolutionException {
	// @SuppressWarnings("unchecked")
	// List<Literal>[] result = new List[_configurations.length];
	// boolean[] keep = new boolean[result.length];
	// for (int cfg = 0; cfg < result.length; cfg++) {
	// keep[cfg] = true;
	// if (_configurations[cfg].isEnabled()) {
	// result[cfg] = new ArrayList<Literal>();
	// }
	// }
	// for (int i = 0; i < _attributes.size(); i++) {
	// _currentValuation.put(av, i);
	// List<ForceAttributeSchema> cfgToReEnable = new
	// ArrayList<ForceAttributeSchema>();
	// for (ForceAttributeSchema conf : _configurations) {
	// if (conf.isEnabled()
	// && conf.match(sv, _attributes.get(i), false)) {
	// cfgToReEnable.add(conf);
	// conf.disable();
	// }
	// }
	// LiteralOrValue[] eL = process(e);
	// for (int cfg = 0; cfg < result.length; cfg++) {
	// if (_configurations[cfg].isEnabled()) {
	// if (eL[cfg]._l != null) {
	// if (_configurations[cfg].match(sv,
	// _attributes.get(i), true)) {
	// result[cfg].add(eL[cfg]._l);
	// } else {
	// Literal andL = _cf.newLiteral(true);
	// _sh.addEquivToLiteralConj(_cf
	// .newReifiedConjunction(new Literal[] {
	// _domain[sv.getIndex()][i],
	// eL[cfg]._l }, andL));
	// result[cfg].add(andL);
	// }
	// } else if (eL[cfg]._v) {
	// if (_configurations[cfg].match(sv,
	// _attributes.get(i), true)) {
	// keep[cfg] = false;
	// } else {
	// result[cfg].add(_domain[sv.getIndex()][i]);
	// }
	// }
	// }
	// }
	// for (ForceAttributeSchema conf : cfgToReEnable) {
	// conf.enable();
	// }
	// }
	// for (int cfg = 0; cfg < result.length; cfg++) {
	// if (!keep[cfg]) {
	// result[cfg] = null;
	// }
	// }
	// return result;
	// }
	//
	// @Override
	// public LiteralOrValue[] exists(AttributeVariable av, SchemaVariable sv,
	// MiningExpression e) {
	// try {
	// LiteralOrValue[] result = new LiteralOrValue[_configurations.length];
	// List<Literal>[] clauses;
	// if (e.getBoundAttVariables().isEmpty()) {
	// clauses = terminalExists(av, sv, e);
	// } else {
	// clauses = nonTerminalExists(av, sv, e);
	// }
	// for (int cfg = 0; cfg < clauses.length; cfg++) {
	// if (clauses[cfg] == null) {
	// result[cfg] = LiteralOrValue.TRUE;
	// } else if (clauses[cfg].isEmpty()) {
	// result[cfg] = LiteralOrValue.FALSE;
	// } else {
	// Literal eqTo = _cf.newLiteral(true);
	// _sh.addEquivToClause(eqTo,
	// _cf.newClause(false, clauses[cfg]));
	// result[cfg] = new LiteralOrValue(eqTo);
	// }
	// }
	// return result;
	// } catch (NoSolutionException ex) {
	// throw new RuntimeException(ex);
	// }
	// }
	//
	// @Override
	// public LiteralOrValue[] eq(MiningValue a, MiningValue b) {
	// LiteralOrValue[] result = new LiteralOrValue[_configurations.length];
	// for (int i = 0; i < result.length; i++) {
	// result[i] = getValue(getId(MiningExpression.eq(a, b)),
	// new int[] {}, _currentValues) ? LiteralOrValue.TRUE
	// : LiteralOrValue.FALSE;
	// }
	// return result;
	// }
	//
	// @Override
	// public LiteralOrValue[] trueV() {
	// LiteralOrValue[] result = new LiteralOrValue[_configurations.length];
	// for (int i = 0; i < result.length; i++) {
	// result[i] = LiteralOrValue.TRUE;
	// }
	// return result;
	// }
	//
	// @Override
	// public LiteralOrValue[] and(MiningExpression a, MiningExpression b) {
	// LiteralOrValue[] result = new LiteralOrValue[_configurations.length];
	// LiteralOrValue[] lvA = process(a);
	// LiteralOrValue[] lvB = process(b);
	// for (int i = 0; i < result.length; i++) {
	// if (lvA[i]._l != null) {
	// if (lvB[i]._l != null) {
	// Literal andL = _cf.newLiteral(true);
	// try {
	// _sh.addEquivToLiteralConj(_cf
	// .newReifiedConjunction(new Literal[] {
	// lvA[i]._l, lvB[i]._l }, andL));
	// } catch (NoSolutionException ex) {
	// throw new RuntimeException(ex);
	// }
	// result[i] = new LiteralOrValue(andL);
	// } else {
	// result[i] = lvB[i]._v ? lvA[i] : lvB[i];
	// }
	// } else {
	// result[i] = lvA[i]._v ? lvB[i] : lvA[i];
	// }
	// }
	// return result;
	// }
	//
	// @Override
	// public LiteralOrValue[] or(MiningExpression a, MiningExpression b) {
	// LiteralOrValue[] result = new LiteralOrValue[_configurations.length];
	// LiteralOrValue[] lvA = process(a);
	// LiteralOrValue[] lvB = process(b);
	// for (int i = 0; i < result.length; i++) {
	// if (lvA[i]._l != null) {
	// if (lvB[i]._l != null) {
	// Literal orL = _cf.newLiteral(true);
	// try {
	// _sh.addEquivToClause(orL,
	// _cf.newClause(lvA[i]._l, lvB[i]._l));
	// } catch (NoSolutionException ex) {
	// throw new RuntimeException(ex);
	// }
	// result[i] = new LiteralOrValue(orL);
	// } else {
	// result[i] = lvB[i]._v ? lvB[i] : lvA[i];
	// }
	// } else {
	// result[i] = lvA[i]._v ? lvA[i] : lvB[i];
	// }
	// }
	// return result;
	// }
	//
	// @Override
	// public LiteralOrValue[] neg(MiningExpression a) {
	// LiteralOrValue[] result = new LiteralOrValue[_configurations.length];
	// LiteralOrValue[] lvA = process(a);
	// for (int i = 0; i < result.length; i++) {
	// if (lvA[i]._l != null) {
	// result[i] = new LiteralOrValue(lvA[i]._l.getOpposite());
	// } else {
	// result[i] = lvA[i]._v ? LiteralOrValue.FALSE
	// : LiteralOrValue.TRUE;
	// }
	// }
	// return result;
	// }
	// }

	private class CacheEnabler extends AbstractMiningExpressionVisitor {

		@Override
		public void exists(MiningExpression e, AttributeVariable av,
				SchemaVariable sv, MiningExpression a) {
			e.enableCache(true);
		}

		@Override
		public void forall(MiningExpression e, AttributeVariable av,
				SchemaVariable sv, MiningExpression a) {
			e.enableCache(true);
		}
		
	}
	
	public <L extends Literal<L>> void runEvaluation(PBBuilder<L> handler, BitSetFetcher bsr,
			List<SchemaVariable> toMinimize, List<SchemaVariable> toMaximize) throws NoSolutionException, SQLException,
			IOException {
		LOG.debug("Enabling cache in main expression and quantifiers");
		_mainExpression.acceptPrefix(new CacheEnabler());
		_mainExpression.enableCache(true);
		LOG.debug("Generating intermediate formulas");
		int nbConfigs = 1 + _attributes.size()
				* (toMinimize.size() + toMaximize.size());
		ForceAttributeSchema[] minMaxCfg = new ForceAttributeSchema[nbConfigs];
		minMaxCfg[0] = new ForceAttributeSchema(null, null, true);
		int cfg = 0;
		for (SchemaVariable sv : toMinimize) {
			for (AttributeConstant att : _attributes) {
				minMaxCfg[++cfg] = new ForceAttributeSchema(att, sv, false);
			}
		}
		for (SchemaVariable sv : toMaximize) {
			for (AttributeConstant att : _attributes) {
				minMaxCfg[++cfg] = new ForceAttributeSchema(att, sv, true);
			}
		}
		BFormula[] intermediateFormulas = new BFormula[nbConfigs];
		@SuppressWarnings("unchecked")
		Set<LiteralOrValue>[] minMaxLitSets = new Set[nbConfigs];
		for (cfg = 0; cfg < nbConfigs; cfg++) {
			minMaxLitSets[cfg] = new HashSet<LiteralOrValue>();
			ForceAttributeSchema fas = minMaxCfg[cfg];
			intermediateFormulas[cfg] = _mainExpression
					.getIntermediateFormula(new AttributeValuation(_dict),
							_attributes, fas, _domain);
			if (cfg > 0) {
				intermediateFormulas[cfg] = new BNeg(intermediateFormulas[cfg]);
			}
		}
		LOG.debug("Running query for generating SAT problem ...");
		int nbTupleComb = 0;
		BFormula.cacheHits = 0;
		int nbCachedTuples = 0;
		while (bsr.next()) { // for all tuple combination
			BitSet data = bsr.getBitSet();
			nbTupleComb++;
			int oldCacheHits = BFormula.cacheHits;
			for (cfg = 0; cfg < nbConfigs; cfg++) {
				minMaxLitSets[cfg].add(intermediateFormulas[cfg]
						.getRepresentation(handler, data));
			}
			if (oldCacheHits != BFormula.cacheHits) {
				nbCachedTuples++;
			}
		}
		LOG.info(
				"Generated formula from {} tuple combinations, {} cache hits ({} full hits) on {} combinations",
				new Object[] { nbTupleComb, BFormula.cacheHits, intermediateFormulas[0].getCacheHits(), nbCachedTuples });
		if (minMaxLitSets[0].contains(LiteralOrValue.FALSE)) {
			throw new NoSolutionException(
					"Formula is always false for some tuple combinaition");
		} else {
			minMaxLitSets[0].remove(LiteralOrValue.TRUE);
			for (LiteralOrValue lv : minMaxLitSets[0]) {
				handler.addClause(lv.getLiteral(handler));
			}
		}
		for (cfg = 1; cfg < nbConfigs; cfg++) {
			ForceAttributeSchema fas = minMaxCfg[cfg];
			Set<LiteralOrValue> lvs = minMaxLitSets[cfg];
			lvs.remove(LiteralOrValue.FALSE);
			L repr = handler.fromDimacs(_domain.get(fas.getSchemaVariable()).get(
					fas.getAttribute()));
			if (lvs.contains(LiteralOrValue.TRUE)) {
				// nothing to do, the implication is trivially satisfied
			} else {
				Collection<L> clause = new ArrayList<L>();
				clause.add(fas.isInSet() ? repr : repr.getOpposite());
				for (LiteralOrValue lv : lvs) {
					clause.add(lv.getLiteral(handler));
				}
				handler.addClause(clause);
			}
		}
	}

	public List<SQLBooleanValue> getSelectStatements() {
		return _selectStatements;
	}

	public int registerSQLStatement(SQLBooleanValue statement) {
		int idx = _selectStatements.size();
		_selectStatements.add(statement);
		return idx;
	}
}
