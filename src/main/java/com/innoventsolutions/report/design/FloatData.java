package com.innoventsolutions.report.design;

public interface FloatData extends ReportComponent {
	double getValue(Object dataRow);

	String getFormat();
}
