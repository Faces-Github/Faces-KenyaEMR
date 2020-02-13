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

@Handler(supports = FDateStartedARTDataDefinition.class, order = 50)
public class FDateStartedARTDataEvaluator implements PatientDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPatientData c = new EvaluatedPatientData(definition, context);
		
		String qry = "SELECT e.patient_id AS patient_id,\n"
		        + "COALESCE(MIN(IF(o.concept_id in(159599),DATE(o.value_datetime),NULL)),\n"
		        + "IF(o.concept_id IN(160540) AND o.value_coded IN(5622,159937,159938,160536,160537,160538,160539,160541,160542,160544,160631,162050,162223,160563),MIN(DATE(e.encounter_datetime)),NULL)\n"
		        + ") AS ART_Start_Date \n"
		        + "FROM openmrs.encounter e \n"
		        + "INNER JOIN openmrs.obs o ON e.encounter_id=o.encounter_id AND o.voided=0 and e.voided=0 and o.concept_id in(159599,160540) AND date(e.encounter_datetime)<=date(:endDate)\n"
		        + "LEFT OUTER JOIN openmrs.concept_name cn ON o.value_coded=cn.concept_id \n" + "group by e.patient_id";
		
		SqlQueryBuilder builder = new SqlQueryBuilder();
		builder.append(qry);
		Date endDate = (Date) context.getParameterValue("endDate");
		builder.addParameter("endDate", endDate);
		Map<Integer, Object> data = evaluationService.evaluateToMap(builder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
