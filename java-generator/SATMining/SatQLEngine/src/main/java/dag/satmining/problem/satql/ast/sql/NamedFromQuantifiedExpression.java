package dag.satmining.problem.satql.ast.sql;

import java.util.ArrayList;
import java.util.List;

public class NamedFromQuantifiedExpression implements QuantifierExpression, FromExpression {

	private final String _name;
	private final ArrayList<String> _namesNUplet;
	private final QuantifierExpression _quant;
	private final FromExpression _expr;
	private final List<NamedFromExpression> _queries;
	private final FromExpression _filter;
	private final boolean _isFirstQuantifier;
	private final int _nQuantifierValue; // -1 if universal quantifier
	private final boolean _isPercentQuantifier;

	public NamedFromQuantifiedExpression(String name, ArrayList<String> namesNUplet, 
			QuantifierExpression quant, List<NamedFromExpression> queries, FromExpression filter, 
			boolean isFirstQuantifier, int nQuantifierValue, boolean isPercentQuantifier) {
		this._name = name;
		this._namesNUplet = namesNUplet;	    	
		this._quant = quant;
		this._expr = extractQuery(name, queries);
		this._queries = queries;
		this._filter = filter;
		this._isFirstQuantifier = isFirstQuantifier;
		this._nQuantifierValue = nQuantifierValue;
		this._isPercentQuantifier = isPercentQuantifier;

		if (_expr == null) {
			throw new IllegalArgumentException("No SCOPE for " + this._name);
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

	public String getName() {
		return _name;
	}

	public ArrayList<String> getnamesNUplet() {
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

			if(_namesNUplet == null)
				buildFromSimpleSQLQuery(output, filter);
			else 	    		
				buildFromNUpletSQLQuery(output, filter);

		} else { //LEFT OUTER JOIN
			
			if(_namesNUplet == null)
				buildLeftOuterJoinSimpleSQLQuery(output, filter);				
			else 	    		
				buildLeftOuterJoinNUpletSQLQuery(output, filter);	

		}
		filter.setLength(0);
		_filter.buildSQLQueryNoName(filter);
	}

	public void buildFromSimpleSQLQuery(StringBuilder output, StringBuilder filter) {

		output.append("(select");

		if (_name != null) {
			output.append(" " + _name + ".");
		}

		output.append("*, ROW_NUMBER() OVER() as row_num from ");
		_expr.buildSQLQuery(output);
		output.append(" where ");
		_quant.buildSQLQueryNoName(output);
		output.append(")");

		if (_name != null) {
			output.append(" ");
			output.append(_name);
		}
	}

	public void buildFromNUpletSQLQuery(StringBuilder output, StringBuilder filter) {

		FromExpression exprNUplet;
		String name;

		output.append("((select");

		if (_name != null) {
			output.append(" ");
			output.append(_name);
			output.append(".");
		}
		output.append("*, ROW_NUMBER() OVER() as row_num from ");
		_expr.buildSQLQuery(output);


		output.append(")");

		if (_name != null) {
			output.append(" ");
			output.append(_name);
		}

		for(int i=0; i<_namesNUplet.size()-1;i++){ //pour les n-1 premieres variables donc avec ON (0=0)
			name = _namesNUplet.get(i);
			output.append(" INNER JOIN (select " + name + ".*, ROW_NUMBER() OVER() as row_num from ");
			exprNUplet = extractQuery(name, _queries);	  
			exprNUplet.buildSQLQuery(output);
			output.append(") " + name + " ON (0=0)");
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

	public void buildLeftOuterJoinSimpleSQLQuery(StringBuilder output, StringBuilder filter) {

		output.append("(select");

		if (_name != null) {
			output.append(" ");
			output.append(_name);
			output.append(".");
		}

		output.append("*, ROW_NUMBER() OVER() as row_num from ");
		_expr.buildSQLQuery(output);
		output.append(")");


		if (_name != null) {
			output.append(" ");
			output.append(_name);
		}

		output.append(" ON (");
		_quant.buildSQLQueryNoName(output);
		output.append(" AND " + filter);
		output.append(")");
	}

	public void buildLeftOuterJoinNUpletSQLQuery(StringBuilder output, StringBuilder filter) {

		FromExpression exprNUplet;

		output.append("((select");

		if (_name != null) {
			output.append(" " + _name + ".");
		}

		output.append("*, ROW_NUMBER() OVER() as row_num from ");
		_expr.buildSQLQuery(output);
		output.append(")");


		if (_name != null) {
			output.append(" " + _name);
		}


		for(String nameCrossJoin: _namesNUplet){
			output.append(" CROSS JOIN (select " + nameCrossJoin + ".*, ROW_NUMBER() OVER() as row_num from ");
			exprNUplet = extractQuery(nameCrossJoin, _queries);	  
			exprNUplet.buildSQLQuery(output);
			output.append(") " + nameCrossJoin + ")");
		}

		output.append(" ON (");
		_quant.buildSQLQueryNoName(output);
		output.append(" AND " + filter);
		output.append(")");
	}
}