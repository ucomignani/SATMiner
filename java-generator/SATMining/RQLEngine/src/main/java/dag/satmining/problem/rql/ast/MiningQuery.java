/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.Interpretation;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.output.PatternConverter;
import dag.satmining.problem.rql.ast.sql.BitSetFetcher;
import dag.satmining.problem.rql.ast.sql.From;
import dag.satmining.problem.rql.ast.sql.FromExpression;
import dag.satmining.problem.rql.ast.sql.SQLBooleanValue;
import dag.satmining.problem.rql.ast.sql.SQLTrue;
import dag.satmining.problem.rql.ast.sql.Where;
import dag.satmining.problem.rql.parser.ParseException;
import dag.satmining.problem.rql.parser.RQLParser;

/**
 * 
 * @author ecoquery
 */
public class MiningQuery<L extends Literal<L>> implements Constraint<L>, PatternConverter {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory
			.getLogger(MiningQuery.class);
	private ASTDictionnary _dict = new ASTDictionnary();
	private List<AttributeConstant> _attributes = new ArrayList<AttributeConstant>();
	private From _from = new From(true);
	private Where _where = new Where(SQLTrue.instance());
	private MiningExpression _suchThat;
	// private Connection _connection;
	private L[][] _domain;
	private Map<SchemaVariable, Map<AttributeConstant, Integer>> _domainMap;
	private BitSetFetcher _bsr;
	private List<SchemaVariable> _toMinimize = new ArrayList<SchemaVariable>();
	private List<SchemaVariable> _toMaximize = new ArrayList<SchemaVariable>();

	public List<AttributeConstant> getAttributes() {
		return _attributes;
	}

	public ASTDictionnary getDict() {
		return _dict;
	}

	public From getFrom() {
		return _from;
	}

	public List<SchemaVariable> getSchemaVariables() {
		return _dict.getSchemaVariables();
	}

	public MiningExpression getSuchThat() {
		return _suchThat;
	}

	public Where getWhere() {
		return _where;
	}

	public void setWhere(Where where) {
		this._where = where;
	}

	public void addQueryName(FromExpression query, String name) {
		_from.addQueryName(query, name);
	}

	public void addName(String name) {
		_from.addName(name);
	}

	public void addSchemaVariable(String name) {
		if (_dict.hasSchemaVariable(name)) {
			throw new IllegalArgumentException(name + " was already declared");
		} else {
			_dict.addSchemaVariable(name);
		}
	}

	public void addAttribute(String name) {
		if (_dict.hasAttribute(name)) {
			throw new IllegalArgumentException(name + " was already declared");
		}
		_attributes.add(_dict.getAttributeConstant(name));
	}

	public void setSuchThat(MiningExpression suchThat) {
		this._suchThat = suchThat;
	}

	public void setBitSetFetcher(BitSetFetcher bsr) {
		this._bsr = bsr;
	}

	public void minimize(String sv) {
		_toMinimize.add(_dict.getSchemaVariable(sv));
	}

	public void maximize(String sv) {
		_toMaximize.add(_dict.getSchemaVariable(sv));
	}

	public void singleton(String sv) {
		_suchThat = _dict.and(_suchThat, Sugar.singleton(_dict, sv));
	}

	@Override
	public void addClauses(PBBuilder<L> satHandler) throws NoSolutionException {
		try {
			buildDomain(satHandler);
			_suchThat = _suchThat.pushDown();
			SQLBinding sqlBinding = new SQLBinding(_suchThat, _attributes,
					getDomainMap(), _dict);
			List<SQLBooleanValue> subFormulasToEvaluate = sqlBinding
					.getSelectStatements();
			_bsr.setSelect(subFormulasToEvaluate);
			_bsr.setFrom(_from);
			_bsr.setWhere(_where);
			sqlBinding
					.runEvaluation(satHandler, _bsr, _toMinimize, _toMaximize);
		} catch (SQLException ex) {
			throw new NoSolutionException(ex);
		} catch (IOException e) {
			throw new NoSolutionException(e);
		}
	}

	private void buildDomain(PBBuilder<L> satHandler) throws NoSolutionException {
		_domain = satHandler.lMatrix(getSchemaVariables().size(),_attributes.size());
		for (int sv = 0; sv < _domain.length; sv++) {
			for (int att = 0; att < _attributes.size(); att++) {
				_domain[sv][att] = satHandler.newStrongLiteral();
			}
			// non empty sets only
			satHandler.addClause(_domain[sv]);
		}
	}

	private Map<SchemaVariable, Map<AttributeConstant, Integer>> getMapFromDomain() {
		Map<SchemaVariable, Map<AttributeConstant, Integer>> result = new HashMap<SchemaVariable, Map<AttributeConstant, Integer>>();
		for (int sv = 0; sv < _domain.length; sv++) {
			Map<AttributeConstant, Integer> litMap = new HashMap<AttributeConstant, Integer>();
			result.put(getSchemaVariables().get(sv), litMap);
			for (int att = 0; att < _attributes.size(); att++) {
				litMap.put(_attributes.get(att), _domain[sv][att].toDimacs());
			}
		}
		return result;
	}

	public Map<SchemaVariable, Map<AttributeConstant, Integer>> getDomainMap() {
		if (_domainMap == null) {
			_domainMap = getMapFromDomain();
		}
		return _domainMap;
	}

	public L[][] getDomain() {
		return _domain;
	}

	@Override
	public CharSequence getPattern(Interpretation model) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < _domain.length; i++) {
			if (i > 0) {
				sb.append("; ");
			}
			sb.append(getSchemaVariables().get(i).getName());
			sb.append(" = {");
			addVariablePattern(model, _domain[i], sb);
			sb.append("}");
		}
		return sb;
	}

	private void addVariablePattern(Interpretation model, L[] lits,
			StringBuilder sb) {
		boolean first = true;
		for (int i = 0; i < _attributes.size(); i++) {
			if (model.getValue(lits[i])) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(_attributes.get(i).getName());
			}
		}
	}

	public static <L extends Literal<L>> MiningQuery<L> parse(Class<L> clazz, Reader input) throws ParseException {
		return new RQLParser<L>(input).MiningQuery();
	}
}
