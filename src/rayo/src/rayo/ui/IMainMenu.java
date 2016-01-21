package rayo.ui;

import rayo.ui.actions.DeleteFileAction;
import rayo.ui.actions.OpenInWebBrowserAction;
import rayo.ui.actions.OpenLocationAction;
import rayo.ui.actions.RefreshAction;
import rayo.ui.actions.ReloadAction;
import rayo.ui.actions.RenameFileAction;

public interface IMainMenu {

	DeleteFileAction getDeleteFileAction();

	RenameFileAction getRenameFileAction();

	RefreshAction getRefreshAction();

	ReloadAction getReloadAction();

	OpenLocationAction getOpenLocationAction();

	OpenInWebBrowserAction getOpenInBrowserAction();
}