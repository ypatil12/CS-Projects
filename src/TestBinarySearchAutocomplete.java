import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

import org.junit.Test;

public class TestBinarySearchAutocomplete {

	private Term[] myTerms = new Term[] { new Term("ape", 0), new Term("apple", 0), new Term("bat", 0),
			new Term("bee", 0), new Term("cat", 0) };
	private String[] myNames = { "ape", "app", "ban", "bat", "bee", "car", "cat" };
	private double[] myWeights = { 6, 4, 2, 3, 5, 7, 1 };

	/** A comparator which considers all terms equal **/
	public class AllEqual implements Comparator<Term> {
		public int compare(Term o1, Term o2) {
			return 0;
		}
	}

	/**
	 * A comparator which is basically Term.PrefixOrder, but counts the number
	 * of compare calls made
	 */
	public class CompareCounter implements Comparator<Term> {

		private Term.PrefixOrder comparator;
		private int count = 0;

		public CompareCounter(int r) {
			comparator = new Term.PrefixOrder(r);
		}

		@Override
		public int compare(Term o1, Term o2) {
			count++;
			return comparator.compare(o1, o2);
		}

		public int compareCount() {
			return count;
		}

	}

	/**
	 * Sorts terms by ascending weight
	 */
	public class WeightSorter implements Comparator<Term> {

		@Override
		public int compare(Term o1, Term o2) {
			return (int) (100 * (o1.getWeight() - o2.getWeight()));
		}

	}

	public Autocompletor getInstance() {
		return getInstance(myNames, myWeights);
	}

	public Autocompletor getInstance(String[] names, double[] weights) {
		return new BinarySearchAutocomplete(names, weights);
	}

	public class Autoincompletor extends BinarySearchAutocomplete {

		public Autoincompletor(String[] terms, double[] weights) {
			super(terms, weights);
		}

		@Override
		public Iterable<String> topMatches(String prefix, int k) {
			return new LinkedList<String>();
		}
	}

	/**
	 * Tests correctness of topMatch() for a few simple cases
	 */
	@Test(timeout = 10000)
	public void testTopMatch() {
		Autocompletor test = getInstance();
		String[] queries = { "", "a", "ap", "b", "ba", "c", "ca", "cat", "d", " " };
		String[] results = { "car", "ape", "ape", "bee", "bat", "car", "car", "cat", "", "" };
		for (int i = 0; i < queries.length; i++) {
			String query = queries[i];
			String reported = test.topMatch(query);
			String actual = results[i];
			assertEquals("wrong top match for " + query, actual, reported);
		}
	}
	
	private String[] iterToArr(Iterable<String> it) {
		ArrayList<String> list = new ArrayList<String>();
		for (String s: it)
			list.add(s);
		return list.toArray(new String[0]);
	}
	/**
	 * Tests correctness of topKMatches() for a few simple cases
	 */
	@Test(timeout = 10000)
	public void testTopKMatches() {
		Autocompletor test = getInstance();
		String[] queries = { "", "", "", "", "a", "ap", "b", "ba", "d" };
		int[] ks = { 8, 1, 2, 3, 1, 1, 2, 2, 100 };
		String[][] results = { { "car", "ape", "bee", "app", "bat", "ban", "cat" }, { "car" }, { "car", "ape" },
				{ "car", "ape", "bee" }, { "ape" }, { "ape" }, { "bee", "bat" }, { "bat", "ban" }, {} };
		for (int i = 0; i < queries.length; i++) {
			String query = queries[i];
			String[] reported = iterToArr(test.topMatches(query, ks[i]));
			String[] actual = results[i];
			assertArrayEquals("wrong top matches for " + query + " " + ks[i], actual, reported);
		}
	}

	/**
	 * Tests correctness of simple cases where firstIndexOf should find an index
	 */
	@Test(timeout = 10000)
	public void testFirstIndexOfHits() {
		assertEquals(0, BinarySearchAutocomplete.firstIndexOf(myTerms, new Term("a", 0), new Term.PrefixOrder(1)));

		assertEquals(0, BinarySearchAutocomplete.firstIndexOf(myTerms, new Term("a", 0), new Term.PrefixOrder(1)));

		assertEquals(2, BinarySearchAutocomplete.firstIndexOf(myTerms, new Term("ba", 0), new Term.PrefixOrder(2)));

		assertEquals(4, BinarySearchAutocomplete.firstIndexOf(myTerms, new Term("cat", 0), new Term.PrefixOrder(3)));
	}

