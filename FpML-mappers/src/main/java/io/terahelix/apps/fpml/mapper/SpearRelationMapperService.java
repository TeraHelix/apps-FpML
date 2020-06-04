package io.terahelix.apps.fpml.mapper;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import ColumnMapping.ColumnDef;
import ColumnMapping.ColumnDef_Builder;
import ColumnMapping.Definition;
import ColumnMapping.MappingDefinition;
import ColumnMapping.MappingDefinition_Builder;
import Org.FpML.Confirmation.Product;
import Org.FpML.Confirmation.Product_Builder;
import Org.FpML.Confirmation.Trade;
import XSD.FpML.ComplexityLevel;
import XSD.FpML.SourceTypeOfRelation;
import io.terahelix.apps.FpML.transform.Identity;
import io.terahelix.apps.FpML.transform.TransformFunction;
import io.terahelix.common.HelixLogger;
import io.terahelix.common.HelixRuntimeException;
import io.terahelix.spear.javaRuntime.SPTCorruptedException;
import io.terahelix.spear.javaRuntime.SPTHelpers;
import io.terahelix.spear.javaRuntime.SpearBuilder;
import io.terahelix.spear.javaRuntime.SpearObject;
import io.terahelix.spear.javaRuntime.SpearRelation;
import io.terahelix.spear.javaRuntime.SpearStruct;
import io.terahelix.spear.javaRuntime.typeDescriptors.TypeDescriptor;
import io.terahelix.spear.javaRuntime.util.SpearClasses;
import io.terahelix.spear.javaRuntime.util.SpearHelpers;
import io.terahelix.spear.lang.spearParseTree.SPT.Member;
import io.terahelix.spear.lang.spearParseTree.SPT.Struct;
import io.terahelix.spear.lang.spearParseTree.SPT.Type;

/**
 * A service for mapping to/from relations - given some assumptions in the FpML project. 
 * 
 * @author jsteenkamp
 *
 */
public class SpearRelationMapperService
{
	private static final HelixLogger logger = HelixLogger.getLogger(SpearRelationMapperService.class);
	
	public static final String DEFAULT_MAPPER_NAME = "Default";
	
	private final static ReentrantLock _instanceLock = new ReentrantLock();
	private final static AtomicReference<SpearRelationMapperService> _instance = new AtomicReference<>();
	
	public static SpearRelationMapperService getInstance()
	{
		SpearRelationMapperService srv = _instance.get();
		if(srv != null)
		{
			return srv;
		}
		
		_instanceLock.lock();
		try
		{
			srv = _instance.get();
			if(srv != null)
			{
				return srv;
			}
			srv = new SpearRelationMapperService();
			_instance.set(srv);
			return srv;
		}
		finally
		{
			_instanceLock.unlock();	
		}
	}
	
	private final SortedMap<Class<?>, Class<?>> typesToRelations;
	private final SortedMap<Class<?>, Class<?>> relationsToTypes;
	private final SortedMap<Integer, SortedSet<Class<?>>> relationsToLevels;
	
	class ClassSorter implements Comparator<Class<?>>
	{
		@Override
		public int compare(	Class<?> o1,
							Class<?> o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
		
	}
	
