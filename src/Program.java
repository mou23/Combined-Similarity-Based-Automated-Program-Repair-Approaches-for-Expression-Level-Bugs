import java.util.HashSet;
import java.util.Set;

public class Program {
	private static Program program;
	String rootDirectory;
	String sourceFilesDirectory;
	String sourceClassFilesDirectory;
	String testFilesDirectory;
	String testClassFilesDirectory;
	Set<String> dependencies;
	private Program() {
		dependencies = new HashSet<String>();
	}
	
	public static Program createProgram() {
		if(program == null) {
			program = new Program();
		}

		return program;
	}
}
