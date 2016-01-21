package rayo.ui.editors.js.autocomplete;

import static rayo.ui.widgets.WidgetUtils.CLASS_ICON;
import static rayo.ui.widgets.WidgetUtils.METHOD_ICON;

import java.util.ArrayList;
import java.util.List;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;

import phasereditor.inspect.core.jsdoc.IPhaserMember;
import phasereditor.inspect.core.jsdoc.JSDocRenderer;
import phasereditor.inspect.core.jsdoc.PhaserJSDoc;
import phasereditor.inspect.core.jsdoc.PhaserMethod;
import phasereditor.inspect.core.jsdoc.PhaserMethodArg;
import phasereditor.inspect.core.jsdoc.PhaserType;
import tern.server.protocol.completions.TernCompletionItem;

public class RayoFunctionCompletion extends FunctionCompletion implements ICompletionConstants {

	private TernCompletionItem _item;
	private IPhaserMember _member;
	private int _limitNumParams;

	public static Completion createClassCompletion(CompletionProvider provider, TernCompletionItem item) {
		String fullname = item.getDoc();
		PhaserType type = PhaserJSDoc.getInstance().getType(fullname);

		if (type == null) {
			return null;
		}

		BasicCompletion compl = new BasicCompletion(provider, item.getName());
		compl.setIcon(CLASS_ICON);
		compl.setRelevance(METHOD_RELEVANCE);
		compl.setSummary(JSDocRenderer.getInstance().renderType(type, Integer.MAX_VALUE, false, false));
		return compl;
	}

	public static List<Completion> computeCompletions(CompletionProvider provider, TernCompletionItem item) {
		List<Completion> list = new ArrayList<>();

		{
			Completion compl = createClassCompletion(provider, item);
			if (compl != null) {
				list.add(compl);
			}
		}

		IPhaserMember member = null;
		String fullname = item.getDoc();
		if (fullname != null) {
			PhaserJSDoc phaserDoc = PhaserJSDoc.getInstance();
			member = phaserDoc.getMember(fullname);

			if (member != null) {
				List<PhaserMethodArg> args;
				if (member instanceof PhaserType) {
					args = ((PhaserType) member).getConstructorArgs();
				} else {
					args = ((PhaserMethod) member).getArgs();
				}

				int optIndex = 0;
				for (PhaserMethodArg arg : args) {
					if (arg.isOptional()) {
						break;
					}
					optIndex++;
				}
				if (optIndex == args.size()) {
					list.add(new RayoFunctionCompletion(provider, item, args.size()));
				} else {
					for (int i = optIndex; i <= args.size(); i++) {
						list.add(new RayoFunctionCompletion(provider, item, i));
					}
				}
			}
		}

		if (list.isEmpty()) {
			list.add(new RayoFunctionCompletion(provider, item, Integer.MAX_VALUE));
		}

		return list;
	}

	public RayoFunctionCompletion(CompletionProvider provider, TernCompletionItem item, int limitNumParams) {
		super(provider, item.getName(), item.getJsType());
		_limitNumParams = limitNumParams;
		setRelevance(METHOD_RELEVANCE);
		setIcon(METHOD_ICON);

		_item = item;
		String itemdoc = item.getDoc();
		if (itemdoc != null) {
			PhaserJSDoc phaserDoc = PhaserJSDoc.getInstance();
			_member = phaserDoc.getMember(itemdoc);

			if (_member == null) {
				setSummary(itemdoc);
				setShortDescription(itemdoc);
			} else {
				setSummary(_member.getHelp());
				setShortDescription(_member.getHelp());
			}

			{
				// fill params
				List<Parameter> complParams = new ArrayList<>();
				if (_member == null) {
					List<tern.server.protocol.completions.Parameter> funcParams = item.getParameters();
					if (funcParams != null) {
						for (tern.server.protocol.completions.Parameter p : funcParams) {
							complParams.add(new Parameter(p.getType(), p.getName()));
						}
					}
				} else {
					List<PhaserMethodArg> phaserArgs;
					if (_member instanceof PhaserType) {
						phaserArgs = ((PhaserType) _member).getConstructorArgs();
					} else {
						phaserArgs = ((PhaserMethod) _member).getArgs();
					}
					int i = 0;
					for (PhaserMethodArg arg : phaserArgs) {
						if (i == limitNumParams) {
							break;
						}
						i++;
						// XXX: support multiple arguments
						String[] argTypes = arg.getTypes();
						String argType = argTypes[0];
						Parameter p2 = new Parameter(argType, arg.getName());
						p2.setDescription(arg.getHelp());
						complParams.add(p2);
					}
				}

				setParams(complParams);
			}
		}
	}

	public TernCompletionItem getItem() {
		return _item;
	}

	@Override
	public String getDefinitionString() {
		return _item.getText();
	}

	public IPhaserMember getMember() {
		return _member;
	}

	@Override
	public String getSummary() {
		if (_member != null) {
			return JSDocRenderer.getInstance().render(_member, _limitNumParams);
		}
		return super.getSummary();
	}

	@Override
	protected void addDefinitionString(StringBuilder sb) {
		super.addDefinitionString(sb);
		sb.append("<br>");
	}

	@Override
	protected boolean possiblyAddDescription(StringBuilder sb) {
		if (getShortDescription() != null) {
			sb.append("<br>");
			sb.append(getShortDescription());
			sb.append("<br><br><br>");
			return true;
		}
		return false;
	}

	@Override
	protected void addParameters(StringBuilder sb) {
		int paramCount = getParamCount();
		if (paramCount > 0) {
			sb.append("<b>Parameters:</b><br>");
			sb.append("<center><table width='90%'><tr><td>");
			for (int i = 0; i < paramCount; i++) {
				Parameter param = getParam(i);
				sb.append("<b>");
				sb.append(param.getName());
				sb.append("</b>&nbsp;");
				sb.append(param.getType() == null ? "?" : param.getType());
				String desc = param.getDescription();
				if (desc != null) {
					sb.append("&nbsp;" + desc);
				}
				sb.append("<br>");
			}
			sb.append("</td></tr></table></center><br><br>");
		}

		if (getReturnValueDescription() != null) {
			sb.append("<b>Returns:</b><br><center><table width='90%'><tr><td>");
			sb.append(getReturnValueDescription());
			sb.append("</td></tr></table></center><br><br>");
		}
	}
}