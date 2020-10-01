import java.util.Objects;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;

public class FaultyClass {
	CompilationUnit compilationUnit;
	String fileContent;
	String filename;
	int rank;
	
	@Override
	public int hashCode() {
		return Objects.hash(filename);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FaultyClass other = (FaultyClass) obj;
		if (!filename.equals(other.filename))
			return false;
		return true;
	}
}
