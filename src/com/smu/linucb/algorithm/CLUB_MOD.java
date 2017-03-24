package com.smu.linucb.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.ejml.simple.SimpleMatrix;

import com.smu.linucb.global.AlgorithmType;
import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalFunction;
import com.smu.linucb.global.GlobalSQLQuery;

public class CLUB_MOD extends LinUCB {
	private double rewardTotal = 0;
	private static int[][] userGraph = new int[Environment.userLst.size()][Environment.userLst
			.size()];
	private static Map<Integer, IndItem> userItemMap = new HashMap<Integer, IndItem>();
	private Map<Integer, Integer> userFrequency = new HashMap<Integer, Integer>();
	private Set<Integer> walkedOrderUserSet = new HashSet<Integer>();
	private Set<Integer> clusterUser;
	private EuclideanDistance edd = new EuclideanDistance();
	private LinUCB linucb4ICML = new LinUCB(1);
	private static List<Set<Integer>> clusterManager = new ArrayList<Set<Integer>>();
	// private static Map<Integer, Integer> orderUserMap = new HashMap<Integer,
	// Integer>();
	// private Set<Integer> splitConnectedUserSet;
	private boolean isConnectedCheck = false;
	private String fileAdd = GlobalSQLQuery.outputFile + this.getAlgType()
			+ "_MOD_" + Environment.alphaICML + Environment.RUNNINGTIME;

	static {
		IndItem u = null;
		int usr;
		Set<Integer> usrSet = new HashSet<Integer>(Environment.userLst);
		for (int i = 0; i < Environment.userLst.size(); i++) {
			for (int j = 0; j < Environment.userLst.size(); j++) {
				userGraph[i][j] = (i == j) ? 0 : 1;
			}
			u = new IndItem();
			u.setConnectedUserSet(usrSet);
			u.order = i;
			usr = Environment.userLst.get(i);
			userItemMap.put(usr, u);
			// orderUserMap.put(usr, i);
			// usrSet.add(usr);
		}
		clusterManager.add(usrSet);
	}

	public CLUB_MOD() {
		this.setAlgType(AlgorithmType.CLUB);
	}

	private void retrieveConnectedUsers(int usrOrder) {
		// int negOrder;
		// int usr = Environment.userLst.get(usrOrder);
		IndItem negItem = null;
		// for (int i : userItemMap.get(usr).getConnectedUserSet()) {
		// negItem = userItemMap.get(i);
		// negOrder = negItem.order;
		// if (userGraph[usrOrder][negOrder] == 1 && !clusterUser.contains(i)) {
		// clusterUser.add(i);
		// negItem.setConnectedUserSet(clusterUser);
		// retrieveConnectedUsers(i);
		// }
		// }

		for (int i = 0; i < Environment.userLst.size(); i++) {
			if (userGraph[usrOrder][i] == 1
					&& !this.walkedOrderUserSet.contains(i)) {
				negItem = this.userItemMap.get(Environment.userLst.get(i));
				this.clusterUser.add(Environment.userLst.get(i));
				negItem.setConnectedUserSet(clusterUser);
				this.walkedOrderUserSet.add(i);
				retrieveConnectedUsers(i);
			}
		}
	}

	private void updateCluster(int usrOrder) {
		int usr = Environment.userLst.get(usrOrder), neg, negOrder;
		IndItem usrItem = this.userItemMap.get(usr);
		IndItem negItem = null;
		SimpleMatrix usrWeight = usrItem.getTheta(), negWeight;
		double userCB = calConfidenceBound(this.userFrequency.get(usr) - 1), negCB;
		// Set<Integer> eliminatedSet = new HashSet<Integer>();
		for (int i : Environment.userLst) {
			negItem = this.userItemMap.get(i);
			negOrder = negItem.order;
			if (userGraph[usrOrder][negOrder] == 1) {
				negWeight = negItem.getTheta();
				if (!this.userFrequency.containsKey(i)) {
					negCB = calConfidenceBound(0);
				} else {
					negCB = calConfidenceBound(this.userFrequency.get(i) - 1);
				}
				if (this.edd.compute(
						GlobalFunction.convert2DoubleArr(usrWeight),
						GlobalFunction.convert2DoubleArr(negWeight)) > (userCB + negCB)) {
					userGraph[usrOrder][negOrder] = 0;
					userGraph[negOrder][usrOrder] = 0;
					boolean checkConnected = isConnected(usrOrder, negOrder);
					walkedOrderUserSet.clear();
					if (!checkConnected) {
						clusterUser = new HashSet<Integer>();
						clusterUser.add(usr);

						retrieveConnectedUsers(usrOrder);
						walkedOrderUserSet.clear();
						usrItem.getConnectedUserSet().removeAll(clusterUser);
						usrItem.setConnectedUserSet(clusterUser);
						clusterManager.add(clusterUser);
					}
				}
			}
		}
	}

