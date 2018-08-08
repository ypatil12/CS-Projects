import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import org.junit.Test;

public class TestTrieAutocomplete {

	Term[] terms =
			new Term[] {new Term("ape", 6), 
					new Term("app", 4), 
					new Term("ban", 2),
					new Term("bat", 3),
					new Term("bee", 5),
					new Term("car", 7),
					new Term("cat", 1)};
	String[] names= {"ape", "app", "ban", "bat", "bee", "car", "cat"};
	double[] weights = {6, 4, 2, 3, 5, 7, 1};

	public Autocompletor getInstance(){
		return getInstance(names, weights);
	}

	public Autocompletor getInstance(String[] names, double[] weights){
		return new TrieAutocomplete(names, weights);
	}

	public class Autoincompletor extends TrieAutocomplete{

		public Autoincompletor(String[] terms, double[] weights) {
			super(terms, weights);
		}

		@Override
		public Iterable<String> topMatches(String prefix, int k){
			return new LinkedList<String>();
		}

	}

	public ArrayList<ArrayList<Term>> allPermutes(ArrayList<Term> arr){
		if (arr.size() == 1){
			ArrayList<ArrayList<Term>> output = new
					ArrayList<ArrayList<Term>>();
			output.add(arr);
			return output;
		}
		ArrayList<ArrayList<Term>> output = 
				new ArrayList<ArrayList<Term>>();
		for(int i = 0; i < arr.size(); i++){
			ArrayList<Term> arrcopy = new ArrayList<Term>(arr);
			arrcopy.remove(i);
			ArrayList<ArrayList<Term>> subPermutes = allPermutes(arrcopy);
			for(ArrayList<Term> permute: subPermutes)
				permute.add(arr.get(i));
			output.addAll(subPermutes);
		}
		return output;
	}


	/**Tests correctness of topMatch() for a few simple cases
	 */
	@Test(timeout = 10000)
	public void testTopMatch() {
		Autocompletor test = getInstance();
		String[] queries = {"", "a", "ap", "b", "ba", "c", "ca", "cat", "d", " "};
		String[] results = {"car", "ape", "ape", "bee", "bat", "car", "car", "cat", "", ""};
		for(int i = 0; i < queries.length; i++){
			String query = queries[i];
			String reported = test.topMatch(query);
			String actual = results[i];
			assertEquals("wrong top match for "+query, actual, reported);
		}
	}

	private String[] iterToArr(Iterable<String> it) {
		ArrayList<String> list = new ArrayList<String>();
		for (String s: it)
			list.add(s);
		return list.toArray(new String[0]);
	}

	/**Tests correctness of topKMatches() for a few simple cases
	 */
	@Test(timeout = 10000)
	public void testTopKMatches() {
		Autocompletor test = getInstance();
		String[] queries = {"", "", "", "", "a", "ap", "b", "ba", "d"};
		int[] ks = {8, 1, 2, 3, 1, 1, 2, 2, 100};
		String[][] results = {
				{"car", "ape", "bee", "app", "bat", "ban", "cat"},
				{"car"}, 
				{"car", "ape"}, 
				{"car", "ape", "bee"}, 
				{"ape"}, 
				{"ape"},
				{"bee", "bat"},
				{"bat", "ban"},
				{}
		};
		for(int i = 0; i < queries.length; i++){
			String query = queries[i];
			String[] reported = iterToArr(test.topMatches(query, ks[i]));
			String[] actual = results[i];
			assertArrayEquals("wrong top matches for "+query+" "+ks[i],
					actual, reported);
		}
	}

