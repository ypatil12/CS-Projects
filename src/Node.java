import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Node in a general trie, each representing a character. Each node will keep
 * track of additional valid state if it is the last character of a word.
 * 
 * @author Austin Lu
 *
 */
public class Node implements Comparable<Node> {
	/**
	 * The character this Node represents
	 */
	String myInfo;

	/**
	 * Whether or not this node represents the last character in a word
	 */
	boolean isWord;

	/**
	 * Only non-null/interpretable if isWord is true. Holds the entire word ending
	 * at this Node.
	 */
	String myWord;

	/**
	 * Only positive/interpretable if isWord is true. Represents the weight of
	 * myWord.
	 */
	double myWeight = -1;

	/**
	 * The maximum weight of any Node rooted at this Node (i.e. in this Nodes
	 * subtrie, including this Node itself).
	 */
	double mySubtreeMaxWeight;

	Map<Character, Node> children;
	Node parent;

	public Node(char character, Node parentNode, double subtreeMaximumWeight) {
		myInfo = "" + character;
		isWord = false;
		children = new HashMap<Character, Node>();
		parent = parentNode;
		mySubtreeMaxWeight = subtreeMaximumWeight;
	}

	/**
	 * Set the word that this node is the last character of. Only do this if the
	 * Node's character ends a word.
	 */
	public void setWord(String word) {
		myWord = word;
	}

	public String getWord() {
		return myWord;
	}

	/**
	 * Set the weight corresponding to the word that this node is the last character
	 * of.
	 */
	public void setWeight(double w) {
		myWeight = w;
	}

	public double getWeight() {
		return myWeight;
	}

	/**
	 * Returns null if key is not a valid child.
	 */
	Node getChild(char ch) {
		return children.get(ch);
	}

	@Override
	public String toString() {
		return myInfo + " (" + myWeight + ")";
	}

	@Override
	public int compareTo(Node o) {
		// Sort in weight ascending
		if (this.myWeight < o.myWeight) {
			return -1;
		} else if (this.myWeight > o.myWeight) {
			return 1;
		} else {
			return 0;
		}
	}

	/*
	 * In reverse subtreeMaxWeight order to make the PriorityQueue (a min-heap) act
	 * as a max heap.
	 */
	public static class ReverseSubtreeMaxWeightComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			if (o1.mySubtreeMaxWeight < o2.mySubtreeMaxWeight) {
				return 1;
			} else if (o1.mySubtreeMaxWeight > o2.mySubtreeMaxWeight) {
				return -1;
			}
			return 0;
		}
	}
}
