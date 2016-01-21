package rayo.ui.views.jsdoc;

import static javax.swing.BorderFactory.createEmptyBorder;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.html.HTMLEditorKit;

import org.fife.ui.rsyntaxtextarea.focusabletip.TipUtil;

import phasereditor.inspect.core.jsdoc.IPhaserMember;
import phasereditor.inspect.core.jsdoc.JSDocRenderer;
import phasereditor.inspect.core.jsdoc.PhaserType;
import rayo.core.RayoJSDocQuery;
import rayo.ui.IDocView;

public class JSDocView extends JPanel implements IDocView {

	private static final long serialVersionUID = 1L;
	private JEditorPane _editorPane;

	public JSDocView() {
		setLayout(new BorderLayout());
		setBackground(TipUtil.getToolTipBackground());
		_editorPane = new JEditorPane();
		_editorPane.setBorder(createEmptyBorder(10, 10, 10, 10));
		_editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		_editorPane.setEditorKit(new HTMLEditorKit());
		_editorPane.setEditable(false);
		TipUtil.tweakTipEditorPane(_editorPane);
		_editorPane.setText(
				"<html>Put the caret on a symbol and press <code>F4</code>.");

		add(new JScrollPane(_editorPane), BorderLayout.CENTER);
	}

	@Override
	public void display(RayoJSDocQuery query) {
		String html = query.computeHTMLDoc(true);
		display(html);
	}

	@Override
	public void display(IPhaserMember member) {
		JSDocRenderer render = JSDocRenderer.getInstance();
		String html;
		if (member instanceof PhaserType) {
			html = render.renderType((PhaserType) member, Integer.MAX_VALUE, true, true);
		} else {
			html = render.render(member, Integer.MAX_VALUE);
		}
		display(html);
	}

	private void display(String html) {
		_editorPane.setText(html);
		_editorPane.setCaretPosition(0);

		JTabbedPane parent = (JTabbedPane) getParent();
		parent.setSelectedComponent(this);
	}
}
