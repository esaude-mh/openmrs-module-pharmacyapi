/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
/**
 *
 */
package org.openmrs.module.pharmacyapi.api.drugitem.service;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.pharmacyapi.api.common.exception.PharmacyBusinessException;
import org.openmrs.module.pharmacyapi.api.drugitem.dao.DrugItemDAO;
import org.openmrs.module.pharmacyapi.api.drugitem.model.DrugItem;

/**
 */
public interface DrugItemService extends OpenmrsService {
	
	void setDrugItemDAO(final DrugItemDAO drugItemDAO);
	
	DrugItem findDrugItemByUuid(String uuid);
	
	List<DrugItem> findAllDrugItem(Boolean retired);
	
	DrugItem findDrugItemByDrugId(Integer drugId) throws PharmacyBusinessException;
	
	DrugItem findDrugItemByFNM(String fnm);
}
