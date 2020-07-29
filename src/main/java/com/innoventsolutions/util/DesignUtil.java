package com.innoventsolutions.util;

import java.util.HashMap;
import java.util.Map;

public class DesignUtil {
	private DesignUtil() {
	}

	public static Map<String, String> parseStyles(final String string) {
		final Map<String, String> map = new HashMap<>();
		if (string != null) {
			for (final String part : string.split(" *; *")) {
				final int indexOfColon = part.indexOf(":");
				String name;
				String value;
				if (indexOfColon >= 0) {
					name = part.substring(0, indexOfColon);
					value = part.substring(indexOfColon + 1);
				}
				else {
					name = part;
					value = "";
				}
				map.put(name, value);
			}
		}
		return map;
	}
}
