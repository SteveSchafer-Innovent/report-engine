package com.innoventsolutions.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DesignUtil {
	private static final Pattern STYLE_PATTERN = Pattern.compile("([a-zA-Z0-9_\\-]+) *: *(.*)");

	private DesignUtil() {
	}

	public static Map<String, String> parseStyles(final String string) {
		final Map<String, String> map = new HashMap<>();
		if (string != null) {
			for (final String part : string.split(" *; *")) {
				if (part.trim().length() == 0) {
					continue;
				}
				final Matcher matcher = STYLE_PATTERN.matcher(part);
				if (matcher.matches()) {
					final String name = matcher.group(1);
					final String value = matcher.group(2);
					map.put(name, value);
				}
				else {
					System.out.println("Invalid style pattern: " + part);
				}
			}
		}
		return map;
	}
}
