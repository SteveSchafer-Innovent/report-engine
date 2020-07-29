package com.innoventsolutions.report.design;

import java.text.DecimalFormat;

public interface FloatData extends ReportComponent {
	double getValue(Object dataRow);

	DecimalFormat getFormat();
}
