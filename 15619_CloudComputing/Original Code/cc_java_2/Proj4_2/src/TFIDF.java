import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;
import tfidf.ArticleParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TFIDF extends SparksFly {

    private static final int PROGRAM_ARG_COUNT = 4;

    // Step 1: Pair Function
    // public interface PairFunction<T, K, V> extends Serializable {
    //     Tuple2<K, V> call(T var1) throws Exception;
    // }
    private static class PF_Title_Text implements PairFunction<String, String, String> {

        ArticleParser article = new ArticleParser();

        public Tuple2<String, String> call(String line) throws Exception {
            // System.out.println("PF_Title_Text: Processing line: " + line);
            article.Parse(line);
            // It is possible that "line" is a blank line, which means there is no title or text
            // and the result tuple would be (null, null).
            // And one blank line would somehow duplicate the line once so there will be two same lines
            // in the output.
            return new Tuple2<>(article.getTitle(), article.getText());
        }
    }


    // Step 2: Pair Function
    // public interface PairFlatMapFunction<T, K, V> extends Serializable {
    //     Iterable<Tuple2<K, V>> call(T var1) throws Exception;
    // }
    private static class PF_Word_Title implements PairFlatMapFunction<Tuple2<String/*title*/, String/*text*/>, String/*word*/, String/*title*/> {
        public Iterable<Tuple2<String/*word*/, String/*title*/>> call(Tuple2<String/*title*/, String/*text*/> pair) throws Exception {
            String title = pair._1();
            String text = pair._2();
            String[] words = text.split("\\s+");
            ArrayList<Tuple2<String/*word*/, String/*title*/>> results = new ArrayList<>();
            for (String word : words) {
                results.add(new Tuple2<>(word, title));
            }
            return results;
        }
    }


    public static void main(String[] args) throws Exception {

        // Checkpoint index
        int cpIndex = 0;

        // Print all the input arguments
        for (int i = 0; i < args.length; ++i) {
            System.out.println("args[" + String.valueOf(i) + "] = " + args[i]);
        }

        // Check if there are enough arguments.
        if (args.length != PROGRAM_ARG_COUNT) {
            System.err.println("ERROR: Wrong number of arguments! Expected: " +
                    String.valueOf(PROGRAM_ARG_COUNT) +
                    " Actual: " +
                    String.valueOf(args.length));
            System.err.println("Syntax: TFIDF <Source file/folder path> <Result folder path> <Target word> <Top N>");
            System.err.println("Example: TFIDF ~/work/sample.txt ~/work/output/ cloud 100");
            System.err.println("Example: TFIDF ~/work/data_parts/ ~/work/output/ cloud 100");
            return;
        }

        printCheckPoint(++cpIndex);

        // Get the arguments.
        String sourceFile = args[0];
        String resultPath = args[1];
        final String targetWord = args[2];
        // We just assume the user is smart enough to enter an integer correctly.
        Integer topN = Integer.parseInt(args[3]);

        printCheckPoint(++cpIndex);

        // Create the Spark Context
        SparkConf conf = new SparkConf().setAppName("TF-IDF Calculator");
        JavaSparkContext sc = new JavaSparkContext(conf);

        printCheckPoint(++cpIndex);

        // Read the input
        JavaRDD<String> fileData = sc.textFile(sourceFile);

        printCheckPoint(++cpIndex);

        // Step 1: Map <line> to (title, text)
        JavaPairRDD<String, String> pairRDD_Title_Text = fileData.mapToPair(new PF_Title_Text());

        printCheckPoint(++cpIndex);

        // Step 2: Map (title, text) to (word, title)
        JavaPairRDD<String/*word*/, String/*title*/> pairRDD_Word_Title = pairRDD_Title_Text.flatMapToPair(new PF_Word_Title());

        printCheckPoint(++cpIndex);

        // Step 3: Map (word, title) to ((word, title), count)
        JavaPairRDD<Tuple2<String/*word*/,String/*title*/>, Integer> pairRDD_Word_Title_Count =
        pairRDD_Word_Title.mapToPair(new PairFunction<Tuple2<String/*word*/,String/*title*/>, Tuple2<String/*word*/,String/*title*/>, Integer>() {
            @Override
            public Tuple2<Tuple2<String/*word*/,String/*title*/>, Integer> call(Tuple2<String/*word*/,String/*title*/> pair) throws Exception {
                return new Tuple2<>(pair, 1);
            }
        }).reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        });

        printCheckPoint(++cpIndex);

        // Step 4: Calculate number of different documents
        final long docNum = pairRDD_Title_Text.map(new Function<Tuple2<String,String>, String>() {
            @Override
            public String call(Tuple2<String/*title*/, String/*text*/> titleText) throws Exception {
                return titleText._1();
            }
        }).distinct().count();

        printCheckPoint(++cpIndex);

        // Step 5: Map (word, title) to ((word, title), idf)
        JavaPairRDD<String/*word*/, Iterable<String>/*titles*/> pairRDD_Word_Titles = pairRDD_Word_Title.distinct().groupByKey();

        JavaPairRDD<Tuple2<String/*word*/, String/*title*/>, Double/*IDF*/> pairRDD_Word_Title_IDF =
                pairRDD_Word_Titles.flatMapToPair(new PairFlatMapFunction<Tuple2<String, Iterable<String>>, String/*word*/, Tuple2<String/*title*/, Double>>() {
                    public Iterable<Tuple2<String, Tuple2<String/*title*/, Double>>> call(Tuple2<String, Iterable<String>> wordTitles) throws Exception {
                        String word = wordTitles._1();
                        Iterable<String> titles = wordTitles._2();

                        int d_word = 0;
                        for (String title : titles) {
                            ++d_word;
                        }
                        double idf = Math.log10(((double) docNum) / d_word);

                        ArrayList<Tuple2<String, Tuple2<String/*title*/, Double>>> results = new ArrayList<>();
                        for (String title : titles) {
                            results.add(new Tuple2<>(word, new Tuple2<>(title, idf)));
                            // System.out.println("( " + word + ", (" + title + ", " + String.valueOf(d_word) + ", " + String.valueOf(idf) + "))");
                        }

                        return results;
                    }
                }).mapToPair(new PairFunction<Tuple2<String, Tuple2<String, Double>>, Tuple2<String, String>, Double>() {
                    @Override
                    public Tuple2<Tuple2<String, String>, Double> call(Tuple2<String, Tuple2<String, Double>> stringTuple2Tuple2) throws Exception {
                        String word = stringTuple2Tuple2._1();
                        String title = stringTuple2Tuple2._2()._1();
                        Double idf = stringTuple2Tuple2._2()._2();

                        return new Tuple2<>(new Tuple2<>(word, title), idf);
                    }
                });

        printCheckPoint(++cpIndex);

        // Step 6: Calculate TF-IDF value.
