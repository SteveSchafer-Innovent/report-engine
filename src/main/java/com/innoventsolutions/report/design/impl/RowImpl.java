package com.innoventsolutions.report.design.impl;

import java.util.ArrayList;
import java.util.List;

import com.innoventsolutions.report.design.Cell;
import com.innoventsolutions.report.design.Component;
import com.innoventsolutions.report.design.ReportComponent;
import com.innoventsolutions.report.design.Row;

public class RowImpl implements Row {
	private final List<Cell> cells = new ArrayList<>();

	public RowImpl(final Component... components) {
		for (final Component component : components) {
			if (component instanceof Cell) {
				cells.add((Cell) component);
			}
			else if (component instanceof ReportComponent) {
				cells.add(new CellImpl((ReportComponent) component));
			}
		}
	}

	@Override
	public List<Cell> getCells() {
		return cells;
	}
}
