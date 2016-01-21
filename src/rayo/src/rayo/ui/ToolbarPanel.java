package rayo.ui;

import static javax.swing.BorderFactory.createEmptyBorder;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ToolbarPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public ToolbarPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(createEmptyBorder(2, 2, 2, 2));
		try {
			ImageIcon icon = new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/icons/serve.png")));
			AbstractAction action = new AbstractAction("", icon) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO: missing server action
				}
			};
			JButton btn = new JButton(action);
			btn.setPreferredSize(new Dimension(32, 32));
			add(btn);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
