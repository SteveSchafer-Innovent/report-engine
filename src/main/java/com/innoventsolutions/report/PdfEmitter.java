package com.innoventsolutions.report;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.innoventsolutions.report.css.CSS;
import com.innoventsolutions.report.css.CSS.Style;
import com.innoventsolutions.report.css.CSS.StyleMapHolder;
import com.innoventsolutions.report.css.PdfApplier;
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
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfEmitter implements Emitter {
	private final OutputStream outputStream;
	private PdfPTable pdfTable = null;
	private List<Column> columns = null;

	public PdfEmitter(final OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	private static void applyStyles(final Element element, final StyleMapHolder styleMapHolder) {
		System.out.println("applyStyles " + styleMapHolder + " to " + element.getClass().getName()
			+ " " + element);
		final List<CSS.Style> styles = CSS.parseCss(styleMapHolder);
		final CSS.Applier applier = new PdfApplier(element);
		for (final Style style : styles) {
			style.apply(applier);
		}
	}

	@Override
	public void emit(final Stream<DataRowBinding> stream, final Table table) {
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
			final StyleMapHolder tableStyleMapHolder = new StyleMapHolder(table.getStyles(), null);
			applyStyles(pdfTable, tableStyleMapHolder);
			pdfTable.setWidthPercentage(100.0F);
			final List<Group> groups = table.getGroups();
			stream.forEach(dataRowBinding -> {
				final List<com.innoventsolutions.report.design.Row> rows;
				final int level = dataRowBinding.getLevel();
				final DataRowBinding.Type type = dataRowBinding.getType();
				if (level == 0) {
					switch (type) {
					case DETAIL:
						rows = table.getRows();
						break;
					default:
						rows = Collections.emptyList();
					}
				}
				else if (level == groups.size() + 1) {
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
				}
				else {
					final Group group = groups.get(level - 1);
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
				}
				for (final com.innoventsolutions.report.design.Row row : rows) {
					if (row.isHidden(dataRowBinding.getDataRow())) {
						continue;
					}
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
						final StyleMapHolder columnStyleMapHolder = new StyleMapHolder(
								column.getStyles(), tableStyleMapHolder);
						final StyleMapHolder rowStyleMapHolder = new StyleMapHolder(row.getStyles(),
								columnStyleMapHolder);
						if (cellIterator.hasNext()) {
							final com.innoventsolutions.report.design.Cell cell = cellIterator.next();
							final PdfPCell pdfCell = new PdfPCell();
							final StyleMapHolder cellStyleMapHolder = new StyleMapHolder(
									cell.getStyles(), rowStyleMapHolder);
							applyStyles(pdfCell, cellStyleMapHolder);
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
								Paragraph paragraph = null;
								if (component instanceof Label) {
									final Label label = (Label) component;
									final String text = label.getText();
									paragraph = new Paragraph(text);
								}
								else if (component instanceof TextData) {
									final TextData data = (TextData) component;
									final String value = data.getValue(dataRowBinding.getDataRow());
									if (value != null) {
										paragraph = new Paragraph(value);
									}
								}
								else if (component instanceof IntegerData) {
									final IntegerData data = (IntegerData) component;
									final long value = data.getValue(dataRowBinding.getDataRow());
									final String format = data.getFormat();
									final String text;
									if (format != null) {
										final DecimalFormat df = new DecimalFormat(format);
										text = df.format(value);
									}
									else {
										text = String.valueOf(value);
									}
									paragraph = new Paragraph(text);
								}
								else if (component instanceof FloatData) {
									final FloatData data = (FloatData) component;
									final double value = data.getValue(dataRowBinding.getDataRow());
									final String format = data.getFormat();
									final String text;
									if (format != null) {
										final DecimalFormat df = new DecimalFormat(format);
										text = df.format(value);
									}
									else {
										text = String.valueOf(value);
									}
									paragraph = new Paragraph(text);
								}
								else if (component instanceof DateData) {
									final DateData data = (DateData) component;
									final Date value = data.getValue(dataRowBinding.getDataRow());
									final String format = data.getFormat();
									final String text;
									if (format != null) {
										final DateFormat df = new SimpleDateFormat(format);
										text = df.format(value);
									}
									else {
										text = String.valueOf(value);
									}
									;
									paragraph = new Paragraph(text);
								}
								if (paragraph != null) {
									final StyleMapHolder componentStyleMapHolder = new StyleMapHolder(
											component.getStyles(), cellStyleMapHolder);
									applyStyles(paragraph, componentStyleMapHolder);
									pdfCell.addElement(paragraph);
								}
							}
							pdfTable.addCell(pdfCell);
							return;
						}
						else {
							pdfTable.addCell("");
						}
					});
				}
			});
			document.add(pdfTable);
			document.close();
		}
		catch (final DocumentException e) {
			throw new RuntimeException("Failed to generate PDF file", e);
		}
	}
}