	private SpearRelationMapperService()
	{
		try
		{
			final SortedMap<Class<?>, Class<?>> typesToRelationsPre = new ConcurrentSkipListMap<>(new ClassSorter());
			final SortedMap<Class<?>, Class<?>> relationsToTypesPre = new ConcurrentSkipListMap<>(new ClassSorter());
			final SortedMap<Integer, SortedSet<Class<?>>> relationsToLevelsPre = new ConcurrentSkipListMap<>();
		 
			Set<String> allSPTNames = SPTHelpers.getAllSPTNames();
			for(String spt : allSPTNames)
			{
				Type sptType = SPTHelpers.getSPT(spt);
				if(sptType instanceof Struct == false) continue;
				Struct structSPT = (Struct)sptType;

				Optional<SourceTypeOfRelation> relType = extractSourceRefAnnotation(structSPT);
				if(relType.isEmpty()) continue;

				SourceTypeOfRelation sourceType = relType.get();
				String sourceTypeName = sourceType.getTypeName();

				Class<?> sourceTypeClass = SpearClasses.forName(sourceTypeName);
				Class<?> relationTypeClass = SpearClasses.forName(structSPT.getName());

				relationsToTypesPre.put(relationTypeClass, sourceTypeClass);
				typesToRelationsPre.put(sourceTypeClass, relationTypeClass);

				Optional<ComplexityLevel> complOpt = extractComplexityAnnotation(structSPT);
				if(complOpt.isPresent() == true)
				{
					SortedSet<Class<?>> innerSet = relationsToLevelsPre.computeIfAbsent( complOpt.get().getLevel(), s -> new TreeSet<>(new ClassSorter()));
					innerSet.add(relationTypeClass);
				}
			}
			
			typesToRelations = Collections.unmodifiableSortedMap(typesToRelationsPre);
			relationsToTypes = Collections.unmodifiableSortedMap(relationsToTypesPre);
			relationsToLevels = Collections.unmodifiableSortedMap(relationsToLevelsPre);
		} 
		catch (SPTCorruptedException e)
		{
			throw new HelixRuntimeException("Unable to extract the SPT Type : " + e, e);
		} 
		catch (ClassNotFoundException e)
		{
			throw new HelixRuntimeException("Unable to extract the SPT Type : " + e, e);
		}
	}
	
	
	private static Optional<SourceTypeOfRelation> extractSourceRefAnnotation(Struct structSPT)
	{
		Optional<List<SpearObject>> annotationListOpt = structSPT.getAnnotationList();
		if(annotationListOpt.isEmpty()) return Optional.empty();
		
		List<SpearObject> annotationList = annotationListOpt.get();
		for(SpearObject obj : annotationList)
		{
			if(obj instanceof SourceTypeOfRelation)
			{
				return Optional.of((SourceTypeOfRelation)obj);
			}
		}
		return Optional.empty();
	}
	
	private static Optional<ComplexityLevel> extractComplexityAnnotation(Struct structSPT)
	{
		Optional<List<SpearObject>> annotationListOpt = structSPT.getAnnotationList();
		if(annotationListOpt.isEmpty()) return Optional.empty();
		
		List<SpearObject> annotationList = annotationListOpt.get();
		for(SpearObject obj : annotationList)
		{
			if(obj instanceof ComplexityLevel)
			{
				return Optional.of((ComplexityLevel)obj);
			}
		}
		return Optional.empty();
	}
	
	public Map<Integer, SortedSet<Class<?>>> getRelationsToLevels()
	{
		return relationsToLevels;
	}
	
	public List<Struct> getRelationTypesWithComplexityLevel(int level)
	{
		Set<Class<?>> relTypes = relationsToLevels.get(level);
		if(relTypes == null)
		{
			return relationsToTypes.keySet().stream().map(r -> (Struct)SPTHelpers.getSPT(r)).collect(Collectors.toList());
		}
		return relTypes.stream().map(clz -> (Struct)SPTHelpers.getSPT(clz) ).collect(Collectors.toList());
	}
	
	public List<Struct> getTypesWithComplexityLevel(int level)
	{
		List<Struct> allRelationTypes = getRelationTypesWithComplexityLevel(level);
		return allRelationTypes.stream().map(struct -> 
		{
			try
			{
				Class<?> cls = SpearClasses.forName(struct.getName());
				Class<?> typeMapped = relationsToTypes.get(cls);
				return (Struct)SPTHelpers.getSPT(typeMapped);
			} 
			catch (ClassNotFoundException e)
			{
				throw new HelixRuntimeException("Could not load the struct name : " + struct.getName(), e);
			}
			
		}).collect(Collectors.toList());
	}
	
	public Struct getTypeForSelectedRelation(Struct selRelation)
	{
		try
		{
			Optional<Class<?>> typeForSelectedRelation = getTypeForSelectedRelation(SpearClasses.forName(selRelation.getName()));
			Class<?> type = typeForSelectedRelation.orElseThrow(() -> new HelixRuntimeException("No mapping found for : " + selRelation.getName() + " - this is unexpected !"));
			return SPTHelpers.getSPT(type);
		} 
		catch (ClassNotFoundException e)
		{
			throw new HelixRuntimeException("No mapping found for : " + selRelation.getName() + " - this is unexpected ! Exception was : " + e, e);
		}
	}
	
