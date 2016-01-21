package rayo.core;

import java.util.ArrayList;

import rayo.core.Problem.Severity;
import tern.server.protocol.IJSONObjectHelper;
import tern.server.protocol.lint.ITernLintCollector;

public class LintQuery extends RayoQuery implements ITernLintCollector {

	public LintQuery(RayoFile file) {
		super(file, 0);
	}

	@Override
	public void startLint(String file) {
		getFile().setProblems(new ArrayList<>());
	}

	@Override
	public void endLint(String filename) {
		// RayoFile file = getFile();
		// out.println(file.getFilePath().getFileName() + ": found " +
		// file.getProblems().size() + " problems");
	}

	@Override
	public void addMessage(String messageId, String message, Long start, Long end, Long lineNumber, String severity,
			String filename, Object messageObject, IJSONObjectHelper helper) {
		RayoFile file = getFile();
		Problem problem = new Problem(file);
		problem.setId(messageId);
		problem.setMessage(message);
		problem.setSeverity(Severity.valueOf(severity.toUpperCase()));
		problem.setFrom(start == null ? 0 : start.intValue());
		problem.setTo(end == null ? 0 : end.intValue());
		problem.setLine(lineNumber == null ? 0 : lineNumber.intValue());
		file.getProblems().add(problem);
	}

}
