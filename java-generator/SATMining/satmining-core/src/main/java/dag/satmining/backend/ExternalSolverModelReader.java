/* ./satmining-core/src/main/java/dag/satmining/backend/ExternalSolverModelReader.java

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

package dag.satmining.backend;

import dag.satmining.backend.FileModelReader;
import dag.satmining.backend.Interpretation;
import dag.satmining.backend.ModelReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ecoquery
 */
public class ExternalSolverModelReader implements ModelReader {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalSolverModelReader.class);
    private File _solverInput;
    private File _solverOutput;
    private FileModelReader _fileReader;
    private String[] _cmd;
    private boolean _started = false;

    public ExternalSolverModelReader(
            FileModelReader reader, File problemFile, String... cmd)
            throws IOException {
        this._fileReader = reader;
        this._solverInput = problemFile;
        this._solverOutput = File.createTempFile("satminer_ext_output", ".txt");
        this._cmd = Arrays.copyOf(cmd, cmd.length);
        for (int i = 0; i < _cmd.length; i++) {
            _cmd[i] = _cmd[i].replace("#in", _solverInput.getAbsolutePath()).replace("#out", _solverOutput.getAbsolutePath());
        }
    }

    public boolean getNext() {
        try {
            if (!_started) {
                run();
                _fileReader.open(_solverOutput);
            }
            if (_fileReader.getNext()) {
                return true;
            } else {
                _fileReader.close();
                return false;
            }
        } catch (IOException e) {
            LOG.warn("IO: {}",e.getLocalizedMessage());
            return false;
        }
    }

    public Interpretation getCurrentInterpretation() {
        return _fileReader.getCurrentInterpretation();
    }

    public void run() {
        try {
            LOG.info("Executing {} ...", Arrays.toString(_cmd));
            Process solverProcess = Runtime.getRuntime().exec(_cmd);
            try {
                solverProcess.waitFor();
            } catch (InterruptedException e) {
                LOG.warn("Interrupting solver", e);
                solverProcess.destroy();
                throw new RuntimeException(e);
            }
            LOG.info("External call terminated");
        } catch (IOException ex) {
            LOG.error("IO problem: {}"+ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        }
        _started = true;
    }
}
