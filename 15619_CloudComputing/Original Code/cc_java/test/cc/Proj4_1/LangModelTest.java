package cc.Proj4_1;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class LangModelTest {

    @Test
    public void testFindTopN() throws Exception {
        ArrayList<String> values = new ArrayList<String>();
        values.add(";100");
        values.add("a;10");
        values.add("ab;20");
        values.add("cc;30");
        values.add("ac;20");
        values.add("aa;20");
        values.add("ad;20");
        values.add("ae;20");
        values.add("af;20");

        {
            ArrayList<LangModel.WordProb> wpList = LangModel.LMReducer.findTopN(values, 1, 2);

            assertEquals(null, 1, wpList.size());

            {
                LangModel.WordProb wp = wpList.get(0);
                assertEquals(null, 30, wp.count);
                assertEquals(null, "cc", wp.word);
            }
        }

        {
            ArrayList<LangModel.WordProb> wpList = LangModel.LMReducer.findTopN(values, 5, 2);

            assertEquals(null, 5, wpList.size());

            {
                LangModel.WordProb wp = wpList.get(0);
                assertEquals(null, 30, wp.count);
                assertEquals(null, "cc", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(1);
                assertEquals(null, 20, wp.count);
                assertEquals(null, "aa", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(2);
                assertEquals(null, 20, wp.count);
                assertEquals(null, "ab", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(3);
                assertEquals(null, 20, wp.count);
                assertEquals(null, "ac", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(4);
                assertEquals(null, 20, wp.count);
                assertEquals(null, "ad", wp.word);
            }
        }

        {
            ArrayList<LangModel.WordProb> wpList = LangModel.LMReducer.findTopN(values, 8, 2);

            assertEquals(null, 8, wpList.size());

            {
                LangModel.WordProb wp = wpList.get(0);
                assertEquals(null, 30, wp.count);
                assertEquals(null, "cc", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(1);
                assertEquals(null, 20, wp.count);
                assertEquals(null, "aa", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(2);
                assertEquals(null, 20, wp.count);
                assertEquals(null, "ab", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(3);
                assertEquals(null, 20, wp.count);
                assertEquals(null, "ac", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(4);
                assertEquals(null, 20, wp.count);
                assertEquals(null, "ad", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(5);
                assertEquals(null, 20, wp.count);
                assertEquals(null, "ae", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(6);
                assertEquals(null, 20, wp.count);
                assertEquals(null, "af", wp.word);
            }

            {
                LangModel.WordProb wp = wpList.get(7);
                assertEquals(null, 10, wp.count);
                assertEquals(null, "a", wp.word);
            }
        }
    }
}