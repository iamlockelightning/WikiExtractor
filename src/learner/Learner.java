package learner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hankcs.hanlp.dependency.nnparser.Instance;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Debug.Random;
import weka.core.Instances;

public class Learner {
	
	public static void main(String args[]) throws Exception {
		Learner learner = new Learner();
		learner.testFilter("../PTEforHNE/workspace/ww.word.emb", "./cl.test.net");
//		learner.genCrossValidationFolds();
	}
	
	public void testFilter(String emb_file, String cl_test_file) throws Exception {
		int DIM = -1;
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
		
		List<String> hs = new ArrayList<String>(), es = new ArrayList<String>();
		BufferedReader bufferedReader_cl = new BufferedReader(new FileReader(new File(cl_test_file)));
		line = null;
		while (null != (line = bufferedReader_cl.readLine())) {            
			String[] words = line.split("\t");
			if (emb_dict.containsKey(words[0]) && emb_dict.containsKey(words[1])) {
				hs.add(words[0]);
				es.add(words[1]);
			}
        }
		bufferedReader_cl.close();
		System.out.println("hs size:" + hs.size() + "\tes size:" + es.size());
		
		/*
		
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("cl.test.weka")));
		StringBuilder header = new StringBuilder("@RELATION CLs\n");
		for (int i = 0; i < DIM; i++) {
			header.append("@ATTRIBUTE attr" + i + " NUMERIC\n");
		}
		header.append("@ATTRIBUTE class NUMERIC\n");
		header.append("@DATA\n");
		bufferedWriter.write(header.toString());
		
		for (int i = 0; i < hs.size(); i += 1) {
			for (int j = 0; j < es.size(); j += 1) {
				if (i==j) {
					continue;
				}
				StringBuilder data_line = new StringBuilder("");
				for (int ii = 0; ii < DIM; i += 1) {
					data_line.append((Math.abs(emb_dict.get(hs.get(i)).get(ii) - emb_dict.get(es.get(i)).get(ii)) - 
							Math.abs(emb_dict.get(hs.get(i)).get(ii) - emb_dict.get(es.get(j)).get(ii))) + ",");
				}
				bufferedWriter.write(data_line.toString() + (1-Math.random()) + "\n"); // "1\n"
			}
		}
		bufferedWriter.close();
		*/
	}
	
	public void genCrossValidationFolds(String weka_input, int fold_num) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(new File(weka_input)));
		Instances all_instance = new Instances(in);
		all_instance.setClassIndex(all_instance.numAttributes()-1);
		all_instance.randomize(new Random()); 
		for (int n = 0; n < fold_num; n++) {
			Instances train = all_instance.trainCV(fold_num, n);
			Instances test = all_instance.testCV(fold_num, n);
			
//			double[] coeffs = new double[DIM + 1];
//			for (int ii = 0; ii < coeffs.length; ii++) {
//				coeffs[ii] = 1.0;
//			}
			
			LinearRegression classifier = new LinearRegression();
			String[] options = {""};
			classifier.setOptions(options);
			classifier.buildClassifier(train);
//			coeffs = classifier.coefficients();
			
			
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
