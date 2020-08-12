package com.innoventsolutions.report.css;

import java.util.List;

import com.innoventsolutions.report.css.CSS.Side;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDiv;
import com.itextpdf.text.pdf.PdfPCell;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermColor;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.csskit.Color;

/**
 * Apply CSS styles to an iText PDF element.
 *
 * @author Steve Schafer
 */
public class PdfApplier implements CSS.Applier {
	private final Element element;

	public PdfApplier(final Element element) {
		this.element = element;
	}

	@Override
	public void applyTextAlign(final TermIdent termIdent) {
		final String styleString = termIdent.getValue();
		int alignment;
		if ("left".equalsIgnoreCase(styleString)) {
			alignment = Element.ALIGN_LEFT;
		}
		else if ("right".equalsIgnoreCase(styleString)) {
			alignment = Element.ALIGN_RIGHT;
		}
		else if ("center".equalsIgnoreCase(styleString)) {
			alignment = Element.ALIGN_CENTER;
		}
		else if ("initial".equalsIgnoreCase(styleString)) {
			alignment = Element.ALIGN_LEFT;
		}
		else {
			System.out.println("Unrecognized value for text-align");
			System.err.println("Unrecognized value for text-align: " + styleString);
			alignment = PdfPCell.ALIGN_LEFT;
		}
		if (element instanceof PdfPCell) {
			System.out.println("Applying text-align: " + styleString + " to cell " + element);
			final PdfPCell pdfCell = (PdfPCell) element;
			pdfCell.setHorizontalAlignment(alignment);
		}
		else if (element instanceof Paragraph) {
			System.out.println("Applying text-align: " + styleString + " to paragraph " + element);
			final Paragraph paragraph = (Paragraph) element;
			paragraph.setAlignment(alignment);
		}
	}

	protected static BaseColor getColor(final TermColor termColor) {
		if (termColor == null) {
			return BaseColor.BLACK;
		}
		final Color color = termColor.getValue();
		final int red = color.getRed();
		final int green = color.getGreen();
		final int blue = color.getBlue();
		final int alpha = color.getAlpha();
		try {
			return new BaseColor(red, green, blue, alpha);
		}
		catch (final Exception e) {
			System.out.println("Invalid color");
			System.err.println("Invalid color: " + termColor);
			Thread.dumpStack();
			return BaseColor.BLACK;
		}
	}

	@Override
	public void applyPadding(final CSS.Side side, final TermLength termLength) {
		final float length = CSS.convertLength(termLength);
		if (element instanceof PdfPCell) {
			System.out.println("Applying " + side + " padding: " + length + " to cell " + element);
			final PdfPCell cell = (PdfPCell) element;
			switch (side) {
			case TOP:
				cell.setPaddingTop(length);
				break;
			case RIGHT:
				cell.setPaddingRight(length);
				break;
			case BOTTOM:
				cell.setPaddingBottom(length);
				break;
			case LEFT:
				cell.setPaddingLeft(length);
				break;
			}
		}
	}

	@Override
	public void applyBorder(final Side side, final List<Term<?>> list) {
		final CSS.BorderInfo borderInfo = new CSS.BorderInfo(list);
		applyBorderWidth(side, borderInfo.width);
		applyBorderStyle(side, borderInfo.style);
		applyBorderColor(side, borderInfo.color);
	}

	@Override
	public void applyBorderWidth(final Side side, final TermLength termLength) {
		final float specifiedValue = CSS.convertLength(termLength);
		if (element instanceof Rectangle) {
			System.out.println(
				"Applying " + side + " border-width: " + specifiedValue + " to " + element);
			final Rectangle rectangle = (Rectangle) element;
			switch (side) {
			case TOP:
				rectangle.setBorderWidthTop(specifiedValue);
				break;
			case RIGHT:
				rectangle.setBorderWidthRight(specifiedValue);
				break;
			case BOTTOM:
				rectangle.setBorderWidthBottom(specifiedValue);
				break;
			case LEFT:
				rectangle.setBorderWidthLeft(specifiedValue);
				break;
			}
		}
	}

	@Override
	public void applyBorderStyle(final Side side, final TermIdent termIdent) {
		final String style = termIdent.getValue();
		if (!"none".equalsIgnoreCase(style)) {
			System.out.println("ignoring border style " + style);
		}
	}

	@Override
	public void applyBorderColor(final Side side, final TermColor termColor) {
		final BaseColor specifiedValue = getColor(termColor);
		if (element instanceof Rectangle) {
			System.out.println(
				"Applying " + side + " border-color: " + termColor + " to " + element);
			final Rectangle rectangle = (Rectangle) element;
			switch (side) {
			case TOP:
				rectangle.setBorderColorTop(specifiedValue);
				break;
			case RIGHT:
				rectangle.setBorderColorRight(specifiedValue);
				break;
			case BOTTOM:
				rectangle.setBorderColorBottom(specifiedValue);
				break;
			case LEFT:
				rectangle.setBorderColorLeft(specifiedValue);
				break;
			}
		}
	}

	@Override
	public void applyColor(final TermColor termColor) {
		if (element instanceof Phrase) {
			System.out.println("Applying color: " + termColor + " to phrase " + element);
			final Phrase phrase = (Phrase) element;
			phrase.getFont().setColor(getColor(termColor));
		}
		else if (element instanceof Chunk) {
			System.out.println("Applying color: " + termColor + " to chunk " + element);
			final Chunk chunk = (Chunk) element;
			chunk.getFont().setColor(getColor(termColor));
		}
	}

