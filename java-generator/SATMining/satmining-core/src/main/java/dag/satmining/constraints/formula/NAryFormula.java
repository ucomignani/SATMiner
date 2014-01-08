package dag.satmining.constraints.formula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class NAryFormula extends SFormula {

	protected Formula[] _args;

	public NAryFormula(Collection<Formula> args) {
		this._args = new Formula[args.size()];
		this._args = args.toArray(this._args);
	}
	
	public NAryFormula(Formula... args){
		this._args = args;
	}

	@Override
	protected SFormula sanitize(boolean pushNeg) {
		List<Formula> newArgs = new ArrayList<Formula>();
		boolean unchanged = true;
		for (Formula f : _args) {
			Formula f2 = f.sanitize(pushNeg);
			if (f != f2) {
				unchanged = false;
			}
			newArgs.add(f2);
		}
		if (unchanged) {
			return this;
		} else if (pushNeg) {
			return dualFromArgs(newArgs);
		} else {
			return sameFromArgs(newArgs);
		}
	}

	protected abstract SFormula sameFromArgs(List<Formula> args);

	protected abstract SFormula dualFromArgs(List<Formula> args);

}