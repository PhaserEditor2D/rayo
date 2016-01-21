package rayo.ui.editors.js.autocomplete;

import java.awt.Font;
import java.awt.event.MouseEvent;

import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.rtextarea.RTextArea;

import rayo.core.Project;
import rayo.core.RayoJSDocQuery;
import rayo.core.TernManager;
import rayo.ui.IEditor;
import rayo.ui.Workbench;

public class RayoCompletionProvider extends LanguageAwareCompletionProvider {
	private static final String[] EASING_CONSTANTS = { "Power0", "Power1", "Power2", "Power3", "Power4", "Linear",
			"Quad", "Cubic", "Quart", "Quint", "Sine", "Expo", "Circ", "Elastic", "Back", "Bounce", "Quad.easeIn",
			"Cubic.easeIn", "Quart.easeIn", "Quint.easeIn", "Sine.easeIn", "Expo.easeIn", "Circ.easeIn",
			"Elastic.easeIn", "Back.easeIn", "Bounce.easeIn", "Quad.easeOut", "Cubic.easeOut", "Quart.easeOut",
			"Quint.easeOut", "Sine.easeOut", "Expo.easeOut", "Circ.easeOut", "Elastic.easeOut", "Back.easeOut",
			"Bounce.easeOut", "Quad.easeInOut", "Cubic.easeInOut", "Quart.easeInOut", "Quint.easeInOut",
			"Sine.easeInOut", "Expo.easeInOut", "Circ.easeInOut", "Elastic.easeInOut", "Back.easeInOut",
			"Bounce.easeInOut" };

	public RayoCompletionProvider() {
		setDefaultCompletionProvider(JavaScriptCompletionProvider.getInstance());
		setStringCompletionProvider(createStringCompletionProvider());
	}

	private static DefaultCompletionProvider createStringCompletionProvider() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider(EASING_CONSTANTS);
		provider.setAutoActivationRules(true, "");
		CompletionCellRenderer renderer = new CompletionCellRenderer();
		renderer.setDisplayFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		provider.setListCellRenderer(renderer);
		return provider;
	}

	@Override
	public String getToolTipText(RTextArea textArea, MouseEvent e) {
		int offset = textArea.viewToModel(e.getPoint());

		if (offset == -1) {
			return null;
		}

		TernManager tern = Project.getInstance().getTernManager();
		if (tern.isDisposed()) {
			return null;
		}

		IEditor editor = Workbench.getInstance().getActiveEditor();
		RayoJSDocQuery query = new RayoJSDocQuery(editor);
		query.setOffset(offset);

		tern.queryJSDoc(query);

		if (query.resultDoc == null) {
			return null;
		}

		String html = "<html><font color='black'>" + query.computeHTMLDoc(false);

		return html;
	}
}
