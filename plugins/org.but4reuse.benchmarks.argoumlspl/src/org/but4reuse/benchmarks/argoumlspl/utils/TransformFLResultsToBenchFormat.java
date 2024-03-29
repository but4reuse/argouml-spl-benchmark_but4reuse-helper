package org.but4reuse.benchmarks.argoumlspl.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.but4reuse.adaptedmodel.AdaptedModel;
import org.but4reuse.adaptedmodel.Block;
import org.but4reuse.adaptedmodel.helpers.AdaptedModelHelper;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.javajdt.elements.FieldElement;
import org.but4reuse.adapters.javajdt.elements.ImportElement;
import org.but4reuse.adapters.javajdt.elements.MethodBodyElement;
import org.but4reuse.adapters.javajdt.elements.MethodElement;
import org.but4reuse.adapters.javajdt.elements.TypeElement;
import org.but4reuse.adapters.javajdt.elements.TypeExtendsElement;
import org.but4reuse.adapters.javajdt.elements.TypeImplementsElement;
import org.but4reuse.feature.location.LocatedFeature;
import org.but4reuse.feature.location.LocatedFeaturesUtils;
import org.but4reuse.featurelist.Feature;
import org.but4reuse.featurelist.FeatureList;
import org.but4reuse.utils.files.FileUtils;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Transform the result of the feature location technique into the benchmark
 * format
 * 
 * @author Nicolas Ordoñez Chala
 */
public class TransformFLResultsToBenchFormat {

	// Ground-truth format
	private static final String AND_FEATURE_NAMES = "_and_";
	private static final String NOT_FEATURE_NAMES = "not_";
	public static final String REFINEMENT = " Refinement";
	private static final String LINESPACE = "\n";
	
	// Regular expression to determine if one line is method
	public static final String METHOD_REGEX = "(";

	public static Map<String, Set<String>> transform(FeatureList featureList, AdaptedModel adaptedModel,
			List<LocatedFeature> locatedFeatures) {
		// Create the Map where it is going to be save the content of the plain text
		// per feature
		Map<String, Set<String>> benchmarkResults = new HashMap<>();

		// Map <Method Declaration, Size of the original method body>
		Map<String, Integer> originalMethodBody = new HashMap<>();

		List<IElement> elementsAllBlocks = AdaptedModelHelper.getElementsFromAllBlocks(adaptedModel);
		originalMethodBody = getOriginalMethodElement(elementsAllBlocks);

		// Put the calculated feature locations in one file per feature
		for (Feature feature : featureList.getOwnedFeatures()) {

			// Get the elements where each feature is present
			// Located features can have elements and/or blocks
			List<Block> blocksFeature = LocatedFeaturesUtils.getBlocksOfFeature(locatedFeatures, feature);
			List<IElement> totalElementsFeature = AdaptedModelHelper.getElementsOfBlocks(blocksFeature);
			List<IElement> elementsOfFeature = LocatedFeaturesUtils.getElementsOfFeature(locatedFeatures, feature);
			if (!elementsOfFeature.isEmpty()) {
				totalElementsFeature.addAll(elementsOfFeature);
			}

			// Save the benchmark answer into a Map
			Set<String> valuableElements = generateSetOfValuableElements(totalElementsFeature, originalMethodBody);

			// Create the name of the file based on the features
			String fileName = "";
			// Check if the feature has interactions
			if (feature.getInteractionFeatureOf().size() > 0) {
				Set<String> orderedNames = new TreeSet<>();
				// Get all the features that have interacted in the feature object
				for (Feature interactedFeature : feature.getInteractionFeatureOf()) {
					// if (!FeatureListHelper.isCoreFeature(am, feature)) {
					orderedNames.add(interactedFeature.getId());
					// }
				}

				// Add the features found to a String with its names
				for (String orderedFeatures : orderedNames) {
					fileName += orderedFeatures + AND_FEATURE_NAMES;
				}
				// remove last "and"
				fileName = fileName.substring(0, fileName.length() - AND_FEATURE_NAMES.length());
				// Check if it is a negation feature
			} else if (feature.getNegationFeatureOf() != null) {
				fileName = NOT_FEATURE_NAMES + feature.getNegationFeatureOf().getId();
			} else {
				fileName = feature.getId();
			}
			benchmarkResults.put(fileName, valuableElements);
		}
		
		return benchmarkResults;
	}

