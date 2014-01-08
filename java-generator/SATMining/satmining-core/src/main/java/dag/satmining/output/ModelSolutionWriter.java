package dag.satmining.output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import dag.satmining.backend.ModelReader;

public class ModelSolutionWriter implements SolutionWriter {

	private ModelReader _reader;
	private OutputStream _output;

	public ModelSolutionWriter(ModelReader _reader) {
		super();
		this._reader = _reader;
	}

	@Override
	public void setOutput(String outputFile) throws IOException {
		if ("-".equals(outputFile)) {
			this._output = System.out;
		} else {
			this._output = new FileOutputStream(outputFile);
		}
	}

	@Override
	public void writeSolution(PatternConverter converter) throws IOException {
		PrintWriter writer = new PrintWriter(_output);
		while (_reader.getNext()) {
			writer.println(converter.getPattern(_reader
					.getCurrentInterpretation()));
		}
		writer.close();
	}

}
