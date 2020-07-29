package com.innoventsolutions.report.design.impl;

import java.util.Map;

import com.innoventsolutions.report.design.Column;
import com.innoventsolutions.util.DesignUtil;

public class ColumnImpl implements Column {
	private final Map<String, String> styles;
	private final boolean hidden;

	public ColumnImpl(final String styles) {
		this(styles, false);
	}

	public ColumnImpl(final String styles, final boolean hidden) {
		this.styles = DesignUtil.parseStyles(styles);
		this.hidden = hidden;
	}

	@Override
	public Map<String, String> getStyles() {
		return styles;
	}

	@Override
	public boolean isHidden(final Object dataRow) {
		return hidden;
	}
}
