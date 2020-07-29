package com.innoventsolutions.report.design;

import java.util.List;

public interface Grid extends ReportComponent {
	List<Row> getRows();

	List<Column> getColumns();
}
