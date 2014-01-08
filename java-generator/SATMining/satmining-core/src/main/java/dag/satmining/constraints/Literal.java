package dag.satmining.constraints;

public interface Literal<L> {
	
	boolean isPositive();
	
	L getOpposite();
	
	int getVariableId();
	
	int toDimacs();
	
}
