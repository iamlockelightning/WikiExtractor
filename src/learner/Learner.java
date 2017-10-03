package learner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.classifiers.functions.LinearRegression;
import weka.core.Debug.Random;
import weka.core.Instances;

public class Learner {
	
	public static void main(String args[]) throws Exception {
		Learner learner = new Learner();
		
//		learner.testFilter("../PTEforHNE/workspace/all.words.node", "./cl.test.net", 3000);
		
		learner.genCrossValidationFolds("./cl.test.3000.L", 5, "../PTEforHNE/workspace/ww.word.emb");
		
//		learner.trainTest("./3000fold5/", 5, "../PTEforHNE/workspace/ww.word.emb", 2);
	}
	
	public void testFilter(String all_words_node, String cl_test_file, int cl_test_num) throws Exception {
		BufferedReader bufferedReader_all = new BufferedReader(new FileReader(new File(all_words_node)));
		Set<String> en_set = new HashSet<String>();
		String line = null;
		while (null != (line = bufferedReader_all.readLine())) {
			if (line.startsWith("e_zh_") || line.startsWith("e_en_")) {
				en_set.add(line);
			}
        }
		bufferedReader_all.close();
		System.out.println("en_set.size():" + en_set.size());
		
		BufferedReader bufferedReader_cl = new BufferedReader(new FileReader(new File(cl_test_file)));
		List<String> cl_list = new ArrayList<String>();
		line = null;
		while (null != (line = bufferedReader_cl.readLine())) {
			String[] words = line.split("\t");
			if (en_set.contains(words[0]) && en_set.contains(words[1])) {
				cl_list.add(line);
			}
        }
		bufferedReader_cl.close();
		System.out.println("cl_list.size():" + cl_list.size());
		
		if (cl_list.size()>cl_test_num) {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("cl.test." + cl_test_num + ".L")));
			Collections.shuffle(cl_list);
			for (int i = 0; i < cl_test_num; i += 1) {
				bufferedWriter.write(cl_list.get(i) + "\n");
			}
			bufferedWriter.close();
		} else {
			System.out.println("___ cl_list.size() < cl_test_num");
		}
	}
	
	public void genCrossValidationFolds(String cl_test_n, int fold_num, String emb_file) throws Exception {
		BufferedReader bufferedReader_cl = new BufferedReader(new FileReader(new File(cl_test_n)));
		List<String> cls = new ArrayList<String>();
		String line = null;
		while (null != (line = bufferedReader_cl.readLine())) {
			cls.add(line);
        }
		bufferedReader_cl.close();
		
		Collections.shuffle(cls);
		List<String> hs = new ArrayList<String>(), es = new ArrayList<String>();
		for (String l : cls) {
			String[] words = l.split("\t");
			hs.add(words[0]);
			es.add(words[1]);
		}
		System.out.println("hs size:" + hs.size() + "\tes size:" + es.size());
		
		int DIM = -1;
		Map<String, List<Float>> emb_dict = new HashMap<String, List<Float>>();
		BufferedReader bufferedReader_emb = new BufferedReader(new FileReader(new File(emb_file)));
		line = bufferedReader_emb.readLine();
		while (null != (line = bufferedReader_emb.readLine())) {            
			String[] words = line.split(" ");
			if (words[0].startsWith("e_")) {
				List<Float> vec = new ArrayList<Float>();
				for (int i = 1; i < words.length; i += 1) {
					vec.add(Float.parseFloat(words[i]));
				}
				emb_dict.put(words[0], vec);
				DIM = emb_dict.get(words[0]).size();
			}
        }
		bufferedReader_emb.close();
		System.out.println("emb_dict size:" + emb_dict.size());
		
		StringBuilder header = new StringBuilder("@RELATION CLs_fold\n");
		for (int i = 0; i < DIM; i++) {
			header.append("@ATTRIBUTE attr" + i + " NUMERIC\n");
		}
		header.append("@ATTRIBUTE class NUMERIC\n");
		header.append("@DATA\n");

		int patch = cls.size() / fold_num;
		new File(cls.size()+"fold"+fold_num+"/").mkdirs();
		for (int n = 0; n < fold_num; n += 1) {
			BufferedWriter bufferedWriter_train = new BufferedWriter(new FileWriter(new File(cls.size()+"fold"+fold_num+"/cl.train.fold."+n+".weka")));
			BufferedWriter bufferedWriter_train_w = new BufferedWriter(new FileWriter(new File(cls.size()+"fold"+fold_num+"/cl.train.fold."+n+".word")));
			BufferedWriter bufferedWriter_test = new BufferedWriter(new FileWriter(new File(cls.size()+"fold"+fold_num+"/cl.test.fold."+n+".word")));
			
			//test fold
			for (int t = n*patch; t < (n+1)*patch; t += 1) {
				bufferedWriter_test.write(hs.get(t) + "\t" + es.get(t) + "\n");
			}
			bufferedWriter_test.close();
			
			//train fold
			bufferedWriter_train.write(header.toString() + "\n");
			for (int t = 0; t < cls.size(); t += 1) {
				if (t >= n*patch && t < (n+1)*patch) {
					continue;
				}
				bufferedWriter_train_w.write(hs.get(t) + "\t" + es.get(t) + "\n");
				for (int i = 0; i < hs.size(); i += 1) {
					if (i >= n*patch && i < (n+1)*patch) {
						continue;
					}
					for (int j = 0; j < es.size(); j += 1) {
						if (i==j || (j >= n*patch && j < (n+1)*patch)) {
							continue;
						}
						StringBuilder data_line = new StringBuilder("");
						for (int ii = 0; ii < DIM; ii += 1) {
							data_line.append((Math.abs(emb_dict.get(hs.get(i)).get(ii) - emb_dict.get(es.get(i)).get(ii)) - 
									Math.abs(emb_dict.get(hs.get(i)).get(ii) - emb_dict.get(es.get(j)).get(ii))) + ",");
						}
						bufferedWriter_train.write(data_line.toString() + (1-Math.random()) + "\n"); // "1\n"
					}
				}
			}
			bufferedWriter_train.close();
			bufferedWriter_train_w.close();
		}
	}
	
	public void trainTest(String file_dir, int fold_num, String emb_file, int neg_num) throws Exception {
		int DIM = -1;
		Random rand = new Random();
		Map<String, List<Float>> emb_dict = new HashMap<String, List<Float>>();
		BufferedReader bufferedReader_emb = new BufferedReader(new FileReader(new File(emb_file)));
		String line = bufferedReader_emb.readLine();
		while (null != (line = bufferedReader_emb.readLine())) {            
			String[] words = line.split(" ");
			if (words[0].startsWith("e_")) {
				List<Float> vec = new ArrayList<Float>();
				for (int i = 1; i < words.length; i += 1) {
					vec.add(Float.parseFloat(words[i]));
				}
				emb_dict.put(words[0], vec);
				DIM = emb_dict.get(words[0]).size();
			}
        }
		bufferedReader_emb.close();
		System.out.println("emb_dict size:" + emb_dict.size());
		
		for (int n = 0; n < fold_num; n++) {
			System.out.println("Train fold:" + n + ">>>");
			BufferedReader in = new BufferedReader(new FileReader(new File(file_dir+"cl.train.fold."+n+".weka")));
			Instances train_instances = new Instances(in);
			train_instances.setClassIndex(train_instances.numAttributes()-1);
			train_instances.randomize(new Random()); 
			in.close();
			
			double[] coeffs = new double[DIM + 1];
			for (int ii = 0; ii < coeffs.length; ii++) {
				coeffs[ii] = 1.0;
			}
			
			LinearRegression classifier = new LinearRegression();
			String[] options = {""};
			classifier.setOptions(options);
			classifier.buildClassifier(train_instances);
			coeffs = classifier.coefficients();
			
			List<String> train_hs = new ArrayList<String>(), train_es = new ArrayList<String>();
			in = new BufferedReader(new FileReader(new File(file_dir+"cl.train.fold."+n+".word")));
			line = null;
			while (null != (line = in.readLine())) {            
				String[] words = line.split("\t");
				train_hs.add(words[0]);
				train_es.add(words[1]);
	        }
			in.close();
			
			List<Double> pos_scores = new ArrayList<Double>(), neg_scores = new ArrayList<Double>();
			for (int i = 0; i < train_hs.size(); i += 1) {
				pos_scores.add(score(emb_dict.get(train_hs.get(i)), emb_dict.get(train_es.get(i)), coeffs));
				Set<Integer> neg_id = new HashSet<Integer>();
				for (int j = 0; j < neg_num; j += 1) {
					int k = rand.nextInt(train_es.size());
					if (k == i || neg_id.contains(k)) {
						j -= 1;
					} else {
						neg_id.add(k);
					}
				}
				for (Integer k : neg_id) {
					neg_scores.add(score(emb_dict.get(train_hs.get(i)), emb_dict.get(train_es.get(k)), coeffs));
				}
			}
			
			Double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
			Collections.sort(pos_scores);
			if (pos_scores.get(0) < min)
				min = pos_scores.get(0);
			if (pos_scores.get(pos_scores.size() - 1) > max)
				max = pos_scores.get(pos_scores.size() - 1);
			Collections.sort(neg_scores);
			if (neg_scores.get(0) < min)
				min = neg_scores.get(0);
			if (neg_scores.get(neg_scores.size() - 1) > max)
				max = neg_scores.get(neg_scores.size() - 1);

			Double threshold = min;
			Double best_f1 = 0.0;
			while (min <= max) {
				Double tmpThreshold = min;
				int tp = 0, pre = 0;
				for (int i = pos_scores.size() - 1; i >= 0; i--) {
					if (pos_scores.get(i) > tmpThreshold) {
						tp++;
						pre++;
					} else {
						break;
					}
				}
				for (int i = neg_scores.size() - 1; i >= 0; i--) {
					if (neg_scores.get(i) > tmpThreshold) {
						pre++;
					} else {
						break;
					}
				}
				Double local_f1 = 2.0 * tp / (pos_scores.size() + pre);
				if (local_f1 > best_f1) {
					threshold = tmpThreshold;
					best_f1 = local_f1;
				}
				min += 0.001;
			}
			System.out.println("Threshold:=" + threshold + ", F1-Score:=" + best_f1);
			
			
			System.out.println("Test fold:" + n + ">>>");
			List<String> test_hs = new ArrayList<String>(), test_es = new ArrayList<String>();
			in = new BufferedReader(new FileReader(new File(file_dir+"cl.test.fold."+n+".word")));
			line = null;
			while (null != (line = in.readLine())) {            
				String[] words = line.split("\t");
				test_hs.add(words[0]);
				test_es.add(words[1]);
	        }
			in.close();
			
			int tp = 0, pre = 0, rec = 0;
			for (int i = 0; i < test_hs.size(); i += 1) {
				rec += 1;
				if (score(emb_dict.get(test_hs.get(i)), emb_dict.get(test_es.get(i)), coeffs) > threshold) {
					tp += 1;
					pre += 1;
				}
				Set<Integer> neg_id = new HashSet<Integer>();
				for (int j = 0; j < neg_num; j += 1) {
					int k = rand.nextInt(test_es.size());
					if (k == i || neg_id.contains(k)) {
						j -= 1;
					} else {
						neg_id.add(k);
					}
				}
				for (Integer k : neg_id) {
					if (score(emb_dict.get(test_hs.get(i)), emb_dict.get(test_es.get(k)), coeffs) > threshold) {
						pre += 1;
					}
				}
			}
			
			System.out.println("Precision:=" + 1.0 * tp / pre
					+ ", Recall:=" + 1.0 * tp / rec
					+ ", F1-Score:=" + 2.0 * tp / (pre + rec));
		}
	}
	
	public static Double score(List<Float> vec1, List<Float> vec2, double[] coeffs) {
		Double result = 0.0;
		for (int i = 0; i < vec1.size(); i++) {
			result += coeffs[i] * Math.abs(vec1.get(i) - vec2.get(i));
		}
		return result;
	}		
}
