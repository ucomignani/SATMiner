package dag.satmining.problem.satql.ast.sql;

public class RAWSQLQuery extends RAWSQL implements FromExpression {

    public RAWSQLQuery(String expression) {
        super(expression);
    }

    public RAWSQLQuery(String parsed, boolean fromParsing) {
        super(parsed, fromParsing);
    }

    @Override
    public void buildSQLQueryNoName(StringBuilder output) {
        buildSQLQuery(output);
    }

}
