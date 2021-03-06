/*
 * Copyright 2016 (C) Tom Parker <thpr@users.sourceforge.net>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package plugin.function;

import java.util.Arrays;

import pcgen.base.formula.base.DependencyManager;
import pcgen.base.formula.base.EvaluationManager;
import pcgen.base.formula.base.FormulaManager;
import pcgen.base.formula.base.FormulaSemantics;
import pcgen.base.formula.base.Function;
import pcgen.base.formula.parse.Node;
import pcgen.base.formula.visitor.DependencyVisitor;
import pcgen.base.formula.visitor.EvaluateVisitor;
import pcgen.base.formula.visitor.SemanticsVisitor;
import pcgen.base.formula.visitor.StaticVisitor;
import pcgen.base.util.FormatManager;
import pcgen.cdom.format.table.ColumnFormatManager;
import pcgen.cdom.format.table.DataTable;
import pcgen.cdom.format.table.TableColumn;
import pcgen.cdom.format.table.TableFormatManager;

/**
 * This is a Lookup function for finding items in a DataTable.
 * 
 * This function requires 3 arguments: (1) The Table Name (2) The Value to be
 * looked up in the first column (3) The Column name of the result to be
 * returned
 */
public class LookupFunction implements Function
{

	/**
	 * A constant referring to the TableColumn Class.
	 */
	@SuppressWarnings("rawtypes")
	private static final Class<TableColumn> COLUMN_CLASS = TableColumn.class;

	/**
	 * A constant referring to the Table Class.
	 */
	private static final Class<DataTable> DATATABLE_CLASS = DataTable.class;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFunctionName()
	{
		return "Lookup";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean isStatic(StaticVisitor visitor, Node[] args)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FormatManager<?> allowArgs(SemanticsVisitor visitor, Node[] args,
		FormulaSemantics semantics)
	{
		int argCount = args.length;
		if (argCount != 3)
		{
			semantics.setInvalid("Function " + getFunctionName()
				+ " received incorrect # of arguments, expected: 3 got "
				+ args.length + " " + Arrays.asList(args));
			return null;
		}

		//Table name node (must be a DataTable)
		semantics.push(FormulaSemantics.ASSERTED, DATATABLE_CLASS);
		@SuppressWarnings("PMD.PrematureDeclaration")
		Object tableFormat = args[0].jjtAccept(visitor, semantics);
		semantics.pop(FormulaSemantics.ASSERTED);
		if (!semantics.isValid())
		{
			return null;
		}
		if (!(tableFormat instanceof TableFormatManager))
		{
			semantics.setInvalid(
				"Parse Error: Invalid Object: " + tableFormat.getClass()
					+ " found in location requiring a " + "TableFormatManager");
			return null;
		}
		@SuppressWarnings("unchecked")
		TableFormatManager tableFormatManager =
				(TableFormatManager) tableFormat;

		//Lookup value (at this point we don't know the format - only at runtime)
		FormatManager<?> lookupFormat = tableFormatManager.getLookupFormat();
		semantics.push(FormulaSemantics.ASSERTED,
			lookupFormat.getManagedClass());
		args[1].jjtAccept(visitor, semantics);
		semantics.pop(FormulaSemantics.ASSERTED);
		if (!semantics.isValid())
		{
			return null;
		}

		//Result Column Name (must be a String)
		semantics.push(FormulaSemantics.ASSERTED, COLUMN_CLASS);
		@SuppressWarnings("PMD.PrematureDeclaration")
		Object resultColumn = args[2].jjtAccept(visitor, semantics);
		semantics.pop(FormulaSemantics.ASSERTED);
		if (!semantics.isValid())
		{
			return null;
		}
		if (!(resultColumn instanceof ColumnFormatManager))
		{
			semantics.setInvalid("Parse Error: Invalid Result Column Name: "
				+ resultColumn.getClass()
				+ " found in location requiring a Column");
			return null;
		}
		ColumnFormatManager<?> cf = (ColumnFormatManager<?>) resultColumn;
		FormatManager<?> rf = tableFormatManager.getResultFormat();
		if (!rf.equals(cf.getComponentManager()))
		{
			semantics.setInvalid("Parse Error: Invalid Result Column Type: "
				+ resultColumn.getClass()
				+ " found in table that does not contain that type");
			return null;
		}
		return rf;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object evaluate(EvaluateVisitor visitor, Node[] args,
		EvaluationManager manager)
	{
		//Table name node (must be a Table)
		manager.push(EvaluationManager.ASSERTED, DATATABLE_CLASS);
		DataTable dataTable = (DataTable) args[0].jjtAccept(visitor, manager);
		manager.pop(EvaluationManager.ASSERTED);

		FormatManager<?> lookupFormat = dataTable.getFormat(0);

		//Lookup value (format based on the table)
		manager.push(EvaluationManager.ASSERTED,
			lookupFormat.getManagedClass());
		@SuppressWarnings("PMD.PrematureDeclaration")
		Object lookupValue = args[1].jjtAccept(visitor, manager);
		manager.pop(EvaluationManager.ASSERTED);

		//Result Column Name (must be a tableColumn)
		manager.push(EvaluationManager.ASSERTED, COLUMN_CLASS);
		TableColumn column = (TableColumn) args[2].jjtAccept(visitor, manager);
		manager.pop(EvaluationManager.ASSERTED);

		String columnName = column.getName();
		if (!dataTable.isColumn(columnName))
		{
			FormatManager<?> fmt = column.getFormatManager();
			System.out.println("Lookup called on invalid column: '" + columnName
				+ "' is not present on table '" + dataTable.getName()
				+ "' assuming default for " + fmt.getIdentifierType());
			FormulaManager fm = manager.peek(EvaluationManager.FMANAGER);
			return fm.getDefault(fmt.getManagedClass());
		}
		if (!dataTable.hasRow(lookupValue))
		{
			FormatManager<?> fmt = column.getFormatManager();
			System.out.println("Lookup called on invalid item: '" + lookupValue
				+ "' is not present in the first row of table '"
				+ dataTable.getName() + "' assuming default for "
				+ fmt.getIdentifierType());
			FormulaManager fm = manager.peek(EvaluationManager.FMANAGER);
			return fm.getDefault(fmt.getManagedClass());
		}
		return dataTable.lookupExact(lookupValue, columnName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void getDependencies(DependencyVisitor visitor,
		DependencyManager manager, Node[] args)
	{
		manager.push(DependencyManager.ASSERTED, DATATABLE_CLASS);
		args[0].jjtAccept(visitor, manager);
		manager.pop(DependencyManager.ASSERTED);

		//TODO a Semantics Check can tell what this is
		manager.push(DependencyManager.ASSERTED, null);
		args[1].jjtAccept(visitor, manager);
		manager.pop(DependencyManager.ASSERTED);

		manager.push(DependencyManager.ASSERTED, COLUMN_CLASS);
		args[2].jjtAccept(visitor, manager);
		manager.pop(DependencyManager.ASSERTED);
	}

}
