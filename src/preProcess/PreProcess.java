package preProcess;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import org.tartarus.snowball.ext.englishStemmer;

public class PreProcess {

	public static void main(String args[]) throws Exception {
		PreProcess pp = new PreProcess();
		System.out.println("Extraction starts at:" + new Date());
		
//		pp.duplicateEntitiesRemove("./etc/", "wiki-latest-pages-articles-multistream.result.xml");
		
//		pp.CLFilter("./etc/zhwiki-latest-langlinks.2en.result.sql", "./etc/enwiki-latest-pages-articles-multistream.id_titleresult.id_titlexml", "./etc/zhwiki-latest-pages-articles-multistream.id_titleresult.id_titlexml");
//		pp.getAllCLEntities("./etc/en_zh_cl_id_title.txt", "./etc/enwiki-latest-pages-articles-multistream.result.xml", "./etc/zhwiki-latest-pages-articles-multistream.result.xml");
//		pp.getMatchedCLEntities("./etc/en_zh_cl_id_title.txt", "./etc/enwiki-latest-pages-articles-multistream.result.cl.xml", "./etc/zhwiki-latest-pages-articles-multistream.result.cl.xml");
		

//		pp.getText("./etc/enwiki-latest-pages-articles-multistream.result.xml", "en");
		pp.getText("./etc/zhwiki-latest-pages-articles-multistream.result.xml", "zh");
		
//		pp.genTextualNetwork("./etc/enwiki.text", "en");
//		pp.genTextualNetwork("/Users/locke/Desktop/a.txt", "en");
		
	}
	
	public void genTextualNetwork(String pages, String lang) throws Exception {
	
	}
	
