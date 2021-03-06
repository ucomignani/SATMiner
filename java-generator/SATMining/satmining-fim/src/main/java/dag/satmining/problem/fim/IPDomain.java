/* ./satmining-fim/src/main/java/dag/satmining/problem/fim/IPDomain.java

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.Interpretation;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dag.satmining.output.PatternConverter;

/**
 *
 * @author ecoquery
 */
public class IPDomain<L extends Literal<L>> implements PatternConverter, Constraint<L> {

    private static final Logger LOG = LoggerFactory.getLogger(IPDomain.class);
    
    private L[] _items;
    private int _nbItems;
    
    public IPDomain(int nbItems) {
        this._nbItems = nbItems;
    }
    
	private void initVariablesArrays(ReifiedWeightedPBBuilder<L> h) throws NoSolutionException {
        LOG.debug("entering initVariablesArrays");
        _items = h.lArray(_nbItems);
        for(int i = 0; i < _nbItems; i++) {
            _items[i] = h.newStrongLiteral();
        }
        // The domain might be empty so no clause here.
        //h.addClause(_items);
    }
    
    @Override
    public void addClauses(ReifiedWeightedPBBuilder<L> satHandler) throws NoSolutionException {
        initVariablesArrays(satHandler);
    }

    public CharSequence getPattern(Interpretation model) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < _items.length; i++) {
            if (model.getValue(_items[i])) {
                sb.append(i);
                sb.append(' ');
            }
        }
        return sb;
    }
    
    public L getItLit(int id) {
        return _items[id];
    }
    
    public L getItNLit(int id) {
        return getItLit(id).getOpposite();
    }
    
    public int size() {
        return _nbItems;
    }
    
}
