package tfidf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;

public class ArticleParser implements Serializable {

    private String pageID;
    private String title;
    private String date;
    private String metaData;
    private String text;

    public String getPageID() { return pageID; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getMetaData() { return metaData; }
    public String getText() { return text; }

    private String processText(String original) {
        // Replace all "\\n".
        String noNewLine = original.replaceAll("\\\\n", " ");

        // To lower case and meanwhile remove all the non-alphabetic letters and XML tags
        int length = noNewLine.length();
        boolean inXMLTag = false;   // Whether we are currently in an XML tag.
        char prevCh = '\0';
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            char ch = noNewLine.charAt(i);

            if (inXMLTag && ch == '>') {
                inXMLTag = false;
                prevCh = ' ';
                sb.append(prevCh);  // Append a whitespace for the XML tag.
                continue;
            }

            if (!inXMLTag && ch == '<') {
                inXMLTag = true;
                continue;
            }

            if (inXMLTag) {
                continue;
            }

            if ('a' <= ch && ch <= 'z') {
                prevCh = ch;
                sb.append(prevCh);
            } else if ('A' <= ch && ch <= 'Z') {
                prevCh = (char) (ch - 'A' + 'a');
                sb.append(prevCh);
            } else {
                if (prevCh != ' ') {
                    // We don't insert duplicated whitespaces
                    prevCh = ' ';
                    sb.append(prevCh);
                }
            }
        }

        return sb.toString();
    }

    public boolean Parse(String line) {
        String[] parts = line.split("\t");
        if (parts.length != 5) {
            System.err.println("ERROR: Expected parts: 5; Actual parts: " + String.valueOf(parts.length));
            return false;
        }

        pageID = parts[0];
        title = parts[1];
        date = parts[2];
        metaData = parts[3];
        text = processText(parts[3]);

        return true;
    }

    public static void main(String[] args) throws Exception {

        String fileName = args[0];

        try {
            //Create object of FileReader
            FileReader inputFile = new FileReader(fileName);

            //Instantiate the BufferedReader Class
            BufferedReader bufferReader = new BufferedReader(inputFile);

            //Variable to hold the one line data
            String line;

            // Read file line by line and print on the console
            while ((line = bufferReader.readLine()) != null)   {
                ArticleParser article = new ArticleParser();
                article.Parse(line);

                System.out.println("==========");
                System.out.println(article.pageID);
                System.out.println(article.title);
                System.out.println(article.date);
                System.out.println(article.text);
            }
            //Close the buffer reader
            bufferReader.close();
        } catch(Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

}
