import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;

import arch.aufg2.analyzer.Analyzer;

public class Mainclass {

	public static void main(String[] args) throws URISyntaxException,IOException{
		Analyzer smart = new Analyzer();
		smart.setRootdir(Paths.get(new URI("file:///C:/Users/fabian/JavaBsp")));
		System.out.println("\n Bitte Warten auf Ergebnis \n");
		Map<String, Integer> map = smart.analyzeClassfiles();
		System.out.println("+++++++++++++++++++++++++++++++++");
		System.out.println("+++++++++    ERGEBNIS   +++++++++");
		System.out.println("+++++++++++++++++++++++++++++++++");
		for(Map.Entry<String, Integer> entry: map.entrySet()){
			System.out.println(entry.getKey() + "        " + entry.getValue());
		}
	}

}
