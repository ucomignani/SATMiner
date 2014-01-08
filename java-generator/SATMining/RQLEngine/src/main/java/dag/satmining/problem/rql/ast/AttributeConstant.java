/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

/**
 *
 * @author ecoquery
 */
public final class AttributeConstant {

    private final String _att;
    private final int _id;

    AttributeConstant(String att, int id) {
        this._att = att;
        this._id = id;
    }

    public String getName() {
        return _att;
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
		AttributeConstant other = (AttributeConstant) obj;
		if (_id != other._id)
			return false;
		return true;
	}
    
}
