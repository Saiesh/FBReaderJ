package org.zlibrary.core.application.menu;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.resources.ZLResourceKey;
public class Menu {
    protected ZLResource myResource;    
    private List<Item> myItems = new LinkedList<Item>();

    protected Menu(ZLResource resource) {
    	this.myResource = resource;
    }

	public void addItem(int actionId, ZLResourceKey key) {
		myItems.add(new Menubar.PlainItem(myResource.getResource(key).value(), actionId));
	}
	
	public void addSeparator() {
		myItems.add(new Menubar.Separator());
	}
	
	public Menu addSubmenu(ZLResourceKey key) {
		Menubar.Submenu submenu = new Menubar.Submenu(myResource.getResource(key));
	    myItems.add(submenu);
		return submenu;
	}

	public List<Item> items() {
		return Collections.unmodifiableList(myItems);
	}
}

