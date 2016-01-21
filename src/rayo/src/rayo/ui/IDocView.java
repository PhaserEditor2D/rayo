package rayo.ui;

import phasereditor.inspect.core.jsdoc.IPhaserMember;
import rayo.core.RayoJSDocQuery;

public interface IDocView {

	void display(RayoJSDocQuery query);
	
	void display(IPhaserMember member);
}
