import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class Compiler {
	private static Compiler compiler;
	private static boolean compilable;
	private Compiler() {
		
	}
	
	public static Compiler createCompiler() {
		if(compiler == null) {
			compiler = new Compiler();
		}

		return compiler;
	}
	
	static boolean compileProject(String directory) {
//		System.out.println("compile "+directory);
		Process process = null;
		Compiler.compilable = true;
		try {			
			ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "/home/apr/defects4j/framework/bin/defects4j compile -w "+ directory);
			builder.redirectErrorStream(true);
			process = builder.start();
			final InputStream inputStream = process.getInputStream();

			Thread processReader = new Thread(){
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					try {
						while((line = reader.readLine()) != null) {
							if(line.equals("BUILD FAILED")) {
//								System.out.println("Compilation Error!");
								Compiler.compilable = false;
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
		return Compiler.compilable;
	}
}
