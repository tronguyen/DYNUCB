package com.smu.linucb.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalFunction;
import com.smu.linucb.pca.PrincipleComponentAnalysis;

public class Preprocessing {

	private static final String ACCUMULATED_TFIDF = "xxxSUMxxx";
	private static final Map<String, Double> bookmark_tfidf = new HashMap<String, Double>();

	public void readFile(File filename) {
		String line = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sensorToken() {
		String key;
		Set<String> ks = new HashSet(Environment.hm_token_weight.keySet());
		for (Iterator<String> i = ks.iterator(); i.hasNext();) {
			key = i.next();
			if (Environment.hm_token_weight.get(key) < 10) {
				Environment.hm_token_weight.remove(key);
			}
		}
	}

	// split tag values
	protected Set<String> isSplit(String text) {
		Set<String> chk = new HashSet<String>();
		String patternString = "\\w+[[\\_|\\-|\\^|\\/]\\w+]+";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);

		// split tokens
		if (matcher.find()) {
			String[] items = text.split("\\_|\\-|\\^|\\/");
			chk.addAll(Arrays.asList(items));
		} else {
			chk.add(text);
		}
		return chk;
	}

	public void buildTags(ResultSet resultSet) throws SQLException {
		String line = "";
		Set<String> setToken;
		int tagWeight;
		String string;
		while (resultSet.next()) {
			line = resultSet.getString("value");
			tagWeight = resultSet.getInt("tagWeight");
			setToken = isSplit(line.trim());
			for (Iterator<String> i = setToken.iterator(); i.hasNext();) {
				string = i.next();
				addTags(Environment.hm_token_weight, string, tagWeight);
			}
		}
	}

	public void buildBookmark_Tags(ResultSet rs) throws SQLException {
		String line = "";
		int tagWeight;
		int bmID;
		Set<String> setToken;
		String string;
		boolean noTag = false;
		while (rs.next()) {
			bmID = rs.getInt("bookmarkID");
			line = rs.getString("value");
			tagWeight = rs.getInt("tagWeight");
			setToken = isSplit(line.trim());
			for (Iterator<String> i = setToken.iterator(); i.hasNext();) {
				string = i.next();
				// Satisfy num tags > 10 units
				if (Environment.hm_token_weight.containsKey(string)) {
					addBookmarkTags(bmID, string, tagWeight);
					idfFunc(string, bmID);
					noTag = true;
				}
			}
			if (!Environment.hm_bookmark_tag.containsKey(bmID)) {
				// Add bookmark having tags with their weights under 10
				Environment.removedBM.add(bmID);
			}
		}
	}

	public void buildUserList(ResultSet rs) throws SQLException {
		int usr;
		while (rs.next()) {
			usr = rs.getInt("userID");
			Environment.userLst.add(usr);
		}
	}

	private void idfFunc(String txt, int bookmark) {
		Set<Integer> setBM;
		if (Environment.hs_df.containsKey(txt)) {
			setBM = Environment.hs_df.get(txt);
		} else {
			setBM = new HashSet<Integer>();
			Environment.hs_df.put(txt, setBM);
		}
		setBM.add(bookmark);
	}

	public void buildTagChecking(ResultSet resultSet) throws SQLException {
		String line = "";
		Set<String> setToken;
		String string;
		while (resultSet.next()) {
			line = resultSet.getString("value");
			setToken = isSplit(line.trim());
			for (Iterator<String> i = setToken.iterator(); i.hasNext();) {
				string = i.next();
				Environment.tagSet.add(string);
			}
		}
	}

	protected static void addTags(Map<String, Double> obj, String txt, double d) {
		if (txt.equals("") | txt.matches("^\\?+$")) {
			return;
		}
		if (obj.containsKey(txt)) {
			obj.put(txt, obj.get(txt) + d);
		} else {
			obj.put(txt, d);
		}
	}

