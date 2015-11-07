package cc.Proj4_1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class NGramCounter {

    public static boolean isEnglishLetter(char ch) {
        return ('a' <= ch && ch <= 'z');
    }

    // Replace the non-alphabetic chars in the input line of text.
    public static ArrayList<String> splitWords(String value) {

        ArrayList<String> wordList = new ArrayList<String>();

        String line = value.toLowerCase();

        if (line.isEmpty()) {
            return wordList; // Empty list.
        }

        StringBuilder sb = new StringBuilder();
        char ch = line.charAt(0);
        boolean wordFlag = isEnglishLetter(ch); // A flag that is true when the current token is a word
        if (wordFlag) {
            // If the first char is an English letter, then append it
            // to the word.
            sb.append(ch);
        }

        for (int i = 1; i < line.length(); ++i) {
            ch = line.charAt(i);
            if (wordFlag && isEnglishLetter(ch)) {
                // We are still in a word. Append the letter.
                sb.append(ch);
            } else if (wordFlag && !isEnglishLetter(ch)) {
                // We just finished a word. Put it into the word list.
                wordList.add(sb.toString());
                // Clear the builder.
                sb.delete(0, sb.length());
                // Flip the flag.
                wordFlag = false;
            } else if (!wordFlag && isEnglishLetter(ch)) {
                // We start a new word. Append the letter.
                sb.append(ch);
                // Flip the flag.
                wordFlag = true;
            } else if (!wordFlag && !isEnglishLetter(ch)) {
                // Do nothing.
            } else {
                // Should never happen.
            }
        }

        // Deal with the last word.
        if (wordFlag) {
            wordList.add(sb.toString());
        }

        return wordList;
    }

    public static ArrayList<String> n_gramnizeLine(ArrayList<String> words, int N) {

        ArrayList<String> phrases = new ArrayList<String>();
        int sz = words.size();

        for (int i = 0; i < sz; ++i) {
            String ph = words.get(i);
            phrases.add(ph);
            for (int j = i+1; j < i+N && j < sz; ++j) {
                ph = ph + ' ' + words.get(j);
                phrases.add(ph);
            }
        }

        return phrases;
    }

    /**************************************************************************
    .___________.  ______    __  ___  _______ .__   __.  __   ________   _______ .______      .___  ___.      ___      .______   .______    _______ .______
    |           | /  __  \  |  |/  / |   ____||  \ |  | |  | |       /  |   ____||   _  \     |   \/   |     /   \     |   _  \  |   _  \  |   ____||   _  \
    `---|  |----`|  |  |  | |  '  /  |  |__   |   \|  | |  | `---/  /   |  |__   |  |_)  |    |  \  /  |    /  ^  \    |  |_)  | |  |_)  | |  |__   |  |_)  |
        |  |     |  |  |  | |    <   |   __|  |  . `  | |  |    /  /    |   __|  |      /     |  |\/|  |   /  /_\  \   |   ___/  |   ___/  |   __|  |      /
        |  |     |  `--'  | |  .  \  |  |____ |  |\   | |  |   /  /----.|  |____ |  |\  \----.|  |  |  |  /  _____  \  |  |      |  |      |  |____ |  |\  \----.
        |__|      \______/  |__|\__\ |_______||__| \__| |__|  /________||_______|| _| `._____||__|  |__| /__/     \__\ | _|      | _|      |_______|| _| `._____|

     *************************************************************************/

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable>{

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            ArrayList<String> words = NGramCounter.splitWords(value.toString());
            if (words.isEmpty()) {
                return;
            }

            ArrayList<String> phrases = NGramCounter.n_gramnizeLine(words, 5);
            if (phrases.isEmpty()) {
                return;
            }

            for (String phrase : phrases) {
                word.set(phrase);
                context.write(word, one);
            }
        }
    }

    /**************************************************************************
      __  .__   __. .___________.    _______. __    __  .___  ___. .______       _______  _______   __    __    ______  _______ .______
     |  | |  \ |  | |           |   /       ||  |  |  | |   \/   | |   _  \     |   ____||       \ |  |  |  |  /      ||   ____||   _  \
     |  | |   \|  | `---|  |----`  |   (----`|  |  |  | |  \  /  | |  |_)  |    |  |__   |  .--.  ||  |  |  | |  ,----'|  |__   |  |_)  |
     |  | |  . `  |     |  |        \   \    |  |  |  | |  |\/|  | |      /     |   __|  |  |  |  ||  |  |  | |  |     |   __|  |      /
     |  | |  |\   |     |  |    .----)   |   |  `--'  | |  |  |  | |  |\  \----.|  |____ |  '--'  ||  `--'  | |  `----.|  |____ |  |\  \----.
     |__| |__| \__|     |__|    |_______/     \______/  |__|  |__| | _| `._____||_______||_______/  \______/   \______||_______|| _| `._____|

     *************************************************************************/

    public static class IntSumReducer
            extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[input] " + args[0]);
        System.out.println("[output] " + args[1]);
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(NGramCounter.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}