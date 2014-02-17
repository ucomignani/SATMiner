/* ./satmining-backend/src/test/java/dag/satmining/backend/boolvarpb/BoolvarPBBackendTest.java

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
