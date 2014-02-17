/* ./satmining-seq/src/main/java/dag/satmining/problem/seq/SPMaximal.java

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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

/**
 *
 * @author ecoquery
 */
public class SPMaximal<L extends Literal<L>> implements Constraint<L> {

    private static final Logger LOG = LoggerFactory.getLogger(SPMaximal.class);
    private SPDomain<L> _domain;
    private SPSupport<L> _support;
    private int _minFreq;

    public SPMaximal(SPDomain<L> domain, SPSupport<L> support, int minFreq) {
        this._domain = domain;
        this._support = support;
        this._minFreq = minFreq;
    }

    @Override
    public void addClauses(PBBuilder<L> satHandler) throws NoSolutionException {
//        for (int c = 0; c < _domain.getJoker(); ++c) {
//            for (int i = 0; i < _domain.getPatternMaxSize(); ++i) {
//                LOG.debug("sumLetterAtPos({},{})", c, i);
//                sumLetterAtPos(c, i, satHandler);
//            }
//        }
        for (int i = 0; i < _domain.getPatternMaxSize(); i++) {
            LOG.debug("constraintJoker for maximals at pos {}", i);
            constraintJoker(i, satHandler);
        }
        for (int c = 0; c < _domain.getJoker(); ++c) {
            for (int j = 1; j < _domain.getPatternMaxSize(); ++j) {
                LOG.debug("notShiftSumLetter({},{})", c, j);
                notShiftSumLetter(c, j, satHandler);
            }
        }
    }

	private void constraintJoker(int pos, PBBuilder<L> satHandler) throws NoSolutionException {
		L[] conj = satHandler.lArray(_domain.getJoker());
        for (int c = 0; c < _domain.getJoker(); ++c) {
            conj[c] = satHandler.newStrongLiteral();
            List<L> toSum = new ArrayList<L>();
            for (int k = 0; k + pos < _support.size(); ++k) {
                if (_support.getLetterAt(k + pos) == c) {
                    toSum.add(_support.getLiteral(k));
                }
            }
            if (toSum.size() <= _minFreq - 1) {
                LOG.debug("inequality always satisfied "
                        + "in constraint joker for pos {}, letter {}: {} literals <= {}",
                        new Object[]{pos,c,toSum.size(),_minFreq-1});
                satHandler.addClause(conj[c]);
            } else {
                satHandler.addReifiedPBInequality(
                        toSum, Ineq.LEQ, _minFreq - 1, conj[c]);
            }
        }
        L l = satHandler.newLiteral();
        satHandler.addReifiedConjunction(l, conj);
        satHandler.addClause(
                _domain.getLiteral(pos, _domain.getJoker(), true).getOpposite(), l);
    }

/*    private void sumLetterAtPos(int letter, int pos, SATHandler satHandler)
            throws NoSolutionException {
        ClauseFactory f = satHandler.getClauseFactory();
        List<Literal> toSum = new ArrayList<Literal>();
        for (int k = 0; k < _support.size() - pos; ++k) {
            if (_support.getLetterAt(k + pos) == letter) {
                toSum.add(_support.getLiteral(k));
            }
        }
        if (toSum.size() < _minFreq) {
            LOG.debug("Not adding sum reified constraint: {} literals >= {}",
                    toSum.size(), _minFreq);
        } else {
            Literal eqIneq = f.newLiteral(true);
            satHandler.addReifiedPBInequality(f.newReifiedPBInequality(
                    toSum, Ineq.GEQ, _minFreq, eqIneq));
            satHandler.addClause(f.newClause(
                    eqIneq.getOpposite(),
                    _domain.getLiteral(pos, letter, true)));
        }
    }*/

    private void notShiftSumLetter(int letter, int shift, PBBuilder<L> satHandler) throws NoSolutionException {
        List<L> toSum = new ArrayList<L>();
        for (int k = shift; k < _support.size(); ++k) {
            if (_support.getLetterAt(k - shift) == letter) {
                toSum.add(_support.getLiteral(k));
            }
        }
        if (toSum.size() <= _minFreq - 1) {
            LOG.debug("Not adding sum reified constraint: {} literals <= {}",
                    toSum.size(), _minFreq - 1);
        } else {
            L eqIneq = satHandler.newLiteral();
            satHandler.addReifiedPBInequality(
                    toSum, Ineq.LEQ, _minFreq - 1, eqIneq);
            List<L> implClause = new ArrayList<L>(shift);
            for (int i = _domain.getPatternMaxSize() - shift; i < _domain.getPatternMaxSize(); ++i) {
                implClause.add(_domain.getLiteral(i, _domain.getJoker(), false));
            }
            implClause.add(eqIneq);
            satHandler.addClause(implClause);
        }
    }
}
