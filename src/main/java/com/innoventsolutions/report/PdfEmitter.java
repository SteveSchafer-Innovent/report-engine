package com.innoventsolutions.report;

import java.io.OutputStream;
import java.text.Format;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfEmitter implements Emitter {
	private final OutputStream outputStream;
	private PdfPTable pdfTable = null;
	private List<Column> columns = null;
	private Table table = null;;

	public PdfEmitter(final OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void emit(final Stream<DataRowBinding> stream, final Table table) {
		this.table = table;
		final Document document = new Document();
		try {
			PdfWriter.getInstance(document, outputStream);
			document.setMargins(72.0F / 4.0F, 72.0F / 4.0F, 72.0F / 4.0F, 72.0F / 4.0F);
			document.open();
			columns = table.getColumns();
			int colCount = 0;
			for (final Column column : columns) {
				if (!column.isHidden(null)) {
					colCount++;
				}
			}
			pdfTable = new PdfPTable(colCount);
			pdfTable.setWidthPercentage(100.0F);
			final List<Group> groups = table.getGroups();
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
							populateRow(row, dataRowBinding);
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
							populateRow(row, dataRowBinding);
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
							populateRow(row, dataRowBinding);
						}
					}
				}
			});
			document.add(pdfTable);
			document.close();
		}
		catch (final DocumentException e) {
			throw new RuntimeException("Failed to generate PDF file", e);
		}
	}

	private void populateRow(final com.innoventsolutions.report.design.Row row,
			final DataRowBinding dataRowBinding) {
		final Map<String, String> tableStyles = table.getStyles();
		final Map<String, String> rowStyles = inheritable(tableStyles);
		rowStyles.putAll(row.getStyles());
		final Iterator<com.innoventsolutions.report.design.Cell> cellIterator = row.getCells().iterator();
		final AtomicInteger spanning = new AtomicInteger(1);
		columns.forEach(column -> {
			if (column.isHidden(null)) {
				if (cellIterator.hasNext()) {
					cellIterator.next(); // swallow the cell
				}
				return;
			}
			if (spanning.get() > 1) {
				spanning.decrementAndGet();
				return;
			}
			final Map<String, String> columnStyles = inheritable(tableStyles);
			columnStyles.putAll(column.getStyles());
			if (cellIterator.hasNext()) {
				final com.innoventsolutions.report.design.Cell cell = cellIterator.next();
				final Map<String, String> cellStyles = inheritable(columnStyles);
				cellStyles.putAll(inheritable(rowStyles));
				cellStyles.putAll(cell.getStyles());
				final PdfPCell pdfCell = new PdfPCell();
				// TODO set cell background and border
				final int colspan = cell.getColSpan();
				spanning.set(colspan);
				if (colspan > 1) {
					pdfCell.setColspan(colspan);
				}
				final List<ReportComponent> components = cell.getComponents();
				for (final ReportComponent component : components) {
					if (component.isHidden(dataRowBinding.getDataRow())) {
						continue;
					}
					final Map<String, String> componentStyles = inheritable(cellStyles);
					componentStyles.putAll(component.getStyles());
					final Font font = createPdfFont(componentStyles);
					if (component instanceof Label) {
						final Label label = (Label) component;
						final String text = label.getText();
						final Chunk chunk = new Chunk(text, font);
						pdfCell.addElement(chunk);
					}
					else if (component instanceof TextData) {
						final TextData data = (TextData) component;
						final String value = data.getValue(dataRowBinding.getDataRow());
						if (value != null) {
							final Chunk chunk = new Chunk(value, font);
							pdfCell.addElement(chunk);
						}
					}
					else if (component instanceof IntegerData) {
						final IntegerData data = (IntegerData) component;
						final long value = data.getValue(dataRowBinding.getDataRow());
						final Format format = data.getFormat();
						final String text = format != null ? format.format(value)
							: String.valueOf(value);
						final Chunk chunk = new Chunk(text, font);
						pdfCell.addElement(chunk);
					}
					else if (component instanceof FloatData) {
						final FloatData data = (FloatData) component;
						final double value = data.getValue(dataRowBinding.getDataRow());
						final Format format = data.getFormat();
						final String text = format != null ? format.format(value)
							: String.valueOf(value);
						final Chunk chunk = new Chunk(text, font);
						pdfCell.addElement(chunk);
					}
					else if (component instanceof DateData) {
						final DateData data = (DateData) component;
						final Date value = data.getValue(dataRowBinding.getDataRow());
						final Format format = data.getFormat();
						final String text = format != null ? format.format(value)
							: String.valueOf(value);
						final Chunk chunk = new Chunk(text, font);
						pdfCell.addElement(chunk);
					}
					component.getStyles();
				}
				pdfTable.addCell(pdfCell);
				return;
			}
			else {
				pdfTable.addCell("");
			}
		});
	}

	private Map<String, String> inheritable(final Map<String, String> inputStyles) {
		final Map<String, String> outputStyles = new HashMap<>();
		for (final String key : inputStyles.keySet()) {
			final String value = inputStyles.get(key);
			if (key.startsWith("font")) {
				outputStyles.put(key, value);
			}
		}
		return outputStyles;
	}

	private Font createPdfFont(final Map<String, String> styles) {
		String fontName = styles.get("font-family");
		if (fontName == null) {
			fontName = FontFactory.HELVETICA;
		}
		String sizeString = styles.get("font-size");
		if (sizeString == null) {
			sizeString = "10pt";
		}
		if (sizeString.toLowerCase().endsWith("pt")) {
			sizeString = sizeString.substring(0, sizeString.length() - 2);
		}
		// TODO other units
		final float size = Float.parseFloat(sizeString);
		String styleString = styles.get("font-style");
		if (styleString == null) {
			styleString = "normal";
		}
		String weightString = styles.get("font-weight");
		if (weightString == null) {
			weightString = "normal";
		}
		int style = 0;
		if ("bold".equalsIgnoreCase(weightString)) {
			style |= Font.BOLD;
		}
		if ("italic".equalsIgnoreCase(styleString)) {
			style |= Font.ITALIC;
		}
		// TODO more styles
		final BaseColor color = BaseColor.BLACK; // TODO
		return FontFactory.getFont(fontName, size, style, color);
	}
}
