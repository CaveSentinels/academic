package cc;


public class playground {
    public static void main(String[] args) {
        try {
            String str = ";1";
            String[] parts = str.split(";");
            System.out.println("parts[0] = " + parts[0]);
            System.out.println("parts[1] = " + parts[1]);
            System.out.println("parts[0] is empty: " + parts[0].isEmpty());
            System.out.println("parts[1] is empty: " + parts[1].isEmpty());
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getCause());
        } finally {
            // Empty
        }
    }

    public String foo(String str1, BufferredReader br){
        String line;

        String retval = "";
        while((line=br.readLine()) != null) {

            if (str1.equals("foo")){
                retval+=line;
            } else {
                retval += line.reverse();
            } }

        return retval;
    }
}


