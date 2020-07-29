package com.innoventsolutions.report.design;

import java.util.List;

public interface Table extends Grid {
	List<Group> getGroups();

	List<Row> getHeaderRows();

	List<Row> getFooterRows();
}
