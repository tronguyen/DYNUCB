package com.smu.linucb.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import com.smu.linucb.global.AlgorithmType;
import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalFunction;

class X {
	private int a;

	public X() {
		a = 1;
	}

	protected void sum(int usr) {
		a = a + usr;
		System.out.println("value: " + a);
	}
}

public class MiniFunc extends X {

	/**
	 * @param args
	 */
	public MiniFunc(int k) {
		super();
		sum(k);
	}

	BufferedReader br;
	BufferedWriter bw;
	String s = "";
	String[] line;
	Map<Integer, Double> res = new TreeMap<Integer, Double>();
	int iter;

	public void mergeFile() throws IOException {
		File f = new File(Environment.RW2FILE_WARM);
		File[] fLst = f.listFiles(new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
				return fileName.matches("\\_[\\d]+");
			}
		});
		for (File fin : fLst) {
			br = new BufferedReader(new FileReader(fin));
			while ((s = br.readLine()) != null) {
				line = s.split("\\|");
				GlobalFunction.sumValueMap(res, Integer.valueOf(line[0]),
						Double.valueOf(line[1]));
			}
			br.close();
			fin.delete();
		}
		bw = new BufferedWriter(new FileWriter(new File(
				Environment.RW2FILE_WARM + "_merged_" + Environment.alphaUCB)));
		for (Iterator<Integer> it = res.keySet().iterator(); it.hasNext();) {
			iter = it.next();
			bw.write(iter + "\t" + res.get(iter) / Environment.numAvgLoop
					+ "\n");
		}
		bw.flush();
		bw.close();
	}

	public static void avgALGBandit() throws IOException {
		String fileAdd = "/Volumes/DATA/OnCloud/Dropbox/_shared/CIKM_Output/Delicious_10RUNS/";
		String add;
		Map<Integer, Double> algMap = new TreeMap<Integer, Double>();
		String line;
		String[] items;
		AlgorithmType algtype = AlgorithmType.LINUCB_SIN;
		BufferedReader br;
		String stuff = " [0.55]";
		for (int k = 1; k <= Environment.numAvgLoop; k++) {
			add = fileAdd + "TRY" + k + "/" + algtype;
			br = new BufferedReader(new FileReader(new File(add)));
			while ((line = br.readLine()) != null) {
				items = line.split("\t");
				GlobalFunction.sumValueMap(algMap, Integer.parseInt(items[0]),
						Double.parseDouble(items[1]));

			}
			br.close();
		}
		add = fileAdd + "AVG_" + algtype;
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(add)));
		for (Entry<Integer, Double> entry : algMap.entrySet()) {
			System.out.println(entry.getKey() + "/" + entry.getValue());
			bw.write(entry.getKey() + "\t" + entry.getValue()
					/ Environment.numAvgLoop + "\n");
			bw.flush();
		}
		bw.flush();
		bw.close();
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		avgALGBandit();

		// try {
		// new MiniFunc().mergeFile();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// EuclideanDistance ed = new EuclideanDistance();
		// double[] x = new double[2];
		// double[] y = new double[2];
		// x[0] = 1;
		// x[1] = 0;
		// y[0] = 0;
		// y[1] = 1;
		// for (double k = 0; k < 1; k += 0.03) {
		// System.out.println(ed.compute(x, y) + " --- " + k);
		// }
		// MiniFunc A = new MiniFunc(1);
		// MiniFunc B = new MiniFunc(2);
		// A.sum(1);
		// B.sum(1);
		// Random r = new Random();
		// for (int i = 0; i < 100; i++)
		// System.out.println(r.nextDouble());
	}

}