	public Optional<Class<?>> getTypeForSelectedRelation(Class<?> reltype)
	{
		Class<?> type = relationsToTypes.get(reltype);
		return Optional.ofNullable(type);
	}

	public Struct getRelationForSelectedType(Struct selType)
	{
		try
		{
			Class<?> type = typesToRelations.get(SpearClasses.forName(selType.getName()));
			if(type == null)
			{
				throw new HelixRuntimeException("No mapping found for : " + selType.getName() + " - this is unexpected !");
			}
			return SPTHelpers.getSPT(type);
		} 
		catch (ClassNotFoundException e)
		{
			throw new HelixRuntimeException("No mapping found for : " + selType.getName() + " - this is unexpected ! Exception was : " + e, e);
		}
	}


	/**
	 * Creates a relation from the given string[]. The
	 * order that is assumed is that of the ColumnMapping.Definition's source columns.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends SpearStruct> T constructRelationFromStrings(final ColumnMapping.Definition colDefMapping, 
	                                                              final String[] columnValues)
	{
		final String targetRelationType = colDefMapping.getTargetRelation();
		final SpearStruct targetRelation = (SpearStruct)SpearHelpers.create(targetRelationType);
		List<MappingDefinition> mappingDefinitions = colDefMapping.getMappingDefinitions();
		
		int colValuesIndex = 0;
		
		for(MappingDefinition mapping : mappingDefinitions)
		{
			final TransformFunction transFunc = getTransformFunction(mapping);
			
			List<ColumnDef> sourceColumns = mapping.getSourceColumns();
			Map<ColumnDef, String> sourceColumnVals = new LinkedHashMap<>(sourceColumns.size());
			
			for(ColumnDef colVal : sourceColumns)
			{
				sourceColumnVals.put(colVal, columnValues[colValuesIndex]);
				colValuesIndex++;	
			}
			
			final Map<ColumnDef, String> targetColumnsResult = transFunc.invoke(mapping, sourceColumnVals);
			
			targetColumnsResult.forEach((k,v) -> 
			{
				if(v != null && v.isEmpty() == false)
				{
					try
					{
						TypeDescriptor typeDescr = targetRelation.$getTypeDescriptorAt(k.getName());
						Object targetObject = typeDescr.convertObject(v);
						targetRelation.set(k.getName(), targetObject);
					} 
					catch (Exception e)
					{
						String msg = "Unable to convert the string value '" + v + "' to a value acceptable for the field - " + k.getName() + " - of relation : " + colDefMapping.getTargetRelation() + " | Exception was : " + e;
						logger.info(msg, e);
						throw new HelixRuntimeException(msg, e);
					}
				}
			});
		}
		
		final Optional<Map<String, String>> defColumValuesOpt = colDefMapping.getDefaultValueColumns();
		if(defColumValuesOpt.isPresent() == true)
		{
			Map<String, String> defColumnValues = defColumValuesOpt.get();	
			defColumnValues.forEach((k,v) -> 
			{
				targetRelation.set(k, v);
			});
		}
		
		return (T)targetRelation.makeImmutable();
	}
	
	private TransformFunction getTransformFunction(MappingDefinition mapping)
	{
		try
		{
			String transformFunctionClass = mapping.getTransformFunction();
			Class<?> typeName = SpearClasses.forName(transformFunctionClass);
			return (TransformFunction)typeName.getConstructor().newInstance();
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			throw new HelixRuntimeException("Unable to instantiate the transform function : " + e, e);
		}
	}
	
	@SuppressWarnings("unused")
	private SpearStruct randomizeOrCreateType(Class<?> type) 
	{
		try
		{
			SpearStruct createdType = (SpearStruct)SpearHelpers.randomize((Class)type);
			return createdType;
		} 
		catch (Exception e)
		{
			logger.warn("Unable to randomize the given type ! " + e + " | Falling back to normal create ");
			SpearStruct createdType = (SpearStruct)SpearHelpers.create((Class)type);
			return createdType;
		}
	}
	
	/**
	 * 
	 * @param relationClass
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public ColumnMapping.Definition getDefaultMappingDefinition(Class<?> relationClass)
	{
		ColumnMapping.Definition_Builder bldr = ColumnMapping.Definition.Create();
		bldr.setName(DEFAULT_MAPPER_NAME);
		bldr.setTargetRelation(relationClass.getName());
		List<MappingDefinition> mappingDefs = bldr.getOrInitMappingDefinitions();
		
		Optional<Class<?>> typeFoRelationOpt = getTypeForSelectedRelation(relationClass);
		Class typeForRelation = typeFoRelationOpt.orElseThrow(() -> new HelixRuntimeException("Unable to find a type for the the relation: " + relationClass.getName() + " | Something must be wrong here !"));
		
		SpearStruct createdType = randomizeOrCreateType(typeForRelation);
		SpearStruct createdRelation = (SpearStruct)SpearHelpers.create((Class)relationClass);
		SpearHelpers.CopyToRelation(createdType, (SpearRelation)createdRelation);
		
		Struct relationSPT = SPTHelpers.getSPT(relationClass);
		List<Member> members = relationSPT.getMembers();
		
		for(Member m : members)
		{
			String colName = m.getName();
			
			if(colName.endsWith("SPEAR_TYPE") || colName.equals("MERGED_INFO_TYPE"))
			{
				Optional<Object> autoGenValueOpt = createdRelation.get(colName);
				Object val = autoGenValueOpt.orElse("No AutoGen for column '" + colName + "' from the relation : " + relationClass.getName());
				bldr.getOrInitDefaultValueColumns().put(colName, String.valueOf(val));
				continue;
			}
			
			MappingDefinition_Builder mapB = MappingDefinition.Create();
			
			ColumnDef_Builder sourceCol = ColumnDef.Create();
			sourceCol.setName(colName);
			sourceCol.setType(m.getType().toString());
			mapB.getOrInitSourceColumns().add(sourceCol.build());
			
			ColumnDef_Builder targetCol = ColumnDef.Create();
			targetCol.setName(colName);
			targetCol.setType(m.getType().toString());
			mapB.setTargetColumns(List.of(targetCol.build()));
			mapB.setTransformFunction(Identity.class.getName());
			
			mappingDefs.add(mapB);
		}
			
		return bldr.build();
	}
	
	/**
	 * Returns the relation type for 
	 * @param spearTypeClass
	 * @return
	 */
	public Optional<Class<?>> getRelationType(Class<?> spearTypeClass)
	{
		Class<?> res = typesToRelations.get(spearTypeClass);
		return Optional.ofNullable(res);
	}
	
