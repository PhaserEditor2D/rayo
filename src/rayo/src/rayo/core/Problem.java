package rayo.core;

import static rayo.ui.widgets.WidgetUtils.ERROR_ICON;
import static rayo.ui.widgets.WidgetUtils.WARNING_ICON;

import javax.swing.Icon;

public class Problem {
	public enum Severity {
		ERROR(ERROR_ICON), WARNING(WARNING_ICON);

		private Icon _icon;

		private Severity(Icon icon) {
			_icon = icon;
		}

		public Icon getIcon() {
			return _icon;
		}
	}

	private String _message;
	private String _id;
	private Severity _severity;
	private int _from;
	private int _to;
	private RayoFile _file;
	private int _line;

	public Problem(RayoFile file) {
		_file = file;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public RayoFile getFile() {
		return _file;
	}

	public String getMessage() {
		return _message;
	}

	public void setMessage(String message) {
		_message = message;
	}

	public Severity getSeverity() {
		return _severity;
	}

	public void setSeverity(Severity severity) {
		_severity = severity;
	}

	public int getFrom() {
		return _from;
	}

	public void setFrom(int from) {
		_from = from;
	}

	public int getTo() {
		return _to;
	}

	public void setTo(int to) {
		_to = to;
	}

	public void setLine(int line) {
		_line = line;
	}

	public int getLine() {
		return _line;
	}

}