	private void addBookmarkTags(int bookmark, String item, double count) {
		Map<String, Double> m;
		if (Environment.hm_bookmark_tag.containsKey(bookmark)) {
			m = Environment.hm_bookmark_tag.get(bookmark);
			addTags(m, item, count);
		} else {
			m = new HashMap<String, Double>();
			m.put(item, (double) count);
			Environment.hm_bookmark_tag.put(bookmark, m);
		}
	}

	private static double getTf_Idf(double tf, int df) {
		return tf
				* Math.log((double) Environment.hm_bookmark_tag.keySet().size()
						/ (1 + df));
	}

	public void calTF_IDF() {
		// change value of hm_bookmark_tags
		Set<Integer> bm = Environment.hm_bookmark_tag.keySet();
		Map<String, Double> hmTag;
		Set<String> tag;
		String tgItem;
		Integer bmid;
		double val_TFIDF;
		for (Iterator<Integer> i = bm.iterator(); i.hasNext();) {
			bmid = i.next();
			hmTag = Environment.hm_bookmark_tag.get(bmid);
			tag = hmTag.keySet();
			for (Iterator<String> j = tag.iterator(); j.hasNext();) {
				tgItem = j.next();
				val_TFIDF = getTf_Idf(hmTag.get(tgItem),
						Environment.hs_df.get(tgItem).size());
				hmTag.put(tgItem, val_TFIDF);
				addTags(bookmark_tfidf, String.valueOf(bmid),
						Math.pow(val_TFIDF, 2));
			}
		}
	}

	protected static double normalizeTF_IDF(double numerator, double denominator) {
		return numerator / Math.sqrt(denominator);
	}

