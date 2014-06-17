/* ZincBackend.java

   Copyright (C) 2014 Emmanuel Coquery.

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
exception statement from your version.

 */

/**
 * 
 */
package dag.satmining.backend.flatzinc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dag.satmining.constraints.WeightedPBBuilder;
import dag.satmining.output.SolutionWriter;

/**
 * @author Emmanuel Coquery <emmanuel.coquery@liris.cnrs.fr> Implementation of
 *         {@link WeightedPBBuilder} by generating a flatzinc model.
 */
public final class ZincBackend implements ReifiedWeightedPBBuilder<ZincLiteral> {

    private BitSet _strongLiterals = new BitSet();
    private int _nextIntermediateLiteral = 1;
    private int _nextStrongLiteral = 1;
    private List<ZincLiteral> _literals = new ArrayList<ZincLiteral>();
    private Collection<ZincConstraint> _constraints = new ArrayList<ZincConstraint>();
    private int _recomputeIdxFrom = Integer.MAX_VALUE;
    private boolean _wroteSomeOutput = false;

    static final String TRUE = "true";

    /**
     * 
     */
    public ZincBackend() {
        _literals.add(null);
    }

    @Override
    public ZincLiteral fromDimacs(int dimacs) {
        int vid = Math.abs(dimacs);
        while (vid >= _literals.size()) {
            newLiteral();
        }
        ZincLiteral l = _literals.get(vid);
        return (l.isPositive() == (dimacs > 0)) ? l : l.getOpposite();
    }

    @Override
    public ZincLiteral newLiteral() {
        return newLiteral(true, false);
    }

    @Override
    public ZincLiteral newStrongLiteral() {
        return newLiteral(true, true);
    }

    @Override
    public ZincLiteral newLiteral(boolean positive, boolean strong) {
        int idx = strong ? _nextStrongLiteral++ : _nextIntermediateLiteral++;
        int vid = _nextIntermediateLiteral + _nextStrongLiteral - 2;
        ZincLiteral l = new ZincLiteral(vid, idx, positive, strong);
        if (strong) {
            _strongLiterals.set(l.getVariableId());
        }
        _literals.add(l);
        return l;
    }

    @Override
    public ZincLiteral[] lArray(int size) {
        return lArray(size, true);
    }

    @Override
    public ZincLiteral[] lArray(int size, boolean filled) {
        ZincLiteral[] a = new ZincLiteral[size];
        if (filled) {
            for (int i = 0; i < size; ++i) {
                a[i] = newLiteral();
            }
        }
        return a;
    }

    @Override
    public ZincLiteral[][] lMatrix(int size, int size2) {
        return lMatrix(size, size2, true);
    }

