package com.innoventsolutions.report.design;

public interface IntegerData extends ReportComponent {
	long getValue(Object dataRow);

	String getFormat();
}
