package dag.satmining.backend;

import dag.satmining.constraints.Literal;

public interface Interpretation {
	
	boolean getValue(Literal<?> lit);
	
}