    @Override
    public ZincLiteral[][] lMatrix(int size, int size2, boolean filled) {
        ZincLiteral[][] a = new ZincLiteral[size][size2];
        if (filled) {
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    a[i][j] = newLiteral();
                }
            }
        }
        return a;
    }

    @Override
    public void addClause(ZincLiteral[] lits) throws NoSolutionException {
        _constraints.add(new ZCArrayBoolOr(null, lits));
    }

    @Override
    public void addClause(ZincLiteral l) throws NoSolutionException {
        _constraints.add(new ZCArrayBoolOr(null, l));
    }

    @Override
    public void addClause(ZincLiteral l1, ZincLiteral l2)
            throws NoSolutionException {
        _constraints.add(new ZCArrayBoolOr(null, l1, l2));
    }

    @Override
    public void addClause(ZincLiteral l1, ZincLiteral l2, ZincLiteral l3)
            throws NoSolutionException {
        _constraints.add(new ZCArrayBoolOr(null, l1, l2, l3));
    }

    @Override
    public void addClause(Collection<ZincLiteral> lits)
            throws NoSolutionException {
        _constraints.add(new ZCArrayBoolOr(null, lits));
    }

    @Override
    public void addToStrongBackdoor(ZincLiteral l) {
        if (!_strongLiterals.get(l.getVariableId())) {
            _strongLiterals.set(l.getVariableId());
            l.setStrong(true);
            if (l.getVariableId() == _nextIntermediateLiteral
                    + _nextStrongLiteral - 2) {
                _nextIntermediateLiteral--;
                l.setIdx(_nextStrongLiteral++);
            } else {
                _recomputeIdxFrom = Math.min(_recomputeIdxFrom,
                        l.getVariableId());
            }
        }
    }

    private void recomputeIdx() {
        if (_recomputeIdxFrom != Integer.MAX_VALUE) {
            if (_wroteSomeOutput) {
                throw new IllegalStateException(
                        "cannot reindex if some output has already been written");
            }
            int prev = _recomputeIdxFrom - 1;
            while (prev >= 1 && _strongLiterals.get(prev)) {
                prev--;
            }
            _nextIntermediateLiteral = prev == 0 ? 1 : _literals.get(prev)
                    .getIdx() + 1;
            prev = _recomputeIdxFrom - 1;
            while (prev >= 1 && !_strongLiterals.get(prev)) {
                prev--;
            }
            _nextStrongLiteral = prev == 0 ? 1
                    : _literals.get(prev).getIdx() + 1;
            for (int cur = _recomputeIdxFrom; cur < _literals.size(); ++cur) {
                _literals.get(cur).setIdx(
                        _strongLiterals.get(cur) ? _nextStrongLiteral++
                                : _nextIntermediateLiteral++);
            }
            _recomputeIdxFrom = Integer.MAX_VALUE;
        }
    }

    @Override
    public void endProblem() throws NoSolutionException {
        recomputeIdx();
    }

    @Override
    public void unify(ZincLiteral[] lits) throws NoSolutionException {
        _constraints.add(new ZCUnify(lits));
    }

    @Override
    public void unify(ZincLiteral l1, ZincLiteral l2)
            throws NoSolutionException {
        _constraints.add(new ZCUnify(l1, l2));
    }

    @Override
    public SolutionWriter getCNFWriter() {
        throw new UnsupportedOperationException(
                "No builtin compiler from zinc to CNF");
    }

    @Override
    public void addWPBInequality(ZincLiteral[] lits, int[] coefs, Ineq ineq,
            int value) throws NoSolutionException {
        _constraints.add(new ZCSum(lits, coefs, ineq, value, null));
    }

    @Override
    public void addPBInequality(ZincLiteral[] lits, Ineq ineq, int value)
            throws NoSolutionException {
        _constraints.add(new ZCSum(lits, ineq, value, null));
    }

    @Override
    public void addPBInequality(Collection<ZincLiteral> lits, Ineq ineq,
            int value) throws NoSolutionException {
        _constraints.add(new ZCSum(lits, ineq, value, null));
    }

    @Override
    public void addReifiedPBInequality(ZincLiteral[] lits, Ineq ineq,
            int value, ZincLiteral equivalentTo) throws NoSolutionException {
        _constraints.add(new ZCSum(lits, ineq, value, equivalentTo));
    }

    @Override
    public void addReifiedPBInequality(Collection<ZincLiteral> lits, Ineq ineq,
            int value, ZincLiteral equivalentTo) throws NoSolutionException {
        _constraints.add(new ZCSum(lits, ineq, value, equivalentTo));
    }

    @Override
    public void addExactlyOneTrue(Collection<ZincLiteral> lits)
            throws NoSolutionException {
        _constraints.add(new ZCSum(lits, Ineq.EQ, 1, null));
    }

    @Override
    public void addExactlyOneTrue(ZincLiteral[] lits)
            throws NoSolutionException {
        _constraints.add(new ZCSum(lits, Ineq.EQ, 1, null));
    }

    @Override
    public void addReifiedConjunction(ZincLiteral equivalentTo,
            ZincLiteral[] lits) throws NoSolutionException {
        _constraints.add(new ZCArrayBoolAnd(equivalentTo, lits));
    }

    @Override
    public void addReifiedConjunction(ZincLiteral equivalentTo, ZincLiteral l1,
            ZincLiteral l2) throws NoSolutionException {
        _constraints.add(new ZCArrayBoolAnd(equivalentTo, l1, l2));
    }

    @Override
    public void addReifiedConjunction(ZincLiteral equivalentTo,
            Collection<ZincLiteral> lits) throws NoSolutionException {
        _constraints.add(new ZCArrayBoolAnd(equivalentTo, lits));
    }

    @Override
    public void addReifiedClause(ZincLiteral equivalentTo, ZincLiteral[] lits)
            throws NoSolutionException {
        _constraints.add(new ZCArrayBoolOr(equivalentTo, lits));
    }

    @Override
    public void addReifiedClause(ZincLiteral equivalentTo, ZincLiteral l1,
            ZincLiteral l2) throws NoSolutionException {
        _constraints.add(new ZCArrayBoolOr(equivalentTo, l1, l2));
    }

    @Override
    public void addReifiedClause(ZincLiteral equivalentTo, ZincLiteral l1,
            ZincLiteral l2, ZincLiteral l3) throws NoSolutionException {
        _constraints.add(new ZCArrayBoolOr(equivalentTo, l1, l2, l3));
    }

    @Override
    public void addReifiedClause(ZincLiteral equivalentTo,
            Collection<ZincLiteral> lits) throws NoSolutionException {
        _constraints.add(new ZCArrayBoolOr(equivalentTo, lits));
    }

    @Override
    public void addReifiedWPBInequality(ZincLiteral[] lits, int[] coefs,
            Ineq ineq, int value, ZincLiteral equivTo)
            throws NoSolutionException {
        _constraints.add(new ZCSum(lits, coefs, ineq, value, equivTo));
    }
  
    private void writeZinc(PrintWriter out) {
        recomputeIdx();
        _wroteSomeOutput = true;
        writeHeader(out);
        for (ZincConstraint zc : _constraints) {
            zc.print(out);
            out.println();
        }
        writeFooter(out);
    }

    private void writeFooter(PrintWriter out) {
        out.println("solve satisfy");
    }

    private void writeArrayDecl(PrintWriter out, int size, String type,
            String name, boolean introduced) {
        out.print("array [1..");
        out.print(size);
        out.print("] of ");
        out.print(type);
        out.print(": ");
        out.print(name);
        if (introduced) {
            out.println(";");
        } else {
            out.print(":: output_array([1..");
            out.print(size);
            out.println("]);");
        }
    }

    private void writeHeader(PrintWriter out) {
        writeArrayDecl(out, _nextStrongLiteral-1, "bool",
                ZincLiteral.STRONG_POSITIVE, false);
        writeArrayDecl(out, _nextStrongLiteral-1, "bool", ZincLiteral.STRONG_NEGATIVE, true);
        writeArrayDecl(out, _nextIntermediateLiteral-1, "bool", ZincLiteral.INTRODUCED_POSITIVE, true);
        writeArrayDecl(out, _nextIntermediateLiteral-1, "bool", ZincLiteral.INTRODUCED_NEGATIVE, true);
        writeArrayDecl(out, _literals.size(), "int", ZincLiteral.INTRODUCED_INT_POS, true);
        writeArrayDecl(out, _literals.size(), "int", ZincLiteral.INTRODUCED_INT_NEG, true);
        for(ZincLiteral l : _literals) {
            l.printOppositeConstraintB(out);
            l.printIntBoolConstraint(out);
            l.getOpposite().printIntBoolConstraint(out);
        }
    }

}
