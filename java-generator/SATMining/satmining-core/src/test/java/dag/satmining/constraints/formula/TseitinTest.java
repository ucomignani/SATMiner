package dag.satmining.constraints.formula;

import static dag.satmining.constraints.formula.Formula.*;

import org.sat4j.specs.TimeoutException;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.dimacs.DimacsLiteral;

public class TseitinTest extends DirectTest {

	@Override
	protected void checkPass(Formula f) {
		try {
			f.tseitin(this);
			assertTrue(getSolver().isSatisfiable());
		} catch (NoSolutionException e) {
			fail(e.getLocalizedMessage());
		} catch (TimeoutException e) {
			fail(e.getLocalizedMessage());
		}
	}

	@Override
	protected void checkFail(Formula f) {
		try {
			DimacsLiteral l = f.tseitinLit(this);
			addClause(l.getOpposite());
			assertTrue(getSolver().isSatisfiable());
		} catch (NoSolutionException e) {
			fail(e.getLocalizedMessage());
		} catch (TimeoutException e) {
			fail(e.getLocalizedMessage());
		}
	}

	public void testTseitinOr() {
		try {
			DimacsLiteral l = newLiteral();
			DimacsLiteral l2 = or(l, not(l)).tseitinLit(this);
			addClause(l2.getOpposite());
			assertFalse(getSolver().isSatisfiable());
		} catch (NoSolutionException e) {
		} catch (TimeoutException e) {
			fail(e.getLocalizedMessage());
		}
	}

}
