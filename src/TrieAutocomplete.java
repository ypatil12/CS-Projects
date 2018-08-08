import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * General trie/priority queue algorithm for implementing Autocompletor
 * 
 * @author Austin Lu
 * @author Jeff Forbes
 */
public class TrieAutocomplete implements Autocompletor {

	/**
	 * Root of entire trie
	 */
	protected Node myRoot;

	/**
	 * Constructor method for TrieAutocomplete. Should initialize the trie rooted at
	 * myRoot, as well as add all nodes necessary to represent the words in terms.
	 * 
	 * @param terms
	 *            - The words we will autocomplete from
	 * @param weights
	 *            - Their weights, such that terms[i] has weight weights[i].
	 * @throws NullPointerException
	 *             if either argument is null
	 * @throws IllegalArgumentException
	 *             if terms and weights are different length
	 */
	public TrieAutocomplete(String[] terms, double[] weights) {
		if (terms == null || weights == null) {
			throw new NullPointerException("One or more arguments null");
		}
		//Exception for terms and weights
		if (terms.length != weights.length) {
			throw new IllegalArgumentException("Terms and weights are different lengths");
		}

		// Represent the root as a dummy/placeholder node
		myRoot = new Node('-', null, 0);

		for (int i = 0; i < terms.length; i++) {
			add(terms[i], weights[i]);
		}
	}

	/**
	 * Add the word with given weight to the trie. If word already exists in the
	 * trie, no new nodes should be created, but the weight of word should be
	 * updated.
	 * 
	 * In adding a word, this method should do the following: Create any necessary
	 * intermediate nodes if they do not exist. Update the subtreeMaxWeight of all
	 * nodes in the path from root to the node representing word. Set the value of
	 * myWord, myWeight, isWord, and mySubtreeMaxWeight of the node corresponding to
	 * the added word to the correct values
	 * 
	 * @throws a
	 *             NullPointerException if word is null
	 * @throws an
	 *             IllegalArgumentException if weight is negative.
	 */
	private void add(String word, double weight) {
		//Exception weight being negative
		if (weight < 0) {
			throw new IllegalArgumentException("Illegal Weight");
		}
		//Exception if word is null 
		if (word == null) {
			throw new NullPointerException("One or more arguments null");
		}
		//Initialize node current as the root
		Node current = myRoot;
		//Add new nodes to the trie and set weight for each
		for (int i = 0; i < word.length(); i++) {
			char ch = word.charAt(i);
			//Set mySubtreeMaxWeight to be the maximum of given weight and mySubt
			if (current.mySubtreeMaxWeight<weight) {
				current.mySubtreeMaxWeight = weight;
			}
			if (current.children.get(ch)==null) {
				current.children.put(ch, new Node(ch, current, weight));
			}
			current = current.children.get(ch);
		}
		//Set the current node to being a word because we've added entire string to the trie
		current.isWord = true;
		//Set the current node to have the specific word given because it is the last element
		current.myWord = word;
		//Set weight of final node to be given weight
		current.myWeight = weight;
	}
	/**
	 * Required by the Autocompletor interface. Returns an array containing the k
	 * words in the trie with the largest weight which match the given prefix, in
	 * descending weight order. If less than k words exist matching the given prefix
	 * (including if no words exist), then the array instead contains all those
	 * words. e.g. If terms is {air:3, bat:2, bell:4, boy:1}, then topKMatches("b",
	 * 2) should return {"bell", "bat"}, but topKMatches("a", 2) should return
	 * {"air"}
	 * 
	 * @param prefix
	 *            - A prefix which all returned words must start with
	 * @param k
	 *            - The (maximum) number of words to be returned
	 * @return An Iterable of the k words with the largest weights among all words
	 *         starting with prefix, in descending weight order. If less than k such
	 *         words exist, return all those words. If no such words exist, return
	 *         an empty Iterable
	 * @throws a
	 *             NullPointerException if prefix is null
	 */
	public Iterable<String> topMatches(String prefix, int k) {
		if (prefix == null) {
			throw new NullPointerException("Invalid parameter");
		}
		if (k < 0) {
			throw new IllegalArgumentException("Illegal value of k:" + k);
		}
		//Initialize current node as root
		Node current = myRoot;
		//Initialize empty iterable arraylist to return in special cases
		ArrayList<String> emptyCase = new ArrayList<>();
		if (k == 0) {
			return emptyCase;
		}
		//Get to the prefix node
		for (int i = 0; i < prefix.length(); i++) {
			char ch = prefix.charAt(i);
			current = current.children.get(ch);
			//If prefix is not stored return an empty string
			if(current == null) {
				return emptyCase;
			}
		}
		//Initialize priority queue for nodes
		PriorityQueue<Node> nodePQ = new PriorityQueue<Node>(new Node.ReverseSubtreeMaxWeightComparator());
		//Initialize Priority Queue for terms
		PriorityQueue<Term> termPQ = new PriorityQueue<Term>(k, new Term.WeightOrder());
		//Add the current node to the priority queue
		nodePQ.add(current);
		while (nodePQ.size()>0) {
			current = nodePQ.remove();
			if(current.isWord) {
				termPQ.add(new Term(current.getWord(), current.getWeight()));
			}
			if (termPQ.size() > k) {
				termPQ.remove();
			}
			if (termPQ.peek() != null && nodePQ.peek() != null && termPQ.peek().getWeight() > nodePQ.peek().mySubtreeMaxWeight && termPQ.size() == k) {
					break;
			}
			for (Node below: current.children.values()) {
				nodePQ.add(below);
			}
		}
		LinkedList<String> ret = new LinkedList<String>();
		while(termPQ.size()>0) {
			ret.addFirst(termPQ.remove().getWord());
		}
		return ret;
	}

	/**
	 * Given a prefix, returns the largest-weight word in the trie starting with
	 * that prefix.
	 * 
	 * @param prefix
	 *            - the prefix the returned word should start with
	 * @return The word from with the largest weight starting with prefix, or an
	 *         empty string if none exists
	 * @throws a
	 *             NullPointerException if the prefix is null
	 */
	public String topMatch(String prefix) {
		if (prefix == null) {
			throw new NullPointerException("Invalid Prefix");
		}
		Node current = myRoot;	
		for (int i = 0; i< prefix.length(); i++) {
			char ch = prefix.charAt(i);
			current = current.children.get(ch);
			//If prefix is not stored return an empty string
			if(current == null) {
				return "";
			}
		}
		while(current.mySubtreeMaxWeight != current.myWeight) {
			for (Node below: current.children.values()) {
				if (below.mySubtreeMaxWeight == current.mySubtreeMaxWeight) {
					current = below;
					break;
				}
			}
		}
		return current.getWord();
}

	/**
	 * Return the weight of a given term. If term is not in the dictionary, return
	 * 0.0
	 */
	public double weightOf(String term) {
		Node current = myRoot;
		char[] chars = term.toCharArray();
		for (char c: chars) {
			if (current.children == null) {
				return 0.0;
			}
			current = current.children.get(c);
		}
		return current.getWeight();
	}
}
