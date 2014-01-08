/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.seq;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Set;

import dag.satmining.backend.dimacs.DimacsLiteral;

import junit.framework.TestCase;

/**
 *
 * @author ecoquery
 */
public class CharSetAlphabetTest extends TestCase {
    
    // Assume chars in seq are unique
    private void checkSet(CharSequence seq, Set<Character> set) {
        assertEquals(seq.length(), set.size());
        for(int i = 0; i < seq.length(); i++) {
            assertTrue(set.contains(seq.charAt(i)));
        }
    }
    
    public void testDecode() throws IOException {
        CharSetAlphabet<DimacsLiteral> csa = new CharSetAlphabet<DimacsLiteral>();
        String input = "ab,abc,bc,bac,b,a";
        List<Set<Character>> output = csa.decode(new StringReader(input));
        assertEquals(6,output.size());
        checkSet("ab", output.get(0));
        checkSet("abc", output.get(1));
        checkSet("bc", output.get(2));
        checkSet("abc", output.get(3));
        checkSet("b", output.get(4));
        checkSet("a", output.get(5));
    }
    
}
