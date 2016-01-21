package rayo.core;

import phasereditor.inspect.core.jsdoc.IPhaserMember;
import phasereditor.inspect.core.jsdoc.JSDocRenderer;
import phasereditor.inspect.core.jsdoc.PhaserJSDoc;
import phasereditor.inspect.core.jsdoc.PhaserType;
import rayo.ui.IEditor;

public class RayoJSDocQuery extends RayoQuery {

	public String resultDoc;
	public String resultType;

	public RayoJSDocQuery(IEditor editor) {
		super(editor);
	}

	public String computeHTMLDoc(boolean fullDetails) {
		PhaserJSDoc phaserDoc = PhaserJSDoc.getInstance();
		if (resultDoc == null) {
			return "<html><br>&nbsp;&nbsp;&nbsp;&nbsp;<i>Documentation not found.</i>";
		}

		IPhaserMember member = phaserDoc.getMember(resultDoc);
		if (member == null) {
			StringBuilder html = new StringBuilder();
			html.append("<html>");
			if (resultType != null) {
				html.append("<b>" + resultType + "</b>");
			}
			html.append("<p>" + resultDoc + "</p>");
			return html.toString();
		}

		JSDocRenderer renderer = JSDocRenderer.getInstance();
		String html;
		if (member instanceof PhaserType) {
			html = renderer.renderType((PhaserType) member, Integer.MAX_VALUE, true, fullDetails);
		} else {
			html = renderer.render(member, Integer.MAX_VALUE);
		}
		return html;
	}

}
