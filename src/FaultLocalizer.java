
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Statement;


public class FaultLocalizer {
	ArrayList<FaultyLine> faultyLines;
	private static FaultLocalizer faultLocalizer;
	Set<String> javaClasses;
	Set<String> testClasses;
	Set<String> faultyClasses;

	private FaultLocalizer() {
		this.faultyLines = new ArrayList<FaultyLine>();
		this.javaClasses = new HashSet<String>();
		this.testClasses = new HashSet<String>();
		this.faultyClasses = new HashSet<String>();
	}

	public static FaultLocalizer createFaultLocalizer() {
		if(faultLocalizer == null){
			faultLocalizer = new FaultLocalizer();
		}

		return faultLocalizer;
	}


	//	private void findJavaClasses(File currentFolder) {
	//		Program program = Program.createProgram();
	//		File rootFolder = new File(program.sourceClassFilesDirectory);
	//		File[] listOfFiles = currentFolder.listFiles();
	////		System.out.println(currentFolder);
	//		for (int i = 0; i < listOfFiles.length; i++) {
	//			File file = listOfFiles[i];
	//			if (file.isFile() && file.getName().endsWith(".class")) {
	//				String relative = rootFolder.toURI().relativize(file.toURI()).getPath();
	//
	//				String temp = relative.replace("/", ".");
	//				String className = temp.substring(0, temp.length() - 6);
	//				javaClasses.add(className);
	//			}
	//			else if (listOfFiles[i].isDirectory()) {
	//				findJavaClasses(new File(currentFolder+"/"+file.getName()));
	//			}
	//		}
	//	}
	//	
	//	private void findTestClasses(File currentFolder) {
	//		Program program = Program.createProgram();
	//		File rootFolder = new File(program.testClassFilesDirectory);
	//		File[] listOfFiles = currentFolder.listFiles();
	//		for (int i = 0; i < listOfFiles.length; i++) {
	//			File file = listOfFiles[i];
	//			if (file.isFile() && file.getName().endsWith(".class")) {
	//				String filename = file.getName();
	//				filename = filename.substring(0, filename.length() - 6);
	//				String relative = rootFolder.toURI().relativize(file.toURI()).getPath();
	//				String temp = relative.replace("/", ".");
	//				String className = temp.substring(0, temp.length() - 6);
	//				
	//				testClasses.add(className);
	//			}
	//			else if (listOfFiles[i].isDirectory()) {
	//				findTestClasses(new File(currentFolder+"/"+file.getName()));
	//			}
	//		}
	//		
	//	}

	private void findRelevantJavaClasses(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				this.javaClasses.add(line);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void findRelevantTestClasses(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				this.testClasses.add(line);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void runGZoltar(String rootDir, String binJavaDir, String binTestDir, Set<String> dependencies) throws FileNotFoundException, IOException {
		//		String projLoc = new File("").getAbsolutePath();
		//		System.out.println(binTestDir);
		//		System.out.println(binJavaDir);
		GZoltar gz = new GZoltar(rootDir);

		gz.getClasspaths().add(binJavaDir);
		gz.getClasspaths().add(binTestDir);

		if (dependencies.size()>0)
			gz.getClasspaths().addAll(dependencies);

		for (String testClass : this.testClasses)
			gz.addTestToExecute(testClass);

		for (String javaClass : this.javaClasses)
			gz.addClassToInstrument(javaClass);

		//		System.out.println("classPath:\t" + gz.getClasspaths().toString());
		//	    System.out.println(gz.getWorkingDirectory());
		gz.run();

		for (Statement gzoltarStatement : gz.getSuspiciousStatements()) {
			FaultyLine faultyLine = new FaultyLine();
			String info[] = (gzoltarStatement.getClazz().getLabel()).split("\\$", 2);
			faultyLine.fileName = info[0]; //package.class$inner_class
			faultyLine.lineNumber = gzoltarStatement.getLineNumber();
			faultyLine.suspiciousValue = gzoltarStatement.getSuspiciousness();
			if(faultyLine.suspiciousValue > 0) {
				this.faultyLines.add(faultyLine);
				this.faultyClasses.add(faultyLine.fileName);
			}
		}
		PatchEvaluator patchEvaluator = PatchEvaluator.createPatchEvaluator();
		patchEvaluator.prepareTestCases(gz.getTestResults());
	}


	public void localizeFault() {
		try {
			this.init();
			Program program = Program.createProgram();
			//			findJavaClasses(new File());
			findRelevantJavaClasses(new File(program.rootDirectory+"javaClasses.txt"));
			findRelevantTestClasses(new File(program.rootDirectory+"testClasses.txt"));			
//			ClassFinder finder = new ClassFinder(program.rootDirectory+program.sourceClassFilesDirectory, program.rootDirectory+program.testClassFilesDirectory, program.dependencies);
//			this.javaClasses = finder.findBinJavaClasses();
//			this.testClasses = finder.findBinExecuteTestClasses();
			//			System.out.println("testClasses");
			//			System.out.println(testClasses);
//			System.out.println("javaClasses");
//			System.out.println(javaClasses);
			this.runGZoltar(program.rootDirectory, program.sourceClassFilesDirectory, program.testClassFilesDirectory, program.dependencies);
			System.out.println("Fault Localizaton done!");
//			System.out.println(faultyLines.size());
//			for(int i = 0; i < faultyLines.size(); i++) {
//				System.out.println(faultyLines.get(i));
//			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void init() {
		this.faultyLines.clear();
		this.javaClasses.clear();
		this.testClasses.clear();
		this.faultyClasses.clear();
	}
}
