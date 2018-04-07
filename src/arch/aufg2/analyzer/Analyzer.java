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
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.hm.cs.rs.arch18.a02_staticanalyzer.ComplexityAnalyzer;

/**
 * Der Analyzer berechnet die Komplexitaet von class Dateien.
 * @author fabian
 * */
public class Analyzer implements ComplexityAnalyzer {
	/**
	 * rootdir Das Default-Verzeichnis.
	 * */
	private Path rootdir;
	
	/**
	 * Setzt das Root-Verzeichnis auf den Pfad
	 * in dem sich der User aktuell befindet.
	 * @throws URISyntaxException bei ungueltiger URI Syntax
	 * @author fabian
	 * */
	public Analyzer() throws URISyntaxException{
		rootdir = Paths.get(new File(System.getProperty("user.dir")).toURI());
	}

	/**
	 * Setzt das Root-Verzeichnis auf einen beliebigen Pfad.
	 * @author fabian
	 * @param root Pfad zum neuen Root-Verzeichnis
	 * @return Analyzer mit neuem Root-Verzeichnis
	 * */
	@Override
	public ComplexityAnalyzer setRootdir(Path root) throws IOException {
		this.rootdir = root;
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
            if(process.waitFor() != 0){
                throw new IOException("process failed");
            }
            collector.join();
        }
        return output.stream().collect(Collectors.joining("\n"));
    }
    
    /**
     * Sucht das Programm javap.
     * @author fabian
     * @return Pfad zu Javap
     * @throws IOException wenn javap nicht gefunden wird
     * */
    private Path getJavapPath() throws IOException{
    	Path result = rootdir;
    		final String osname = System.getProperty("os.name");
    	if(osname.matches("(l|L)inux")){
    		try {
    			result = Paths.get(new URI("file:///usr/lib/jvm/java-8-openjdk-amd64/bin/javap"));
    		}catch(URISyntaxException exception){
    			Logger.getAnonymousLogger().log(Level.SEVERE, "URISyntaxException occured!", exception);
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Berechnet die Komplexitaet des Assembly code.
     * @author fabian
     * @param compiledjavapcode Die Ausgabe der von javap geparsten class datei in Assembly Code  
     * @return Komplexitaet
     * */
    private int getComplexity(String compiledjavapcode){
    	System.out.println("##################  §§§§§§§§§§§  ##################");
    	System.out.println(compiledjavapcode);
    	System.out.println("##################  §§§§§§§§§§§  ##################");
    	
    	//final String[] strings = compiledjavapcode.split("([0-9]+\\: if[a-z_]+[ ]+[0-9]+)|([0-9]+\\: invoke[a-z]+[ ]+#[0-9]+)");
    	//final String[] strings = compiledjavapcode.split("(invokevirtual[ ]#[0-9]+)([0-9]+\\: invokedynamic #[0-9]+\\,[ ]+[0-9]+)|(Code\\:[ ]*)|([0-9]+\\: if[a-z_]*[ ]+[0-9]+)|(Class)");
    	final List<String> allMatches = new ArrayList<String>();
    	final Matcher m = Pattern.compile("([0-9]+\\: invokedynamic #[0-9]+\\,[ ]+[0-9]+)|(Code\\:[ ]*)|([0-9]+\\: if[a-z_]*[ ]+[0-9]+)|(Class)").matcher(compiledjavapcode);
    	
    	
    	while (m.find()) {
    	   allMatches.add(m.group());
    	}
    	int i=0;
    	int u=0;
    	for(String s:allMatches){
    		
    		if(s.matches("invokevirtual #3")){
    			u=1; System.out.println(s + "   " +u);
    		}
    		else{
    			i++; System.out.println(s + "   " +i);
    		}
    	}
    	/*
    	Set<String> set = new TreeSet<>(allMatches);
    	System.out.println("XXXXXXXXXXXX EEEEEEEEEE XXXXXXXXXXXXX");
    	for(String s:set){
    		System.out.println(s);
    	} */
    	
    	return allMatches.size();
    }

    /**
     * Sucht im Root-Verzeichnis und allen Unterverzeichnissen
     * nach .class Dateien und berechnet deren Komplexitaet.
     * @author fabian
     * @return Eine Map welche die class Dateien und deren Komplexitaet enthaelt
     * */
	@Override
	public Map<String, Integer> analyzeClassfiles() throws IOException {
		final List<Path> list = new ArrayList<Path>();
		final Map<String, Integer> map = new HashMap<>();
		
		final Path javap = getJavapPath();	// Versuche den Pfad zu javap herauszufinden
		final String javapstring = javap.toString();
		
		for(Iterator<Path> iterator= Files.walk(rootdir).iterator(); iterator.hasNext();){
			final Path path = iterator.next();
			final String string = path.toString();
			if(string.matches("(.)*\\.class")){
				System.out.println("String s : " + string);
				list.add(path);
			}
		}
		
		for(Path path:list){
			try{
				final String string = runProgram(javapstring,"-c",path.toString());
				final int complexity = getComplexity(string);
				map.put(path.toString(), complexity);
			}
			catch( InterruptedException interruptedException){
				Logger.getAnonymousLogger().log(Level.SEVERE, "InterruptedException occured!", interruptedException);
			}
		}
		
		return map;
	}

}
