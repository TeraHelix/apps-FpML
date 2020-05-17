package io.terahelix.apps.FpML.transform;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ColumnMapping.ColumnDef;
import ColumnMapping.MappingDefinition;
import io.terahelix.common.HelixRuntimeException;

/**
 * The generic transform function 
 * 
 * @author jsteenkamp
 *
 */
@FunctionalInterface
public interface TransformFunction
{
	/**
	 * Given a mapping definition and a set of source input values, returns the map of the output target columns.
	 * 
	 * @param mappingDefinition
	 * @param sourceInputValues
	 * @return
	 */
	public Map<ColumnDef, String> invoke(MappingDefinition mappingDefinition, Map<ColumnDef, String> sourceInputValues) throws TransformException;
	
	
	/**
	 * Convenience method to get the column definition by name.
	 * 
	 * @param columns
	 * @return
	 */
	public static ColumnDef getByName(final Map<ColumnDef, String> columns, final String columnName)
	{
		Optional<ColumnDef> colDef = columns.keySet().stream().filter(cd -> cd.getName().equals(columnName)).findAny();
		return colDef.orElseThrow(() -> new HelixRuntimeException("Could not find the column name - '" + columnName + "' - available columns was : " + columns.keySet().stream().map(f -> f.getName()).collect(Collectors.toList())));
	}


	public static ColumnDef getByName(final List<ColumnDef> columns,
	                                  final String columnName)
	{

		Optional<ColumnDef> colDef = columns.stream().filter(cd -> cd.getName().equals(columnName)).findAny();
		return colDef.orElseThrow(() -> new HelixRuntimeException("Could not find the column name - '" + columnName + "' - available columns was : " + columns.stream().map(f -> f.getName()).collect(Collectors.toList())));
	}
	
}









