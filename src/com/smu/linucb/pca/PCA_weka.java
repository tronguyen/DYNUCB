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
