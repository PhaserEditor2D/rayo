package rayo.core;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CoreUtils {

	public static String moveTree(File srcFile, File dstFolder) {
		try {
			String fname = srcFile.getName();
			File moveTo = new File(dstFolder, fname);
			if (moveTo.exists()) {
				if (moveTo.isFile()) {
					if (showConfirmDialog(null, "Do you want to override file " + moveTo + "?", "Move", YES_NO_OPTION,
							QUESTION_MESSAGE) == NO_OPTION) {
						return "";
					}
					Files.move(srcFile.toPath(), moveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} else {
					return String.format("<html>Cannot move <b>%s</b> to <b>%s</b>.", projectRelative(srcFile.toPath()),
							projectRelative(dstFolder.toPath()));
				}
			} else {
				Files.move(srcFile.toPath(), moveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			e.printStackTrace();
			showMessageDialog(null, e, "Move", ERROR_MESSAGE);
		}
		return null;
	}

	public static String copyTree(File srcFile, File dstFolder) {
		try {
			String fname = srcFile.getName();
			File copyTo = new File(dstFolder, fname);
			if (copyTo.exists()) {
				if (copyTo.isFile()) {
					if (showConfirmDialog(null, "Do you want to override file " + copyTo + "?", "Copy", YES_NO_OPTION,
							QUESTION_MESSAGE) == NO_OPTION) {
						return "";
					}
					Files.copy(srcFile.toPath(), copyTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} else {
					return String.format("<html>Cannot copy <b>%s</b> to <b>%s</b>.", projectRelative(srcFile.toPath()),
							projectRelative(dstFolder.toPath()));
				}
			} else {
				if (srcFile.isDirectory()) {
					copyTo.mkdir();
					for (File file : srcFile.listFiles()) {
						copyTree(file, copyTo);
					}
				} else {
					Files.copy(srcFile.toPath(), copyTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			showMessageDialog(null, e, "Copy", ERROR_MESSAGE);
		}
		return null;
	}

	public static Path projectRelative(Path path) {
		return Project.getInstance().getProjectFolder().getFilePath().relativize(path);
	}

	public static String getExtPart(Path file) {
		String s = file.getFileName().toString();
		return getExtPart(s);
	}

	public static String getExtPart(String filename) {
		int i = filename.lastIndexOf('.');
		if (i == -1) {
			return "";
		}
		return filename.substring(i + 1);
	}

	public static String getNamePart(Path file) {
		String s = file.getFileName().toString();
		return getNamePart(s);
	}

	public static String getNamePart(String filename) {
		int i = filename.lastIndexOf('.');
		if (i == -1) {
			return filename;
		}

		return filename.substring(0, i);
	}

	public static String getJavaIdentifier(String filename) {
		StringBuilder sb = new StringBuilder();
		for (char c : filename.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)) {
				sb.append(c);
			} else {
				sb.append('_');
			}
		}
		return sb.toString();
	}

	public static String getCapitalName(String name) {
		if (name.length() == 0) {
			return "";
		}
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public static String findNextFileName(Path folder, String name) {
		Path file = folder.resolve(name);
		if (!Files.exists(file)) {
			return name;
		}

		String a = getNamePart(file);
		String b = getExtPart(file);
		if (b.length() > 0) {
			b = "." + b;
		}
		int i = 1;
		while (Files.exists(folder.resolve(a + i + b))) {
			i++;
		}
		return a + i + b;
	}

}
