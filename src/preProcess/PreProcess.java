package preProcess;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.HashedMap;
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
		
//		pp.CLFilter("./etc/zhwiki-latest-langlinks.2en.result.sql", "./etc/en_id_title.txt", "./etc/zh_id_title.txt");
//		pp.getAllCLEntities("./etc/en_zh_cl_id_title.txt", "./etc/en_pages.json", "./etc/zh_pages.json");
//		pp.getMatchedCLEntities("./etc/en_zh_cl_id_title.txt", "./etc/en_pages.cl.json", "./etc/zh_pages.cl.json");
		
//		pp.getText("./etc/en_pages.json", "en");
//		pp.getText("./etc/zh_pages.json", "zh");
		
//		pp.genTrainData("./etc/cl.train.40000.net", "./etc/enwiki_zhwiki_cl.txt", "./etc/enwiki.text", "./etc/zhwiki.text");
		
//		pp.genTextualNetPTEInput("./etc/enwiki.text", "en", 5);
//		pp.genTextualNetPTEInput("./etc/zhwiki.text", "zh", 5);
		
//		pp.genLinkageNet("./etc/enwiki.text", "en");
//		pp.genLinkageNet("./etc/zhwiki.text", "zh");
		
//		pp.genCL("./etc/en_zh_cl_titleid.txt", "./etc/en_title_all.txt", "./etc/zh_title_all.txt");
//		pp.sampleCL("./etc/enwiki_zhwiki_cl.txt", 40000, 4, 3000);
		
		
		
		// added 1118
//		pp.getNewText("./etc/en_pages.json", "en");
		
