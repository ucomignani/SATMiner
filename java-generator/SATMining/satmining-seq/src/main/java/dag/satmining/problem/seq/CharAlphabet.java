/* ./satmining-seq/src/main/java/dag/satmining/problem/seq/CharAlphabet.java

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

import static dag.satmining.constraints.formula.Formula.or;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.Interpretation;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

/**
 * 
 * @author ecoquery
 */
public class CharAlphabet<L extends Literal<L>> implements
		Alphabet<Character, L> {

	private Map<Character, Integer> _c2i = new HashMap<Character, Integer>();
	private StringBuilder _i2c = new StringBuilder();
	private char _joker = '*';

	private char getChar(int num) {
		if (num == _i2c.length()) {
			return _joker;
		} else {
			return _i2c.charAt(num);
		}
	}

	private int getJokerId() {
		return _i2c.length();
	}

	public LetterLiteral<Character, L> newLetterVariable(PBBuilder<L> handler)
			throws NoSolutionException {
		return new Variable(handler);
	}

	public List<Character> decode(Reader input) throws IOException {
		List<Character> result = new ArrayList<Character>();
		int i = input.read();
		while (i >= 0) {
			char c = (char) i;
			if (!_c2i.containsKey(c)) {
				_c2i.put(c, _i2c.length());
				_i2c.append(c);
			}
			result.add(c);
			i = input.read();
		}
		return result;
	}

	public CharSequence getSeparator() {
		return "";
	}

	private class Variable implements LetterLiteral<Character, L> {

		private L[] _domain;
		private L[] _matchChar;

		public Variable(PBBuilder<L> handler) throws NoSolutionException {
			_domain = handler.lArray(getJokerId() + 1);
			_matchChar = handler.lArray(getJokerId() + 1);
			for (int i = 0; i <= getJokerId(); i++) {
				_domain[i] = handler.newStrongLiteral();
			}
			// useless because of the next line
			// handler.addClause(_domain);
			handler.addExactlyOneTrue(_domain);
		}

		public L equivToMatch(Character value, PBBuilder<L> handler)
				throws NoSolutionException {
			int charId = _c2i.get(value);
			if (_matchChar[charId] == null) {
				_matchChar[charId] = or(_domain[charId], getJokerLiteral())
						.tseitinLit(handler);
			}
			return _matchChar[charId];
		}

		private L getJokerLiteral() {
			return _domain[_domain.length - 1];
		}

		public L equivToJoker(PBBuilder<L> handler) throws NoSolutionException {
			return getJokerLiteral();
		}

		public boolean isJoker(Interpretation i) {
			return i.getValue(getJokerLiteral());
		}

		public CharSequence getPattern(Interpretation model) {
			for (int i = 0; i < _domain.length; i++) {
				if (model.getValue(_domain[i])) {
					return String.valueOf(getChar(i));
				}
			}
			throw new IllegalArgumentException("Inconcsistent model");
		}
	}

	public String toString() {
		return "characters";
	}
}
