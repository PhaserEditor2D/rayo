package rayo.ui.widgets;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class RayoMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	public RayoMenu() {
		super();
	}

	public RayoMenu(Action a) {
		super(a);
	}

	public RayoMenu(String s, boolean b) {
		super(s, b);
	}

	public RayoMenu(String s) {
		super(s);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension preferred = super.getPreferredSize();
		if (!(getParent() instanceof JMenuBar)) {
			preferred.width = Math.max(preferred.width, 200);
		}
		return preferred;
	}

	@Override
	public JMenuItem add(Action a) {
		return super.add(new RayoMenuItem(a));
	}
}
