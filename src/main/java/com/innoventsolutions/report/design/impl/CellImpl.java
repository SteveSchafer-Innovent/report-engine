package com.innoventsolutions.report.design.impl;

import java.util.ArrayList;
import java.util.List;

import com.innoventsolutions.report.design.Cell;
import com.innoventsolutions.report.design.ReportComponent;

public class CellImpl implements Cell {
	private final List<ReportComponent> components;

	public CellImpl(final ReportComponent... components) {
		this.components = new ArrayList<>();
		for (final ReportComponent component : components) {
			this.components.add(component);
		}
	}

	@Override
	public List<ReportComponent> getComponents() {
		return components;
	}
}
