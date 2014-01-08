package dag.satmining.backend.boolvarpb;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.backend.BackendTest;
import dag.satmining.backend.ExternalSolverModelReader;
import dag.satmining.backend.FileModelReader;
import dag.satmining.backend.minisat.MinisatModelReader;

public class BoolvarPBBackendTest extends BackendTest<BVLiteral>{

    private static final Logger LOG = LoggerFactory.getLogger(BoolvarPBBackendTest.class);
    protected File _cnfFile;
    private String _solverExe = "./minisat.sh";
    private FileModelReader _fileReader;

	@Override
	protected void initHandler() throws Exception {
        _cnfFile = File.createTempFile("satminer_junit_", ".cnf");
        LOG.info("Wrinting CNF to {}", _cnfFile);
        BVPBBuilder builder = new BVPBBuilder();
        builder.setOutput(_cnfFile.getAbsolutePath());
        _handler = builder;
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

}
