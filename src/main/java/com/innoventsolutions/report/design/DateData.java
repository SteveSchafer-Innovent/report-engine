package com.innoventsolutions.report.design;

import java.text.DateFormat;
import java.util.Date;

public interface DateData extends ReportComponent {
	Date getValue(Object dataRow);

	DateFormat getFormat();
}
