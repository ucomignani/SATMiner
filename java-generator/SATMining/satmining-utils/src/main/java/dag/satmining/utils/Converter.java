/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.utils;

/**
 *
 * @author ecoquery
 */
public interface Converter <E,F> {
    F convert(E e);
}
