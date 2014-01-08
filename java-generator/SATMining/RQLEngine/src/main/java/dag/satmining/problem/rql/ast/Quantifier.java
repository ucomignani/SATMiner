/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

/**
 *
 * @author ecoquery
 */
public enum Quantifier {

    ForAll, Exists;

    public Quantifier opp() {
        switch (this) {
            case ForAll:
                return Exists;
            default:
                return ForAll;
        }
    }

    public String syntax() {
        switch (this) {
            case ForAll:
                return "F";
            default:
                return "E";
        }
    }
}
