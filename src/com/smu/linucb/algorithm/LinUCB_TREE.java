package com.smu.linucb.algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.aliasi.cluster.ClusterScore;
import com.smu.control.AlgorithmThreadBuilder;
import com.smu.linucb.global.AlgorithmType;
import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalFunction;

public class LinUCB_TREE extends AlgorithmThreadBuilder {
	private double rewardTotal = 0;
	private UCB1 rootTree;
	private List<UCB1> leavesTree = new ArrayList<UCB1>();
	private Set<Integer> fstTimeUsrLst = new HashSet<Integer>();
	private Random rClus = new Random();
	private boolean fixedCluster = false; // fix clusters permanently &
											// temporarily
	private boolean isWarmStart = false;
	private int warmIter;
	private int indexLeaf = 0;
	private Map<Integer, Integer> userLeafMap = new HashMap<Integer, Integer>();
	private int hitBranch = 0;
	private boolean includedErr = false;

	// 4 Clustering by K-Mean
	public Map<Integer, Integer> usrClusterMap = new HashMap<Integer, Integer>(
			Environment.usrClusterMap);
	public Map<Integer, List<Integer>> clusterMap = new HashMap<Integer, List<Integer>>(
			Environment.clusterMap);
	public Map<Integer, Integer> errUsrClsMap = new HashMap<Integer, Integer>();
	public Set<Integer> errUsrSet = new HashSet<Integer>();

	public LinUCB_TREE() {
		// super(AlgorithmType.LINUCB_TREE);
		// this.setAlgType(AlgorithmType.LINUCB_TREE);
		this.rClus.setSeed(System.nanoTime() * Thread.currentThread().getId());
	}

	// Build empty tree
	public void buildTree(UCB1 pRoot, int maxLv) {
		if (maxLv == 0) {
			// Create LinUCB node
			pRoot.linucb = new LinUCB();
			pRoot.setIndexLeaf(this.indexLeaf);
			this.leavesTree.add(pRoot);
			this.indexLeaf++;
			return;
		}
		UCB1 pNext = null;
		for (int i = 0; i < Environment.numBranch; i++) {
			pNext = new UCB1(pRoot);
			// Generate payoff list for all users

			buildTree(pNext, maxLv - 1);
		}
	}

	public static void backPropagation(UCB1 curNode, double payoff, int usr) {
		UCB1 cur = curNode;
		UserItem usrItem = null;
		while (cur != null) {
			if (cur.payoffMap.containsKey(usr)) {
				usrItem = cur.payoffMap.get(usr);
				usrItem.setVisit(usrItem.getVisit() + 1);
				usrItem.setPayoff(usrItem.getPayoff() + payoff);
			} else {
				cur.payoffMap.put(usr, new UserItem(payoff, 1));
			}
			cur = cur.pNode;
		}
	}

	@Override
	public void run() {
		if (this.isIncludedErr()) {
//			this.genSyntheticData();
			this.genSyntheticData4NegUsr();
		}

		int usr;
		this.rootTree = new UCB1();
		UCB1 cur = null;
		LinUCB cluster = null;
		buildTree(this.rootTree,
				(int) Math.ceil((Math.log(Environment.numCluster) / Math
						.log(Environment.numBranch))));
		int index;
		List<Integer> itemOrder;
		int rightBackOrder = 0;
		File f = null;
		long threadID = this.getId();
		if (this.getAlgType() == AlgorithmType.LINUCB_WARM) {
			f = new File(Environment.RW2FILE_WARM + "_" + threadID);
		} else if (this.getAlgType() == AlgorithmType.LINUCB_VER) {
			f = new File(Environment.RW2FILE_VER + "_" + threadID);
		}
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(f));

