package com.smu.linucb.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.ejml.simple.SimpleMatrix;

import com.smu.linucb.global.AlgorithmType;
import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalFunction;
import com.smu.linucb.global.GlobalSQLQuery;

public class CLUB extends LinUCB {
	private double rewardTotal = 0;
	private int[][] userGraph;
	private Map<Integer, IndItem> userItemMap;
	private Map<Integer, Integer> userFrequency;
	private Set<Integer> walkedOrderUserSet;
	private Set<Integer> clusterUser;
	private EuclideanDistance edd;
	private LinUCB linucb4ICML;
	private Set<Integer> visitedSet;
	private int userLstSize;
	private Random genGraph;
	private String fileAdd;

	private void init() {
		userGraph = new int[Environment.userLst.size()][Environment.userLst
				.size()];
		userItemMap = new HashMap<Integer, IndItem>();
		userFrequency = new HashMap<Integer, Integer>();
		walkedOrderUserSet = new HashSet<Integer>();
		clusterUser = new HashSet<Integer>();
		edd = new EuclideanDistance();
		linucb4ICML = new LinUCB(1);
		visitedSet = new HashSet<Integer>();

		this.setAlgType(AlgorithmType.CLUB);
		fileAdd = fileAddCommon + this.getAlgType()
				+ Environment.RUNNINGTIME;
		userLstSize = Environment.userLst.size();
	}

	public CLUB() {
		init();
		if (Environment.randomGraph) {
			if (Environment.readMode) {
				// Read the graph from file
				userGraph = GlobalFunction.readInGraph(new File(fileAdd
						+ "_GRAPHFILE"), userLstSize);
			} else {
				double pValue = 3 * Math.log(userLstSize) / userLstSize;
				for (int i = 0; i < userLstSize; i++) {
					for (int j = i; j < userLstSize; j++) {
						userGraph[j][i] = userGraph[i][j] = (i != j && ((new Random())
								.nextDouble() < pValue)) ? 1 : 0;
					}
				}
				// Write graph to file
				GlobalFunction.writeOutGraph(new File(fileAdd + "_GRAPHFILE"),
						userGraph, userLstSize);
			}

			// Init user items
			IndItem u = null;
			for (int i = 0; i < userLstSize; i++) {
				u = new IndItem();
				userItemMap.put(Environment.userLst.get(i), u);
			}
		} else {
			IndItem u = null;
			userLstSize = Environment.userLst.size();
			for (int i = 0; i < userLstSize; i++) {
				for (int j = 0; j < userLstSize; j++) {
					userGraph[i][j] = (i == j) ? 0 : 1;
				}
				u = new IndItem();
				userItemMap.put(Environment.userLst.get(i), u);
			}
		}
	}

	private void retrieveConnectedUsers(int usrOrder) {
		for (int i = 0; i < userLstSize; i++) {
			if (userGraph[usrOrder][i] == 1
					&& !this.walkedOrderUserSet.contains(i)) {
				this.clusterUser.add(Environment.userLst.get(i));
				this.walkedOrderUserSet.add(i);
				retrieveConnectedUsers(i);
			}
		}
	}

	private void updateCluster(int usrOrder) {
		int usr = Environment.userLst.get(usrOrder), neg;
		IndItem usrItem = this.userItemMap.get(usr);
		IndItem negItem = null;
		SimpleMatrix usrWeight = usrItem.getM().invert().mult(usrItem.getB()), negWeight;
		double userCB = calConfidenceBound(this.userFrequency.get(usr) - 1), negCB;
		for (int i = 0; i < userLstSize; i++) {
			if (userGraph[usrOrder][i] == 1) {
				neg = Environment.userLst.get(i);
				negItem = this.userItemMap.get(neg);
				negWeight = negItem.getM().invert().mult(negItem.getB());
				if (!this.userFrequency.containsKey(neg)) {
					negCB = calConfidenceBound(0);
				} else {
					negCB = calConfidenceBound(this.userFrequency.get(neg) - 1);
				}
				if (this.edd.compute(
						GlobalFunction.convert2DoubleArr(usrWeight),
						GlobalFunction.convert2DoubleArr(negWeight)) > (userCB + negCB)) {
					userGraph[usrOrder][i] = 0;
					userGraph[i][usrOrder] = 0;
				}
			}
		}
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
					fileAdd + " [" + Environment.alphaICML + "]")));
			for (int i = 1; i <= Environment.limitTime; i++) {
				// Pick user randomly
				usrOrder = rUSR.nextInt(userLstSize);
				usr = Environment.userLst.get(usrOrder);
				if (this.userFrequency.containsKey(usr)) {
					this.userFrequency
							.put(usr, this.userFrequency.get(usr) + 1);
				} else {
					this.userFrequency.put(usr, 1);
				}
				System.out.println("###User: " + usr);

				// Retrieve Users belong to the same cluster
				this.walkedOrderUserSet.add(usrOrder);
				this.clusterUser.add(usr);
				retrieveConnectedUsers(usrOrder);
				System.out.println("---ConnectedUsers: "
						+ this.clusterUser.size());

				// Run LinUCB
				linucb4ICML.setUser(usr);
				linucb4ICML.implICML(this.clusterUser, this.userItemMap, i);
				linucb4ICML.resetICML();
				this.rewardTotal += linucb4ICML.getPayoff();

				// Update clusters
				updateCluster(usrOrder);

				// Remove data for each user
				this.walkedOrderUserSet.clear();
				this.clusterUser.clear();
				// Draw chart
				// this.displayResult(i, LinUCB_KMEAN.rewardTotal);
				// this.updateRewardMap(this.getInClass(), i, this.rewardTotal);
				if ((i % Environment.buffSizeDisplay) == 0) {
					// Draw chart
					this.displayResult(i, this.rewardTotal, this.getDrChart());
					bw.write(i + "\t" + this.rewardTotal + "\n");
					bw.flush();
				}
			}
			bw.flush();
			bw.close();
			bw = new BufferedWriter(new FileWriter(new File(fileAdd + " ["
					+ Environment.alphaICML + "]" + "_TRACKING")));

			for (int i = 0; i < userLstSize; i++) {
				if (!visitedSet.contains(Environment.userLst.get(i))) {
					this.walkedOrderUserSet.add(i);
					this.clusterUser.add(Environment.userLst.get(i));
					retrieveConnectedUsers(i);
					visitedSet.addAll(this.clusterUser);

					bw.write(this.clusterUser + "\n");
					bw.flush();

					this.walkedOrderUserSet.clear();
					this.clusterUser.clear();
				}
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
