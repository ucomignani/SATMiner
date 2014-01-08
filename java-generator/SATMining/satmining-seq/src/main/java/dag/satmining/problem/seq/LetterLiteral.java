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
