package rayo.ui;

import static java.lang.System.out;
import static rayo.ui.widgets.WidgetUtils.CLASS_ICON;
import static rayo.ui.widgets.WidgetUtils.ERROR_ICON;
import static rayo.ui.widgets.WidgetUtils.FIELD_ICON;
import static rayo.ui.widgets.WidgetUtils.METHOD_ICON;
import static rayo.ui.widgets.WidgetUtils.PROPERTY_ICON;
import static rayo.ui.widgets.WidgetUtils.WARNING_ICON;
import static rayo.ui.widgets.WidgetUtils.createOverlayIcon;

import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import phasereditor.inspect.core.jsdoc.PhaserConstant;
import phasereditor.inspect.core.jsdoc.PhaserMethod;
import phasereditor.inspect.core.jsdoc.PhaserProperty;
import phasereditor.inspect.core.jsdoc.PhaserType;
import rayo.core.Problem;
import rayo.core.Problem.Severity;
import rayo.core.RayoFile;

public class Icons {
	public static Icon getPhaserMemberIcon(Object member) {
		if (member instanceof PhaserMethod || member == PhaserMethod.class) {
			return METHOD_ICON;
		}

		if (member instanceof PhaserProperty || member == PhaserProperty.class) {
			return PROPERTY_ICON;
		}

		if (member instanceof PhaserConstant || member == PhaserConstant.class) {
			return FIELD_ICON;
		}

		if (member instanceof PhaserType || member == PhaserType.class) {
			return CLASS_ICON;
		}

		return null;
	}

	private static ImageIcon _fileErrorIcon;
	private static ImageIcon _fileWarningIcon;

	public static Icon getFileIcon(RayoFile file) {
		if (file.isFolder()) {
			return UIManager.getIcon("Tree.openIcon");
		}

		Icon fileIcon = getFileIcon();

		List<Problem> problems = file.getProblems();
		if (!problems.isEmpty()) {

			ensureFileIcons();

			rayo.core.Problem.Severity severity = Severity.WARNING;
			for (Problem p : problems) {
				if (p.getSeverity() == Severity.ERROR) {
					severity = Severity.ERROR;
				}
			}

			if (severity == Severity.ERROR) {
				fileIcon = _fileErrorIcon;
			} else {
				fileIcon = _fileWarningIcon;
			}
		}

		return fileIcon;
	}

	public static Icon getFileIcon() {
		return UIManager.getIcon("Tree.leafIcon");
	}

	private static void ensureFileIcons() {
		if (_fileErrorIcon != null) {
			return;
		}

		Icon icon = getFileIcon();
		_fileErrorIcon = createOverlayIcon(icon, ERROR_ICON);
		_fileWarningIcon = createOverlayIcon(icon, WARNING_ICON);
	}
}
