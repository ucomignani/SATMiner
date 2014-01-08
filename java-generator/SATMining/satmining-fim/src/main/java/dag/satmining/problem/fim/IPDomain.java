/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.fim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.Interpretation;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.output.PatternConverter;

/**
 *
 * @author ecoquery
 */
public class IPDomain<L extends Literal<L>> implements PatternConverter, Constraint<L> {

    private static final Logger LOG = LoggerFactory.getLogger(IPDomain.class);
    
    private L[] _items;
    private int _nbItems;
    
    public IPDomain(int nbItems) {
        this._nbItems = nbItems;
    }
    
	private void initVariablesArrays(PBBuilder<L> h) throws NoSolutionException {
        LOG.debug("entering initVariablesArrays");
        _items = h.lArray(_nbItems);
        for(int i = 0; i < _nbItems; i++) {
            _items[i] = h.newStrongLiteral();
        }
        // The domain might be empty so no clause here.
        //h.addClause(_items);
    }
    
    @Override
    public void addClauses(PBBuilder<L> satHandler) throws NoSolutionException {
        initVariablesArrays(satHandler);
    }

    public CharSequence getPattern(Interpretation model) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < _items.length; i++) {
            if (model.getValue(_items[i])) {
                sb.append(i);
                sb.append(' ');
            }
        }
        return sb;
    }
    
    public L getItLit(int id) {
        return _items[id];
    }
    
    public L getItNLit(int id) {
        return getItLit(id).getOpposite();
    }
    
    public int size() {
        return _nbItems;
    }
    
}
