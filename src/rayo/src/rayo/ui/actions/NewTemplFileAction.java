package rayo.ui.actions;

import static rayo.core.CoreUtils.getCapitalName;
import static rayo.core.CoreUtils.getExtPart;
import static rayo.core.CoreUtils.getJavaIdentifier;
import static rayo.core.CoreUtils.getNamePart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import rayo.core.RayoFile;
import rayo.ui.views.project.ProjectJTree;

public class NewTemplFileAction extends NewFileAction {
	private static final long serialVersionUID = 1L;
	private Path _templFile;

	public NewTemplFileAction(Path templFile, ProjectJTree tree, RayoFile contextFile) {
		super(getNamePart(templFile), getExtPart(templFile), tree, contextFile);
		_templFile = templFile;
	}

	@Override
	protected String createInitialContent(String name) throws IOException {
		String string = new String(Files.readAllBytes(_templFile));
		string = string.replace("$name$", getCapitalName(getJavaIdentifier(getNamePart(name))));
		return string;
	}
}
