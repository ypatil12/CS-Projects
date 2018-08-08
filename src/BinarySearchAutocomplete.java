import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
/**
 * 
 * Using a sorted array of Term objects, this implementation uses binary search
 * to find the top term(s).
 * 
 * @author Austin Lu, adapted from Kevin Wayne
 * @author Jeff Forbes
 */
public class BinarySearchAutocomplete implements Autocompletor {

	Term[] myTerms;

	/**
	 * Given arrays of words and weights, initialize myTerms to a corresponding
	 * array of Terms sorted lexicographically.
	 * 
	 * This constructor is written for you, but you may make modifications to it.
	 * 
	 * @param terms
	 *            - A list of words to form terms from
	 * @param weights
	 *            - A corresponding list of weights, such that terms[i] has
	 *            weight[i].
	 * @return a BinarySearchAutocomplete whose myTerms object has myTerms[i] = a
	 *         Term with word terms[i] and weight weights[i].
	 * @throws a
	 *             NullPointerException if either argument passed in is null
	 * @throws IllegalArgumentException
	 *             if terms and weights are different length
	 */
	public BinarySearchAutocomplete(String[] terms, double[] weights) {
		if (terms == null || weights == null) {
			throw new NullPointerException("One or more arguments null");
		}
		if (terms.length != weights.length) {
			throw new IllegalArgumentException("Weights and terms are different lengths");
		}
		myTerms = new Term[terms.length];

		for (int i = 0; i < terms.length; i++) {
			if (weights[i] < 0) {
				throw new IllegalArgumentException("Negative weight:" + terms[i]);
			}
			myTerms[i] = new Term(terms[i], weights[i]);
		}
		Arrays.sort(myTerms);
	}

	/**
	 * Uses binary search to find the index of the first Term in the passed in array
	 * which is considered equivalent by a comparator to the given key. This method
	 * should not call comparator.compare() more than 1+log n times, where n is the
	 * size of a.
	 * 
	 * @param a
	 *            - The array of Terms being searched
	 * @param key
	 *            - The key being searched for.
	 * @param comparator
	 *            - A comparator, used to determine equivalency between the values
	 *            in a and the key.
	 * @return The first index i for which comparator considers a[i] and key as
	 *         being equal. If no such index exists, return -1 instead.
	 */
	public static int firstIndexOf(Term[] a, Term key, Comparator<Term> comparator) {
		if (a.length==0) {
			return -1;
		}
		List<Term> list = Arrays.asList(a);
		int low = -1;
		int high = a.length - 1;
		while (low+1 != high) {
			int mid = (low + high)/2;
			Term midval = list.get(mid);
			int trueVal = comparator.compare(midval, key);
			if (trueVal < 0) {
				low = mid;
			}
			else if (trueVal >= 0) {
				high = mid;
			}
		}
		if (comparator.compare(list.get(high), key) == 0){
			return high;
		}
		return -1;
	}

	/**
	 * The same as firstIndexOf, but instead finding the index of the last Term.
	 * 
	 * @param a
	 *            - The array of Terms being searched
	 * @param key
	 *            - The key being searched for.
	 * @param comparator
	 *            - A comparator, used to determine equivalency between the values
	 *            in a and the key.
	 * @return The last index i for which comparator considers a[i] and key as being
	 *         equal. If no such index exists, return -1 instead.
	 */
	public static int lastIndexOf(Term[] a, Term key, Comparator<Term> comparator) {
		if (a.length == 0) {
			return -1;
		}
		List<Term> list = Arrays.asList(a);
		int low = 0;
		int high = a.length;
		while (low + 1 != high) {
			int mid = (low + high)/2;
			Term midval = list.get(mid);
			int trueVal = comparator.compare(midval, key);
			if (trueVal <= 0) {
				low = mid;
			}
			else if (trueVal > 0) {
				high = mid;
			}
		}
		if (comparator.compare(list.get(low), key) == 0) {
			return low;
		}
		return -1;
	}

	/**
	 * Required by the Autocompletor interface. Returns an array containing the k
	 * words in myTerms with the largest weight which match the given prefix, in
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
	 * @return An array of the k words with the largest weights among all words
	 *         starting with prefix, in descending weight order. If less than k such
	 *         words exist, return an array containing all those words If no such
	 *         words exist, reutrn an empty array
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
		//Create new term objects for prefix that has a weight 0 & find lower and upper bound weights
		int lowBound = firstIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		int upBound = lastIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		//Empty case iterable element
		ArrayList<String> emptyCase = new ArrayList<>();
		//If the firstIndex doesn't exist or the lastIndex doens't exist, then return an empty array
		if(lowBound < 0 || upBound < 0 || k == 0) {
			return emptyCase;
		}
		//Make priority queue for weights
		PriorityQueue<Term> pq = new PriorityQueue<Term>(k, new Term.WeightOrder());
		//Iterate through the terms given and add those to priority queue with up to k elements
		for (int i = lowBound; i <= upBound; i++) {
			if (pq.size() < k) {
				pq.add(myTerms[i]);
			} else if (pq.peek().getWeight() < myTerms[i].getWeight()) {
				pq.remove();
				pq.add(myTerms[i]);
			}
		}
		LinkedList<String> ret = new LinkedList<String>();
		//If k > M, then make the parameter, then return all strings that specify the prefix
		int param = Math.min(pq.size(), k);
		for (int i = 0; i < param; i++) {
			ret.addFirst(pq.remove().getWord());
		}
		return ret;
	}

	/**
	 * Given a prefix, returns the largest-weight word in myTerms starting with that
	 * prefix. e.g. for {air:3, bat:2, bell:4, boy:1}, topMatch("b") would return
	 * "bell". If no such word exists, return an empty String.
	 * 
	 * @param prefix
	 *            - the prefix the returned word should start with
	 * @return The word from myTerms with the largest weight starting with prefix,
	 *         or an empty string if none exists
	 * @throws a
	 *             NullPointerException if the prefix is null
	 * 
	 */
	public String topMatch(String prefix) {
		//Throw exception
		if (prefix == null) {
			throw new NullPointerException("Invalid parameter");
		}
		//Create new term objects for prefix that has a weight 0 & find lower and upper bound weights
		int lowBound = firstIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		int upBound = lastIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		//Initialize empty term
		Term match = new Term("", 0);
		//If the firstIndex doesn't exist or the lastIndex doens't exist, then return an empty array
		if(lowBound < 0 || upBound < 0) {
			return match.getWord();
		}
		//Iterate through the terms given and add those to priority queue with up to k elements
		for (int i = lowBound; i <= upBound; i++) {
			if (myTerms[i].getWeight() > match.getWeight()) {
				match = myTerms[i];
			}
		}
		return match.getWord();
	}

	/**
	 * Return the weight of a given term. If term is not in the dictionary, return
	 * 0.0
	 */
	public double weightOf(String term) {
		for (Term T:myTerms) {
			if (T.getWord() == term) {
				return T.getWeight();
			}
		}
		return 0.0;
	}
}
