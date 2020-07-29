package com.innoventsolutions.report.design;

import java.util.stream.Stream;

public interface DataProvider {
	Stream<?> getStream();
}
