package dag.satmining.problem.rql.ast.intermediate;

import java.util.BitSet;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.utils.BitSetMap;

public abstract class BFormula {

	private BitSetMap<LiteralOrValue> _cache;
	private int _localCacheHits = 0;
	
	public static int cacheHits = 0;

	public final <L extends Literal<L>> LiteralOrValue getRepresentation(PBBuilder<L> handler,
			BitSet data) throws NoSolutionException {
		if (_cache == null) {
			return makeRepresentation(handler, data);
		} else {
			LiteralOrValue result = _cache.get(data);
			if (result == null) {
				result = makeRepresentation(handler, data);
				_cache.put(data, result);
			} else {
				cacheHits++;
				_localCacheHits++;
			}
			return result;
		}
	}
	
	public void setCached(boolean doCache) {
		if (doCache) {
			if (_cache == null) {
				_cache = new BitSetMap<LiteralOrValue>();
				_cache.setMask(getMask());
			}
		} else {
			_cache = null;
		}
	}

	public final int getCacheHits() {
		return _localCacheHits;
	}
	
	abstract <L extends Literal<L>> LiteralOrValue makeRepresentation(PBBuilder<L> handler, BitSet data)
			throws NoSolutionException;

	abstract BitSet getMask();

}
