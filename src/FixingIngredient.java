import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;

public class FixingIngredient extends Node{
	
	public FixingIngredient() {
		this.genealogy = null;
		this.tokens = new HashMap<String,Integer>(); 
	}
	
	@Override
	public String toString() {
		return "FixingIngredient [node=" + node + ", startLine=" + startLine + ", endLine=" + endLine + ", type=" + type
				+ "]";
	}
}
