/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.seq;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 *
 * @author ecoquery
 */
public interface Alphabet<E, L extends Literal<L>> {
    
    /**
     * Creates a new variable for representing a letter.
     * @param handler
     * @return 
     */
    LetterLiteral<E,L> newLetterVariable(PBBuilder<L> handler) throws NoSolutionException;
    
    /**
     * Reads data and transforms it into a list of E
     * @param input the input data.
     * @return a list of E that was serialized in input
     */
    List<E> decode(Reader input) throws IOException;

    /**
     * The string that separates two letters in the sequence serialization
     * @return The letter separator
     */
    public CharSequence getSeparator();
    
}
