/* DataGenerator/src/main/java/fr/liris/bd/datagen/Main.java

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.github.jankroken.commandline.CommandLineParser;
import com.github.jankroken.commandline.OptionStyle;
import com.github.jankroken.commandline.annotations.LongSwitch;
import com.github.jankroken.commandline.annotations.Multiple;
import com.github.jankroken.commandline.annotations.Option;
import com.github.jankroken.commandline.annotations.Required;
import com.github.jankroken.commandline.annotations.ShortSwitch;
import com.github.jankroken.commandline.annotations.SingleArgument;
import com.github.jankroken.commandline.annotations.SubConfiguration;

import fr.liris.bd.datagen.output.CSVSQLOutput;
import fr.liris.bd.datagen.output.MultipleSQLOutput;
import fr.liris.bd.datagen.output.ScriptSQLOutput;

public class Main {

    private MultipleSQLOutput _output = new MultipleSQLOutput();

    private static OutputStream streamFromName(String name)
            throws FileNotFoundException {
        if (name == null || name.equals("<std>")) {
            return System.out;
        } else {
            return new FileOutputStream(new File(name));
        }
    }

    @Option
    @LongSwitch("csv")
    @SingleArgument
    public void setCSV(String fileName) throws FileNotFoundException {
        OutputStream os = streamFromName(fileName);
        _output.addOutput(new CSVSQLOutput(os));
    }

    @Option
    @LongSwitch("sql")
    @SingleArgument
    public void setSQL(String fileName) throws FileNotFoundException {
        OutputStream os = streamFromName(fileName);
        _output.addOutput(new ScriptSQLOutput(new PrintStream(os)));

    }

    private String _tableName;

    @Option
    @LongSwitch("table")
    @ShortSwitch("t")
    @SingleArgument
    @Required
    public void setTableName(String name) {
        _tableName = name;
    }

    private List<ColumnConfig> _columns = new ArrayList<ColumnConfig>();

    @Option
    @LongSwitch("column")
    @ShortSwitch("c")
    @Multiple
    @SubConfiguration(ColumnConfig.class)
    public void setColumns(List<ColumnConfig> columns) {
        _columns = columns;
    }

    private long _howMany = 0L;

    @Option
    @LongSwitch("nb")
    @SingleArgument
    @Required
    public void setHowMany(String howMany) {
        _howMany = Long.parseLong(howMany);
    }

    public Main() {
    }

    public void run(PrintStream out, PrintStream err)
            throws Exception {
        if (!_output.hasOutput()) {
            setCSV("<std>");
        }
        DataGen datagen = new DataGen(_tableName, _output);
        for (ColumnConfig c : _columns) {
            c.generateColumns(datagen);
        }
        datagen.generateSchema();
        datagen.generateInserts(_howMany);
        _output.close();
    }

    public void run() {
        try {
            run(System.out, System.err);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Main parseArgs(String[] args) throws IllegalAccessException,
            InstantiationException, InvocationTargetException {
        return CommandLineParser.parse(Main.class, args,
                OptionStyle.LONG_OR_COMPACT);
    }

    public static void main(String[] args) {
        try {
            Main main = parseArgs(args);
            main.run(System.out, System.err);
        } catch (Exception e) {
        }
    }
}
