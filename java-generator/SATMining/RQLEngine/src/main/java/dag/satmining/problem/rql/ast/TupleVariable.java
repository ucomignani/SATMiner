/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

/**
 *
 * @author ecoquery
 */
public final class TupleVariable {
    private final String _name;
    private final int _id;

    TupleVariable(String name, int id) {
        this._name = name;
        this._id = id;
    }

    public String getName() {
        return _name;
    }
    
    public int getId() {
    	return _id;
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TupleVariable other = (TupleVariable) obj;
		if (_id != other._id)
			return false;
		return true;
	}

	@Override
    public String toString() {
        return getName();
    }
}
