/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.seq;

import java.util.ArrayList;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.constraints.mining.AbstractSupport;

/**
 *
 * @author ecoquery
 */
public class GSSupport<E,L extends Literal<L>> extends AbstractSupport<L> {

    private List<E> _data;
    private GSDomain<E,L> _domain;

    public GSSupport(GSDomain<E,L> domain) {
        super(domain.getData().size());
        this._domain = domain;
        this._data = domain.getData();
    }
    
    @Override
    protected void addMatchAt(int patternPos, PBBuilder<L> h) throws NoSolutionException {
        int firstForcedJok = Math.min(_data.size()-patternPos, _domain.size());
        List<L> letterMatchers = new ArrayList<L>(_domain.size());
        for(int i = 0; i < firstForcedJok; i++) {
            letterMatchers.add(_domain.getVariableAt(i).equivToMatch(
                    _data.get(patternPos+i), h));
        }
        for(int i = firstForcedJok; i < _domain.size(); i++) {
            letterMatchers.add(_domain.getVariableAt(i).equivToJoker(h));
        }
        h.addReifiedConjunction(getLiteral(patternPos), letterMatchers);
    }
    
}
