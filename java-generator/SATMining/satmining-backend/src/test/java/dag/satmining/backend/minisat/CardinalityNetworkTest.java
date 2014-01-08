package dag.satmining.backend.minisat;

import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.pb.gen.CardNetworksPBFactory;
import dag.satmining.constraints.ClauseBuilder;
import dag.satmining.constraints.PBBuilder;

public class CardinalityNetworkTest extends MinisatBackendTest {

	@Override
	protected PBBuilder<DimacsLiteral> getPBBuilder(
			ClauseBuilder<DimacsLiteral> internalBuilder) throws Exception {
		return new CardNetworksPBFactory<DimacsLiteral>(internalBuilder);
	}

}
