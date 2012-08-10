package utils;

import semanticdiscoverytoolkit.GeneralizedSuffixTree;
import junit.framework.TestCase;

/**
 * Just test for the GeneralizedSuffixTree. If they passed, Tree should work as expected.
 * 
 * @author den.konakov@gmail.com
 */
public class GeneralizedSuffixTreeTest extends TestCase {
	public void testGST1() {
		String[] str = {"abab", "baba", "abba"};
		GeneralizedSuffixTree st = new GeneralizedSuffixTree(str);
		
		assertEquals("ba",  st.longestSubstrs().toArray()[0]);
		assertEquals("ab",  st.longestSubstrs().toArray()[1]);
	}
	
	public void testGST2() {
		String[] str = {"windows", "dog", "commandos"};
		GeneralizedSuffixTree st = new GeneralizedSuffixTree(str);
		
		assertEquals("do",  st.longestSubstrs().toArray()[0]);
	}
	
	public void testGST3() {
		String[] str = {"banana", "cianaic"};
		GeneralizedSuffixTree st = new GeneralizedSuffixTree(str);
		
		assertEquals("ana",  st.longestSubstrs().toArray()[0]);
	}	
}