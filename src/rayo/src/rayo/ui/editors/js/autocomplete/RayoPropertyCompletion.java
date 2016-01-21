package rayo.ui.editors.js.autocomplete;

import static rayo.ui.widgets.WidgetUtils.FIELD_ICON;
import static rayo.ui.widgets.WidgetUtils.PROPERTY_ICON;

import org.fife.ui.autocomplete.VariableCompletion;

import phasereditor.inspect.core.jsdoc.IPhaserMember;
import phasereditor.inspect.core.jsdoc.JSDocRenderer;
import phasereditor.inspect.core.jsdoc.PhaserJSDoc;
import tern.server.protocol.completions.TernCompletionItem;

public class RayoPropertyCompletion extends VariableCompletion implements ICompletionConstants {
	private TernCompletionItem _item;
	private IPhaserMember _member;

	public RayoPropertyCompletion(JavaScriptCompletionProvider provider, TernCompletionItem item) {
		super(provider, item.getName(), item.getJsType());

		setRelevance(PROPERTY_RELEVANCE);
		setIcon(PROPERTY_ICON);

		_item = item;
		String itemdoc = item.getDoc();
		if (itemdoc != null) {
			PhaserJSDoc phaserDoc = PhaserJSDoc.getInstance();
			_member = null;
			_member = phaserDoc.getMember(itemdoc);

			if (_member == null) {
				setSummary(itemdoc);
				setShortDescription(itemdoc);
			} else {
				setIcon(FIELD_ICON);
				setRelevance(CONSTANT_RELEVANCE);
				setSummary(_member.getHelp());
				setShortDescription(_member.getHelp());
			}
		}
	}

	public IPhaserMember getMember() {
		return _member;
	}

	public TernCompletionItem getItem() {
		return _item;
	}

	@Override
	public String getSummary() {
		if (_member != null) {
			return JSDocRenderer.getInstance().render(_member, Integer.MAX_VALUE);
		}
		return super.getSummary();
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
