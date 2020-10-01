import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import com.gzoltar.core.instr.testing.TestResult;

public class PatchEvaluator {
	private static PatchEvaluator patchEvaluator;
	ArrayList<TestCase> testCases;
	private static boolean pass;
	
	private PatchEvaluator() {
		this.testCases = new ArrayList<TestCase>();
	}

	public static PatchEvaluator createPatchEvaluator() {
		if(patchEvaluator == null){
			patchEvaluator = new PatchEvaluator();
		}

		return patchEvaluator;
	}

	public void prepareTestCases(List <TestResult> testResults) {
		this.testCases.clear();

		try {
			for (TestResult tr : testResults) {
				TestCase testCase = new TestCase();
				String testName = tr.getName();
				String info[] = testName.split("#",2);
				testCase.className = info[0];
				testCase.methodName = info[1];

				if (tr.wasSuccessful()) {
					testCase.index = 0;
				}
				else {
					if (!tr.getName().startsWith("junit.framework")) {
						testCase.index = 1;
					}
					else {
						continue;
					}
				}
//				
				if(isDuplicate(testCase)==false) {
					this.testCases.add(testCase);
				}
			}

			Collections.sort(this.testCases);
//			System.out.println("Test Cases Size: " + this.testCases.size());
//			for(int i = 0; i < this.testCases.size(); i++) {
//				TestCase testCase = testCases.get(i);
//				if(testCase.index>0)
//					System.out.println(testCase.methodName+"("+testCase.className+")");
//			}
			//			

		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	boolean isDuplicate(TestCase currentTestCase) {
		for(int i = 0; i < this.testCases.size(); i++) {
			TestCase testCase = testCases.get(i);
			if(testCase.className.equals(currentTestCase.className) && testCase.methodName.equals(currentTestCase.methodName)) {
				return true;
			}
		}
		return false;
	}

	public boolean evaluatePatch(String directory) {
		int length = this.testCases.size();
		for(int i = 0; i < length; i++) {
			TestCase testCase = this.testCases.get(i);
			if(testCase.index>0) {
				if(runNegativeTest(directory,testCase.className+"::"+testCase.methodName)==false) {
//					System.out.println("failed in "+testCase.className+"::"+testCase.methodName);
					return false;
				}
			}
			else {
				break;
			}
		}
		return runAllTests(directory);
	}
	
	static boolean runNegativeTest(String directory, String testcase) {
//		System.out.println("test "+directory);
		PatchEvaluator.pass = false;
		Process process = null;
		try {			
			ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "/usr/bin/timeout 60 /home/apr/defects4j/framework/bin/defects4j test -w "+ directory+ " -t "+testcase);
			builder.redirectErrorStream(true);
			process = builder.start();
			final InputStream inputStream = process.getInputStream();

			Thread processReader = new Thread(){
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					try {
						while((line = reader.readLine()) != null) {
							if(line.equals("Failing tests: 0")) {
								PatchEvaluator.pass = true;
							}
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
		return PatchEvaluator.pass;
	}
	
	static boolean runAllTests(String directory) {
//		System.out.println("test "+directory);
		PatchEvaluator.pass = false;
		Process process = null;
		try {			
			ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "/usr/bin/timeout 600 /home/apr/defects4j/framework/bin/defects4j test -w "+ directory);
			builder.redirectErrorStream(true);
			process = builder.start();
			final InputStream inputStream = process.getInputStream();

			Thread processReader = new Thread(){
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					try {
						while((line = reader.readLine()) != null) {
							if(line.equals("Failing tests: 0")) {
								PatchEvaluator.pass = true;
							}
//							System.out.println(line);
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
		return PatchEvaluator.pass;
	}
//	public boolean evaluatePatch(String sourcePath) {
//		//		System.out.println("Evaluating");
//		Program program = Program.createProgram();
//		File testDir = new File(sourcePath+"/"+program.testClassFilesDirectory); //new File("digit/" + "test/");
//		File srcDir = new File(sourcePath+"/"+program.sourceClassFilesDirectory); //new File("digit/"+ "bin/");
////		System.out.println(sourcePath+"/"+program.sourceClassFilesDirectory);
//		URL testUrl = null;
//		URL srcUrl = null;
//
//		try {
//			testUrl = testDir.toURI().toURL();
//			srcUrl = srcDir.toURI().toURL();
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//		}
//
//		URL[] loadpath = new URL[2];
//
//		loadpath[0] = testUrl;
//		loadpath[1] = srcUrl;
//
//		ClassLoader classLoader = new URLClassLoader(loadpath);
//		//		boolean correctPatch = true;
//		for(int i = 0; i < this.testCases.size(); i++) {
//			try {
//				TestCase testCase = this.testCases.get(i);
//				Class testClass = Class.forName(testCase.className, true, classLoader);
//				Request request = Request.method(testClass, testCase.methodName);
//				//				System.out.println(testCase.methodName);
//				//				JUnitListener listener = new JUnitListener();
//				//				runner.addListener(listener);
//				JUnitCore runner = new JUnitCore();
//				Result result = runner.run(request);  
//
//				//				System.out.println(result.getFailureCount());
//				boolean pass = result.wasSuccessful();
//				//				System.out.println(pass + " in "+ testCase.methodName+ " from "+testCase.className);
//				if(pass == false) {
//					System.out.println("Failed in "+ testCase.methodName+ " from "+testCase.className);
//					//					for (Failure failure : result.getFailures()) {
//					//						System.out.println(failure.getException());
//					//						System.out.println(failure.getDescription());
//					//					}
//					//					testCase.index++;
//					//					Collections.sort(testClasses);
//					return false;
//					//					correctPatch = false;
//				}
//			} catch (Exception | Error e) {
//				System.out.println(e.getMessage());
//				return false;
//			}
//		}
//		//		System.out.println("DONE");
//		return true;
//	}

	//	private boolean threadTimer(long startTime, long timeToWait, Thread thread) {
	//        return ((System.currentTimeMillis() - startTime) > timeToWait) && thread.isAlive();
	//    }

	void processPatches(long startingTime) {
		PatchGenerator patchGenerator = PatchGenerator.createPatchGenerator();
		Collections.sort(patchGenerator.candidatePatchesList);
		boolean correctPatchFound = false;
		this.writeCandidatePatches();
		int length = patchGenerator.candidatePatchesList.size();
		for(int i=0; i<length; i++) { //candidatePatches.size()
//			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//			//			System.out.println(threadSet);
//			for(Thread t : threadSet) {
//				if(t.getName().equals("Time-limited test")) {
//					t.stop();
//					//					System.out.println("Stop " + t.getName());
//				}
//			}
			long currentTime = System.nanoTime();
			CandidatePatch candidatePatch = patchGenerator.candidatePatchesList.get(i);
//			System.out.print("patch no " + i + " ");
			File project = patchGenerator.generateConcretePatch(candidatePatch);
			if(project==null) {
				continue;
			}
//			System.out.println("Project "+project);

			if((currentTime - startingTime) >= (long)90*60*1000000000) {
				System.out.println("time-up!!!!!!!!!!!!!!!!");
				Program program = Program.createProgram();
				File fileToBeDeleted = new File("mutants/"+program.rootDirectory);
				if(deleteDirectory(fileToBeDeleted)==false) {
					System.out.println("Error in deleting "+fileToBeDeleted.getName());
				}
				break;
			}

			if(project.exists()) {
				if(Compiler.compileProject(project.getAbsolutePath()) == true) { //file.getAbsolutePath(),Program.sourceClassFilesDirectory
					correctPatchFound = evaluatePatch(project.getAbsolutePath());

					if(correctPatchFound == true) {
						System.out.println("Correct Patch Generated!");//+ " Elapsed Time: " +(System.nanoTime()-startingTime));
						System.out.println("Elapsed time: "+ ((double)(System.nanoTime() - startingTime)/1000000000.0) + " seconds");
//						System.out.println("File no " +candidatePatch.initialRank);
						System.out.println(candidatePatch.filename);
						System.out.println(candidatePatch.faultyNode);
						System.out.println(candidatePatch.fixingIngredient);
						System.out.println("Total Candidate Patches: " +patchGenerator.candidatePatchesList.size());
						System.out.println("Correct Patch Rank: " + (i+1));
						break;
						//					System.out.println(candidatePatch.mutationOperation);
					}
				}
			}
		}
	}

	void writeCandidatePatches() {
		Program program = Program.createProgram();
		File newfile = new File(program.rootDirectory+"temp.csv");
		PatchGenerator patchGenerator = PatchGenerator.createPatchGenerator();
		try {
			FileWriter fileWrite = new FileWriter(newfile.getAbsolutePath());
			for(int i=0; i<patchGenerator.candidatePatchesList.size(); i++) {
				fileWrite.write(patchGenerator.candidatePatchesList.get(i).toString()+"\n");
			}

			fileWrite.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	boolean deleteDirectory(File directoryToBeDeleted) {
		File[] contents = directoryToBeDeleted.listFiles();
		if (contents != null) {
			for (File file : contents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
}
