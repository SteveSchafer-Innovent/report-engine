package com.innoventsolutions.report.design;

public interface DataRowBinding {
	int getLevel();

	Type getType();

	public enum Type {
		HEADER, FOOTER, DETAIL, END_MARKER
	}

	Object getDataRow();
}
