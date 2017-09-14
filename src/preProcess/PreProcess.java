package preProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

public class PreProcess {

	public static void main(String args[]) throws Exception {
		PreProcess pp = new PreProcess();
		Date start_date = new Date();
		System.out.println("Extraction starts at:" + start_date);
		
		String location = "/home/lcj/WikiExtractor/etc/";
//		String location = "/Users/locke/Desktop/preprocess/";
		
//		pp.CLFilter(location+"zhwiki-latest-langlinks.2en.result.sql", location+"enwiki-latest-pages-articles-multistream.id_titleresult.id_titlexml", location+"zhwiki-latest-pages-articles-multistream.id_titleresult.id_titlexml");
//		pp.getCLEntities(location+"en_zh_cl_id_title.txt", location+"enwiki-latest-pages-articles-multistream.result.xml", location+"zhwiki-latest-pages-articles-multistream.result.xml");
		pp.matchEntities(location+"en_zh_cl_id_title.txt", location+"enwiki-latest-pages-articles-multistream.result.cl.xml", location+"zhwiki-latest-pages-articles-multistream.result.cl.xml");
		
		Date end_date = new Date();
		double cost = (double)(end_date.getTime()-start_date.getTime())/1000.0/60.0;
		System.out.println("Extraction ents at: " + end_date + "\tcost: " + cost + "min");
	}
	
