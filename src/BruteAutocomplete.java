import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Implements Autocompletor by scanning through the entire array of terms for
 * every topKMatches or topMatch query.
 */
public class BruteAutocomplete implements Autocompletor {

	Term[] myTerms;

	/**
	 * Create immutable instance with terms constructed from parameter
	 * 
	 * @param terms
	 *            words such that terms[k] is part of a word pair 0 <= k <
	 *            terms.length
	 * @param weights
	 *            weights such that weights[k] corresponds to terms[k]
	 * @throws NullPointerException
	 *             if either parameter is null
	 * @throws IllegalArgumentException
	 *             if terms.length != weights.length
	 *             if any element of weights is negative
	 *             if any element of terms is a duplicate
	 */
	public BruteAutocomplete(String[] terms, double[] weights) {

		if (terms == null || weights == null) {
			throw new NullPointerException("One or more arguments null");
		}

		if (terms.length != weights.length) {
			throw new IllegalArgumentException("terms and weights are not the same length");
		}

		myTerms = new Term[terms.length];

		HashSet<String> words = new HashSet<String>();

		for (int i = 0; i < terms.length; i++) {
			words.add(terms[i]);
			myTerms[i] = new Term(terms[i], weights[i]);
			if (weights[i] < 0) {
				throw new IllegalArgumentException("Negative weight " + weights[i]);
			}
		}
		if (words.size() != terms.length) {
			throw new IllegalArgumentException("Duplicate input terms");
		}
	}

	public Iterable<String> topMatches(String prefix, int k) {
		if (k < 0) {
			throw new IllegalArgumentException("Illegal value of k:" + k);
		}

		// maintain pq of size k
		PriorityQueue<Term> pq = new PriorityQueue<Term>(k, new Term.WeightOrder());
		for (Term t : myTerms) {
			if (!t.getWord().startsWith(prefix))
				continue;
			if (pq.size() < k) {
				pq.add(t);
			} else if (pq.peek().getWeight() < t.getWeight()) {
				pq.remove();
				pq.add(t);
			}
		}
		int numResults = Math.min(k, pq.size());
		LinkedList<String> ret = new LinkedList<String>();
		for (int i = 0; i < numResults; i++) {
			ret.addFirst(pq.remove().getWord());
		}
		return ret;
	}

	public String topMatch(String prefix) {
		String maxTerm = "";
		double maxWeight = -1;
		for (Term t : myTerms) {
			if (t.getWeight() > maxWeight && t.getWord().startsWith(prefix)) {
				maxWeight = t.getWeight();
				maxTerm = t.getWord();
			}
		}
		return maxTerm;
	}

	public double weightOf(String term) {
		for (Term t : myTerms) {
			if (t.getWord().equalsIgnoreCase(term))
				return t.getWeight();
		}
		// term is not in dictionary return 0
		return 0;
	}
}
