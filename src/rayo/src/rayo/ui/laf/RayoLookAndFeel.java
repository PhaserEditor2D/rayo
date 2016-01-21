package rayo.ui.laf;

import javax.swing.UIDefaults;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class RayoLookAndFeel extends NimbusLookAndFeel {
	private static final long serialVersionUID = 1L;

	public RayoLookAndFeel() {
	}
	
	@Override
	public String getName() {
		return "Rayo";
	}

	@Override
	public String getID() {
		return "Rayo";
	}

	@Override
	protected void initComponentDefaults(UIDefaults table) {
		super.initComponentDefaults(table);
	}

	@Override
	protected void initClassDefaults(UIDefaults table) {
		super.initClassDefaults(table);

		table.putDefaults(new Object[] { "TableUI", "rayo.ui.laf.RayoTableUI" });
	}
}
