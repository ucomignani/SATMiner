package dag.satmining.problem.seq;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.constraints.mining.AbstractSupport;

/**
 * High-level constraint expressing that the sequence pattern matches the in
 * original sequence for each position if some literal is set to true.
 * The literals are created by the constraint.
 * 
 * @author ecoquery
 * 
 */
public class SPSupport<L extends Literal<L>> extends AbstractSupport<L> {

    /**
     * The pattern domain.
     */
    private SPDomain<L> _domain;
    /**
     * The content in terms of ids.
     */
    private int[] _intContent;

    public SPSupport(SPDomain<L> domain, int[] content) {
        super(content.length);
        this._domain = domain;
        this._intContent = content;
    }

    /**
     * Adds the clauses for expressing that the returned literal should be true
     * if the given position in pattern match the input data if the pattern is
     * placed at the given position.
     * 
     * @param patternPos
     *            the position of the (first letter of the) pattern.
     * @param posInPattern
     *            the position to look at in the pattern.
     * @return a literal which value is true if the indicated pattern letter
     *         matches the input data letter at position patternPos +
     *         patternLetter.
     * @throws NoSolutionException if the SAT handler discovers that there is no solutions
     */
	private L literalForPatternMatchAt(int patternPos,
            int posInPattern, PBBuilder<L> h) throws NoSolutionException {
        int absPos = patternPos + posInPattern;
        if (absPos >= _intContent.length) {
            // out of content, letter should be the joker
            return _domain.getLiteral(posInPattern, _domain.getJoker(), true);
        } else {
            // introduce a variable for the matching
            L v = h.newLiteral();
            int letterIdx = _intContent[absPos];
            h.addReifiedClause(v, 
            		_domain.getLiteral(posInPattern, letterIdx, true),
                    _domain.getLiteral(posInPattern, _domain.getJoker(), true));
            return v;
        }
    }

    @Override
    protected void addMatchAt(int patternPosition, PBBuilder<L> satHandler) throws NoSolutionException {
        int patternSize = _domain.getPatternMaxSize();
		L[] conj = satHandler.lArray(patternSize);
        for (int posInPattern = 0; posInPattern < patternSize; posInPattern++) {
            conj[posInPattern] = literalForPatternMatchAt(patternPosition,
                    posInPattern, satHandler);
        }
        satHandler.addReifiedConjunction(getLiteral(patternPosition), conj);
    }
    
    public int getLetterAt(int pos) {
        return _intContent[pos];
    }
}