	static private void applyFontStyle(final Font font, final String styleString) {
		int style = font.getStyle();
		if (style == -1) {
			style = 0;
		}
		if ("italic".equalsIgnoreCase(styleString)) {
			style |= Font.ITALIC;
		}
		else if ("normal".equalsIgnoreCase(styleString)) {
			style |= Font.NORMAL;
		}
		else {
			System.out.println("Font style " + styleString + " is not supported in PDF");
		}
		font.setStyle(style);
	}

	@Override
	public void applyFontStyle(final TermIdent termIdent) {
		final String styleString = termIdent.getValue();
		if (element instanceof Phrase) {
			System.out.println("Applying font-style: " + styleString + " to phrase " + element);
			final Phrase phrase = (Phrase) element;
			applyFontStyle(phrase.getFont(), styleString);
		}
		else if (element instanceof Chunk) {
			System.out.println("Applying font-style: " + styleString + " to chunk " + element);
			final Chunk chunk = (Chunk) element;
			applyFontStyle(chunk.getFont(), styleString);
		}
	}

	private static void applyFontWeight(final Font font, final String weightString) {
		int style = font.getStyle();
		if (style == -1) {
			style = 0;
		}
		if ("bold".equalsIgnoreCase(weightString)) {
			style |= Font.BOLD;
		}
		else if ("normal".equalsIgnoreCase(weightString)) {
			style |= Font.NORMAL;
		}
		else {
			System.out.println("Font weight " + weightString + " is not supported in PDF");
		}
		font.setStyle(style);
	}

	@Override
	public void applyFontWeight(final TermIdent termIdent) {
		final String weightString = termIdent.getValue();
		if (element instanceof Phrase) {
			System.out.println("Applying font-weight: " + weightString + " to phrase " + element);
			final Phrase phrase = (Phrase) element;
			applyFontWeight(phrase.getFont(), weightString);
		}
		else if (element instanceof Chunk) {
			System.out.println("Applying font-weight: " + weightString + " to chunk " + element);
			final Chunk chunk = (Chunk) element;
			applyFontWeight(chunk.getFont(), weightString);
		}
	}

	@Override
	public void applyFontSize(final Term<?> term) {
		final float size;
		if (term instanceof TermIdent) {
			final TermIdent termIdent = (TermIdent) term;
			final String sizeString = termIdent.getValue();
			if ("xx-small".equalsIgnoreCase(sizeString)) {
				size = 6;
			}
			else if ("x-small".equalsIgnoreCase(sizeString)) {
				size = 8;
			}
			else if ("small".equalsIgnoreCase(sizeString)) {
				size = 10;
			}
			else if ("medium".equalsIgnoreCase(sizeString)) {
				size = 12;
			}
			else if ("large".equalsIgnoreCase(sizeString)) {
				size = 14;
			}
			else if ("x-large".equalsIgnoreCase(sizeString)) {
				size = 16;
			}
			else if ("xx-large".equalsIgnoreCase(sizeString)) {
				size = 18;
			}
			else if ("smaller".equalsIgnoreCase(sizeString)) {
				size = 10;
			}
			else if ("larger".equalsIgnoreCase(sizeString)) {
				size = 14;
			}
			else {
				System.err.println("Unrecognized font size name: " + sizeString);
				System.out.println("Unrecognized font size name");
				size = 10;
			}
		}
		else if (term instanceof TermLength) {
			final TermLength termLength = (TermLength) term;
			size = CSS.convertLength(termLength);
		}
		else {
			throw new IllegalArgumentException("Expecting TermIdent or TermLength for font-size");
		}
		if (element instanceof Phrase) {
			System.out.println("Applying font-size: " + size + " to chunk " + element);
			final Phrase phrase = (Phrase) element;
			phrase.getFont().setSize(size);
		}
		else if (element instanceof Chunk) {
			System.out.println("Applying font-size: " + size + " to chunk " + element);
			final Chunk chunk = (Chunk) element;
			chunk.getFont().setSize(size);
		}
	}

	@Override
	public void applyFontFamily(final TermIdent termIdent) {
		final String family = termIdent.getValue();
		if (element instanceof Phrase) {
			System.out.println("Applying font-family: " + termIdent + " to chunk " + element);
			final Phrase phrase = (Phrase) element;
			phrase.getFont().setFamily(family);
		}
		else if (element instanceof Chunk) {
			System.out.println("Applying font-family: " + termIdent + " to chunk " + element);
			final Chunk chunk = (Chunk) element;
			chunk.getFont().setFamily(family);
		}
	}

	@Override
	public void applyBackgroundColor(final TermColor termColor) {
		if (element instanceof Chunk) {
			System.out.println("Applying background-color: " + termColor + " to chunk " + element);
			final Chunk chunk = (Chunk) element;
			chunk.setBackground(getColor(termColor));
		}
		else if (element instanceof Rectangle) {
			System.out.println(
				"Applying background-color: " + termColor + " to rectangle " + element);
			final Rectangle rectangle = (Rectangle) element;
			rectangle.setBackgroundColor(getColor(termColor));
		}
		else if (element instanceof PdfDiv) {
			System.out.println("Applying background-color: " + termColor + " to div " + element);
			final PdfDiv div = (PdfDiv) element;
			div.setBackgroundColor(getColor(termColor));
		}
	}
}
