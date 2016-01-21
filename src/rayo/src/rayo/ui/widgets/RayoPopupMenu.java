package rayo.ui.widgets;

import java.awt.Dimension;

import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;

public class RayoPopupMenu extends JPopupMenu{

	private static final long serialVersionUID = 1L;

	
	@Override
	public Dimension getPreferredSize() {
		Dimension preferred = super.getPreferredSize();
		if (!(getParent() instanceof JMenuBar)) {
			preferred.width = Math.max(preferred.width, 200);
		}
		return preferred;
	}
}
