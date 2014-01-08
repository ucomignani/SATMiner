package dag.satmining.backend.dimacs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import dag.satmining.NoSolutionException;
import dag.satmining.output.SolutionWriter;

public class MemoryDimacsBackend extends DimacsBackend {

	protected Collection<int[]> _clauses;
	
	public MemoryDimacsBackend() {
		_clauses = new ArrayList<int[]>();
	}
	
	@Override
	public void addClause(DimacsLiteral[] l) throws NoSolutionException {
		_clauses.add(DimacsLiteral.from(l));
	}

	@Override
	public void endProblem() throws NoSolutionException {
		// Nothing to do
	}
	
	public int[] getStrongBackdoorClause() {
		int[] res = new int[_strongBackdoorClause.size()];
		Iterator<Integer> it = _strongBackdoorClause.iterator();
		for(int i = 0; i < res.length; i++) {
			res[i] = it.next();
		}
		return res;
	}

	@Override
	public SolutionWriter getCNFWriter() {
		throw new UnsupportedOperationException("MemoryDimacsBackend.getCNFWriter()");
	}


}
