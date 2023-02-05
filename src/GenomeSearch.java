import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.lang.StringBuilder;
import java.lang.Integer;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;


public class GenomeSearch {

	static Map<String, StringBuilder> bySequence = new HashMap<String, StringBuilder>();

	public static void main(String[] args) throws IOException {
		//gets target search files from arguments
        String targets = args[0];
        File corpus = new File(args[1]);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(targets)));
        
        Node root = new Node();

        String seq;
        
        while ((seq = reader.readLine()) != null) {
            buildTrie(root, seq); 
        }

        File[] files = corpus.listFiles();
        Set<File> fileSet = new TreeSet<File>(Arrays.asList(files)); //to sort files alphanumerically

        for (File f : fileSet) {
        	searchTrie(f, root);
        }

        //prints to extra-credit file by sequence
        File extra = new File("extra");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(extra)));
        for (String s : bySequence.keySet()) {
        	writer.write(s);
        	writer.newLine();
        	writer.write(bySequence.get(s).toString());
        	writer.newLine();
        }

        writer.close();

	}

	public static void buildTrie(Node root, String line) {
		Node node = root;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			//if node does not have a child for this nucleotide
			if (node.get(ch) == null) { 
				node.set(ch, new Node());
			}
			//move pointer to child for this nucleotide
			node = node.get(ch);
		}
		//stores payload at terminal nodes
		node.setPayload(line);

	}

	public static void searchTrie(File file, Node root) {
		try {
			int i = 0; //outer index sets overall position
			Node pn = root;
			//print current file name to output
			System.out.println(file.getName());

			//read from current file
	        RandomAccessFile chrFile = new RandomAccessFile(file, "r");
	        FileChannel fileChannel = chrFile.getChannel();
	        long limit = fileChannel.size();
	        //"inner index position" scans through file
	        MappedByteBuffer j = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, limit);

	        while (i < limit && j.hasRemaining()) {
	        	char ch = (char) j.get();
	        	if (pn.isTerminal()) { //match found
	        		//converts index i to hex number, pads to 8 digits, prints match to output
	        		String offset = String.format("%1$08X", i);
	        		System.out.println("\t" + offset + "\t" + pn.getPayload());
	        		//to map matches by sequence
	        		mapMatches(pn.getPayload(), file.getName(), offset);
	        		//increments overall position
	        		i++;
	        		//resets inner position
		        	j.position(i);
		        	//resets node pointer
		        	pn = root;	
	        	} else if (pn.get(ch) == null) { //no match
	        		//increments overall position
	        		i++;
	        		//resets inner position
		        	j.position(i);
		        	//resets node pointer
		        	pn = root;
	        	} else { //continues searching trie
	        		pn = pn.get(ch);
	        	}
	        } 
	        
	        chrFile.close();

    	} catch (IOException ex) {
    		System.out.println(ex.toString());
		} 
	}

	//records matches in a map by sequence
	public static void mapMatches(String seq, String fileName, String offset) {
		StringBuilder value = new StringBuilder("\t" + offset + "\t" + fileName);
		if (bySequence.containsKey(seq)) {
			StringBuilder current = bySequence.get(seq);
			bySequence.put(seq, current.append("\n" + value));
		} else {
			bySequence.put(seq, value);
		}
	}

}

//trie node class
class Node {

	private Node a;
	private Node t;
	private Node c;
	private Node g;
	private String payload;

	public Node() {
		a = null;
		t = null;
		c = null;
		g = null;
		payload = null;
	}

	//creates child for appropriate nucleotide
	public void set(char ch, Node n) {
		if (ch == 'A' || ch == 'a') {
			a = n;
		} else if (ch == 'T' || ch == 't') {
			t = n;
		} else if (ch == 'C' || ch == 'c') {
			c = n;
		} else if (ch == 'G' || ch == 'g') {
			g = n;
		} 
	}

	public void setPayload(String str) {
		payload = str;
	}

	//gets child for appropriate nucleotide
	public Node get(char ch) {
		if (ch == 'A' || ch == 'a') {
			return a;
		} else if (ch == 'T'|| ch == 't') {
			return t;
		} else if (ch == 'C' || ch == 'c') {
			return c;
		} else if (ch == 'G' || ch == 'g') {
			return g;
		} else {
			return null;
		}

	}

	public String getPayload() {
		return payload;
	}

	//checks if all children are null
	public boolean isTerminal() {
		if (a == null && t == null && c == null && g == null) {
			return true;
		} else {
			return false;
		}
	}

}

