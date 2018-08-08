import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JFileChooser;

public class AutocompletorBenchmark {

	public static Random ourRandom = new Random(1234);
	public static final String CHARSET = "UTF-8";
	public static final Locale LOCALE = Locale.US;
	public static Autocompletor getInstance(String[] words, double[] weights) {
		//return new BruteAutocomplete(words, weights);
		//return new BinarySearchAutocomplete(words,weights);
		return new TrieAutocomplete(words,weights);
	}
	// chooser allows users to select a file by navigating through
	// directories
	private static JFileChooser ourChooser = new JFileChooser(System
			.getProperties().getProperty("user.dir"));

	/**
	 * Brings up chooser for user to select a file
	 * 
	 * @return Scanner for user selected file, null if file not found
	 */
	public static Scanner getScanner() {
		int retval = ourChooser.showOpenDialog(null);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File f = ourChooser.getSelectedFile();
			Scanner s;
			try {
				if (f.canRead()) {
					System.out.println("Opening - " +  f.getCanonicalPath() + ".");
				} else {
					System.out.println("Could not open selected file.");
					return null;
				}
				s = new Scanner(f, CHARSET);
				s.useLocale(LOCALE);
				
			} catch (IOException e) {
				return null;
			}
			return s;
		}
		return null;
	}

	public static long countNodes(Node root) {
		long result = 1;
		for (Node child : root.children.values())
			result += countNodes(child);
		return result;
	}

		public static void main(String[] args) {
		
		Scanner in = null;
		do {
			in = getScanner();
			
		} while (in == null);
		
		int N = 0;
		String[] terms = null;
		double[] weights = null;
		try {
			N = Integer.parseInt(in.nextLine());
			terms = new String[N];
			weights = new double[N];

			for (int i = 0; i < N; i++) {
				String line = in.nextLine();
				int tab = line.indexOf('\t');
				weights[i] = Double.parseDouble(line.substring(0, tab).trim());
				terms[i] = line.substring(tab + 1).toLowerCase();
			}
		} catch (Exception e) { //could be any parsing related exception
			System.err.println("File is malformatted");
			System.exit(0);
		}
		long startTime = System.nanoTime();
		Autocompletor auto = getInstance(terms, weights);
		System.out.println("Benchmarking " + auto.getClass().getName() + "...");
		System.out.println("Found " + N + " words");
		System.out.println("Time to initialize - " + (System.nanoTime() - startTime) / 1E9);
		if (auto instanceof TrieAutocomplete)
			System.out.println("Created " + countNodes(((TrieAutocomplete) auto).myRoot) + " nodes");
		String randomWord = "";
		while (randomWord.length() <= 2)
			randomWord = terms[ourRandom.nextInt(terms.length)];
		String randomPrefix1 = randomWord.substring(0, 1);
		String randomPrefix2 = randomWord.substring(0, 2);
		String[] queries = { "", randomWord, randomPrefix1, randomPrefix2, "notarealword" };
		int trial;
		for (String query : queries) {
			startTime = System.nanoTime();
			for (trial = 0; trial < 1000; trial++) {
				auto.topMatch(query);
				if (System.nanoTime() - startTime > 5E9)
					break;
			}
			System.out.println(
					"Time for topMatch(\"" + query + "\") - " + (System.nanoTime() - startTime) / (1E9 * trial));
		}
		final int NTRIALS = 100;
		for (String query : queries) {
			for (int k = 1; k <= 256; k *= 2) {
				startTime = System.nanoTime();
				for (trial = 0; trial < NTRIALS; trial++) {
					auto.topMatches(query, k);
					if (System.nanoTime() - startTime > 5E9)
						break;
				}
				System.out.println("Time for topKMatches(\"" + query + "\", " + k + ")" + " - " + " "
						+ (System.nanoTime() - startTime) / (1E9 * NTRIALS));
			}
		}
	}
}
