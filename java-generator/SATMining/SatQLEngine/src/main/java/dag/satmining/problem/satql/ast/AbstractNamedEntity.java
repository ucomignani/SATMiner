package dag.satmining.problem.satql.ast;

public abstract class AbstractNamedEntity {

    private final String _name;
    private final int _id;

    public AbstractNamedEntity(String name, int id) {
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
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
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
        AbstractNamedEntity other = (AbstractNamedEntity) obj;
        if (_id != other._id)
            return false;
        if (_name == null) {
            if (other._name != null)
                return false;
        } else if (!_name.equals(other._name))
            return false;
        return true;
    }
}
