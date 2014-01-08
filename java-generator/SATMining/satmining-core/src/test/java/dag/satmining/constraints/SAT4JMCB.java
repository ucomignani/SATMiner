package dag.satmining.constraints;

import java.util.Collection;

import junit.framework.TestCase;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.constraints.MinimalClauseBuilder;
import dag.satmining.output.SolutionWriter;

public abstract class SAT4JMCB extends TestCase implements MinimalClauseBuilder<DimacsLiteral> {

	private ISolver _solver;
	private int _lastVar = 0;
	private int _startingVars;

	public SAT4JMCB() {
		this(20);
	}

	public SAT4JMCB(int initSize) {
		_startingVars = initSize;
	}
	
	@Override
	protected void setUp() throws Exception {
		_solver = SolverFactory.newDefault();
		_solver.newVar(_startingVars);
		_lastVar = 0;
	}

	@Override
	protected void tearDown() throws Exception {
		_solver.reset();
		_solver = null;
		_lastVar = 0;
	}

	@Override
	public DimacsLiteral fromDimacs(int dimacs) {
		return new DimacsLiteral(dimacs);
	}

	@Override
	public DimacsLiteral newLiteral() {
		return newLiteral(true, false);
	}

	@Override
	public DimacsLiteral newStrongLiteral() {
		return newLiteral(true, true);
	}

	@SuppressWarnings("deprecation")
	@Override
	public DimacsLiteral newLiteral(boolean positive, boolean strong) {
		if (_lastVar >= _startingVars) {
			_solver.newVar();
		}
		DimacsLiteral l = new DimacsLiteral(++_lastVar);
		return positive ? l : l.getOpposite();
	}

	@Override
	public DimacsLiteral[] lArray(int size) {
		return new DimacsLiteral[size];
	}

	@Override
	public DimacsLiteral[] lArray(int size, boolean filled) {
		DimacsLiteral[] res = new DimacsLiteral[size];
		if (filled) {
			for (int i = 0; i < size; i++) {
				res[i] = newLiteral();
			}
		}
		return res;
	}

	@Override
	public DimacsLiteral[][] lMatrix(int size, int size2) {
		return new DimacsLiteral[size][size2];
	}

	@Override
	public DimacsLiteral[][] lMatrix(int size, int size2, boolean filled) {
		DimacsLiteral[][] res = new DimacsLiteral[size][];
		for (int i = 0; i < size; i++) {
			res[i] = lArray(size2, filled);
		}
		return res;
	}

	private static IVecInt from(DimacsLiteral[] lits) {
		int[] ilits = new int[lits.length];
		for (int i = 0; i < lits.length; i++) {
			ilits[i] = lits[i].intRepr();
		}
		return new VecInt(ilits);
	}

	@Override
	public void addClause(DimacsLiteral[] lits) throws NoSolutionException {
		try {
			_solver.addClause(from(lits));
		} catch (ContradictionException e) {
			throw new NoSolutionException(e);
		}
	}

	@Override
	public void addClause(DimacsLiteral l) throws NoSolutionException {
		addClause(new DimacsLiteral[] { l });
	}

	@Override
	public void addClause(DimacsLiteral l1, DimacsLiteral l2)
			throws NoSolutionException {
		addClause(new DimacsLiteral[] { l1, l2 });
	}

	@Override
	public void addClause(DimacsLiteral l1, DimacsLiteral l2, DimacsLiteral l3)
			throws NoSolutionException {
		addClause(new DimacsLiteral[] { l1, l2, l3 });
	}

	@Override
	public void addClause(Collection<DimacsLiteral> lits)
			throws NoSolutionException {
		addClause(lits.toArray(new DimacsLiteral[lits.size()]));
	}

	@Override
	public void addToStrongBackdoor(DimacsLiteral l) {
	}

	@Override
	public void endProblem() throws NoSolutionException {
	}

	@Override
	public void unify(DimacsLiteral[] lits) throws NoSolutionException {
		for(int i = 1; i < lits.length; i++) {
			unify(lits[0],lits[i]);
		}
	}

	@Override
	public void unify(DimacsLiteral l1, DimacsLiteral l2)
			throws NoSolutionException {
		addClause(l1.getOpposite(),l2);
		addClause(l1,l2.getOpposite());
	}

	@Override
	public SolutionWriter getCNFWriter() {
		return null;
	}
	
	public ISolver getSolver() {
		return _solver;
	}

}
