import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

public class FileLineCounter {

    private static final int PROGRAM_ARG_COUNT = 3;

    private static void printCheckPoint(int index) {
        String cp;

        if (index == -1) {
            cp = "END";
        } else {
            cp = String.valueOf(index);
        }

        System.out.println("Progress checkpoint: " + cp);
    }

    public static void main(String[] args) throws Exception {

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
            return;
        }

        printCheckPoint(1);

        final String filePath = args[0];
        final String keyword = args[1];
        final String midPath = args[2];

        // Create the Spark Context
        SparkConf conf = new SparkConf().setAppName("File Line Counter");
        JavaSparkContext sc = new JavaSparkContext(conf);

        printCheckPoint(2);

        // Read the input
        JavaRDD<String> fileData = sc.textFile(filePath);

        printCheckPoint(3);

        // Filter all the lines that contains the specified keyword.
        JavaRDD<String> linesWithKeyword = fileData.filter(
                new Function<String, Boolean>() {
                    public Boolean call(String line) {
                        return line.contains(keyword);
                    }
                }
        );

        printCheckPoint(4);

        // Output the intermediate result.
        linesWithKeyword.saveAsTextFile(midPath);

        printCheckPoint(5);

        // Count the line number
        long count = linesWithKeyword.count();

        // Print the result
        System.out.println("File \"" + filePath + "\" has <" + String.valueOf(count) +
                "> line(s) containing the keyword \"" + keyword + "\"");

        printCheckPoint(-1);
    }

}
