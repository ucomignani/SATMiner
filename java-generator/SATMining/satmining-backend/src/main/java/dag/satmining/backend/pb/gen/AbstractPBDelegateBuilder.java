package dag.satmining.backend.pb.gen;

import java.util.Collection;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.ClauseBuilder;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.output.SolutionWriter;

public abstract class AbstractPBDelegateBuilder<L extends Literal<L>> implements PBBuilder<L>{

	protected ClauseBuilder<L> _builder;
	
	public AbstractPBDelegateBuilder(ClauseBuilder<L> internalBuilder) {
		this._builder = internalBuilder;
	}

	public L newLiteral() {
		return _builder.newLiteral();
	}

	public L newStrongLiteral() {
		return _builder.newStrongLiteral();
	}

	public L newLiteral(boolean positive, boolean strong) {
		return _builder.newLiteral(positive, strong);
	}

	public void addReifiedConjunction(L equivalentTo, L[] lits)
			throws NoSolutionException {
		_builder.addReifiedConjunction(equivalentTo, lits);
	}

	public void addReifiedConjunction(L equivalentTo, Collection<L> lits)
			throws NoSolutionException {
		_builder.addReifiedConjunction(equivalentTo, lits);
	}

	public L[] lArray(int size) {
		return _builder.lArray(size);
	}

	public L[] lArray(int size, boolean filled) {
		return _builder.lArray(size, filled);
	}

	public L[][] lMatrix(int size, int size2, boolean filled) {
		return _builder.lMatrix(size, size2, filled);
	}

	public L[][] lMatrix(int size, int size2) {
		return _builder.lMatrix(size, size2);
	}

	public void addClause(L[] l) throws NoSolutionException {
		_builder.addClause(l);
	}

	public void addReifiedClause(L equivalentTo, L[] lits)
			throws NoSolutionException {
		_builder.addReifiedClause(equivalentTo, lits);
	}

	public void addClause(Collection<L> lits) throws NoSolutionException {
		_builder.addClause(lits);
	}

	public void addReifiedClause(L equivalentTo, Collection<L> lits)
			throws NoSolutionException {
		_builder.addReifiedClause(equivalentTo, lits);
	}

	public void addToStrongBackdoor(L l) {
		_builder.addToStrongBackdoor(l);
	}

	public void endProblem() throws NoSolutionException {
		_builder.endProblem();
	}

	public void unify(L[] lits) throws NoSolutionException {
		_builder.unify(lits);
	}

	public L fromDimacs(int dimacs) {
		return _builder.fromDimacs(dimacs);
	}

	public void addReifiedConjunction(L equivalentTo, L l1, L l2)
			throws NoSolutionException {
		_builder.addReifiedConjunction(equivalentTo, l1, l2);
	}

	public void addClause(L l) throws NoSolutionException {
		_builder.addClause(l);
	}

	public void addReifiedClause(L equivalentTo, L l1, L l2)
			throws NoSolutionException {
		_builder.addReifiedClause(equivalentTo, l1, l2);
	}

	public void addClause(L l1, L l2) throws NoSolutionException {
		_builder.addClause(l1, l2);
	}

	public void addReifiedClause(L equivalentTo, L l1, L l2, L l3)
			throws NoSolutionException {
		_builder.addReifiedClause(equivalentTo, l1, l2, l3);
	}

	public void addClause(L l1, L l2, L l3) throws NoSolutionException {
		_builder.addClause(l1, l2, l3);
	}

	public void unify(L l1, L l2) throws NoSolutionException {
		_builder.unify(l1, l2);
	}

	public SolutionWriter getCNFWriter() {
		return _builder.getCNFWriter();
	}
	
	
	
}
