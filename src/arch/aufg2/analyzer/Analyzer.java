package arch.aufg2.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.hm.cs.rs.arch18.a02_staticanalyzer.ComplexityAnalyzer;

/**
 * Der Analyzer berechnet die Komplexitaet von class Dateien
 * @author fabian
 * */
public class Analyzer implements ComplexityAnalyzer {
	private Path rootdir;
	
	/**
	 * Setzt das Root-Verzeichnis auf den Pfad
	 * in dem sich der User aktuell befindet
	 * @author fabian
	 * */
	public Analyzer(){
		try{
			rootdir = Paths.get(new URI("file://"+System.getProperty("user.dir")));
		}catch( URISyntaxException e){
			e.printStackTrace();
		}
	}

	/**
	 * Setzt das Root-Verzeichnis auf einen beliebigen Pfad
	 * @author fabian
	 * @param rootdir Pfad zum neuen Root-Verzeichnis
	 * @return Analyzer mit neuem Root-Verzeichnis
	 * */
	@Override
	public ComplexityAnalyzer setRootdir(Path rootdir) throws IOException {
		this.rootdir = rootdir;
		return this;
	}
	
	/**
     * Startet ein anderes Programm und liefert dessen Konsolenausgabe (out und err) zurueck.
     * @author Schiedermeier
     * @param command Programmname und Kommandozeilenargumente.
     * @return Ausgabe des Programms.
     * @exception IOException bei einem Fehler im Filesystem.
     * @exception InterruptedException bei einer Unterbrechung des Prozesses.
     */
    private String runProgram(String... command) throws IOException, InterruptedException {
        /*for(String s: command)
        	System.out.println(s);
    	*/
    	final Process process = new ProcessBuilder(command)
        .redirectErrorStream(true)
        .start();
        final List<String> output = new ArrayList<>();
        try(InputStream inputStream = process.getInputStream();
            Reader reader = new InputStreamReader(inputStream,Charset.defaultCharset());
            BufferedReader bufferedReader = new BufferedReader(reader)) {
            final Thread collector = new Thread(() -> bufferedReader.lines().forEach(output::add));
            collector.start();
            if(process.waitFor() != 0)
                throw new IOException("process failed");
            collector.join();
        }
        return output.stream().collect(Collectors.joining("\n"));
    }
    
    /**
     * Sucht das Programm javap
     * @author fabian
     * @return Pfad zu Javap
     * @throws IOException wenn javap nicht gefunden wird
     * */
    private Path getJavapPath() throws IOException{
    	Path result = rootdir;
    		final String os_name = System.getProperty("os.name");
    	if(os_name.matches("(l|L)inux")){
    		try {
    			result = Paths.get(new URI("file:///usr/lib/jvm/java-8-openjdk-amd64/bin/javap"));
    		}catch(URISyntaxException exception){
    			exception.printStackTrace();
    		}
        	return result;
    	}
    	else if(os_name.matches("(.)*Windows(.)*")){ 
        	result = Paths.get(new File("C:\\Program Files\\Java\\jdk1.8.0_66/bin\\javap").toURI());
        	return result;
    	}
    	
    	return result;
    }
    
    /**
     * Berechnet die Komplexitaet des Assembly code
     * @author fabian
     * @return Komplexitaet
     * */
    private int getComplexity(String compiledjavapcode){
    	String[] strings = compiledjavapcode.split("([0-9]+\\: if[a-z]+[ ]+[0-9]+)|([0-9]+\\: invoke[a-z]+[ ]+#[0-9]+)");
    	return strings.length-1;
    }

    /**
     * Sucht im Root-Verzeichnis und allen Unterverzeichnissen
     * nach .class Dateien und berechnet deren Komplexitaet
     * @author fabian
     * @return Eine Map welche die class Dateien und deren Komplexitaet enthaelt
     * */
	@Override
	public Map<String, Integer> analyzeClassfiles() throws IOException {
		final List<Path> list = new ArrayList<Path>();
		final Map<String, Integer> map = new HashMap<>();
		
		Path javap = getJavapPath();	// Versuche den Pfad zu javap herauszufinden
		String javapstring = javap.toString();
		
		for(Iterator<Path> iterator= Files.walk(rootdir).iterator(); iterator.hasNext();){
			Path path = iterator.next();
			final String string = path.toString();
			if(string.matches("(.)*\\.class")){
				System.out.println("String s : " + string);
				list.add(path);
			}
		}
		
		for(Path path:list){
			try{
				String string = runProgram(javapstring,"-c",path.toString());
				int i = getComplexity(string);
				map.put(path.toString(), i);
			}
			catch( InterruptedException e){}
		}
		
		return map;
	}

}
