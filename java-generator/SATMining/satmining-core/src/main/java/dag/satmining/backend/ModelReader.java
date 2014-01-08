package dag.satmining.backend;

public interface ModelReader {

	boolean getNext();
	
	Interpretation getCurrentInterpretation();
	
}
