package org.openmrs.module.faces.reporting.builder;

import org.openmrs.PatientIdentifierType;
import org.openmrs.module.faces.Metadata;
import org.openmrs.module.faces.reporting.converter.*;
import org.openmrs.module.faces.reporting.definition.CurrentARTDef;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({ "kenyaemr.faces.common.report.FacesTXCurr" })
public class CurrentARTReportBuilder extends AbstractReportBuilder {
	
	public static final String ENC_DATE_FORMAT = "yyyy/MM/dd";
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	@Override
	protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
		return Arrays.asList(new Parameter("endDate", "Current on ART as at:", Date.class), new Parameter(
		        "dateBasedReporting", "", String.class));
	}
	
	@Override
	protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor reportDescriptor,
	        ReportDefinition reportDefinition) {
		return Arrays.asList(ReportUtils.map(datasetColumns(), "endDate=${endDate}"));
	}
	
	protected DataSetDefinition datasetColumns() {
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("FACES_TX_Curr");
		dsd.setDescription("FACES Custom query to output number of patients currently on treatment");
		dsd.addSortCriteria("Last Encounter", SortCriteria.SortDirection.ASC);
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		String paramMapping = "endDate=${endDate}";
		
		DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName} {middleName}");
		DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
		
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class,
		    Metadata.IdentifierType.UNIQUE_PATIENT_NUMBER);
		PatientIdentifierType serialno = MetadataUtils.existing(PatientIdentifierType.class,
		    Metadata.IdentifierType.PATIENT_CLINIC_NUMBER);
		
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition UPN = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        upn.getName(), upn), identifierFormatter);
		DataDefinition SerialNo = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(
		        serialno.getName(), serialno), identifierFormatter);
		
		LastTCADataDefinition lastTCADataDefinition = new LastTCADataDefinition();
		lastTCADataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		LastEncounterDataDefinition lastEncounterDataDefinition = new LastEncounterDataDefinition();
		lastEncounterDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		//		NEW DATA COLUMNS
		DateConfPosDataDefinition dateConfPosDataDefinition = new DateConfPosDataDefinition();
		dateConfPosDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		DateEnrolledDataDefinition dateEnrolledDataDefinition = new DateEnrolledDataDefinition();
		dateEnrolledDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		FDateStartedARTDataDefinition fDateStartedARTDataDefinition = new FDateStartedARTDataDefinition();
		fDateStartedARTDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		CurrentRegimenDataDefinition currentRegimenDataDefinition = new CurrentRegimenDataDefinition();
		currentRegimenDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		DateNextDrugPickDataDefinition dateNextDrugDataDefinition = new DateNextDrugPickDataDefinition();
		dateNextDrugDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		DaysNextDrugPickDataDefinition daysNextDrugDataDefinition = new DaysNextDrugPickDataDefinition();
		daysNextDrugDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		MonthsNextDrugPickDataDefinition monthsNextDrugDataDefinition = new MonthsNextDrugPickDataDefinition();
		monthsNextDrugDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		DiffCareModelDataDefinition careModelDataDefinition = new DiffCareModelDataDefinition();
		careModelDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		EntryPointDataDefinition entryPointDataDefinition = new EntryPointDataDefinition();
		entryPointDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addColumn("Name", nameDef, "");
		dsd.addColumn("id", new PatientIdDataDefinition(), "");
		dsd.addColumn("UPN", UPN, "");
		dsd.addColumn("Serial No", SerialNo, "");
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
		dsd.addColumn("Age", new AgeDataDefinition(), "");
		dsd.addColumn("Sex", new GenderDataDefinition(), "");
		dsd.addColumn("Last Encounter", lastEncounterDataDefinition, "endDate=${endDate}");
		dsd.addColumn("TCA", lastTCADataDefinition, "endDate=${endDate}");
		
		dsd.addColumn("Date Pos", dateConfPosDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Date Enrolled", dateEnrolledDataDefinition, "endDate=${endDate}");
		dsd.addColumn("ART Date", fDateStartedARTDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Current Regimen", currentRegimenDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Date Next Drug", dateNextDrugDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Days Next Drug", daysNextDrugDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Months Next Drug", monthsNextDrugDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Care Model", careModelDataDefinition, "endDate=${endDate}");
		dsd.addColumn("Entry Point", entryPointDataDefinition, "endDate=${endDate}");
		
		CurrentARTDef cd = new CurrentARTDef();
		cd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		dsd.addRowFilter(cd, paramMapping);
		return dsd;
		
	}
}
