package dag.satmining.problem.satql.ast.sql;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.problem.satql.ast.MiningExpressionTest;

public class NamedFromQuantifiedExpression implements QuantifierExpression, FromExpression {

	
	private static final Logger LOG = LoggerFactory
			.getLogger(MiningExpressionTest.class);
	
	private final ArrayList<String> _namesNUplet;
	private final QuantifierExpression _quant;
	private final FromExpression _expr;
	private final List<NamedFromExpression> _queries;
	private final FromExpression _filter;
	private final boolean _isFirstQuantifier;
	private final int _nQuantifierValue; // -1 if universal quantifier
	private final boolean _isPercentQuantifier;

	public NamedFromQuantifiedExpression(ArrayList<String> namesNUplet, 
			QuantifierExpression quant, List<NamedFromExpression> queries, FromExpression filter, 
			boolean isFirstQuantifier, int nQuantifierValue, boolean isPercentQuantifier) {		
		this._namesNUplet = namesNUplet;	    	
		this._quant = quant;
		this._expr = extractQuery(_namesNUplet.get(0), queries);
		this._queries = queries;
		this._filter = filter;
		this._isFirstQuantifier = isFirstQuantifier;
		this._nQuantifierValue = nQuantifierValue;
		this._isPercentQuantifier = isPercentQuantifier;

		if (_expr == null) {
			throw new IllegalArgumentException("No SCOPE for " + this._namesNUplet.get(0));
		} 
	}

	private NamedFromExpression extractQuery(String name, List<NamedFromExpression> _queries)
	{
		for (NamedFromExpression expr : _queries){
			if(expr.getName().equals(name)) {
				return expr;
			}
		}
		return null;
	}

	public QuantifierExpression getQuant() {
		return _quant;
	}

	public FromExpression getExpr() {
		return _expr;
	}

	public FromExpression getFilter() {
		return _filter;
	}

	public ArrayList<String> getNamesNUplet() {
		return _namesNUplet;
	}

	public int getNQuantifierValue(){
		return _nQuantifierValue; 
	}
	public boolean getIsPercentQuantifier(){
		return _isPercentQuantifier;
	}

	@Override
	public void buildSQLQueryNoName(StringBuilder output) {
		_quant.buildSQLQueryNoName(output);
	}

	@Override
	public void buildSQLQuery(StringBuilder output) {

	}

	@Override
	public void buildSQLQuery(StringBuilder output, StringBuilder filter) {

		if(this._isFirstQuantifier){ //FROM

			if(_namesNUplet.size() == 1)
				buildFromSimpleSQLQuery(output, filter);
			else 	    		
				buildFromNUpletSQLQuery(output, filter);

		} else { //LEFT OUTER JOIN
   		
			buildLeftOuterJoinNUpletSQLQuery(output, filter);	
		}
		filter.setLength(0);
		_filter.buildSQLQueryNoName(filter);
	}

	public void buildFromSimpleSQLQuery(StringBuilder output, StringBuilder filter) {

		output.append("(select");

		if (_namesNUplet.get(0) != null) {
			output.append(" " + _namesNUplet.get(0) + ".");
		}

		output.append("*, ROW_NUMBER() OVER() as row_num from ");
		_expr.buildSQLQuery(output);
		output.append(" where ");
		_quant.buildSQLQueryNoName(output);
		output.append(")");

		if (_namesNUplet.get(0) != null) {
			output.append(" ");
			output.append(_namesNUplet.get(0));
		}
	}

	public void buildFromNUpletSQLQuery(StringBuilder output, StringBuilder filter) {

		FromExpression exprNUplet;
		String name;

		output.append("((select");

		if (_namesNUplet.get(0) != null) {
			output.append(" ");
			output.append(_namesNUplet.get(0));
			output.append(".");
		}
		output.append("*, ROW_NUMBER() OVER() as row_num from ");
		_expr.buildSQLQuery(output);


		output.append(")");

		if (_namesNUplet.get(0) != null) {
			output.append(" ");
			output.append(_namesNUplet.get(0));
		}

		for(int i=1; i<_namesNUplet.size()-1;i++){ //pour les n-1 premieres variables donc avec ON (0=0)
			name = _namesNUplet.get(i);
			output.append(" CROSS JOIN (select " + name + ".*, ROW_NUMBER() OVER() as row_num from ");
			exprNUplet = extractQuery(name, _queries);	  
			exprNUplet.buildSQLQuery(output);
			output.append(") " + name);
		}

		//dernière variable: ON avec la contrainte
		name = _namesNUplet.get(_namesNUplet.size()-1);
		output.append(" INNER JOIN (select " + name + ".*, ROW_NUMBER() OVER() as row_num from ");
		exprNUplet = extractQuery(name, _queries);	  
		exprNUplet.buildSQLQuery(output);
		output.append(") " + name + " ON ");
		_quant.buildSQLQueryNoName(output);
		output.append(")");

	}

	public void buildLeftOuterJoinNUpletSQLQuery(StringBuilder output, StringBuilder filter) {

		FromExpression exprNUplet;

		output.append("((select");

		if (_namesNUplet.get(0) != null) {
			output.append(" " + _namesNUplet.get(0) + ".");
		}

		output.append("*, ROW_NUMBER() OVER() as row_num from ");
		_expr.buildSQLQuery(output);
		output.append(")");


		if (_namesNUplet.get(0) != null) {
			output.append(" " + _namesNUplet.get(0));
		}


		for(int i=1; i<_namesNUplet.size();i++){
			output.append(" CROSS JOIN (select " + _namesNUplet.get(i) + ".*, ROW_NUMBER() OVER() as row_num from ");
			exprNUplet = extractQuery(_namesNUplet.get(i), _queries);	  
			exprNUplet.buildSQLQuery(output);
			output.append(") " + _namesNUplet.get(i));
		}

		output.append(") ON (");
		_quant.buildSQLQueryNoName(output);
		output.append(" AND " + filter);
		output.append(")");
	}
}