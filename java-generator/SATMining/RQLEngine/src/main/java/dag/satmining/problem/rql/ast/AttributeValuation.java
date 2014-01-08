package dag.satmining.problem.rql.ast;

public final class AttributeValuation {

	private AttributeConstant [] _data;
	
	public AttributeValuation(ASTDictionnary dict) {
		this._data = new AttributeConstant[dict.getAttributeVariableCount()];		
	}
	
	public void set(AttributeVariable var, AttributeConstant val) {
		_data[var.getId()] = val;
	}
	
	public AttributeConstant getAtt(AttributeVariable var) {
		return _data[var.getId()];
	}
	
	public int getInt(AttributeVariable var) {
		AttributeConstant c = getAtt(var);
		return c == null ? -1 : c.getId();
	}
	
}
