/* DataGenerator/src/main/java/fr/liris/bd/datagen/DataGen.java

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

package fr.liris.bd.datagen;

import java.util.ArrayList;
import java.util.List;

import fr.liris.bd.datagen.generator.AutoIncrementFactory;
import fr.liris.bd.datagen.generator.AutoIncrementGenerator;
import fr.liris.bd.datagen.generator.DoubleGenerator;
import fr.liris.bd.datagen.generator.ValueGenerator;
import fr.liris.bd.datagen.generator.ValueGeneratorFactory;
import fr.liris.bd.datagen.output.ColumnSpec;
import fr.liris.bd.datagen.output.SQLOutput;

public class DataGen {

    private List<ColumnSpec> _specs = new ArrayList<ColumnSpec>();
    private SQLOutput _output;

    public DataGen(String tableName, SQLOutput output) {
        _output = output;
        _output.setTableName(tableName);
    }

    public void generateSchema() {
        _output.setAttSpec(_specs);
        _output.createTable();
    }

    public void generateInserts(long howMany) {
        for (long i = 0; i < howMany; ++i) {
            _output.execInsert();
        }
    }

    public void addColumn(String name, ValueGenerator generator) {
        _specs.add(new ColumnSpec(name, generator));
    }

    public void addColumns(String prefix, String suffix, int howMany,
            ValueGeneratorFactory factory) {
        if (suffix == null) {
            suffix = "";
        }
        int size = (int) Math.ceil(Math.log10(howMany + 1));
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= howMany; ++i) {
            sb.delete(0, sb.length());
            sb.append(prefix);
            int sizeI = (int) Math.ceil(Math.log10(i + 1));
            int nbZero = size-sizeI;
            for(int j = 0; j < nbZero; ++j) {
                sb.append('0');
            }
            sb.append(i);
            sb.append(suffix);
            addColumn(sb.toString(), factory.getGenerator());
        }
    }

    public void addDoubleColumns(String prefix, String suffix, int howMany) {
        addColumns(prefix, suffix, howMany, new DoubleGenerator());
    }

    public void addAutoIncrementColumn(String name) {
        addColumn(name, new AutoIncrementGenerator());
    }

    public void addAutoIncrementColumns(String prefix, String suffix,
            int howMany, boolean sharedSequence) {
        addColumns(prefix, suffix, howMany,
                sharedSequence ? new AutoIncrementGenerator()
                        : new AutoIncrementFactory());
    }
}
