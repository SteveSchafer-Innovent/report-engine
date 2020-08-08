package com.innoventsolutions.report.css;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.RGBColor;

import com.innoventsolutions.report.css.CSS.Side;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.steadystate.css.dom.CSSValueImpl;

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
	public void applyTextAlign(final CSSValueImpl cssValueImpl) {
		final String styleString = cssValueImpl == null ? "left" : cssValueImpl.getStringValue(); // TODO left if ltr, right if rtl
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
			System.out.println("Applying text-align: " + cssValueImpl + " to cell " + element);
			final PdfPCell pdfCell = (PdfPCell) element;
			pdfCell.setHorizontalAlignment(alignment);
		}
		else if (element instanceof Paragraph) {
			System.out.println("Applying text-align: " + cssValueImpl + " to paragraph " + element);
			final Paragraph paragraph = (Paragraph) element;
			paragraph.setAlignment(alignment);
		}
	}

	protected static BaseColor getColor(final CSSValueImpl cssValue) {
		final RGBColor rgbColor = cssValue.getRGBColorValue();
		final int red = (int) CSS.getColor(rgbColor.getRed());
		final int green = (int) CSS.getColor(rgbColor.getGreen());
		final int blue = (int) CSS.getColor(rgbColor.getBlue());
		try {
			return new BaseColor(red, green, blue);
		}
		catch (final Exception e) {
			System.out.println("Invalid color");
			System.err.println("Invalid color: " + cssValue);
			Thread.dumpStack();
			return BaseColor.BLACK;
		}
	}

	@Override
	public void applyPadding(final CSS.Side side, final CSSValueImpl cssValueImpl) {
		if (element instanceof PdfPCell) {
			System.out.println(
				"Applying " + side + " padding: " + cssValueImpl + " to cell " + element);
			final PdfPCell cell = (PdfPCell) element;
			switch (side) {
			case TOP:
				cell.setPaddingTop(CSS.convertLength(cssValueImpl, 0F));
				break;
			case RIGHT:
				cell.setPaddingRight(CSS.convertLength(cssValueImpl, 0F));
				break;
			case BOTTOM:
				cell.setPaddingBottom(CSS.convertLength(cssValueImpl, 0F));
				break;
			case LEFT:
				cell.setPaddingLeft(CSS.convertLength(cssValueImpl, 0F));
				break;
			}
		}
	}

	@Override
	public void applyBorder(final Side side, final CSSValueImpl cssValueImpl) {
		final CSS.BorderInfo borderInfo = new CSS.BorderInfo(cssValueImpl);
		applyBorderWidth(side, borderInfo.width);
		applyBorderStyle(side, borderInfo.style);
		applyBorderColor(side, borderInfo.color);
	}

	@Override
	public void applyBorderWidth(final Side side, final CSSValueImpl cssValueImpl) {
		final float specifiedValue = cssValueImpl == null ? 2 : CSS.convertLength(cssValueImpl, 0F); // TODO
		if (element instanceof Rectangle) {
			System.out.println(
				"Applying " + side + " border-width: " + cssValueImpl + " to " + element);
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
	public void applyBorderStyle(final Side side, final CSSValueImpl cssValueImpl) {
		final String style = cssValueImpl == null ? "none" : cssValueImpl.getStringValue();
		if (!"none".equalsIgnoreCase(style)) {
			System.out.println("ignoring border style " + style);
		}
	}

	@Override
	public void applyBorderColor(final Side side, final CSSValueImpl cssValueImpl) {
		final BaseColor specifiedValue = cssValueImpl == null ? BaseColor.BLACK
			: getColor(cssValueImpl);
		if (element instanceof Rectangle) {
			System.out.println(
				"Applying " + side + " border-color: " + cssValueImpl + " to " + element);
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
	public void applyColor(final CSSValueImpl cssValueImpl) {
		if (element instanceof Phrase) {
			System.out.println("Applying color: " + cssValueImpl + " to phrase " + element);
			final Phrase phrase = (Phrase) element;
			phrase.getFont().setColor(getColor(cssValueImpl));
		}
		else if (element instanceof Chunk) {
			System.out.println("Applying color: " + cssValueImpl + " to chunk " + element);
			final Chunk chunk = (Chunk) element;
			chunk.getFont().setColor(getColor(cssValueImpl));
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
	public void applyFontStyle(final CSSValueImpl cssValueImpl) {
		final String styleString = cssValueImpl == null ? "normal" : cssValueImpl.getStringValue();
		if (element instanceof Phrase) {
			System.out.println("Applying font-style: " + cssValueImpl + " to phrase " + element);
			final Phrase phrase = (Phrase) element;
			applyFontStyle(phrase.getFont(), styleString);
		}
		else if (element instanceof Chunk) {
			System.out.println("Applying font-style: " + cssValueImpl + " to chunk " + element);
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
	public void applyFontWeight(final CSSValueImpl cssValueImpl) {
		final String weightString = cssValueImpl == null ? "normal" : cssValueImpl.getStringValue();
		if (element instanceof Phrase) {
			System.out.println("Applying font-weight: " + cssValueImpl + " to phrase " + element);
			final Phrase phrase = (Phrase) element;
			applyFontWeight(phrase.getFont(), weightString);
		}
		else if (element instanceof Chunk) {
			System.out.println("Applying font-weight: " + cssValueImpl + " to chunk " + element);
			final Chunk chunk = (Chunk) element;
			applyFontWeight(chunk.getFont(), weightString);
		}
	}

	@Override
	public void applyFontSize(final CSSValueImpl cssValueImpl) {
		final float size;
		if (cssValueImpl.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
			final String sizeString = cssValueImpl.getStringValue();
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
				System.out.println("Unrecognize font size name: " + sizeString);
				size = 10;
			}
		}
		else {
			size = CSS.convertLength(cssValueImpl, 0F); // TODO
		}
		if (element instanceof Phrase) {
			System.out.println("Applying font-size: " + cssValueImpl + " to chunk " + element);
			final Phrase phrase = (Phrase) element;
			phrase.getFont().setSize(size);
		}
		else if (element instanceof Chunk) {
			System.out.println("Applying font-size: " + cssValueImpl + " to chunk " + element);
			final Chunk chunk = (Chunk) element;
			chunk.getFont().setSize(size);
		}
	}

	@Override
	public void applyFontFamily(final CSSValueImpl cssValueImpl) {
		final String family = cssValueImpl == null ? "helvetica" : cssValueImpl.getStringValue();
		if (element instanceof Phrase) {
			System.out.println("Applying font-family: " + cssValueImpl + " to chunk " + element);
			final Phrase phrase = (Phrase) element;
			phrase.getFont().setFamily(family);
		}
		else if (element instanceof Chunk) {
			System.out.println("Applying font-family: " + cssValueImpl + " to chunk " + element);
			final Chunk chunk = (Chunk) element;
			chunk.getFont().setFamily(family);
		}
	}
}
