package com.innoventsolutions.report.design.impl;

import java.util.Map;

import com.innoventsolutions.report.design.Label;
import com.innoventsolutions.util.DesignUtil;

public class LabelImpl implements Label {
	private final String text;
	private final Map<String, String> styles;

	public LabelImpl(final String text) {
		this(text, "");
	}

	public LabelImpl(final String text, final String styles) {
		this.text = text;
		this.styles = DesignUtil.parseStyles(styles);
	}

	@Override
	public Map<String, String> getStyles() {
		return styles;
	}

	@Override
	public String getText() {
		return text;
	}
}
