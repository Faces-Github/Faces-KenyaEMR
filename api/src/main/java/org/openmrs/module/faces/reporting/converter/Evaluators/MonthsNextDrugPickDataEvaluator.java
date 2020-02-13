package org.openmrs.module.faces.reporting.converter;

import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

@Handler(supports = MonthsNextDrugPickDataDefinition.class, order = 50)
public class MonthsNextDrugPickDataEvaluator implements PatientDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPatientData c = new EvaluatedPatientData(definition, context);
		
		String qry = "SELECT \n"
		        + "e.patient_id AS patient_id,\n"
		        + "CASE \n"
		        + "    WHEN DATEDIFF(if(l_enc3>=l_enc6,Next_TCA_3,Next_TCA),Last_Encounter)<77 THEN '<3 Months' \n"
		        + "    WHEN DATEDIFF(if(l_enc3>=l_enc6,Next_TCA_3,Next_TCA),Last_Encounter) BETWEEN 77 AND 175 THEN '3-5 Months' \n"
		        + "    WHEN DATEDIFF(if(l_enc3>=l_enc6,Next_TCA_3,Next_TCA),Last_Encounter)>175 THEN '6+ Months' \n"
		        + "    END AS Months_to_Next_DrugPick \n"
		        + "FROM openmrs.encounter e \n"
		        + "INNER JOIN (\n"
		        + "SELECT \n"
		        + "e.patient_id as patient_id,\n"
		        + "MAX(DATE(e.encounter_datetime)) AS Last_Encounter,\n"
		        + "SUBSTRING_INDEX(GROUP_CONCAT(IF(o.concept_id IN(5096),DATE(o.value_datetime),null) ORDER BY e.encounter_datetime SEPARATOR '|'),'|',-1) AS Next_TCA,\n"
		        + "MAX(DATE(IF(o.concept_id in(162549),e.encounter_datetime,null))) AS l_enc3,\n"
		        + "MAX(DATE(IF(o.concept_id in(5096),e.encounter_datetime,null))) AS l_enc6,\n"
		        + "SUBSTRING_INDEX(GROUP_CONCAT(IF(o.concept_id IN(162549),DATE(o.value_datetime),null) ORDER BY e.encounter_datetime SEPARATOR '|'),'|',-1) AS Next_TCA_3 \n"
		        + "FROM openmrs.obs o \n" + "JOIN openmrs.encounter e ON o.encounter_id=e.encounter_id \n"
		        + "AND o.voided=0 and e.voided=0 and o.concept_id in(5096) \n"
		        + "AND DATE(e.encounter_datetime) <= DATE(:endDate) \n"
		        + "JOIN openmrs.encounter_type et ON e.encounter_type=et.encounter_type_id \n"
		        + " AND et.name IN('HIV Enrollment','HIV Consultation','ART Refill') \n" + "and et.retired=0\n"
		        + "group by patient_id \n" + ")lv ON e.patient_id=lv.patient_id group by e.patient_id\n";
		
		SqlQueryBuilder builder = new SqlQueryBuilder();
		builder.append(qry);
		Date endDate = (Date) context.getParameterValue("endDate");
		builder.addParameter("endDate", endDate);
		Map<Integer, Object> data = evaluationService.evaluateToMap(builder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
