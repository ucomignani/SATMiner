/* ./satmining-seq/src/main/java/dag/satmining/problem/seq/LetterLiteral.java

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

import dag.satmining.NoSolutionException;
import dag.satmining.backend.Interpretation;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.output.PatternConverter;

/**
 * Represent a letter variable. The class parameter E is the type used to
 * represent values of this domain.
 *
 * @author ecoquery
 */
public interface LetterLiteral<E,L extends Literal<L>> extends PatternConverter {

    /**
     * Adds the reified constraint that the variable should match the given
     * value. That is the value taken by the variable should be lesser than or
     * equal to the provided value.
     *
     * @param value the value to compare to
     * @param handler the handler for adding basic constraints
     * @return the literal to be equivalent to the matching of this value
     * @throws NoSolutionException
     */
    L equivToMatch(E value, PBBuilder<L> handler) throws NoSolutionException;

    /**
     * Adds a reified constraint that the current variable value is joker, that
     * is the bottom of the values.
     *
     * @param handler the handler for adding basic constraints
     * @return the literal to be equivalent to the letter variable being equal
     * to the joker.
     * @throws NoSolutionException
     */
    L equivToJoker(PBBuilder<L> handler) throws NoSolutionException;
    
    /**
     * True if the value associated to this variable is the joker.
     * @param model the values of boolean variables
     * @return true if the value associated to this variable is the joker.
     */
    boolean isJoker(Interpretation model);
}
