package dag.satmining.problem.rql.ast;


public final class ForceAttributeSchema {
	private final AttributeConstant _attribute;
	private final SchemaVariable _schemaVar;
	private final boolean _inSet;
	private int _attributeId;
	private boolean _enabled = true;

	public ForceAttributeSchema(AttributeConstant attribute,
			SchemaVariable schemaVar, boolean inSet) {
		this._attribute = attribute;
		this._schemaVar = schemaVar;
		this._inSet = inSet;
	}

	public AttributeConstant getAttribute() {
		return _attribute;
	}

	public SchemaVariable getSchemaVariable() {
		return _schemaVar;
	}

	public boolean isInSet() {
		return _inSet;
	}

	public void setAttributeId(int id) {
		this._attributeId = id;
	}

	public int getAttributeId() {
		return _attributeId;
	}

	public boolean isEnabled() {
		return _enabled;
	}

	public void disable() {
		this._enabled = false;
	}

	public void enable() {
		this._enabled = true;
	}

	public boolean match(SchemaVariable sv, AttributeConstant at, boolean inSet) {
		return sv.equals(_schemaVar) && at.equals(_attribute)
				&& (inSet == _inSet);
	}
}