//		pp.genNewTextualNetandLinkageNet("./etc/enwiki.text", "en", 5);
		
		pp.getTriples("./etc/en_pages.json", "en", 100);
	}
	
	public void genTrainData(String cl_train, String cl_all, String en_wiki_text, String zh_wiki_text) throws Exception {
		BufferedReader bufferedReader_cl = new BufferedReader(new FileReader(new File(cl_train)));
		
		Set<String> en_titles = new HashSet<String>(), sub_en_titles = new HashSet<String>(), pre_en_titles = new HashSet<String>();
		Set<String> zh_titles = new HashSet<String>(), sub_zh_titles = new HashSet<String>();
		
		int JUMP = 2;
		
		String line = null;
		while (null != (line = bufferedReader_cl.readLine())) {            
			String[] words = line.split("\t");
			en_titles.add(words[0]);
			pre_en_titles.add(words[0]);
			zh_titles.add(words[1]);
        }
		bufferedReader_cl.close();
		
		for (int i = 0; i < JUMP; i += 1) {
			System.out.println("In JUMP:" + i);
			line = null;
			BufferedReader bufferedReader_en = new BufferedReader(new FileReader(new File(en_wiki_text)));
			while (null != (line = bufferedReader_en.readLine())) {            
				String[] words = line.split("\t\t");
				if (en_titles.contains("e_en_"+words[0]) && words.length>1) {
					String[] w_e = words[1].split("\\|\\|\\|");
		            if (w_e.length>1) {
		            	for (String s : w_e[1].split(" ")) {
		            		String e = "e_en_" + s;
		            		sub_en_titles.add(e);
		            	}
		            }
				}
	        }
			bufferedReader_en.close();
			
			line = null;
			BufferedReader bufferedReader_zh = new BufferedReader(new FileReader(new File(zh_wiki_text)));
			while (null != (line = bufferedReader_zh.readLine())) {            
				String[] words = line.split("\t\t");
				if (zh_titles.contains("e_zh_"+words[0]) && words.length>1) {
					String[] w_e = words[1].split("\\|\\|\\|");
		            if (w_e.length>1) {
		            	for (String s : w_e[1].split(" ")) {
		            		String e = "e_zh_" + s;
		            		sub_zh_titles.add(e);
		            	}
		            }
				}
	        }
			bufferedReader_zh.close();
			
			System.out.println("en_titles:"+en_titles.size()+"\tsub_en_titles:"+sub_en_titles.size());
			System.out.println("zh_titles:"+zh_titles.size()+"\tsub_zh_titles:"+sub_zh_titles.size());
			en_titles.addAll(sub_en_titles);
			zh_titles.addAll(sub_zh_titles);
			System.out.println("after, en_titles:"+en_titles.size());
			System.out.println("after, zh_titles:"+zh_titles.size());
		}

		line = null;
		BufferedReader bufferedReader_cl_all = new BufferedReader(new FileReader(new File(cl_all)));
		Set<String> test_titles = new HashSet<String>();
		while (null != (line = bufferedReader_cl_all.readLine())) {            
			String[] words = line.split("\t");
			test_titles.add(words[0]);
        }
		bufferedReader_cl_all.close();
		test_titles.retainAll(en_titles);
		System.out.println("test_titles:" + test_titles.size());
		
		bufferedReader_cl_all = new BufferedReader(new FileReader(new File(cl_all)));
		BufferedWriter bufferedWriter_cl = new BufferedWriter(new FileWriter(new File("cl.test.net")));
		while (null != (line = bufferedReader_cl_all.readLine())) {            
			String[] words = line.split("\t");
			if (test_titles.contains(words[0])==true && pre_en_titles.contains(words[0])==false) {
				bufferedWriter_cl.write(line + "\n");
			}
        }
		bufferedReader_cl_all.close();
		bufferedWriter_cl.close();
		
		BufferedReader bufferedReader_en = new BufferedReader(new FileReader(new File(en_wiki_text)));
		BufferedWriter bufferedWriter_en = new BufferedWriter(new FileWriter(new File(en_wiki_text.replace(".text", ".40000.text"))));
		line = null;
		while (null != (line = bufferedReader_en.readLine())) {            
			String[] words = line.split("\t\t");
			if (en_titles.contains("e_en_"+words[0])) {
				bufferedWriter_en.write(line + "\n");
			}
        }
		bufferedReader_en.close();
		bufferedWriter_en.close();
		
		BufferedReader bufferedReader_zh = new BufferedReader(new FileReader(new File(zh_wiki_text)));
		BufferedWriter bufferedWriter_zh = new BufferedWriter(new FileWriter(new File(zh_wiki_text.replace(".text", ".40000.text"))));
		while (null != (line = bufferedReader_zh.readLine())) {            
			String[] words = line.split("\t\t");
			if (zh_titles.contains("e_zh_"+words[0])) {
				bufferedWriter_zh.write(line + "\n");
			}
        }
		bufferedReader_zh.close();
		bufferedWriter_zh.close();
	}
	
	public void sampleCL(String enwiki_zhwiki_cl, int train_num, int times, int test_num) throws Exception {
		BufferedReader bufferedReader_cl = new BufferedReader(new FileReader(new File(enwiki_zhwiki_cl)));
		List<String> cls = new ArrayList<String>();
		String line = new String();
		while (null != (line = bufferedReader_cl.readLine())) {            
			cls.add(line);
        }
		bufferedReader_cl.close();
		
		Collections.shuffle(cls);
		
		BufferedWriter bufferedWriter_test = new BufferedWriter(new FileWriter(new File("cl.test."+test_num+".net")));
		for (int i = 0; i < test_num; i += 1) {
			bufferedWriter_test.write(cls.get(0) + "\t1\tc\n");
			cls.remove(0);
		}
		bufferedWriter_test.close();
		
		for (int i = 0; i < times; i += 1) {
			BufferedWriter bufferedWriter_train = new BufferedWriter(new FileWriter(new File("cl.train."+(i+1)*train_num+".net")));
			for (int j = 0; j < (i+1)*train_num; j += 1) {
				bufferedWriter_train.write(cls.get(j) + "\t1\tc\n");
			}
			bufferedWriter_train.close();
		}
	}
	
	public void genCL(String en_zh_cl_titleid, String en_title_all, String zh_title_all) throws Exception {
		BufferedReader bufferedReader_cl = new BufferedReader(new FileReader(new File(en_zh_cl_titleid)));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("enwiki_zhwiki_cl.txt")));
		BufferedReader bufferedReader_en = new BufferedReader(new FileReader(new File(en_title_all)));
		BufferedReader bufferedReader_zh = new BufferedReader(new FileReader(new File(zh_title_all)));
		Set<String> en_title = new HashSet<String>();
		Set<String> zh_title = new HashSet<String>();
		
		String line = new String();
		while (null != (line = bufferedReader_en.readLine())) {            
            en_title.add(line);
        }
		bufferedReader_en.close();
		
		line = null;
		while (null != (line = bufferedReader_zh.readLine())) {            
			zh_title.add(line);
        }
		bufferedReader_zh.close();
		
		line = null;
		while (null != (line = bufferedReader_cl.readLine())) {            
            String[] words = line.split("\t\t");
            if (en_title.contains("e_en_"+words[0].replace(" ", "_")) && zh_title.contains("e_zh_"+words[2].replace(" ", "_"))) {
            	bufferedWriter.write("e_en_"+words[0].replace(" ", "_") + "\t" + "e_zh_"+words[2].replace(" ", "_") + "\n");
            }
        }
		bufferedReader_cl.close();
		bufferedWriter.close();
	}
	
	public void genTextualNetPTEInput(String text_file, String lang, int min_count) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(text_file)));
		BufferedWriter bufferedWriter_text = new BufferedWriter(new FileWriter(new File(lang + "_text_all.txt")));
		BufferedWriter bufferedWriter_title = new BufferedWriter(new FileWriter(new File(lang + "_title_all.txt")));
		Map<String, Integer> freq_dict = new HashedMap<String, Integer>();
		String line = new String();
        while (null != (line = bufferedReader.readLine())) {
            String words[] = line.split("\t\t")[1].split("\\|\\|")[0].split(" ");
            for (String w : words) {
            	if (lang.equals("zh") && w.matches("[\u4e00-\u9fa5]+")==false) {
		            continue;
		        }
        		if (!freq_dict.containsKey(w)) {
        			freq_dict.put(w, 1);
        		} else {
        			freq_dict.put(w, freq_dict.get(w)+1);
        		}
            }
        }
        System.out.println("Total words num: " + freq_dict.size());
		
        bufferedReader = new BufferedReader(new FileReader(new File(text_file)));
		line = new String();
        while (null != (line = bufferedReader.readLine())) {
            String words[] = line.split("\t\t");
            String[] w_e = words[1].split("\\|\\|\\|");
            List<String> text_word = new ArrayList<String>();
            Set<String> bb = new HashSet<String>();
            if (w_e.length > 1) {
	            if (w_e[1].length()>0) {
	            	for (String en : w_e[1].split(" ")) {
	            		if (freq_dict.containsKey(en)) {
	            			bb.add(en);
	            		}
	                }
	            }
            }
            if (w_e[0].length()>0) {
            	String[] tmp_w = w_e[0].split(" ");
            	for (String w : tmp_w) {
            		if (freq_dict.containsKey(w) && freq_dict.get(w) >= min_count) {
            			if (bb.contains(w)) {
            				text_word.add("e_"+lang+"_"+w);
            			} else {
            				text_word.add("w_"+lang+"_"+w);
            			}
            		}
            	}
            }
            String pure_text = StringUtils.join(text_word, " ").trim();
            if (pure_text.equals("") == false) {
            	bufferedWriter_title.write("e_"+lang+"_" + words[0] + "\n");
            	bufferedWriter_text.write(pure_text + "\n");
            }
        }
        bufferedReader.close();
        bufferedWriter_text.close();
        bufferedWriter_title.close();
	}
	
	public void genLinkageNet(String text_file, String lang) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(text_file)));
		BufferedWriter bufferedWriter_net = new BufferedWriter(new FileWriter(new File(lang + ".linkage.net")));
		Set<String> title_set = new HashSet<String>();
		String line = new String();
        while (null != (line = bufferedReader.readLine())) {
        	title_set.add(line.split("\t\t")[0]);
        }
        System.out.println("Total titles num: " + title_set.size());
		
        bufferedReader = new BufferedReader(new FileReader(new File(text_file)));
		line = new String();
        while (null != (line = bufferedReader.readLine())) {
        	String words[] = line.split("\t\t");
            String[] w_e = words[1].split("\\|\\|\\|");
            Map<String, Integer> target = new HashMap<String, Integer>();
            if (w_e.length > 1) {
	            if (w_e[1].length()>0) {
	            	for (String en : w_e[1].split(" ")) {
	            		if (title_set.contains(en)) {
	            			if (target.containsKey(en)) {
	            				target.put(en, target.get(en)+1);
	            			} else {
	            				target.put(en, 1);
	            			}
	            		}
	                }
	            }
            }
            String start = "e_"+lang+"_" + words[0];
            for (String k : target.keySet()) {
            	bufferedWriter_net.write(start + "\t" + "e_"+lang+"_"+k + "\t" + target.get(k) + "\tl" + "\n");
            }
        }
        bufferedReader.close();
        bufferedWriter_net.close();
	}
	
	// added 1118
	public void getTriples(String pages, String lang, int limit) throws Exception {
		BufferedReader bufferedReader_pages = new BufferedReader(new FileReader(new File(pages)));
		BufferedWriter bufferedWriter_seman = new BufferedWriter(new FileWriter(new File(lang + ".semantic.net")));
		englishStemmer stemmer = new englishStemmer();
		Segment segment = HanLP.newSegment();
		segment.enableNameRecognize(true);
		segment.enableOrganizationRecognize(true);
		segment.enablePlaceRecognize(true);
		segment.enableTranslatedNameRecognize(true);
		BufferedReader bufferedReader_title = new BufferedReader(new FileReader(new File(pages.replace("_pages.json", "_title_all.txt"))));
		String line = null;
		Set<String> entities = new HashSet<String>();
		while (null != (line = bufferedReader_title.readLine())) {
			entities.add(line.trim());
		}
		
		Map<String, Integer> attr_freq = new HashMap<String, Integer>();
		line = null;
		while (null != (line = bufferedReader_pages.readLine())) {
			JSONObject page = new JSONObject(line);
			JSONObject infobox = page.getJSONObject("infobox");
			for (String k : infobox.keySet()) {
				if (attr_freq.containsKey(k)) {
					attr_freq.put(k, attr_freq.get(k)+1);
				} else {
					attr_freq.put(k, 1);
				}
			}
		}
		System.out.println("len(attr_freq):" + attr_freq.size());
		BufferedWriter bufferedWriter_attr = new BufferedWriter(new FileWriter(new File(lang + ".attr_cnt.net")));
		List<Map.Entry<String, Integer>> en_list = new ArrayList<Map.Entry<String, Integer>>(attr_freq.entrySet());
        // 通过比较器实现比较排序
        Collections.sort(en_list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> mapping1, Map.Entry<String, Integer> mapping2) {
                return mapping2.getValue().compareTo(mapping1.getValue());
            }
        });
        for (Map.Entry<String, Integer> s : en_list) {
        	bufferedWriter_attr.write(s.getKey() + "\t" + s.getValue() + "\n");
        }
        bufferedWriter_attr.close();
        System.out.println("Write out done");
        
        
		line = null;
		bufferedReader_pages = new BufferedReader(new FileReader(new File(pages)));
		while (null != (line = bufferedReader_pages.readLine())) {
			JSONObject page = new JSONObject(line);
			String title = "e_"+lang+"_"+page.getString("title").toLowerCase();
			if (entities.contains(title)==false) {
				continue;
			}
			JSONObject infobox = page.getJSONObject("infobox");

			for (String k : infobox.keySet()) {
				if (attr_freq.get(k) < limit) {
					continue;
				}
				String val = infobox.getString(k);
				Set<String> attr_vals = new HashSet<String>();

				List<String> links = new ArrayList<String>();
				Matcher matcher = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(val);
				while (matcher.find()) {
					links.add(matcher.group(0));
				}
				for (String el : links) {
					String n_el;
					if (el.contains("|")) {
						n_el = "e_"+lang+"_"+el.substring(2, el.indexOf("|")).replace(" ", "_");
					} else {
						n_el = "e_"+lang+"_"+el.substring(2, el.length()-2).replace(" ", "_");
					}
					attr_vals.add(n_el);
					val = val.replace(el, " ").replaceAll("\\s+", " ").trim();
				}

				if (val.equals("")==false) {
					if (lang.equals("en")) {
						String tmp_spl[] = val.split(" ");
						for (String word : tmp_spl) {
							if (!StringUtils.isAlphaSpace(word)) {
								attr_vals.add("w_xx_"+word);
							} else {
								stemmer.setCurrent(word);
								if (stemmer.stem()==false){ continue;	}
								attr_vals.add("w_"+lang+"_"+stemmer.getCurrent());
							}
						}
					} else {
						List<Term> its = segment.seg(val);
						for (Term word : its) {
							if (word.word.matches("[\u4e00-\u9fa5]+")==false) {
								attr_vals.add("w_xx_"+word.word);
							} else {
								attr_vals.add("w_"+lang+"_"+word.word);
							}
						}
					}
				}

				for (String w : attr_vals) {
					bufferedWriter_seman.write(title + "\t" + "r_"+lang+"_"+k + "\t" + w + "\ts\n");
				}
			}
		}
		bufferedReader_title.close();
		bufferedReader_pages.close();
		bufferedWriter_seman.close();
	}
	public void genNewTextualNetandLinkageNet(String text_file, String lang, int min_count) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(text_file)));
		BufferedWriter bufferedWriter_text = new BufferedWriter(new FileWriter(new File(lang + "_text_all.txt")));
		BufferedWriter bufferedWriter_title = new BufferedWriter(new FileWriter(new File(lang + "_title_all.txt")));
		BufferedWriter bufferedWriter_linkage = new BufferedWriter(new FileWriter(new File(lang + ".linkage.net")));
		Set<String> title_set = new HashSet<String>();
		Map<String, Integer> freq_dict = new HashedMap<String, Integer>();
		
		String line = new String();
		while (null != (line = bufferedReader.readLine())) {
			title_set.add("e_"+lang+"_"+line.split("\t\t")[0]);
			String words[] = line.split("\t\t")[1].split("\\|\\|")[0].split(" ");
			for (String w : words) {
				if (lang.equals("zh") && w.matches("[\u4e00-\u9fa5]+")==false) {
					continue;
				}
				if (!freq_dict.containsKey(w)) {
					freq_dict.put(w, 1);
				} else {
					freq_dict.put(w, freq_dict.get(w)+1);
				}
			}
		}
		System.out.println("Total words num: " + freq_dict.size());
		
		bufferedReader = new BufferedReader(new FileReader(new File(text_file)));
		line = new String();
		while (null != (line = bufferedReader.readLine())) {
			String words[] = line.split("\t\t");
			String[] w_e = words[1].split("\\|\\|\\|");
			List<String> text_word = new ArrayList<String>();
			Map<String, Integer> link_dict = new HashedMap<String, Integer>();
			if (w_e.length > 1) {
				if (w_e[1].length()>0) {
					for (String en : w_e[1].split(" ")) {
						if (title_set.contains(en)) {
							if (link_dict.containsKey(en)) {
								link_dict.put(en, link_dict.get(en)+1);
							} else {
								link_dict.put(en, 1);
							}
						}
					}
				}
			}
			if (w_e[0].length()>0) {
				String[] tmp_w = w_e[0].split(" ");
				for (String w : tmp_w) {
					if (freq_dict.containsKey(w) && freq_dict.get(w) >= min_count) {
						text_word.add(w);
					}
				}
			}
			String pure_text = StringUtils.join(text_word, " ").trim();
			if (pure_text.equals("") == false) {
				bufferedWriter_title.write("e_"+lang+"_" + words[0] + "\n");
				bufferedWriter_text.write(pure_text + "\n");
				for (String k : link_dict.keySet()) {
	            	bufferedWriter_linkage.write("e_"+lang+"_"+words[0] + "\t" + k + "\t" + link_dict.get(k) + "\tl" + "\n");
	            }
			}
		}
		bufferedReader.close();
		bufferedWriter_text.close();
		bufferedWriter_title.close();
		bufferedWriter_linkage.close();
	}
	public void getNewText(String pages, String lang) throws Exception {
		BufferedReader bufferedReader_pages = new BufferedReader(new FileReader(new File(pages)));
		BufferedWriter bufferedWriter_text = new BufferedWriter(new FileWriter(new File(lang + "wiki.text")));
		
		String line = null;
		Set<String> stopwords = new HashSet<String>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("./"+ lang +"_stopwords.txt")));
		
		while (null != (line = bufferedReader.readLine())) {
			stopwords.add(line.trim());
		}

		bufferedReader.close();
		englishStemmer stemmer = new englishStemmer();
		String[] filter_words = {"wikipedia:", "wikiprojects", "lists", "mediawiki", "template:", "user:", "portal:", "category:", "categories:", "file:", "help:", "image:", "module:", "articles", "extension:", "manual:"};

		if (lang.equals("en")) {
			line = null;
			while (null != (line = bufferedReader_pages.readLine())) {
				JSONObject page = new JSONObject(line);
				String title = page.getString("title").toLowerCase();
				String article = page.getString("article").toLowerCase();

				boolean contains = false;
				for (String w : filter_words) { if (title.contains(w.toLowerCase())) { contains = true; break;	}	}
				if (contains) { continue;	}
				
				List<String> links = new ArrayList<String>();
				Matcher matcher = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(article);
				while (matcher.find()) {
					links.add(matcher.group(0));
				}

				List<String> entity_links = new ArrayList<String>();
				for (String el : links) {
					String n_el;
					if (el.contains("|")) {
						n_el = "e_en_"+el.substring(2, el.indexOf("|")).replace(" ", "_");
					} else {
						n_el = "e_en_"+el.substring(2, el.length()-2).replace(" ", "_");
					}
					article = article.replace(el, "[__]"+n_el+"[__]");
					entity_links.add(n_el);
				}

				String tmp_spl[] = article.split("\\[__\\]");
				List<String> words2article = new ArrayList<String>();
				for (String s : tmp_spl) {
					if (s.startsWith("e_en_")) {
						words2article.add(s);
					} else {
						String tmp_s = s.replaceAll("[^_a-zA-Z0-9\u4e00-\u9fa5]+", " ").replaceAll("\\s+", " ").trim();
						if (tmp_s.length() >= 2) {
							String[] words = tmp_s.split(" ");
							for (String word : words) {
								if (stopwords.contains(word) || word.length() < 2 || !StringUtils.isAlphaSpace(word)) {
									continue;
								} else {
									if (word.equals(title)) {
										words2article.add("e_en_"+word);
									} else {
										stemmer.setCurrent(word);
										if (stemmer.stem()==false){ continue;	}
										words2article.add("w_en_"+stemmer.getCurrent());
									}
								}
							}
						}
					}
				}
				
				article = StringUtils.join(words2article, " ").replaceAll("\\s+", " ").trim();
				if (article.equals("")==false) {
					bufferedWriter_text.write(title.replace(" ", "_") + "\t\t" + article+"|||"+StringUtils.join(entity_links, " ") + "\n");
				}
			}
		} else {
			Segment segment = HanLP.newSegment();
			segment.enableNameRecognize(true);
			segment.enableOrganizationRecognize(true);
			segment.enablePlaceRecognize(true);
			segment.enableTranslatedNameRecognize(true);
			line = null;
				
			while (null != (line = bufferedReader_pages.readLine())) {
				JSONObject page = new JSONObject(line);
				String title = page.getString("title").toLowerCase();
				String article = page.getString("article").toLowerCase();
				
				boolean contains = false;
				for (String w : filter_words) { if (title.contains(w.toLowerCase())) { contains = true; break;	}	}
				if (contains) { continue;	}


				List<String> links = new ArrayList<String>();
				Matcher matcher = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(article);
				while (matcher.find()) {
					links.add(matcher.group(0));
				}

				List<String> entity_links = new ArrayList<String>();
				for (String el : links) {
					String n_el;
					if (el.contains("|")) {
						n_el = "e_zh_"+el.substring(2, el.indexOf("|")).replace(" ", "_");
					} else {
						n_el = "e_zh_"+el.substring(2, el.length()-2).replace(" ", "_");
					}
					article = article.replace(el, "[__]"+n_el+"[__]");
					entity_links.add(n_el);
				}
				
				String tmp_spl[] = article.split("\\[__\\]");
				List<String> words2article = new ArrayList<String>();
				for (String s : tmp_spl) {
					if (s.startsWith("e_zh_")) {
						words2article.add(s);
					} else {
						String tmp_s = s.replaceAll("[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]", "").replaceAll("\\d+" , "").replaceAll("[a-zA-Z]+" , "").replaceAll("\\s+", "").trim();
						if (tmp_s.length() >= 2) {
							List<Term> words = segment.seg(tmp_s);
							for (Term word : words) {
								if (stopwords.contains(word.word) || word.word.length() < 2) {
									continue;
								} else {
									if (word.word.equals(title)) {
										words2article.add("e_zh_"+word.word);
									} else {
										words2article.add("w_zh_"+word.word);
									}
								}
							}
						}
					}
				}
					
				article = StringUtils.join(words2article, " ").replaceAll("\\s+", " ").trim();
				if (article.equals("")==false) {
					bufferedWriter_text.write(title.replace(" ", "_") + "\t\t" + article+"|||"+StringUtils.join(entity_links, " ") + "\n");
				}
			}
		}
		bufferedReader_pages.close();
		bufferedWriter_text.close();
	}
	
	
	
	public void getText(String pages, String lang) throws Exception {
		BufferedReader bufferedReader_pages = new BufferedReader(new FileReader(new File(pages)));
		BufferedWriter bufferedWriter_text = new BufferedWriter(new FileWriter(new File(lang + "wiki.text")));
		String line = null;
		Set<String> stopwords = new HashSet<String>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("./"+ lang +"_stopwords.txt")));
        while (null != (line = bufferedReader.readLine())) {
        	stopwords.add(line.trim());
        }
        bufferedReader.close();
        englishStemmer stemmer = new englishStemmer();
        String[] filter_words = {"Wikipedia:", "Category:", "Template:", "Portal:", "WikiProjects", "File:", "User:", "Help:", "Image:", "Module:",
        		"list", "mediawiki", "categories", " articles"};
        
		if (lang.equals("en")) {	        
	        line = null;
	        while (null != (line = bufferedReader_pages.readLine())) {
	        	JSONObject page = new JSONObject(line);
	        	String title = page.getString("title");
	        	String article = page.getString("article");
	    		
	        	boolean contains = false;
	        	for (String w : filter_words) {
	            	if (title.contains(w.toLowerCase())) {
	            		contains = true;
	            		break;
	            	}
	            }
	            if (contains) {
	            	continue;
	            }
	            
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
	    		
		        article = StringUtils.join(words2article, " ").replaceAll("\\s+", " ").trim();
		        if (article.equals("")==false) {
		        	bufferedWriter_text.write(title.replace(" ", "_") + "\t\t" + article+"|||"+StringUtils.join(entity_links, " ") + "\n");
		        }
        	}
		} else {
			Segment segment = HanLP.newSegment();
			segment.enableNameRecognize(true);
			segment.enableOrganizationRecognize(true);
			segment.enablePlaceRecognize(true);
			segment.enableTranslatedNameRecognize(true);
			line = null;
			
			while (null != (line = bufferedReader_pages.readLine())) {
	        	JSONObject page = new JSONObject(line);
	        	String title = page.getString("title");
	        	String article = page.getString("article");
	    		
	        	boolean contains = false;
	        	for (String w : filter_words) {
	            	if (title.contains(w.toLowerCase())) {
	            		contains = true;
	            		break;
	            	}
	            }
	            if (contains) {
	            	continue;
	            }
	            
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
	    		
	    		article = article.replaceAll( "[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]" , "").replaceAll( "\\d+" , " ").replaceAll( "[a-zA-Z]+" , " ").replaceAll("\\s+", " ").trim();
	    		List<Term> words = segment.seg(article);
	    		List<String> words2article = new ArrayList<String>();
	    		for (Term word : words) {
	    			if (StringUtils.isAlphaSpace(word.word)) {
	    				if (stopwords.contains(word.word) || word.word.length() < 2) {
	    					continue;
	    				}
	    				stemmer.setCurrent(word.word);
	    				if (stemmer.stem()==false){
	    					continue;
	    				}
	    				words2article.add(stemmer.getCurrent());
	    			} else {
	    				words2article.add(word.word);
	    			}
	    		}
	    		
		        article = StringUtils.join(words2article, " ").replaceAll("\\s+", " ").trim();
		        if (article.equals("")==false) {
		        	bufferedWriter_text.write(title.replace(" ", "_") + "\t\t" + article+"|||"+StringUtils.join(entity_links, " ") + "\n");
		        }
			}
		}
        bufferedReader_pages.close();
        bufferedWriter_text.close();
	}
	
	public void duplicateEntitiesRemove(String page_loc, String suffix) throws Exception {
		String[] type = {"en", "zh"};
//		String[] type = {"zh"};
		for (String t : type) {
			BufferedReader bufferedReader_pages = new BufferedReader(new FileReader(new File(page_loc + t + suffix)));
			Set<String> title_set = new HashSet<String>();
			String line = null;
	        while (null != (line = bufferedReader_pages.readLine())) {
	        	JSONObject page = new JSONObject(line);
	        	if (t.equals("zh")) {
	        		title_set.add(HanLP.convertToSimplifiedChinese(page.getString("title")).toLowerCase());
	        	} else {
	        		title_set.add(page.getString("title").toLowerCase());	        		
	        	}
	        }
	        System.out.println(t + " size: " + title_set.size());
	        bufferedReader_pages = new BufferedReader(new FileReader(new File(page_loc + t + suffix)));
	        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(t + "_pages.json")));
	        BufferedWriter bufferedWriter_id_title = new BufferedWriter(new FileWriter(new File(t + "_id_title.txt")));
			line = null;
			int cnt = 0;
	        while (null != (line = bufferedReader_pages.readLine())) {
	        	JSONObject page = new JSONObject(line);
	        	if (t.equals("zh")) {
	        		page.put("article", HanLP.convertToSimplifiedChinese(page.getString("article")).toLowerCase());
		        	page.put("title", HanLP.convertToSimplifiedChinese(page.getString("title")).toLowerCase());
	        	} else {
	        		page.put("article", page.getString("article").toLowerCase());
		        	page.put("title", page.getString("title").toLowerCase());
	        	}
	        	cnt += 1;
	        	if (cnt%1000000==0) {
	        		System.out.println("__" + cnt);
	        	}
	        	
	        	if (title_set.contains(page.getString("title"))) {
	        		bufferedWriter.write(page.toString() + "\n");
	        		bufferedWriter_id_title.write(page.getString("id") + "\t\t" + page.getString("title") + "\n");
	        		title_set.remove(page.getString("title"));
	        	}
	        }
	        bufferedWriter.close();
	        bufferedWriter_id_title.close();
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
        BufferedWriter bufferedWriter_en = new BufferedWriter(new FileWriter(new File("en_cl_page.json")));
        BufferedWriter bufferedWriter_zh = new BufferedWriter(new FileWriter(new File("zh_cl_page.json")));
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
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(en_pages.replace(".json", ".cl.json"))));
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
        bufferedWriter = new BufferedWriter(new FileWriter(new File(zh_pages.replace(".json", ".cl.json"))));
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
            	bufferedWriter.write(line.toLowerCase() + "\n");
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
