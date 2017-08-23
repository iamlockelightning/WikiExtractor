package zhwikiExtractor;

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

public class ZhwikiExtractor {

	public static void main(String args[]) throws Exception {
		ZhwikiExtractor ze = new ZhwikiExtractor();
		Date start_date = new Date();
		System.out.println("Extraction starts at:" + start_date);
		ze.test("/Users/locke/Downloads/a.txt");
		Date end_date = new Date();
		double cost = (double)(end_date.getTime()-start_date.getTime())/1000.0/60.0;
		System.out.println("Extraction ents at: " + end_date + "\tcost: " + cost + "min");
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
            	JSONObject infobox = new JSONObject();
            	JSONArray links = new JSONArray();
            	Elements doc = Jsoup.parse(rawXml, "UTF-8").getElementsByTag("page");
            	Element title = doc.get(0).getElementsByTag("title").get(0);
            	Elements redirect = doc.get(0).getElementsByTag("redirect");
            	Element text = doc.get(0).getElementsByTag("text").get(0);
            	if (redirect.size() == 0) {
            		String article = text.toString();
            		article = article.replaceAll("<text(.*?)>", "").replaceAll("</text>", "");
            		article = article.replaceAll("\\{\\{(.*?)\\}\\}", "").replaceAll("\\&lt;(.*?)\\&gt;", "");
            		article = article.replaceAll("\\[\\[Category(.*?)\\]\\]", "");
            		article = article.replaceAll("===(.*?)===", "").replaceAll("==(.*?)==", "");
            		article = article.replaceAll("\\[\\[File(.*?)\\]\\]", "");
            		article = article.replaceAll("\\[https://(.*?)\\]", "");
            		article = article.replaceAll("\n", "").replaceAll("[ ]+", " ").trim();
//            		System.out.println(article);
            		
            		if (infobox_list.size() < infobox_list_sub.size()) {
            			infobox_list = infobox_list_sub;
            		}
            		String infotext = StringUtils.join(infobox_list, "\n");
            		List<String> temp_list = new ArrayList<String>();
            		for (String s : infotext.split("\n")) {
            			s = s.replaceAll("\\{\\{(.*?)\\}\\}", "").replaceAll("\\&lt;(.*?)\\&gt;", "").trim();
//            			System.out.println(s);
            			if (s.startsWith("|")) {
            				temp_list.add(s);
            			} else if (s.startsWith("*")) {
            				temp_list.add(temp_list.remove(temp_list.size()-1) + " " + s);
            			}
            		}
            		for (String s : temp_list) {
        				String[] ss = s.substring(1).split("=");
        				if (ss.length > 1 && ss[1].trim().equals("")==false) {
        					infobox.put(ss[0].trim(), ss[1].trim());
        				}
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
//            		System.out.println(page.toString());
            		bufferedWriter.write(page.toString() + "\n");
//        			break;
            	} else {
            		// redirect page
            		String res_page_title = title.text().trim();
            		String tar_page_title = redirect.get(0).attr("title").trim();
            		if (res_page_title.equals("")==false && tar_page_title.equals("")==false) {
            			bufferedWriterRedirect.write(res_page_title + "\t\t" + tar_page_title + "\n");
            		}
//            		break;
            	}
            	cnt += 1;
            	if (cnt % 500000 == 0) {
            		System.out.println("__" + cnt);
            	}
//            	break;
            } else {
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
