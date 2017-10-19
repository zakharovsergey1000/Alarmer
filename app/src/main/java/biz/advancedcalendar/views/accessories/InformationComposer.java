package biz.advancedcalendar.views.accessories;

import biz.advancedcalendar.activities.accessories.InformationUnitMatrix;
import biz.advancedcalendar.greendao.Task.InformationUnitSelector;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitRow;
import java.util.ArrayList;
import java.util.Iterator;

public class InformationComposer {
	private InformationUnitMatrix informationUnitMatrix;
	private ArrayList<InformationUnitRow> informationUnitRows;
	private int rowsCount;

	public InformationComposer(InformationUnitMatrix informationUnitMatrix) {
		this.informationUnitMatrix = informationUnitMatrix;
		initialize();
	}

	private void initialize() {
		informationUnitRows = informationUnitMatrix.getInformationUnitRows();
		if (informationUnitRows != null) {
			for (Iterator<InformationUnitRow> rowIterator = informationUnitRows
					.iterator(); rowIterator.hasNext();) {
				InformationUnitRow informationUnitRow = rowIterator.next();
				ArrayList<InformationUnit> informationUnits = informationUnitRow
						.getInformationUnits();
				for (Iterator<InformationUnit> columnIterator = informationUnits
						.iterator(); columnIterator.hasNext();) {
					InformationUnit informationUnit = columnIterator.next();
					InformationUnitSelector informationUnitSelector = informationUnit
							.getInformationUnitSelector();
					if (informationUnitSelector == null) {
						columnIterator.remove();
					}
				}
				int size = informationUnits.size();
				if (size == 0) {
					rowIterator.remove();
				}
			}
		} else {
			informationUnitRows = new ArrayList<InformationUnitRow>();
			informationUnitMatrix.setInformationUnitRows(informationUnitRows);
		}
		rowsCount = informationUnitRows.size();
	}

	public InformationUnitMatrix getInformationUnitMatrix() {
		return informationUnitMatrix;
	}

	public void setInformationUnitMatrix(InformationUnitMatrix informationUnitMatrix) {
		this.informationUnitMatrix = informationUnitMatrix;
		initialize();
	}

	public int getRowsCount() {
		return rowsCount;
	}

	public InformationUnitRow getInformationUnitRow(int rowIndex) {
		return informationUnitRows.get(rowIndex);
	}

	public void removeInformationUnitRow(int rowIndex) {
		informationUnitRows.remove(rowIndex);
		rowsCount = informationUnitRows.size();
	}

	public void addInformationUnits(ArrayList<InformationUnit> informationUnits) {
		InformationUnitRow informationUnitRow = new InformationUnitRow(informationUnits);
		informationUnitRows.add(informationUnitRow);
		rowsCount = informationUnitRows.size();
	}

	public void addInformationUnits(ArrayList<InformationUnit> informationUnits,
			int rowIndex) {
		InformationUnitRow informationUnitRow = new InformationUnitRow(informationUnits);
		informationUnitRows.add(rowIndex, informationUnitRow);
		rowsCount = informationUnitRows.size();
	}

	public ArrayList<InformationUnit> getInformationUnits(int rowIndex) {
		InformationUnitRow informationUnitRow = informationUnitRows.get(rowIndex);
		return informationUnitRow.getInformationUnits();
	}
}
