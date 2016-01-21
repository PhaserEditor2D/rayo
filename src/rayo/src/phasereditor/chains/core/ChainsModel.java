// The MIT License (MIT)
//
// Copyright (c) 2015 Arian Fornaris
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to permit
// persons to whom the Software is furnished to do so, subject to the
// following conditions: The above copyright notice and this permission
// notice shall be included in all copies or substantial portions of the
// Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
// NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
// USE OR OTHER DEALINGS IN THE SOFTWARE.
package phasereditor.chains.core;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import phasereditor.inspect.core.jsdoc.PhaserConstant;
import phasereditor.inspect.core.jsdoc.PhaserJSDoc;
import phasereditor.inspect.core.jsdoc.PhaserMethod;
import phasereditor.inspect.core.jsdoc.PhaserMethodArg;
import phasereditor.inspect.core.jsdoc.PhaserType;
import phasereditor.inspect.core.jsdoc.PhaserVariable;

public class ChainsModel {
	private static ChainsModel _instance;

	public static ChainsModel getInstance() {
		if (_instance == null) {
			_instance = new ChainsModel();
		}
		return _instance;
	}

	private ArrayList<ChainItem> _chains;
	private List<String> _examplesFiles;
	private List<Line> _examplesLines;
	private PhaserJSDoc _jsdoc;

	private ChainsModel() {
		_jsdoc = PhaserJSDoc.getInstance();
		build();
	}

	private void build() {
		_chains = new ArrayList<>();

		buildChain("Phaser.Game", _chains, 0, 3, null);

		for (PhaserType unit : _jsdoc.getTypes()) {
			String name = unit.getName();

			if (!name.equals("Phaser.Game")) {
				buildChain(name, _chains, 0, 2, null);
			}
		}

		// global constants
		for (PhaserConstant cons : _jsdoc.getGlobalConstants()) {
			String name = cons.getName();
			String type = cons.getTypes()[0];
			String chain = "const Phaser." + name;
			_chains.add(new ChainItem(cons, chain, type, 0));
		}

		// sort data
		_chains.sort(new Comparator<ChainItem>() {

			@Override
			public int compare(ChainItem a, ChainItem b) {
				if (a.getDepth() != b.getDepth()) {
					return a.getDepth() - b.getDepth();
				}

				boolean a_phaser = a.getChain().contains("Phaser");
				boolean b_phaser = b.getChain().contains("Phaser");

				if (a_phaser != b_phaser) {
					return a_phaser ? -1 : 1;
				}

				a_phaser = a.getReturnTypeName().contains("Phaser");
				b_phaser = b.getReturnTypeName().contains("Phaser");

				if (a_phaser != b_phaser) {
					return a_phaser ? -1 : 1;
				}

				int a_type_weight = a.isType() ? 0 : countDots(a.getReturnTypeName());
				int b_type_weight = b.isType() ? 0 : countDots(b.getReturnTypeName());

				if (a_type_weight != b_type_weight) {
					return (a_type_weight - b_type_weight);
				}

				return 0;
			}
		});

		buildExamples();

	}

	private void buildExamples() {
		_examplesFiles = new ArrayList<>();
		_examplesLines = new ArrayList<>();

		Path path = Paths.get("data/phaser-examples-index.json");
		try (InputStream input = Files.newInputStream(path)) {
			JSONObject doc = new JSONObject(new JSONTokener(input));
			JSONArray files = doc.getJSONArray("files");
			for (int i = 0; i < files.length(); i++) {
				_examplesFiles.add(files.getString(i));
			}

			JSONArray lines = doc.getJSONArray("lines");
			for (int i = 0; i < lines.length(); i++) {
				JSONObject obj = lines.getJSONObject(i);
				Line line = new Line();
				line.text = obj.getString("t");
				line.linenum = obj.getInt("l");
				line.filename = _examplesFiles.get(obj.getInt("f"));
				_examplesLines.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showMessageDialog(null, e, "Chains", ERROR_MESSAGE);
		}
	}

	public List<Match> searchChains(String aQuery, int limit) {
		String query = aQuery.toLowerCase();

		if (query.startsWith("this.")) {
			query = "state." + query.substring(5);
		}

		List<Match> matches = new ArrayList<>();
		query = quote(query);
		Pattern pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);

		for (ChainItem item : _chains) {
			Matcher matcher = pattern.matcher(item.getDisplay());
			if (matcher.matches()) {
				Match match = new Match();
				match.item = item;
				match.start = matcher.start(1);
				match.length = matcher.end(1) - match.start;
				matches.add(match);
				if (matches.size() == limit) {
					break;
				}
			}
		}
		return matches;
	}

