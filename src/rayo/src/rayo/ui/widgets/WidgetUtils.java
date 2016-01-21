package rayo.ui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class WidgetUtils {

	public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
	public static final Color DARK_COLOR = new Color(0, 0, 0, 50);
	public static final Color LIGHT_COLOR = new Color(255, 255, 255, 20);
	public static ImageIcon FOLDER_ICON;
	public static ImageIcon CLOSE_ICON;
	public static ImageIcon METHOD_ICON;
	public static ImageIcon PROPERTY_ICON;
	public static ImageIcon CLASS_ICON;
	public static ImageIcon VARIABLE_ICON;
	public static ImageIcon FIELD_ICON;
	public static ImageIcon PROBLEMS_ICON;
	public static ImageIcon FIND_ICON;
	public static ImageIcon ERROR_ICON;
	public static ImageIcon WARNING_ICON;

	static {
		try {
			Class<WidgetUtils> cls = WidgetUtils.class;
			CLOSE_ICON = new ImageIcon(ImageIO.read(cls.getResourceAsStream("/icons/close.png")));

			METHOD_ICON = new ImageIcon(ImageIO.read(cls.getResourceAsStream("/icons/method.png")));
			PROPERTY_ICON = new ImageIcon(ImageIO.read(cls.getResourceAsStream("/icons/property.png")));
			CLASS_ICON = new ImageIcon(ImageIO.read(cls.getResourceAsStream("/icons/class.png")));
			VARIABLE_ICON = new ImageIcon(ImageIO.read(cls.getResourceAsStream("/icons/variable.png")));
			FIELD_ICON = new ImageIcon(ImageIO.read(cls.getResourceAsStream("/icons/field.png")));
			ERROR_ICON = new ImageIcon(ImageIO.read(cls.getResourceAsStream("/icons/error.png")));
			WARNING_ICON = new ImageIcon(ImageIO.read(cls.getResourceAsStream("/icons/warning.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends JComponent> T findParent(JComponent comp, Class<T> cls) {
		if (comp == null) {
			return null;
		}
		if (comp.getClass() == cls) {
			return (T) comp;
		}
		return findParent((JComponent) comp.getParent(), cls);
	}

	public static String getHtmlColor(Color color) {
		return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
	}

	public static ImageIcon createOverlayIcon(Icon baseIcon, ImageIcon overlayIcon) {
		BufferedImage result = new BufferedImage(baseIcon.getIconWidth(), baseIcon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = result.createGraphics();
		baseIcon.paintIcon(null, g2, 0, 0);

		Image overlay = overlayIcon.getImage();

		int w = 12;
		int h = 12;
		int x = baseIcon.getIconWidth() - w;
		int y = baseIcon.getIconHeight() - h;

		g2.drawImage(overlay, x, y, x + w, y + w, 0, 0, overlayIcon.getIconWidth(), overlayIcon.getIconHeight(), null);

		g2.dispose();

		return new ImageIcon(result);
	}

	public static JSplitPane createSplitPane(int orientation, Component left, Component right) {
		JSplitPane pane = new JSplitPane(orientation, left, right);
		return pane;
	}

	public static JSplitPane createHorizSplitPane(Component left, Component right) {
		return createSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
	}

	public static JSplitPane createVertSplitPane(Component left, Component right) {
		return createSplitPane(JSplitPane.VERTICAL_SPLIT, left, right);
	}

	public static void setLaf() {
		try {
			// MetalLookAndFeel.setCurrentTheme(new RayoTheme());
			// UIManager.setLookAndFeel(RayoLookAndFeel.class.getName());
			UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
			UIManager.put("Table.rowHeight", Integer.valueOf(25));
			NimbusLookAndFeel laf = (NimbusLookAndFeel) UIManager.getLookAndFeel();
			laf.getDefaults().put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	public static JTabbedPane createTabbedPane() {
		JTabbedPane tabbedPane = new JTabbedPane();
		return tabbedPane;
	}

	public static Font strikeOut(Font font) {
		Map<TextAttribute, Object> attrs = new HashMap<>(font.getAttributes());
		attrs.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		return new Font(attrs);

	}

	public static Font getCodeFont() {
		return new Font(Font.MONOSPACED, Font.PLAIN, 16);
	}

}
