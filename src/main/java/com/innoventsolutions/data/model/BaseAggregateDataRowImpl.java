package com.innoventsolutions.data.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseAggregateDataRowImpl<DataRow> implements AggregateDataRow<DataRow> {
	private final Map<Integer, AtomicInteger> childCounts = new HashMap<>();

	@Override
	public void accumulateLevel(final AggregateDataRow<DataRow> levelRow, final int level) {
		AtomicInteger counter = childCounts.get(Integer.valueOf(level));
		if (counter == null) {
			counter = new AtomicInteger(0);
			childCounts.put(Integer.valueOf(level), counter);
		}
		counter.addAndGet(1);
	}

	public int getChildCount(final int level) {
		final AtomicInteger counter = childCounts.get(Integer.valueOf(level));
		if (counter == null) {
			return 0;
		}
		return counter.get();
	}
}
