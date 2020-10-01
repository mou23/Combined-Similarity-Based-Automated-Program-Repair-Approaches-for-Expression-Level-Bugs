import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SetupDefects4J {
	public static void main(String[] args) {
		Process process = null;
		try {			
			//			ProcessBuilder builder = new ProcessBuilder("java", "-jar", "lib/com.gzoltar-1.6.1-java7-jar-with-dependencies.jar", "-Dgzoltar_data_dir=fault/",  "-DclassesDir=digit003/bin/", "-DtestsDir=digit003/test/", "-Dcoefficients=JACCARD", "-diagnose");//, "-DtestsDir=E:/fault-localization-research-master/src/triangle/target/test-classes/triangle/");
			String projects = "Math_2;Math_32;Chart_1;Math_30;Chart_13;Math_33";
			String[] projectsWithBugID = projects.split(";");
			for(int i=0; i<projectsWithBugID.length;i++) {
				String[] info = projectsWithBugID[i].split("_");
				String projectName = info[0];
				String bugID = info[1]+"f";
				System.out.println("/home/apr/defects4j/framework/bin/defects4j checkout -p "+ projectName + " -v " + bugID + " -w "+ "fixed/"+projectsWithBugID[i]);
				ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "/home/apr/defects4j/framework/bin/defects4j checkout -p "+ projectName + " -v " + bugID + " -w "+ "fixed/"+projectsWithBugID[i]);
//				ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "/home/apr/defects4j/framework/bin/defects4j info -p Lang");
				process = builder.start();
				final InputStream inputStream = process.getInputStream();

				Thread processReader = new Thread(){
					public void run() {
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
						String line;
						try {
							while((line = reader.readLine()) != null) {
								System.out.println(line);
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
					compileProject("fixed/"+projectsWithBugID[i]);
//					getRelevantClasses("fixed/"+projectsWithBugID[i]);
//					getTestClasses("fixed/"+projectsWithBugID[i]);
				} catch (InterruptedException e) {
					System.out.println(e.getMessage());
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			if (process != null) {
				process.destroy();
			}
			process = null;
		}
	}

	static void compileProject(String directory) {
		Process process = null;
		try {			
			ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "/home/apr/defects4j/framework/bin/defects4j compile -w "+ directory);
			process = builder.start();
			final InputStream inputStream = process.getInputStream();

			Thread processReader = new Thread(){
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					try {
						while((line = reader.readLine()) != null) {
							System.out.println(line);
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
	}
	static void getRelevantClasses(String directory) {
		Process process = null;
		try {			
			ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "/home/apr/defects4j/framework/bin/defects4j export -p classes.relevant -o " +directory+"/javaClasses.txt -w "+ directory);
			process = builder.start();
			final InputStream inputStream = process.getInputStream();

			Thread processReader = new Thread(){
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					try {
						while((line = reader.readLine()) != null) {
							System.out.println(line);
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
	}
	
	static void getTestClasses(String directory) {
		Process process = null;
		try {			
			ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "/home/apr/defects4j/framework/bin/defects4j export -p tests.all -o " +directory+"/testClasses.txt -w "+ directory);
			process = builder.start();
			final InputStream inputStream = process.getInputStream();

			Thread processReader = new Thread(){
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					try {
						while((line = reader.readLine()) != null) {
							System.out.println(line);
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
	}
}
