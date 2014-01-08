/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.backend.boolvarpb;

import boolvar.output.Clause;

/**
 *
 * @author ecoquery
 */
public class StrongClause extends Clause {

    public StrongClause() {
        super();
    }

    @Override
    public String dimacsLine() {
        return "b "+super.dimacsLine();
    }
    
    
    
}
