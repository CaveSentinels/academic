package cc.Proj4_1;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

public class LangModel {

    private static String SEPARATOR = ";";
    private static int OCCURRENCES_THRESHOLD = 2; // Only the words that occurr more than OCCURRENCES_THRESHOLD will be considered.
    private static int TOP_N = 5;   // Command-line modifiable. See main().

    /**************************************************************************
     __      .___  ___. .___  ___.      ___      .______   .______    _______ .______
     |  |     |   \/   | |   \/   |     /   \     |   _  \  |   _  \  |   ____||   _  \
     |  |     |  \  /  | |  \  /  |    /  ^  \    |  |_)  | |  |_)  | |  |__   |  |_)  |
     |  |     |  |\/|  | |  |\/|  |   /  /_\  \   |   ___/  |   ___/  |   __|  |      /
     |  `----.|  |  |  | |  |  |  |  /  _____  \  |  |      |  |      |  |____ |  |\  \----.
     |_______||__|  |__| |__|  |__| /__/     \__\ | _|      | _|      |_______|| _| `._____|

     *************************************************************************/

    public static class LMMapper
            extends Mapper<Object, Text, Text, Text>{

        private Text outKey = new Text();
        private Text outValue = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            String line = value.toString();

            // line is in the format of "<phrase>\t<count>"
            String[] parts = line.split("\t");
            String phrase = parts[0];
            String count = parts[1];

            // First, output the entire phrase.
            {
                outKey.set(phrase);
                outValue.set(SEPARATOR + count);
                context.write(outKey, outValue);
            }

            // Second, output the entire phrase without last word.
//            String output2;
            int idxLastSpace = phrase.lastIndexOf(' ');
            if (idxLastSpace != -1) {
                // There are multiple words.
                String firstWords = phrase.substring(0, idxLastSpace);
                String lastWord = phrase.substring(idxLastSpace + 1);

                outKey.set(firstWords);
                outValue.set(lastWord + SEPARATOR + count);
                context.write(outKey, outValue);
            }
        }
    }

    /**************************************************************************
     __      .___  ___. .______       _______  _______   __    __    ______  _______ .______
     |  |     |   \/   | |   _  \     |   ____||       \ |  |  |  |  /      ||   ____||   _  \
     |  |     |  \  /  | |  |_)  |    |  |__   |  .--.  ||  |  |  | |  ,----'|  |__   |  |_)  |
     |  |     |  |\/|  | |      /     |   __|  |  |  |  ||  |  |  | |  |     |   __|  |      /
     |  `----.|  |  |  | |  |\  \----.|  |____ |  '--'  ||  `--'  | |  `----.|  |____ |  |\  \----.
     |_______||__|  |__| | _| `._____||_______||_______/  \______/   \______||_______|| _| `._____|

     *************************************************************************/

    public static class WordProb implements Comparable<WordProb> {
        public String word;
        public int count = 0;
        public double prob = 0.0;
        public WordProb(String w, int c) {
            word = w;
            count = c;
        }

        // Make it descending.
        @Override
        public int compareTo(WordProb o) {
            int ret = 0;

            if (this.count > o.count) {
                ret = -1;
            } else if (this.count == o.count) {
                ret = this.word.compareTo(o.word);
            } else if (this.count < o.count) {
                ret = 1;
            }

            return ret;
        }
    }

    public static class LMReducer
            extends TableReducer<Text,Text,ImmutableBytesWritable> {

        public static ArrayList<WordProb> findTopN(ArrayList<String> valueStrList,
                                                   int topN,
                                                   int occurrenceThreshold) {

            int baseCount = 0;
            ArrayList<WordProb> topNWords = new ArrayList<WordProb>();
            for (String val : valueStrList) {
                String[] parts = val.split(SEPARATOR);
                String nextWord = parts[0];
                String countStr = parts[1];
                int count = Integer.parseInt(countStr);

                if (count <= occurrenceThreshold) {
                    // If the word occurs too few times, we just skip it.
                    continue;
                }

                if (nextWord.isEmpty()) {
                    // If nextWord is empty, we are now handling the base phrase.
                    baseCount = count;
                } else {
                    // If nextWord is not empty, we are now handling the phrase
                    // that may come after the base phrase.
                    WordProb wp = new WordProb(nextWord, count);
                    if (topNWords.size() < topN) {
                        topNWords.add(wp);
                    } else {
                        if (wp.compareTo(topNWords.get(topNWords.size()-1)) < 0) {
                            topNWords.set(topNWords.size() - 1, wp);
                        }
                    }
                    Collections.sort(topNWords);
                }
            }

            for (WordProb wp : topNWords) {
                wp.prob = (double) wp.count / baseCount;
            }

            return topNWords;
        }

        public void reduce(Text key, Iterable<Text> values,
                           Context context
        ) throws IOException, InterruptedException {
            // Transform to strings
            ArrayList<String> valueStrList = new ArrayList<String>();
            for (Text val : values) {
                valueStrList.add(val.toString());
            }

            // Get the top N words
            ArrayList<WordProb> topNWords = findTopN(valueStrList, TOP_N, OCCURRENCES_THRESHOLD);

            // Output
            byte[] row = Bytes.toBytes(key.toString());
            if (row != null) {
                Put put = new Put(row);
                for (WordProb wp : topNWords) {
                    put.add(Bytes.toBytes("cf"),
                            Bytes.toBytes(wp.word),
                            Bytes.toBytes(String.valueOf(wp.prob)));

                    context.write(new ImmutableBytesWritable(key.getBytes()), put);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Job job = Job.getInstance(conf, "Language Model");
        TableMapReduceUtil.initTableReducerJob("LangModel", LMReducer.class, job);

        job.setJarByClass(LangModel.class);
        job.setMapperClass(LMMapper.class);
        job.setReducerClass(LMReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TableOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        // Because we are going to export the data to HBase,
        // we don't need a file output format class.

        LangModel.TOP_N = Integer.parseInt(args[1]);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}