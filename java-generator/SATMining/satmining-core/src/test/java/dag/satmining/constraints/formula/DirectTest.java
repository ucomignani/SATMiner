package dag.satmining.constraints.formula;

import static dag.satmining.constraints.formula.Formula.*;

import org.sat4j.specs.TimeoutException;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.constraints.SAT4JMCB;

public class DirectTest extends SAT4JMCB {

	private DimacsLiteral _a;
	private DimacsLiteral _b;
	private DimacsLiteral _c;
	private DimacsLiteral _d;
	private DimacsLiteral _e;
	private DimacsLiteral _f;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_a = newLiteral();
		addClause(_a);
		_b = newLiteral();
		addClause(_b);
		_c = newLiteral();
		addClause(_c);
		_d = newLiteral();
		addClause(_d);
		_e = newLiteral();
		addClause(_e);
		_f = newLiteral();
		addClause(_f);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		_a = null;
		_b = null;
		_c = null;
		_d = null;
		_e = null;
		_f = null;
	}

	protected void checkPass(Formula f) {
		try {
			f.direct(this);
			assertTrue(getSolver().isSatisfiable());
		} catch (NoSolutionException e) {
			fail(e.getMessage());
		} catch (TimeoutException e) {
			fail(e.getMessage());
		}

	}

	protected void checkFail(Formula f) {
		try {
			f.direct(this);
			assertFalse(getSolver().isSatisfiable());
		} catch (NoSolutionException e) {
		} catch (TimeoutException e) {
			fail(e.getMessage());
		}
	}

	public void testAnd1() throws TimeoutException {
		checkPass(and(_a, _b));
	}

	public void testAnd2() throws TimeoutException {
		checkFail(and(_a.getOpposite(), _b));
	}

	public void testAnd3() {
		checkFail(and(_a.getOpposite(), _b.getOpposite()));
	}

	public void testOr1() {
		checkPass(or(_a, _b));
	}

	public void testOr2() {
		checkPass(or(_a.getOpposite(), _b));
	}

	public void testOr3() {
		checkFail(or(_a.getOpposite(), _b.getOpposite()));
	}

	public void testImpl1() {
		checkPass(impl(atom(_a), atom(_b)));
	}

	public void testImpl2() {
		checkPass(impl(atom(_a.getOpposite()), atom(_b.getOpposite())));
	}

	public void testImpl3() {
		checkFail(impl(atom(_a), atom(_b.getOpposite())));
	}

	public void testEquiv1() {
		checkPass(equiv(atom(_a), _b));
	}

	public void testEquiv2() {
		checkFail(equiv(_a.getOpposite(), atom(_b)));
	}

	public void testEquiv3() {
		checkFail(equiv(atom(_a), _b.getOpposite()));
	}

	public void testEquiv4() {
		checkPass(equiv(atom(_a.getOpposite()), _b.getOpposite()));
	}
	
	public void testNeg1() {
		checkPass(not(_a.getOpposite()));
	}
	
	public void testNeg2() {
		checkFail(not(_a));
	}

}