	/**
	 * Count the number of lines -\n- in String
	 * 
	 * @param str
	 * @return
	 */
	private static int countLines(String str) {
		String[] lines = str.split("\r\n|\r|\n");
		return lines.length;
	}

	/**
	 * Get the number of lines of each method body to know which one do reference to
	 * the original method body element in the SPL
	 * 
	 * @param elementsAllBlocks
	 * @return
	 */
	private static Map<String, Integer> getOriginalMethodElement(List<IElement> elementsAllBlocks) {
		Map<String, Integer> originalMethodBody = new HashMap<String, Integer>();
		try {
			for (IElement e : elementsAllBlocks) {

				if (e instanceof MethodBodyElement) {

					// Save the text of the MethodDeclaration
					MethodElement me = ((MethodElement) ((MethodBodyElement) e).getDependencies().get("methodBody")
							.get(0));

					String content = TraceIdUtils.getId((MethodDeclaration) me.node);
					if (content != null && !content.isEmpty()) {
						Integer actualNumberOfLines = countLines(((MethodBodyElement) e).body);

						// Get the size of the original method body
						if (!originalMethodBody.containsKey(content)) {
							originalMethodBody.put(content, actualNumberOfLines);
						}

						// Evaluate the size of the actual method body
						if (actualNumberOfLines < originalMethodBody.get(content)) {
							originalMethodBody.put(content, actualNumberOfLines);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return originalMethodBody;
	}

	/**
	 * Create a set with the lines of interest for the Benchmark
	 * 
	 * @param List of elements per feature
	 * @return
	 */
	private static Set<String> generateSetOfValuableElements(List<IElement> elements,
			Map<String, Integer> originalMethodBody) {
		Set<String> answer = new HashSet<String>();
		try {
			// Look into each IElement per feature
			for (IElement e : elements) {
				// if the element is type class declaration
				if (e instanceof TypeElement) {
					// Save the text that is going to be save in the plain text
					// String content = ((TypeElement) e).id;
					String content = TraceIdUtils.getId((TypeDeclaration) ((TypeElement) e).node);
					// Analyze if the content refinement has to be added
					alwaysDeleteRefinement(answer, content);
				}

				// if the element is type implement modifier
				else if (e instanceof TypeImplementsElement) {
					// Save the text that is going to be save in the plain text
					String content = ((TypeImplementsElement) e).type.id;
					// Use it when TypeImplementsDeclaration will be available
					// String content =
					// TraceIdUtils.getId((TypeImplementsDeclaration)((TypeImplementsElement)
					// e).node);
					ifContentIsNotInsertRefinementTag(answer, content);
				}

				// if the element is type extends modifier
				else if (e instanceof TypeExtendsElement) {
					// Save the text that is going to be save in the plain text
					String content = ((TypeExtendsElement) e).type.id;
					// Use it when TypeExtendsDeclaration will be available
					// String content =
					// TraceIdUtils.getId((TypeExtendsDeclaration)((TypeExtendsElement) e).node);
					// Analyze if the content refinement has to be added
					ifContentIsNotInsertRefinementTag(answer, content);
				}

				// if the element is type method element
				else if (e instanceof MethodElement) {
					// Save the text that is going to be save in the plain text
					// String content = ((MethodElement) e).id;
					String content = TraceIdUtils.getId((MethodDeclaration) ((MethodElement) e).node);
					// Analyze if the content refinement has to be added
					alwaysDeleteRefinement(answer, content);

				}

				// if the element is type method body
				else if (e instanceof MethodBodyElement) {

					// Save the text of the MethodDeclaration
					MethodElement me = ((MethodElement) ((MethodBodyElement) e).getDependencies().get("methodBody")
							.get(0));

					String content = TraceIdUtils.getId((MethodDeclaration) ((MethodElement) me).node);
					if (content != null && !content.isEmpty()) {
						// Evaluate the size of the actual method body
						int actualNumberOfLines = countLines(((MethodBodyElement) e).body);
						if (actualNumberOfLines != originalMethodBody.get(content)) {
							ifContentIsNotInsertRefinementTag(answer, content);
						}
					}
				}

				// if the element is import element
				else if (e instanceof ImportElement) {
					String[] structure = ((ImportElement) e).id.split(" ");
					// Use when getID allows ImportDeclaration
					// String content = TraceIdUtils.getId((ImportDeclaration)((ImportElement)
					// e).node);
					String className = structure[1].replace(".java", "");
					String content = structure[0] + "." + className;

					// Analyze if the content refinement has to be added
					ifContentIsNotInsertRefinementTag(answer, content);
				}
				// Get variable declaration
				else if (e instanceof FieldElement) {
					String content = "";
					if (e.getDependencies().containsKey("type")) {
						TypeElement elementInterest = (TypeElement) e.getDependencies().get("type").get(0);
						// content = elementInterest.id;
						content = TraceIdUtils.getId((TypeDeclaration) (elementInterest).node);
					} else if (e.getDependencies().containsKey("method")) {
						MethodElement elementInterest = (MethodElement) e.getDependencies().get("method").get(0);
						// content = elementInterest.id;
						content = TraceIdUtils.getId((MethodDeclaration) (elementInterest).node);
					}

					// Analyze if the content refinement has to be added
					ifContentIsNotInsertRefinementTag(answer, content);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<String> sortedList = new ArrayList<>(answer);
		Collections.sort(sortedList);
		answer = new LinkedHashSet<>(sortedList);

		return answer;
	}

	/**
	 * Analyze the content to determine if it has to be added or not to the plain
	 * text.
	 * 
	 * @param answer  - representation of the plain text as a Set
	 * @param content - Content that has to be added to the plain text
	 */
	private static void alwaysDeleteRefinement(Set<String> answer, String content) {
		try {
			// Check if the content + refinement is already added
			if (answer.contains(content + REFINEMENT)) {
				// if was added then remove it
				answer.remove(content + REFINEMENT);
				// added without tag
				answer.add(content);
			}
			// if in other hand, it had not content + refinement, check if
			// the content is already added. Also check if content is empty
			else if (content != null && !content.isEmpty() && !answer.contains(content)) {
				// if is not in the plain text, then add
				answer.add(content);
			}
			// if the content is in the plain text do nothing
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Analyze the content to determine if it has to be added or not to the plain
	 * text.
	 * 
	 * @param answer  - representation of the plain text as a Set
	 * @param content - Content that has to be added to the plain text
	 */
	private static void ifContentIsNotInsertRefinementTag(Set<String> answer, String content) {
		try {
			// Check if the content is not already added, that is to say
			// that the class declaration had not been founded yet
			if (!answer.contains(content)) {
				// if not, added
				answer.add(content + REFINEMENT);
			}
			// else do nothing because it means that the class declaration
			// modify the refinement tag
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void serializeResults(File resultsFolder, Map<String, Set<String>> benchmarkResults) {
		try {
			for (String f : benchmarkResults.keySet()) {

				// Check if there are class declaration then
				// it has to delete every method declared
				// TODO This is part of TransformFLResultsToBenchFormat
				StringBuilder content = checkClassWithoutRefinement(benchmarkResults.get(f));

				// Do not create a txt file if content is empty
				if (content.length() == 0) {
					continue;
				}

				// Create the txt file object for each feature
				File file = new File(resultsFolder, f + ".txt");
				FileUtils.createFile(file);
				FileUtils.writeFile(file, content.toString());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return a StringBuilder without the methods that are inside an entire class
	 * tag
	 * 
	 * @param feature - Lines found inside each feature
	 * @return StringBuilder
	 */
	private static StringBuilder checkClassWithoutRefinement(Set<String> feature) {
		StringBuilder content = new StringBuilder();
			if (feature.size() != 0) {
				String lastClassWithoutRefinementTag = "//";
				for (String line : feature) {
					// Review the last class found without refinement because
					// it is a tree set and it stores
					// lines in alphabetical order then if it finds new 
					// class without refinement it wont find methods with
					// previous class founded
					if (!line.contains(lastClassWithoutRefinementTag)) {
						// If the line is not a method and also is not refinement
						if (!line.contains(METHOD_REGEX) && !line.contains(REFINEMENT)) {
							// it means that is a class without refinement
							lastClassWithoutRefinementTag = line;
						}
						content.append(line + LINESPACE);
					}
				}
				// Delete the last linespace added
				content.setLength(content.length() - LINESPACE.length());
			}
		return content;
	}

}
