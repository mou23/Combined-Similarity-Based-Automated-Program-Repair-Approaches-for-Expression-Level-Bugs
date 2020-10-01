import java.util.HashMap;
import java.util.HashSet;


public class FaultyNode extends Node implements Comparable <FaultyNode> {
	double suspiciousValue;
	
	public FaultyNode() {
		this.genealogy = null;
		this.tokens = new HashMap<String,Integer>();
	}
	@Override
	public int compareTo(FaultyNode faultyNode) {
		// TODO Auto-generated method stub
		return Double.compare(faultyNode.suspiciousValue, this.suspiciousValue);
	}
	
	@Override
	public String toString() {
		return "FaultyNode [node=" + node + ", suspiciousValue=" + suspiciousValue + ", startLine=" + startLine
				+ ", endLine=" + endLine + ", type=" + type + "]";
	}	
}