	/**
	 * Gets the relation type (if applicable to extract from the relation)
	 * 
	 * @param products
	 * @return
	 */
	public Optional<Class<?>> getProductRelationType(List<Product> products)
	{
		Class<?> relType = null;
		for(Product p : products)
		{
			Optional<Class<?>> discoveredOpt = getRelationType(p.$getSpearAppClass());
			if(discoveredOpt.isEmpty()) continue;
			
			Class<?> discovered = discoveredOpt.get();
			if(relType == null)
			{
				relType = discovered;
			}
			else if(relType.equals(discovered) == false)
			{
				return Optional.empty();
			}
		}
		return Optional.ofNullable(relType);
	}
	
	/**
	 * Creates a relation from the given object.
	 * 
	 * @param input
	 * @return
	 */
	public SpearObject toRelation(SpearObject input)
	{
		Optional<Class<?>> relationTypeOpt = getRelationType(input.getSpearAppClass());
		Class<?> relationType = relationTypeOpt.orElseThrow(() -> new HelixRuntimeException("Could not find an equivalent relation class for : " + input.getSpearAppClass().getName() + " | Perhaps you haven't generated it?"));
		
		SpearRelation relInstance = (SpearRelation)SpearHelpers.create(relationType.getName());
		SpearRelation output = SpearHelpers.CopyToRelation((SpearStruct)input, relInstance);
		
		return ((SpearBuilder)(output)).build();
	}

	public int getNumberOfSourceColumns(final ColumnMapping.Definition colDef)
	{
		return colDef.getMappingDefinitions().stream().mapToInt(m -> m.getSourceColumns().size()).sum();
	}

