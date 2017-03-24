package com.smu.linucb.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.ejml.simple.SimpleMatrix;

import com.smu.linucb.global.Environment;

class UserItem {
	private double payoff;
	private int visit;

	public UserItem(double payoff, int visit) {
		this.payoff = payoff;
		this.visit = visit;
	}

	public double getPayoff() {
		return payoff;
	}

	public void setPayoff(double payoff) {
		this.payoff = payoff;
	}

	public int getVisit() {
		return visit;
	}

	public void setVisit(int visit) {
		this.visit = visit;
	}
}

public class UCB1 {
	public List<UCB1> childLst = new ArrayList<UCB1>();
	public Map<Integer, UserItem> payoffMap; // Key: user, Value: payoff for
												// each
												// node
	public UCB1 pNode = null; // parent node
	public UCB1 cNode = null; // child node
	public LinUCB linucb = null;
	public SimpleMatrix centroid = null;
	private int indexLeaf;
	private static Mean mean = new Mean();
	private static Variance var = new Variance();

	public UCB1(UCB1 pNode) {
		this.pNode = pNode;
		this.pNode.childLst.add(this);
		this.payoffMap = new HashMap<Integer, UserItem>();
	}

	public UCB1() {
		this.payoffMap = new HashMap<Integer, UserItem>();
	}

	// UCB1 algorithm
	public static UCB1 impl(int usr, UCB1 pNode) {
		UCB1 selectedNode = null;
		double maxVal = Double.NEGATIVE_INFINITY;
		double val = 0;
		UserItem usrItem = null;
		for (UCB1 child : pNode.childLst) {
			usrItem = child.payoffMap.get(usr);
			// Create user entry
			if (usrItem == null) {
				usrItem = new UserItem(0, 0);
				child.payoffMap.put(usr, usrItem);
			}
			val = usrItem.getPayoff()
					/ (usrItem.getVisit() + 1)
					+ Environment.alphaUCB
					* Math.sqrt(2
							* Math.log(pNode.payoffMap.get(usr).getVisit() + 1)
							/ (usrItem.getVisit() + 1));
			if (val > maxVal) {
				selectedNode = child;
				maxVal = val;
			}
		}
		return selectedNode;
	}

	// public static UCB1 impl(int usr, UCB1 pNode) {
	// UCB1 selectedNode = null;
	// double maxVal = Double.NEGATIVE_INFINITY;
	// double val = 0;
	// UserItem usrItem = null;
	//
	// double avgX = 0, avgY = 0, boundX = 0, boundY = 0;
	// List<Double> rewardRelation;
	// int countRelation = 0;
	// double[] rewardRelationVal;
	// List<Integer> usrRelation = null;
	//
	// for (UCB1 child : pNode.childLst) {
	// usrItem = child.payoffMap.get(usr);
	// // Create user entry
	// if (usrItem == null) {
	// usrItem = new UserItem(0, 0);
	// child.payoffMap.put(usr, usrItem);
	// }
	//
	// /*
	// * Snippet-code for friend relationship
	// */
	// avgX = usrItem.getPayoff() / (usrItem.getVisit() + 1);
	// boundX = Math.sqrt(2
	// * Math.log(pNode.payoffMap.get(usr).getVisit() + 1)
	// / (usrItem.getVisit() + 1));
	//
	// usrRelation = Environment.usrRelationMap.get(usr);
	// rewardRelation = new ArrayList<Double>();
	// if (usrRelation != null) {
	// for (int i = 0; i < usrRelation.size(); i++) {
	// usrItem = child.payoffMap.get(usrRelation.get(i));
	// if (usrItem != null && usrItem.getPayoff() != 0) {
	// rewardRelation.add(usrItem.getPayoff()
	// / (usrItem.getVisit() + 1));
	// countRelation++;
	// }
	// }
	// if (rewardRelation.size() != 0) {
	// rewardRelationVal = ArrayUtils.toPrimitive(rewardRelation
	// .toArray(new Double[rewardRelation.size()]));
	// avgY = UCB1.mean.evaluate(rewardRelationVal);
	// boundY = UCB1.var.evaluate(rewardRelationVal);
	// }
	// // if (avgY > 0 && avgX > 0) {
	// // System.out.println();
	// // }
	// if (Math.abs(avgX - avgY) < Math.abs((boundX - boundY) / 2)) {
	// val = avgY + Environment.alphaUCB * boundY;
	// } else {
	// val = avgX + Environment.alphaUCB * boundX;
	// }
	// } else {
	// val = avgX + Environment.alphaUCB * boundX;
	// }
	//
	// if (val > maxVal) {
	// selectedNode = child;
	// maxVal = val;
	// }
	// }
	// return selectedNode;
	// }

	public int getIndexLeaf() {
		return indexLeaf;
	}

	public void setIndexLeaf(int indexLeaf) {
		this.indexLeaf = indexLeaf;
	}
}
