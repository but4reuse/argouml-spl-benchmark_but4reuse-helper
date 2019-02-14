package org.but4reuse.feature.location.sfs_lsi;

import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adaptedmodel.AdaptedModel;
import org.but4reuse.adaptedmodel.Block;
import org.but4reuse.adaptedmodel.helpers.AdaptedModelHelper;
import org.but4reuse.adapters.IElement;
import org.but4reuse.feature.location.IFeatureLocation;
import org.but4reuse.feature.location.LocatedFeature;
import org.but4reuse.feature.location.LocatedFeaturesUtils;
import org.but4reuse.feature.location.impl.StrictFeatureSpecificFeatureLocation;
import org.but4reuse.feature.location.lsi.impl.ApplyLSI;
import org.but4reuse.featurelist.Feature;
import org.but4reuse.featurelist.FeatureList;
import org.eclipse.core.runtime.IProgressMonitor;

public class SFS_LSI implements IFeatureLocation {
	@Override
	public List<LocatedFeature> locateFeatures(FeatureList featureList, AdaptedModel adaptedModel,
			IProgressMonitor monitor) {

		// Get SFS results, all located feature are 1 confidence
		StrictFeatureSpecificFeatureLocation sfs = new StrictFeatureSpecificFeatureLocation();
		List<LocatedFeature> sfsLocatedBlocks = sfs.locateFeatures(featureList, adaptedModel, monitor);

		List<LocatedFeature> locatedFeatures = new ArrayList<LocatedFeature>();

		// Get all the features of a given block and all its elements
		for (Block block : adaptedModel.getOwnedBlocks()) {
			// user cancel
			if (monitor.isCanceled()) {
				return locatedFeatures;
			}

			monitor.subTask("Feature location FCA SFS and LSI. Features competing for Elements at " + block.getName()
					+ " /" + adaptedModel.getOwnedBlocks().size());
			List<Feature> blockFeatures = LocatedFeaturesUtils.getFeaturesOfBlock(sfsLocatedBlocks, block);
			List<IElement> blockElements = AdaptedModelHelper.getElementsOfBlock(block);

			// Calculate LSI in each block
			ApplyLSI flsi = new ApplyLSI();
			List<LocatedFeature> lfs = flsi.locateFeaturesFromAnotherTechnique(block, blockFeatures, blockElements);
			if(lfs != null && !lfs.isEmpty()) {
				locatedFeatures.addAll(lfs);
			}

		}
		return locatedFeatures;
	}
}