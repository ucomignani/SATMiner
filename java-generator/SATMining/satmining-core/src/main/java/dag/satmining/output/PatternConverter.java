package dag.satmining.output;

import dag.satmining.backend.Interpretation;

public interface PatternConverter {

	/**
	 * Build a string representation of the pattern from the boolean model.
	 * @param model
	 * @return the representation of the model
	 */
	CharSequence getPattern(Interpretation model);
	
}
