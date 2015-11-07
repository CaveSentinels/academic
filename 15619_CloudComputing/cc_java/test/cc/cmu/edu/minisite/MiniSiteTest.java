package cc.cmu.edu.minisite;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class MiniSiteTest {
    @Test
    public void testStep4Sorting() throws Exception {
        ArrayList<String> friendNames = new ArrayList<String>();
        ArrayList<String> imageURLs = new ArrayList<String>();
        ArrayList<String> imageTimes = new ArrayList<String>();

        String[] names = { "ddd", "bbb", "ccc", "aaa", "eee", "AAA" };
        String[] times = { "2014-07-28", "2013-08-12", "2015-03-12", "2011-01-01", "2011-01-01", "2011-01-01" };
        String[] images = { "url4", "url2", "url3", "url1", "url6", "url5" };

        for (int j = 0; j < names.length; ++j) {
            int i = 0;
            while (i < friendNames.size()) {
                int compTime = times[j].compareTo(imageTimes.get(i));
                int compName = names[j].compareToIgnoreCase(friendNames.get(i));
                if ((compTime < 0) || (compTime == 0 && compName < 0)) {
                    friendNames.add(i, names[j]);
                    imageURLs.add(i, images[j]);
                    imageTimes.add(i, times[j]);
                    break;
                }
                ++i;
            }
            if (i >= friendNames.size()) {
                friendNames.add(names[j]);
                imageURLs.add(images[j]);
                imageTimes.add(times[j]);
            }
        }

        for (int j = 0; j < names.length; ++j) {
            System.out.println(friendNames.get(j) + " , " + imageTimes.get(j) + " , " + imageURLs.get(j));
        }
    }
}