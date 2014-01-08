/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.fim;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.constraints.mining.AbstractSupport;

/**
 *
 * @author ecoquery
 */
public class IPSupport<L extends Literal<L>> extends AbstractSupport<L> {

    private List<BitSet> _data;
    private IPDomain<L> _domain;
    
    public IPSupport(IPDomain<L> domain, List<BitSet> data) {
        super(data.size());
        this._data = data;
        this._domain = domain;
    }
    
    @Override
    protected void addMatchAt(int patternPos, PBBuilder<L> satHandler) throws NoSolutionException {
        List<L> lits = new ArrayList<L>();
        BitSet tr = _data.get(patternPos);
        for(int i = 0; i < _domain.size(); i++) {
            if (!tr.get(i)) {
                lits.add(_domain.getItNLit(i));
            }
        }
        satHandler.addReifiedConjunction(getLiteral(patternPos), lits);
    }
    
    public BitSet getTransaction(int idx) {
        return _data.get(idx);
    }
    
    public boolean isInTr(int item, int trId) {
        return _data.get(trId).get(item);
    }
}