	@Test(timeout = 10000)
	public void testLastIndexOfHits() {
		assertEquals(1, BinarySearchAutocomplete.lastIndexOf(myTerms, new Term("a", 0), new Term.PrefixOrder(1)));

		assertEquals(1, BinarySearchAutocomplete.lastIndexOf(myTerms, new Term("ap", 0), new Term.PrefixOrder(2)));

		assertEquals(0, BinarySearchAutocomplete.lastIndexOf(myTerms, new Term("ape", 0), new Term.PrefixOrder(3)));

		assertEquals(3, BinarySearchAutocomplete.lastIndexOf(myTerms, new Term("b", 0), new Term.PrefixOrder(1)));

		assertEquals(4, BinarySearchAutocomplete.lastIndexOf(myTerms, new Term("c", 0), new Term.PrefixOrder(1)));
	}

	/**
	 * Tests correctness of simple cases where firstIndexOf and lastIndexOf
	 * should not find a valid index (and thus return -1)
	 */
	@Test(timeout = 10000)
	public void testFirstIndexOfMisses() {
		assertEquals(-1, BinarySearchAutocomplete.firstIndexOf(myTerms, new Term("d", 0), new Term.PrefixOrder(1)));
		assertEquals(-1, BinarySearchAutocomplete.firstIndexOf(myTerms, new Term("ab", 0), new Term.PrefixOrder(2)));
		assertEquals(-1, BinarySearchAutocomplete.firstIndexOf(myTerms, new Term("car", 0), new Term.PrefixOrder(3)));
		assertEquals(-1, BinarySearchAutocomplete.firstIndexOf(myTerms, new Term("cats", 0), new Term.PrefixOrder(4)));
	}

	/**
	 * This test checks if lastIndexOf returns -1 for missing words"
	 */
	@Test(timeout = 10000)
	public void testLastIndexOfMisses() {
		assertEquals(-1, BinarySearchAutocomplete.lastIndexOf(myTerms, new Term("d", 0), new Term.PrefixOrder(1)));
		assertEquals(-1, BinarySearchAutocomplete.lastIndexOf(myTerms, new Term("ab", 0), new Term.PrefixOrder(2)));
		assertEquals(-1, BinarySearchAutocomplete.lastIndexOf(myTerms, new Term("car", 0), new Term.PrefixOrder(3)));
		assertEquals(-1, BinarySearchAutocomplete.lastIndexOf(myTerms, new Term("cat ", 0), new Term.PrefixOrder(4)));
	}

	/**
	 * Tests if firstIndexOf/lastIndexOf return the correct first/last index
	 * when duplicates exist.
	 */
	@Test(timeout = 10000)
	public void testFirstIndexOfDuplicates() {
		Term[] terms = new Term[] { new Term("ape", 0), new Term("apple", 0), new Term("apple", 0),
				new Term("apple", 0), new Term("bat", 0), new Term("bat", 0), new Term("bat", 0), new Term("bee", 0),
				new Term("bee", 0), new Term("bee", 0), new Term("cat", 0) };
		assertEquals(0, BinarySearchAutocomplete.firstIndexOf(terms, new Term("a", 0), new Term.PrefixOrder(1)));
		assertEquals(1, BinarySearchAutocomplete.firstIndexOf(terms, new Term("app", 0), new Term.PrefixOrder(3)));
		assertEquals(4, BinarySearchAutocomplete.firstIndexOf(terms, new Term("b", 0), new Term.PrefixOrder(1)));
		assertEquals(7, BinarySearchAutocomplete.firstIndexOf(terms, new Term("be", 0), new Term.PrefixOrder(2)));
	}

