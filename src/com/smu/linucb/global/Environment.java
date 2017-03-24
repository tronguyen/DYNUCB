package com.smu.linucb.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smu.alg.view.DrawChart;

public class Environment {
	// Delicious
	public static Map<String, Double> hm_token_weight = new HashMap<String, Double>();
	public static Map<String, Set<Integer>> hs_df = new HashMap<String, Set<Integer>>();
	public static Map<Integer, Map<String, Double>> hm_bookmark_tag = new HashMap<Integer, Map<String, Double>>();
	public static Set<String> tagSet = new HashSet<String>();
	public static List<Integer> userLst = new ArrayList<Integer>();
	public static Set<Integer> removedBM = new HashSet<Integer>();

	// LastFM
	public static Map<Integer, Map<String, Double>> hm_artist_tag = new HashMap<Integer, Map<String, Double>>();
	public static Map<String, Set<Integer>> hm_tag_artistset = new HashMap<String, Set<Integer>>();

	public static Map<Integer, Double[]> normMatrix = new HashMap<Integer, Double[]>();
	public static List<Integer> bmidLst = new ArrayList<Integer>();
	public static Map<Integer, Integer> usrClusterMap = new HashMap<Integer, Integer>();
	public static Map<Integer, List<Integer>> clusterMap = new HashMap<Integer, List<Integer>>();

	// public static Map<Integer, List<Integer>> clusterExtraMap = new
	// HashMap<Integer, List<Integer>>();

	// Configure for LinUCB TREE
	public static int numCluster = 4;
	public static int numBranch = 4;

	// Configure for warm-start
	public static int numWarmIter = 2000;

	// Configure parameters
	public static int featureSize = 25;
	public static int numContextVecs = 25;
	public static double delta = 0.05;
	public static double alphaLin = 1 + Math.sqrt(Math.log(2 / delta) / 2);
	public static double alphaUCB = Math.pow(10, -1); // Fixed 0.1 for best
	public static double alphaICML = 0.35; // Math.pow(10, 0);
	public static double payoffRight = 1;
	public static double payoffWrong = (double) -1 / 24;
	public static int limitTime = 50000;
	public static int numAvgLoop = 10; // Number of thread for each algorithm
	public static int buffSizeDisplay = 10;
	public static double percentExchange = 0.1;

	// For checking
	public static Map<Integer, List<Integer>> usrReturnMap = new HashMap<Integer, List<Integer>>();
	public static Map<Integer, Double> rwUserAfterWarm = new HashMap<Integer, Double>();

	// For friend relationship
	public static Map<Integer, List<Integer>> usrRelationMap = new HashMap<Integer, List<Integer>>();

	// Compare to LinUCB_Verification
	public static Map<Integer, Double> trackUserRewardMap = new HashMap<Integer, Double>();
	public static Map<Integer, Double> typeRewardMap = new HashMap<Integer, Double>();

	public static String RW2FILE_WARM = "Output4Stats/RW2File_WARM/";
	public static String RW2FILE_VER = "Output4Stats/RW2File_VER/";
	public static DATASET DATASOURCE = DATASET.LASTFM;
	public static String RUNNINGTIME = "_TIME_6";
	public static boolean readMode = true;
	public static boolean randomGraph = false;
	public static AlgorithmType runningAlgType = AlgorithmType.LINUCB_KMEAN;
}
