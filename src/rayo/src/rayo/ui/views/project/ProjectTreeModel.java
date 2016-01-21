package rayo.ui.views.project;

import javax.swing.tree.DefaultTreeModel;

import rayo.core.Project;

public class ProjectTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1L;

	public ProjectTreeModel() {
		super(Project.getInstance().getProjectFolder());
	}
}
