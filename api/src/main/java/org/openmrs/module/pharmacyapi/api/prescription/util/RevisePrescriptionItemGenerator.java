/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.pharmacyapi.api.prescription.util;

import java.util.Date;

import org.openmrs.DrugOrder;
import org.openmrs.Order.Action;
import org.openmrs.module.pharmacyapi.api.common.exception.PharmacyBusinessException;
import org.openmrs.module.pharmacyapi.api.prescription.model.Prescription;
import org.openmrs.module.pharmacyapi.api.prescription.model.PrescriptionItem;
import org.openmrs.module.pharmacyapi.api.prescription.model.PrescriptionItem.PrescriptionItemStatus;
import org.springframework.stereotype.Component;

@Component
public class RevisePrescriptionItemGenerator extends AbstractPrescriptionItemGenerator {
	
	@Override
	public PrescriptionItem generate(final Prescription prescription, final DrugOrder drugOrder,
	        final Date creationDate) throws PharmacyBusinessException {
		
		final PrescriptionItem prescriptionItem = super.generate(prescription, drugOrder, creationDate);
		final Double quantity = this.calculateDrugPikckedUp(prescriptionItem.getDrugOrder());
		prescriptionItem.setDrugPickedUp(quantity);
		prescriptionItem
		        .setDrugToPickUp(prescriptionItem.getDrugOrder().getQuantity() - prescriptionItem.getDrugPickedUp());
		
		return prescriptionItem;
	}
	
	@Override
	protected PrescriptionItemStatus calculatePrescriptionItemStatus(final PrescriptionItem item,
	        final Date consultationDate) {
		
		final PrescriptionItemStatus status = Action.DISCONTINUE.equals(item.getDrugOrder().getAction())
		        ? PrescriptionItemStatus.FINALIZED : PrescriptionItemStatus.ACTIVE;
		
		return this.isOrderExpired(item, consultationDate) ? PrescriptionItemStatus.EXPIRED : status;
	}
}
