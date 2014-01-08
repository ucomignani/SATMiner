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
