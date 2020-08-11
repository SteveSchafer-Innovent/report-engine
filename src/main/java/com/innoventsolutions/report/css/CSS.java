package com.innoventsolutions.report.css;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.helpers.ParserFactory;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

import com.steadystate.css.dom.CSSValueImpl;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.csskit.Color;

/**
 * Static CSS parsing methods and objects. This is independent of any kind of
 * output.
 *
 * @author Steve Schafer
 */
public class CSS {
	private CSS() {
	}

	/**
	 * Tracks which styles are shortcuts.
	 */
	public static final Map<String, List<String>> SHORTCUT_TARGETS;

	private static void addShortcut(final String name, final String... strings) {
		final List<String> list = new ArrayList<>();
		for (final String string : strings) {
			list.add(string);
		}
		SHORTCUT_TARGETS.put(name, list);
	}

	static {
		SHORTCUT_TARGETS = new HashMap<>();
		addShortcut("border", "border-width", "border-style", "border-color");
		addShortcut("border-width", "border-top-width", "border-right-width", "border-bottom-width",
			"border-left-width");
		addShortcut("border-style", "border-top-style", "border-right-style", "border-bottom-style",
			"border-left-style");
		addShortcut("border-color", "border-top-color", "border-right-color", "border-bottom-color",
			"border-left-color");
		addShortcut("border-top", "border-top-width", "border-top-style", "border-top-color");
		addShortcut("border-right", "border-right-width", "border-right-style",
			"border-right-color");
		addShortcut("border-bottom", "border-bottom-width", "border-bottom-style",
			"border-bottom-color");
		addShortcut("border-left", "border-left-width", "border-left-style", "border-left-color");
		addShortcut("padding", "padding-top", "padding-right", "padding-bottom", "padding-left");
		addShortcut("font", "font-style", "font-variant", "font-weight", "font-size",
			"font-family");
	}
	/**
	 * Supplies default values for styles.
	 */
	static Map<String, Supplier<CSSValueImpl>> DEFAULT_STYLE_SUPPLIERS;
	static {
		final Map<String, Supplier<CSSValueImpl>> map = new HashMap<>();
		map.put("padding-top", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("0");
			return cssValueImpl;
		});
		map.put("padding-right", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("0");
			return cssValueImpl;
		});
		map.put("padding-bottom", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("0");
			return cssValueImpl;
		});
		map.put("padding-left", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("0");
			return cssValueImpl;
		});
		map.put("border-top-width", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setFloatValue(CSSPrimitiveValue.CSS_PT, 2);
			return cssValueImpl;
		});
		map.put("border-right-width", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setFloatValue(CSSPrimitiveValue.CSS_PT, 2);
			return cssValueImpl;
		});
		map.put("border-bottom-width", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setFloatValue(CSSPrimitiveValue.CSS_PT, 2);
			return cssValueImpl;
		});
		map.put("border-left-width", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setFloatValue(CSSPrimitiveValue.CSS_PT, 2);
			return cssValueImpl;
		});
		map.put("border-top-style", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("none");
			return cssValueImpl;
		});
		map.put("border-right-style", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("none");
			return cssValueImpl;
		});
		map.put("border-bottom-style", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("none");
			return cssValueImpl;
		});
		map.put("border-left-style", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("none");
			return cssValueImpl;
		});
		map.put("border-top-color", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("#000000");
			return cssValueImpl;
		});
		map.put("border-right-color", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("#000000");
			return cssValueImpl;
		});
		map.put("border-bottom-color", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("#000000");
			return cssValueImpl;
		});
		map.put("border-left-color", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("#000000");
			return cssValueImpl;
		});
		map.put("font-family", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("helvetica");
			return cssValueImpl;
		});
		map.put("font-size", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("medium");
			return cssValueImpl;
		});
		map.put("font-weight", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("normal");
			return cssValueImpl;
		});
		map.put("font-style", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("normal");
			return cssValueImpl;
		});
		map.put("color", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("#000000");
			return cssValueImpl;
		});
		map.put("text-align", () -> {
			final CSSValueImpl cssValueImpl = new CSSValueImpl();
			cssValueImpl.setCssText("left");
			return cssValueImpl;
		});
		DEFAULT_STYLE_SUPPLIERS = map;
	}

	public interface Style {
		void apply(Applier applier);
	}

	interface StyleFactory {
		String getName();

		Style parse(CSSValueImpl cssValueImpl);
	}

	private static Map<String, StyleFactory> STYLE_APPLIER_FACTORIES;
	static {
		final List<StyleFactory> factories = new ArrayList<>();
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "padding";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						if (cssValueImpl != null) {
							final CSSValueImpl[] specifiedValues = getValues(cssValueImpl);
							switch (specifiedValues.length) {
							case 4:
								applier.applyPadding(Side.TOP, specifiedValues[0]);
								applier.applyPadding(Side.RIGHT, specifiedValues[1]);
								applier.applyPadding(Side.BOTTOM, specifiedValues[2]);
								applier.applyPadding(Side.LEFT, specifiedValues[3]);
								break;
							case 3:
								applier.applyPadding(Side.TOP, specifiedValues[0]);
								applier.applyPadding(Side.RIGHT, specifiedValues[1]);
								applier.applyPadding(Side.BOTTOM, specifiedValues[2]);
								applier.applyPadding(Side.LEFT, specifiedValues[1]);
								break;
							case 2:
								applier.applyPadding(Side.TOP, specifiedValues[0]);
								applier.applyPadding(Side.RIGHT, specifiedValues[1]);
								applier.applyPadding(Side.BOTTOM, specifiedValues[0]);
								applier.applyPadding(Side.LEFT, specifiedValues[1]);
								break;
							case 1:
								applier.applyPadding(Side.TOP, specifiedValues[0]);
								applier.applyPadding(Side.RIGHT, specifiedValues[0]);
								applier.applyPadding(Side.BOTTOM, specifiedValues[0]);
								applier.applyPadding(Side.LEFT, specifiedValues[0]);
								break;
							default:
								System.out.println("Too many values for padding");
							}
						}
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "padding-top";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyPadding(Side.TOP, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "padding-right";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyPadding(Side.RIGHT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "padding-bottom";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyPadding(Side.BOTTOM, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "padding-left";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyPadding(Side.LEFT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-top";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorder(Side.TOP, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-right";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorder(Side.RIGHT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-bottom";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorder(Side.BOTTOM, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-left";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorder(Side.LEFT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorder(Side.TOP, cssValueImpl);
						applier.applyBorder(Side.RIGHT, cssValueImpl);
						applier.applyBorder(Side.BOTTOM, cssValueImpl);
						applier.applyBorder(Side.LEFT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-width";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderWidth(Side.TOP, cssValueImpl);
						applier.applyBorderWidth(Side.RIGHT, cssValueImpl);
						applier.applyBorderWidth(Side.BOTTOM, cssValueImpl);
						applier.applyBorderWidth(Side.LEFT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-style";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderStyle(Side.TOP, cssValueImpl);
						applier.applyBorderStyle(Side.RIGHT, cssValueImpl);
						applier.applyBorderStyle(Side.BOTTOM, cssValueImpl);
						applier.applyBorderStyle(Side.LEFT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-color";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderColor(Side.TOP, cssValueImpl);
						applier.applyBorderColor(Side.RIGHT, cssValueImpl);
						applier.applyBorderColor(Side.BOTTOM, cssValueImpl);
						applier.applyBorderColor(Side.LEFT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-top-width";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderWidth(Side.TOP, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-right-width";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderWidth(Side.RIGHT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-bottom-width";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderWidth(Side.BOTTOM, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-left-width";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderWidth(Side.LEFT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-top-style";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderStyle(Side.TOP, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-right-style";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderStyle(Side.RIGHT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-bottom-style";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderStyle(Side.BOTTOM, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-left-style";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderStyle(Side.LEFT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-top-color";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderColor(Side.TOP, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-right-color";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderColor(Side.RIGHT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-bottom-color";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderColor(Side.BOTTOM, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "border-left-color";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderColor(Side.LEFT, cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "font-family";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyFontFamily(cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "font-size";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyFontSize(cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "font-weight";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyFontWeight(cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "font-style";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyFontStyle(cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "color";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyColor(cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "text-align";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyTextAlign(cssValueImpl);
					}
				};
			}
		});
		factories.add(new StyleFactory() {
			@Override
			public String getName() {
				return "background-color";
			}

			@Override
			public Style parse(final CSSValueImpl cssValueImpl) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBackgroundColor(cssValueImpl);
					}
				};
			}
		});
		final Map<String, StyleFactory> map = new HashMap<>();
		for (final StyleFactory factory : factories) {
			map.put(factory.getName(), factory);
		}
		STYLE_APPLIER_FACTORIES = map;
	}

	/**
	 * Contains all the styles to be applied to an element including the
	 * inheritance hierarchy.
	 *
	 * @author Steve Schafer
	 */
	public static class StyleMapHolder {
		final Map<String, String> styleMap;
		final StyleMapHolder container;

		public StyleMapHolder(final Map<String, String> styleMap, final StyleMapHolder container) {
			this.styleMap = styleMap;
			this.container = container;
		}

		/**
		 * Return all the CSS values to be applied to an element taking
		 * inheritance and defaults into account.
		 *
		 * @return
		 */
		public Map<String, CSSValueImpl> getCSSValues() {
			final Map<String, CSSValueImpl> cssValues = new HashMap<>();
			populateCssValuesMap(cssValues);
			return cssValues;
		}

		private void populateCssValuesMap(final Map<String, CSSValueImpl> cssValues) {
			if (container != null) {
				// add parents before children
				container.populateCssValuesMap(cssValues);
			}
			System.out.println("get CSS values " + styleMap);
			final String styleString = makeStylesString(styleMap);
			System.out.println("styleString = " + styleString);
			try {
				final StyleSheet styleSheet = CSSFactory.parseString("*{" + styleString + "}",
					null);
				System.out.println("styleSheet.size = " + styleSheet.size());
				styleSheet.forEach(ruleBlock -> {
					final RuleSet ruleSet = (RuleSet) ruleBlock;
					final CombinedSelector[] combinedSelectors = ruleSet.getSelectors();
					System.out.println("combinedSelectors = " + (combinedSelectors == null ? "null"
						: String.valueOf(combinedSelectors.length)));
					ruleSet.forEach(declaration -> {
						final String property = declaration.getProperty();
						System.out.println("property = " + property);
						declaration.forEach(term -> {
							final Object value = term.getValue();
							System.out.println("value = " + value);
							if (value instanceof Color) {
								final Color color = (Color) value;
								System.out.println(
									"color = " + color.getRed() + ", " + color.getGreen() + ", "
										+ color.getBlue() + ", " + color.getAlpha());
							}
							else {
								System.out.println("value class = " + value.getClass().getName());
							}
						});
					});
				});
			}
			catch (final IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (final cz.vutbr.web.css.CSSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			final InputSource source = new InputSource(new StringReader(styleString));
			final CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
			try {
				final CSSStyleDeclaration declaration = parser.parseStyleDeclaration(source);
				final int length = declaration.getLength();
				for (int i = 0; i < length; i++) {
					final String propName = declaration.item(i).toLowerCase();
					final CSSValueImpl cssValueImpl = (CSSValueImpl) declaration.getPropertyCSSValue(
						propName);
					removeShortcutTargets(propName, cssValues);
					if (cssValueImpl.getCssValueType() != CSSValue.CSS_INHERIT) {
						cssValues.put(propName, cssValueImpl);
					}
				}
				System.out.println("  declared = " + cssValues);
				final Set<String> effectiveProps = new HashSet<>();
				for (final String declaredProp : cssValues.keySet()) {
					effectiveProps.add(declaredProp);
					addEffectiveProps(declaredProp, effectiveProps);
				}
				System.out.println("  effective props = " + effectiveProps);
				// add defaults
				for (final String propName : CSS.DEFAULT_STYLE_SUPPLIERS.keySet()) {
					if (!effectiveProps.contains(propName)) {
						final Supplier<CSSValueImpl> supplier = CSS.DEFAULT_STYLE_SUPPLIERS.get(
							propName);
						if (supplier != null) {
							final CSSValueImpl defaultValue = supplier.get();
							cssValues.put(propName, defaultValue);
						}
					}
				}
				System.out.println("  with defaults = " + cssValues);
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
		}

		private void addEffectiveProps(final String declaredProp,
				final Set<String> effectiveProps) {
			final List<String> shortcutTargets = CSS.SHORTCUT_TARGETS.get(declaredProp);
			if (shortcutTargets != null) {
				for (final String shortcutTarget : shortcutTargets) {
					System.out.println("    add " + shortcutTarget);
					effectiveProps.add(shortcutTarget);
					addEffectiveProps(shortcutTarget, effectiveProps);
				}
			}
		}

		private void removeShortcutTargets(final String propName,
				final Map<String, CSSValueImpl> cssValues) {
			final List<String> shortcutTargets = CSS.SHORTCUT_TARGETS.get(propName);
			if (shortcutTargets != null) {
				for (final String shortcutTarget : shortcutTargets) {
					System.out.println("    remove " + shortcutTarget);
					cssValues.remove(shortcutTarget);
					removeShortcutTargets(shortcutTarget, cssValues);
				}
			}
		}

		private static String makeStylesString(final Map<String, String> styles) {
			final StringBuilder sb = new StringBuilder();
			for (final String key : styles.keySet()) {
				final String value = styles.get(key);
				sb.append(key);
				sb.append(": ");
				sb.append(value);
				sb.append("; ");
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(styleMap);
			if (container != null) {
				sb.append(" in ");
				sb.append(container);
			}
			return sb.toString();
		}
	}

	/**
	 * Return a list of styles that need to be applied to an element.
	 *
	 * @param styleMapHolder
	 * @return a list of styles
	 */
	public static List<Style> parseCss(final StyleMapHolder styleMapHolder) {
		final Map<String, CSSValueImpl> cssValues = styleMapHolder.getCSSValues();
		final List<Style> styleAppliers = new ArrayList<>();
		for (final String propName : cssValues.keySet()) {
			final CSSValueImpl cssValueImpl = cssValues.get(propName);
			System.out.println("    " + propName + ": " + cssValueImpl);
			final StyleFactory factory = STYLE_APPLIER_FACTORIES.get(propName);
			if (factory != null) {
				styleAppliers.add(factory.parse(cssValueImpl));
			}
			else {
				System.out.println("Cannot handle " + propName + " = " + cssValueImpl);
			}
		}
		return styleAppliers;
	}

	static CSSValueImpl[] getValues(final CSSValueImpl cssValueImpl) {
		if (cssValueImpl.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final CSSValueImpl[] values = new CSSValueImpl[1];
			values[0] = cssValueImpl;
			return values;
		}
		final int length = cssValueImpl.getLength();
		final CSSValueImpl[] values = new CSSValueImpl[length];
		for (int i = 0; i < length; i++) {
			final CSSValueImpl itemValueImpl = (CSSValueImpl) cssValueImpl.item(i);
			if (itemValueImpl.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				values[i] = itemValueImpl;
			}
			else {
				System.out.println("Expecting primitive value in sub-value " + (i + 1));
				values[i] = null;
			}
		}
		return values;
	}

	@SuppressWarnings("unused")
	private static void parseCssSac(final String stylesString) {
		try {
			System.setProperty("org.w3c.css.sac.parser", "org.w3c.flute.parser.Parser");
			final ParserFactory parserFactory = new ParserFactory();
			final Parser parser = parserFactory.makeParser();
			final InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(stylesString));
			final DocumentHandler documentHandler = new DocumentHandler() {
				@Override
				public void comment(final String arg0) throws CSSException {
				}

				@Override
				public void endDocument(final InputSource arg0) throws CSSException {
				}

				@Override
				public void endFontFace() throws CSSException {
					System.out.println("endFontFace");
				}

				@Override
				public void endMedia(final SACMediaList arg0) throws CSSException {
				}

				@Override
				public void endPage(final String arg0, final String arg1) throws CSSException {
				}

				@Override
				public void endSelector(final SelectorList arg0) throws CSSException {
				}

				@Override
				public void ignorableAtRule(final String arg0) throws CSSException {
					System.out.println("ignorableAtRule = " + arg0);
				}

				@Override
				public void importStyle(final String arg0, final SACMediaList arg1,
						final String arg2) throws CSSException {
				}

				@Override
				public void namespaceDeclaration(final String arg0, final String arg1)
						throws CSSException {
				}

				@Override
				public void property(final String name, final LexicalUnit lu,
						final boolean important) throws CSSException {
					System.out.println(name);
					try {
						System.out.println("  dimensionalUnitText = " + lu.getDimensionUnitText());
					}
					catch (final Exception e) {
						System.out.println("  failed to get dimensionalUnitText: " + e);
					}
					System.out.println("  floatValue = " + lu.getFloatValue());
					System.out.println("  functionName = " + lu.getFunctionName());
					System.out.println("  integerValue = " + lu.getIntegerValue());
					System.out.println("  lexicalUnitType = " + lu.getLexicalUnitType());
					System.out.println("  stringValue = " + lu.getStringValue());
					System.out.println("  parameters = " + lu.getParameters());
					System.out.println("  subValues = " + lu.getSubValues());
					System.out.println("  important = " + important);
				}

				@Override
				public void startDocument(final InputSource arg0) throws CSSException {
				}

				@Override
				public void startFontFace() throws CSSException {
					System.out.println("startFontFace");
				}

				@Override
				public void startMedia(final SACMediaList arg0) throws CSSException {
				}

				@Override
				public void startPage(final String arg0, final String arg1) throws CSSException {
				}

				@Override
				public void startSelector(final SelectorList arg0) throws CSSException {
				}
			};
			parser.setDocumentHandler(documentHandler);
			parser.parseStyleDeclaration(inputSource);
		}
		catch (ClassNotFoundException | IllegalAccessException | InstantiationException
				| NullPointerException | ClassCastException | CSSException | IOException e) {
			e.printStackTrace();
		}
	}

	public static enum Side {
		TOP, RIGHT, BOTTOM, LEFT
	}

	public interface Applier {
		void applyTextAlign(CSSValueImpl cssValueImpl);

		void applyBackgroundColor(CSSValueImpl cssValueImpl);

		void applyPadding(Side side, CSSValueImpl cssValueImpl);

		void applyBorder(Side side, CSSValueImpl cssValueImpl);

		void applyBorderWidth(Side side, CSSValueImpl cssValueImpl);

		void applyBorderStyle(Side side, CSSValueImpl cssValueImpl);

		void applyBorderColor(Side side, CSSValueImpl cssValueImpl);

		void applyColor(CSSValueImpl cssValueImpl);

		void applyFontStyle(CSSValueImpl cssValueImpl);

		void applyFontWeight(CSSValueImpl cssValueImpl);

		void applyFontSize(CSSValueImpl cssValueImpl);

		void applyFontFamily(CSSValueImpl cssValueImpl);
	}

	protected static float getColor(final CSSPrimitiveValue primitiveValue) {
		final short type = primitiveValue.getPrimitiveType();
		final float value = primitiveValue.getFloatValue(type);
		// type is ignored;
		return value;
	}

	protected static float convertLength(final CSSValueImpl cssValueImpl,
			final float parentLength) {
		if (cssValueImpl.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			System.out.println("expecting primitive value");
			return 0;
		}
		final short type = cssValueImpl.getPrimitiveType();
		final float value = cssValueImpl.getFloatValue(type);
		return convertLength(type, value, parentLength);
	}

	protected static float convertLength(final short type, final float value,
			final float parentValue) {
		final Converter converter = LENGTH_CONVERTERS.get(Short.valueOf(type));
		if (converter == null) {
			System.err.println("Unrecognized primitive type: " + type + ", " + value);
			Thread.dumpStack();
			return 0;
		}
		return converter.convert(value, parentValue);
	}

	protected static interface Converter {
		float convert(float value, float parentLength);
	}

	protected static Map<Short, Converter> LENGTH_CONVERTERS;
	static {
		final Map<Short, Converter> map = new HashMap<>();
		map.put(Short.valueOf(CSSPrimitiveValue.CSS_PT), (value, parentLength) -> value);
		map.put(Short.valueOf(CSSPrimitiveValue.CSS_NUMBER), (value, parentLength) -> value);
		map.put(Short.valueOf(CSSPrimitiveValue.CSS_PX),
			(value, parentLength) -> value * 96.0F / 72.26999F);
		map.put(Short.valueOf(CSSPrimitiveValue.CSS_PERCENTAGE),
			(value, parentLength) -> parentLength * value / 100.0F);
		map.put(Short.valueOf(CSSPrimitiveValue.CSS_EMS),
			(value, parentLength) -> value * 10.00002F);
		map.put(Short.valueOf(CSSPrimitiveValue.CSS_EXS),
			(value, parentLength) -> value * 4.30554F);
		map.put(Short.valueOf(CSSPrimitiveValue.CSS_MM), (value, parentLength) -> value * 2.84526F);
		map.put(Short.valueOf(CSSPrimitiveValue.CSS_CM),
			(value, parentLength) -> value * 28.45274F);
		map.put(Short.valueOf(CSSPrimitiveValue.CSS_PC), (value, parentLength) -> value * 12F);
		map.put(Short.valueOf(CSSPrimitiveValue.CSS_IN),
			(value, parentLength) -> value * 72.26999F);
		LENGTH_CONVERTERS = map;
	}

	protected enum BorderStyle {
		NONE, HIDDEN, DOTTED, DASHED, SOLID, DOUBLE, GROOVE, RIDGE, INSET, OUTSET, INITIAL
	}

	protected static boolean isBorderStyle(final CSSValueImpl cssValueImpl) {
		if (cssValueImpl.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
			return false;
		}
		final String ident = cssValueImpl.getStringValue();
		for (final BorderStyle borderType : BorderStyle.values()) {
			if (borderType.name().equalsIgnoreCase(ident)) {
				return true;
			}
		}
		return false;
	}

	protected static class BorderInfo {
		final CSSValueImpl width;
		final CSSValueImpl style;
		final CSSValueImpl color;

		BorderInfo(final CSSValueImpl cssValueImpl) {
			CSSValueImpl width = null;
			CSSValueImpl style = null;
			CSSValueImpl color = null;
			if (cssValueImpl != null) {
				final CSSValueImpl[] values = CSS.getValues(cssValueImpl);
				for (int i = 0; i < values.length; i++) {
					final CSSValueImpl value = values[i];
					if (i == 0) {
						if (isBorderStyle(value)) {
							style = value;
						}
						else {
							width = value;
						}
					}
					else if (i == 2) {
						if (style == null) {
							style = value;
						}
						else {
							color = value;
						}
					}
					else if (i == 3) {
						if (color == null) {
							color = value;
						}
						else {
							System.out.println("Too many values: " + value);
						}
					}
					else {
						System.out.println("Too many values: " + value);
					}
				}
			}
			this.width = width;
			this.style = style;
			this.color = color;
		}
	}
}
