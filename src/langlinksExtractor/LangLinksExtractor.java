package langlinksExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;

public class LangLinksExtractor {
	public static void main(String args[]) throws Exception {
		LangLinksExtractor lle = new LangLinksExtractor();
		Date start_date = new Date();
		System.out.println("Extraction starts at:" + start_date);
		lle.test("/Users/locke/Downloads/zhwiki-latest-langlinks.sql", "en");
		lle.test("/Users/locke/Downloads/enwiki-latest-langlinks.sql", "zh");
//		ze.test("/home/lcj/zhwiki-latest-langlinks.sql");
		Date end_date = new Date();
		double cost = (double)(end_date.getTime()-start_date.getTime())/1000.0/60.0;
		System.out.println("Extraction ents at: " + end_date + "\tcost: " + cost + "min");
	}
	
	public void test(String filename, String lang) throws Exception {
    	System.out.println("Processing file: " + filename);
        BufferedReader bufferedReaderRaw = new BufferedReader(new FileReader(new File(filename)));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(filename.replace(".", "."+"2"+lang+".result."))));

        String line = new String();
        while (true) {
            line = bufferedReaderRaw.readLine();
            if (line == null) {
            	break;
            }
            if (line.trim().startsWith("INSERT INTO")) {
            	line = line.substring(line.indexOf("(")+1, line.lastIndexOf(")"));
            	String[] links = line.split("\\),\\(");
            	for (String e : links) {
            		String[] words = e.split(",", 3);
//            		System.out.println(words[0] + "__" + words[1].substring(1, words[1].length()-1) + "__" + words[2].substring(1, words[2].length()-1));
            		if (words[1].substring(1, words[1].length()-1).equals(lang)) {
            			bufferedWriter.write(words[0] + "\t\t" + words[2].substring(1, words[2].length()-1) + "\n");
            		} else {
            			break;
            		}
            	}
            }
        }
        
        bufferedReaderRaw.close();
        bufferedWriter.close();
	}
}
