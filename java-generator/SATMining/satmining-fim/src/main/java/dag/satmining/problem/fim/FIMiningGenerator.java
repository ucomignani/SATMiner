/* ./satmining-fim/src/main/java/dag/satmining/problem/fim/FIMiningGenerator.java

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
package dag.satmining.problem.fim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.mining.Generator;
import dag.satmining.constraints.mining.MinFrequency;
import dag.satmining.constraints.mining.UsageException;
import dag.satmining.output.PatternConverter;

/**
 * Implementation of Luc De Raedt, Tias Guns and Siegfried Nijssen's Constraint
 * Programming for Itemset Mining (SIGKDD 2008).
 *
 * @author ecoquery
 */
public class FIMiningGenerator<L extends Literal<L>> extends Generator<L> {
    public static final String CLOSED_OPT = "closed";

    public static final String FREQ_OPT = "f";
    public static final String MAXIMAL_OPT = "maximal";
    private final static Logger LOG = LoggerFactory.getLogger(FIMiningGenerator.class);
    private IPDomain<L> _domain;
    private IPSupport<L> _support;

    public FIMiningGenerator(int nbItems, List<BitSet> data) {
        init(nbItems, data);
    }

    public FIMiningGenerator() {
    }

    private void init(int nbItems, List<BitSet> data) {
        _domain = new IPDomain<L>(nbItems);
        _support = new IPSupport<L>(_domain, data);
        addConstraint(_domain);
        addConstraint(_support);
    }

    private void parseAndInit(Reader input) throws IOException {
        BufferedReader br;
        if (input instanceof BufferedReader) {
            br = (BufferedReader) input;
        } else {
            br = new BufferedReader(input);
        }
        List<BitSet> transactions = new ArrayList<BitSet>();
        BitSet current = new BitSet();
        int biggestItem = 0;
        int item;
        String line;
        boolean found;
        do {
            line = br.readLine();
            if (line != null) {
                int sz = line.length();
                item = 0;
                char c;
                found = false;
                for (int i = 0; i < sz; i++) {
                    c = line.charAt(i);
                    if (c >= '0' && c <= '9') {
                        item = item * 10 + c - '0';
                        found = true;
                    } else {
                        if (found) {
                            current.set(item);
                            biggestItem = Math.max(biggestItem, item);
                            LOG.debug("added {}", item);
                        }
                        item = 0;
                        found = false;
                    }
                }
                if (found) {
                    current.set(item);
                    biggestItem = Math.max(biggestItem, item);
                    LOG.debug("added {}", item);
                }
                transactions.add(current);
                current = new BitSet();
            }
        } while (line != null);
        input.close();
        init(biggestItem + 1, transactions);
    }

    public IPDomain<L> getDomain() {
        return _domain;
    }

    public IPSupport<L> getSupport() {
        return _support;
    }

    @Override
    public PatternConverter getPatternConverter() {
        return getDomain();
    }

    @Override
    public void configure(Reader inputData, CommandLine opts)
            throws IOException, UsageException {
        parseAndInit(inputData);
        if (opts.hasOption(FREQ_OPT)) {
            addConstraint(new MinFrequency<L>(
                    _support, Integer.parseInt(opts.getOptionValue(FREQ_OPT))));
        }
        if (opts.hasOption(CLOSED_OPT)) {
            addConstraint(new IPClosed<L>(_domain,_support));
        }
        if (opts.hasOption(MAXIMAL_OPT)) {
            if (!opts.hasOption(FREQ_OPT)) {
                throw new UsageException("-" + FREQ_OPT + 
                        " required when using -" + MAXIMAL_OPT);
            }
            addConstraint(new IPMaximal<L>(_domain, _support,
                    Integer.parseInt(opts.getOptionValue(FREQ_OPT))));
        }
    }

    @Override
    public String getTitle() {
        return "frequent itemset mining";
    }

    @SuppressWarnings("static-access")
	@Override
    public Options getOptions() {
        Options opts = new Options();
        opts.addOption(OptionBuilder.hasArg().withArgName("freq").withDescription("requires the pattern to appear at least in freq transactions").create(FREQ_OPT));
        opts.addOption(OptionBuilder.withDescription("output only closed patterns").create(CLOSED_OPT));
        opts.addOption(OptionBuilder.withDescription("output only maximal patterns").create(MAXIMAL_OPT));
        return opts;
    }
}
