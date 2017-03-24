package com.smu.linucb.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import com.smu.linucb.global.AlgorithmType;
import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalSQLQuery;

public class LinUCB_IND extends LinUCB {

	private Map<Integer, LinUCB_IND_impl> banditLst;
	private double rewardTotal = 0;
	private String fileAdd;

	// public static int time = 0;

	public LinUCB_IND() {
		this.setAlgType(AlgorithmType.LINUCB_IND);
		fileAdd = fileAddCommon + this.getAlgType();
		banditLst = new HashMap<Integer, LinUCB_IND_impl>();
	}

	private LinUCB_IND_impl getUserBandit(int usr) {
		LinUCB_IND_impl ucb = this.banditLst.get(usr);
		if (ucb == null) {
			ucb = new LinUCB_IND_impl(usr);
			this.banditLst.put(usr, ucb);
		}
		return ucb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.smu.control.ALGControl#active() distribute thread for each user
	 */

	@Override
	public void run() {
		int usr;
		LinUCB_IND_impl r;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					fileAdd)));
			// Environment.drChart.genDiffConfig(AlgorithmType.LINUCB_IND);
			for (int i = 1; i <= Environment.limitTime; i++) {
				// Pick user randomly
				usr = Environment.userLst.get(rUSR.nextInt(Environment.userLst
						.size()));
				// System.out.println("User: " + usr);
				r = this.getUserBandit(usr);
				r.run_nonthread();
				this.rewardTotal += r.getPayoff();
				// Draw chart
				// this.displayResult(i, this.rewardTotal);
				// this.updateRewardMap(this.getInClass(), i, this.rewardTotal);
				if ((i % Environment.buffSizeDisplay) == 0) {
					// Draw chart
					this.displayResult(i, this.rewardTotal, this.getDrChart());
					bw.write(i + "\t" + this.rewardTotal + "\n");
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

	// public void active() {
	//
	// // Iterate users from data
	// // for (Iterator<Integer> i = Environment.userLst.iterator();;
	// // i.hasNext()) {
	// // LinUCB_IND r = new LinUCB_IND(i.next());
	// // System.out.println("User: " + r.getUser());
	// // r.run();
	// // }
	// this.run();
	// }
}
