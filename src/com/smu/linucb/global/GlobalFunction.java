/**
*  Copyright 2014 Singapore Management University (SMU). All Rights Reserved. 
*
* Permission to use, copy, modify and distribute this software and 
* its documentation for purposes of research, teaching and general
* academic pursuits, without fee and without a signed licensing
* agreement, is hereby granted, provided that the above copyright
* statement, this paragraph and the following paragraph on disclaimer
* appear in all copies, modifications, and distributions.  Contact
* Singapore Management University, Intellectual Property Management
* Office at iie@smu.edu.sg, for commercial licensing opportunities.
*
* This software is provided by the copyright holder and creator "as is"
* and any express or implied warranties, including, but not Limited to,
* the implied warranties of merchantability and fitness for a particular 
* purpose are disclaimed.  In no event shall SMU or the creator be 
* liable for any direct, indirect, incidental, special, exemplary or 
* consequential damages, however caused arising in any way out of the
* use of this software.
*/

package com.smu.linucb.global;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;

public class GlobalFunction {
	public static void addValueMap(Map<Integer, List<Integer>> objMap, int key,
			int value) {
		if (objMap.containsKey(key)) {
			objMap.get(key).add(value);
		} else {
			List<Integer> initLst = new ArrayList<Integer>();
			initLst.add(value);
			objMap.put(key, initLst);
		}
	}

	public static void addValueMapSet(Map<Integer, Set<Integer>> objMap,
			int key, int value) {
		if (objMap.containsKey(key)) {
			objMap.get(key).add(value);
		} else {
			Set<Integer> initLst = new HashSet<Integer>();
			initLst.add(value);
			objMap.put(key, initLst);
		}
	}

	public static void sumValueMap(Map<Integer, Double> objMap, int key,
			double value) {
		if (objMap.containsKey(key)) {
			objMap.put(key, objMap.get(key) + value);
		} else {
			objMap.put(key, value);
		}
	}

	public static void delValueMap(Map<Integer, Set<Integer>> objMap, int key,
			int value) {
		objMap.get(key).remove(value);
	}

	public static double[] convert2DoubleArr(SimpleMatrix mx) {
		double[] out = new double[Environment.featureSize];
		for (int i = 0; i < Environment.featureSize; i++) {
			out[i] = mx.get(i);
		}
		return out;
	}

	public static void writeOutGraph(File f, int[][] graph, int graphSize) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			StringBuffer bs = new StringBuffer();
			for (int i = 0; i < graphSize; i++) {
				for (int j = 0; j < graphSize; j++) {
					if (graph[i][j] == 1) {
						bs.append("\t" + j);
					}
				}
				bw.write(bs.toString().trim() + "\n");
				bw.flush();
				bs.setLength(0);
			}

			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int[][] readInGraph(File f, int graphSize) {
		int[][] graph = new int[graphSize][graphSize];
		String line = "";
		String[] items;
		Set<Integer> connectedOrder;
		int rowCount = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			while ((line = br.readLine()) != null) {
				connectedOrder = new HashSet<Integer>(
						Arrays.asList(convert2ArrInt(line.trim().split("\t"))));
				for (int colCount = 0; colCount < graphSize; colCount++) {
					graph[rowCount][colCount] = (connectedOrder
							.contains(colCount)) ? 1 : 0;
				}
				rowCount++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return graph;
	}

	private static Integer[] convert2ArrInt(String[] items) {
		Integer[] results = new Integer[items.length];
		if (items[0].equals(""))
			return results;
		for (int i = 0; i < items.length; i++) {
			results[i] = Integer.parseInt(items[i]);
		}
		return results;
	}
}
