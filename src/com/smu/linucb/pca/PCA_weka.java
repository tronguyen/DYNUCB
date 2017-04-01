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

package com.smu.linucb.pca;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.ArffSaver;

public class PCA_weka {
	public static void reduceDimension(File fin, File fout) throws Exception{
//		Ranker ranker = new Ranker();
//		InfoGainAttributeEval ig = new InfoGainAttributeEval();
//		Instances instances = SamplesManager.asWekaInstances(trainSet);
//		ig.buildEvaluator(instances);
//		firstAttributes = ranker.search(ig,instances);
//		candidates = Arrays.copyOfRange(firstAttributes, 0, FIRST_SIZE_REDUCTION);
//		instances = reduceDimenstions(instances, candidates);
		
		// Read matrix from arff file
		BufferedReader br = new BufferedReader(new FileReader(fin));
		ArffReader arff = new ArffReader(br);
		Instances data = arff.getData();
		
		PrincipalComponents pca = new PrincipalComponents();
		pca.setCenterData(false);
		pca.setMaximumAttributeNames(25);
		
		pca.buildEvaluator(data);
		Instances output = pca.transformedData(data);
		ArffSaver arffSave = new ArffSaver();
		arffSave.setInstances(output);
		arffSave.setFile(fout);
//		arffSave.setDestination(fout);
		arffSave.writeBatch();
		
//		ranker = new Ranker();
//		ranker.setNumToSelect(numFeatures);
//		selection = new AttributeSelection();
//		selection.setEvaluator(pca);
//		selection.setSearch(ranker);
//		selection.SelectAttributes(instances );
//		instances = selection.reduceDimensionality(wekaInstances);
	}
}
