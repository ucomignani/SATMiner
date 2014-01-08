/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.seq;

import java.util.ArrayList;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.Interpretation;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.output.PatternConverter;

/**
 * Domain of sequence patterns occuring in sequences of type E.
 *
 * @author ecoquery
 */
public class GSDomain<E,L extends Literal<L>> implements Constraint<L>, PatternConverter {

    private List<E> _data;
    private Alphabet<E,L> _lFactory;
    private List<LetterLiteral<E,L>> _domain;
    private CharSequence _sep;
    private int _size;

    public GSDomain(List<E> data, Alphabet<E,L> letterFactory, int patternMaxSize) {
        this._data = data;
        this._lFactory = letterFactory;
        this._sep = _lFactory.getSeparator();
        this._size = patternMaxSize;
    }
    
    private void buildDomain(PBBuilder<L> satHandler) throws NoSolutionException {
        _domain = new ArrayList<LetterLiteral<E,L>>(_size);
        for (int i = 0; i < _size; i++) {
            _domain.add(_lFactory.newLetterVariable(satHandler));
        }
    }

	public void addClauses(PBBuilder<L> satHandler) throws NoSolutionException {
        buildDomain(satHandler);
        L firstIsJoker = _domain.get(0).equivToJoker(satHandler);
        // The first element of the sequence pattern cannot be the joker.
        satHandler.addClause(firstIsJoker.getOpposite());
//                satHandler.getClauseFactory().newClause(firstIsJoker.getOpposite()));
    }

    public CharSequence getPattern(Interpretation model) {
        StringBuilder output = new StringBuilder();
        boolean started = false;
        int actualSize = 0;
        for(LetterLiteral<E,L> lv : _domain) {
            if(started) {
                output.append(_sep);
            } else {
                started = true;
            }
            output.append(lv.getPattern(model));
            if(!lv.isJoker(model)) {
                actualSize = output.length();
            }
        }
        output.setLength(actualSize);
        return output;
    }
    
    public LetterLiteral<E,L> getVariableAt(int index) {
        return _domain.get(index);
    }
    
    public int size() {
        return _size;
    }

    public List<E> getData() {
        return _data;
    }
}
