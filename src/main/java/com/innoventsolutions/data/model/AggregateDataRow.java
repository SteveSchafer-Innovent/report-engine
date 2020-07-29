package com.innoventsolutions.data.model;

public interface AggregateDataRow<DataRow> {
	void accumulate(DataRow t);

	default void accumulateLevel(final AggregateDataRow<DataRow> levelRow, final int level) {
	}

	void finish();

	boolean isFinished();

	void clear();
}
