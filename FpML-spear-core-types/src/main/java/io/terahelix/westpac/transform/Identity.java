package io.terahelix.westpac.transform;

import java.util.List;
import java.util.Map;

import ColumnMapping.ColumnDef;
import ColumnMapping.MappingDefinition;

/**
 * The identity transform function
 * 
 * @author jsteenkamp
 *
 */
public class Identity implements TransformFunction
{
	@Override
	public Map<ColumnDef, String> invoke(	MappingDefinition mappingDefinition,
											Map<ColumnDef, String> sourceInputValues)
	{
		if(sourceInputValues.size() != 1)
		{
			throw new TransformException("Identity only expects source inputs of size 1"); 
		}
		List<ColumnDef> targetColumns = mappingDefinition.getTargetColumns();
		
		if(targetColumns.size() != 1)
		{
			throw new TransformException("Identity only expects target inputs of size 1"); 
		}
		
		return Map.of(targetColumns.get(0), sourceInputValues.values().stream().findFirst().get());
	}

}