	private static String quote(String query) {
		String patternStart = query.startsWith("*") ? "" : ".*";
		String patternEnd = query.endsWith("*") ? "" : ".*";

		String pattern = query.replace(".", "\\.").replace("*", ".*").replace("(", "\\(").replace(")", "\\)")
				.replace(":", "\\:");

		return patternStart + "(" + pattern + ")" + patternEnd;
	}

	public List<Match> searchExamples(String aQuery, int limit) {
		String query = aQuery.toLowerCase();
		List<Match> matches = new ArrayList<>();
		if (query.length() > 2) {
			query = quote(query);
			Pattern pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);

			// search of file names

			for (String filename : _examplesFiles) {
				Matcher matcher = pattern.matcher(filename);
				if (matcher.matches()) {
					Match match = new Match();
					match.item = filename;
					match.start = matcher.start(1);
					match.length = matcher.end(1) - match.start;
					matches.add(match);
					if (matches.size() == limit) {
						break;
					}
				}
			}

			// search on lines

			for (Line line : _examplesLines) {
				Matcher matcher = pattern.matcher(line.text);
				if (matcher.matches()) {
					Match match = new Match();
					match.item = line;
					match.start = matcher.start(1);
					match.length = matcher.end(1) - match.start;
					matches.add(match);
					if (matches.size() == limit) {
						break;
					}
				}
			}
		}
		return matches;
	}

	public boolean isPhaserType(String typeName) {
		return _jsdoc.getTypesMap().containsKey(typeName);
	}

	static int countDots(String s) {
		int n = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '.') {
				n++;
			}
		}
		return n;
	}

	Set<String> _usedTypes = new HashSet<>();

	private void buildChain(String className, List<ChainItem> chains, int currentDepth, int depth, String aPrefix) {
		if (currentDepth == depth) {
			return;
		}

		PhaserType unit = _jsdoc.getType(className);

		if (unit == null) {
			return;
		}

		// constructor
		if (!_usedTypes.contains(className)) {
			// class
			{
				String chain = "class " + className + (unit.getExtends().isEmpty() ? "" : " extends");
				int i = 0;
				for (String e : unit.getExtends()) {
					chain += (i == 0 ? " " : "|") + e;
					i++;
				}
				_chains.add(new ChainItem(unit, chain, className, 0));
			}

			// constructor
			{
				String chain = "new " + className + "(";
				int i = 0;
				for (PhaserMethodArg arg : unit.getConstructorArgs()) {
					chain += (i > 0 ? "," : "") + arg.getName();
					i++;
				}
				chain += ")";

				_chains.add(new ChainItem(unit, chain, className, 0));
			}
			_usedTypes.add(className);
		}

		String prefix = aPrefix == null ? className : aPrefix;

		// properties

		for (PhaserVariable prop : unit.getProperties()) {
			for (String type : prop.getTypes()) {
				String name = prop.getName();
				String chain = prefix + "." + name;
				chains.add(new ChainItem(prop, chain, type, currentDepth));
				buildChain(type, chains, currentDepth + 1, depth, chain);
			}
		}

		// constants

		for (PhaserVariable cons : unit.getConstants()) {
			String name = cons.getName();
			String type = cons.getTypes()[0];
			String chain = prefix + "." + name;
			chains.add(new ChainItem(cons, chain, type, currentDepth));
		}

		// methods

		for (PhaserMethod method : unit.getMethods()) {
			String[] methodTypes = method.getReturnTypes();

			if (methodTypes.length == 0) {
				methodTypes = new String[] { "void" };
			}

			for (String type : methodTypes) {
				String name = method.getName();
				String chain = prefix + "." + name + "(";
				int i = 0;
				for (PhaserVariable param : method.getArgs()) {
					if (i > 0) {
						chain += ", ";
					}
					chain += param.getName();
					i++;
				}
				chain += ")";

				chains.add(new ChainItem(method, chain, type, currentDepth));
			}
		}
	}

	public ArrayList<ChainItem> getChains() {
		return _chains;
	}
}
