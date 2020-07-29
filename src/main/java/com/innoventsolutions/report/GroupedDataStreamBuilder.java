package com.innoventsolutions.report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.innoventsolutions.data.model.AggregateDataRow;
import com.innoventsolutions.report.design.DataRowBinding;

public abstract class GroupedDataStreamBuilder<DataRow> {
	private class RowHolder {
		DataRow dataRow = null;
	}

	private static class DataRowBindingImpl implements DataRowBinding {
		private final int level;
		private final Type type;
		private final Object dataRow;

		public DataRowBindingImpl(final int level, final Type type, final Object dataRow) {
			this.level = level;
			this.type = type;
			this.dataRow = dataRow;
		}

		@Override
		public int getLevel() {
			return level;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public Object getDataRow() {
			return dataRow;
		}
	}

	public Stream<DataRow> sort(final Stream<DataRow> inputStream) {
		final int levelCount = getLevelCount();
		return inputStream.sorted((o1, o2) -> {
			for (int i = levelCount; i >= 1; i--) {
				final int diff = compareLevel(o1, o2, i);
				if (diff != 0) {
					return diff;
				}
			}
			return 0;
		});
	}

	/**
	 * This is the main logic for producing a grouped stream of rows.
	 *
	 * @param inputStream
	 * @return
	 */
	public Stream<DataRowBinding> build(final Stream<DataRow> inputStream) {
		final int levelCount = getLevelCount();
		// sort the rows and the produce a stream of RowHolders so we can mark the end
		// a row holder with a null row marks the end
		// we need an end marker so we know when to add all the final footers
		final Stream<RowHolder> holderStream = inputStream.map(dataRow -> {
			final RowHolder rowHolder = new RowHolder();
			rowHolder.dataRow = dataRow;
			return rowHolder;
		});
		final List<AggregateDataRow<DataRow>> levelRows = new ArrayList<>();
		// initialize all the level rows
		for (int level = 1; level <= levelCount + 1; level++) {
			levelRows.add(createLevelRow(level));
		}
		final RowHolder previousRowHolder = new RowHolder();
		// mark the end (with empty RowHolder) and then flatten the map to add aggregate rows
		return Stream.concat(holderStream, Stream.of(new RowHolder())).flatMap(rowHolder -> {
			final DataRow dataRow = rowHolder.dataRow;
			final Stream.Builder<DataRowBinding> builder = Stream.builder();
			final DataRow prevRow = previousRowHolder.dataRow;
			// output the overall header
			if (prevRow == null) {
				builder.add(new DataRowBindingImpl(levelCount + 1, DataRowBinding.Type.HEADER,
						levelRows.get(levelCount)));
			}
			// end marker - add all footers
			if (dataRow == null) {
				// if prevRow is null then we haven't seen any rows yet, so no footers
				if (prevRow != null) {
					// for footers, iterate from most detailed to most general
					for (int level = 1; level <= levelCount + 1; level++) {
						final AggregateDataRow<DataRow> levelRow = levelRows.get(level - 1);
						levelRow.finish();
						for (int parentLevel = level + 1; parentLevel <= levelCount
							+ 1; parentLevel++) {
							final AggregateDataRow<DataRow> parentLevelRow = levelRows.get(
								parentLevel - 1);
							parentLevelRow.accumulateLevel(levelRow, level);
						}
						builder.add(
							new DataRowBindingImpl(level, DataRowBinding.Type.FOOTER, levelRow));
					}
				}
				// output the overall footer
				builder.add(new DataRowBindingImpl(levelCount + 1, DataRowBinding.Type.FOOTER,
						levelRows.get(levelCount)));
				return builder.build();
			}
			if (prevRow == null) {
			}
			// get the most general group level that has different key
			// that and all more detailed levels will need header/footers
			int breakLevel = 0;
			for (int level = levelCount; level >= 1; level--) {
				if (prevRow == null || compareLevel(prevRow, dataRow, level) != 0) {
					breakLevel = level;
					break;
				}
			}
			// if prevRow is null then we haven't seen any rows yet, so no footers
			if (prevRow != null) {
				// for footers, iterate from most detailed to most general
				for (int level = 1; level <= breakLevel; level++) {
					final AggregateDataRow<DataRow> levelRow = levelRows.get(level - 1);
					levelRow.finish();
					for (int parentLevel = level + 1; parentLevel <= levelCount; parentLevel++) {
						final AggregateDataRow<DataRow> parentLevelRow = levelRows.get(
							parentLevel - 1);
						parentLevelRow.accumulateLevel(levelRow, level);
					}
					builder.add(
						new DataRowBindingImpl(level, DataRowBinding.Type.FOOTER, levelRow));
					levelRows.set(level - 1, createLevelRow(level));
				}
			}
			// for headers, iterate from most general to most detailed
			if (breakLevel >= 1) {
				for (int level = breakLevel; level >= 1; level--) {
					builder.add(new DataRowBindingImpl(level, DataRowBinding.Type.HEADER,
							levelRows.get(level - 1)));
				}
			}
			// add the detail row
			builder.add(new DataRowBindingImpl(0, DataRowBinding.Type.DETAIL, rowHolder.dataRow));
			// accumulate the detail row into all the level rows
			for (int level = 1; level <= levelCount; level++) {
				final AggregateDataRow<DataRow> row = levelRows.get(level - 1);
				row.accumulate(dataRow);
			}
			previousRowHolder.dataRow = dataRow;
			return builder.build();
		});
	}

	protected abstract int getLevelCount();

	protected abstract AggregateDataRow<DataRow> newLevelRow(int level);

	private final AggregateDataRow<DataRow> createLevelRow(final int level) {
		final AggregateDataRow<DataRow> levelRow = newLevelRow(level);
		levelRow.clear();
		return levelRow;
	}

	protected abstract int compareLevel(DataRow prevRow, DataRow thisRow, int level);
}
