/* ./satmining-seq/src/main/java/dag/satmining/problem/seq/SPDomain.java

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.Interpretation;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dag.satmining.output.PatternConverter;
import dag.satmining.utils.ArrayContainer;
import dag.satmining.utils.MatrixCollection;

/**
 * Constraint representing the domain for sequence patterns.
 *
 * @author ecoquery
 *
 */
public class SPDomain<L extends Literal<L>> implements Constraint<L>, PatternConverter {

    private static final Logger LOG = LoggerFactory.getLogger(SPDomain.class);
    public static final char UNSPECIFIED_CHAR = '_';
    /**
     * The mapping from Id to char.
     */
    private CharSequence _mappingIdChar;
    /**
     * The mapping from chars to ids.
     */
    private Map<Character, Integer> _mappingCharId;
    /**
     * The id of the joker.
     */
    private int _joker;
    /**
     * The arrays of variables for representing the domain. The first index is
     * the position in the pattern. The second index is the char id. There is
     * k+1 char ids (one for each char + the joker).
     */
    private L[][] _variables;
    /**
     * The size of the pattern
     */
    private int _patternSize;
    /**
     * A list of literals that are true if the actual length of the pattern is
     * less that the literal's position. _isInTrail[i] is false if pattern size
     * > i.
     */
    private L[] _isInTrail;

    /**
     * Creates a new SPDomain together with the required variables.
     */
    public SPDomain(int patternSize, CharSequence mappingIdChar,
            Map<Character, Integer> mappingCharId) {
        this._mappingIdChar = mappingIdChar;
        this._mappingCharId = mappingCharId;
        this._joker = mappingIdChar.length() - 1; // last char id is the joker
        this._patternSize = patternSize;
    }

    /**
     * Creates the variables for the domain of the pattern.
     *
     * @param patternSize the size of the pattern.
     * @throws NoSolutionException
     */
	private void buildDomain(ReifiedWeightedPBBuilder<L> h) throws NoSolutionException {
        _variables = h.lMatrix(_patternSize,_joker + 1);
        for (int i = 0; i < _patternSize; i++) {
            for (int j = 0; j < _joker + 1; j++) {
                _variables[i][j] = h.newStrongLiteral();
            }
        }
    }

	private void addIsInTrail(ReifiedWeightedPBBuilder<L> h) throws NoSolutionException {
        _isInTrail = h.lArray(_variables.length);
        int last = _variables.length - 1;
        // case of the last position in the pattern
        _isInTrail[last] = getLiteral(last, _joker, true);
        // other positions
        for (int i = last - 1; i >= 0; i--) {
            _isInTrail[i] = h.newLiteral();
            // x mean isInTrail
            // p is the pattern
            // jok is the joker
            // x[i] <=> p[i][jok] /\ x[i+1]
            //
            //    (~x[i] \/ p[i][jok])
            // /\ (~x[i] \/ x[i+1])
            // /\ ( x[i] \/ ~p[i][jok] \/ ~x[i+1])
            L x_i = _isInTrail[i];
            L p_i_jok = getLiteral(i, getJoker(), true);
            L x_i_1 = _isInTrail[i + 1];
            h.addClause(x_i.getOpposite(), p_i_jok);
            h.addClause(x_i.getOpposite(), x_i_1);
            h.addClause(x_i, p_i_jok.getOpposite(), x_i_1.getOpposite());
        }
    }

    @Override
    public final void addClauses(ReifiedWeightedPBBuilder<L> h)
            throws NoSolutionException {
        LOG.debug("Creating domain ...");
        buildDomain(h);
        for (int i = 0; i < getPatternMaxSize(); i++) {
			L[] posDomain = h.lArray(_joker + 1);
            for (int j = 0; j < _joker + 1; j++) {
                posDomain[j] = _variables[i][j];
            }
            h.addExactlyOneTrue(posDomain);
        }
        addFirstNotJoker(h);
        LOG.debug("Adding trail ...");
        addIsInTrail(h);
        for(L l : getStrongBackdoor()) {
        	h.addToStrongBackdoor(l);	
        }
        LOG.debug("Added domain");
    }

    /**
     * The size of the pattern.
     *
     * @return the size of the pattern.
     */
    public final int getPatternMaxSize() {
        return _variables.length;
    }

    /**
     * Provides a positive/negative literal for the pattern at the given
     * position, for the given char id.
     *
     * @param pos theposition in the pattern
     * @param charId the id of the character
     * @param sign true for positive literals
     * @return the specified literal
     */
    public final L getLiteral(int pos, int charId, boolean sign) {
        L l = _variables[pos][charId];
        return sign ? l : l.getOpposite();
    }

    /**
     * The id of the joker.
     *
     * @return the char id for the joker.
     */
    public final int getJoker() {
        return _joker;
    }

