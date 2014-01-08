/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

/**
 *
 * @author ecoquery
 */
public class SchemaVariable {

    private final String _name;
    private final int _index;

    SchemaVariable(String name, int index) {
        this._name = name;
        this._index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchemaVariable other = (SchemaVariable) obj;
        if ((this._name == null) ? (other._name != null) : !this._name.equals(other._name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + _index;
        return hash;
    }

    public String getName() {
        return _name;
    }
    
    @Override
    public String toString() {
    	return _name;
    }
    
    public int getIndex() {
    	return _index;
    }
}
