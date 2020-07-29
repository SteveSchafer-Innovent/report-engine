package com.innoventsolutions.report.design;

import java.util.List;

public interface Cell extends Component {
	List<ReportComponent> getComponents();

	default int getColSpan() {
		return 1;
	}
}
