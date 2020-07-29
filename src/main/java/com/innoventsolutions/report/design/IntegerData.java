package com.innoventsolutions.report.design;

import java.text.DecimalFormat;

public interface IntegerData extends ReportComponent {
	long getValue(Object dataRow);

	DecimalFormat getFormat();
}
