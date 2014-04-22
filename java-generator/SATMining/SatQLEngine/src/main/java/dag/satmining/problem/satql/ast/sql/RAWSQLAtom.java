package dag.satmining.problem.satql.ast.sql;


public final class RAWSQLAtom extends RAWSQL implements SQLBooleanValue {
    
    public RAWSQLAtom(String expression) {
        super(expression);
    }
    
    public RAWSQLAtom(String expression, boolean fromParsing) {
        super(expression,fromParsing);
    }
    
}
