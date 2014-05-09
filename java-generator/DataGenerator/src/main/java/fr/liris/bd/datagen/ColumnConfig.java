/* DataGenerator/src/main/java/fr/liris/bd/datagen/ColumnConfig.java

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

import com.github.jankroken.commandline.annotations.LongSwitch;
import com.github.jankroken.commandline.annotations.Option;
import com.github.jankroken.commandline.annotations.Required;
import com.github.jankroken.commandline.annotations.ShortSwitch;
import com.github.jankroken.commandline.annotations.SingleArgument;
import com.github.jankroken.commandline.annotations.Toggle;

import fr.liris.bd.datagen.generator.AutoIncrementFactory;
import fr.liris.bd.datagen.generator.AutoIncrementGenerator;
import fr.liris.bd.datagen.generator.DoubleGenerator;
import fr.liris.bd.datagen.generator.ValueGeneratorFactory;

public class ColumnConfig {

    private String _prefix;
    private String _suffix;
    private int _howmany = 0;
    private ValueGeneratorFactory _factory;
    
    @Option
    @LongSwitch("prefix")
    @ShortSwitch("p")
    @SingleArgument
    @Required
    public void setPrefix(String prefix) {
        _prefix = prefix;
    }

    @Option
    @LongSwitch("suffix")
    @ShortSwitch("s")
    @SingleArgument
    public void setSuffix(String suffix) {
        _suffix = suffix;
    }
    
    @Option
    @LongSwitch("ncols")
    @SingleArgument
    public void setHowMany(String howMany) {
        _howmany = Integer.parseInt(howMany);
    }
    
    @Option
    @LongSwitch("double")
    @ShortSwitch("d")
    @Toggle(true)
    public void setDouble(boolean flag) {
        _factory = new DoubleGenerator();
    }
    
    @Option
    @LongSwitch("autoi")
    @ShortSwitch("i")
    @Toggle(true)
    public void setAutoIncrementIsolated(boolean flag) {
        _factory = new AutoIncrementFactory();
    }
    
    @Option
    @LongSwitch("autos")
    @Toggle(true)
    public void setAutoIncrementShared(boolean flag) {
        _factory = new AutoIncrementGenerator();
    }
    
    public void generateColumns(DataGen datagen) {
        if (_factory == null) {
            _factory = new DoubleGenerator();
        }
        if (_howmany == 0) {
            datagen.addColumn(_prefix, _factory.getGenerator());
        } else {
            datagen.addColumns(_prefix, _suffix, _howmany, _factory);
        }
    }
}
