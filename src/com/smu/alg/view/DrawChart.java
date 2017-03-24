package com.smu.alg.view;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author n2t
 */
import java.awt.BasicStroke;
import java.awt.Color;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.smu.linucb.global.AlgorithmType;

//import org.jfree.ui.Spacer;

public class DrawChart extends JFrame {

	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *            the frame title.
	 */
	String title;

	XYSeriesCollection dataset;
	XYSeries xyLinUCB_SIN;
	XYSeries xyLinUCB_IND;
	XYSeries xyLinUCB_TREE;
	XYSeries xyLinUCB_VER;
	XYSeries xyLinUCB_WARM;
	XYSeries xyLinUCB_KMEAN;
	XYSeries xyCLUB;

	public DrawChart(final String title) {
		super(title);
		this.title = title;
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setup();
	}

	public void addData(AlgorithmType type, double x, double y) {
		switch (type) {
		case LINUCB_SIN:
			xyLinUCB_SIN.add(x, y);
			break;
		case LINUCB_IND:
			xyLinUCB_IND.add(x, y);
			break;
		case LINUCB_TREE:
			xyLinUCB_TREE.add(x, y);
			break;
		case LINUCB_VER:
			xyLinUCB_VER.add(x, y);
			break;
		case LINUCB_WARM:
			xyLinUCB_WARM.add(x, y);
			break;
		case LINUCB_KMEAN:
			xyLinUCB_KMEAN.add(x, y);
			break;
		case CLUB:
			xyCLUB.add(x, y);
			break;
		default:
			break;
		}
	}

	public void genDiffConfig(AlgorithmType type) {
		switch (type) {
		case LINUCB_SIN:
			xyLinUCB_SIN = new XYSeries("LinUCB_SIN");
			dataset.addSeries(xyLinUCB_SIN);
			break;
		case LINUCB_IND:
			xyLinUCB_IND = new XYSeries("LinUCB_IND");
			dataset.addSeries(xyLinUCB_IND);
			break;
		case LINUCB_TREE:
			xyLinUCB_TREE = new XYSeries("LINUCB_TREE");
			dataset.addSeries(xyLinUCB_TREE);
			break;
		case LINUCB_VER:
			xyLinUCB_VER = new XYSeries("LINUCB_VER");
			dataset.addSeries(xyLinUCB_VER);
			break;
		case LINUCB_WARM:
			xyLinUCB_WARM = new XYSeries("LINUCB_WARM");
			dataset.addSeries(xyLinUCB_WARM);
			break;
		case LINUCB_KMEAN:
			xyLinUCB_KMEAN = new XYSeries("LINUCB_KMEAN");
			dataset.addSeries(xyLinUCB_KMEAN);
			break;
		case CLUB:
			xyCLUB = new XYSeries("CLUB");
			dataset.addSeries(xyCLUB);
			break;
		default:
			break;
		}
	}

	public void setup() {
		// XYDataset dataset = createDataset();
		dataset = new XYSeriesCollection();
		JFreeChart chart = createChart(dataset);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));
		// this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setContentPane(chartPanel);
	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            the data for the chart.
	 * 
	 * @return a chart.
	 */
	private JFreeChart createChart(final XYDataset dataset) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createXYLineChart(this.title, // chart
				"TIME", // x axis label
				"CUMULATIVE REWARD", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				true, // tooltips
				false // urls
				);

		// // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		// chart.setBackgroundPaint(Color.white);
		//
		// // final StandardLegend legend = (StandardLegend) chart.getLegend();
		// // legend.setDisplaySeriesShapes(true);
		//
		// // get a reference to the plot for further customisation...
		// final XYPlot plot = chart.getXYPlot();
		// plot.setBackgroundPaint(Color.lightGray);
		// // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0,
		// 5.0));
		// plot.setDomainGridlinePaint(Color.white);
		// plot.setRangeGridlinePaint(Color.white);
		//
		// final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		// // renderer.setSeriesLinesVisible(1, false);
		// renderer.setSeriesShapesVisible(1, true);
		// renderer.setSeriesShapesVisible(2, true);
		// plot.setRenderer(renderer);
		//
		// // change the auto tick unit selection to integer units only...
		// final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// OPTIONAL CUSTOMISATION COMPLETED.

		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		// plot.getRenderer().setBaseStroke(new BasicStroke(3));
		plot.getRenderer().setStroke(new BasicStroke(1.5f));
		ValueAxis axis = plot.getDomainAxis();
		axis.setAutoRange(true);
		// axis.setFixedAutoRange(2000.0);
		axis = plot.getRangeAxis();
		axis.setAutoRange(true);
//		axis.setRange(-50.0, 250.0);
		// axis.setRange(-50, 600.0);
		return chart;

	}

	// ****************************************************************************
	// * JFREECHART DEVELOPER GUIDE *
	// * The JFreeChart Developer Guide, written by David Gilbert, is available
	// *
	// * to purchase from Object Refinery Limited: *
	// * *
	// * http://www.object-refinery.com/jfreechart/guide.html *
	// * *
	// * Sales are used to provide funding for the JFreeChart project - please *
	// * support us so that we can continue developing free software. *
	// ****************************************************************************
	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *            ignored.
	 */

}
