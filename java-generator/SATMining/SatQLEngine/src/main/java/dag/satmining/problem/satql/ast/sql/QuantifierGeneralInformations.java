package dag.satmining.problem.satql.ast.sql;

public class QuantifierGeneralInformations {
	private boolean _isUniversalQuantifier;
	private boolean _isPercentQuantifier;
	private int _nValue;
	
	public QuantifierGeneralInformations(boolean isUniversalQuantifier,boolean isPercentQuantifier,int nValue){
		_isUniversalQuantifier = isUniversalQuantifier;
		_isPercentQuantifier = isPercentQuantifier;
		_nValue = nValue;
	}
	
	public boolean isUniversalQuantifier(){
		return _isUniversalQuantifier;
	}
	
	public boolean isPercentQuantifier(){
		return _isPercentQuantifier;
	}
	
	public int getNValue(){
		return _nValue;
	}
}
