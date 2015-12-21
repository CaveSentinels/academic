package cc.Proj4_1;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class NGramCounterTest {

    @Before
    public void setUp() throws Exception {
        // Empty
    }

    @After
    public void tearDown() throws Exception {
        // Empty
    }

    @Test
    public void testIsEnglishLetter() throws Exception {
        for (char ch = 'a'; ch <= 'z'; ++ch) {
            assertTrue(NGramCounter.isEnglishLetter(ch));
        }

        for (char ch = 0; ch <= 255; ++ch) {
            if ('a' <= ch && ch <= 'z') {
                continue;
            }

            assertTrue(!NGramCounter.isEnglishLetter(ch));
        }
    }

    @Test
    public void testSplitWords() throws Exception {
        String t;

        {
            t = ("");
            ArrayList<String> words = NGramCounter.splitWords(t);
            assertEquals(0, words.size());
        }

        {
            t = (" `12234567890-=~!@#$%^&*()_+[]\\{}|;\':\",./<>?\t\n");
            ArrayList<String> words = NGramCounter.splitWords(t);
            assertEquals(0, words.size());
        }

        {
            t = ("Hello");
            ArrayList<String> words = NGramCounter.splitWords(t);
            assertEquals(1, words.size());
            assertEquals(null, "hello", words.get(0));
        }

        {
            t = ("Hello world");
            ArrayList<String> words = NGramCounter.splitWords(t);
            assertEquals(2, words.size());
            assertTrue(words.get(0).equals("hello"));
            assertEquals(null, "hello", words.get(0));
            assertEquals(null, "world", words.get(1));
        }

        {
            t = ("Hello   world");
            ArrayList<String> words = NGramCounter.splitWords(t);
            assertEquals(2, words.size());
            assertEquals(null, "hello", words.get(0));
            assertEquals(null, "world", words.get(1));
        }

        {
            t = ("Hello, world!!!");
            ArrayList<String> words = NGramCounter.splitWords(t);
            assertEquals(2, words.size());
            assertEquals(null, "hello", words.get(0));
            assertEquals(null, "world", words.get(1));
        }

        {
            t = ("Hello111world!!2");
            ArrayList<String> words = NGramCounter.splitWords(t);
            assertEquals(2, words.size());
            assertEquals(null, "hello", words.get(0));
            assertEquals(null, "world", words.get(1));
        }

        {
            t = ("Hello! My name is Li Lei! What's your name, please?");
            ArrayList<String> words = NGramCounter.splitWords(t);
            assertEquals(11, words.size());
            assertEquals(null, "hello", words.get(0));
            assertEquals(null, "lei", words.get(5));
            assertEquals(null, "s", words.get(7));
            assertEquals(null, "please", words.get(10));
        }
    }

    @Test
    public void testN_gramnizeLine() throws Exception {
        String text;

        {
            text = "";
            ArrayList<String> words = NGramCounter.splitWords(text);
            ArrayList<String> phrases1 = NGramCounter.n_gramnizeLine(words, 1);
            ArrayList<String> phrases2 = NGramCounter.n_gramnizeLine(words, 2);
            ArrayList<String> phrases5 = NGramCounter.n_gramnizeLine(words, 5);
            assertEquals(0, phrases1.size());
            assertEquals(0, phrases2.size());
            assertEquals(0, phrases5.size());
        }

        {
            text = "   ";
            ArrayList<String> words = NGramCounter.splitWords(text);
            ArrayList<String> phrases1 = NGramCounter.n_gramnizeLine(words, 1);
            ArrayList<String> phrases2 = NGramCounter.n_gramnizeLine(words, 2);
            ArrayList<String> phrases5 = NGramCounter.n_gramnizeLine(words, 5);
            assertEquals(0, phrases1.size());
            assertEquals(0, phrases2.size());
            assertEquals(0, phrases5.size());
        }

        {
            text = ",,1,2,,3,2";
            ArrayList<String> words = NGramCounter.splitWords(text);
            ArrayList<String> phrases1 = NGramCounter.n_gramnizeLine(words, 1);
            ArrayList<String> phrases2 = NGramCounter.n_gramnizeLine(words, 2);
            ArrayList<String> phrases5 = NGramCounter.n_gramnizeLine(words, 5);
            assertEquals(0, phrases1.size());
            assertEquals(0, phrases2.size());
            assertEquals(0, phrases5.size());
        }

        {
            text = "Hello";
            ArrayList<String> words = NGramCounter.splitWords(text);
            ArrayList<String> phrases1 = NGramCounter.n_gramnizeLine(words, 1);
            ArrayList<String> phrases2 = NGramCounter.n_gramnizeLine(words, 2);
            ArrayList<String> phrases5 = NGramCounter.n_gramnizeLine(words, 5);
            assertEquals(1, phrases1.size());
            assertEquals(null, "hello", phrases1.get(0));
            assertEquals(1, phrases2.size());
            assertEquals(null, "hello", phrases2.get(0));
            assertEquals(1, phrases5.size());
            assertEquals(null, "hello", phrases5.get(0));
        }

        {
            text = "Hello world";
            ArrayList<String> words = NGramCounter.splitWords(text);
            ArrayList<String> phrases1 = NGramCounter.n_gramnizeLine(words, 1);
            ArrayList<String> phrases2 = NGramCounter.n_gramnizeLine(words, 2);
            ArrayList<String> phrases5 = NGramCounter.n_gramnizeLine(words, 5);
            assertEquals(2, phrases1.size());
            assertEquals(null, "hello", phrases1.get(0));
            assertEquals(null, "world", phrases1.get(1));
            assertEquals(3, phrases2.size());
            assertEquals(null, "hello", phrases2.get(0));
            assertEquals(null, "hello world", phrases2.get(1));
            assertEquals(null, "world", phrases2.get(2));
            assertEquals(3, phrases5.size());
            assertEquals(null, "hello", phrases5.get(0));
            assertEquals(null, "hello world", phrases5.get(1));
            assertEquals(null, "world", phrases5.get(2));
        }

        {
            text = "A: Hello! My name is Li Lei! What's your name? B: My name is Han Meimei.";
            ArrayList<String> words = NGramCounter.splitWords(text);
            ArrayList<String> phrases1 = NGramCounter.n_gramnizeLine(words, 1);
            ArrayList<String> phrases2 = NGramCounter.n_gramnizeLine(words, 2);
            ArrayList<String> phrases5 = NGramCounter.n_gramnizeLine(words, 5);

            int P1 = 17;
            assertEquals(P1, phrases1.size());
            assertEquals(null, "a", phrases1.get(0));
            assertEquals(null, "li", phrases1.get(5));
            assertEquals(null, "s", phrases1.get(8));
            assertEquals(null, "meimei", phrases1.get(P1 - 1));

            int P2 = 17 + 16;
            assertEquals(P2, phrases2.size());
            assertEquals(null, "a", phrases2.get(0));
            assertEquals(null, "a hello", phrases2.get(1));
            assertEquals(null, "hello", phrases2.get(2));
            assertEquals(null, "hello my", phrases2.get(3));
            assertEquals(null, "han", phrases2.get(P2-3));
            assertEquals(null, "han meimei", phrases2.get(P2 - 2));
            assertEquals(null, "meimei", phrases2.get(P2 - 1));

            int P3 = 17+16+15+14+13;
            assertEquals(P3, phrases5.size());
            assertEquals(null, "a", phrases5.get(0));
            assertEquals(null, "a hello", phrases5.get(1));
            assertEquals(null, "a hello my", phrases5.get(2));
            assertEquals(null, "a hello my name", phrases5.get(3));
            assertEquals(null, "a hello my name is", phrases5.get(4));
            assertEquals(null, "hello", phrases5.get(5));
            assertEquals(null, "hello my", phrases5.get(6));
            assertEquals(null, "hello my name", phrases5.get(7));
            assertEquals(null, "hello my name is", phrases5.get(8));
            assertEquals(null, "hello my name is li", phrases5.get(9));
            assertEquals(null, "name", phrases5.get(P3 - 10));
            assertEquals(null, "name is", phrases5.get(P3 - 9));
            assertEquals(null, "name is han", phrases5.get(P3 - 8));
            assertEquals(null, "name is han meimei", phrases5.get(P3 - 7));
            assertEquals(null, "is", phrases5.get(P3 - 6));
            assertEquals(null, "is han", phrases5.get(P3 - 5));
            assertEquals(null, "is han meimei", phrases5.get(P3 - 4));
            assertEquals(null, "han", phrases5.get(P3 - 3));
            assertEquals(null, "han meimei", phrases5.get(P3-2));
            assertEquals(null, "meimei", phrases5.get(P3-1));
        }
    }
}