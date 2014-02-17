/* ./satmining-seq/src/main/java/dag/satmining/problem/seq/GSDomain.java

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

import java.util.ArrayList;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.Interpretation;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.output.PatternConverter;

/**
 * Domain of sequence patterns occuring in sequences of type E.
 *
 * @author ecoquery
 */
public class GSDomain<E,L extends Literal<L>> implements Constraint<L>, PatternConverter {

    private List<E> _data;
    private Alphabet<E,L> _lFactory;
    private List<LetterLiteral<E,L>> _domain;
    private CharSequence _sep;
    private int _size;

    public GSDomain(List<E> data, Alphabet<E,L> letterFactory, int patternMaxSize) {
        this._data = data;
        this._lFactory = letterFactory;
        this._sep = _lFactory.getSeparator();
        this._size = patternMaxSize;
    }
    
    private void buildDomain(PBBuilder<L> satHandler) throws NoSolutionException {
        _domain = new ArrayList<LetterLiteral<E,L>>(_size);
        for (int i = 0; i < _size; i++) {
            _domain.add(_lFactory.newLetterVariable(satHandler));
        }
    }

	public void addClauses(PBBuilder<L> satHandler) throws NoSolutionException {
        buildDomain(satHandler);
        L firstIsJoker = _domain.get(0).equivToJoker(satHandler);
        // The first element of the sequence pattern cannot be the joker.
        satHandler.addClause(firstIsJoker.getOpposite());
//                satHandler.getClauseFactory().newClause(firstIsJoker.getOpposite()));
    }

    public CharSequence getPattern(Interpretation model) {
        StringBuilder output = new StringBuilder();
        boolean started = false;
        int actualSize = 0;
        for(LetterLiteral<E,L> lv : _domain) {
            if(started) {
                output.append(_sep);
            } else {
                started = true;
            }
            output.append(lv.getPattern(model));
            if(!lv.isJoker(model)) {
                actualSize = output.length();
            }
        }
        output.setLength(actualSize);
        return output;
    }
    
    public LetterLiteral<E,L> getVariableAt(int index) {
        return _domain.get(index);
    }
    
    public int size() {
        return _size;
    }

    public List<E> getData() {
        return _data;
    }
}