	/**
	 * A more rigorous testing of Trie, to make sure add works.
	 * The Trie should be constructed the same regardless of the order
	 * words are added to the Trie, which means topMatch should return
	 * the same output regardless of what order they are added in.
	 * 
	 * So, we compute all orders to add the elements, add words to the trie
	 * in those orders, and then call topMatch on a fixed series of inputs.
	 * We keep a set of the list of outputs - if the set size goes past 1,
	 * then two different add orders produced two different tries
	 * and thus two different outputs, so add is not working.
	 */
	@Test(timeout = 10000)
	public void testAdd() {
		ArrayList<Term> termList = new ArrayList<Term>();
		Term[] terms =
				new Term[] {new Term("ape", 6), 
						new Term("app", 4), 
						new Term("ban", 2),
						new Term("bat", 3),
						new Term("bee", 5),
						new Term("car", 7),
						new Term("cat", 1)};
		String[] queries = {"", "a", "ap", "ape", "app", "b", "ba", "ban", 
				"bat", "be", "bee",	"c", "ca", "car", "cat", "f"};
		for(Term t: terms)
			termList.add(t);
		ArrayList<ArrayList<Term>> orders = allPermutes(termList);
		HashSet<ArrayList<String>> outputs = 
				new HashSet<ArrayList<String>>();
		for(ArrayList<Term> order: orders){
			String[] names = new String[order.size()];
			double[] weights = new double[order.size()];
			for(int i = 0; i < order.size(); i++){
				names[i] = order.get(i).getWord();
				weights[i] = order.get(i).getWeight();
			}
			TrieAutocomplete auto = new TrieAutocomplete(names, weights);
			ArrayList<String> output = new ArrayList<String>();
			for(String query: queries){
				output.add(auto.topMatch(query));
			}
			outputs.add(output);
			assertTrue("results depend on add order",
					outputs.size() <= 1);
		}
	}

	@Test(timeout = 10000) 
	public void testMultipleInstances(){
		String[] names= {"day", "dip", "ear", "eat", "eye", "fir", "fun"};
		double[] weights = {13, 11, 9, 10, 12, 14, 8};
		Autocompletor test1 = getInstance();
		Autocompletor test2 = getInstance(names, weights);
		String[] queries1 = 
			{"", "a", "ap", "b", "ba", "c", "ca", "cat", "d", " "};
		String[] results1 = 
			{"car", "ape", "ape", "bee", "bat", "car", "car", "cat", "", ""};
		String[] queries2 =
			{"", "d", "di", "e", "ea", "f", "fi", "fun", "g" ," "};
		String[] results2 =
			{"fir", "day", "dip", "eye", "eat", "fir", "fir", "fun", "", ""};
		for(int i = 0; i < queries1.length; i++){
			String query1 = queries1[i];
			String reported1 = test1.topMatch(query1);
			String actual1 = results1[i];
			String query2 = queries2[i];
			String reported2 = test2.topMatch(query2);
			String actual2 = results2[i];
			assertEquals("wrong top match for "+query1, actual1, reported1);
			assertEquals("wrong top match for "+query2, actual2, reported2);
		}
		int[] ks = {8, 1, 2, 3, 1, 1, 2, 2, 100, 1};
		String[][] results3 =
			{
					{"car", "ape", "bee", "app", "bat", "ban", "cat"},
					{"ape"},
					{"ape", "app"},
					{"bee", "bat", "ban"},
					{"bat"},
					{"car"},
					{"car", "cat"},
					{"cat"},
					{},
					{}				
			};
		String[][] results4 = 
			{
					{"fir", "day", "eye", "dip", "eat", "ear", "fun"},
					{"day"},
					{"dip"},
					{"eye", "eat", "ear"},
					{"eat"},
					{"fir"},
					{"fir"},
					{"fun"},
					{},
					{}
			};
		for(int i = 0; i < queries1.length; i++){
			String[] expected1 = results3[i];
			String[] expected2 = results4[i];
			String[] observed1 = iterToArr(test1.topMatches(queries1[i], ks[i]));
			String[] observed2 = iterToArr(test2.topMatches(queries2[i], ks[i]));
			assertArrayEquals(expected1, observed1);
			assertArrayEquals(expected2, observed2);
		}
	}
}
