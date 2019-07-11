package org.but4reuse.benchmarks.argoumlspl.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.but4reuse.artefactmodel.Artefact;
import org.but4reuse.artefactmodel.ArtefactModel;
import org.but4reuse.artefactmodel.ArtefactModelFactory;
import org.but4reuse.featurelist.Feature;
import org.but4reuse.featurelist.FeatureList;
import org.but4reuse.featurelist.FeatureListFactory;
import org.but4reuse.utils.emf.EMFUtils;
import org.but4reuse.utils.files.FileUtils;

/**
 * 
 * Generate the resources for the ArgoUML SPL Benchmark
 * 
 * @author Nicolas Ordoñez Chala
 *
 */
public class GenerateScenarioResources {

	// Artefact Model Factory to generate Artefact Objects
	private ArtefactModelFactory amf = ArtefactModelFactory.eINSTANCE;
	// Create the artefact model
	private ArtefactModel artefactModel;

	// Feature List Factory to generate Feature Objects
	private FeatureListFactory flf = FeatureListFactory.eINSTANCE;
	// Create feature List
	private FeatureList featureList;

	/**
	 * From a folder which contains the file with the descriptions of each
	 * feature obtain a Map to optimize the addition of implementedElements into
	 * the Feature Object
	 * 
	 * @param project
	 *            - Path of the project
	 * @return HashMap where Key is the name of the feature, and its content is
	 *         the Feature object
	 */
	private Map<String, Feature> featuresDescriptionFileToMap(File project) {
		Map<String, Feature> featureMap = new HashMap<String, Feature>();
		try {
			// Get the file where is the description of the features of ArgoUML
			// SPL
			File featureInfo = getFileofFileByName(project, "featuresInfo", 0);

			// Read the folder which contain the description of each feature
			File featuresTXT = getFileofFileByName(featureInfo, "features.txt", 1);

			// Read the file which contain the description of each feature
			List<String> st = FileUtils.getLinesOfFile(featuresTXT);

			// The structure of each line is
			// NAME; lowercase name splitted by comma; Description
			for (String line : st) {
				// Split the line to separate the name and its description
				String[] lines = line.split(";");

				// Create the feature with a name and a description
				Feature f = flf.createFeature();
				f.setName(lines[0]);
				f.setId(lines[0]);
				f.setDescription(lines[2]);
				featureMap.put(lines[0], f);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return featureMap;

	}

	/**
	 * Search and retrieve one file with an specific name and also it can
	 * specific the type of file.
	 * 
	 * @param parentFile
	 *            - Location of the file
	 * @param nameOfFile
	 *            - name of the file of interest
	 * @param typeOfFile
	 *            - 0 Folder, 1 File, other does not matter
	 * @return
	 */
	private File getFileofFileByName(File parentFile, String nameOfFile, int typeOfFile) {

		File fileOfInterest = null;
		try {

			// if typeOfFile is 0 search for folderFiles
			if (typeOfFile == 0) {
				// Get the two folders of interest inside each Scenario Folder
				for (File scenarioSubFolder : parentFile.listFiles()) {
					// We are just interested in the folder which is called
					// configs
					if (scenarioSubFolder.isDirectory() && scenarioSubFolder.getName().equals(nameOfFile))
						fileOfInterest = scenarioSubFolder;
				}
				// if typeOfFile is 1 search for file
			} else if (typeOfFile == 1) {
				// Get the two folders of interest inside each Scenario Folder
				for (File scenarioSubFolder : parentFile.listFiles()) {
					// We are just interested in the folder which is called
					// configs
					if (scenarioSubFolder.isFile() && scenarioSubFolder.getName().equals(nameOfFile))
						fileOfInterest = scenarioSubFolder;
				}
				// otherwise search any kind of file
			} else {
				// Get the two folders of interest inside each Scenario Folder
				for (File scenarioSubFolder : parentFile.listFiles()) {
					// We are just interested in the folder which is called
					// configs
					if (scenarioSubFolder.getName().equals(nameOfFile))
						fileOfInterest = scenarioSubFolder;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileOfInterest;

	}

	/**
	 * Clean the implemented artefacts found in previous scenarios
	 * 
	 * @param featureMap
	 */
	private void cleanFeatureMap(Map<String, Feature> featureMap) {
		try {
			for (String featureName : featureMap.keySet())
				featureMap.get(featureName).getImplementedInArtefacts().clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Based on the scenario folder of argoUML SPL
	 * 
	 * @param scenarioFolder
	 */
	public void generateArtefactModelAndFeatureList(File scenarioFolder) {

		// Get the file of the project
		File benchmarkFolder = scenarioFolder.getParentFile();
		// Create map to optimize the insertion of the implemented elements
		Map<String, Feature> featureMap = featuresDescriptionFileToMap(benchmarkFolder);
		// Get path of the project
		String argoUMLSPLPlatformPath = "platform:/resource/" + benchmarkFolder.getName() + "/scenarios/";
		// Get path of the project
		String argoUMLSPLAbsolutePath = scenarioFolder.getAbsolutePath();
		// Get all the files inside the scenario Folder
		File[] scenarioFile = scenarioFolder.listFiles();

		// Check if the array of Files is not null
		if (scenarioFile != null) {
			for (File scenarioDirectory : scenarioFile) {
				// Check if the scenarioDirectory is a folder
				if (scenarioDirectory.isDirectory()) {

					// Create String with the path of the scenario
					String scenarioAbsolutePath = argoUMLSPLAbsolutePath + "\\" + scenarioDirectory.getName();
					String scenarioPlatformPath = argoUMLSPLPlatformPath + scenarioDirectory.getName();

					// Init the featureList and the artefact model for each
					// scenario
					featureList = flf.createFeatureList();
					artefactModel = amf.createArtefactModel();
					cleanFeatureMap(featureMap);

					// Set the name to the name of the scenario
					featureList.setName(scenarioDirectory.getName());
					artefactModel.setName(scenarioDirectory.getName());
					
					// Set the default adapter
					artefactModel.setAdapters("jdt");

					// // Create the file object for the featureList and
					// artefactModel from absolute
					// path
					File but4ReuseDirectoryResourceAbsolute = new File(scenarioAbsolutePath + "/BUT4Reuse/");

					// Create the file
					FileUtils.deleteFile(but4ReuseDirectoryResourceAbsolute);
					but4ReuseDirectoryResourceAbsolute.mkdir();

					// Search into each scenario for the configs
					// We are just interested in the folder which is called
					// configs
					File configsFolder = getFileofFileByName(scenarioDirectory, "configs", 0);

					// Get all config files and variant folders
					File[] configFile = configsFolder.listFiles();

					for (int i = 0; i < configFile.length; i++) {
						// Get the variant name
						File variant = configFile[i];

						// Create Artefact
						Artefact variantArtefact = amf.createArtefact();
						variantArtefact.setName(variant.getName());

						variantArtefact.setArtefactURI(scenarioPlatformPath + "/variants/" + variant.getName());

						// Modify featureList updating the feature with the
						// implemented element
						// represented into the artefact
						List<String> lines = FileUtils.getLinesOfFile(variant);
						if (lines.size() > 0)
							for (int n = 0; n < lines.size(); n++)
								featureMap.get(lines.get(n)).getImplementedInArtefacts().add(variantArtefact);

						// Add each artefact to the artefact model
						artefactModel.getOwnedArtefacts().add(variantArtefact);
					}

					// Add each feature to the feature List
					for (Feature f : featureMap.values()) {
						if (!f.getImplementedInArtefacts().isEmpty())
							featureList.getOwnedFeatures().add(f);
					}

					// Add the artefactmodel to the featureList
					featureList.setArtefactModel(artefactModel);

					URI afmURI = new File(but4ReuseDirectoryResourceAbsolute,
							scenarioDirectory.getName() + ".artefactmodel").toURI();
					URI flURI = new File(but4ReuseDirectoryResourceAbsolute,
							scenarioDirectory.getName() + ".featurelist").toURI();

					try {
						EMFUtils.saveEObject(afmURI, artefactModel);
						EMFUtils.saveEObject(flURI, featureList);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

			}
		}

	}
}
