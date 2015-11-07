import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PageRanker extends SparksFly {

    private static final int PROGRAM_ARG_COUNT = 5;

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
            System.err.println("Syntax: PageRanker <Source file/folder path> <Result folder path> <Target word> <Top N>");
            System.err.println("Example: PageRanker ~/work/sample.txt ~/work/output/ cloud 100");
            System.err.println("Example: PageRanker ~/work/data_parts/ ~/work/output/ cloud 100");
            return;
        }

        String fileArcs = args[0];
        String fileMappings = args[1];
        String resultPath = args[2];
        // We just assume that the user is smart enough to enter a correct integer.
        int maxIterations = Integer.parseInt(args[3]);
        int topN = Integer.parseInt(args[4]);

        printCheckPoint(++cpIndex, "Program arguments have been read.");

        // Create the Spark Context
        SparkConf conf = new SparkConf().setAppName("Page Ranker");
        JavaSparkContext sc = new JavaSparkContext(conf);

        printCheckPoint(++cpIndex, "Spark Context is created.");

        // Read the arcs.
        JavaRDD<String> fileDataArcs = sc.textFile(fileArcs);

        printCheckPoint(++cpIndex, "Arcs file is read in.");

        // Read the mappings.
        JavaRDD<String> fileDataMappings = sc.textFile(fileMappings);

        printCheckPoint(++cpIndex, "Mappings file is read in.");

        // Step 1: [arcs] -> [adj_list]
        JavaPairRDD<String/*node*/, Iterable<String>/*neighbors*/> adjList =
                fileDataArcs.mapToPair(new PairFunction<String/*row*/, String/*source*/, String/*dest*/>() {
                    @Override
                    public Tuple2<String, String> call(String s) throws Exception {
                        String[] parts = s.split("\t");
                        return new Tuple2<>(parts[0], parts[1]);
                    }
                }).distinct().groupByKey().persist(StorageLevel.MEMORY_AND_DISK()); // Memory..AND DISK??

        // Step 2: [mappings] -> [full_set_1_ranks]
        JavaPairRDD<String/*node*/, String/*article*/> nodeArticleMap = fileDataMappings.mapToPair(new PairFunction<String, String, String>() {
            @Override
            public Tuple2<String, String> call(String s) throws Exception {
                String[] parts = s.split("\t");
                return new Tuple2<>(parts[0], parts[1]);
            }
        }).distinct();

        JavaPairRDD<String/*node*/, Double/*1.0*/> fullSet1Ranks =
                nodeArticleMap.mapToPair(new PairFunction<Tuple2<String, String>, String, Double>() {
                    @Override
                    public Tuple2<String, Double> call(Tuple2<String, String> pair) throws Exception {
                        return new Tuple2<>(pair._1(), 1.0);
                    }
                });

        // Step 3: full set size
        long fullSetSize = fullSet1Ranks.count();

        // Step 4: ([adj_list], [full_set_1_ranks]) -> [dangling_set_1_ranks]
        // Step 4.1: [adj_list] -> [non_dangling_1_ranks]
        JavaPairRDD<String/*node*/, Double/*1.0*/> nonDangling1Ranks =
                adjList.mapValues(new Function<Iterable<String>, Double>() {
                    @Override
                    public Double call(Iterable<String> strings) throws Exception {
                        return 1.0;
                    }
                });

        // Step 4.2: [full_set_1_ranks] - [non_dangling_1_ranks] -> [dangling_1_ranks]
        JavaPairRDD<String/*dnode*/, Double/*1.0*/> dangling1Ranks =
                fullSet1Ranks.subtractByKey(nonDangling1Ranks);

        // Step 5: Iterations
        JavaPairRDD<String/*node*/, Double/*rank*/> fullSetRanks = fullSet1Ranks;
        for (int iteration = 0; iteration < maxIterations; ++iteration) {
            // Step 5.1: Save the previous ranks
            // We need to make a copy in order to use the page ranks in the previous step.
            JavaPairRDD<String/*node*/, Double/*rank*/> fullSetRanksPrev = fullSetRanks.filter(new Function<Tuple2<String, Double>, Boolean>() {
                @Override
                public Boolean call(Tuple2<String, Double> stringDoubleTuple2) throws Exception {
                    return true;    // This is essentially copy... Is there a better way?
                }
            });

            // Step 5.2: Calculate the contributions.
            JavaPairRDD<String/*node*/, Double/*rank*/> contributions = adjList.join(fullSetRanks).values()
                    .flatMapToPair(new PairFlatMapFunction<Tuple2<Iterable<String>,Double>, String/*node*/, Double/*new rank*/>() {
                        @Override
                        public Iterable<Tuple2<String/*node*/, Double/*new rank*/>> call(Tuple2<Iterable<String>, Double> values) throws Exception {
                            Iterable<String> dests = values._1();
                            Double rank = values._2();

                            int destSize = 0;
                            for (String tmp : dests) {
                                ++destSize;
                            }

                            ArrayList<Tuple2<String/*node*/, Double/*rank*/>> nodeRanks = new ArrayList<>();
                            for (String node : dests) {
                                nodeRanks.add(new Tuple2<>(node, rank / destSize));
                            }

                            return nodeRanks;
                        }
                    });

            // Step 5.3: Calculate the new ranks without dangling pages' contributions
            fullSetRanks = contributions.reduceByKey(new Function2<Double, Double, Double>() {
                @Override
                public Double call(Double val1, Double val2) throws Exception {
                    return val1 + val2;
                }
            }).mapValues(new Function<Double, Double>() {
                @Override
                public Double call(Double rank) throws Exception {
                    return (0.15 + 0.85 * rank);
                }
            });

            // Step 5.4: Calculate the dangling pages' contributions
            Double allDanglingNodesContributions =
            dangling1Ranks.join(fullSetRanksPrev).mapToPair(new PairFunction<Tuple2<String,Tuple2<Double,Double>>, String/*"all_dangling_node"*/, Double/*rank*/>() {
                @Override
                public Tuple2<String, Double> call(Tuple2<String, Tuple2<Double, Double>> dangling) throws Exception {
                    return new Tuple2<>("all_dangling_nodes", dangling._2()._2());
                }
            }).reduceByKey(new Function2<Double, Double, Double>() {
                @Override
                public Double call(Double val1, Double val2) throws Exception {
                    return val1 + val2;
                }
            }).first()._2();

            final Double contribToAddToEachNode = allDanglingNodesContributions / fullSetSize;

            // Step 5.5: Update the fullSetRanks to include dangling nodes' contributions
            fullSetRanks = fullSetRanks.mapValues(new Function<Double, Double>() {
                @Override
                public Double call(Double rank) throws Exception {
                    return rank + 0.85 * contribToAddToEachNode;
                }
            });
        }   // Iteration ends

        // Step 6: Find the topN documents
        // Step 6.1: Sort
        JavaPairRDD<Double/*rank*/, String/*article*/> sortedArticleRanks =
                fullSetRanks.join(nodeArticleMap).values().mapToPair(new PairFunction<Tuple2<Double, String>, Double, String>() {
                    @Override
                    public Tuple2<Double, String> call(Tuple2<Double, String> pair) throws Exception {
                        return pair;
                    }
                }).sortByKey(false);

        // Step 6.2: Take the topN
        List<Tuple2<Double, String>> topNArticles = sortedArticleRanks.take(topN);

        // Step 6.3: Sort again according to rank and name.
        Collections.sort(topNArticles, new SparksFly.TupleComparator());

        // Step Final: Output the results
        for (Tuple2<Double, String> pair : topNArticles) {
            System.out.println(pair._2() + "\t" + String.valueOf(pair._1()));
        }
    }

}
