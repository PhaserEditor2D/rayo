package rayo.ui.laf;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.SynthTableUI;

public class RayoTableUI extends SynthTableUI {
	public static ComponentUI createUI(JComponent c) {
		return new RayoTableUI();
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();
		table.setRowHeight(25);
	}
}
