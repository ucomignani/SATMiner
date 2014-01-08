package dag.satmining.problem.rql.ast.sql;

import java.util.Map;

public final class Tuple {
	
	private Map<String, Integer> _schema;
	private String[] _values;
	
	public Tuple(Map<String, Integer> schema) {
		this._schema = schema;
		this._values = new String[schema.size()];
	}
	
	public void setValue(String key, String value) {
		_values[_schema.get(key)] = value;
	}
	
	public void setValue(int idx, String value) {
		_values[idx] = value;
	}
	
	public String getValue(String key) {
		return _values[_schema.get(key)];
	}
	
	public String getValue(int idx) {
		return _values[idx];
	}
		
}
