package com.innoventsolutions.report;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.innoventsolutions.report.design.Cell;
import com.innoventsolutions.report.design.Column;
import com.innoventsolutions.report.design.DataRowBinding;
import com.innoventsolutions.report.design.DateData;
import com.innoventsolutions.report.design.FloatData;
import com.innoventsolutions.report.design.Group;
import com.innoventsolutions.report.design.IntegerData;
import com.innoventsolutions.report.design.Label;
import com.innoventsolutions.report.design.ReportComponent;
import com.innoventsolutions.report.design.Row;
import com.innoventsolutions.report.design.Table;
import com.innoventsolutions.report.design.TextData;

public class HtmlEmitter implements Emitter {
	private final PrintWriter writer;

	public HtmlEmitter(final PrintWriter writer) {
		this.writer = writer;
	}

	private static abstract class Element {
		abstract void emit();
	}

	private static class Comment extends Element {
		private final PrintWriter writer;
		private final String text;

		public Comment(final PrintWriter writer, final String text) {
			this.writer = writer;
			this.text = text;
		}

		@Override
		public void emit() {
			writer.print("<!-- ");
			writer.print(text);
			writer.print("-->");
		}
	}

	private static abstract class Tag extends Element {
		private final PrintWriter writer;
		private final String name;
		private final Map<String, String> attributes;

		public Tag(final PrintWriter writer, final String name,
				final Map<String, String> attributes) {
			this.writer = writer;
			this.name = name;
			this.attributes = attributes;
		}

		protected abstract Stream<Element> getChildTags();

		protected String getContent() {
			return "";
		}

		@Override
		public void emit() {
			writer.print("<");
			writer.print(name);
			for (final String attrName : attributes.keySet()) {
				writer.print(" ");
				writer.print(attrName);
				writer.print("=\"");
				writer.print(attributes.get(attrName));
				writer.print("\"");
			}
			writer.print(">");
			final Stream<Element> childTags = getChildTags();
			childTags.forEach(tag -> {
				tag.emit();
			});
			writer.print(getContent());
			writer.print("</");
			writer.print(name);
			writer.println(">");
			writer.flush();
		}
	}

	private Tag getLabelTag(final Label label) {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("style", getStylesString(label.getStyles()));
		return new Tag(writer, "span", attributes) {
			@Override
			public Stream<Element> getChildTags() {
				return Stream.empty();
			}

			@Override
			public String getContent() {
				return label.getText();
			}
		};
	}

	private Element getDataTag(final DataRowBinding dataRowBinding, final TextData data) {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("style", getStylesString(data.highlight(dataRowBinding.getDataRow())));
		return new Tag(writer, "span", attributes) {
			@Override
			public Stream<Element> getChildTags() {
				return Stream.empty();
			}

			@Override
			public String getContent() {
				return data.getValue(dataRowBinding.getDataRow());
			}
		};
	}

	private Element getDataTag(final DataRowBinding dataRowBinding, final IntegerData data) {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("style", getStylesString(data.highlight(dataRowBinding.getDataRow())));
		return new Tag(writer, "span", attributes) {
			@Override
			public Stream<Element> getChildTags() {
				return Stream.empty();
			}

			@Override
			public String getContent() {
				final long value = data.getValue(dataRowBinding.getDataRow());
				final String format = data.getFormat();
				if (format != null) {
					final DecimalFormat df = new DecimalFormat(format);
					return df.format(value);
				}
				return String.valueOf(value);
			}
		};
	}

	private Element getDataTag(final DataRowBinding dataRowBinding, final FloatData data) {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("style", getStylesString(data.highlight(dataRowBinding.getDataRow())));
		return new Tag(writer, "span", attributes) {
			@Override
			public Stream<Element> getChildTags() {
				return Stream.empty();
			}

			@Override
			public String getContent() {
				final double value = data.getValue(dataRowBinding.getDataRow());
				final String format = data.getFormat();
				if (format != null) {
					final DecimalFormat df = new DecimalFormat(format);
					return df.format(value);
				}
				return String.valueOf(value);
			}
		};
	}

	private Element getDataTag(final DataRowBinding dataRowBinding, final DateData data) {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("style", getStylesString(data.highlight(dataRowBinding.getDataRow())));
		return new Tag(writer, "span", attributes) {
			@Override
			public Stream<Element> getChildTags() {
				return Stream.empty();
			}

			@Override
			public String getContent() {
				final Date value = data.getValue(dataRowBinding.getDataRow());
				final String format = data.getFormat();
				if (format != null) {
					final DateFormat df = new SimpleDateFormat(format);
					return df.format(value);
				}
				return String.valueOf(value);
			}
		};
	}

	private Element getComponentTag(final DataRowBinding dataRowBinding,
			final ReportComponent component) {
		if (component instanceof Label) {
			return getLabelTag((Label) component);
		}
		if (component instanceof TextData) {
			return getDataTag(dataRowBinding, (TextData) component);
		}
		if (component instanceof IntegerData) {
			return getDataTag(dataRowBinding, (IntegerData) component);
		}
		if (component instanceof FloatData) {
			return getDataTag(dataRowBinding, (FloatData) component);
		}
		if (component instanceof DateData) {
			return getDataTag(dataRowBinding, (DateData) component);
		}
		return new Comment(writer, "unknown component class");
	}

	private Element getEmptyCellTag() {
		final Map<String, String> attributes = new HashMap<>();
		return new Tag(writer, "td", attributes) {
			@Override
			protected Stream<Element> getChildTags() {
				return Stream.empty();
			}
		};
	}

