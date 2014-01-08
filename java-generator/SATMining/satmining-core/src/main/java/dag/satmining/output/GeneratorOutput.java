package dag.satmining.output;

import java.io.IOException;

/**
 * An object to write the output of a problem.
 * @author ecoquery
 *
 */
public interface GeneratorOutput {

	/**
	 * Writes the problem to the output.
	 * @throws IOException if an I/O problem occurs.
	 */
	void writeOutput() throws IOException;

}
