/* ./satmining-seq/src/main/java/dag/satmining/problem/seq/SequenceMiningGenerator1.java

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

package dag.satmining.problem.seq;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.mining.Generator;
import dag.satmining.constraints.mining.MinFrequency;
import dag.satmining.constraints.mining.UsageException;
import dag.satmining.output.PatternConverter;

/**
 * A class for generating the SAT problem for a given input. Based on the first
 * version of the generator.
 *
 * @author ecoquery
 *
 */
public class SequenceMiningGenerator1<L extends Literal<L>> extends Generator<L> {

    public static final String CLOSED_OPT = "closed";
    public static final String FREQ_OPT = "f";
    public static final String SIZE_OPT = "m";
    public static final String MAXIMAL_OPT = "maximal";
    public static final String MAX_JOKS_OPTS = "maxJoks";
    public static final String REGEXP_OPT = "regexp";
    /**
     * Mapping from positions to character id.
     */
    private int[] _intContent;
    /**
     * Mapping from character id to actual character
     */
    private StringBuffer _alphabet;
    /**
     * maximal pattern size
     */
    private int _maxSize = 0;
    /**
     * The number of possible values for a position in the pattern.
     */
    private int _nbValues;
    /**
     * The joker's id
     */
    private int _joker;
    /**
     * The constraint for representing the domain
     */
    private SPDomain<L> _domain;
    /**
     * The constraint for representing support
     */
    private SPSupport<L> _support;
    /**
     * The minimal frequency required for patterns.
     */
    private int _minFreq;

    /**
     * Build a generator from the given input
     *
     * @param contentReader a reader providing the input data
     * @param maxSize the maximal size for a pattern
     * @throws IOException if a problem occurs while reading contentReader
     */
    public SequenceMiningGenerator1() throws IOException {
    }

    private void read(Reader contentReader) throws IOException {
        ArrayList<Integer> intContent = new ArrayList<Integer>();
        _alphabet = new StringBuffer();
        HashMap<Character, Integer> alphabetPositions = new HashMap<Character, Integer>();
        int lu;
        do {
            lu = contentReader.read();
            if (lu != -1) {
                char c = (char) lu;
                if (!alphabetPositions.containsKey(c)) {
                    int newCharIdx = _alphabet.length();
                    alphabetPositions.put(c, newCharIdx);
                    intContent.add(newCharIdx);
                    _alphabet.append(c);
                } else {
                    intContent.add(alphabetPositions.get(c));
                }
            }
        } while (lu != -1);
        int contentLength = intContent.size();
        if (_maxSize == 0) {
            _maxSize = Integer.MAX_VALUE;
        }
        _maxSize = Math.min(_maxSize, contentLength);
        _alphabet.append('*');
        _nbValues = _alphabet.length();
        _joker = _nbValues - 1;
        alphabetPositions.put('*', _joker);
        _domain = new SPDomain<L>(_maxSize, _alphabet, alphabetPositions);
        addConstraint(_domain);
        _intContent = new int[contentLength];
        for (int i = 0; i < contentLength; i++) {
            _intContent[i] = intContent.get(i);
        }
        _support = new SPSupport<L>(_domain, _intContent);
        addConstraint(_support);
    }

    /**
     * The id of the joker.
     *
     * @return the id of the joker.
     */
    public final int getJoker() {
        return _joker;
    }

    /**
     * The number of letters found in the input data.
     *
     * @return the number of possible values for a pattern letter.
     */
    public final int getNbValues() {
        return _nbValues;
    }

    /**
     * The configured maximal size of a pattern.
     *
     * @return the maximal size.
     */
    public final int getMaxSize() {
        return _maxSize;
    }

    /**
     * The SAT variables used to represent the pattern values.
     *
     * @return an array indexed by position in patterns containing an array of
     * SAT variables, one by letter plus the joker.
     */
    public final L[][] getPatternVariables() {
        return _domain.getVariables();
    }

    /**
     * Mapping from character id to actual character
     *
     * @return
     */
    public final CharSequence getMapping() {
        return _alphabet;
    }

    /**
     * The domain constraint associated to this problem.
     *
     * @return
     */
    public final SPDomain<L> getDomain() {
        return _domain;
    }

    /**
     * The content as an array of char ids.
     *
     * @return the content as an array of char ids.
     */
    public final int[] getContent() {
        return _intContent;
    }

    @Override
    public PatternConverter getPatternConverter() {
        return getDomain();
    }

    @Override
    public void configure(Reader inputData, CommandLine cmd) 
            throws UsageException, IOException {
        if (cmd.hasOption(SIZE_OPT)) {
            _maxSize = Integer.parseInt(cmd.getOptionValue(SIZE_OPT));
        }
        read(inputData);
        if (cmd.hasOption(FREQ_OPT)) {
            addFreq(Integer.parseInt(cmd.getOptionValue(FREQ_OPT)));
        }
        if (cmd.hasOption(CLOSED_OPT)) {
            addClosed();
        }
        if (cmd.hasOption(MAXIMAL_OPT)) {
            addMaximal();
        }
        if (cmd.hasOption(MAX_JOKS_OPTS)) {
            addMaxJoks(Integer.parseInt(cmd.getOptionValue(MAX_JOKS_OPTS)));
        }
        if (cmd.hasOption(REGEXP_OPT)) {
            addRegexp(cmd.getOptionValue(REGEXP_OPT));
        }
    }

    @SuppressWarnings("static-access")
	public Options getOptions() {
        Options opts = new Options();
        opts.addOption(OptionBuilder.withArgName("freq").hasArg().isRequired().withDescription("sets the minimal occurences number to freq").create(FREQ_OPT));
        opts.addOption(OptionBuilder.withArgName("maxsize").hasArg().isRequired().withDescription("sets the maximal size of motifs to maxsize").create(SIZE_OPT));
        opts.addOption(OptionBuilder.withDescription("only output sequences closed w.r.t. inclusion and positions").create(CLOSED_OPT));
        opts.addOption(OptionBuilder.withDescription("only output maximal sequences").create(MAXIMAL_OPT));
        opts.addOption(OptionBuilder.withArgName("max").hasArg().withDescription("only output sequences with atmost max consecutive jokers").create(MAX_JOKS_OPTS));
        opts.addOption(OptionBuilder.withArgName("expr").hasArg().withDescription("only output sequences matching the regular expression expr").create(REGEXP_OPT));
        return opts;
    }

    private void addFreq(int minFreq) {
        addConstraint(new MinFrequency<L>(_support, minFreq));
        _minFreq = minFreq;
    }

    private void addClosed() {
        addConstraint(new SPClosed<L>(_domain, _support));
    }

    private void addMaximal() {
        addConstraint(new SPMaximal<L>(_domain, _support, _minFreq));
    }

    private void addMaxJoks(int maxJoks) {
        addConstraint(new SPMaxConsecutiveJokers<L>(_domain, maxJoks));
    }

    private void addRegexp(String regexp) {
        addConstraint(new SPRegExp<L>(_domain, regexp));
    }

    @Override
    public String getTitle() {
        return "frequent sequence mining";
    }
}
