package dag.satmining.backend.minisat;

import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.pb.gen.NaivePBFactory;
import dag.satmining.constraints.ClauseBuilder;
import dag.satmining.constraints.PBBuilder;

public class NaiveEncodingTest extends MinisatBackendTest {

	@Override
	protected PBBuilder<DimacsLiteral> getPBBuilder(
			ClauseBuilder<DimacsLiteral> internalBuilder) throws Exception {
		return new NaivePBFactory<DimacsLiteral>(internalBuilder);
	}

}