	public int getNumberOfTargetColumns(final ColumnMapping.Definition colDef)
	{
		return colDef.getMappingDefinitions().size();
	}
	
	public Object[][] generateRandomRowValues(Definition colDef)
	{
		try
		{
			final String targetRel = colDef.getTargetRelation();
			List<SpearStruct> allItems = SpearHelpers.randomizeMany(10, (Class)SpearClasses.forName(targetRel));
			
			int targetColCount = getNumberOfTargetColumns(colDef);
			
			final Object[][] rndRows = new Object[10][];
			int rowIndex = 0;
			int colIndex = 0;
			
			for(SpearStruct struct : allItems)
			{
				colIndex = 0;
				rndRows[rowIndex] = new Object[targetColCount];
				
				final List<MappingDefinition> mappingDefs = colDef.getMappingDefinitions();
				for(MappingDefinition m : mappingDefs)
				{
					ColumnDef targetColumn = m.getTargetColumns().get(0);
					String targetColName = targetColumn.getName();
					
					Optional<Object> objItem = struct.get(targetColName);
					if(objItem.isPresent() == true)
					{
						rndRows[rowIndex][colIndex] = objItem.get();
					}
					else
					{
						rndRows[rowIndex][colIndex] = null;	
					}
					colIndex++;
				}
				rowIndex++;
			}
			return rndRows;
		} 
		catch (ClassNotFoundException e)
		{
			throw new HelixRuntimeException("Failure in creating the relation : " + e);
		}
	}
	
	
	public List<Product> extractProducts(SpearObject input, String originFile)
	{
		if(input instanceof SpearStruct == false)
		{
			throw new HelixRuntimeException("Cannot convert to a product relation - as this type is not a Struct");
		}
		
		SpearStruct struct = (SpearStruct)input;
		int tradeIndex = struct.$getIndexOfName("Trade");
		
		if(tradeIndex == -1)
		{
			return Collections.emptyList();
			//	throw new HelixRuntimeException("Unable to extract the products - as you do not have a Trade Contained !");
		}
		
		Optional<Object> tradeObjectOpt = struct.get(tradeIndex);
		if(tradeObjectOpt.isEmpty() == true)
		{
			return Collections.emptyList();
		}
		Object tradeObject = tradeObjectOpt.get();
		List<Trade> item = new ArrayList<>();
		if(tradeObject instanceof Trade)
		{
			item.add((Trade)tradeObject);
		}
		else if(tradeObject instanceof List)
		{
			item.addAll((List<Trade>)tradeObject);
		}
		else
		{
			logger.warn("No Idea what to do with : " + tradeObject );
			return Collections.emptyList();
		}
		
		List<Product> products = new ArrayList<>();
		  
		int idCount = 0;
		DecimalFormat dmcf = new DecimalFormat("000000");
		
		for(Trade trade : item)
		{	
			idCount++;
			
			Optional<Product> optProduct = trade.getProduct();
			if(optProduct.isPresent() == false) continue;
			
			Product product = optProduct.get();
			if(product.getId().isEmpty() == true)
			{
				product = ((Product_Builder)product.Clone()).setId(dmcf.format(idCount) + "-" + originFile ).build();
			}
			products.add(product);
		}
		
		return products;
	}
	
	public List<SpearObject> convertToProductRelations(SpearObject input, String originFile)
	{
		List<Product> products = extractProducts(input, originFile);
		if(products.isEmpty()) return Collections.emptyList();
		
		Optional<Class<?>> productRelType = getProductRelationType(products);
		
		if(productRelType.isEmpty() == true)
		{
			throw new HelixRuntimeException("Could not find the product relation type for the products - please check your setup. The product types were : \n" + products.stream().map(s -> s.$getSpearAppClass()).collect(Collectors.toList()));
		}
		
		final List<SpearObject> allRelations = products.stream().map(this::toRelation).collect(Collectors.toList());
		
		logger.info("Converted a total of " + allRelations.size() + " relations.");
		
		return allRelations;
	}


	public Map<Class<?>, Class<?>> getRelationToTypeMap()
	{
		return this.relationsToTypes;
	}


	public Map<Class<?>, Class<?>> getTypeToRelationMap()
	{
		return this.typesToRelations;
	}





	


	
	
	
	
}





















