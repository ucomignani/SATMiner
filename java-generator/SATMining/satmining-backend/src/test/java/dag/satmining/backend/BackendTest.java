/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;
import dag.satmining.NoSolutionException;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

/**
 * 
 * @author ecoquery
 */
public abstract class BackendTest<L extends Literal<L>> extends TestCase {

	private static final Logger LOG = LoggerFactory
			.getLogger(BackendTest.class);

	protected PBBuilder<L> _handler;
	protected ModelReader _modelReader;
	protected L _a, _b, _c, _d, _e;

	@Override
	protected void setUp() throws Exception {
		initHandler();
		_a = newLiteral();
		_b = newLiteral();
		_c = newLiteral();
		_d = newLiteral();
		_e = newLiteral();
	}

	@Override
	protected void tearDown() throws Exception {
		destroyHandler();
	}

	private L newLiteral() {
		return _handler.newStrongLiteral();
	}

	public void testClause() throws NoSolutionException {
		_handler.addClause(_a, _b, _c);
		_handler.addClause(_d);
		_handler.addClause(_e);
		_handler.endProblem();
		int nb = 0;
		while (_modelReader.getNext()) {
			Interpretation inter = _modelReader.getCurrentInterpretation();
			nb++;
			LOG.debug("_a: {}",inter.getValue(_a));
			LOG.debug("_b: {}",inter.getValue(_b));
			LOG.debug("_c: {}",inter.getValue(_c));
			LOG.debug("_d: {}",inter.getValue(_d));
			LOG.debug("_e: {}",inter.getValue(_e));
			assertTrue(inter.getValue(_a) || inter.getValue(_b)
					|| inter.getValue(_c));
			assertTrue(inter.getValue(_d));
			assertTrue(inter.getValue(_e));
		}
		assertEquals(7, nb);
	}

	private L[] array(L a, L b, L c) {
		L[] lits = _handler.lArray(3);
		lits[0] = a;
		lits[1] = b;
		lits[2] = c;
		return lits;
	}

	private L[] array(L a, L b, L c, L d) {
		L[] lits = _handler.lArray(4);
		lits[0] = a;
		lits[1] = b;
		lits[2] = c;
		lits[3] = d;
		return lits;
	}

	public void testPBInequality_LEQ_SAT() throws NoSolutionException {
		_handler.addClause(_a, _b, _c);
		_handler.addPBInequality(array(_a, _b, _c), Ineq.LEQ, 2);
		_handler.endProblem();
		assertTrue(_modelReader.getNext());
	}

	public void testPBInequality_LEQ_UNSAT() {
		try {
			_handler.addClause(_a, _b, _c);
			_handler.addClause(_a);
			_handler.addClause(_b);
			_handler.addClause(_c);
			_handler.addPBInequality(array(_a, _b, _c), Ineq.LEQ, 2);
			_handler.endProblem();
			assertFalse(_modelReader.getNext());
		} catch (NoSolutionException e) {
		}
	}

	public void testPBInequality_GEQ_SAT() throws NoSolutionException {
		_handler.addClause(_a, _b, _c);
		_handler.addPBInequality(array(_a, _b, _c), Ineq.GEQ, 2);
		_handler.endProblem();
		assertTrue(_modelReader.getNext());
	}

	public void testPBInequality_GEQ_UNSAT3() throws NoSolutionException {
		try {
			_handler.addClause(_a, _b, _c);
			_handler.addClause(_a.getOpposite());
			_handler.addClause(_b.getOpposite());
			_handler.addPBInequality(array(_a, _b, _c), Ineq.GEQ, 2);
			_handler.endProblem();
			assertFalse(_modelReader.getNext());
		} catch (NoSolutionException e) {
		}
	}

	public void testPBInequality_GEQ_UNSAT4a() throws NoSolutionException {
		try {
			_handler.addClause(array(_a, _b, _c, _d));
			_handler.addClause(_a.getOpposite());
			_handler.addClause(_b.getOpposite());
			_handler.addClause(_c.getOpposite());
			_handler.addPBInequality(array(_a, _b, _c, _d), Ineq.GEQ, 2);
			_handler.endProblem();
			if (_modelReader.getNext()) {
				Interpretation i = _modelReader.getCurrentInterpretation();
				LOG.debug("a: {}",i.getValue(_a));
				LOG.debug("b: {}",i.getValue(_b));
				LOG.debug("c: {}",i.getValue(_c));
				LOG.debug("d: {}",i.getValue(_d));
				LOG.debug("test: {}", i.getValue(new DimacsLiteral(25)));
				fail();
			}
			// assertFalse(_modelReader.getNext());
		} catch (NoSolutionException e) {
		}
	}

	public void testPBInequality_GEQ_UNSAT4b() throws NoSolutionException {
		try {
			_handler.addClause(array(_a, _b, _c, _d));
			_handler.addClause(_a.getOpposite());
			_handler.addClause(_b.getOpposite());
			_handler.addClause(_c.getOpposite());
			_handler.addPBInequality(array(_a, _b, _c, _d), Ineq.GEQ, 3);
			_handler.endProblem();
			assertFalse(_modelReader.getNext());
		} catch (NoSolutionException e) {
		}
	}

