package com.innoventsolutions.report.css;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermColor;
import cz.vutbr.web.css.TermFactory;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermLength;
import cz.vutbr.web.css.TermNumeric.Unit;
import cz.vutbr.web.csskit.Color;
import cz.vutbr.web.csskit.TermFactoryImpl;

/**
 * Static CSS parsing methods and objects. This is independent of any kind of
 * output.
 *
 * @author Steve Schafer
 */
public class CSS {
	private CSS() {
	}

	public static final float dpi = 72.0F;

	public static float convertLength(final TermLength termLength) {
		final float value = termLength.getValue().floatValue();
		final TermLength.Unit unit = termLength.getUnit();
		switch (unit) {
		case pt:
			return value;
		case in:
			return value * dpi;
		case cm:
			return (value * dpi) / 2.54f;
		case mm:
			return (value * dpi) / 25.4f;
		case q:
			return (value * dpi) / (2.54f * 40f);
		case pc:
			return (value * 12 * dpi) / 72.0f;
		case px:
			return value;
		case em:
			return MediaSpec.em * value;
		case ex:
			return MediaSpec.ex * value;
		default:
			return 0.0F;
		}
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
	static Map<String, Supplier<Term<?>>> DEFAULT_STYLE_SUPPLIERS;
	static {
		final Map<String, Supplier<Term<?>>> map = new HashMap<>();
		map.put("padding-top", () -> {
			return TermFactoryImpl.getInstance().createLength(0.0F, Unit.pt);
		});
		map.put("padding-right", () -> {
			return TermFactoryImpl.getInstance().createLength(0.0F, Unit.pt);
		});
		map.put("padding-bottom", () -> {
			return TermFactoryImpl.getInstance().createLength(0.0F, Unit.pt);
		});
		map.put("padding-left", () -> {
			return TermFactoryImpl.getInstance().createLength(0.0F, Unit.pt);
		});
		map.put("border-top-width", () -> {
			return TermFactoryImpl.getInstance().createLength(2.0F, Unit.pt);
		});
		map.put("border-right-width", () -> {
			return TermFactoryImpl.getInstance().createLength(2.0F, Unit.pt);
		});
		map.put("border-bottom-width", () -> {
			return TermFactoryImpl.getInstance().createLength(2.0F, Unit.pt);
		});
		map.put("border-left-width", () -> {
			return TermFactoryImpl.getInstance().createLength(2.0F, Unit.pt);
		});
		map.put("border-top-style", () -> {
			return TermFactoryImpl.getInstance().createIdent("none");
		});
		map.put("border-right-style", () -> {
			return TermFactoryImpl.getInstance().createIdent("none");
		});
		map.put("border-bottom-style", () -> {
			return TermFactoryImpl.getInstance().createIdent("none");
		});
		map.put("border-left-style", () -> {
			return TermFactoryImpl.getInstance().createIdent("none");
		});
		map.put("border-top-color", () -> {
			return TermFactoryImpl.getInstance().createColor(0, 0, 0);
		});
		map.put("border-right-color", () -> {
			return TermFactoryImpl.getInstance().createColor(0, 0, 0);
		});
		map.put("border-bottom-color", () -> {
			return TermFactoryImpl.getInstance().createColor(0, 0, 0);
		});
		map.put("border-left-color", () -> {
			return TermFactoryImpl.getInstance().createColor(0, 0, 0);
		});
		map.put("font-family", () -> {
			return TermFactoryImpl.getInstance().createIdent("helvetica");
		});
		map.put("font-size", () -> {
			return TermFactoryImpl.getInstance().createIdent("medium");
		});
		map.put("font-weight", () -> {
			return TermFactoryImpl.getInstance().createIdent("normal");
		});
		map.put("font-style", () -> {
			return TermFactoryImpl.getInstance().createIdent("normal");
		});
		map.put("color", () -> {
			return TermFactoryImpl.getInstance().createColor(0, 0, 0);
		});
		map.put("text-align", () -> {
			return TermFactoryImpl.getInstance().createIdent("left");
		});
		DEFAULT_STYLE_SUPPLIERS = map;
	}

	public interface Style {
		void apply(Applier applier);
	}

	interface StyleFactory {
		String getName();

		Style parse(List<Term<?>> object);
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
			public Style parse(final List<Term<?>> list) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						switch (list.size()) {
						case 4:
							applier.applyPadding(Side.TOP, getTermLength(list, 0, 0F, Unit.pt));
							applier.applyPadding(Side.RIGHT, getTermLength(list, 1, 0F, Unit.pt));
							applier.applyPadding(Side.BOTTOM, getTermLength(list, 2, 0F, Unit.pt));
							applier.applyPadding(Side.LEFT, getTermLength(list, 3, 0F, Unit.pt));
							break;
						case 3:
							applier.applyPadding(Side.TOP, getTermLength(list, 0, 0F, Unit.pt));
							applier.applyPadding(Side.RIGHT, getTermLength(list, 1, 0F, Unit.pt));
							applier.applyPadding(Side.BOTTOM, getTermLength(list, 2, 0F, Unit.pt));
							applier.applyPadding(Side.LEFT, getTermLength(list, 1, 0F, Unit.pt));
							break;
						case 2:
							applier.applyPadding(Side.TOP, getTermLength(list, 0, 0F, Unit.pt));
							applier.applyPadding(Side.RIGHT, getTermLength(list, 1, 0F, Unit.pt));
							applier.applyPadding(Side.BOTTOM, getTermLength(list, 0, 0F, Unit.pt));
							applier.applyPadding(Side.LEFT, getTermLength(list, 1, 0F, Unit.pt));
							break;
						case 1:
							applier.applyPadding(Side.TOP, getTermLength(list, 0, 0F, Unit.pt));
							applier.applyPadding(Side.RIGHT, getTermLength(list, 0, 0F, Unit.pt));
							applier.applyPadding(Side.BOTTOM, getTermLength(list, 0, 0F, Unit.pt));
							applier.applyPadding(Side.LEFT, getTermLength(list, 0, 0F, Unit.pt));
							break;
						default:
							System.out.println("Too many values for padding");
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
			public Style parse(final List<Term<?>> list) {
				final TermLength termLength = getTermLength(list, 0F, Unit.pt);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyPadding(Side.TOP, termLength);
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
			public Style parse(final List<Term<?>> list) {
				final TermLength termLength = getTermLength(list, 0F, Unit.pt);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyPadding(Side.RIGHT, termLength);
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
			public Style parse(final List<Term<?>> list) {
				final TermLength termLength = getTermLength(list, 0F, Unit.pt);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyPadding(Side.BOTTOM, termLength);
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
			public Style parse(final List<Term<?>> list) {
				final TermLength termLength = getTermLength(list, 0F, Unit.pt);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyPadding(Side.LEFT, termLength);
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
			public Style parse(final List<Term<?>> object) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorder(Side.TOP, object);
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
			public Style parse(final List<Term<?>> object) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorder(Side.RIGHT, object);
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
			public Style parse(final List<Term<?>> object) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorder(Side.BOTTOM, object);
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
			public Style parse(final List<Term<?>> object) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorder(Side.LEFT, object);
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
			public Style parse(final List<Term<?>> object) {
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorder(Side.TOP, object);
						applier.applyBorder(Side.RIGHT, object);
						applier.applyBorder(Side.BOTTOM, object);
						applier.applyBorder(Side.LEFT, object);
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
			public Style parse(final List<Term<?>> list) {
				final TermLength termLength = getTermLength(list, 0F, Unit.pt);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderWidth(Side.TOP, termLength);
						applier.applyBorderWidth(Side.RIGHT, termLength);
						applier.applyBorderWidth(Side.BOTTOM, termLength);
						applier.applyBorderWidth(Side.LEFT, termLength);
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
			public Style parse(final List<Term<?>> list) {
				final TermIdent termIdent = getTermIdent(list, "none");
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderStyle(Side.TOP, termIdent);
						applier.applyBorderStyle(Side.RIGHT, termIdent);
						applier.applyBorderStyle(Side.BOTTOM, termIdent);
						applier.applyBorderStyle(Side.LEFT, termIdent);
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
			public Style parse(final List<Term<?>> list) {
				final TermColor termColor = getTermColor(list, 0, 0, 0, 255);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderColor(Side.TOP, termColor);
						applier.applyBorderColor(Side.RIGHT, termColor);
						applier.applyBorderColor(Side.BOTTOM, termColor);
						applier.applyBorderColor(Side.LEFT, termColor);
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
			public Style parse(final List<Term<?>> list) {
				final TermLength termLength = getTermLength(list, 0F, Unit.pt);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderWidth(Side.TOP, termLength);
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
			public Style parse(final List<Term<?>> list) {
				final TermLength termLength = getTermLength(list, 0F, Unit.pt);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderWidth(Side.RIGHT, termLength);
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
			public Style parse(final List<Term<?>> list) {
				final TermLength termLength = getTermLength(list, 0F, Unit.pt);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderWidth(Side.BOTTOM, termLength);
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
			public Style parse(final List<Term<?>> list) {
				final TermLength termLength = getTermLength(list, 0F, Unit.pt);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderWidth(Side.LEFT, termLength);
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
			public Style parse(final List<Term<?>> list) {
				final TermIdent termIdent = getTermIdent(list, "none");
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderStyle(Side.TOP, termIdent);
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
			public Style parse(final List<Term<?>> list) {
				final TermIdent termIdent = getTermIdent(list, "none");
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderStyle(Side.RIGHT, termIdent);
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
			public Style parse(final List<Term<?>> list) {
				final TermIdent termIdent = getTermIdent(list, "none");
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderStyle(Side.BOTTOM, termIdent);
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
			public Style parse(final List<Term<?>> list) {
				final TermIdent termIdent = getTermIdent(list, "none");
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderStyle(Side.LEFT, termIdent);
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
			public Style parse(final List<Term<?>> list) {
				final TermColor termColor = getTermColor(list, 0, 0, 0, 255);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderColor(Side.TOP, termColor);
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
			public Style parse(final List<Term<?>> list) {
				final TermColor termColor = getTermColor(list, 0, 0, 0, 255);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderColor(Side.RIGHT, termColor);
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
			public Style parse(final List<Term<?>> list) {
				final TermColor termColor = getTermColor(list, 0, 0, 0, 255);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderColor(Side.BOTTOM, termColor);
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
			public Style parse(final List<Term<?>> list) {
				final TermColor termColor = getTermColor(list, 0, 0, 0, 255);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBorderColor(Side.LEFT, termColor);
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
			public Style parse(final List<Term<?>> list) {
				final TermIdent termIdent = getTermIdent(list, "helvetica");
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyFontFamily(termIdent);
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
			public Style parse(final List<Term<?>> list) {
				final Term<?> term = getTerm(list,
					TermFactoryImpl.getInstance().createIdent("medium"));
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyFontSize(term);
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
			public Style parse(final List<Term<?>> list) {
				final TermIdent termIdent = getTermIdent(list, "normal");
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyFontWeight(termIdent);
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
			public Style parse(final List<Term<?>> list) {
				final TermIdent termIdent = getTermIdent(list, "normal");
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyFontStyle(termIdent);
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
			public Style parse(final List<Term<?>> list) {
				final TermColor termColor = getTermColor(list, 0, 0, 0, 255);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyColor(termColor);
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
			public Style parse(final List<Term<?>> list) {
				final TermIdent termIdent = getTermIdent(list, "left");
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyTextAlign(termIdent);
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
			public Style parse(final List<Term<?>> list) {
				final TermColor termColor = getTermColor(list, 0, 0, 0, 255);
				return new Style() {
					@Override
					public void apply(final Applier applier) {
						applier.applyBackgroundColor(termColor);
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
		public Map<String, List<Term<?>>> getCSSValues() {
			final Map<String, List<Term<?>>> cssValues = new HashMap<>();
			populateCssValuesMap(cssValues);
			return cssValues;
		}

		private void populateCssValuesMap(final Map<String, List<Term<?>>> cssValues) {
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
						final String propName = declaration.getProperty();
						removeShortcutTargets(propName, cssValues);
						System.out.println("property = " + propName);
						cssValues.put(propName, declaration.asList());
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
				e1.printStackTrace();
			}
			catch (final cz.vutbr.web.css.CSSException e1) {
				e1.printStackTrace();
			}
			final Set<String> effectiveProps = new HashSet<>();
			for (final String declaredProp : cssValues.keySet()) {
				effectiveProps.add(declaredProp);
				addEffectiveProps(declaredProp, effectiveProps);
			}
			System.out.println("  effective props = " + effectiveProps);
			// add defaults
			for (final String propName : CSS.DEFAULT_STYLE_SUPPLIERS.keySet()) {
				if (!effectiveProps.contains(propName)) {
					final Supplier<Term<?>> supplier = CSS.DEFAULT_STYLE_SUPPLIERS.get(propName);
					if (supplier != null) {
						final Term<?> defaultValue = supplier.get();
						final List<Term<?>> list = new ArrayList<>();
						list.add(defaultValue);
						cssValues.put(propName, list);
					}
				}
			}
			System.out.println("  with defaults = " + cssValues);
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

		private void removeShortcutTargets(final String propName, final Map<String, ?> cssValues) {
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
		final Map<String, List<Term<?>>> cssValues = styleMapHolder.getCSSValues();
		final List<Style> styleAppliers = new ArrayList<>();
		for (final String propName : cssValues.keySet()) {
			final List<Term<?>> list = cssValues.get(propName);
			System.out.println("    " + propName + ": " + list);
			final StyleFactory factory = STYLE_APPLIER_FACTORIES.get(propName);
			if (factory != null) {
				styleAppliers.add(factory.parse(list));
			}
			else {
				System.out.println("Cannot handle " + propName + " = " + list);
			}
		}
		return styleAppliers;
	}

	public static enum Side {
		TOP, RIGHT, BOTTOM, LEFT
	}

	public interface Applier {
		void applyTextAlign(TermIdent term);

		void applyBackgroundColor(TermColor term);

		void applyPadding(Side side, TermLength term);

		void applyBorder(Side side, List<Term<?>> list);

		void applyBorderWidth(Side side, TermLength term);

		void applyBorderStyle(Side side, TermIdent term);

		void applyBorderColor(Side side, TermColor term);

		void applyColor(TermColor term);

		void applyFontStyle(TermIdent term);

		void applyFontWeight(TermIdent term);

		void applyFontSize(Term<?> term);

		void applyFontFamily(TermIdent term);
	}

	protected static class BorderInfo {
		final TermLength width;
		final TermIdent style;
		final TermColor color;

		BorderInfo(final List<Term<?>> list) {
			final TermFactory termFactory = TermFactoryImpl.getInstance();
			TermLength width = termFactory.createLength(0F, Unit.pt);
			TermIdent style = termFactory.createIdent("none");
			TermColor color = termFactory.createColor(0, 0, 0);
			int i = 0;
			for (final Term<?> term : list) {
				if (i == 0) {
					if (term instanceof TermIdent) {
						style = (TermIdent) term;
					}
					else {
						width = (TermLength) term;
					}
				}
				else if (i == 2) {
					if (style == null) {
						style = (TermIdent) term;
					}
					else {
						color = (TermColor) term;
					}
				}
				else if (i == 3) {
					if (color == null) {
						color = (TermColor) term;
					}
					else {
						System.out.println("Too many values: " + term);
					}
				}
				else {
					System.out.println("Too many values: " + term);
				}
				i++;
			}
			this.width = width;
			this.style = style;
			this.color = color;
		}
	}

	private static TermIdent getTermIdent(final List<Term<?>> list, final String defaultValue) {
		if (list == null || list.isEmpty()) {
			return TermFactoryImpl.getInstance().createIdent(defaultValue);
		}
		if (list.size() > 1) {
			System.out.println("Additional terms ignored");
			Thread.dumpStack();
		}
		final Term<?> term = list.get(0);
		if (!(term instanceof TermIdent)) {
			throw new IllegalArgumentException("Expecting TermIdent");
		}
		final TermIdent termIdent = (TermIdent) term;
		if ("initial".equalsIgnoreCase(termIdent.getValue())) {
			return TermFactoryImpl.getInstance().createIdent(defaultValue);
		}
		return termIdent;
	}

	private static TermLength getTermLength(final List<Term<?>> list, final float defaultValue,
			final Unit defaultUnit) {
		if (list == null || list.isEmpty()) {
			return TermFactoryImpl.getInstance().createLength(defaultValue, defaultUnit);
		}
		if (list.size() > 1) {
			System.out.println("Additional terms ignored");
			Thread.dumpStack();
		}
		final Term<?> term = list.get(0);
		if (term instanceof TermIdent) {
			final TermIdent termIdent = (TermIdent) term;
			if ("initial".equalsIgnoreCase(termIdent.getValue())) {
				return TermFactoryImpl.getInstance().createLength(defaultValue, defaultUnit);
			}
		}
		if (!(term instanceof TermLength)) {
			throw new IllegalArgumentException("Expecting a TermLength");
		}
		return (TermLength) term;
	}

	private static TermLength getTermLength(final List<Term<?>> list, final int index,
			final float defaultValue, final Unit defaultUnit) {
		if (list == null || list.size() < index + 1) {
			return TermFactoryImpl.getInstance().createLength(defaultValue, defaultUnit);
		}
		final Term<?> term = list.get(index);
		if (term instanceof TermIdent) {
			final TermIdent termIdent = (TermIdent) term;
			if ("initial".equalsIgnoreCase(termIdent.getValue())) {
				return TermFactoryImpl.getInstance().createLength(defaultValue, defaultUnit);
			}
		}
		if (!(term instanceof TermLength)) {
			throw new IllegalArgumentException("Expecting a TermLength");
		}
		return (TermLength) term;
	}

	private static TermColor getTermColor(final List<Term<?>> list, final int red, final int green,
			final int blue, final int alpha) {
		if (list == null || list.isEmpty()) {
			return TermFactoryImpl.getInstance().createColor(red, green, blue, alpha);
		}
		if (list.size() > 1) {
			System.out.println("Additional terms ignored");
			Thread.dumpStack();
		}
		final Term<?> term = list.get(0);
		if (term instanceof TermIdent) {
			final TermIdent termIdent = (TermIdent) term;
			if ("initial".equalsIgnoreCase(termIdent.getValue())) {
				return TermFactoryImpl.getInstance().createColor(red, green, blue, alpha);
			}
		}
		if (!(term instanceof TermColor)) {
			throw new IllegalArgumentException("Expecting a TermColor");
		}
		return (TermColor) term;
	}

	private static Term<?> getTerm(final List<Term<?>> list, final Term<?> defaultTerm) {
		if (list == null || list.isEmpty()) {
			return defaultTerm;
		}
		if (list.size() > 1) {
			System.out.println("Additional terms ignored");
			Thread.dumpStack();
		}
		return list.get(0);
	}
}
