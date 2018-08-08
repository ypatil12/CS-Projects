/**
 * An Autocompletor supports returning either the top k best matches, or the
 * single top match, given a String prefix.
 * 
 * @author Austin Lu
 *
 */
public interface Autocompletor {

	/**
	 * Returns the top k matching terms in descending order of weight. If there
	 * are fewer than k matches, return all matching terms in descending order
	 * of weight. If there are no matches, return an empty iterable.
	 */
	public Iterable<String> topMatches(String prefix, int k);

	/**
	 * Returns the single top matching term, or an empty String if there are no
	 * matches.
	 */
	public String topMatch(String prefix);

	/**
	 * Return the weight of a given term. If term is not in the dictionary,
	 * return 0.0
	 */
	public double weightOf(String term);

}