	public void testReifiedPBInequality_LEQ_SAT_EQ() throws NoSolutionException {
		_handler.addClause(_d, _e);
		_handler.addClause(_a, _b, _c);
		_handler.addClause(_a);
		_handler.addClause(_b.getOpposite());
		_handler.addClause(_c.getOpposite());
		_handler.addClause(_d);
		_handler.addReifiedPBInequality(array(_a, _b, _c), Ineq.LEQ, 2, _d);
		_handler.endProblem();
		assertTrue(_modelReader.getNext());
	}

	public void testReifiedPBInequality_LEQ_SAT_NEQ()
			throws NoSolutionException {
		try {
			_handler.addClause(_d, _e);
			_handler.addClause(_a, _b, _c);
			_handler.addClause(_a);
			_handler.addClause(_b);
			_handler.addClause(_c.getOpposite());
			_handler.addClause(_d);
			_handler.addReifiedPBInequality(array(_a, _b, _c), Ineq.LEQ, 2,
					_d.getOpposite());
			_handler.endProblem();
			assertFalse(_modelReader.getNext());
		} catch (NoSolutionException e) {
		}
	}

	public void testReifiedPBInequality_LEQ_UNSAT_EQ()
			throws NoSolutionException {
		_handler.addClause(_d, _e);
		_handler.addClause(_a, _b, _c);
		_handler.addClause(_d);
		_handler.addClause(_a);
		_handler.addClause(_b);
		_handler.addClause(_c);
		_handler.addClause(_d);
		_handler.addReifiedPBInequality(array(_a, _b, _c), Ineq.LEQ, 2,
				_d.getOpposite());
		_handler.endProblem();
		assertTrue(_modelReader.getNext());
	}

	public void testReifiedPBInequality_LEQ_UNSAT_NEQ()
			throws NoSolutionException {
		try {
			_handler.addClause(_d, _e);
			_handler.addClause(_a, _b, _c);
			_handler.addClause(_d);
			_handler.addClause(_a);
			_handler.addClause(_b);
			_handler.addClause(_c);
			_handler.addClause(_d);
			_handler.addReifiedPBInequality(array(_a, _b, _c), Ineq.LEQ, 2, _d);
			_handler.endProblem();
			assertFalse(_modelReader.getNext());
		} catch (NoSolutionException e) {
		}
	}

	public void testReifiedPBInequality_GEQ_SAT_EQ() throws NoSolutionException {
		_handler.addClause(_d, _e);
		_handler.addClause(_a, _b, _c);
		_handler.addClause(_a);
		_handler.addClause(_b);
		_handler.addClause(_c);
		_handler.addClause(_d);
		_handler.addReifiedPBInequality(array(_a, _b, _c), Ineq.GEQ, 2, _d);
		_handler.endProblem();
		assertTrue(_modelReader.getNext());
		Interpretation i = _modelReader.getCurrentInterpretation();
		assertTrue(i.getValue(_a));
		assertTrue(i.getValue(_b));
		assertTrue(i.getValue(_c));
		assertTrue(i.getValue(_d));
	}

	public void testReifiedPBInequality_GEQ_SAT_NEQ()
			throws NoSolutionException {
		try {
			_handler.addClause(_d, _e);
			_handler.addClause(_a, _b, _c);
			_handler.addClause(_a);
			_handler.addClause(_b);
			_handler.addClause(_c);
			_handler.addClause(_d);
			_handler.addReifiedPBInequality(array(_a, _b, _c), Ineq.GEQ, 2,
					_d.getOpposite());
			_handler.endProblem();
			assertFalse(_modelReader.getNext());
		} catch (NoSolutionException e) {
			// LOG.warn("bla", e);
		}
	}

	public void testReifiedPBInequality_GEQ_SAT() throws NoSolutionException {
		_handler.addClause(_d, _e);
		_handler.addClause(_a, _b, _c);
		_handler.addClause(_a);
		_handler.addClause(_b);
		_handler.addClause(_c);
		_handler.addClause(_d);
		_handler.addPBInequality(array(_a, _b, _c), Ineq.GEQ, 2);
		_handler.endProblem();
		assertTrue(_modelReader.getNext());
	}

	public void testReifiedPBInequality_GEQ_UNSAT_EQ()
			throws NoSolutionException {
		_handler.addClause(_d, _e);
		_handler.addClause(_a, _b, _c);
		_handler.addClause(_d);
		_handler.addClause(_a.getOpposite());
		_handler.addClause(_b.getOpposite());
		_handler.addClause(_c);
		_handler.addReifiedPBInequality(array(_a, _b, _c), Ineq.GEQ, 2,
				_d.getOpposite());
		_handler.endProblem();
		assertTrue(_modelReader.getNext());
	}

	public void testReifiedPBInequality_GEQ_UNSAT_NEQ()
			throws NoSolutionException {
		try {
			_handler.addClause(_d, _e);
			_handler.addClause(_a, _b, _c);
			_handler.addClause(_d);
			_handler.addClause(_a.getOpposite());
			_handler.addClause(_b.getOpposite());
			_handler.addClause(_c);
			_handler.addClause(_d);
			_handler.addReifiedPBInequality(array(_a, _b, _c), Ineq.GEQ, 2, _d);
			_handler.endProblem();
			assertFalse(_modelReader.getNext());
		} catch (NoSolutionException e) {
		}
	}

	public void testReifiedConjunction() throws NoSolutionException {
		_handler.endProblem();
		_modelReader.getNext();
	}

	protected abstract void initHandler() throws Exception;

	protected abstract void destroyHandler() throws Exception;
}
