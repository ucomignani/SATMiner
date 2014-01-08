package dag.satmining.backend.sat4j;

import java.util.BitSet;
import java.util.Collection;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.restarts.MiniSATRestarts;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.constraints.PBMaxDataStructure;
import org.sat4j.pb.core.PBDataStructureFactory;
import org.sat4j.pb.core.PBSolverResolution;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.Interpretation;
import dag.satmining.backend.ModelReader;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.constraints.impl.AbstractClauseBuilder;
import dag.satmining.constraints.impl.PBReifier;
import dag.satmining.constraints.impl.PBReifier.WeightedPBBuilder;
import dag.satmining.output.SolutionWriter;

public class SAT4JPBBuilder extends AbstractClauseBuilder<DimacsLiteral>
		implements PBBuilder<DimacsLiteral>, WeightedPBBuilder<DimacsLiteral>,
		ModelReader, Interpretation {

	private static final Logger LOG = LoggerFactory
			.getLogger(SAT4JPBBuilder.class);
	public static final int SMALL = 20;
	public static final int LARGE = 100000;
	private IPBSolver _solver;
	private int _lastNewVar = 0;
	private BitSet _strongBackdoor;
	private IVecInt _strongBackdoorList;
	private StrongBackdoorVarOrderHeap _varOrder;
	private int _sat4jInitNbVar;
	private PBReifier<DimacsLiteral> _reifier;
	private ModelIterator _iteratorOnSolver;
	private boolean _noMoreModels;
	private boolean _started;

	public SAT4JPBBuilder(int initNbVar) {
		_reifier = new PBReifier<DimacsLiteral>(this);
		_sat4jInitNbVar = initNbVar;
		_strongBackdoor = new BitSet();
		_strongBackdoorList = new VecInt();
		initSolver();
		_started = false;
		_noMoreModels = false;
	}

	private void initSolver() {
		// Taken from SolverFactory.PBSolverWithImpliedClause() to use
		// specialized order for strong backdoor.
		_varOrder = new StrongBackdoorVarOrderHeap(_strongBackdoor);
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverResolution solver = new PBSolverResolution(learning,
				new PBMaxDataStructure(), _varOrder, new MiniSATRestarts());
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		_solver = solver;
		_solver.newVar(_sat4jInitNbVar);
		_iteratorOnSolver = new ModelIterator(_solver);
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

	@Override
	public DimacsLiteral newLiteral(boolean positive, boolean strong) {
		int varId = createVar();
		DimacsLiteral l = new DimacsLiteral(positive ? varId : -varId);
		if (strong) {
			addToStrongBackdoor(l);
		}
		return l;
	}

	private int createVar() {
		++_lastNewVar;
		if (_lastNewVar > _sat4jInitNbVar) {
			_lastNewVar = _solver.nextFreeVarId(true);
		}
		return _lastNewVar;
	}

	private static IVecInt iVectIntFrom(DimacsLiteral[] lits) {
		VecInt vi = new VecInt(lits.length);
		for (DimacsLiteral l : lits) {
			vi.push(l.intRepr());
		}
		return vi;
	}

	@Override
	public void addClause(DimacsLiteral[] l) throws NoSolutionException {
		try {
			_solver.addClause(iVectIntFrom(l));
		} catch (ContradictionException e) {
			throw new NoSolutionException(e);
		}
	}

	@Override
	public void addToStrongBackdoor(DimacsLiteral l) {
		int v = l.getVariableId();
		if (!_strongBackdoor.get(v)) {
			_strongBackdoor.set(v);
			_strongBackdoorList.push(v);
		}
	}

	@Override
	public void endProblem() throws NoSolutionException {
		// Do nothing
	}

	@Override
	public void addWPBInequality(DimacsLiteral[] lits, int[] coefs, Ineq ineq,
			int value) throws NoSolutionException {
		try {
			switch (ineq) {
			case GEQ:
				_solver.addAtLeast(iVectIntFrom(lits), new VecInt(coefs), value);
				break;
			case LEQ:
				_solver.addAtMost(iVectIntFrom(lits), new VecInt(coefs), value);
				break;
			case EQ:
				_solver.addExactly(iVectIntFrom(lits), new VecInt(coefs), value);
			default:
				throw new Error("bug in " + getClass().getSimpleName()
						+ " unknown ineq " + ineq);
			}
		} catch (ContradictionException e) {
			throw new NoSolutionException(e);
		}
	}

	@Override
	public void addPBInequality(DimacsLiteral[] lits, Ineq ineq, int value)
			throws NoSolutionException {
		try {
			switch (ineq) {
			case GEQ:
				_solver.addAtLeast(iVectIntFrom(lits), value);
				break;
			case LEQ:
				_solver.addAtMost(iVectIntFrom(lits), value);
				break;
			case EQ:
				_solver.addExactly(iVectIntFrom(lits), value);
				break;
			default:
				throw new Error("bug in " + getClass().getSimpleName()
						+ " unknown ineq " + ineq);
			}
		} catch (ContradictionException e) {
			throw new NoSolutionException(e);
		}
	}

	@Override
	public void addPBInequality(Collection<DimacsLiteral> lits, Ineq ineq,
			int value) throws NoSolutionException {
		addPBInequality(lits.toArray(new DimacsLiteral[lits.size()]), ineq,
				value);
	}

	@Override
	public void addReifiedPBInequality(DimacsLiteral[] lits, Ineq ineq,
			int value, DimacsLiteral equivalentTo) throws NoSolutionException {
		_reifier.addReifiedIneqality(lits, ineq, value, equivalentTo);
	}

	@Override
	public void addReifiedPBInequality(Collection<DimacsLiteral> lits,
			Ineq ineq, int value, DimacsLiteral equivalentTo)
			throws NoSolutionException {
		addReifiedPBInequality(lits.toArray(new DimacsLiteral[lits.size()]),
				ineq, value, equivalentTo);
	}

	@Override
	public void addExactlyOneTrue(Collection<DimacsLiteral> lits)
			throws NoSolutionException {
		addPBInequality(lits, Ineq.EQ, 1);
	}

	@Override
	public void addExactlyOneTrue(DimacsLiteral[] lits)
			throws NoSolutionException {
		addPBInequality(lits, Ineq.EQ, 1);
	}

	@Override
	public boolean getNext() {
		if (_noMoreModels) {
			return false;
		}
		try {
			if (_started) {
				forbidCurrentModel();
			} else {
				_started = true;
			}
			LOG.debug("Finding a new model");
			boolean isSatisfiable = _iteratorOnSolver.isSatisfiable();
			LOG.debug("Found model: {}; {} models in total", isSatisfiable,
					_iteratorOnSolver.numberOfModelsFoundSoFar());
			_noMoreModels = !isSatisfiable;
		} catch (ContradictionException e) {
			_noMoreModels = true;
		} catch (TimeoutException e) {
			LOG.error("SAT4J timed out ...", e);
			_noMoreModels = true;
		}
		return !_noMoreModels;
	}

	private void forbidCurrentModel() throws ContradictionException {
		IVecInt nogoodForIterate = new VecInt(_strongBackdoorList.size(), 0);
		for (int i = 0; i < _strongBackdoorList.size(); i++) {
			int var = _strongBackdoorList.get(i);
			int lit = _iteratorOnSolver.model(var) ? -var : var;
			nogoodForIterate.set(i, lit);
		}
		LOG.debug("Solver has {} clauses", _iteratorOnSolver.nConstraints());
		_iteratorOnSolver.addBlockingClause(nogoodForIterate);
		// _solver.discardCurrentSolution();
		LOG.debug(
				"Discarted current solution: {}. Now has {} constraints. Found {} solutions",
				new Object[] { nogoodForIterate,
						_iteratorOnSolver.nConstraints(),
						_iteratorOnSolver.numberOfModelsFoundSoFar() });
	}

	@Override
	public Interpretation getCurrentInterpretation() {
		return _noMoreModels ? null : this;
	}

	@Override
	public boolean getValue(Literal<?> lit) {
		return (!lit.isPositive())
				^ _iteratorOnSolver.model(lit.getVariableId());
	}

	@Override
	public DimacsLiteral[] lArray(int size) {
		return new DimacsLiteral[size];
	}

	@Override
	public DimacsLiteral[][] lMatrix(int size, int size2) {
		return new DimacsLiteral[size][size2];
	}

	@Override
	protected DimacsLiteral[] toArray(Collection<DimacsLiteral> c) {
		return c.toArray(new DimacsLiteral[c.size()]);
	}

	@Override
	public SolutionWriter getCNFWriter() {
		throw new UnsupportedOperationException("SAT4J backend does generate dimacs output");
	}

}
