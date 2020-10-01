import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;


public class PatchGenerator implements Callable<String> {
	private static PatchGenerator patchGenerator;
	CompilationUnit compilationUnit;
	int correctPatches;
	//	static HashSet<CandidatePatch> candidatePatchesSet = new HashSet<CandidatePatch>();
	ArrayList<CandidatePatch> candidatePatchesList = new ArrayList<CandidatePatch>();
	Set<FaultyClass> faultyClasses;
	Document document;
	File file;
	String filename;
	int count;
	File candidatePatchesDirectory;
	//	boolean correctPatchFound;
	//	long startingTime;
	IngredientCollector ingredientCollector;

	PatchEvaluator patchEvaluator;

	private PatchGenerator() {
		this.patchEvaluator = PatchEvaluator.createPatchEvaluator();
		this.faultyClasses = new HashSet<FaultyClass>();
		this.ingredientCollector = IngredientCollector.createIngredientCollector();
	}

	public static PatchGenerator createPatchGenerator() {
		if(patchGenerator == null){
			patchGenerator = new PatchGenerator();
		}

		return patchGenerator;
	}
	
	@Override
	public String call() throws Exception {
		Program program = Program.createProgram();
		//		this.startingTime = startingTime;
		FaultyClass faultyClass = new FaultyClass();
		File rootFolder = new File(program.rootDirectory+program.sourceFilesDirectory);
		//		System.out.println(rootFolder.toURI());
		//		System.out.println(file.toURI());
		String relative = rootFolder.toURI().relativize(file.toURI()).getPath();
		faultyClass.filename = relative;
		this.filename = relative;

		String temp = relative.replace("/", ".");
		String className = temp.substring(0, temp.length() - 5);
		//		System.out.println("Class "+className);
		this.init(className);
		//	file = new File("digit003/src/main/java/introclassJava/digits_6e464f2b_003_old.java"); // //D:/thesis/software repair/resources/20/capgen/CapGen/IntroClassJava/dataset/syllables/fcf701e8bed9c75a4cc52a990a577eb0204d7aadf138a4cad08726a847d66e77126f95f06f839ec9224b7e8a887b873fe0d4b6f4311b4e8bd2a36e5028d1feca/002/src/main/java/introclassJava/syllables_fcf701e8_002.java
		//		this.file = file;

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String fileContent = readFileToString(file.getAbsolutePath());
		this.document = new Document(fileContent);
		parser.setSource(document.get().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		String[] jarFiles = program.dependencies.toArray(new String[program.dependencies.size()]);
		String[] classFiles = new String[] {program.rootDirectory+program.sourceClassFilesDirectory};
		int jarFilesLength = jarFiles.length;
		int classFilesLength = classFiles.length;
		String[] classPathEntries = new String[jarFilesLength + classFilesLength];

		System.arraycopy(jarFiles, 0, classPathEntries, 0, jarFilesLength);
		System.arraycopy(classFiles, 0, classPathEntries, jarFilesLength, classFilesLength);

		//        System.out.println(classPathEntries);
		parser.setEnvironment(classPathEntries, null, null, true);
		parser.setUnitName(file.getName());
		this.compilationUnit = (CompilationUnit) parser.createAST(null);
		//		System.out.println("Collecting Fixing Ingredients");

		//		tokenizer.tokenize(file.getAbsolutePath());
		//		this.compilationUnit.accept(new VariableCollector());
		//		System.out.println("DONE");
		this.compilationUnit.accept(ingredientCollector);

//		Tokenizer tokenizer = Tokenizer.createTokenizer();
//		tokenizer.tokenize(this.ingredientCollector.faultyNodes);
//		tokenizer.tokenize(this.ingredientCollector.fixingIngredients);
		//		System.out.println("INGREDIENT");
		////		System.out.println(IngredientCollector.fixingIngredients.size());
//		for(int i=0; i<this.ingredientCollector.fixingIngredients.size(); i++) {
//			System.out.println(this.ingredientCollector.fixingIngredients.get(i).toString());
//			System.out.println(this.ingredientCollector.fixingIngredients.get(i).tokens);
//		}
		//			System.out.println(IngredientCollector.faultyNodes.size());
		//		
		//		System.out.println("VARIABLES");
		//		for(int i=0; i<VariableCollector.variables.size(); i++) {
		//			System.out.println(VariableCollector.variables.get(i));
		//		}
//		Collections.sort(this.ingredientCollector.faultyNodes);
		ReplaceHandler replaceHandler = ReplaceHandler.createReplaceHandler();
		int length = this.ingredientCollector.faultyNodes.size();
//		System.out.println("FAULT" + length);
//		System.out.println("fix" + this.ingredientCollector.fixingIngredients.size());
//		if(length>0)
//			return "";
		for(int i=0; i<length; i++) {
			FaultyNode faultyNode = this.ingredientCollector.faultyNodes.get(i);
//			System.out.println(faultyNode);
			this.count = patchGenerator.candidatePatchesList.size()-1;
			if(this.count<0) {
				this.count = 0;
			}
			replaceHandler.replace(faultyNode);
		}

		faultyClass.compilationUnit = (CompilationUnit)ASTNode.copySubtree(compilationUnit.getAST(), compilationUnit);
		faultyClass.fileContent = fileContent;
		faultyClass.rank = this.faultyClasses.size();
		this.faultyClasses.add(faultyClass);

		candidatePatchesDirectory = new File("mutants/"+program.rootDirectory);
		File mutantDirectory = new File("mutants/"+program.rootDirectory+faultyClass.rank); 
		mutantDirectory.mkdirs();

		try {
			FileUtils.copyDirectory(new File(program.rootDirectory), mutantDirectory);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return "done";
	}



	private void init(String filename) {
		Tokenizer.stringTokenPairList.clear();
		//		this.count = 0;
		//		this.correctPatches = 0;
		//		this.candidatePatchesList.clear();
		//		VariableCollector.variables.clear();
		IngredientCollector.filename = filename;
		this.ingredientCollector.faultyNodes.clear();
		this.ingredientCollector.fixingIngredients.clear();
	}

	public String readFileToString(String filePath) {
		StringBuilder fileData = new StringBuilder(100000);
		try{		
			BufferedReader reader = new BufferedReader(new FileReader(filePath));

			char[] buffer = new char[10];
			int numRead = 0;
			while ((numRead = reader.read(buffer)) != -1) {
				String readData = String.valueOf(buffer, 0, numRead);
				fileData.append(readData);
				buffer = new char[1024];
			}

			reader.close();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}

		return  fileData.toString();	
	}


	public File generateConcretePatch(CandidatePatch candidatePatch) {
		Iterator<FaultyClass> faultyClassIterator = this.faultyClasses.iterator(); 
		while(faultyClassIterator.hasNext()){
			FaultyClass faultyClass = faultyClassIterator.next();
			if(faultyClass.filename.equals(candidatePatch.filename)) {
				Document currentDocument = new Document(faultyClass.fileContent);

				ASTRewrite rewriter = ASTRewrite.create(faultyClass.compilationUnit.getAST());
				try{
					rewriter.replace(candidatePatch.faultyNode, candidatePatch.fixingIngredient, null);
					TextEdit edits = rewriter.rewriteAST(currentDocument,null);
					edits.apply(currentDocument);

					Program program = Program.createProgram();
					File mutantDirectory = new File("mutants/"+program.rootDirectory+faultyClass.rank);
					//	 				System.out.println("Modified file: "+ mutantDirectory+"/"+program.sourceFilesDirectory+"/"+(faultyClass.filename));
					generateProgramVariant(new File(mutantDirectory+"/"+program.sourceFilesDirectory+"/"+(faultyClass.filename)),currentDocument);
					return mutantDirectory;
				} catch(Exception e) {
					//	 				System.out.println("ERROR!!!!!!!!!!!!!!!!!!!");

					//	 				System.out.println(candidatePatch.faultyNode+ " "+compilationUnit.getLineNumber(candidatePatch.faultyNode.getStartPosition()));
					//	 				System.out.println(candidatePatch.fixingIngredient+ " "+compilationUnit.getLineNumber(candidatePatch.fixingIngredient.getStartPosition()));
					//	 							System.out.println(ModelExtractor.getNodeType(candidatePatch.faultyNode));
					//	 							System.out.println(ModelExtractor.getNodeType(candidatePatch.fixingIngredient));
					//	 				System.out.println(e.getMessage());
					//			e.printStackTrace();
					return null;
				} catch(StackOverflowError overflow) {
					//	 				System.out.println("OVERFLOW!!!!!!!!!!!!!!!!");
					//	 				System.out.println(candidatePatch.faultyNode+ " "+compilationUnit.getLineNumber(candidatePatch.faultyNode.getStartPosition()));
					//	 				System.out.println(candidatePatch.fixingIngredient+ " "+compilationUnit.getLineNumber(candidatePatch.fixingIngredient.getStartPosition()));
					//	 							System.out.println(ModelExtractor.getNodeType(candidatePatch.faultyNode));
					//	 							System.out.println(ModelExtractor.getNodeType(candidatePatch.fixingIngredient));
					//	 				System.out.println(overflow.getMessage());
					//	 							System.out.println();
					return null;
				}
			}
		}
		return null;
	}



	void generateProgramVariant(File file, Document document) {
		try {
			FileWriter fileWrite = new FileWriter(file.getAbsolutePath());
			fileWrite.write(document.get());
			fileWrite.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
