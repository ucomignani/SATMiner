/**
 * 
 */
package dag.satmining.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import dag.satmining.backend.ModelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ecoquery
 * 
 */
public class FileModelOutput implements GeneratorOutput {

    private final static Logger LOG = LoggerFactory.getLogger(FileModelOutput.class);
    
	private PatternConverter _converter;
	private ModelReader _models;
	private PrintWriter _output;

	public FileModelOutput(PatternConverter converter, ModelReader models,
			Writer output) {
		this._converter = converter;
		this._models = models;
		if (output instanceof PrintWriter) {
			this._output = (PrintWriter) output;
		} else {
			this._output = new PrintWriter(output);
		}
	}

	public FileModelOutput(PatternConverter domain, ModelReader models,
			OutputStream output) {
		this(domain, models, new PrintWriter(output));
	}

	public FileModelOutput(PatternConverter domain, ModelReader models,
			File outputFile) throws FileNotFoundException {
		this(domain, models, new PrintWriter(outputFile));
	}

	public FileModelOutput(PatternConverter domain, ModelReader models,
			String outputFile) throws FileNotFoundException {
		this(domain, models, writerFromName(outputFile));
	}
    
    private static PrintWriter writerFromName(String name) throws FileNotFoundException {
        if (name == null || "".equals(name) || "-".equals(name)) {
            return new PrintWriter(System.out);
        } else {
            return new PrintWriter(name);
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.cnrs.liris.dag.smsat.output.GeneratorOutput#writeOutput()
	 */
	public void writeOutput() throws IOException {
        LOG.info("Converting models to patterns ...");
		while(_models.getNext()) {
            CharSequence res = _converter.getPattern(_models.getCurrentInterpretation());
            LOG.debug("Found pattern {}",res);
			_output.println(res);
		}
		_output.close();
	}
	
}
