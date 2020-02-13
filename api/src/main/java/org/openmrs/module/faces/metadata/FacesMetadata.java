package org.openmrs.module.faces.metadata;

import org.openmrs.module.metadatadeploy.bundle.AbstractMetadataBundle;
import org.springframework.stereotype.Component;

import static org.openmrs.module.metadatadeploy.bundle.CoreConstructors.form;

/**
 * Metadata constants
 */
@Component
public class FacesMetadata extends AbstractMetadataBundle {
	
	public static final String MODULE_ID = "faces";
	
	public static final class _EncounterType {
		
		public static final String Consultation = "465a92f2-baf8-42e9-9612-53064be868e8";
	}
	
	public static final class _Form {
		
		public static final String FACES_TRIAL_FORM = "097c78e6-802e-450a-bce6-3f8002f3df66";
	}
	
	@Override
	public void install() throws Exception {
		// doing this in the scheduled task so that previous value set is preserved
		install(form("Trial Faces Encounter Form", "This is a sample FACES custom forms. Do not use it",
		    _EncounterType.Consultation, "1", _Form.FACES_TRIAL_FORM));
	}
	
}
