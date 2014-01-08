/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.seq;

import java.util.ArrayList;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

/**
 * Implements the constraint that no more than l jokers can be found 
 * consecutively in the pattern. This is encoded by the following formula:
 * 
 * m is the maximal pattern size
 * p is the pattern 
 * t_i is true if i is a position in the pattern's trail
 * 
 * \bigwedge_{i=0}^{m-1-l}(t_i \vee \bigvee_{j=0}^{l} p_{i+j} \neq \jok)
 * 
 * @author manu
 */
public class SPMaxConsecutiveJokers<L extends Literal<L>> implements Constraint<L> {

    private SPDomain<L> _domain;
    private final int _maxJoks;
    
    public SPMaxConsecutiveJokers(SPDomain<L> domain, int maxConsecutiveJokers) {
        this._domain = domain;
        this._maxJoks = maxConsecutiveJokers;
    }
    
    @Override
    public void addClauses(PBBuilder<L> satHandler) throws NoSolutionException {
        for(int i = 0; i < _domain.getPatternMaxSize()-_maxJoks; i++) {
            List<L> lits = new ArrayList<L>();
            lits.add(_domain.isInTrail(i));
            for(int j = 0; j <= _maxJoks; j++) {
                lits.add(_domain.getLiteral(i+j, _domain.getJoker(), false));
            }
            satHandler.addClause(lits);
        }
    }
    
}