			for (int i = 1; i <= Environment.limitTime; i++) {
				// Pick user randomly
				usr = Environment.userLst.get(rUSR.nextInt(Environment.userLst
						.size()));
				if (!this.isFixedCluster()) {
					if (this.fstTimeUsrLst.contains(usr)
							&& (!this.isWarmStart || (this.isWarmStart
									&& (this.isIncludedErr() ? this.errUsrSet
											.contains(usr) : true) && this.warmIter > Environment.numWarmIter))) {
						/*
						 * Run UCB1. Find the cluster the user to which belongs
						 */
						cur = this.rootTree;
						while (cur.childLst.size() != 0) {
							cur = UCB1.impl(usr, cur);
						}
						// Checking: Reward User after Warm-Step
						// if (!Environment.rwUserAfterWarm.containsKey(usr)) {
						// Environment.rwUserAfterWarm.put(usr,
						// this.leavesTree.get(this.userLeafMap
						// .get(usr)).payoffMap.get(usr)
						// .getPayoff());
						// }

						// Increase num of hits (users err-switched)
						// this.hitBranch++;
						// if (Environment.usrReturnMap.containsKey(usr)) {
						// rightBackOrder =
						// Environment.usrReturnMap.get(usr).get(
						// 0) + 1;
						// Environment.usrReturnMap.get(usr)
						// .set(0, rightBackOrder);
						// if (cur.getIndexLeaf() == Environment.errUsrClsMap
						// .get(usr)) {
						// Environment.usrReturnMap.get(usr).add(
						// rightBackOrder);
						// }
						// } else {
						// itemOrder = new ArrayList<Integer>();
						// itemOrder.add(1);
						// if (cur.getIndexLeaf() == Environment.errUsrClsMap
						// .get(usr)) {
						// itemOrder.add(1);
						// }
						// Environment.usrReturnMap.put(usr, itemOrder);
						//
						// }
					} else {
						// Select randomly cluster for user having the first
						// time
						// falling
						// down
						this.fstTimeUsrLst.add(usr);
						if (!this.isWarmStart) {
							cur = this.leavesTree.get(this.rClus
									.nextInt(Environment.numCluster));
						} else {
							cur = this.leavesTree.get(this.usrClusterMap
									.get(usr));
							this.warmIter++;
						}
					}
				} else {
					cur = this.leavesTree.get(this.usrClusterMap.get(usr));
				}
				// Put user into leaf
				this.userLeafMap.put(usr, cur.getIndexLeaf());

				// Run LinUCB for the cluster
				cluster = cur.linucb;
				cluster.setUser(usr);
				cluster.impl();
				cluster.reset();

				// Update weight for the path
				LinUCB_TREE.backPropagation(cur, cluster.getPayoff(), usr);

				this.rewardTotal += cluster.getPayoff();

				// Draw chart
				// this.displayResult(i, this.rewardTotal);
				// this.updateRewardMap(this.getInClass(), i, this.rewardTotal);
				printRW2File(i, this.rewardTotal, f, bw); // 4PrintFile

				// Tracking user reward
				// GlobalFunction.sumValueMap(Environment.trackUserRewardMap,
				// usr,
				// cluster.getPayoff());

			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Print Reward User after Warm-Step
		// try {
		// BufferedWriter bww = new BufferedWriter(new FileWriter(new File(
		// "Output4Stats/afterwarm" + Environment.alphaUCB)));
		// int k;
		// for (Iterator<Integer> it = Environment.rwUserAfterWarm.keySet()
		// .iterator(); it.hasNext();) {
		// k = it.next();
		// bww.write(k + "\t" + Environment.rwUserAfterWarm.get(k) + "\n");
		// }
		// bww.flush();
		// bww.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Compare to K-Mean clustering
		// compare2Kmean();
		// compare2Origin();
		// displayUserReward4Ver();
		this.interrupt();
	}

