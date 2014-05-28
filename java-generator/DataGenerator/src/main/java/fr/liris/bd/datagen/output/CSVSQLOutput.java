/* DataGenerator/src/main/java/fr/liris/bd/datagen/output/CSVSQLOutput.java

   Copyright (C) 2014 Emmanuel Coquery.

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

package fr.liris.bd.datagen.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVSQLOutput extends AbstractSQLOutput {
    
    private Writer _outputW;
    private CSVWriter _csv;
    
    public CSVSQLOutput(Writer out) {
        _outputW = out;
        _csv = new CSVWriter(_outputW);
    }
    
    public CSVSQLOutput(OutputStream out) {
        this(new OutputStreamWriter(out));
    }
    
    @Override
    public void createTable() {
        String [] nextLine = new String[_specs.size()];
        for(int i = 0; i < _specs.size(); ++i) {
            nextLine[i] = _specs.get(i).getName();
        }
        _csv.writeNext(nextLine);
    }

    @Override
    public void execInsert(List<Object> data) {
        String [] nextLine = new String[data.size()];
        for(int i = 0; i < data.size(); ++i) {
            nextLine[i] = data.get(i).toString();
        }
        _csv.writeNext(nextLine);
    }

    @Override
    public void close() throws IOException {
        _csv.close();
        _outputW.close();
    }
    
}
