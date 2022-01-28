package org.but4reuse.benchmarks.argoumlspl.fl.results.visualisation;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.but4reuse.adaptedmodel.AdaptedModel;
import org.but4reuse.benchmarks.argoumlspl.utils.TransformFLResultsToBenchFormat;
import org.but4reuse.feature.location.LocatedFeature;
import org.but4reuse.feature.location.LocatedFeaturesManager;
import org.but4reuse.featurelist.FeatureList;
import org.but4reuse.utils.emf.EMFUtils;
import org.but4reuse.utils.files.FileUtils;
import org.but4reuse.utils.workbench.WorkbenchUtils;
import org.but4reuse.visualisation.IVisualisation;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * ArgoUML SPL Visualisation that creates the resulting files of the feature
 * location technique
 * 
 * @author Nicolas Ordoñez Chala
 */
public class ArgoUMLSPLFeatureLocationResultsVisualisation implements IVisualisation {

	FeatureList featureList;
	AdaptedModel adaptedModel;

	@Override
	public void prepare(FeatureList featureList, AdaptedModel adaptedModel, Object extra, IProgressMonitor monitor) {
		this.featureList = featureList;
		this.adaptedModel = adaptedModel;
	}

	@Override
	public void show() {
		// Only if the featureList have been filled previously
		if (featureList != null && featureList.getName() != null) {

			// Get the artefact model
			IResource res = EMFUtils
					.getIResource(adaptedModel.getOwnedAdaptedArtefacts().get(0).getArtefact().eResource());

			// Create the file reference of the artefact model
			File artefactModelFile = WorkbenchUtils.getFileFromIResource(res);

			// Get your results package
			// TODO find a better way
			boolean isArgoUMLSPLBenchmark = artefactModelFile.getParentFile().getParentFile().getName()
					.contains("cenario");
			File yourResults = generateFolderFromArtefactModel(artefactModelFile, isArgoUMLSPLBenchmark);

			// Generate the files according to the ground truth
			generateFLResultsWithGroundTruthFormat(yourResults);

			// Refresh
			WorkbenchUtils.refreshIResource(res.getParent());
		}
	}

	/**
	 * Create the folder where it will be saved the answer.
	 * 
	 * @param artefactModelFile
	 * @param argoUMLSPLBenchmark - if true, then it will be save into the structure
	 *                            of argoUMLSPL Benchmark. Otherwise saved in the
	 *                            parent folder
	 * @return
	 */
	private File generateFolderFromArtefactModel(File artefactModelFile, boolean argoUMLSPLBenchmark) {
		File answer = null;
		try {
			if (argoUMLSPLBenchmark)
				// Look into the structure of argoUML for the folder yourResult
				answer = new File(artefactModelFile.getParentFile().getParentFile().getParentFile().getParentFile(),
						"yourResults");
			else
				// Create the folder inside the parent director of the artefact model
				answer = new File(artefactModelFile.getParent(), "benchmarkResults");

			// If not exists then create
			if (!answer.exists())
				answer.mkdir();
			else {
				FileUtils.deleteFile(answer);
				answer.mkdir();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return answer;
	}
	
	/**
	 * For each feature in the feature List get the elements and create the
	 * necessary files to visualize the files in the argoUMLSPL Benchmark structure
	 * 
	 * @param yourResults - Folder where will be created the txt files per feature
	 */
	private void generateFLResultsWithGroundTruthFormat(File yourResults) {
		// Get the located features
		List<LocatedFeature> locatedFeatures = LocatedFeaturesManager.getLocatedFeatures();

		// Create the Map where it is going to be save the content of the plain text
		// per feature
		Map<String, Set<String>> benchmarkResults = TransformFLResultsToBenchFormat.transform(featureList, adaptedModel, locatedFeatures);

		TransformFLResultsToBenchFormat.serializeResults(yourResults, benchmarkResults);
	}

}
