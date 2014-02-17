/* ./satmining-seq/src/main/java/dag/satmining/problem/seq/GSProblem.java

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
