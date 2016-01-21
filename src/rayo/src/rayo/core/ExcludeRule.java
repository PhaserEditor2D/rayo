package rayo.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcludeRule {

	public static final String EXCLUDE_FILENAME = ".rayo-exclude";

	public static ExcludeRule create(Path path) {
		if (path.getFileName().toString().equals(EXCLUDE_FILENAME)) {
			try {
				return new ExcludeRule(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private Path _path;
	private List<Pattern> _patterns;

	private ExcludeRule(Path path) throws IOException {
		super();

		_path = path;
		_patterns = new ArrayList<>();

		List<String> lines = Files.readAllLines(path);
		for (String line : lines) {
			if (line.startsWith("#")) {
				continue;
			}

			String expr = line.replace(".", "\\.").replace("*", ".*");
			Pattern pattern = Pattern.compile(expr);
			_patterns.add(pattern);
		}
	}

	public Path getPath() {
		return _path;
	}

	public boolean isExcluded(Path path) {
		String name = path.toString().replace("\\", "/");

		for (Pattern re : _patterns) {
			Matcher matcher = re.matcher(name);
			if (matcher.matches()) {
				return true;
			}
		}

		return false;
	}

}
