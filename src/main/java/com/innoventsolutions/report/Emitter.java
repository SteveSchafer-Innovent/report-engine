package com.innoventsolutions.report;

import java.util.stream.Stream;

import com.innoventsolutions.report.design.DataRowBinding;
import com.innoventsolutions.report.design.Table;

public interface Emitter {
	void emit(Stream<DataRowBinding> stream, Table table);
}
