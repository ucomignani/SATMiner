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
