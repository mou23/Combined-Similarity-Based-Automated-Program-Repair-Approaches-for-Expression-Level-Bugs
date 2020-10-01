import java.io.Serializable;

import org.eclipse.jdt.core.dom.ASTNode;

public class CandidatePatch implements Serializable, Comparable <CandidatePatch> {
	ASTNode faultyNode;
	ASTNode fixingIngredient;
	double score;
	double suspiciousScore;
	String mutationOperation;
	double tokenScore;
	double genealogyScore;
	double variableScore;
	double LCS;
	int initialRank;
	int startLine;
	int endLine;
	String filename;
	
	@Override
	public int compareTo(CandidatePatch candidatePatch) {
		// TODO Auto-generated method stub
		return Double.compare(candidatePatch.score, this.score);
	}

	@Override
	public String toString() {
		ModelExtractor modelExtractor = ModelExtractor.createModelExtractor();
		PatchGenerator patchGenerator = PatchGenerator.createPatchGenerator();
		String modifiedFaultyNode = faultyNode.toString().replaceAll("[\\t\\n\\r,]+"," ") + "," + modelExtractor.getNodeType(faultyNode);
		String modifiedFixingIngredient = fixingIngredient.toString().replaceAll("[\\t\\n\\r,]+"," ") + "," + modelExtractor.getNodeType(fixingIngredient);
		return modifiedFaultyNode + ", file: "+ filename + ", line no: " + startLine + "," + modifiedFixingIngredient + ", " +score + ", " + genealogyScore+", "+variableScore+", "+LCS+ ", "+tokenScore+ ", "+ suspiciousScore;
	}
}