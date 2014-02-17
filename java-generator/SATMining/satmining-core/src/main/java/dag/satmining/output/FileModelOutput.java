/* ./satmining-core/src/main/java/dag/satmining/output/FileModelOutput.java

   Copyright (C) 2013, 2014 Emmanuel Coquery.

This file is part of SATMiner

SATMiner is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

SATMiner is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with SATMiner; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

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