	@Test(timeout = 10000)
	public void testLastIndexOfDuplicates() {
		Term[] terms = new Term[] { new Term("ape", 0), new Term("apple", 0), new Term("apple", 0),
				new Term("apple", 0), new Term("bat", 0), new Term("bat", 0), new Term("bat", 0), new Term("bee", 0),
				new Term("bee", 0), new Term("bee", 0), new Term("cat", 0) };
		assertEquals(3, BinarySearchAutocomplete.lastIndexOf(terms, new Term("a", 0), new Term.PrefixOrder(1)));
		assertEquals(3, BinarySearchAutocomplete.lastIndexOf(terms, new Term("app", 0), new Term.PrefixOrder(3)));
		assertEquals(6, BinarySearchAutocomplete.lastIndexOf(terms, new Term("ba", 0), new Term.PrefixOrder(2)));
		assertEquals(9, BinarySearchAutocomplete.lastIndexOf(terms, new Term("b", 0), new Term.PrefixOrder(1)));
	}
	
	@Test(timeout = 10000)
	public void testFirstIndexOfAllEqual(){
		for(int i = 2; i <= 256; i = i*2){
			Term[] terms = new Term[i];
			for(int j = 0; j < i; j++){
				terms[j] = new Term(""+(char)j, 0);
			}
			AllEqual comp = new AllEqual();
			assertEquals("all equal "+i,0, BinarySearchAutocomplete.
					firstIndexOf(terms, new Term("a", 0), comp));
		}
	}
	
	@Test(timeout = 10000)
	public void testLastIndexOfAllEqual(){
		AllEqual comp = new AllEqual();
		for(int i = 2; i <= 256; i = i*2){
			Term[] terms = new Term[i];
			for(int j = 0; j < i; j++){
				terms[j] = new Term(""+(char)j, 0);
			}
			assertEquals(i-1, BinarySearchAutocomplete.
					lastIndexOf(terms, new Term("a", 0), comp));
		}
	}
	
	/**Tests if firstIndexOf or lastIndexOf change the arrays passed in
	 */
	@Test(timeout = 10000)
	public void testFirstIndexOfMutates(){
		for(int i = 2; i <= 256; i = i*2){
			Term[] terms = new Term[i];
			for(int j = 0; j < i; j++){
				terms[j] = new Term(""+(char)j, j);
			}
			Term[] terms2 = terms.clone();
			BinarySearchAutocomplete.firstIndexOf(
					terms2, new Term("a", 0), new Term.PrefixOrder(1));
			assertArrayEquals("firstIndexOf mutates arguments",
					terms, terms2);
		}
	}
	
	@Test(timeout = 10000)
	public void testLastIndexOfMutates(){
		for(int i = 2; i <= 256; i = i*2){
			Term[] terms = new Term[i];
			for(int j = 0; j < i; j++){
				terms[j] = new Term(""+(char)j, j);
			}
			Term[] terms2 = terms.clone();
			BinarySearchAutocomplete.lastIndexOf(
					terms2, new Term("a", 0), new Term.PrefixOrder(1));
			assertArrayEquals("firstIndexOf mutates arguments",
					terms, terms2);
		}
	}
	
	@Test(timeout = 10000)
	public void testFirstIndexOfUsesEquals(){
		WeightSorter comp = new WeightSorter();
		for(int i = 2; i <= 256; i = i*2){
			Term[] terms = new Term[i];
			for(int j = 0; j < i; j++){
				terms[j] = new Term("a", j);
			}
			assertEquals("first index of issue",0, BinarySearchAutocomplete.
					firstIndexOf(terms, new Term("b", 0), comp));
		}
	}
	

	@Test(timeout = 10000)
	public void testLastIndexOfUsesEquals(){
		WeightSorter comp = new WeightSorter();
		for(int i = 2; i <= 256; i = i*2){
			Term[] terms = new Term[i];
			for(int j = 0; j < i; j++){
				terms[j] = new Term("b", j);
			}
			assertEquals("last index issue",i-1, BinarySearchAutocomplete.
					lastIndexOf(terms, new Term("a", i-1), comp));
		}
	}

	/**
	 * Tests that constructor throws the correct exceptions
	 */
	@Test(timeout = 10000)
	public void testConstructorException(){
		try{
			Autocompletor test = getInstance(null, myWeights);
			fail("No exception thrown");
		}
		catch(NullPointerException e){
		}
		catch(Throwable e){
			fail("Wrong throw");
		}
		try{
			Autocompletor test = getInstance(myNames, null);
			fail("No exception thrown");
		}
		catch(NullPointerException e){
		}
		catch(Throwable e){
			fail("Wrong throw");
		}
	}
}
