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
