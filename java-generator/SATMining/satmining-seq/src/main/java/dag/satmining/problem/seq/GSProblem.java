/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.seq;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.mining.Generator;
import dag.satmining.constraints.mining.MinFrequency;
import dag.satmining.constraints.mining.UsageException;
import dag.satmining.output.PatternConverter;

/**
 *
 * @author ecoquery
 */
public class GSProblem<E,L extends Literal<L>> extends Generator<L> {

    private static final String FREQ_OPT = "f";
    private static final String SIZE_OPT = "m";
    private GSDomain<E,L> _domain;
    private GSSupport<E,L> _support;
    private Alphabet<E,L> _alphabet;
    private int _minFreq = -1;

    public GSProblem(Alphabet<E,L> alphabet) {
        this._alphabet = alphabet;
    }

    @Override
    public PatternConverter getPatternConverter() {
        return _domain;
    }

    @Override
    public void configure(Reader inputData, CommandLine cmd) throws IOException, UsageException {
        int maxSize = Integer.MAX_VALUE;
        if (cmd.hasOption(SIZE_OPT)) {
            maxSize = Integer.parseInt(cmd.getOptionValue(SIZE_OPT));
        }
        List<E> data = _alphabet.decode(inputData);
        maxSize = Math.min(maxSize, data.size());
        _domain = new GSDomain<E,L>(data, _alphabet, maxSize);
        addConstraint(_domain);
        _support = new GSSupport<E,L>(_domain);
        addConstraint(_support);
        if (cmd.hasOption(FREQ_OPT)) {
            addFreq(Integer.parseInt(cmd.getOptionValue(FREQ_OPT)));
        }
    }

    @Override
    public String getTitle() {
        return "sequence mining on " + _alphabet;
    }

    @SuppressWarnings("static-access")
	@Override
    public Options getOptions() {
        Options opts = new Options();
        opts.addOption(OptionBuilder.withArgName("freq").hasArg().isRequired().withDescription("sets the minimal occurences number to freq").create(FREQ_OPT));
        opts.addOption(OptionBuilder.withArgName("maxsize").hasArg().isRequired().withDescription("sets the maximal size of motifs to maxsize").create(SIZE_OPT));
        return opts;
    }

    public void addFreq(int minFreq) {
        addConstraint(new MinFrequency<L>(_support, minFreq));
        _minFreq = minFreq;
    }

    public enum Predefined {

        Char, CharSet
    }

    public static <L extends Literal<L>> Generator<L> newProblem(Class<L> litClazz, Predefined alphabetOption) {
        switch (alphabetOption) {
            case CharSet:
                return new GSProblem<Set<Character>,L>(new CharSetAlphabet<L>());
            case Char:
            default:
                return new GSProblem<Character,L>(new CharAlphabet<L>());
        }
    }
    
    public String toString() {
    	return "Sequence problem over alphabet "+_alphabet+" size: "+_support.size()+" minFreq: "+_minFreq;
    }
}