	private void printRW2File(int count, double rw, File f, BufferedWriter bw) {
		if ((count % Environment.buffSizeDisplay) == 0) {
			try {
				bw.write(count + "|" + rw + "\n");
				bw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void displayUserReward4Ver() {
		File fType = new File("Output4Stats/TrackingUser_Warm/Results["
				+ Environment.numCluster + "cls-" + Environment.alphaUCB
				+ "alpha]_UserType");
		File f = new File("Output4Stats/TrackingUser_Ver/Results["
				+ Environment.numCluster + "cls-" + Environment.alphaUCB
				+ "alpha]");
		String str = "";
		String[] val;
		Map<Integer, Double> usrTypeRewardMap = new HashMap<Integer, Double>();
		int usr, usrType;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fType));
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			while ((str = br.readLine()) != null) {
				val = str.split("\\|");
				usr = Integer.valueOf(val[0]);
				usrType = Integer.valueOf(val[1]);
				GlobalFunction.sumValueMap(usrTypeRewardMap, usrType,
						Environment.trackUserRewardMap.get(usr));
				bw.write(usr + "|" + Environment.trackUserRewardMap.get(usr)
						+ "\n");
			}
			for (Iterator<Integer> i = usrTypeRewardMap.keySet().iterator(); i
					.hasNext();) {
				usrType = i.next();
				bw.write("Type " + usrType + ": "
						+ usrTypeRewardMap.get(usrType) + "\n");
			}
			bw.write("Reward: " + this.rewardTotal + "\n");
			br.close();
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Compare only 10% changes (90% fixed class) to original clustering
	// (K-Mean)
	private void compare2Origin() {
		List<Integer> usrItems;
		int usr, cls, right = 0, wrong = 0, itemVal, usrType;
		double rate = 0, usrRW;
		Map<Integer, Double> usrTypeRewardMap = new HashMap<Integer, Double>();
		File f = new File("Output4Stats/TrackingUser_Warm/Results["
				+ Environment.numCluster + "cls-" + Environment.alphaUCB
				+ "alpha]");
		File fType = new File("Output4Stats/TrackingUser_Warm/Results["
				+ Environment.numCluster + "cls-" + Environment.alphaUCB
				+ "alpha]_UserType");
		List<Integer> returnLst;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			BufferedWriter bwType = new BufferedWriter(new FileWriter(fType));

			for (Iterator<Integer> i = this.errUsrClsMap.keySet().iterator(); i
					.hasNext();) {
				usr = i.next();
				cls = this.errUsrClsMap.get(usr);
				returnLst = new ArrayList<Integer>();
				// for (int j = 0; j < usrLst.size(); j++) {
				// Turn back their right cluster
				if (this.userLeafMap.get(usr) == cls) {
					right++;
				} else {
					wrong++;
				}
				usrItems = Environment.usrReturnMap.get(usr);
				rate += (double) (usrItems.size() - 1) / usrItems.get(0);
				bw.write(usr + "|" + usrItems.get(0) + "<<");
				for (int k = 1; k < usrItems.size(); k++) {
					itemVal = usrItems.get(k);
					returnLst.add(itemVal);
					bw.write(" " + itemVal);
				}
				usrType = getTypeUser(returnLst, usrItems.get(0));
				usrRW = Environment.trackUserRewardMap.get(usr);
				bw.write("|" + usrType + "|" + usrRW + "\n");
				bwType.write(usr + "|" + usrType + "\n");

				// Sum rewards for each user-type
				GlobalFunction.sumValueMap(usrTypeRewardMap, usrType, usrRW);
			}
			// Print result comparison
			bw.write("Right back: " + right + "\n");
			bw.write("Wrong back: " + wrong + "\n");
			bw.write("Hit Err-User: " + this.hitBranch + "\n");
			bw.write("Size error: " + this.errUsrSet.size() + "\n");
			bw.write("Compare: " + (double) right / (right + wrong) + "\n");
			bw.write("Hit Rate: " + (double) this.hitBranch
					/ this.errUsrSet.size() + "\n");
			bw.write("Right Back Rate: " + rate
					/ this.errUsrClsMap.keySet().size() + "\n");
			bw.write("Reward: " + this.rewardTotal + "\n");

			for (Iterator<Integer> i = usrTypeRewardMap.keySet().iterator(); i
					.hasNext();) {
				usrType = i.next();
				bw.write("Type " + usrType + ": "
						+ usrTypeRewardMap.get(usrType) + "\n");
			}

			bw.flush();
			bw.close();
			bwType.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get type of user based on list of return-times Type1: User has right back
	 * Type2: User keeps staying in wrong cluster (maybe refined cluster) Type3:
	 * User wanders around, tries nearly regular-times in each cluster
	 * otherwise, return type0
	 * 
	 * @param returnLst
	 * @return
	 */

	private int getTypeUser(List<Integer> returnLst, int totalReturn) {
		int type = 0, size = returnLst.size() - 1;
		if (size < 0)
			return type;
		double[] val = new double[size];
		double sdValue, meanValue;
		StandardDeviation sd = new StandardDeviation();
		Mean mn = new Mean();
		for (int i = 1; i <= size; i++) {
			val[i - 1] = returnLst.get(i) - returnLst.get(i - 1);
		}
		sdValue = sd.evaluate(val);
		meanValue = mn.evaluate(val);
		if (size >= 2 * Math.floor((double) this.hitBranch
				/ (this.errUsrSet.size() * Environment.numCluster))
				&& meanValue < 3) {
			if (sdValue < 2 && returnLst.get(size) == totalReturn) {
				type = 1;
			} else {
				if (returnLst.get(size) == totalReturn) {
					type = getTypeUser(returnLst.subList(2, returnLst.size()),
							totalReturn);
				} else if (totalReturn - returnLst.get(size) <= 3) {
					type = getTypeUser(returnLst.subList(2, returnLst.size()),
							returnLst.get(size));
				}
			}
		} else if ((totalReturn - returnLst.get(size)) / Environment.numCluster >= 2) {
			type = 2;
		} else if (sdValue < 2
				&& Math.abs(meanValue - Environment.numCluster) < 2) {
			type = 3;
		}
		return type;
	}

	// Compare B-cube-measured clustering
	private void compare2Kmean() {
		Set<Set<Integer>> referencePartition = new HashSet<Set<Integer>>();
		Set<Set<Integer>> responsePartition = new HashSet<Set<Integer>>();
		for (Iterator<Integer> i = this.clusterMap.keySet().iterator(); i
				.hasNext();) {
			referencePartition.add(new HashSet(this.clusterMap.get(i.next())));
		}

		System.out.println("reference set");
		printMap(this.clusterMap);
		Map<Integer, List<Integer>> tempMap = new HashMap<Integer, List<Integer>>();
		// Convert response to clusterMap
		for (Iterator<Integer> i = this.userLeafMap.keySet().iterator(); i
				.hasNext();) {
			int usr = i.next();
			GlobalFunction.addValueMap(tempMap, this.userLeafMap.get(usr), usr);
		}

		for (Iterator<Integer> i = tempMap.keySet().iterator(); i.hasNext();) {
			responsePartition.add(new HashSet(tempMap.get(i.next())));
		}
		System.out.println("response set");
		printMap(tempMap);

		ClusterScore<Integer> score = new ClusterScore<Integer>(
				referencePartition, responsePartition);
		System.out.println("\nB-Cubed Measures");
		System.out.println("  Cluster Averaged Precision = "
				+ score.b3ClusterPrecision());
		System.out.println("  Cluster Averaged Recall = "
				+ score.b3ClusterRecall());
		System.out.println("  Cluster Averaged F(1) = " + score.b3ClusterF());
		System.out.println("  Element Averaged Precision = "
				+ score.b3ElementPrecision());
		System.out.println("  Element Averaged Recall = "
				+ score.b3ElementRecall());
		System.out.println("  Element Averaged F(1) = " + score.b3ElementF());
	}

	public void genSyntheticData4NegUsr() {
		File f = new File("Output4Stats/negativeUser.txt");
		String s = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			while ((s = br.readLine()) != null) {
				this.errUsrSet.add(Integer.valueOf(s));
			}
			br.close();
		} catch (Exception ex) {
			System.out.println("Exception at gen error data 4 negative users");
		}
	}

	public void genSyntheticData() {
		List<Integer> clusterLst = new ArrayList<Integer>(
				this.clusterMap.keySet());
		// Map<Integer, List<Integer>> rmMap = new HashMap<Integer,
		// List<Integer>>();
		Random r = new Random();
		int lenClus = 0, lenRun = 0;
		int chosenUsr, chosenUsrIndex, chosenCls, clsIndex;
		for (int cls : clusterLst) {
			lenClus = (int) (this.clusterMap.get(cls).size() * Environment.percentExchange);
			lenRun = lenClus;
			// pick random 5% items
			for (int k = 0; k < lenClus; k++) {
				// pick randomly one user in cluster
				chosenUsrIndex = r.nextInt(lenRun--);
				chosenUsr = this.clusterMap.get(cls).get(chosenUsrIndex);
				// pick randomly cluster to push the user in
				clsIndex = r.nextInt(clusterLst.size());
				chosenCls = clusterLst.get(clsIndex);
				if (chosenCls == cls) {
					chosenCls = clusterLst.get((clsIndex + 1)
							% clusterLst.size());
				}
				// Check 5%: Keep track of original status of user
				this.errUsrClsMap.put(chosenUsr, cls);
				this.errUsrSet.add(chosenUsr);
				this.clusterMap.get(cls).remove(chosenUsrIndex);
				// addSpecMap(Environment.clusterExtraMap, chosenCls,
				// chosenUsr);
				// addSpecMap(rmMap, cls, chosenUsr);

				// Change user-cluster Map
				this.usrClusterMap.put(chosenUsr, chosenCls);
			}
		}
	}

	private void printMap(Map<Integer, List<Integer>> mp) {
		int count = 0;
		for (Iterator<Integer> i = mp.keySet().iterator(); i.hasNext();) {
			int cls = i.next();
			System.out.print("Cluster: " + cls);
			System.out.println(Arrays.toString(mp.get(cls).toArray()));
			count += mp.get(cls).size();
		}
		System.out.println("User Total: " + count);
	}

	public boolean isFixedCluster() {
		return fixedCluster;
	}

	public void setFixedCluster(boolean fixedCluster) {
		this.fixedCluster = fixedCluster;
	}

	public boolean isWarmStart() {
		return isWarmStart;
	}

	public void setWarmStart(boolean isWarmStart) {
		this.isWarmStart = isWarmStart;
		this.warmIter = 0;
	}

	public boolean isIncludedErr() {
		return includedErr;
	}

	public void setIncludedErr(boolean includedErr) {
		this.includedErr = includedErr;
	}
}
