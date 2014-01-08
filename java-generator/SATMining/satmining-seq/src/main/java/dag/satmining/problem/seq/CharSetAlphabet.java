/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.seq;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.Interpretation;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

/**
 *
 * @author ecoquery
 */
public class CharSetAlphabet<L extends Literal<L>> implements Alphabet<Set<Character>,L> {

    private String _sep = ",";
    private Map<Character, Integer> _c2i = new HashMap<Character, Integer>();
    private StringBuilder _i2c = new StringBuilder();
    private Map<Set<Character>, HashSet<Character>> _setCache =
            new HashMap<Set<Character>, HashSet<Character>>();

    private int getCharId(char c) {
        if (!_c2i.containsKey(c)) {
            _c2i.put(c, _i2c.length());
            _i2c.append(c);
        }
        return _c2i.get(c);
    }

    private char getChar(int id) {
        return _i2c.charAt(id);
    }

    private Set<Character> getEquivSet(Set<Character> set) {
        HashSet<Character> hs = new HashSet<Character>(set);
        Set<Character> res = _setCache.get(hs);
        if (res == null) {
            res = hs;
            _setCache.put(hs, hs);
        }
        return res;
    }

    public LetterLiteral<Set<Character>,L> newLetterVariable(PBBuilder<L> handler) throws NoSolutionException {
        return new Variable(handler);
    }

    public List<Set<Character>> decode(Reader input) throws IOException {
        int sepIdx = 0;
        int i = input.read();
        List<Set<Character>> list = new ArrayList<Set<Character>>();
        Set<Character> current = new HashSet<Character>();
        while (i != -1) {
            char c = (char) i;
            if (_sep.charAt(sepIdx) == c) {
                sepIdx++;
                if (sepIdx == _sep.length()) {
                    sepIdx = 0;
                    list.add(getEquivSet(current));
                    current = new HashSet<Character>();
                }
            } else {
                // If sep was started to be recognized, we need to add the 
                // begining up to the place where it can be recognized again, 
                // which will usually be the start of the string
                String readData = _sep.substring(0, sepIdx) + c;
                int eaten = 0;
                while (!_sep.startsWith(readData.substring(eaten))) {
                    char toEat = readData.charAt(eaten);
                    getCharId(toEat); // ensure that the char is mapped
                    current.add(toEat);
                    eaten++;
                }
                sepIdx = sepIdx - eaten + 1;
            }
            i = input.read();
        }
        if (!current.isEmpty()) {
            list.add(getEquivSet(current));
        }
        return list;
    }

    public CharSequence getSeparator() {
        return _sep;
    }

    private class Variable implements LetterLiteral<Set<Character>,L> {

        private L[] _domainNeg;
        private Map<Set<Character>, L> _matchLits = new HashMap<Set<Character>, L>();
        private L _jokerLit = null;

		public Variable(PBBuilder<L> h) throws NoSolutionException {
            _domainNeg = h.lArray(_i2c.length());
            for (int i = 0; i < _domainNeg.length; i++) {
                _domainNeg[i] = h.newStrongLiteral().getOpposite();
            }
        }

        public L equivToMatch(Set<Character> value, PBBuilder<L> h) throws NoSolutionException {
            if (!_matchLits.containsKey(value)) {
                L matcher = h.newStrongLiteral();
                List<L> forbidder = new ArrayList<L>();
                for (int i = 0; i < _domainNeg.length; i++) {
                    if (!value.contains(getChar(i))) {
                        forbidder.add(_domainNeg[i]);
                    }
                }
                h.addReifiedConjunction(matcher, forbidder);
                
                _matchLits.put(value, matcher);
            }
            return _matchLits.get(value);
        }

        public L equivToJoker(PBBuilder<L> h) throws NoSolutionException {
            if (_jokerLit == null) {
                _jokerLit = h.newStrongLiteral();
                h.addReifiedConjunction(_jokerLit, _domainNeg);
            }
            return _jokerLit;
        }

        public boolean isJoker(Interpretation model) {
            return model.getValue(_jokerLit);
        }

        public CharSequence getPattern(Interpretation model) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < _domainNeg.length; i++) {
                if (model.getValue(_domainNeg[i].getOpposite())) {
                    sb.append(getChar(i));
                }
            }
            return sb;
        }
    }
    
    public String toString() {
        return "set of characters";
    }
}
