import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Tokenizer {
	private static Tokenizer tokenizer;
	static ArrayList<StringTokenPair> stringTokenPairList = new ArrayList<StringTokenPair>();
	String[] seperators = {"(", ")", "{", "}", "[", "]", ";", ",", ".", "...", "@", "::"};
	List<String> seperatorList;
	
	private Tokenizer() {
		this.seperatorList = (List<String>) Arrays.asList(seperators);
	}
	
	public static Tokenizer createTokenizer() {
		if(tokenizer == null){
			tokenizer = new Tokenizer();
		}

		return tokenizer;
	}
	
	void tokenize(List<? extends Node>nodes) {
		HashMap<String,Integer> tokens = new HashMap<String,Integer>();  
		try {
//			System.out.println("Tokenizing "+expression);
			
//			FileWriter fileWrite = new FileWriter("out.txt");
//			
//			fileWrite.write();
//			fileWrite.close();
			writeExpressions(nodes);	
			ProcessBuilder processBuilder = new ProcessBuilder("python3","tokenizer.py");
			Process process = processBuilder.start();

			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String token;
			
			int i=0;
			while((token = in.readLine())!=null) {
				if(token.length()==0) {
//					System.out.println("EMPTY LINE");
//					System.out.println("I "+i);
//					System.out.println("LIST " +tokens);
					Node node = nodes.get(i);
					node.tokens.putAll(tokens);;
					i++;
					tokens.clear();
				}
				else {
//					System.out.println("TOKEN " +token);
					if(tokens.containsKey(token)) {
						tokens.put(token, tokens.get(token)+1);
					}
					else {
						tokens.put(token, 1);
					}
				}	
			}
			
//			System.out.println(lineWiseTokenList.size());
//			
//			for (int i=0;i<lineWiseTokenList.size();i++) {
//				System.out.println(lineWiseTokenList.get(i));
//			}
			
//			System.out.println("\n\n");
			
//			String []tokens = list.split(",");
//			
//			NodeTokens nodeTokens = new NodeTokens();
//			for (int i=0; i<tokens.length;i++) {
//				if(nodeTokens.tokens.containsKey(tokens[i])) {
//					nodeTokens.tokens.put(tokens[i], nodeTokens.tokens.get(tokens[i])+1);
//				}
//				else {
//					nodeTokens.tokens.put(tokens[i], 1);
//				}
//			}
//			nodeWiseTokenList.add(nodeTokens);
//			System.out.println("FINAL LIST!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! \n\n\n");
//			for(i=0; i<nodes.size(); i++) {
//				System.out.println(nodes.get(i).node.toString());
//				System.out.println(nodes.get(i).tokens);
//			}
			
		} catch(Exception e){
//			System.out.println("PROBLEM!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	void writeExpressions(List<? extends Node>nodes) {
		File newfile = new File("expressions.txt");

		try {
			FileWriter fileWrite = new FileWriter(newfile.getAbsolutePath());
			for(int i=0; i<nodes.size(); i++) {
//				System.out.println(nodes.get(i).node.toString());
				Node currentNode = nodes.get(i);
				String str = currentNode.node.toString();
				if(currentNode.startLine!=currentNode.endLine) {
					str = str.replaceAll("[\\t\\n\\r]+"," ");
				}
				fileWrite.write(str+" \n");
			}

			fileWrite.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