//
        JavaPairRDD<Tuple2<String/*word*/, String/*title*/>, Tuple2<Double/*IDF*/, Integer/*count*/>> pairRDD_Word_Title_IDF_Count =
                pairRDD_Word_Title_IDF.join(pairRDD_Word_Title_Count);
        JavaPairRDD<Tuple2<String/*word*/, String/*title*/>, Double> pairRDD_Word_Title_TFIDF =
                pairRDD_Word_Title_IDF_Count.mapToPair(new PairFunction<Tuple2<Tuple2<String, String>, Tuple2<Double, Integer>>, Tuple2<String, String>, Double>() {
                    @Override
                    public Tuple2<Tuple2<String, String>, Double> call(Tuple2<Tuple2<String, String>, Tuple2<Double, Integer>> tuple2Tuple2Tuple2) throws Exception {
                        Tuple2<String, String> wordTitle = tuple2Tuple2Tuple2._1();
                        Double tfidf = tuple2Tuple2Tuple2._2()._1() * tuple2Tuple2Tuple2._2()._2();
                        return new Tuple2<>(wordTitle, tfidf);
                    }
                });

        printCheckPoint(++cpIndex);

        // Step 7: Sort according to the keyword and filter out the first TOP entries.
        List<Tuple2<Double/*TF-IDF*/, String/*title*/>> topNArticles =
            pairRDD_Word_Title_TFIDF.filter(new Function<Tuple2<Tuple2<String, String>, Double>, Boolean>() {
                @Override
                public Boolean call(Tuple2<Tuple2<String, String>, Double> tuple2DoubleTuple2) throws Exception {
                    String word = tuple2DoubleTuple2._1()._1();
                    return word.equals(targetWord);
                }
            }).mapToPair(new PairFunction<Tuple2<Tuple2<String, String>, Double>, Double/*TF-IDF*/, String/*title*/>() {
                @Override
                public Tuple2<Double/*TF-IDF*/, String/*title*/> call(Tuple2<Tuple2<String, String>, Double> pair) throws Exception {
                    String title = pair._1()._2();
                    Double tfidf = pair._2();
                    return new Tuple2<>(tfidf, title);
                }
            }).sortByKey(false/*descending sort*/).take(topN);

        Collections.sort(topNArticles, new SparksFly.TupleComparator());

        // Final step: Output the results
        System.out.println("Number of documents N = " + String.valueOf(docNum));    // Step 4: test
//        pairRDD_Title_Text.saveAsTextFile(resultPath);  // Step 1: test
//        pairRDD_Word_Title.saveAsTextFile(resultPath);  // Step 2: test
//        pairRDD_Word_Title_Count.saveAsTextFile(resultPath);    // Step 3: test
//        pairRDD_Word_Title_IDF.saveAsTextFile(resultPath);  // Step 5: test
//        pairRDD_Word_Title_IDF_Count.saveAsTextFile(resultPath);  // Step 6-1: test
//        pairRDD_Word_Title_TFIDF.saveAsTextFile(resultPath);    // Step 6: test

        for (Tuple2<Double, String> pair : topNArticles) {
            System.out.println(pair._2() + "\t" + String.valueOf(pair._1()));
        }


        printCheckPoint(INDEX_END);
    }
}
