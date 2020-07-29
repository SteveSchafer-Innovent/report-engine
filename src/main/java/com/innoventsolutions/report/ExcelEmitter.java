package com.innoventsolutions.report;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.innoventsolutions.report.design.Column;
import com.innoventsolutions.report.design.DataRowBinding;
import com.innoventsolutions.report.design.DateData;
import com.innoventsolutions.report.design.FloatData;
import com.innoventsolutions.report.design.Group;
import com.innoventsolutions.report.design.IntegerData;
import com.innoventsolutions.report.design.Label;
import com.innoventsolutions.report.design.ReportComponent;
import com.innoventsolutions.report.design.Table;
import com.innoventsolutions.report.design.TextData;

public class ExcelEmitter implements Emitter {
	private final OutputStream outputStream;
	private Sheet sheet = null;
	private List<Column> columns;

	public ExcelEmitter(final OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void emit(final Stream<DataRowBinding> stream, final Table table) {
		final Workbook workbook = new XSSFWorkbook();
		try {
			sheet = workbook.createSheet("Report");
			final Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.RED.getIndex());
			final CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			columns = table.getColumns();
			final List<Group> groups = table.getGroups();
			final AtomicInteger rowNum = new AtomicInteger(0);
			stream.forEach(dataRowBinding -> {
				final int level = dataRowBinding.getLevel();
				final DataRowBinding.Type type = dataRowBinding.getType();
				if (level == 0) {
					final List<com.innoventsolutions.report.design.Row> rows;
					switch (type) {
					case DETAIL:
						rows = table.getRows();
						break;
					default:
						rows = Collections.emptyList();
					}
					for (final com.innoventsolutions.report.design.Row row : rows) {
						if (!row.isHidden(dataRowBinding.getDataRow())) {
							final Row xlRow = sheet.createRow(rowNum.getAndIncrement());
							populateRow(xlRow, row, dataRowBinding);
						}
					}
				}
				else if (level == groups.size() + 1) {
					final List<com.innoventsolutions.report.design.Row> rows;
					switch (type) {
					case HEADER:
						rows = table.getHeaderRows();
						break;
					case FOOTER:
						rows = table.getFooterRows();
						break;
					default:
						rows = Collections.emptyList();
					}
					for (final com.innoventsolutions.report.design.Row row : rows) {
						if (!row.isHidden(dataRowBinding.getDataRow())) {
							final Row xlRow = sheet.createRow(rowNum.getAndIncrement());
							populateRow(xlRow, row, dataRowBinding);
						}
					}
				}
				else {
					final Group group = groups.get(level - 1);
					final List<com.innoventsolutions.report.design.Row> rows;
					switch (type) {
					case HEADER:
						rows = group.getHeaderRows();
						break;
					case FOOTER:
						rows = group.getFooterRows();
						break;
					default:
						rows = Collections.emptyList();
					}
					for (final com.innoventsolutions.report.design.Row row : rows) {
						if (!row.isHidden(dataRowBinding.getDataRow())) {
							final Row xlRow = sheet.createRow(rowNum.getAndIncrement());
							populateRow(xlRow, row, dataRowBinding);
						}
					}
				}
			});
			try {
				try {
					workbook.write(outputStream);
				}
				catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			finally {
				try {
					outputStream.close();
				}
				catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		finally {
			try {
				workbook.close();
			}
			catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void populateRow(final Row xlRow, final com.innoventsolutions.report.design.Row row,
			final DataRowBinding dataRowBinding) {
		final Iterator<com.innoventsolutions.report.design.Cell> cellIterator = row.getCells().iterator();
		final AtomicInteger spanning = new AtomicInteger(1);
		final AtomicInteger colNum = new AtomicInteger(0);
		columns.forEach(column -> {
			if (column.isHidden(null)) {
				if (cellIterator.hasNext()) {
					cellIterator.next(); // swallow the cell
				}
				return;
			}
			if (spanning.get() > 1) {
				spanning.decrementAndGet();
				colNum.getAndIncrement();
				return;
			}
			if (cellIterator.hasNext()) {
				final com.innoventsolutions.report.design.Cell cell = cellIterator.next();
				final int colspan = cell.getColSpan();
				spanning.set(colspan);
				if (colspan > 1) {
					final int rowNum = xlRow.getRowNum();
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, colNum.get(),
							colNum.get() + colspan - 1));
				}
				final List<ReportComponent> components = cell.getComponents();
				if (components.size() == 1) {
					final ReportComponent component = components.get(0);
					if (component instanceof Label) {
						final Label label = (Label) component;
						final String text = label.getText();
						xlRow.createCell(colNum.getAndIncrement()).setCellValue(text);
					}
					else if (component instanceof TextData) {
						final TextData data = (TextData) component;
						final String value = data.getValue(dataRowBinding.getDataRow());
						if (value != null) {
							xlRow.createCell(colNum.getAndIncrement()).setCellValue(value);
						}
					}
					else if (component instanceof IntegerData) {
						final IntegerData data = (IntegerData) component;
						final long value = data.getValue(dataRowBinding.getDataRow());
						xlRow.createCell(colNum.getAndIncrement()).setCellValue(value);
					}
					else if (component instanceof FloatData) {
						final FloatData data = (FloatData) component;
						final double value = data.getValue(dataRowBinding.getDataRow());
						xlRow.createCell(colNum.getAndIncrement()).setCellValue(value);
					}
					else if (component instanceof DateData) {
						final DateData data = (DateData) component;
						final Date value = data.getValue(dataRowBinding.getDataRow());
						xlRow.createCell(colNum.getAndIncrement()).setCellValue(value);
					}
				}
				else {
					final StringBuilder sb = new StringBuilder();
					for (final ReportComponent component : components) {
						if (component.isHidden(dataRowBinding.getDataRow())) {
							continue;
						}
						if (component instanceof Label) {
							final Label label = (Label) component;
							final String text = label.getText();
							sb.append(text);
						}
						else if (component instanceof TextData) {
							final TextData data = (TextData) component;
							final String value = data.getValue(dataRowBinding.getDataRow());
							if (value != null) {
								sb.append(value);
							}
						}
						else if (component instanceof IntegerData) {
							final IntegerData data = (IntegerData) component;
							final long value = data.getValue(dataRowBinding.getDataRow());
							final DecimalFormat format = data.getFormat();
							if (format != null) {
								sb.append(format.format(value));
							}
							else {
								sb.append(String.valueOf(value));
							}
						}
						else if (component instanceof FloatData) {
							final FloatData data = (FloatData) component;
							final double value = data.getValue(dataRowBinding.getDataRow());
							final DecimalFormat format = data.getFormat();
							if (format != null) {
								sb.append(format.format(value));
							}
							else {
								sb.append(String.valueOf(value));
							}
						}
						else if (component instanceof DateData) {
							final DateData data = (DateData) component;
							final Date value = data.getValue(dataRowBinding.getDataRow());
							final DateFormat format = data.getFormat();
							if (format != null) {
								sb.append(format.format(value));
							}
							else {
								sb.append(String.valueOf(value));
							}
						}
					}
					xlRow.createCell(colNum.getAndIncrement()).setCellValue(sb.toString());
				}
				return;
			}
			colNum.getAndIncrement();
		});
	}
}
