package org.but4reuse.benchmarks.argoumlspl.actions;

import java.io.File;

import org.but4reuse.benchmarks.argoumlspl.utils.GenerateScenarioResources;
import org.but4reuse.utils.workbench.WorkbenchUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Enable the button to create but4Resources to the ArgoUML SPL
 * Benchmark
 * 
 * @author Nicolas Ordoñez Chala, Jabier Martinez
 *
 */
public class CreateScenariosAction implements IObjectActionDelegate {

	private ISelection selection;

	public CreateScenariosAction() {

	}

	@Override
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection) selection).getFirstElement();
			File file = WorkbenchUtils.getFileFromIResource((IResource) o);
			GenerateScenarioResources genArtefactModelAndFeatureList = new GenerateScenarioResources();
			genArtefactModelAndFeatureList.generateArtefactModelAndFeatureList(file);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

}
