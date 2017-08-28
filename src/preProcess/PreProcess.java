package preProcess;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PreProcess {

	public static void main(String args[]) throws Exception {
		PreProcess pp = new PreProcess();
		Date start_date = new Date();
		System.out.println("Extraction starts at:" + start_date);
		String location = "/home/lcj/WikiExtractor/etc/";
		
		pp.filter(location+"zhwiki-latest-langlinks.2en.result.sql", location+"enwiki-latest-pages-articles-multistream.id_titleresult.id_titlexml", location+"zhwiki-latest-pages-articles-multistream.id_titleresult.id_titlexml");
//		ze.filter("/home/lcj/zhwiki-latest-langlinks.sql");
		Date end_date = new Date();
		double cost = (double)(end_date.getTime()-start_date.getTime())/1000.0/60.0;
		System.out.println("Extraction ents at: " + end_date + "\tcost: " + cost + "min");
	}
	
	public void filter(String zh_cl, String en_id_title, String zh_id_title) throws Exception {
    	System.out.println("Processing file: " + zh_cl);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(zh_cl)));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(zh_cl.replace("result.", "result.new."))));
        
        String[] filter_words = {"Wikipedia:", "Category:", "Template:", "Portal:", "WikiProjects", "File:", "User:", "Help:", "Image:", "Module:",
                        		"list", "mediawiki", "categories", " articles"};
        String line = new String();
        while (true) {
            line = bufferedReader.readLine();
            if (line == null) {
            	break;
            }
            boolean contains = false;
            for (String word : filter_words) {
            	if (line.toLowerCase().contains(word.toLowerCase())) {
            		contains = true;
            		break;
            	}
            }
            if (contains == false) {
            	bufferedWriter.write(line);
            }
        }
        bufferedReader.close();
        bufferedWriter.close();
        
        Map<String, String> cl_zh_id2title_dict = read_w_t_t_w(zh_cl.replace("result.", "result.new."), 0);
        Map<String, String> cl_zh_title2id_dict = read_w_t_t_w(zh_cl.replace("result.", "result.new."), 1);

        Map<String, String> en_id2title_dict = read_w_t_t_w(en_id_title, 0);
		Map<String, String> en_title2id_dict = read_w_t_t_w(en_id_title, 1);
        
		Map<String, String> zh_id2title_dict = read_w_t_t_w(zh_id_title, 0);
		Map<String, String> zh_title2id_dict = read_w_t_t_w(zh_id_title, 1);
		
		List<String> common_zh_id = new ArrayList<String>();
		common_zh_id.addAll(zh_id2title_dict.keySet());
		common_zh_id.retainAll(cl_zh_id2title_dict.keySet());
		
		List<String> common_en_title = new ArrayList<String>();
		common_en_title.addAll(cl_zh_title2id_dict.keySet());
		common_en_title.retainAll(en_title2id_dict.keySet());
		
        System.out.println(common_zh_id.size());
        System.out.println(common_en_title.size());
        
//        bufferedWriter = new BufferedWriter(new FileWriter(new File("en_zh_cl_id_title.txt")));
//        for (String k : cl_zh_id2title_dict.keySet()) {
//        	if (common_zh_id.contains(k) && common_en_title.contains(cl_zh_id2title_dict.get(k))) {
//        		bufferedWriter.write(cl_zh_id2title_dict.get(k) + "\t\t" + en_title2id_dict.get(cl_zh_id2title_dict.get(k)) + "\t\t" + zh_id2title_dict.get(k) + "\t\t" + k + "\n");
//        	}
//        }
//        bufferedWriter.close();
	}
	
	public Map<String, String> read_w_t_t_w(String filename, int key_pos) throws Exception {
		Map<String, String> w_t_t_w = new HashMap<String, String>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
		String line = new String();
        while (true) {
            line = bufferedReader.readLine();
            if (line == null) {
            	break;
            }
            String words[] = line.split("\t\t");
            w_t_t_w.put(words[key_pos], words[1-key_pos]);
        }
        bufferedReader.close();
        return w_t_t_w;
	}
	
	
	
	
}
