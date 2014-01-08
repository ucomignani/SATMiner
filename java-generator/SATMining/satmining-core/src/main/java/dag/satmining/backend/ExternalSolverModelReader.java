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
