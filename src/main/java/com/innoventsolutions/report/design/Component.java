package com.innoventsolutions.report.design;

import java.util.HashMap;
import java.util.Map;

public interface Component {
	default Map<String, String> getStyles() {
		return new HashMap<>();
	}

	default boolean isHidden(final Object dataRow) {
		return false;
	}

	default Map<String, String> highlight(final Object dataRow) {
		return getStyles();
	};
}