	public void getText(String pages, String lang) throws Exception {
		BufferedReader bufferedReader_pages = new BufferedReader(new FileReader(new File(pages)));
		BufferedWriter bufferedWriter_text = new BufferedWriter(new FileWriter(new File(lang + "wiki.text")));
		String line = null;
		if (lang.equals("en")) {
			Set<String> stopwords = new HashSet<String>();
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("./stopwords.txt")));
	        while (null != (line = bufferedReader.readLine())) {
	        	stopwords.add(line.trim());
	        }
	        bufferedReader.close(); 
	        englishStemmer stemmer = new englishStemmer();	        
	        line = null;
	        while (null != (line = bufferedReader_pages.readLine())) {
	        	JSONObject page = new JSONObject(line);
	        	String title = page.getString("title");
	        	String article = page.getString("article");
	    		
	        	List<String> links = new ArrayList<String>();
	        	Matcher matcher = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(article);
	    		while (matcher.find()) {
	    			links.add(matcher.group(0));
	    		}
	    		List<String> entity_links = new ArrayList<String>();
	    		
	    		for (String el : links) {
	    			if (el.contains("|")) {
	    				entity_links.add(el.substring(2, el.indexOf("|")).replace(" ", "_"));
	    			} else {
	    				entity_links.add(el.substring(2, el.length()-2).replace(" ", "_"));
	    			}
					article = article.replace(el, "");
				}
//	        	System.out.println(entity_links);
	    		article = article.replaceAll("[^_a-zA-Z0-9\u4e00-\u9fa5]+", " ").replaceAll("\\s+", " ").trim();
	    		String[] words = article.split(" ");
	    		List<String> words2article = new ArrayList<String>();
	    		for (String word : words) {
	    			if (stopwords.contains(word) || word.length() < 2 || !StringUtils.isAlphaSpace(word)) {
	    				continue;
	    			}
	    			stemmer.setCurrent(word);
	    			if (stemmer.stem()==false){
	    				continue;
	    			}
	    			words2article.add(stemmer.getCurrent());
	    		}
		        article = StringUtils.join(words2article, " ");
//	    		System.out.println(article);
	        	bufferedWriter_text.write(title.replace(" ", "_") + "\t\t" + article+"|||"+StringUtils.join(entity_links, " ") + "\n");
        	}
		} else {
			Segment segment = HanLP.newSegment();
			segment.enableNameRecognize(true);
			segment.enableOrganizationRecognize(true);
			segment.enablePlaceRecognize(true);
			segment.enableTranslatedNameRecognize(true);
			while (null != (line = bufferedReader_pages.readLine())) {
	        	JSONObject page = new JSONObject(line);
	        	String title = page.getString("title");
	        	String article = page.getString("article");
	    		
	        	List<String> links = new ArrayList<String>();
	        	Matcher matcher = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(article);
	    		while (matcher.find()) {
	    			links.add(matcher.group(0));
	    		}
	    		List<String> entity_links = new ArrayList<String>();
	    		
	    		for (String el : links) {
	    			if (el.contains("|")) {
	    				entity_links.add(el.substring(2, el.indexOf("|")).replace(" ", "_"));
	    			} else {
	    				entity_links.add(el.substring(2, el.length()-2).replace(" ", "_"));
	    			}
					article = article.replace(el, "");
				}	    		
	    		article = article.replaceAll( "[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]" , "").replaceAll("\\s+", " ").trim();
	    		List<Term> words = segment.seg(article);
	    		List<String> words2article = new ArrayList<String>();
	    		for (Term word : words) {
	    			words2article.add(word.word);
	    		}
		        article = StringUtils.join(words2article, " ");
	    		System.out.println(article);
	    		break;
//	        	bufferedWriter_text.write(title.replace(" ", "_") + "\t\t" + article+"|||"+StringUtils.join(entity_links, " ") + "\n");
			}
		}
        bufferedReader_pages.close();
        bufferedWriter_text.close();
	}
	
	public void numberAll(String en_pages, String zh_pages) throws Exception {
		Map<String, String> en_entity_Id2Tit = new HashMap<String, String>();
		Map<String, String> en_entity_Tit2Id = new HashMap<String, String>();

//		    filter                         zhwiki.id.word
//		enwiki.id.word      zhwiki.enwiki.cross.net.test   zhwiki.linkage.net
//		enwiki.linkage.net  zhwiki.enwiki.cross.net.train  zhwiki.textual.net
//		enwiki.textual.net  zhwiki.id.entity
		
		
		BufferedReader bufferedReader_pages_en = new BufferedReader(new FileReader(new File(en_pages)));
		BufferedWriter bufferedWriter_entity_en = new BufferedWriter(new FileWriter(new File("enwiki.id.entity")));
		BufferedWriter bufferedWriter_word_en = new BufferedWriter(new FileWriter(new File("enwiki.id.word")));
		
		String line = null;
        while (null != (line = bufferedReader_pages_en.readLine())) {
        	JSONObject page = new JSONObject(line);
        	String title = page.getString("title");
        	bufferedWriter_entity_en.write(title + "\n");
        	
        	int id = en_entity_Id2Tit.size();
        	en_entity_Id2Tit.put("e_en_"+id, title);
        	en_entity_Tit2Id.put(title, "e_en_"+id);
        	
        	String article = page.getString("article");

        }
        
        
        
        
		BufferedReader bufferedReader_pages_zh = new BufferedReader(new FileReader(new File(zh_pages)));
		
        
	}
	
	public void duplicateEntitiesRemove(String page_loc, String suffix) throws Exception {
		String[] type = {"en", "zh"};
		for (String t : type) {
			BufferedReader bufferedReader_pages = new BufferedReader(new FileReader(new File(page_loc + t + suffix)));
			Map<String, String> pages_map = new HashMap<String, String>();
			String line = null;
	        while (null != (line = bufferedReader_pages.readLine())) {
	        	if (t.equals("zh")) {
	        		line = HanLP.convertToSimplifiedChinese(line).toLowerCase();
	        	} else {
	        		line = line.toLowerCase();
	        	}
	        	JSONObject page = new JSONObject(line);
	        	page.getString("article");
	        	page.getString("title");
	        	if (pages_map.containsKey(page.getString("title"))) {
	        		if (line.length() > pages_map.get(page.getString("title")).length()) {
	        			pages_map.put(page.getString("title"), line);
	        		}
	        	} else {
	        		pages_map.put(page.getString("title"), line);
	        	}
	        }
	        bufferedReader_pages.close();
	        System.out.println(t + "_pages after duplicateEntitiesRemove: " + pages_map.size());
	        
	        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(t + "_pages.json")));
	        for (String k : pages_map.keySet()) {
	        	bufferedWriter.write(pages_map.get(k) + "\n");
	        }
	        bufferedWriter.close();
		}
	}
	
	public void getMatchedCLEntities(String en_zh_cl_id_title, String en_cl_pages, String zh_cl_pages) throws Exception {
		Map<String, String> en_cl_pages_titleid2page = new HashMap<String, String>();
		Map<String, String> zh_cl_pages_titleid2page = new HashMap<String, String>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(en_cl_pages)));
		String line = new String();
        while (null != (line = bufferedReader.readLine())) {            
            JSONObject page = new JSONObject(line);
            String titleid = page.getString("title") + "#####" + page.getString("id");
            if (page.getString("article").equals("")==false) {
            	 en_cl_pages_titleid2page.put(titleid, line);
            }
        }
        bufferedReader.close();
        bufferedReader = new BufferedReader(new FileReader(new File(zh_cl_pages)));
        while (null != (line = bufferedReader.readLine())) {
            JSONObject page = new JSONObject(line);
            String titleid = page.getString("title") + "#####" + page.getString("id");
            if (page.getString("article").equals("")==false) {
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
        while (null != (line = bufferedReader.readLine())) {
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
            			en_attrs.put(k, 1);
            		}
            	}
            	
            	JSONObject zh_page = new JSONObject(zh_cl_pages_titleid2page.get(zh_titleid));
            	JSONObject zh_infobox = zh_page.getJSONObject("infobox");
            	for (String k : zh_infobox.keySet()) {
            		if (zh_attrs.containsKey(k)) {
            			zh_attrs.put(k, zh_attrs.get(k)+1);
            		} else {
            			zh_attrs.put(k, 1);
            		}
            	}
            }
        }
        bufferedReader.close();
        bufferedWriter_enzhtitleid.close();
        bufferedWriter_en.close();
        bufferedWriter_zh.close();
        System.out.println(en_attrs.size());
        System.out.println(zh_attrs.size());
        
        // 通过ArrayList构造函数把map.entrySet()转换成list
        List<Map.Entry<String, Integer>> en_list = new ArrayList<Map.Entry<String, Integer>>(en_attrs.entrySet());
        // 通过比较器实现比较排序
        Collections.sort(en_list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> mapping1, Map.Entry<String, Integer> mapping2) {
                return mapping1.getValue().compareTo(mapping2.getValue());
            }
        });
        // 通过ArrayList构造函数把map.entrySet()转换成list
        List<Map.Entry<String, Integer>> zh_list = new ArrayList<Map.Entry<String, Integer>>(zh_attrs.entrySet());
        // 通过比较器实现比较排序
        Collections.sort(zh_list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> mapping1, Map.Entry<String, Integer> mapping2) {
                return mapping1.getValue().compareTo(mapping2.getValue());
            }
        });
        
        for (Map.Entry<String, Integer> item : en_list) {
        	bufferedWriter_attr_en.write(item.getKey() + "\t\t" + item.getValue() + "\n");
        }
        for (Map.Entry<String, Integer> item : zh_list) {
        	bufferedWriter_attr_zh.write(item.getKey() + "\t\t" + item.getValue() + "\n");
        }
        bufferedWriter_attr_en.close();
        bufferedWriter_attr_zh.close();
	}
	
	public void getAllCLEntities(String en_zh_cl_id_title, String en_pages, String zh_pages) throws Exception {
		Map<String, String> cl_en_id2title = new HashMap<String, String>();
		Map<String, String> cl_en_title2id = new HashMap<String, String>();
		Map<String, String> cl_zh_id2title = new HashMap<String, String>();
		Map<String, String> cl_zh_title2id = new HashMap<String, String>();		
		BufferedReader bufferedReaderCL = new BufferedReader(new FileReader(new File(en_zh_cl_id_title)));
		String line = new String();
        while (null != (line = bufferedReaderCL.readLine())) {
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
        while (null != (line = bufferedReader.readLine())) {
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
        while (null != (line = bufferedReader.readLine())) {
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
        while (null != (line = bufferedReader.readLine())) {
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
        while (null != (line = bufferedReader.readLine())) {
            String words[] = line.split("\t\t");
            w_t_t_w.put(words[key_pos], words[1-key_pos]);
        }
        bufferedReader.close();
        return w_t_t_w;
	}
	
	
	
	
}
