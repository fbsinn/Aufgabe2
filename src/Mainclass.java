import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;

import arch.aufg2.analyzer.Analyzer;

/**
 * Testet die Analyzer Klasse.
 * @author fabian
 * */
public class Mainclass {

	/**
	 * Startpunkt der Mainclass.
	 * @author fabian
	 * @param args Kommandozeilenargumente
	 * @throws URISyntaxException Bei ungueltiger Eingabe einer URI
	 * @throws IOException Bei Auftreten eines Fehlers
	 * */
	public static void main(String[] args) throws URISyntaxException,IOException{
		final Analyzer smart = new Analyzer();
		smart.setRootdir(Paths.get(new File("C:\\Users\\fabian\\JavaBsp").toURI()));
		System.out.println("\n Bitte Warten auf Ergebnis \n");
		final Map<String, Integer> map = smart.analyzeClassfiles();
		System.out.println("+++++++++++++++++++++++++++++++++");
		System.out.println("+++++++++    ERGEBNIS   +++++++++");
		System.out.println("+++++++++++++++++++++++++++++++++");
		for(Map.Entry<String, Integer> entry: map.entrySet()){
			System.out.println(entry.getKey() + "        " + entry.getValue());
		}
	}

}