	private Element getCellTag(final DataRowBinding dataRowBinding, final Cell cell,
			final Column column, final boolean header) {
		final Map<String, String> attributes = new HashMap<>();
		if (cell != null) {
			final Map<String, String> styles = new HashMap<>(cell.getStyles());
			final String textAlign = column.getStyles().get("text-align");
			if (textAlign != null && !styles.containsKey("text-align")) {
				styles.put("text-align", textAlign);
			}
			attributes.put("style", getStylesString(styles));
			final int colspan = cell.getColSpan();
			if (colspan > 1) {
				attributes.put("colspan", String.valueOf(colspan));
			}
		}
		return new Tag(writer, header ? "th" : "td", attributes) {
			@Override
			public Stream<Element> getChildTags() {
				if (cell == null) {
					return Stream.empty();
				}
				final List<ReportComponent> components = cell.getComponents();
				final Stream.Builder<Element> cellBuilder = Stream.builder();
				for (final ReportComponent component : components) {
					if (!component.isHidden(dataRowBinding.getDataRow())) {
						cellBuilder.add(getComponentTag(dataRowBinding, component));
					}
				}
				return cellBuilder.build();
			}
		};
	}

	private Element getRowTag(final DataRowBinding dataRowBinding, final Row row,
			final List<Column> columns, final boolean header) {
		final Iterator<Cell> cellIterator = row.getCells().iterator();
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("style", getStylesString(row.getStyles()));
		return new Tag(writer, "tr", attributes) {
			@Override
			public Stream<Element> getChildTags() {
				final AtomicInteger spanning = new AtomicInteger(1);
				return columns.stream().map(column -> {
					if (column.isHidden(null)) {
						if (cellIterator.hasNext()) {
							cellIterator.next(); // swallow the cell
						}
						return new Comment(writer, "hidden");
					}
					if (spanning.get() > 1) {
						spanning.decrementAndGet();
						return new Comment(writer, "spanned");
					}
					if (cellIterator.hasNext()) {
						final Cell cell = cellIterator.next();
						spanning.set(cell.getColSpan());
						return getCellTag(dataRowBinding, cell, column, header);
					}
					return getEmptyCellTag();
				});
			}
		};
	}

	private Element getTableTag(final Stream<DataRowBinding> stream, final Table table) {
		final List<Column> columns = table.getColumns();
		final List<Group> groups = table.getGroups();
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("style", getStylesString(table.getStyles()));
		return new Tag(writer, "table", attributes) {
			@Override
			public Stream<Element> getChildTags() {
				final Stream.Builder<Tag> builder = Stream.builder();
				for (final Column column : columns) {
					if (column.isHidden(null)) {
						continue;
					}
					final Map<String, String> attributes = new HashMap<>();
					attributes.put("style", getStylesString(column.getStyles()));
					builder.add(new Tag(writer, "col", attributes) {
						@Override
						public Stream<Element> getChildTags() {
							return Stream.empty();
						}
					});
				}
				final Stream<Tag> columnsStream = builder.build();
				final Stream<Element> rowsStream = stream.flatMap(dataRowBinding -> {
					final Stream.Builder<Element> rowBuilder = Stream.builder();
					final int level = dataRowBinding.getLevel();
					final DataRowBinding.Type type = dataRowBinding.getType();
					if (level == 0) {
						final List<Row> rows;
						switch (type) {
						case DETAIL:
							rows = table.getRows();
							break;
						default:
							rows = Collections.emptyList();
						}
						for (final Row row : rows) {
							if (!row.isHidden(dataRowBinding.getDataRow())) {
								rowBuilder.add(getRowTag(dataRowBinding, row, columns, false));
							}
						}
					}
					else if (level == groups.size() + 1) {
						final List<Row> rows;
						final boolean header;
						switch (type) {
						case HEADER:
							rows = table.getHeaderRows();
							header = true;
							break;
						case FOOTER:
							rows = table.getFooterRows();
							header = false;
							break;
						default:
							rows = Collections.emptyList();
							header = false;
						}
						for (final Row row : rows) {
							if (!row.isHidden(dataRowBinding.getDataRow())) {
								rowBuilder.add(getRowTag(dataRowBinding, row, columns, header));
							}
						}
					}
					else {
						final Group group = groups.get(level - 1);
						final List<Row> rows;
						final boolean header;
						switch (type) {
						case HEADER:
							rows = group.getHeaderRows();
							header = true;
							break;
						case FOOTER:
							rows = group.getFooterRows();
							header = false;
							break;
						default:
							rows = Collections.emptyList();
							header = false;
						}
						for (final Row row : rows) {
							if (!row.isHidden(dataRowBinding.getDataRow())) {
								rowBuilder.add(getRowTag(dataRowBinding, row, columns, header));
							}
						}
					}
					return rowBuilder.build();
				});
				return Stream.concat(columnsStream, rowsStream);
			}
		};
	}

	private static String getStylesString(final Map<String, String> styles) {
		final StringBuilder sb = new StringBuilder();
		String sep = "";
		for (final String name : styles.keySet()) {
			sb.append(sep);
			sep = "; ";
			sb.append(name);
			sb.append(": ");
			sb.append(styles.get(name));
		}
		return sb.toString();
	}

	@Override
	public void emit(final Stream<DataRowBinding> stream, final Table table) {
		final Element tableTag = getTableTag(stream, table);
		tableTag.emit();
	}
}
