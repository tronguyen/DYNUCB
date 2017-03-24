package com.smu.linucb.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.smu.linucb.global.AlgorithmType;
import com.smu.linucb.global.Environment;
import com.smu.linucb.global.GlobalSQLQuery;

public class LinUCB_SIN extends LinUCB {

	// public static int time = 0;
	private double rewardTotal = 0;

	public LinUCB_SIN() {
		// TODO Auto-generated constructor stub
		this.setAlgType(AlgorithmType.LINUCB_SIN);
		fileAdd = fileAddCommon + this.getAlgType();
	}

	@Override
	public void run() {
		// Environment.drChart.genDiffConfig(AlgorithmType.LINUCB_SIN);
		// TODO Auto-generated method stub
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					fileAdd)));

			for (int i = 1; i <= Environment.limitTime; i++) {
				// Pick user randomly
				this.setUser(Environment.userLst.get(rUSR
						.nextInt(Environment.userLst.size())));
				// System.out.println("User: " + this.getUser());
				this.impl();
				this.reset();
				this.rewardTotal += this.getPayoff();

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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.interrupt();
	}
}
