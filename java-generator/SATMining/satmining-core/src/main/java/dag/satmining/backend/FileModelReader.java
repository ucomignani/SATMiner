/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.backend;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author ecoquery
 */
public interface FileModelReader extends ModelReader {
    
    void open(File file) throws IOException;
    
    void close() throws IOException;
    
}
