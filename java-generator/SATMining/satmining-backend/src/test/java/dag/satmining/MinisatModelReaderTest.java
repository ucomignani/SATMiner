package dag.satmining;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import junit.framework.TestCase;
import dag.satmining.backend.Interpretation;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.minisat.MinisatModelReader;

public class MinisatModelReaderTest extends TestCase {

	public void testReader() {
		String input = "=\n|12 \n=  gh\n12 1 1245 56 \n78 80 789\n";
		StringReader sr = new StringReader(input);
		MinisatModelReader mr = new MinisatModelReader(sr);
		assertTrue(mr.getNext());
		Interpretation inter = mr.getCurrentInterpretation();
		assertNotNull(inter);
		assertFalse(inter.getValue(new DimacsLiteral(3)));
		assertTrue(inter.getValue(new DimacsLiteral(1)));
		assertTrue(inter.getValue(new DimacsLiteral(12)));
		assertTrue(inter.getValue(new DimacsLiteral(56)));
		assertTrue(inter.getValue(new DimacsLiteral(1245)));
		assertTrue(mr.getNext());
		inter = mr.getCurrentInterpretation();
		assertNotNull(inter);
		assertFalse(inter.getValue(new DimacsLiteral(1)));
		assertFalse(inter.getValue(new DimacsLiteral(12)));
		assertTrue(inter.getValue(new DimacsLiteral(78)));
		assertTrue(inter.getValue(new DimacsLiteral(80)));
		assertTrue(inter.getValue(new DimacsLiteral(789)));
		assertFalse(mr.getNext());
	}
	
	public void testNbModels() throws IOException {
		InputStream is = getClass().getResourceAsStream("/output1.txt");
		MinisatModelReader mr = new MinisatModelReader(new InputStreamReader(is));
		for(int i = 0 ; i < 40; ++i) {
			assertTrue(mr.getNext());
		}
		assertFalse(mr.getNext());
	}
	
}
