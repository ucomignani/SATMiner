/* ./satmining-core/src/main/java/dag/satmining/output/ModelSolutionWriter.java

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

package dag.satmining.output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import dag.satmining.backend.ExternalSolverModelReader;
import dag.satmining.backend.ModelReader;

public class ModelSolutionWriter implements SolutionWriter, Limitable {

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

    @Override
    public void setLimit(long max) {
        _reader.setLimit(max);
    }

    public void setTimeout(long max) {
        if (_reader instanceof ExternalSolverModelReader) {
            ((ExternalSolverModelReader) _reader).setTimeout(max);
        } else {
            throw new IllegalStateException("Cannot set timeout on "+_reader.getClass());
        }
    }

}
