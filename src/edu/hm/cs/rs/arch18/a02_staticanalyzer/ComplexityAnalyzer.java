/* (C) 2018, R. Schiedermeier, rs@cs.hm.edu
 * Java 1.8.0_121, Linux x86_64 4.14.12
 * violet (Intel Core i7 CPU 920/2.67GHz, 8 cores, 2668 MHz, 24128 MByte RAM)
 **/
package edu.hm.cs.rs.arch18.a02_staticanalyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/** Interface fuer alle statischen Analyzer.
 * @author R. Schiedermeier, rs@cs.hm.edu
 * @version 2018-03-21
 */
public interface ComplexityAnalyzer {
    /** Legt das Startdirectory neu fest.
     * Voreingestellt ist das aktuelle Arbeitsdirectory (System-Property user.dir).
     * @param rootdir Neues Startdirectory.
     * @throws IOException wenn rootdir ungueltig ist
     * @return Dieser Analyzer.
     * @throws IOException wenn rootdir kein Directory ist.
     */
    ComplexityAnalyzer setRootdir(Path rootdir) throws IOException;

    /** Startet die Analyse und liefert die Ergebnisse als Map zurueck.
     * @throws IOException wenn ein fehler auftritt
     * @return Map von FQCNs auf die zyklomatische Komplexitaet.
     */
    Map<String, Integer> analyzeClassfiles() throws IOException;

}