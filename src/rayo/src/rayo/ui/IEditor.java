package rayo.ui;

import javax.swing.JComponent;

import rayo.core.RayoFile;

public interface IEditor {

	public String getContent();

	public void setCaretPosition(int position);

	public int getCaretPosition();

	public RayoFile getFile();

	public JComponent getComponent();

	public void focus();
	
	public void activated();
	
	public void closed();

	public void save();

	JComponent getStatusComponent();

	public void updateStatusBar();

	public void refreshProblems();

	public boolean isActive();
}
