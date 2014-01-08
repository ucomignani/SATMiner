package dag.satmining.backend.boolvarpb;

import dag.satmining.backend.BackendTest;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.sat4j.SAT4JPBBuilder;

public class BoolvarPBWrapperTest  extends BackendTest<BVLiteral> {

	private SAT4JPBBuilder _sat4j;
	
	@Override
	protected void initHandler() throws Exception {
		_sat4j = new SAT4JPBBuilder(SAT4JPBBuilder.SMALL); 
		_handler = new BVPBWrapper<DimacsLiteral>(_sat4j);
		_modelReader = _sat4j;
	}

	@Override
	protected void destroyHandler() throws Exception {
		_sat4j = null;
		_handler = null;
		_modelReader = null;
	}

}
