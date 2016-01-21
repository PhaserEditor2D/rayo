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
package phasereditor.inspect.core.jsdoc;

import java.util.List;

/**
 * Class to render JSDoc comments.
 * 
 * @author arian
 *
 */
@SuppressWarnings({ "static-method" })
public class JSDocRenderer {
	private static JSDocRenderer _instance = new JSDocRenderer();

	public static JSDocRenderer getInstance() {
		return _instance;
	}

	public String render(Object member, int lastParamIndex) {
		if (member instanceof PhaserType) {
			return renderType((PhaserType) member, lastParamIndex, true, false);
		}

		if (member instanceof PhaserMethod) {
			return renderMethod((PhaserMethod) member, lastParamIndex);
		}

		if (member instanceof PhaserConstant) {
			return renderConstant((PhaserConstant) member);
		}

		if (member instanceof PhaserVariable) {
			return renderVariable((PhaserVariable) member);
		}

		return member.toString();
	}

	private String renderConstant(PhaserConstant cons) {
		StringBuilder sb = new StringBuilder();

		String returnSignature = htmlTypes(cons.getTypes());
		PhaserType declType = cons.getDeclType();
		String qname;
		if (cons.isGlobal()) {
			// FIXME: we assume global constants are from the Phaser namespace,
			// but it can be false in the future.
			qname = "Phaser." + cons.getName();
		} else {
			qname = declType.getName() + "." + cons.getName();
		}
		sb.append("<b>" + returnSignature + " " + qname + "</b>");

		sb.append("<p>" + html(cons.getHelp()) + "</p>");

		return sb.toString();
	}

	private String renderVariable(PhaserVariable var) {
		StringBuilder sb = new StringBuilder();

		String returnSignature = htmlTypes(var.getTypes());
		PhaserType declType = var.getDeclType();
		String qname = declType.getName() + "." + var.getName();
		sb.append("<b>" + returnSignature + " " + qname + "</b>");

		sb.append("<p>" + html(var.getHelp()) + "</p><br>");

		if (var instanceof PhaserProperty && ((PhaserProperty) var).isReadOnly()) {
			sb.append("<p><b>readonly</b></p>");
		}

		return sb.toString();
	}

	public String renderMethod(PhaserMethod method, int lastParamIndex) {
		StringBuilder sb = new StringBuilder();

		String returnSignature = htmlTypes(method.getReturnTypes());

		String qname = method.getDeclType().getName() + "." + method.getName();
		sb.append("<b>" + returnSignature + " " + qname + htmlArgsList(method.getArgs()) + "</b>");

		sb.append("<p>" + html(method.getHelp()) + "</p><br>");

		if (method.getReturnTypes().length > 0) {
			sb.append("<b>Returns:</b> " + returnSignature);
			sb.append("<dd>" + html(method.getReturnHelp()) + "</dd>");
		}

		sb.append(htmlArgsDoc(method.getArgs(), lastParamIndex));
		return sb.toString();
	}