	public void matchEntities(String en_zh_cl_id_title, String en_cl_pages, String zh_cl_pages) throws Exception {
		Map<String, String> en_cl_pages_titleid2page = new HashMap<String, String>();
		Map<String, String> zh_cl_pages_titleid2page = new HashMap<String, String>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(en_cl_pages)));
		String line = new String();
        while (true) {
            line = bufferedReader.readLine();
            if (line == null) {
            	break;
            }
            JSONObject page = new JSONObject(line);
            String titleid = page.getString("title") + "#####" + page.getString("id");
            if (page.getString("article").equals("")==false && page.getJSONObject("infobox").length()!=0) {
            	 en_cl_pages_titleid2page.put(titleid, line);
            }
        }
        bufferedReader.close();
        bufferedReader = new BufferedReader(new FileReader(new File(zh_cl_pages)));
        while (true) {
            line = bufferedReader.readLine();
            if (line == null) {
            	break;
            }
            JSONObject page = new JSONObject(line);
            String titleid = page.getString("title") + "#####" + page.getString("id");
            if (page.getString("article").equals("")==false && page.getJSONObject("infobox").length()!=0) {
            	zh_cl_pages_titleid2page.put(titleid, line);
            }
        }
        bufferedReader.close();
        BufferedWriter bufferedWriter_enzhtitleid = new BufferedWriter(new FileWriter(new File("en_zh_cl_titleid.txt")));
        BufferedWriter bufferedWriter_en = new BufferedWriter(new FileWriter(new File("en_cl_page.txt")));
        BufferedWriter bufferedWriter_zh = new BufferedWriter(new FileWriter(new File("zh_cl_page.txt")));
        BufferedWriter bufferedWriter_attr_en = new BufferedWriter(new FileWriter(new File("en_cl_attr.txt")));
        BufferedWriter bufferedWriter_attr_zh = new BufferedWriter(new FileWriter(new File("zh_cl_attr.txt")));
        Map<String, Integer> en_attrs = new LinkedHashMap<String, Integer>();
        Map<String, Integer> zh_attrs = new LinkedHashMap<String, Integer>();
        bufferedReader = new BufferedReader(new FileReader(new File(en_zh_cl_id_title)));
        while (true) {
            line = bufferedReader.readLine();
            if (line == null) {
            	break;
            }
            String[] words = line.split("\t\t");
            String en_titleid = words[0] + "#####" + words[1];
            String zh_titleid = words[2] + "#####" + words[3];
            if (en_cl_pages_titleid2page.containsKey(en_titleid) && zh_cl_pages_titleid2page.containsKey(zh_titleid)) {
            	bufferedWriter_enzhtitleid.write(line + "\n");
            	bufferedWriter_en.write(en_cl_pages_titleid2page.get(en_titleid) + "\n");
            	bufferedWriter_zh.write(zh_cl_pages_titleid2page.get(zh_titleid) + "\n");
            	
            	JSONObject en_page = new JSONObject(en_cl_pages_titleid2page.get(en_titleid));
            	JSONObject en_infobox = en_page.getJSONObject("infobox");
            	for (String k : en_infobox.keySet()) {
            		if (en_attrs.containsKey(k)) {
            			en_attrs.put(k, en_attrs.get(k)+1);
            		} else {
            			en_attrs.put(k, 0);
            		}
            	}
            	
            	JSONObject zh_page = new JSONObject(zh_cl_pages_titleid2page.get(zh_titleid));
            	JSONObject zh_infobox = zh_page.getJSONObject("infobox");
            	for (String k : zh_infobox.keySet()) {
            		if (zh_attrs.containsKey(k)) {
            			zh_attrs.put(k, zh_attrs.get(k)+1);
            		} else {
            			zh_attrs.put(k, 0);
            		}
            	}
            }
        }
        bufferedReader.close();
        bufferedWriter_enzhtitleid.close();
        bufferedWriter_en.close();
        bufferedWriter_zh.close();
        
        // 通过ArrayList构造函数把map.entrySet()转换成list
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(en_attrs.entrySet());
        // 通过比较器实现比较排序
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> mapping1, Map.Entry<String, Integer> mapping2) {
                return mapping1.getValue().compareTo(mapping2.getValue());
            }
        });
        // 通过ArrayList构造函数把map.entrySet()转换成list
        list = new ArrayList<Map.Entry<String, Integer>>(zh_attrs.entrySet());
        // 通过比较器实现比较排序
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> mapping1, Map.Entry<String, Integer> mapping2) {
                return mapping1.getValue().compareTo(mapping2.getValue());
            }
        });
        
        for (String k : en_attrs.keySet()) {
        	bufferedWriter_attr_en.write(k + "\t\t" + en_attrs.get(k) + "\n");
        }
        for (String k : zh_attrs.keySet()) {
        	bufferedWriter_attr_zh.write(k + "\t\t" + zh_attrs.get(k) + "\n");
        }
        bufferedWriter_attr_en.close();
        bufferedWriter_attr_zh.close();
	}
	
	public void getCLEntities(String en_zh_cl_id_title, String en_pages, String zh_pages) throws Exception {
		
		Map<String, String> cl_en_id2title = new HashMap<String, String>();
		Map<String, String> cl_en_title2id = new HashMap<String, String>();
		Map<String, String> cl_zh_id2title = new HashMap<String, String>();
		Map<String, String> cl_zh_title2id = new HashMap<String, String>();		
		BufferedReader bufferedReaderCL = new BufferedReader(new FileReader(new File(en_zh_cl_id_title)));
		String line = new String();
        while (true) {
            line = bufferedReaderCL.readLine();
            if (line == null) {
            	break;
            }
            String[] words = line.split("\t\t");
            cl_en_id2title.put(words[1], words[0]);
            cl_en_title2id.put(words[0], words[1]);
            cl_zh_id2title.put(words[3], words[2]);
            cl_zh_title2id.put(words[2], words[3]);
        }
        bufferedReaderCL.close();
		
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(en_pages)));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(en_pages.replace(".xml", ".cl.xml"))));
        line = new String();
        int cnt = 0;
        while (true) {
            line = bufferedReader.readLine();
            if (line == null) {
            	break;
            }
            JSONObject page = new JSONObject(line);
//            System.out.println(page.get("id") + "\t\t" + page.get("title"));
            if (cl_en_id2title.containsKey( page.get("id") ) && cl_en_title2id.containsKey( page.get("title") )) {
            	bufferedWriter.write(line + "\n");
            	cnt += 1;
            	if (cnt%100000==0) {
                	System.out.println("__" + cnt);
                }
            }
        }
        bufferedReader.close();
        bufferedWriter.close();
        
        bufferedReader = new BufferedReader(new FileReader(new File(zh_pages)));
        bufferedWriter = new BufferedWriter(new FileWriter(new File(zh_pages.replace(".xml", ".cl.xml"))));
        line = new String();
        while (true) {
            line = bufferedReader.readLine();
            if (line == null) {
            	break;
            }
            JSONObject page = new JSONObject(line);
            if (cl_zh_id2title.containsKey( page.get("id") ) && cl_zh_title2id.containsKey( page.get("title") )) {
            	bufferedWriter.write(line + "\n");
            }
        }
        bufferedReader.close();
        bufferedWriter.close();
	}
	
	public void CLFilter(String zh_cl, String en_id_title, String zh_id_title) throws Exception {
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
            line = line.replace("\\'", "'");
            for (String word : filter_words) {
            	if (line.toLowerCase().contains(word.toLowerCase())) {
            		contains = true;
            		break;
            	}
            }
            if (contains == false) {
            	bufferedWriter.write(line + "\n");
            }
        }
        bufferedReader.close();
        bufferedWriter.close();
		
        Map<String, String> cl_zh_id2title_dict = read_w_t_t_w(zh_cl.replace("result.", "result.new."), 0);
        Map<String, String> cl_zh_title2id_dict = read_w_t_t_w(zh_cl.replace("result.", "result.new."), 1);
		Map<String, String> en_title2id_dict = read_w_t_t_w(en_id_title, 1); 
		Map<String, String> zh_id2title_dict = read_w_t_t_w(zh_id_title, 0);
		
		Set<String> common_zh_id = new HashSet<String>();
		common_zh_id.addAll(zh_id2title_dict.keySet());
		common_zh_id.retainAll(cl_zh_id2title_dict.keySet());
        System.out.println(common_zh_id.size());
        
        
		Set<String> common_en_title = new HashSet<String>();
		common_en_title.addAll(en_title2id_dict.keySet());
		common_en_title.retainAll(cl_zh_title2id_dict.keySet());
        System.out.println(common_en_title.size());
        
        bufferedWriter = new BufferedWriter(new FileWriter(new File("en_zh_cl_id_title.txt")));
        for (String k : cl_zh_id2title_dict.keySet()) {
        	if (common_zh_id.contains(k) && common_en_title.contains(cl_zh_id2title_dict.get(k))) {
        		bufferedWriter.write(cl_zh_id2title_dict.get(k) + "\t\t" + en_title2id_dict.get(cl_zh_id2title_dict.get(k)) + "\t\t" + zh_id2title_dict.get(k) + "\t\t" + k + "\n");
        	}
        }
        bufferedWriter.close();
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
