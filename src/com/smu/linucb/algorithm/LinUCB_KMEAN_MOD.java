package com.smu.linucb.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

import com.smu.linucb.global.AlgorithmType;
import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalFunction;
import com.smu.linucb.global.GlobalSQLQuery;

public class LinUCB_KMEAN_MOD extends LinUCB {

	private double rewardTotal = 0;
	private List<Integer> fstTimeUsrLst;
	private List<UCB1> clusterLst;
	private Map<Integer, IndItem> userItemMap;
	private Map<Integer, Set<Integer>> clusterItemLstMap;
	int initClusCount = 0;
	private Random rClus;

	private EuclideanDistance edd;
	private String fileAdd;

	public LinUCB_KMEAN_MOD() {
		init();
		this.setAlgType(AlgorithmType.LINUCB_KMEAN);
		fileAdd = fileAddCommon + this.getAlgType()
				+ "_MOD_" + Environment.numCluster;
	}

	public void init() {
		fstTimeUsrLst = new ArrayList<Integer>();
		clusterLst = new ArrayList<UCB1>();
		userItemMap = new HashMap<Integer, IndItem>();
		clusterItemLstMap = new HashMap<Integer, Set<Integer>>();
		initClusCount = 0;
		rClus = new Random(System.nanoTime() * Thread.currentThread().getId());
		edd = new EuclideanDistance();
	}

	@Override
	public void run() {
		int usr;
		UCB1 cur = null;
		IndItem u = null;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					fileAdd)));
			// Environment.drChart.genDiffConfig(AlgorithmType.LINUCB_SIN);
			// TODO Auto-generated method stub
			for (int i = 1; i <= Environment.limitTime; i++) {
				// Pick user randomly
				usr = Environment.userLst.get(rUSR.nextInt(Environment.userLst
						.size()));

				if (this.fstTimeUsrLst.contains(usr)) {
					// Update user info: M, b matrix
					// System.out.println("###User: " + usr + " << "
					// + this.userItemMap.get(usr).getClusterIndex());
					calPayoff(this.clusterLst.get(this.userItemMap.get(usr)
							.getClusterIndex()), usr, i);
					// Reassign to a new cluster
					updateMembership(usr);

				} else {
					this.fstTimeUsrLst.add(usr);
					if (this.initClusCount < Environment.numCluster) {
						cur = new UCB1();
						cur.linucb = new LinUCB(1);
						cur.setIndexLeaf(this.initClusCount++);
						this.clusterLst.add(cur);

						u = new IndItem();
						u.setClusterIndex(cur.getIndexLeaf());
						this.userItemMap.put(usr, u);
						GlobalFunction.addValueMapSet(this.clusterItemLstMap,
								cur.getIndexLeaf(), usr);
						calPayoff(cur, usr, i);

						// Set centroid for K first users
						cur.centroid = u.getTheta();
					} else {
						cur = this.clusterLst.get(this.rClus
								.nextInt(Environment.numCluster));
						u = new IndItem();
						u.setClusterIndex(cur.getIndexLeaf());
						this.userItemMap.put(usr, u);
						GlobalFunction.addValueMapSet(this.clusterItemLstMap,
								cur.getIndexLeaf(), usr);
						calPayoff(cur, usr, i);
						SimpleMatrix uMx = u.getM().invert().mult(u.getB());
						updateCentroid(uMx, cur, usr, true);
					}
				}
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

			bw = new BufferedWriter(new FileWriter(new File(fileAdd
					+ "_TRACKING")));
			for (int k : this.clusterItemLstMap.keySet()) {
				bw.write(this.clusterItemLstMap.get(k) + "\n");
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

	// Update user's membership
	private void updateMembership(int usr) {
		UCB1 chosenCls = null;
		double minDistance = Double.POSITIVE_INFINITY, tempDistance;
		IndItem uItem = this.userItemMap.get(usr);
		SimpleMatrix uMx = uItem.getTheta(), uMxOld = uItem.getThetaOld();
		int clsSize;
		for (UCB1 cls : this.clusterLst) {
			clsSize = this.clusterItemLstMap.get(cls.getIndexLeaf()).size();
			tempDistance = this.edd.compute(GlobalFunction
					.convert2DoubleArr(uMx),
					GlobalFunction.convert2DoubleArr(cls.centroid
							.scale((double) 1 / clsSize)));
			if (tempDistance < minDistance) {
				minDistance = tempDistance;
				chosenCls = cls;
			}
		}

		int clusterIdx = chosenCls.getIndexLeaf();
		int clusterIdxOld = uItem.getClusterIndex();

		// Change membership
		int oldIdx = this.userItemMap.get(usr).getClusterIndex();
		GlobalFunction.delValueMap(this.clusterItemLstMap, oldIdx, usr);
		// Update centroid old cluster
		updateCentroid(uMxOld, this.clusterLst.get(clusterIdxOld), usr, false);

		// Update link to new cluster
		uItem.setClusterIndex(clusterIdx);
		GlobalFunction.addValueMapSet(this.clusterItemLstMap, clusterIdx, usr);
		// Update centroid new cluster
		updateCentroid(uMx, chosenCls, usr, true);

	}

	// Update cluster's centroid
	private void updateCentroid(SimpleMatrix usrVector, UCB1 chosenCls,
			int usr, boolean newUpdate) {
		// IndItem u = null;
		// int clusterIdx = chosenCls.getIndexLeaf();
		// int clusterSize = this.clusterItemLstMap.get(clusterIdx).size();
		// for (int uItemID : this.clusterItemLstMap.get(clusterIdx)) {
		// if (usr == uItemID)
		// continue;
		// u = this.userItemMap.get(uItemID);
		// avgVector = avgVector.plus(u.getTheta());
		// }

		chosenCls.centroid = (newUpdate) ? (chosenCls.centroid.plus(usrVector))
				: (chosenCls.centroid.minus(usrVector));
	}

	private void calPayoff(UCB1 cur, int usr, int times) {
		LinUCB cluster = cur.linucb;
		int clusterIdx = cur.getIndexLeaf();

		cluster.setUser(usr);
		cluster.implICML(this.clusterItemLstMap.get(clusterIdx),
				this.userItemMap, times);
		cluster.resetICML();
		this.rewardTotal += cluster.getPayoff();
	}
}
