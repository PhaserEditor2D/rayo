package rayo.ui.editors.js.autocomplete;

import static rayo.ui.widgets.WidgetUtils.VARIABLE_ICON;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.VariableCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;

import tern.server.protocol.completions.TernCompletionItem;

public class RayoVarCompletion extends VariableCompletion implements ICompletionConstants {

	public RayoVarCompletion(CompletionProvider provider, TernCompletionItem item) {
		super(provider, item.getName(), RSyntaxUtilities.escapeForHtml(item.getType(), null, true));
		setIcon(VARIABLE_ICON);
		setRelevance(VAR_RELEVANCE);
		setSummary(item.getDoc());
		setShortDescription(item.getDoc());
	}

	@Override
	protected boolean possiblyAddDescription(StringBuilder sb) {
		if (getShortDescription() != null) {
			sb.append("<br><br>");
			sb.append(getShortDescription());
			sb.append("<br><br><br>");
			return true;
		}
		return false;
	}

}