	public String renderType(PhaserType type, int lastParamIndex, boolean renderParams, boolean renderMembers) {
		StringBuilder sb = new StringBuilder();

		sb.append("<b>class</b> " + type.getName());

		if (!type.getExtends().isEmpty()) {
			sb.append(" <b>extends</b> " + renderExtends(type));
		}

		if (renderParams) {
			sb.append("<p><b>constructor " + type.getName() + htmlArgsList(type.getConstructorArgs()) + "</b></p>");
		}

		sb.append("<p>" + html(type.getHelp()) + "</p><br>");

		if (renderParams) {
			sb.append(htmlArgsDoc(type.getConstructorArgs(), lastParamIndex));
		}

		if (renderMembers) {
			if (!type.getConstants().isEmpty()) {
				sb.append("<b>Constants:</b>");
				sb.append("<center><table width='90%'><tr><td>");
				for (PhaserConstant cons : type.getConstants()) {
					String typeStr = htmlTypes(cons.getTypes());
					sb.append("<br><b>" + typeStr + " " + cons.getName() + "</b>");
					sb.append(" " + cons.getHelp());
				}
				sb.append("</td></tr></table></center>");
			}

			if (!type.getProperties().isEmpty()) {
				sb.append("<b>Properties:</b>");
				sb.append("<center><table width='90%'><tr><td>");
				for (PhaserProperty prop : type.getProperties()) {
					String typeStr = htmlTypes(prop.getTypes());
					sb.append(
							"<b>" + (prop.isReadOnly() ? " readonly" : "") + " " + prop.getName() + "</b> " + typeStr);
					sb.append(" " + prop.getHelp());
					sb.append("<br><br>");
				}
				sb.append("</td></tr></table></center>");
			}

			if (!type.getMethods().isEmpty()) {
				sb.append("<b>Methods:</b>");
				sb.append("<center><table width='90%'><tr><td>");
				for (PhaserMethod method : type.getMethods()) {
					String typeStr = htmlTypes(method.getReturnTypes());
					sb.append("<b> " + typeStr + " " + method.getName() + htmlArgsList(method.getArgs()) + "</b>");
					sb.append(" " + method.getHelp());
					sb.append("<br><br>");
				}
				sb.append("</td></tr></table></center>");
			}

		}

		return sb.toString();
	}

	private String renderExtends(PhaserType type) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String e : type.getExtends()) {
			sb.append((i == 0 ? "" : " | ") + e);
			i++;
		}
		return sb.toString();
	}

	private String htmlArgsDoc(List<PhaserMethodArg> args, int lastParamIndex) {
		StringBuilder sb = new StringBuilder();
		if (!args.isEmpty()) {
			sb.append("<br><br><b>Parameters:</b><br>");
			sb.append("<center><table width='90%'><tr><td>");
		}

		int i = 0;
		for (PhaserVariable var : args) {
			sb.append("<b>" + var.getName() + "</b> ");

			if (var.isOptional()) {
				sb.append("[=" + var.getDefaultValue() + "]");
			}

			sb.append(htmlTypes(var.getTypes()));
			if (i < lastParamIndex) {
				sb.append(" <dd>" + html(var.getHelp()) + "</dd>");
			}
			sb.append("<br><br>");
			i++;
		}

		if (!args.isEmpty()) {
			sb.append("</td></tr></table></center>");
		}

		return sb.toString();
	}

	private String htmlArgsList(List<PhaserMethodArg> args) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		int i = 0;
		for (PhaserVariable arg : args) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(arg.getName());
			i++;
		}
		sb.append(")");
		return sb.toString();
	}

	private String htmlTypes(String[] types) {
		if (types.length == 0) {
			return "void";
		}

		if (types.length == 1) {
			return "{" + types[0] + "}";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				sb.append("|");
			}
			sb.append(types[i]);
		}
		sb.append("}");
		return sb.toString();
	}

	private static String html(String help) {
		return convertToHTMLContent(help).replace("\\n", "<br>");
	}

	public static String convertToHTMLContent(String content) {
		String content2 = replace(content, '&', "&amp;"); //$NON-NLS-1$
		content2 = replace(content2, '"', "&quot;"); //$NON-NLS-1$
		content2 = replace(content2, '<', "&lt;"); //$NON-NLS-1$
		return replace(content2, '>', "&gt;"); //$NON-NLS-1$
	}

	private static String replace(String text, char c, String s) {

		int previous = 0;
		int current = text.indexOf(c, previous);

		if (current == -1)
			return text;

		StringBuffer buffer = new StringBuffer();
		while (current > -1) {
			buffer.append(text.substring(previous, current));
			buffer.append(s);
			previous = current + 1;
			current = text.indexOf(c, previous);
		}
		buffer.append(text.substring(previous));

		return buffer.toString();
	}
}
