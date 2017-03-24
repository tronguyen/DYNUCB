package com.smu.linucb.algorithm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

import com.smu.control.AlgorithmThreadBuilder;
import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalSQLQuery;
import com.smu.linucb.preprocessing.Dbconnection;

class IndItem {
	private SimpleMatrix M = null;
	private SimpleMatrix b = null;
	private int clusterIndex = -1;
	private LinUCB linucb = null;
	private SimpleMatrix MOld = null;
	private SimpleMatrix bOld = null;
	private SimpleMatrix theta = null;
	private SimpleMatrix thetaOld = null;
	private Set<Integer> connectedUserSet = null;
	public int order;

	public IndItem() {
		M = SimpleMatrix.identity(Environment.featureSize);
		b = new SimpleMatrix(Environment.featureSize, 1);
		setMOld(SimpleMatrix.identity(Environment.featureSize));
		setbOld(new SimpleMatrix(Environment.featureSize, 1));
		theta = M.invert().mult(b);
		thetaOld = M.invert().mult(b); 
//		setTheta(new SimpleMatrix(Environment.featureSize, 1));
//		setThetaOld(new SimpleMatrix(Environment.featureSize, 1));
	}

	public SimpleMatrix getM() {
		return M;
	}

	public void setM(SimpleMatrix m) {
		M = m;
	}

	public SimpleMatrix getB() {
		return b;
	}

	public void setB(SimpleMatrix b) {
		this.b = b;
	}

	public int getClusterIndex() {
		return clusterIndex;
	}

	public void setClusterIndex(int clusterIndex) {
		this.clusterIndex = clusterIndex;
	}

	public LinUCB getLinucb() {
		return linucb;
	}

	public void setLinucb(LinUCB linucb) {
		this.linucb = linucb;
	}

	public SimpleMatrix getMOld() {
		return MOld;
	}

	public void setMOld(SimpleMatrix mOld) {
		MOld = mOld;
	}

	public SimpleMatrix getbOld() {
		return bOld;
	}

	public void setbOld(SimpleMatrix bOld) {
		this.bOld = bOld;
	}

	public SimpleMatrix getTheta() {
		return theta;
	}

	public void setTheta(SimpleMatrix theta) {
		this.theta = theta;
	}

	public SimpleMatrix getThetaOld() {
		return thetaOld;
	}

	public void setThetaOld(SimpleMatrix thetaOld) {
		this.thetaOld = thetaOld;
	}

	public Set<Integer> getConnectedUserSet() {
		return connectedUserSet;
	}

	public void setConnectedUserSet(Set<Integer> connectedUserSet) {
		this.connectedUserSet = connectedUserSet;
	}
}

public class LinUCB extends AlgorithmThreadBuilder {

	private Dbconnection dbconn;

	private SimpleMatrix M;
	private SimpleMatrix b;
	private SimpleMatrix theta;
	private DenseMatrix64F X;
	private List<Integer> bmLst;

	private SimpleMatrix I, avgM, avgb;

	private int user;
	private double payoff = 0;

	protected int sampleCol = 0;

	public int getUser() {
		return user;
	}

	public void setUser(int user) {
		this.user = user;
	}

	/**
	 * @param args
	 */
	public LinUCB() {
		M = SimpleMatrix.identity(Environment.featureSize);
		b = new SimpleMatrix(Environment.featureSize, 1);
		theta = new SimpleMatrix(Environment.featureSize, 1);
		X = new DenseMatrix64F(Environment.featureSize,
				Environment.numContextVecs);
		bmLst = new ArrayList<Integer>();
		dbconn = Dbconnection._getConn();
	}

	public LinUCB(int mode) {
		I = SimpleMatrix.identity(Environment.featureSize);
		avgM = SimpleMatrix.identity(Environment.featureSize);
		avgb = new SimpleMatrix(Environment.featureSize, 1);
		theta = new SimpleMatrix(Environment.featureSize, 1);
		X = new DenseMatrix64F(Environment.featureSize,
				Environment.numContextVecs);
		bmLst = new ArrayList<Integer>();
		dbconn = Dbconnection._getConn();
	}

