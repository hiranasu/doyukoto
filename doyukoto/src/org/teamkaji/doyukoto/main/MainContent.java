package org.teamkaji.doyukoto.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class MainContent {

	/**
	 * An array of items.
	 */
	public static List<MainItem> ITEMS = new ArrayList<MainItem>();

	/**
	 * A map of items, by ID.
	 */
	public static Map<String, MainItem> ITEM_MAP = new HashMap<String, MainItem>();

	static {
		addItem(new MainItem("1", "Device"));
		addItem(new MainItem("2", "View"));
	}

	private static void addItem(MainItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class MainItem {
		public String id;
		public String content;

		public MainItem(String id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