	private boolean isConnected(int orderUsrA, int orderUsrB) {
		// Stack<Integer> stackUsers = new Stack<Integer>();
		// Set<Integer> visitedSet = new HashSet<Integer>();
		// boolean hasNeg;
		// stackUsers.push(orderUsrA);
		// visitedSet.add(orderUsrA);
		// int head;
		// int count = 0;
		// while (!stackUsers.empty()) {
		// System.out.println("..." + count++);
		// hasNeg = false;
		// for (int i = 0; i < Environment.userLst.size(); i++) {
		// head = stackUsers.peek();
		// if (userGraph[head][i] == 1 && !visitedSet.contains(i)) {
		// if (i == orderUsrB)
		// return true;
		// stackUsers.push(i);
		// visitedSet.add(i);
		// hasNeg = true;
		// break;
		// }
		// }
		// if (!hasNeg) {
		// stackUsers.pop();
		// }
		// }
		if (orderUsrA == orderUsrB)
			return true;
		for (int i = 0; i < Environment.userLst.size(); i++) {
			if (userGraph[orderUsrA][i] == 1 && !walkedOrderUserSet.contains(i)) {
				walkedOrderUserSet.add(i);
				boolean check = isConnected(i, orderUsrB);
				if (check)
					return check;
			}
		}
		return false;
	}

	public double calConfidenceBound(int times) {
		double val = 0;
		val = Environment.alphaICML
				* Math.sqrt((1 + Math.log(1 + times)) / (1 + times));
		return val;
	}

	@Override
	public void run() {
		int usr;
		int usrOrder;
		// Environment.drChart.genDiffConfig(AlgorithmType.LINUCB_SIN);
		// TODO Auto-generated method stub
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					fileAdd)));
			for (int i = 1; i <= Environment.limitTime; i++) {
				System.out.println("Time: " + i);
				// Pick user randomly
				usrOrder = rUSR.nextInt(Environment.userLst.size());
				usr = Environment.userLst.get(usrOrder);
				if (this.userFrequency.containsKey(usr)) {
					this.userFrequency
							.put(usr, this.userFrequency.get(usr) + 1);
				} else {
					this.userFrequency.put(usr, 1);
				}
				System.out.println("###User: " + usr);

				// Retrieve Users belong to the same cluster
				// this.walkedOrderUserSet.add(usrOrder);
				// this.clusterUser.add(usr);
				// retrieveConnectedUsers(usrOrder);
				// System.out.println("---ConnectedUsers: "
				// + this.clusterUser.size());

				// Run LinUCB
				linucb4ICML.setUser(usr);
				linucb4ICML.implICML(this.userItemMap.get(usr)
						.getConnectedUserSet(), this.userItemMap, i);
				linucb4ICML.resetICML();
				this.rewardTotal += linucb4ICML.getPayoff();

				// Update clusters
				updateCluster(usrOrder);

				// Remove data for each user
				// this.walkedOrderUserSet.clear();
				// this.clusterUser.clear();
				// Draw chart
				// this.displayResult(i, LinUCB_KMEAN.rewardTotal);
				this.updateRewardMap(this.getInClass(), i, this.rewardTotal);
				if ((i % Environment.buffSizeDisplay) == 0) {
					bw.write(i + "\t" + this.rewardTotal + "\n");
					bw.flush();
				}
			}
			bw.flush();
			bw.close();

			bw = new BufferedWriter(new FileWriter(new File(fileAdd
					+ "_TRACKING")));
			for (int k = 0; k < clusterManager.size(); k++) {
				bw.write(clusterManager.get(k) + "\n");
				bw.flush();
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.interrupt();
	}
}
