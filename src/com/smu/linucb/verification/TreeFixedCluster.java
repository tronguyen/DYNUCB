package com.smu.linucb.verification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import com.smu.control.AlgorithmThreadBuilder;
import com.smu.linucb.algorithm.LinUCB_TREE;
import com.smu.linucb.global.AlgorithmType;
import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalFunction;
import com.smu.linucb.global.GlobalSQLQuery;
import com.smu.linucb.preprocessing.Dbconnection;

/*
 * 1: K-mean to cluster users
 * 2: Apply LinUCB_SIN with fixed clusters
 */
public class TreeFixedCluster extends AlgorithmThreadBuilder {
	private static SimpleKMeans kmean;
	private static Instances dataset;
	private boolean warmStart;

	public TreeFixedCluster(boolean warmStart) {
		this.warmStart = warmStart;
	}

	private static Instance createInstance(List<Integer> bookmarkLst) {
		Instance inst = null;
		Double[] tagVal;
		double[] avgTagVec = new double[Environment.featureSize];
		for (int bm : bookmarkLst) {
			tagVal = Environment.normMatrix.get(bm);
			// Calculate average tags
			for (int i = 0; i < Environment.featureSize; i++) {
				avgTagVec[i] += tagVal[i] / bookmarkLst.size();
			}
		}
		inst = new Instance(1, avgTagVec);
		return inst;
	}

	public static void doCluster() {
		kmean = new SimpleKMeans();
		Instance instance;
		List<Integer> lsTrueBM;
		Attribute attr = null;
		int cluster, user;
		try {
			getKmean().setNumClusters(Environment.numCluster);
			FastVector attrs = new FastVector();
			dataset = new Instances("my_dataset", attrs, 0);
			for (int i = 0; i < Environment.featureSize; i++) {
				attr = new Attribute(String.valueOf(i));
				attrs.addElement(attr);
			}

			// Build instances
			for (int usr : Environment.userLst) {
				lsTrueBM = Dbconnection._getConn().getBookmark4User(
						GlobalSQLQuery.GETSUGGESTION4USER, usr);
				instance = createInstance(lsTrueBM);
				getDataset().add(instance);
			}
			getKmean().buildClusterer(getDataset());

			// Instances centroids = kmean.getClusterCentroids();
			// for (int i = 0; i < centroids.numInstances(); i++) {
			// System.out.println(centroids.instance(i).dataset()
			// .numInstances());
			// }

			// Put users into their cluster
			for (int i = 0; i < getDataset().numInstances(); i++) {
				// System.out.print((getDataset().instance(i)));
				// System.out.print(" is in cluster ");
				// System.out.print(getKmean().clusterInstance(
				// getDataset().instance(i))
				// + 1 + "\n");
				user = Environment.userLst.get(i);
				cluster = getKmean().clusterInstance(getDataset().instance(i));
				Environment.usrClusterMap.put(user, cluster);
				GlobalFunction.addValueMap(Environment.clusterMap, cluster,
						user);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		LinUCB_TREE alg;
		// Enable fixedCluster mode --> true
		alg = new LinUCB_TREE();
		alg.setInClass(this.getInClass());
		// Make noise original data
//		alg.setIncludedErr(true);
		if (!this.warmStart) {
			alg.setFixedCluster(true);
			alg.setAlgType(AlgorithmType.LINUCB_VER);
		} else {
			alg.setWarmStart(true);
			alg.setAlgType(AlgorithmType.LINUCB_WARM);
		}
		alg.start();
		this.interrupt();
	}

	public static SimpleKMeans getKmean() {
		return kmean;
	}

	public static Instances getDataset() {
		return dataset;
	}

}
