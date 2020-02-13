package org.openmrs.module.faces.reporting.converter;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

import java.util.Date;

@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
public class EntryPointDataDefinition extends BaseDataDefinition implements PatientDataDefinition {
	
	public Date endDate;
	
	public static final long serialVersionUID = 1L;
	
	/**
	 * Default Constructor
	 */
	public EntryPointDataDefinition() {
		super();
	}
	
	/**
	 * Constructor to populate name only
	 */
	public EntryPointDataDefinition(String name) {
		super(name);
	}
	
	//***** INSTANCE METHODS *****
	
	/**
	 * @see org.openmrs.module.reporting.data.DataDefinition#getDataType()
	 */
	public Class<?> getDataType() {
		return Date.class;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
