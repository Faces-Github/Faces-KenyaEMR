package org.openmrs.module.faces.reporting.definition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Handler(supports = { CurrentARTDef.class })
public class CurrentARTDefEval implements CohortDefinitionEvaluator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
		
		CurrentARTDef definition = (CurrentARTDef) cohortDefinition;
		
		if (definition == null)
			return null;
		
		Cohort newCohort = new Cohort();
		
		String qry = "SELECT patient_id FROM(SELECT \n"
		        + "DISTINCT(e.patient_id) AS patient_id,\n"
		        + "SUBSTRING_INDEX(GROUP_CONCAT(IF(o.concept_id IN(164855,1193,1088,1085),cn.name,null) ORDER BY e.encounter_datetime SEPARATOR '|'),'|',-1) AS Current_Regimen,\n"
		        + "Next_TCA \n"
		        + "FROM openmrs.encounter e \n"
		        + "INNER JOIN (\n"
		        + "SELECT \n"
		        + "e.patient_id as patient_id,\n"
		        + "SUBSTRING_INDEX(GROUP_CONCAT(IF(o.concept_id IN(5096),DATE(o.value_datetime),null) ORDER BY e.encounter_datetime SEPARATOR '|'),'|',-1) AS Next_TCA \n"
		        + "FROM openmrs.obs o \n"
		        + "JOIN openmrs.encounter e ON o.encounter_id=e.encounter_id \n"
		        + "AND o.voided=0 and e.voided=0 and o.concept_id in(5096) \n"
		        + "AND DATE(e.encounter_datetime) <= DATE(:endDate) \n"
		        + "JOIN openmrs.encounter_type et ON e.encounter_type=et.encounter_type_id \n"
		        + " AND et.name IN('HIV Enrollment','HIV Consultation','ART Refill') \n"
		        + "and et.retired=0\n"
		        + "group by patient_id \n"
		        + ")lv ON e.patient_id=lv.patient_id \n"
		        + "\n"
		        + "INNER JOIN \n"
		        + "openmrs.obs o ON o.encounter_id=e.encounter_id AND DATE(e.encounter_datetime)<=DATE(:endDate) and o.voided=0 and e.voided=0 \n"
		        + "AND o.concept_id IN (159599,160555,160554,164855,1193,1088,1085,160540) \n"
		        + "LEFT OUTER JOIN openmrs.concept_name cn ON o.value_coded=cn.concept_id AND cn.voided=0  AND cn.locale_preferred=1 \n"
		        + "WHERE e.patient_id NOT IN(\n"
		        + "   \n"
		        + "SELECT DISTINCT(o.person_id) AS patient_id \n"
		        + "FROM openmrs.obs o\n"
		        + "JOIN \n"
		        + "openmrs.encounter e ON o.encounter_id=e.encounter_id AND DATE(e.encounter_datetime)<=DATE(:endDate) and e.voided=0 and o.voided=0\n"
		        + "AND o.concept_id IN (1543) \n"
		        + " AND DATE(o.value_datetime) IS NOT NULL \n"
		        + ")\n"
		        + "AND e.patient_id NOT IN(\n"
		        + "SELECT DISTINCT(p.person_id) as patient_id FROM openmrs.person p where p.dead=1 AND DATE(death_date)<=DATE(:endDate) \n"
		        + "and p.voided=0 \n"
		        + ") \n"
		        + "AND e.patient_id NOT IN(\n"
		        + "      \n"
		        + "SELECT patient_id \n"
		        + "FROM(SELECT DISTINCT(e.patient_id) AS patient_id,\n"
		        + "DATE(o.value_datetime) AS Date_TO,\n"
		        + "MAX(Date(e.encounter_datetime)) AS LastEncounter,\n"
		        + "All_LastEncounter\n"
		        + "FROM openmrs.obs o\n"
		        + "INNER JOIN \n"
		        + "openmrs.encounter e ON o.encounter_id=e.encounter_id AND DATE(e.encounter_datetime)<=DATE(:endDate) and e.voided=0 and o.voided=0\n"
		        + "AND o.concept_id IN (160649,159495,161555) \n"
		        + "INNER JOIN openmrs.encounter_type et ON e.encounter_type=et.encounter_type_id AND et.name in('HIV Discontinuation')\n"
		        + "INNER JOIN (\n"
		        + "SELECT e.patient_id AS patient_id,MAX(DATE(e.encounter_datetime)) AS All_LastEncounter \n"
		        + "FROM openmrs.encounter e \n" + "WHERE DATE(e.encounter_datetime) <= DATE(:endDate) and e.voided=0\n"
		        + "GROUP BY patient_id\n" + ") AS enc ON e.patient_id=enc.patient_id\n" + " group by patient_id \n"
		        + "HAVING LastEncounter>=All_LastEncounter) AS TOs\n" + ")\n" + " group by patient_id \n"
		        + " HAVING Next_TCA IS NOT NULL AND Next_TCA>=DATE_SUB(DATE(:endDate),INTERVAL 30 DAY) \n"
		        + " AND Current_Regimen IS NOT NULL ) as tx_curr\n";
		
		SqlQueryBuilder builder = new SqlQueryBuilder();
		builder.append(qry);
		Date endDate = (Date) context.getParameterValue("endDate");
		builder.addParameter("endDate", endDate);
		List<Integer> ptIds = evaluationService.evaluateToList(builder, Integer.class, context);
		newCohort.setMemberIds(new HashSet<Integer>(ptIds));
		
		return new EvaluatedCohort(newCohort, definition, context);
	}
	
}
