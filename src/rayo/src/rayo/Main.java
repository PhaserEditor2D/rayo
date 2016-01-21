package rayo;

import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import rayo.ui.Workbench;

public class Main {
	public static void main(String[] args) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(TimeUnit.MINUTES.toMillis(3));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					JOptionPane.showMessageDialog(null, "This is an evaluation product.", "Evaluation",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}).start();

		Workbench workbench = new Workbench();
		workbench.start();
	}
}
