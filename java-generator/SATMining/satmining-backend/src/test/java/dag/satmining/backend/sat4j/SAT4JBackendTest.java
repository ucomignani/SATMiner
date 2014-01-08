/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.backend.sat4j;

import dag.satmining.backend.BackendTest;
import dag.satmining.backend.dimacs.DimacsLiteral;

/**
 *
 * @author ecoquery
 */
public class SAT4JBackendTest extends BackendTest<DimacsLiteral> {

    @Override
    protected void initHandler() {
        SAT4JPBBuilder sat4jHandler = new SAT4JPBBuilder(SAT4JPBBuilder.SMALL); // use a light solver to avoid heap space problems
        _handler = sat4jHandler;
        _modelReader = sat4jHandler;
    }

    @Override
    protected void destroyHandler() {
        _handler = null;
        _modelReader = null;
    }
    
}
