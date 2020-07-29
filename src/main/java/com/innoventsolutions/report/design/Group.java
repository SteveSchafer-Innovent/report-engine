package com.innoventsolutions.report.design;

import java.util.List;

public interface Group {
	List<Row> getHeaderRows();

	List<Row> getFooterRows();
}
