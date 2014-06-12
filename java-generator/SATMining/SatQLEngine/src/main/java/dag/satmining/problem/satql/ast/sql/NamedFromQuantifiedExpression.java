package dag.satmining.problem.satql.ast.sql;

import java.util.List;

public class NamedFromQuantifiedExpression implements QuantifierExpression, FromExpression {

	private final String _name;
	private final String _nameCouple;
	private final QuantifierExpression _quant;
	private final FromExpression _expr;
	private final FromExpression _exprCouple;	    
	private final FromExpression _filter;
	private final boolean _isFirstQuantifier;
	private final int _nQuantifierValue; // -1 if universal quantifier
	private final boolean _isPercentQuantifier;

	public NamedFromQuantifiedExpression(String name, String nameCouple, 
			QuantifierExpression quant, List<NamedFromExpression> _queries, FromExpression filter, 
			boolean isFirstQuantifier, int nQuantifierValue, boolean isPercentQuantifier) {
		this._name = name;
		this._nameCouple = nameCouple;	    	
		this._quant = quant;
		this._expr = extractQuery(name, _queries);

		if(_nameCouple != null){
			this._exprCouple = extractQuery(_nameCouple, _queries);	        	
		}else{
			this._exprCouple = null;
		}

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
	
	public String getNameCouple() {
		return _nameCouple;
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

			if(_nameCouple == null){

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

			} else {//gestion des couples de variables de tuples	    		
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

				output.append(" INNER JOIN (select " + _nameCouple + ".*, ROW_NUMBER() OVER() as row_num from ");
				_exprCouple.buildSQLQuery(output);
				output.append(")" + _nameCouple);

				output.append(" ON ");
				_quant.buildSQLQueryNoName(output);
				output.append(")");

			}

		} else { //LEFT OUTER JOIN

			if(_nameCouple == null){
				
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

			} else {//gestion des couples de variables de tuples	    		
					
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

				output.append(" CROSS JOIN (select " + _nameCouple + ".*, ROW_NUMBER() OVER() as row_num from ");
				_exprCouple.buildSQLQuery(output);
				output.append(") " + _nameCouple + ")");

				output.append(" ON (");
				_quant.buildSQLQueryNoName(output);
				output.append(" AND " + filter);
				output.append(")");
			}

		}
		filter.setLength(0);
		_filter.buildSQLQueryNoName(filter);

	}
}