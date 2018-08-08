import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

public class TestTerm {
	private Random rng = new Random(1234);

	private String[] myNames = { "bhut jolokia", "capsaicin", "carolina reaper", "chipotle", "habanero", "jalapeno",
			"jalapeno membrane" };
	private double[] myWeights = { 855000, 16000000, 2200000, 3500, 100000, 3500, 10000 };

	public Term[] getTerms() {
		Term[] terms = new Term[myNames.length];
		for (int i = 0; i < terms.length; i++)
			terms[i] = new Term(myNames[i], myWeights[i]);
		return terms;
	}

	public int indexOf(Term[] arr, Term item) {
		for (int i = 0; i < arr.length; i++)
			if (arr[i].equals(item))
				return i;
		return -1;
	}

	public void shuffle(Object[] arr) {
		for (int i = 0; i < arr.length; i++) {
			int ind = rng.nextInt(arr.length);
			Object temp = arr[i];
			arr[i] = arr[ind];
			arr[ind] = temp;
		}
	}

	/**
	 * This test checks if Term throws a NullPointerException when constructed
	 * with a null argument
	 */
	@Test(timeout = 10000)
	public void testConstructorException() {
		try {
			Term test = new Term(null, 1);
			fail("No exception thrown for null String");
		} catch (NullPointerException e) {
		} catch (Throwable e) {
			fail("Wrong exception thrown");
		}

		try {
			Term test = new Term("test", -1);
			fail("No exception thrown for invalid weight");
		} catch (IllegalArgumentException e) {
		} catch (Throwable e) {
			fail("Wrong exception thrown");
		}
	}

	/**
	 * Tests that sorting terms without comparator is the same as sorting
	 * lexicographically
	 */
	@Test(timeout = 10000)
	public void testNativeSortingOrder() {
		Term[] terms = getTerms();
		Term[] sorted = terms.clone();
		for (int i = 0; i < 10; i++) {
			shuffle(terms);
			Arrays.sort(terms);
			assertArrayEquals(sorted, terms);
		}
	}

	/**
	 * Tests WeightOrder sorts correctly
	 */
	@Test(timeout = 10000)
	public void testWeightSortingOrder() {
		Term[] terms = getTerms();
		Term[] sorted = { terms[3], terms[5], terms[6], terms[4], terms[0], terms[2], terms[1] };
		for (int i = 0; i < 10; i++) {
			// preserve chipotle and jalapeno's order
			shuffle(terms);
			if (indexOf(terms, sorted[0]) > indexOf(terms, sorted[1])) {
				int temp = indexOf(terms, sorted[0]);
				terms[indexOf(terms, sorted[1])] = sorted[0];
				terms[temp] = sorted[1];
			}
			Arrays.sort(terms, new Term.WeightOrder());
			assertArrayEquals(sorted, terms);
		}
	}

	/**
	 * Tests ReverseWeightSortingOrder
	 */
	@Test(timeout = 10000)
	public void testReverseWeightSortingOrder() {
		Term[] terms = getTerms();
		Term[] sorted = { terms[1], terms[2], terms[0], terms[4], terms[6], terms[3], terms[5] };
		for (int i = 0; i < 10; i++) {
			// preserve chipotle and jalapeno's order
			shuffle(terms);
			if (indexOf(terms, sorted[5]) > indexOf(terms, sorted[6])) {
				int temp = indexOf(terms, sorted[5]);
				terms[indexOf(terms, sorted[6])] = sorted[5];
				terms[temp] = sorted[6];
			}
			Arrays.sort(terms, new Term.ReverseWeightOrder());
			assertArrayEquals(sorted, terms);
		}
	}

	@Test(timeout = 10000)
	/**
	 * Tests PrefixOrder
	 */
	public void testPrefixOrder() {
		// Tests basic cases
		Term[] terms1 = getTerms();
		Term[] sorted1 = { terms1[0], terms1[3], terms1[2], terms1[1], terms1[4], terms1[6], terms1[5] };
		for (int i = 0; i < terms1.length / 2; i++) {
			Term temp = terms1[i];
			terms1[i] = terms1[terms1.length - 1 - i];
			terms1[terms1.length - 1 - i] = temp;
		}
		Arrays.sort(terms1, new Term.PrefixOrder(1));
		assertArrayEquals(sorted1, terms1);

		Term[] terms2 = getTerms();
		Term[] sorted2 = { terms2[0], terms2[2], terms2[1], terms2[3], terms2[4], terms2[6], terms2[5] };
		for (int i = 0; i < terms2.length / 2; i++) {
			Term temp = terms2[i];
			terms2[i] = terms2[terms2.length - 1 - i];
			terms2[terms2.length - 1 - i] = temp;
		}
		Arrays.sort(terms2, new Term.PrefixOrder(2));
		assertArrayEquals(sorted2, terms2);

		Term[] terms3 = getTerms();
		Term[] sorted3 = { terms3[0], terms3[1], terms3[2], terms3[3], terms3[4], terms3[6], terms3[5] };
		for (int i = 0; i < terms3.length / 2; i++) {
			Term temp = terms3[i];
			terms3[i] = terms3[terms3.length - 1 - i];
			terms3[terms3.length - 1 - i] = temp;
		}
		Arrays.sort(terms3, new Term.PrefixOrder(3));
		assertArrayEquals(sorted3, terms3);

		// Test prefix case
		Term[] terms4 = getTerms();
		Term[] sorted4 = { terms4[0], terms4[1], terms4[2], terms4[3], terms4[4], terms4[5], terms4[6] };
		shuffle(terms4);
		Arrays.sort(terms4, new Term.PrefixOrder(10));
		assertArrayEquals(sorted4, terms4);

		// Test zero case
		Term[] terms5 = getTerms();
		shuffle(terms5);
		Term[] sorted5 = terms5.clone();
		Arrays.sort(terms5, new Term.PrefixOrder(0));
		assertArrayEquals(sorted5, terms5);
	}

	/**
	 * This test checks that toString returns the expected value
	 */
	@Test(timeout = 10000)
	public void testToString() {
		Term[] terms = getTerms();
		for (Term t : terms) {
			assertTrue("weight missing", t.toString().contains(String.format("%.1f", t.getWeight())));
			assertTrue("word missing", t.toString().contains(t.getWord()));
			assertTrue("no tab", t.toString().contains("\t"));
		}
	}
}
