/* ./satmining-seq/src/main/java/dag/satmining/problem/seq/SPRegExp.java

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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.seq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;

/**
 * Adds a constraint that the pattern should match a given regular expression.
 * The regular expression si treated using <a href="">dk.brics.automaton</a>
 * library and should follow the syntax from the <a
 * href="http://www.brics.dk/automaton/doc/dk/brics/automaton/RegExp.html">RegExp
 * class</a>.
 *
 * Formula:
 *
 * r^i_s is true if the substring of the pattern starting at position i is
 * recognized by the automaton from the state s.
 *
 * tr_i is true if position i in the pattern is the the trail {@link SPDomain#isInTrail(int)}.
 *
 * s_0 is the sinitial state.
 *
 * S_F is the set of accepting (final) states.
 *
 * f(s,c) is the transition function
 *
 * for 0 <= i <= maxSize and s in the automaton's states :
 *
 * if i+1 < maxSize :
 *
 * r^i_s <=> \bigvee_{c\in \Sigma\cup\{\jok\}\} p_i = c \wedge (r^{i+1}_{f(s,c)}
 * \vee (f(s,c)\in S_F \wedge tr_{i+1} \wedge \neg tr_i))
 *
 * if i+1 = maxSize :
 *
 * r^i_s <=> \bigvee_{c\in \Sigma\cup\{\jok\}\} p_i = c \wedge f(s,c)\in S_F
 *
 * and we force the recognition with a unit clause:
 *
 * r^0_{s_i}
 *
 * in the implementation, if f(s,c) is undefined, we don't include c in the
 * disjunction
 *
 * @author ecoquery
 */
public class SPRegExp<L extends Literal<L>> implements Constraint<L> {

    private SPDomain<L> _domain;
    private Automaton _automaton;
    private static final Logger LOG = LoggerFactory.getLogger(SPRegExp.class);

    public SPRegExp(SPDomain<L> domain, String regExp) {
        this._domain = domain;
        RegExp exp = new RegExp(regExp);
        char[][] mapping = _domain.charMapping();
        Automaton regExpAutomaton = exp.toAutomaton(true);
        _automaton = regExpAutomaton.homomorph(mapping[0], mapping[1]);
        LOG.info("Built automaton for regexp: {} states ", _automaton.getNumberOfStates());
    }

	@Override
    public void addClauses(ReifiedWeightedPBBuilder<L> satHandler) throws NoSolutionException {
        RecogLits r = new RecogLits(satHandler);
        for (int i = 0; i < _domain.getPatternMaxSize(); i++) {
            for (State s : _automaton.getStates()) {
                Collection<L> lits = new ArrayList<L>();
                for (char c = 0; c <= _domain.getJoker(); ++c) {
                    if (s.step(c) != null) {
                        if (i + 1 < _domain.getPatternMaxSize()) {
                            L p_i_c = _domain.getLiteral(i, c, true);
                            L l = satHandler.newLiteral();
                            satHandler.addReifiedConjunction(l, p_i_c, r.at(i + 1, s.step(c)));
                            lits.add(l);
                            if (s.step(c).isAccept()) {
                                l = satHandler.newLiteral();
                                satHandler.addReifiedClause(l, p_i_c, _domain.isInTrail(i + 1),
                                        _domain.isInTrail(i).getOpposite());
                                lits.add(l);
                            }
                        } else {
                            if (s.step(c).isAccept()) {
                                lits.add(_domain.getLiteral(i, c, true));
                            }
                        }
                    }
                }
                satHandler.addReifiedClause(r.at(i, s), lits);
            }
        }
        satHandler.addClause(r.at(0, _automaton.getInitialState()));
    }

    private class RecogLits {

        private Map<State, L>[] _recog;

        @SuppressWarnings("unchecked")
		public RecogLits(ReifiedWeightedPBBuilder<L> f) {
            _recog = new Map[_domain.getPatternMaxSize()];
            for (int i = 0; i < _domain.getPatternMaxSize(); i++) {
                _recog[i] = new TreeMap<State, L>();
                for (State s : _automaton.getStates()) {
                    _recog[i].put(s, f.newLiteral());
                }
            }
        }

        public L at(int pos, State state) {
            return _recog[pos].get(state);
        }
    }
}
