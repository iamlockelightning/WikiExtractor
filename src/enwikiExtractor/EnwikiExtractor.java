package enwikiExtractor;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class EnwikiExtractor {

	public static void main(String args[]) throws Exception {
		EnwikiExtractor ee = new EnwikiExtractor();
		Date start_date = new Date();
		System.out.println("Extraction starts at:" + start_date);
		
//		ee.test("/Users/locke/Downloads/a.txt");
//		ee.test("/home/lcj/enwiki-latest-pages-articles-multistream.xml");
		
		ee.getIdWithTitle("/home/lcj/enwiki-latest-pages-articles-multistream.result.xml");
		ee.getIdWithTitle("/home/lcj/zhwiki-latest-pages-articles-multistream.result.xml");
		
		Date end_date = new Date();
		double cost = (double)(end_date.getTime()-start_date.getTime())/1000.0/60.0;
		System.out.println("Extraction ents at: " + end_date + "\tcost: " + cost + "min");
	}
	
	public void getIdWithTitle(String filename) throws Exception {
		BufferedReader bufferedReaderRaw = new BufferedReader(new FileReader(new File(filename)));
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(filename.replace(".", ".id_title."))));
		String line = new String();

        while (true) {
            line = bufferedReaderRaw.readLine();
            if (line == null) {
            	break;
            }
            JSONObject page = new JSONObject(line);
            bufferedWriter.write(page.get("id") + "\t\t" + page.get("title") + "\n");
        }
        bufferedReaderRaw.close();
        bufferedWriter.close();
	}
	
	public JSONObject getInfobox(List<String> infobox_list) {
		JSONObject infobox = new JSONObject();
		String infotext = StringUtils.join(infobox_list, "\n").replaceAll("\\[\\[File:(.*?)((\\[\\[(.*?)\\]\\])(.*?))*\\]\\]", "").replaceAll("\\[https://(.*?)\\]", "").replaceAll("\\[http://(.*?)\\]", "");
		List<String> temp_list = new ArrayList<String>();
		for (String s : infotext.split("\n")) {
//			s = s.replaceAll("\\{\\{(.*?)(\\{\\{(.*?)\\}\\}(.*?))*\\}\\}", "");
			s = s.replaceAll("\\{\\{(.*?)\\}\\}", "");
			s = s.replaceAll("\\&lt;(.*?)\\&gt;", "").trim();
//			System.out.println(s);
			if (s.startsWith("|")) {
				temp_list.add(s);
			} else if (s.startsWith("*")) {
				if (temp_list.size() != 0) {
					temp_list.add(temp_list.remove(temp_list.size()-1) + " " + s);
				}
			}
		}
		for (String s : temp_list) {
			String[] ss = s.substring(1).split("=");
			if (ss.length > 1 && ss[1].trim().equals("")==false) {
				infobox.put(ss[0].trim(), ss[1].trim());
			}
		}
		return infobox;
	}
	
	public void test(String filename) throws Exception {
    	System.out.println("Processing file: " + filename);
        BufferedReader bufferedReaderRaw = new BufferedReader(new FileReader(new File(filename)));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(filename.replace(".", ".result."))));
        BufferedWriter bufferedWriterRedirect = new BufferedWriter(new FileWriter(new File(filename.replace(".", ".redirect."))));

        String line = new String();
        List<String> list = new ArrayList<String>();
        List<String> infobox_list = new ArrayList<String>();
        List<String> infobox_list_sub = new ArrayList<String>();
        
        int cnt = 0;
        while (true) {
            line = bufferedReaderRaw.readLine();
            if (line == null) {
            	break;
            }
            if (line.trim().equals("<page>")) {
            	list.clear();
            	infobox_list.clear();
            	infobox_list_sub.clear();
            	list.add(line);
            } else if (line.trim().equals("</page>")) {
            	list.add(line);
            	String rawXml = StringUtils.join(list, "\n");
            	JSONObject page = new JSONObject();
            	JSONArray links = new JSONArray();
            	Elements doc = Jsoup.parse(rawXml, "UTF-8").getElementsByTag("page");
            	Element title = doc.get(0).getElementsByTag("title").get(0);
            	Elements redirect = doc.get(0).getElementsByTag("redirect");
            	Element text = doc.get(0).getElementsByTag("text").get(0);
            	Element id = doc.get(0).getElementsByTag("id").get(0);
            	if (redirect.size() == 0) {
            		String article = text.toString();
            		article = article.replaceAll("<text(.*?)>", "").replaceAll("</text>", "");
            		article = article.replaceAll("\\&amp;amp;lt;(.*?)\\&amp;amp;gt;", "").replaceAll("\\&amp;lt;(.*?)\\&amp;gt;", "").replaceAll("\\&lt;(.*?)\\&gt;", ""); // new added
//	            	article = article.replaceAll("\\{\\{(.*?)(\\{\\{(.*?)\\}\\}(.*?))*\\}\\}", "");
            		article = article.replaceAll("\\{\\{(.*?)\\}\\}", "");
            		article = article.replaceAll("\\&nbsp;", " ").replaceAll("nbsp;", " ").replaceAll("\\&amp;", ""); // new added
            		article = article.replaceAll("\\[\\[Category(.*?)\\]\\]", "");
            		article = article.replaceAll("===(.*?)===", "").replaceAll("==(.*?)==", "");
            		article = article.replaceAll("\\[\\[User:(.*?)\\]\\]", "").replaceAll("\\[\\[:File:(.*?)\\]\\]", ""); // new added
//            		article = article.replaceAll("\\[\\[File:(.*?)((\\[\\[(.*?)\\]\\])(.*?))*\\]\\]", "");
            		article = article.replaceAll("\\[\\[File:(.*?)\\]\\]", "");  // edit n
            		article = article.replaceAll("\\[https://(.*?)\\]", "").replaceAll("\\[http://(.*?)\\]", "");
            		article = article.replaceAll("\n", "").replaceAll("[ ]+", " ").trim();
//            		System.out.println(article);
            		
            		JSONObject infobox = getInfobox(infobox_list);
            		JSONObject infobox_sub = getInfobox(infobox_list_sub);
            		
            		if (infobox.length() < infobox_sub.length()) {
            			infobox = infobox_sub;
            		}
            		
//            		System.out.println(infobox);
            		
            		Matcher matcher = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(article + infobox.toString());
            		while (matcher.find()) {
            			links.put(matcher.group(0));
            		}
            		
            		page.put("title", title);
            		page.put("article", article);
            		page.put("links", links);
            		page.put("infobox", infobox);
            		page.put("id", id.text());
//            		System.out.println(page.toString());
            		bufferedWriter.write(page.toString() + "\n");
//        			break;
            	} else {
            		// redirect page
            		String res_page_title = title.text().trim();
            		String tar_page_title = redirect.get(0).attr("title").trim();
            		if (res_page_title.equals("")==false && tar_page_title.equals("")==false) {
            			bufferedWriterRedirect.write(id.text() + "\t\t" + res_page_title + "\t\t" + tar_page_title + "\n");
            		}
//            		break;
            	}
            	cnt += 1;
            	if (cnt % 500000 == 0) {
            		System.out.println("__" + cnt);
            	}
//            	break;
            } else {
            	if (line.trim().startsWith("<text xml:space=\"preserve\">{{Infobox")) {
            		String[] tem_s = line.split("\\{\\{");
            		list.add(tem_s[0]);
            		line = "{{" + tem_s[1];
            	}
            	if (line.trim().startsWith("{{Infobox")) {
            		if (infobox_list.isEmpty()) {
            			while (line.trim().equals("}}") == false) {
                			infobox_list.add(line);
                			line = bufferedReaderRaw.readLine();
                		}
                		infobox_list.add(line);
            		} else {
            			while (line.trim().equals("}}") == false) {
            				infobox_list_sub.add(line);
                			line = bufferedReaderRaw.readLine();
                		}
            			infobox_list_sub.add(line);
            		}
            	} else {
            		if (line.trim().startsWith("|")==false) {
            			list.add(line);
            		}
            	}
        	}
        }
        System.out.println(cnt);
        bufferedReaderRaw.close();
        bufferedWriter.close();
        bufferedWriterRedirect.close();
	}
}