    /**
     * Adds a constraints forbidding the given pattern. This is used for
     * iterating over results.
     *
     * @param s the forbidden pattern expressed in the original alphabet.
     * @return true if the constraint could be added.
     * @throws NoSolutionException if the SAT handler detects that there is no
     * solution
     */
    public final void addForbidSequence(CharSequence s, ReifiedWeightedPBBuilder<L> satHandler)
            throws NoSolutionException {
        int[] values = new int[getPatternMaxSize()];
        int i = 0;
        while (i < s.length() && i < getPatternMaxSize()) {
            Integer val = _mappingCharId.get(s.charAt(i));
            if (val != null) {
                values[i] = val;
            } else {
                throw new IllegalArgumentException("Illegal character: "
                        + s.charAt(i));
            }
            i++;
        }
        while (i < getPatternMaxSize()) {
            values[i] = _joker;
            i++;
        }
        addForbidSequence(values, satHandler);
    }

    /**
     * Adds a constraints forbidding the given pattern. This is used for
     * interating over results.
     *
     * @param seq the sequence in terms of letter ids.
     * @return true if the constraint could be added.
     */
    public final void addForbidSequence(int[] seq, ReifiedWeightedPBBuilder<L> satHandler)
            throws NoSolutionException {
        if (seq.length != getPatternMaxSize()) {
            throw new IllegalArgumentException(
                    "forbidden sequence has wrong size: " + seq.length
                    + " instead of " + getPatternMaxSize());
        }
		L[] c = satHandler.lArray(getPatternMaxSize());
        for (int i = 0; i < getPatternMaxSize(); i++) {
            c[i] = _variables[i][seq[i]].getOpposite();
        }
        satHandler.addClause(c);
        LOG.debug("Adding {}", new ArrayContainer<L>(c));
    }

    /**
     * Adds the constraint that the first letter cannot be a joker.
     *
     * @return true if the constraint could be added.
     */
	private void addFirstNotJoker(ReifiedWeightedPBBuilder<L> satHandler)
            throws NoSolutionException {
        satHandler.addClause(_variables[0][_joker].getOpposite());
    }

    public final L[][] getVariables() {
        return _variables;
    }

    // public final int[] getIntPattern(InteractiveSATSolver solver, boolean
    // full) {
    // int size = 1;
    // if (full) {
    // size = _variables.length;
    // } else {
    // for (int i = _variables.length - 1; i > 0; i--) {
    // if (!solver.getValue(_variables[i][_joker])) {
    // size = i + 1;
    // break;
    // }
    // }
    // }
    // int[] pattern = new int[size];
    // for (int i = 0; i < size; i++) {
    // pattern[i] = -1;
    // for (int j = 0; j <= _joker; j++) {
    // if (solver.getValue(_variables[i][j])) {
    // pattern[i] = j;
    // break;
    // }
    // }
    // }
    // return pattern;
    // }
    // public CharSequence getPattern(InteractiveSATSolver solver) {
    // return getPattern(getIntPattern(solver, false));
    // }
    private String getPattern(int[] intPattern, int size) {
        char[] pattern = new char[size];
        for (int i = 0; i < pattern.length; i++) {
            if (intPattern[i] == -1) {
                pattern[i] = UNSPECIFIED_CHAR;
            } else {
                pattern[i] = _mappingIdChar.charAt(intPattern[i]);
            }
        }
        return new String(pattern);
    }

    /**
     * Computes the String representation of the pattern given the model
     *
     * @param model the values of the variables
     * @return a string representation of the pattern
     */
    public String getPattern(Interpretation model) {
        int[] intPattern = new int[_variables.length];
        int size = 0;
        for (int i = 0; i < intPattern.length; ++i) {
            for (int c = 0; c <= _joker; ++c) {
                if (model.getValue(_variables[i][c])) {
                    intPattern[i] = c;
                    if (c != _joker) {
                        size = i + 1;
                    }
                    break;
                }
            }
        }
        return getPattern(intPattern, size);
    }

    public final Collection<L> getStrongBackdoor() {
        return new MatrixCollection<L>(_variables);
    }

    public int getPatternSize(int[] pattern) {
        int size = 1;
        for (int i = 1; i < pattern.length; i++) {
            if (pattern[i] != _joker) {
                size = i + 1;
            }
        }
        return size;
    }

    /**
     * A literal expressing whether a position is not in the actual pattern,
     * that is the position corresponds to a trailing joker.
     *
     * @param pos the position
     * @return a literal that is true if the position contains a trailing joker.
     */
    public L isInTrail(int pos) {
        return _isInTrail[pos];
    }

    /**
     * A literal expressing that the actual size of the pattern is at least the
     * given value.
     *
     * @param minSize the minimal size value
     * @return a literal which value is true is the size constraint is
     * respected.
     */
    public L sizeIsAtLeast(int minSize) {
        return _isInTrail[minSize - 1].getOpposite();
    }

    /**
     * A literal expressing that is actual number of trailing jokers is at least
     * the given value.
     *
     * @param minSize the minimal size value
     * @return a literal which value is true is the size constraint is
     * respected.
     */
    public L trailLengthIsAtLeast(int minSize) {
        return _isInTrail[getPatternMaxSize() - minSize];
    }
    
    public char[][] charMapping() {
        char [][] res = new char[2][getJoker()+1];
        for(int i = 0; i <= getJoker(); i++) {
            res[0][i] = _mappingIdChar.charAt(i);
        }
        Arrays.sort(res[0]);
        for(int i = 0; i <= getJoker(); i++) {
            res[1][i] = (char) (int) _mappingCharId.get(res[0][i]);
        }
        return res;
    }
}
