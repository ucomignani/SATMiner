package dag.satmining;

import junit.framework.TestCase;
import boolvar.model.Variable;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.dimacs.FileDimacsBackend;
import dag.satmining.backend.pb.gen.CardNetworksPBFactory;
import dag.satmining.backend.sat4j.SAT4JPBBuilder;
import dag.satmining.run.Main;

public class OutputTest extends TestCase {

	@Override
	protected final void setUp() throws Exception {
		Variable.setUsed(0);
	}

	public final void testMainWithGeneratedData() throws Exception {
		String[] args = { "-f", "5", "-m", "3", "-genseq", "15", "3", "-o",
				"test.output" };
		Main<DimacsLiteral> pgm = new Main<DimacsLiteral>(DimacsLiteral.class,
				new CardNetworksPBFactory<DimacsLiteral>(
						new FileDimacsBackend()), null);
		pgm.parseArgs(args);
		pgm.run();
	}

	public final void testSolutionOutput() throws Exception {
		String[] args = { "-f", "5", "-m", "3", "-genseq", "15", "3", "-sat4j" };
		SAT4JPBBuilder sat4j = new SAT4JPBBuilder(SAT4JPBBuilder.SMALL);
		Main<DimacsLiteral> pgm = new Main<DimacsLiteral>(DimacsLiteral.class,
				sat4j, sat4j);
		pgm.parseArgs(args);
		pgm.run();
		System.out.flush();
		System.err.flush();
	}

}
