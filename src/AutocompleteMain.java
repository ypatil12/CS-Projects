import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

/**
 * Main class for the Autocomplete program.
 * 
 * @author Austin Lu
 *
 */
public class AutocompleteMain {

	/* Modify K as necessary */
	static int K = 10;

	final static String BRUTE_AUTOCOMPLETE = "BruteAutocomplete";
	final static String BINARY_SEARCH_AUTOCOMPLETE = "BinarySearchAutocomplete";
	final static String TRIE_AUTOCOMPLETE = "TrieAutocomplete";

	/* Modify name of Autocompletor implementation as necessary */
	
	//final static String AUTOCOMPLETOR_CLASS_NAME = BRUTE_AUTOCOMPLETE;
	//final static String AUTOCOMPLETOR_CLASS_NAME = BINARY_SEARCH_AUTOCOMPLETE;
	final static String AUTOCOMPLETOR_CLASS_NAME = TRIE_AUTOCOMPLETE;

	public static void main(String[] args) {
		String filename = null;
		if (args.length >= 2) {
			filename = args[0];
			K = Integer.parseInt(args[1]);
		}
		JFileChooser fileChooser = new JFileChooser(".");
		int retval = fileChooser.showOpenDialog(null);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File file;
			if (filename == null)
				file = fileChooser.getSelectedFile();
			else
				file = new File(filename);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new AutocompleteGUI(file.getAbsolutePath(), K, AUTOCOMPLETOR_CLASS_NAME).setVisible(true);
				}
			});
		}
	}
}
