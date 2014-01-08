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
