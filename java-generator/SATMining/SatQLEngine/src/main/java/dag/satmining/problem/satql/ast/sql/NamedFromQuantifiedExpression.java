package dag.satmining.problem.satql.ast.sql;

import java.util.List;

public class NamedFromQuantifiedExpression implements FromExpression {
	
	    private final String _name;
	    private final FromExpression _quant;
	    private final FromExpression _expr;
	    private final FromExpression _filter;
	    private final boolean _isFirstQuantifier; //permet de gérer un WHERE sur le premier quantificateur
	    
	    public NamedFromQuantifiedExpression(String name, FromExpression quant, List<NamedFromExpression> _queries, FromExpression filter, boolean isFirstQuantifier) {
	    	this._name = name;
	        this._quant = quant;
	        this._expr = extractQuery(name, _queries);
	        this._filter = filter;
	        this._isFirstQuantifier = isFirstQuantifier;
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

	    public FromExpression getQuant() {
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
	    

	    @Override
	    public void buildSQLQueryNoName(StringBuilder output) {
	        _quant.buildSQLQueryNoName(output);
	    }

	    @Override
	    public void buildSQLQuery(StringBuilder output) {
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
	            output.append(" ON ");
	            _quant.buildSQLQueryNoName(output);
	    	}
	    }

}