	public static void writeNormMatrix(PrincipleComponentAnalysis pca,
			File mxIN, File mxOUT) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(mxIN));
		String s = "";
		String[] arrStr;
		double[] nums = null;
		BufferedWriter bw = new BufferedWriter(new FileWriter(mxOUT));
		while ((s = br.readLine()) != null) {
			arrStr = s.split("\t");
			nums = new double[arrStr.length - 1];
			for (int i = 0; i < nums.length; i++) {
				nums[i] = Double.parseDouble(arrStr[i + 1]);
			}
			// Write to file
			bw.write(arrStr[0] + ",");
			bw.write(Arrays.toString(pca.sampleToEigenSpace(nums)) + "\n");
			bw.flush();
		}
		bw.close();
		br.close();
	}

	public void writeBookmark_Tags(File f) {
		List<String> tags = new ArrayList<String>();
		tags.addAll(Environment.hm_token_weight.keySet());
		Set<Integer> bm = Environment.hm_bookmark_tag.keySet();
		Map<String, Double> hmTag;
		System.out.println(tags.size());
		BufferedWriter bw = null;
		Integer bmid;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			for (Iterator<Integer> i = bm.iterator(); i.hasNext();) {
				// c++;
				bmid = i.next();
				bw.write(String.valueOf(bmid));
				// System.out.print(bmid);
				hmTag = Environment.hm_bookmark_tag.get(bmid);
				for (String tg : tags) {
					// Write matrix to file
					// 0: for tags which do not appear in hmTag
					// norm_tfidf: otherwise
					if (hmTag.containsKey(tg)) {
						bw.write("\t"
								+ normalizeTF_IDF(hmTag.get(tg), bookmark_tfidf
										.get(String.valueOf(bmid))));
						// System.out.print("\t" +
						// normalizeTF_IDF(hmTag.get(tg),
						// bookmark_tfidf.get(String.valueOf(bmid))));
					} else {
						bw.write("\t" + 0);
						// System.out.print("\t" + 0);
					}
				}
				bw.write("\n");
				// System.out.print("\n");
				// if(c==500){
				// break;
				// }
			}

			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeBookmark_Tags_ARFF(File f) {
		List<String> tags = new ArrayList<String>();
		tags.addAll(Environment.hm_token_weight.keySet());
		Set<Integer> bm = Environment.hm_bookmark_tag.keySet();

		List<Integer> bms = new ArrayList<Integer>();
		bms.addAll(bm);

		Map<String, Double> hmTag;
		System.out.println(tags.size());
		BufferedWriter bw = null;
		StringBuilder sb = new StringBuilder();
		Integer bmid;
		int count = 0;
		try {
			bw = new BufferedWriter(new FileWriter(f));

			// Write @header arff file
			bw.write("@RELATION " + "matrix_bm_tags\n");
			for (String t : tags) {
				bw.write("@ATTRIBUTE " + t + " NUMERIC\n");
			}

			bw.write("@DATA\n");
			for (Iterator<Integer> i = bm.iterator(); i.hasNext();) {
				count = 0;
				bmid = i.next();
				bw.write("{");
				hmTag = Environment.hm_bookmark_tag.get(bmid);
				sb.delete(0, sb.length());
				for (String tg : tags) {
					// Write matrix to file
					// 0: for tags which do not appear in hmTag
					// norm_tfidf: otherwise
					if (hmTag.containsKey(tg)) {
						sb.append(","
								+ count
								+ " "
								+ normalizeTF_IDF(hmTag.get(tg), bookmark_tfidf
										.get(String.valueOf(bmid))));
					}
					count++;
				}
				bw.write(sb.substring(1));
				bw.write("}\n");
			}
			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeBookmark_Tags_Matlab(File f, File f_bm) {
		List<String> tags = new ArrayList<String>();
		tags.addAll(Environment.hm_token_weight.keySet());
		Set<Integer> bm = Environment.hm_bookmark_tag.keySet();

		Map<String, Double> hmTag;
		System.out.println(tags.size());
		BufferedWriter bw = null;
		BufferedWriter bmindex = null;
		Integer bmid;
		int count = 1, line = 1;
		int c = 0;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			bmindex = new BufferedWriter(new FileWriter(f_bm));
			for (Iterator<Integer> i = bm.iterator(); i.hasNext();) {
				bmid = i.next();
				count = 1;
				c++;
				hmTag = Environment.hm_bookmark_tag.get(bmid);

				// Write bookmark index to file
				bmindex.write(bmid + "\n");

				for (String tg : tags) {
					// Write matrix to file
					// 0: for tags which do not appear in hmTag
					// norm_tfidf: otherwise
					if (hmTag.containsKey(tg)) {
						bw.write(String.valueOf(line));
						bw.write(" "
								+ count
								+ " "
								+ normalizeTF_IDF(hmTag.get(tg), bookmark_tfidf
										.get(String.valueOf(bmid))) + "\n");
					}
					count++;
				}
				line++;
				bw.flush();
				if (c == 1000) {
					break;
				}
			}
			bw.close();
			bmindex.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void buildContactRelation(File f) {
		BufferedReader br = null;
		String l = "";
		String[] s;
		try {
			br = new BufferedReader(new FileReader(f));
			br.readLine();
			while ((l = br.readLine()) != null) {
				s = l.split("\t");
				GlobalFunction.addValueMap(Environment.usrRelationMap,
						Integer.valueOf(s[0]), Integer.valueOf(s[1]));
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// Preprocessing pr = new Preprocessing();
		// pr.readFile(new File(
		// "Data/Delicious/tags.dat"));

		// Link to matlab
		// MatlabProxyFactory factory = new MatlabProxyFactory();
		// MatlabProxy proxy = factory.getProxy();
		//
		// proxy.eval("disp('hello world!')");
		// proxy.setVariable("A", Environment.bookmark_tag);
		// proxy.disconnect();

		// Using PCA_weka
		// File fin = new File("matrix_arff.arff");
		// File fout = new File("matrix_arff_out.arff");
		// PCA_weka.reduceDimension(fin, fout);

	}
}
