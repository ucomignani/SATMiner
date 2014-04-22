package dag.satmining.problem.satql.ast;

public abstract class AttributeEntity extends AbstractNamedEntity {

    public AttributeEntity(String name, int id) {
        super(name, id);
    }

    public abstract AttributeConstant getValue(AttributeValuation valuation);
 
    public abstract boolean isConstant();
    
}
