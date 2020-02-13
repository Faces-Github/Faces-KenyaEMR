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

@Handler(supports = DiffCareModelDataDefinition.class, order = 50)
public class DiffCareModelDataEvaluator implements PatientDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		EvaluatedPatientData c = new EvaluatedPatientData(definition, context);
		
		String qry = "select o.person_id as patient_id,\n"
		        + "SUBSTRING_INDEX(GROUP_CONCAT(cn.name ORDER BY e.encounter_datetime SEPARATOR '|'),'|',-1) AS Differenciated_Care_Model \n"
		        + " FROM openmrs.obs o \n"
		        + " INNER JOIN openmrs.encounter e ON o.encounter_id=e.encounter_id AND DATE(e.encounter_datetime)<=DATE(:endDate) and e.voided=0 and o.voided=0 \n"
		        + " INNER JOIN openmrs.concept_name cn ON o.value_coded=cn.concept_id and o.concept_id in(164947) AND cn.locale='en' and cn.locale_preferred=1 and cn.voided=0\n"
		        + " group by patient_id";
		
		SqlQueryBuilder builder = new SqlQueryBuilder();
		builder.append(qry);
		Date endDate = (Date) context.getParameterValue("endDate");
		builder.addParameter("endDate", endDate);
		Map<Integer, Object> data = evaluationService.evaluateToMap(builder, Integer.class, Object.class, context);
		c.setData(data);
		return c;
	}
}
