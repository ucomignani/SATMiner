/* ./satmining-seq/src/main/java/dag/satmining/problem/seq/SPSupport.java

   Copyright (C) 2013, 2014 Emmanuel Coquery.

This file is part of SATMiner

SATMiner is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

SATMiner is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with SATMiner; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package dag.satmining.problem.seq;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
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
            int posInPattern, ReifiedWeightedPBBuilder<L> h) throws NoSolutionException {
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
    protected void addMatchAt(int patternPosition, ReifiedWeightedPBBuilder<L> satHandler) throws NoSolutionException {
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
