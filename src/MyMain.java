import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MyMain {
	public static void main(String[] args) {
		runProject();
		System.out.println("The End!");
//		System.exit(0);
	}

	public static void runProject() {
		File folder = new File("dataset/");
		File[] listOfFiles = folder.listFiles();
		try {
			for (int i = 0; i < listOfFiles.length; i++) {
				PatchGenerator patchGenerator = PatchGenerator.createPatchGenerator(); 
				patchGenerator.count = 0;
				patchGenerator.candidatePatchesList.clear();
				patchGenerator.faultyClasses.clear();
				String project = folder.getName()+ "/" + listOfFiles[i].getName() + "/";
				System.out.println(project);
//				if(!project.equals("dataset3/Closure_133/")) {
//					continue;
//				}
				Program program = Program.createProgram();
				program.dependencies.clear();
				program.rootDirectory = project;
				program.sourceFilesDirectory = getDirectory(project, "dir.src.classes"); //"grade001/src/main";//"digit003/src/main"; //"syl002/src/main";//
				program.sourceClassFilesDirectory = getDirectory(project, "dir.bin.classes"); //"grade001/bin";//"syl002/bin";//
				program.testClassFilesDirectory = getDirectory(project, "dir.bin.tests"); //"grade001/test";//"syl002/test";//
				File libraryPath = new File(project+"/lib");
				if(libraryPath.exists()) {
					getDependencies(libraryPath);
				}
				libraryPath = new File(project+"/build/lib");
				if(libraryPath.exists()) {
					getDependencies(libraryPath);
				}
//				System.out.println(program.sourceFilesDirectory);
//				System.out.println(program.sourceClassFilesDirectory);
//				System.out.println(program.testClassFilesDirectory);
//				System.out.println(program.dependencies);
//				break;
				long startingTime = System.nanoTime();
//				System.out.println(startingTime);
//				System.out.println("Localizing Fault");
				FaultLocalizer faultLocalizer = FaultLocalizer.createFaultLocalizer();
				faultLocalizer.localizeFault();
//				System.out.println(System.nanoTime());
				Iterator<String> faultyClassIterator = faultLocalizer.faultyClasses.iterator();
				while(faultyClassIterator.hasNext()){
			        String faultyClass = (faultyClassIterator.next()).replace(".", "/");
			        ExecutorService executor = Executors.newSingleThreadExecutor();
			        Future<String> future = executor.submit(patchGenerator);
			        System.out.println("Generating Patches for "+program.rootDirectory+program.sourceFilesDirectory+"/"+faultyClass+".java");
					patchGenerator.file=new File(program.rootDirectory+program.sourceFilesDirectory+"/"+faultyClass+".java");
//					if(patchGenerator.file.exists()) {
//						System.out.println("FILE EXISTS!");
//					}
			        try {
			            System.out.println(future.get(5400, TimeUnit.SECONDS));
			        } catch (Exception e) {
			        	System.out.println(e);
			            future.cancel(true);
			            System.out.println("Terminated!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			            break;
			        }

			        executor.shutdownNow();
			    }
//				scanDirectory(new File(program.sourceFilesDirectory));
				System.out.println("Total Patches: "+ patchGenerator.candidatePatchesList.size());
				System.out.println("Evaluating Patches");
				PatchEvaluator patchEvaluator = PatchEvaluator.createPatchEvaluator();
				patchEvaluator.processPatches(startingTime);
				System.out.println("\n\n");
//				break;
			}
		} catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}
	
	static void getDependencies(File directory) throws Exception {
		File[] listOfFiles = directory.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isDirectory()) {
				getDependencies(new File(directory+"/"+listOfFiles[i].getName()));
			}
			else if (listOfFiles[i].getName().endsWith(".jar")) {
				Program program = Program.createProgram();
				program.dependencies.add(listOfFiles[i].getAbsolutePath());
			}
		}
	}
	
	static String getDirectory(String directory, String command) {
		Process process = null;
		final List<String> message = new ArrayList<String>();
		try {			
			ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "/home/apr/defects4j/framework/bin/defects4j export -w "+ directory+ " -p "+command);
			process = builder.start();
			final InputStream inputStream = process.getInputStream();

			Thread processReader = new Thread(){
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					try {
						while((line = reader.readLine()) != null) {
//							System.out.println(line);
							message.add(line);
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					try {
						reader.close();
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			};

			processReader.start();
			try {
				processReader.join();
				process.waitFor();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			if (process != null) {
				process.destroy();
			}
			process = null;
		}
		return message.get(message.size()-1);
	}
	
//	private static void scanDirectory(File folder) {
//		File[] listOfFiles = folder.listFiles();
//		for (int i = 0; i < listOfFiles.length; i++) {
//			if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(".java")) {
//				PatchGenerator patchGenerator = PatchGenerator.createPatchGenerator(); 
//				patchGenerator.generatePatch(listOfFiles[i]);
//			}
//			else if (listOfFiles[i].isDirectory()) {
//				scanDirectory(new File(folder+"/"+listOfFiles[i].getName()));
//			}
//		}
//	}
}
