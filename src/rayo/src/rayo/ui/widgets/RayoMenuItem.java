package rayo.ui.widgets;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.JMenuItem;

public class RayoMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;

	public RayoMenuItem(Action action) {
		super(action);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension preferred = super.getPreferredSize();
		preferred.width = Math.max(preferred.width, 200);
		return preferred;
	}
}
