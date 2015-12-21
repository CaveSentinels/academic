import scala.Tuple2;

import java.util.Comparator;

// A helper class
public class SparksFly {
    public static final int INDEX_END = -1;

    public static void printCheckPoint(int index) {
        String cp;

        if (index == INDEX_END) {
            cp = "END";
        } else {
            cp = String.valueOf(index);
        }

        System.out.println("Progress checkpoint: " + cp);
    }

    public static void printCheckPoint(int index, String msg) {
        String cp;

        if (index == INDEX_END) {
            cp = "END";
        } else {
            cp = "Step " + String.valueOf(index) + ": " + msg;
        }

        System.out.println("Progress checkpoint: " + cp);
    }

    public static class TupleComparator implements Comparator<Tuple2<Double/*rank*/, String/*article*/>> {
        @Override
        public int compare(Tuple2<Double, String> o1, Tuple2<Double, String> o2) {
            int order = 0;
            if (o1._1() < o2._1()) {
                order = 1;  // make it descending
            } else if (o1._1().equals(o2._1())) {
                order = o1._2().compareTo(o2._2());
            } else {
                order = -1;
            }
            return order;
        }
    }
}
