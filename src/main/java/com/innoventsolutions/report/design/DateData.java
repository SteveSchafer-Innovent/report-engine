package com.innoventsolutions.report.design;

import java.util.Date;

public interface DateData extends ReportComponent {
	Date getValue(Object dataRow);

	String getFormat();
}
