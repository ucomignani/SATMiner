package dag.satmining.problem.satql.ast.sql;

import java.util.List;

public class NamedFromQuantifiedExpression implements QuantifierExpression, FromExpression {
	
	    private final String _name;
	    private final QuantifierExpression _quant;
	    private final FromExpression _expr;
	    private final FromExpression _filter;
	    private final boolean _isFirstQuantifier;
	    private final int _nQuantifierValue; // -1 if universal quantifier
	    private final boolean _isPercentQuantifier;
	    
	    public NamedFromQuantifiedExpression(String name, QuantifierExpression quant, List<NamedFromExpression> _queries, FromExpression filter, boolean isFirstQuantifier, int nQuantifierValue, boolean isPercentQuantifier) {
	    	this._name = name;
	        this._quant = quant;
	        this._expr = extractQuery(name, _queries);
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
	    		if(expr.getName().equals(this._name)) {
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
	    		output.append("(select");
	    		
	    		if (_name != null) {
		            output.append(" ");
		            output.append(_name);
		            output.append(".");
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
	    	} else { //LEFT OUTER JOIN
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
	    	filter.setLength(0);
	    	_filter.buildSQLQueryNoName(filter);

	    }

}
