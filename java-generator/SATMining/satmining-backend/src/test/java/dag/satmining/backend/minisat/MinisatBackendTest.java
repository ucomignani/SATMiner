/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.backend.minisat;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.backend.BackendTest;
import dag.satmining.backend.ExternalSolverModelReader;
import dag.satmining.backend.FileModelReader;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.dimacs.FileDimacsBackend;
import dag.satmining.constraints.ClauseBuilder;
import dag.satmining.constraints.PBBuilder;

/**
 *
 * @author ecoquery
 */
public abstract class MinisatBackendTest extends BackendTest<DimacsLiteral> {

    private static final Logger LOG = LoggerFactory.getLogger(MinisatBackendTest.class);
    protected File _cnfFile;
    private String _solverExe;
    private FileModelReader _fileReader;

    public MinisatBackendTest() {
        _solverExe = System.getProperty("smsat.minisat.cmd", "./minisat.sh");
    }

    @Override
    protected void initHandler() throws Exception {
        _cnfFile = File.createTempFile("satminer_junit_", ".cnf");
        LOG.info("Wrinting CNF to {}", _cnfFile);
        ClauseBuilder<DimacsLiteral> internalBuilder = new FileDimacsBackend(_cnfFile);
        _handler = getPBBuilder(internalBuilder);
        _fileReader = new MinisatModelReader();
        _modelReader = new ExternalSolverModelReader(
                _fileReader, _cnfFile, _solverExe, "#in", "-o", "#out");
    }

    @Override
    protected void destroyHandler() throws Exception {
        _fileReader.close();
        // _cnfFile.deleteOnExit();
        _cnfFile = null;
        _handler = null;
        _fileReader = null;
        _modelReader = null;
    }

    protected abstract PBBuilder<DimacsLiteral> getPBBuilder(ClauseBuilder<DimacsLiteral> internalBuilder) throws Exception;

}
