package arch.aufg2.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.hm.cs.rs.arch18.a02_staticanalyzer.ComplexityAnalyzer;

public class Analyzer implements ComplexityAnalyzer {
	Path rootdir;
	
	public Analyzer(){
		try{
			rootdir = Paths.get(new URI("file://"+System.getProperty("user.dir")));
		}catch( URISyntaxException e){}
	}

	@Override
	public ComplexityAnalyzer setRootdir(Path rootdir) throws IOException {
		this.rootdir = rootdir;
		return this;
	}
	
	/**
     * Startet ein anderes Programm und liefert dessen Konsolenausgabe (out und err) zurueck.
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
            Reader reader = new InputStreamReader(inputStream);
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
     * @return Pfad zu Javap
     * @throws IOException wenn javap nicht gefunden wird
     * */
    private Path getJavapPath() throws IOException{
    	Path result = rootdir;
    	
    	String path = System.getenv("PATH");
    	String[] env = path.split(";");
    	String javapath = "";
    	if(env.length == 1) {
    		path = path.replace(':', ';');
    		env = path.split(";");
    		for(String s: env)System.out.println(s);
    		
    	}
    	for(String s:env) {
    		if(s.matches("(.)+((jre)|(jdk))(.)+"))javapath=s;
    		}
    	System.out.println("**********\n\n"+javapath+"\n\n*********");
    	File file = new File(javapath);
    	
    /*	for(String s:file.list()){
    		System.out.println(s);
    	} 
    	System.out.println(javapath);
    	String[] p = javapath.split("/");
    	//for(String ss:p)System.out.println(ss);
    	
    	//System.out.println(p[0].concat("/"));
    	String w="";
    	for(String ss:p){
    		if(ss.matches("([jJ]ava)|((.)+jdk(.)+)")){
    			w=w.concat(ss);System.out.println(w);
    			break;
    		}
    		w = (w.concat(ss)).concat("/");
    		System.out.println(w);
    	}
    	File jFile = new File(w);
    	String[] ls = jFile.list(new FilenameFilter() {
    		
    		@Override
    		public boolean accept(File dir, String name) {
    			// TODO Auto-generated method stub
    			return name.matches("(.)*[jJ][dD][kK](.)*");
    		}
    	});
    	//for(String s:ls)System.out.println(s);
    	w=(w.concat("/")).concat(ls[0]);
    	File jFile2 = new File(w);
    	for(String s:jFile2.list())System.out.println(s);
    	
    	
    	System.out.println("\n\n\n\n\n\n\nXXXXXXXXXXXXXXXXXXXXX\n\n");
    	w=(w.concat("/")).concat("bin");
    	File jFile3 = new File(w);
    	for(String s:jFile3.list())System.out.println(s);
    	if(new File(w).list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.matches("javap");
			}
		}).length == 1){
    		try{
    			result = Paths.get(new URI("file://"+(w.concat("javap"))));
    		}catch( URISyntaxException e){}
    	}
    	else{
    		throw new IOException();
    	} */
    	try{
    		result = Paths.get(new URI("file:///usr/lib/jvm/java-8-openjdk-amd64/bin/javap"));
    	}catch(URISyntaxException e){}
    		
    	return result;
    }
    
    private int getComplexity(String compiledjavapcode){
    	String[] strings = compiledjavapcode.split("([0-9]+\\: if[a-z]+[ ]+[0-9]+)|([0-9]+\\: invoke[a-z]+[ ]+#[0-9]+)");
    	return strings.length-1;
    }

	@Override
	public Map<String, Integer> analyzeClassfiles() throws IOException {
		final List<Path> list = new ArrayList<Path>();
		final Map<String, Integer> map = new HashMap<>();
		
		Path javap = getJavapPath();	// Versuch den Pfad zu javap herauszufinden
		String javapstring = javap.toString();
		
		for(Iterator<Path> it= Files.walk(rootdir).iterator(); it.hasNext();){
			Path path = it.next();
			String s = path.toString();
			if(s.matches("(.)*\\.class")){
				System.out.println("String s : " + s);
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
