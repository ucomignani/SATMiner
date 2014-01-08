package dag.satmining.output;

import java.io.IOException;


public interface SolutionWriter {
	void setOutput(String outputFile) throws IOException;
	void writeSolution(PatternConverter converter) throws IOException;
}
