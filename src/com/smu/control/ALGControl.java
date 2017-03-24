package com.smu.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.jfree.ui.RefineryUtilities;

import com.smu.alg.view.DrawChart;
import com.smu.linucb.global.AlgorithmType;
import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalSQLQuery;
import com.smu.linucb.pca.PrincipleComponentAnalysis;
import com.smu.linucb.preprocessing.Dbconnection;
import com.smu.linucb.preprocessing.Preprocessing;
import com.smu.linucb.preprocessing.Preprocessing_lastfm;
import com.smu.linucb.verification.TreeFixedCluster;

public class ALGControl extends Thread {
	protected String fileAdd;
	protected static String fileAddCommon;

	/**
	 * @param args
	 */
	public static void readMatrix() {
		String line = "";
		String[] arrStr;
		int bmid;
		Double[] tagVals;
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					GlobalSQLQuery.fMatrix));
			while ((line = br.readLine()) != null) {
				arrStr = line.split(",");
				bmid = Integer.parseInt(arrStr[0]);
				tagVals = new Double[Environment.featureSize];
				for (int i = 0; i < arrStr.length - 1; i++) {
					tagVals[i] = Double.parseDouble(arrStr[i + 1]);
				}
				Environment.normMatrix.put(bmid, tagVals);
				Environment.bmidLst.add(bmid);
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

	public static void initData4Delicious(Preprocessing pr) {
		// Preprocessing pr = new Preprocessing();
		Dbconnection dbconn = Dbconnection._getConn();
		try {
			// Build tag set for counting
			pr.buildTags(dbconn.getResultSet(GlobalSQLQuery.GETVIEWTAG));
			pr.sensorToken();

			// Build tags for each bookmark
			pr.buildBookmark_Tags(dbconn
					.getResultSet(GlobalSQLQuery.GETBOOKMARK_TAG));

			// Build User Relations
			// pr.buildContactRelation(new File(
			// "Data/Delicious/user_contacts-timestamps.dat"));

			// Build user list
			// pr.buildUserList(dbconn.getResultSet(GlobalSQLQuery.GETUSER));

			// Print statistical data
			System.out.println("Size tags: "
					+ Environment.hm_token_weight.size());
			System.out.println("Size bookmark-tags: "
					+ Environment.hm_bookmark_tag.size());

			pr.buildTagChecking(dbconn.getResultSet(GlobalSQLQuery.GETTAG));
			System.out.println("Size true-tags: " + Environment.tagSet.size());

			// Calculate TF-IDF & normalization
			pr.calTF_IDF();

			// pr.writeBookmark_Tags(new File("Output4Stats/matrix"));
			// writeBookmark_Tags_ARFF(new File("matrix_arff.arff"));
			// writeBookmark_Tags_Matlab(new File("matrix_matlab_try_1"), new
			// File(
			// "bm_index_try_1"));
		} catch (SQLException sqlEx) {
			System.out.println("SQL exception...");
		}
	}

	public static void initData4LastFM(Preprocessing pr) {
		Dbconnection dbconn = Dbconnection._getConn();
		try {
			// Build tag set for counting
			pr.buildTags(dbconn.getResultSet(GlobalSQLQuery.GETVIEW_ARTIST_TAG));

			System.out.println("Tags: " + Environment.hm_tag_artistset.size());
			System.out.println("Artist: " + Environment.hm_artist_tag.size());
			// Running PCA
			// executePCAFM(pr);
			// Calculate TF-IDF & normalization
			// pr.calTF_IDF();

			// pr.writeBookmark_Tags(new File("Output4Stats/matrix4lastfm"));
		} catch (SQLException sqlEx) {
			System.out.println("SQL exception...");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void executePCA(Preprocessing pr) throws Exception {
		// Using PCA from EJML
		// PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis();
		// pca.setup(Environment.hm_bookmark_tag.size(),
		// Environment.hm_token_weight.size());
		// BufferedReader br = new BufferedReader(new FileReader(
		// new File("matrix")));
		// String s = "";
		// String[] arrStr;
		// double[] nums = null;
		// while ((s = br.readLine()) != null) {
		// arrStr = s.split("\t");
		// nums = new double[arrStr.length - 1];
		// for (int i = 0; i < nums.length; i++) {
		// nums[i] = Double.parseDouble(arrStr[i + 1]);
		// }
		// pca.addSample(nums);
		// }
		// pca.computeBasis(Environment.featureSize);
		// System.out.println(pca.sampleToEigenSpace(nums));

		PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis();
		// int bm_num = 68479;
		// int tg_num = 11619;
		pca.setup(Environment.hm_bookmark_tag.size(),
				Environment.hm_token_weight.size());
		File mxIN = new File("Output4Stats/matrix");
		File mxOUT = new File("Output4Stats/norm_matrix_ejml");
		BufferedReader br = new BufferedReader(new FileReader(mxIN));
		String s = "";
		String[] arrStr;
		double[] nums = null;
		System.out.println("Start reading matrix...");
		while ((s = br.readLine()) != null) {
			arrStr = s.split("\t");
			nums = new double[arrStr.length - 1];
			for (int i = 0; i < nums.length; i++) {
				nums[i] = Double.parseDouble(arrStr[i + 1]);
			}
			pca.addSample(nums);
		}
		br.close();

		System.out.println("Start PCA...");
		pca.computeBasis(Environment.featureSize);

		// Write down to file
		System.out.println("Start writing norm matrix...");
		pr.writeNormMatrix(pca, mxIN, mxOUT);
	}

	public static void executePCAFM(Preprocessing_lastfm pr) throws Exception {
		PrincipleComponentAnalysis pca = new PrincipleComponentAnalysis();
		// int bm_num = 68479;
		// int tg_num = 11619;
		pca.setup(Environment.hm_artist_tag.size(),
				Environment.hm_tag_artistset.size());
		File mxIN = new File("Output4Stats/matrix4lastfm");
		File mxOUT = new File("Output4Stats/norm_matrix_ejml_lastfm");
		BufferedReader br = new BufferedReader(new FileReader(mxIN));
		String s = "";
		String[] arrStr;
		double[] nums = null;
		System.out.println("Start reading matrix...");
		while ((s = br.readLine()) != null) {
			arrStr = s.split("\t");
			nums = new double[arrStr.length - 1];
			for (int i = 0; i < nums.length; i++) {
				nums[i] = Double.parseDouble(arrStr[i + 1]);
			}
			pca.addSample(nums);
		}
		br.close();

		System.out.println("Start PCA...");
		pca.computeBasis(Environment.featureSize);

		// Write down to file
		System.out.println("Start writing norm matrix...");
		pr.writeNormMatrix(pca, mxIN, mxOUT);
	}

	@Override
	public void run() {
	};

	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		Preprocessing pr;
		switch (Environment.DATASOURCE) {
		case DELICIOUS:
			pr = new Preprocessing();
			// Init data for PCA
			ALGControl.initData4Delicious(pr);
			// Get users from db
			pr.buildUserList(Dbconnection._getConn().getResultSet(
					GlobalSQLQuery.GETUSER));
			break;
		case LASTFM:
			pr = new Preprocessing_lastfm();
			ALGControl.initData4LastFM(pr);
			pr.buildUserList(Dbconnection._getConn().getResultSet(
					GlobalSQLQuery.GETUSER));
			break;
		}

		// Read norm matrix from file outside
		ALGControl.readMatrix();

		ALGControl alg;

		switch (Environment.runningAlgType) {
		case LINUCB_SIN:
			// Running LinSIN
			alg = new AlgorithmThreadBuilder(AlgorithmType.LINUCB_SIN);
			alg.start();
			break;
		case LINUCB_IND:
			// Running LinIND
			alg = new AlgorithmThreadBuilder(AlgorithmType.LINUCB_IND);
			alg.start();
			break;
		case LINUCB_KMEAN:
			// Running LinUCB-Kmean
			alg = new AlgorithmThreadBuilder(AlgorithmType.LINUCB_KMEAN);
			alg.start();
			break;
		case LINUCB_TREE:
			// Run LinUCBTREE
			alg = new AlgorithmThreadBuilder(AlgorithmType.LINUCB_TREE);
			alg.start();
			break;
		case CLUB:
			// Run CLUB
			alg = new AlgorithmThreadBuilder(AlgorithmType.CLUB);
			alg.start();
			break;
		case LINUCB_VER:
			// Running verification && Warmstart
			TreeFixedCluster.doCluster();
			alg = new AlgorithmThreadBuilder(AlgorithmType.LINUCB_VER);
			alg.start();
			break;
		case LINUCB_WARM:
			TreeFixedCluster.doCluster();
			alg = new AlgorithmThreadBuilder(AlgorithmType.LINUCB_WARM);
			alg.start();
			break;
		}
	}
}
