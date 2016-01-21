package rayo.ui.editors.js.autocomplete;

import static rayo.ui.widgets.WidgetUtils.getCodeFont;

import java.awt.Color;

import javax.swing.JList;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;

import phasereditor.inspect.core.jsdoc.IPhaserMember;
import phasereditor.inspect.core.jsdoc.PhaserMethod;
import phasereditor.inspect.core.jsdoc.PhaserType;
import phasereditor.inspect.core.jsdoc.PhaserVariable;

public class RayoCompletionCellRenderer extends CompletionCellRenderer {
	private static final long serialVersionUID = 1L;

	public RayoCompletionCellRenderer() {
		setParamColor(Color.lightGray);
		setTypeColor(Color.lightGray);
		setShowTypes(true);
		setDisplayFont(getCodeFont());
	}

	@Override
	protected void prepareForFunctionCompletion(JList list, FunctionCompletion fc, int index, boolean selected,
			boolean hasFocus) {
		super.prepareForFunctionCompletion(list, fc, index, selected, hasFocus);

		RayoFunctionCompletion rayoCompl = (RayoFunctionCompletion) fc;
		IPhaserMember member = rayoCompl.getMember();
		if (member instanceof PhaserMethod) {
			PhaserType type = member.getDeclType();
			StringBuilder sb = new StringBuilder(getText());
			if (selected) {
				sb.append(" - " + type.getName());
			} else {
				sb.append(" <font color='gray'>- " + type.getName() + "</font>");
			}
			setText(sb.toString());
		}
	}

	@Override
	protected void prepareForVariableCompletion(JList list, VariableCompletion vc, int index, boolean selected,
			boolean hasFocus) {
		super.prepareForVariableCompletion(list, vc, index, selected, hasFocus);

		if (vc instanceof RayoPropertyCompletion) {
			PhaserVariable variable = (PhaserVariable) ((RayoPropertyCompletion) vc).getMember();
			if (variable != null) {
				StringBuilder sb = new StringBuilder(getText());
				PhaserType type = variable.getDeclType();
				if (type != null) {
					if (selected) {
						sb.append(" - " + type.getName());
					} else {
						sb.append(" <font color='gray'>- " + type.getName() + "</font>");
					}
				}
				setText(sb.toString());
			}
		}
	}

	@Override
	protected void prepareForOtherCompletion(JList list, Completion c, int index, boolean selected, boolean hasFocus) {
		super.prepareForOtherCompletion(list, c, index, selected, hasFocus);

		if (c instanceof RayoVarCompletion) {
			String html = ((RayoVarCompletion) c).getSummary();
			setText(html);
		}
	}
}
