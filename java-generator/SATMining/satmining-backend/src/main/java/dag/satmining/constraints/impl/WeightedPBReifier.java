/* WeightedPBReifier.java

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
package dag.satmining.constraints.impl;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dag.satmining.constraints.WeightedPBBuilder;

/**
 * Provides implmentation of reified weighted inequalities from non reified
 * ones.
 * 
 * @author ecoquery
 * 
 */
public class WeightedPBReifier<L extends Literal<L>> {

    private WeightedPBBuilder<L> _builder;
    private boolean _transformNegs;

    /**
     * Constructs a weighted pb reifier to extend the capacities of the provided
     * WeightedPBBuilder.
     * 
     * @param litClazz
     *            the class of literals.
     * @param baseBuilder
     *            the builder to extend.
     * @param canHandleNegatives
     *            true if the base builder can handle negative coefficients.
     */
    public WeightedPBReifier(WeightedPBBuilder<L> baseBuilder, boolean canHandleNegatives) {
        this._builder = baseBuilder;
        this._transformNegs = !canHandleNegatives;
    }

    private void addIneq(L[] lits, int[] coeffs, Ineq ineq, int value)
            throws NoSolutionException {
        if (_transformNegs) {
            // rmq: -c * l = -c * (1 - ~l) = -c + c*~l
            int sumOfNegCoeffs = 0;
            int[] newcoeffs = new int[coeffs.length];
            L[] newLits = _builder.lArray(lits.length, false);
            for (int i = 0; i < coeffs.length; ++i) {
                if (coeffs[i] < 0) {
                    newLits[i] = lits[i].getOpposite();
                    newcoeffs[i] = -coeffs[i];
                    sumOfNegCoeffs += newcoeffs[i];
                } else {
                    newLits[i] = lits[i];
                    newcoeffs[i] = coeffs[i];
                }
            }
            _builder.addWPBInequality(newLits, newcoeffs, ineq, value
                    + sumOfNegCoeffs);
        } else {
            _builder.addWPBInequality(lits, coeffs, ineq, value);
        }
    }

    private void litImpliesGeq(L lit, L[] lits, int[] coefs, int value)
            throws NoSolutionException {
        // lit -> sum(lits*coefs) >= value
        // (value+abs(sumOfNegCoefs)) * ~lit + sum(lits*coefs) >= value
        int[] newCoefs = new int[coefs.length + 1];
        L[] newLits = _builder.lArray(lits.length + 1, false);
        int absOfSumOfNegs = 0;
        for (int i = 0; i < lits.length; ++i) {
            newCoefs[i] = coefs[i];
            newLits[i] = lits[i];
            if (coefs[i] < 0) {
                absOfSumOfNegs -= coefs[i];
            }
        }
        newLits[lits.length] = lit.getOpposite();
        newCoefs[coefs.length] = value + absOfSumOfNegs;
        addIneq(newLits, newCoefs, Ineq.GEQ, value);
    }

    private void litImpliesLeq(L lit, L[] lits, int[] coefs, int value)
            throws NoSolutionException {
        // multiply by -1 to reverse the inequality
        int[] newCoefs = new int[coefs.length];
        for (int i = 0; i < coefs.length; ++i) {
            newCoefs[i] = -coefs[i];
        }
        litImpliesGeq(lit, lits, newCoefs, -value);
    }

    /**
     * Adds a reified weighted pseudo boolean inequality.
     * 
     * @see ReifiedWeightedPBBuilder
     */
    public void addReifiedWPBInequality(L[] lits, int[] coefs, Ineq ineq,
            int value, L equivTo) throws NoSolutionException {
        switch (ineq) {
        case GEQ:
            litImpliesGeq(equivTo, lits, coefs, value);
            litImpliesLeq(equivTo.getOpposite(), lits, coefs, value - 1);
            break;
        case LEQ:
            litImpliesLeq(equivTo, lits, coefs, value);
            litImpliesGeq(equivTo.getOpposite(), lits, coefs, value + 1);
            break;
        case EQ:
            L lgeq = _builder.newLiteral();
            L lleq = _builder.newLiteral();
            _builder.addClause(equivTo.getOpposite(), lgeq);
            _builder.addClause(equivTo.getOpposite(), lleq);
            _builder.addClause(lleq.getOpposite(), lgeq.getOpposite(), equivTo);
            addReifiedWPBInequality(lits, coefs, Ineq.GEQ, value, lgeq);
            addReifiedWPBInequality(lits, coefs, Ineq.LEQ, value, lleq);
        }
    }
}
