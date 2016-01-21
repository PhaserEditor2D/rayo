package rayo.ui.widgets;

import static rayo.ui.widgets.WidgetUtils.setLaf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.fife.rsta.ui.EscapableDialog;

public class ToolWindow extends EscapableDialog  {

	private static final long serialVersionUID = 1L;
	private Component _content;

	public ToolWindow(JFrame owner) {
		super(owner);
		
		setUndecorated(true);
		
		JPanel area = new JPanel(new BorderLayout());
		
		_content = createContent();
		area.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		area.add(_content, BorderLayout.CENTER);
		setContentPane(area);
	}

	public Component getContent() {
		return _content;
	}

	@SuppressWarnings("static-method")
	protected Component createContent() {
		return new JLabel("Override the createContent() method.");
	}

	public static void main(String[] args) {
		setLaf();

		ToolWindow w = new ToolWindow(null);
		w.setSize(400, 600);
		w.setLocation(400, 200);
		w.setVisible(true);
		w.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(1);
			}
		});
	}
}