	public void reset() {
		bmLst.clear();
		// Reset sample index
		this.sampleCol = 0;
		X.zero();
	}

	public void resetICML() {
		bmLst.clear();
		// Reset sample index
		this.sampleCol = 0;
		X.zero();
		avgb.zero();
		avgM = SimpleMatrix.identity(Environment.featureSize);
		theta = new SimpleMatrix(Environment.featureSize, 1);
	}

	private void addSample(Double[] sampleData) {
		for (int i = 0; i < sampleData.length; i++) {
			X.set(i, sampleCol, sampleData[i]);
		}
		sampleCol++;
	}

	private int genRandomBM(int exp) {
		int randBM;
		int bm;
		do {
			randBM = rBM.nextInt(Environment.bmidLst.size());
			bm = Environment.bmidLst.get(randBM);
		} while (bm == exp || Environment.removedBM.contains(bm));
		return bm;
	}

	/*
	 * Pick randomly 24 bookmarks 1- Read norm matrix from file 2- Random choose
	 * 24 vectors from that 3- Select one of true bookmarks of the user
	 */
	protected void impl() {

		try {
			// Pick randomly 1 true-bookmark
			List<Integer> lsTrueBM = dbconn.getBookmark4User(
					GlobalSQLQuery.GETSUGGESTION4USER, this.getUser());

			int selectedBM = lsTrueBM.get(rBM.nextInt(lsTrueBM.size()));
			Double[] seletedBMVal = Environment.normMatrix.get(selectedBM);
			addSample(seletedBMVal);
			bmLst.add(selectedBM);

			// Pick 24 vectors
			int randBM;
			for (int i = 0; i < Environment.numContextVecs - 1; i++) {
				randBM = genRandomBM(selectedBM);
				addSample(Environment.normMatrix.get(randBM));
				bmLst.add(randBM);
			}

			// Core LINUCB
			theta = M.invert().mult(b);
			DenseMatrix64F temp = new DenseMatrix64F(
					Environment.numContextVecs, Environment.featureSize);
			DenseMatrix64F temp2 = new DenseMatrix64F(
					Environment.numContextVecs, Environment.numContextVecs);
			CommonOps.multTransA(X, M.invert().getMatrix(), temp);
			CommonOps.mult(temp, X, temp2);
			DenseMatrix64F diag = new DenseMatrix64F(1,
					Environment.numContextVecs);
			CommonOps.extractDiag(temp2, diag);
			// Get square root of diag vector
			for (int sq = 0; sq < Environment.numContextVecs; sq++) {
				double sqVal = Environment.alphaLin * Math.sqrt(diag.get(sq));
				diag.set(sq, sqVal);
			}

			// Matrix result with each column corresponding to each bookmark
			SimpleMatrix p = theta.transpose().mult(SimpleMatrix.wrap(X))
					.plus(SimpleMatrix.wrap(diag));
			double max = Double.NEGATIVE_INFINITY;
			int resBM = 0, k = 0;
			// Suggest bookmark (k value) for user
			for (k = 0; k < Environment.numContextVecs; k++) {
				if (max < p.getMatrix().get(k)) {
					resBM = this.bmLst.get(k);
					max = p.getMatrix().get(k);
				}
			}
//			System.out.println("---BM list: "
//					+ Arrays.toString(this.bmLst.toArray()));
//			System.out.println("---Suggestion: " + resBM);
//			System.out.println("---True BM: " + selectedBM);
			/*
			 * Compare with user's choice = 1: for right one = -1: for wrong one
			 */

			payoff = (resBM == selectedBM) ? Environment.payoffRight
					: Environment.payoffWrong;
			// Update matrix M, b
			Double[] suggestedBM = Environment.normMatrix.get(resBM);
			SimpleMatrix suggestedBMVec = SimpleMatrix.wrap(new DenseMatrix64F(
					Environment.featureSize, 1, true, ArrayUtils
							.toPrimitive(suggestedBM)));
			M = M.plus(suggestedBMVec.mult(suggestedBMVec.transpose()));
			CommonOps.scale(payoff, suggestedBMVec.getMatrix());
			b = b.plus(suggestedBMVec);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void implICML(Set<Integer> itemSet,
			Map<Integer, IndItem> userItemMap, int time) {
		try {
			// Pick randomly 1 true-bookmark
			List<Integer> lsTrueBM = dbconn.getBookmark4User(
					GlobalSQLQuery.GETSUGGESTION4USER, this.getUser());

			int selectedBM = lsTrueBM.get(rBM.nextInt(lsTrueBM.size()));
			Double[] seletedBMVal = Environment.normMatrix.get(selectedBM);
			addSample(seletedBMVal);
			bmLst.add(selectedBM);

			// Pick 24 vectors
			int randBM;
			for (int i = 0; i < Environment.numContextVecs - 1; i++) {
				randBM = genRandomBM(selectedBM);
				addSample(Environment.normMatrix.get(randBM));
				bmLst.add(randBM);
			}

			// Core LINUCB
			IndItem userIt = null;
			for (Integer i : itemSet) {
				userIt = userItemMap.get(i);
				avgM = avgM.plus(userIt.getM().minus(I));
				avgb = avgb.plus(userIt.getB());
			}
			theta = avgM.invert().mult(avgb);
			DenseMatrix64F temp = new DenseMatrix64F(
					Environment.numContextVecs, Environment.featureSize);
			DenseMatrix64F temp2 = new DenseMatrix64F(
					Environment.numContextVecs, Environment.numContextVecs);
			CommonOps.multTransA(X, avgM.invert().getMatrix(), temp);
			CommonOps.mult(temp, X, temp2);
			DenseMatrix64F diag = new DenseMatrix64F(1,
					Environment.numContextVecs);
			CommonOps.extractDiag(temp2, diag);
			CommonOps.scale(Math.log(time + 1), diag);
			// Get square root of diag vector
			for (int sq = 0; sq < Environment.numContextVecs; sq++) {
				double sqVal = Environment.alphaLin * Math.sqrt(diag.get(sq));
				diag.set(sq, sqVal);
			}

			// Matrix result with each column corresponding to each bookmark
			SimpleMatrix p = theta.transpose().mult(SimpleMatrix.wrap(X))
					.plus(SimpleMatrix.wrap(diag));
			double max = Double.NEGATIVE_INFINITY;
			int resBM = 0, k = 0;
			// Suggest bookmark (k value) for user
			for (k = 0; k < Environment.numContextVecs; k++) {
				if (max < p.getMatrix().get(k)) {
					resBM = this.bmLst.get(k);
					max = p.getMatrix().get(k);
				}
			}
			// System.out.println("---BM list: "
			// + Arrays.toString(this.bmLst.toArray()));
			// System.out.println("---Suggestion: " + resBM);
			// System.out.println("---True BM: " + selectedBM);
			/*
			 * Compare with user's choice 1: for right one; -1/24: for wrong one
			 */

			payoff = (resBM == selectedBM) ? Environment.payoffRight
					: Environment.payoffWrong;
			// Update matrix M, b
			Double[] suggestedBM = Environment.normMatrix.get(resBM);
			SimpleMatrix suggestedBMVec = SimpleMatrix.wrap(new DenseMatrix64F(
					Environment.featureSize, 1, true, ArrayUtils
							.toPrimitive(suggestedBM)));
			userIt = userItemMap.get(this.getUser());

			// Keep the old value
			userIt.setMOld(userIt.getM());
			userIt.setbOld(userIt.getB());
			userIt.setThetaOld(userIt.getTheta());
			// Update new value for user
			userIt.setM(userIt.getM().plus(
					suggestedBMVec.mult(suggestedBMVec.transpose())));
			CommonOps.scale(payoff, suggestedBMVec.getMatrix());
			userIt.setB(userIt.getB().plus(suggestedBMVec));
			userIt.setTheta(userIt.getM().invert().mult(userIt.getB()));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected double getPayoff() {
		return this.payoff;
	}

}
