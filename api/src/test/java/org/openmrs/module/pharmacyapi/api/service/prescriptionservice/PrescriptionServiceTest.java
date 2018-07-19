/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.pharmacyapi.api.service.prescriptionservice;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.Order.Action;
import org.openmrs.OrderFrequency;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.pharmacyapi.api.common.util.MappedEncounters;
import org.openmrs.module.pharmacyapi.api.prescription.model.Prescription;
import org.openmrs.module.pharmacyapi.api.prescription.model.PrescriptionItem;
import org.openmrs.module.pharmacyapi.api.prescription.service.PrescriptionService;
import org.openmrs.module.pharmacyapi.api.prescription.util.PrescriptionUtils;
import org.openmrs.module.pharmacyapi.api.templates.LocationTemplate;
import org.openmrs.module.pharmacyapi.api.templates.PatientTemplate;
import org.openmrs.module.pharmacyapi.api.templates.PrescriptionItemTemplate;
import org.openmrs.module.pharmacyapi.api.templates.PrescriptionTemplate;
import org.openmrs.module.pharmacyapi.api.templates.ProviderTemplate;
import org.openmrs.module.pharmacyapi.api.util.BaseTest;

import br.com.six2six.fixturefactory.Fixture;

public class PrescriptionServiceTest extends BaseTest {
	
	/**
	 * @should create a Prescription for non ARV Drug
	 * @throws Exception
	 */
	@Test
	public void shouldCreateNonArvPrescription() throws Exception {
		
		final PrescriptionService prescriptionService = Context.getService(PrescriptionService.class);
		
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 2005);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		final Date date = calendar.getTime();
		
		final Prescription prescription = new Prescription();
		prescription.setPatient((Patient) Fixture.from(Patient.class).gimme(PatientTemplate.MR_HORATIO));
		prescription.setProvider((Provider) Fixture.from(Provider.class).gimme(ProviderTemplate.TEST));
		prescription.setLocation((Location) Fixture.from(Location.class).gimme(LocationTemplate.XANADU));
		prescription.setPrescriptionDate(date);
		
		final PrescriptionItem prescriptionItem = Fixture.from(PrescriptionItem.class)
		        .gimme(PrescriptionItemTemplate.VALID_PROFLAXIA_TRIOMUNE30);
		prescription.setPrescriptionItems(Arrays.asList(prescriptionItem));
		
		final Prescription createdPrescription = prescriptionService.createPrescription(prescription);
		
		Assert.assertNotNull(createdPrescription);
		
		final Encounter createdEncounter = Context.getEncounterService()
		        .getEncounter(prescription.getPrescriptionEncounter().getEncounterId());
		
		Assert.assertNotNull(createdEncounter);
		Assert.assertEquals(MappedEncounters.GENERAL_PRESCRIPTION, createdEncounter.getEncounterType().getUuid());
		
		final Set<Order> allOrders = createdEncounter.getOrders();
		
		Assert.assertEquals(1, allOrders.size());
		
		final Order createdOrder = allOrders.iterator().next();
		
		final Double durationInDays = 42d; // 7(durationUnits)x2(dose)x3(duration)
		
		Assert.assertEquals(Action.NEW, createdOrder.getAction());
		Assert.assertEquals(prescription.getPatient().getUuid(), createdOrder.getPatient().getUuid());
		Assert.assertEquals(durationInDays, ((DrugOrder) createdOrder).getQuantity());
		Assert.assertEquals(prescriptionItem.getDrugOrder().getDrug().getUuid(),
		    ((DrugOrder) createdOrder).getDrug().getUuid());
	}
	
	@Test
	public void shouldCreateArvPrescription() throws Exception {
		this.executeDataSet("prescriptionservice/shouldCreateArvPrescription-dataset.xml");
		
		final PrescriptionService prescriptionService = Context.getService(PrescriptionService.class);
		
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 2017);
		calendar.set(Calendar.MONTH, 11);
		calendar.set(Calendar.DAY_OF_MONTH, 31);
		final Date date = calendar.getTime();
		
		final Prescription prescription = Fixture.from(Prescription.class)
		        .gimme(PrescriptionTemplate.VALID_ARV_NEVIRAPINA);
		prescription.setPatient((Patient) Fixture.from(Patient.class).gimme(PatientTemplate.MR_HORATIO));
		prescription.setProvider((Provider) Fixture.from(Provider.class).gimme(ProviderTemplate.TEST));
		prescription.setLocation((Location) Fixture.from(Location.class).gimme(LocationTemplate.XANADU));
		prescription.setPrescriptionDate(date);
		final PrescriptionItem prescriptionItem = Fixture.from(PrescriptionItem.class)
		        .gimme(PrescriptionItemTemplate.VALID_ARV_NEVIRAPINA);
		prescription.setPrescriptionItems(Arrays.asList(prescriptionItem));
		
		final Prescription createdPrescription = prescriptionService.createPrescription(prescription);
		
		Assert.assertNotNull(createdPrescription);
		
		final Encounter createdEncounter = Context.getEncounterService()
		        .getEncounter(prescription.getPrescriptionEncounter().getEncounterId());
		
		Assert.assertNotNull(createdEncounter);
		Assert.assertEquals(MappedEncounters.ARV_FOLLOW_UP_ADULT, createdEncounter.getEncounterType().getUuid());
		
		final Set<Order> allOrders = createdEncounter.getOrders();
		
		Assert.assertEquals(1, allOrders.size());
		
		final Order createdOrder = allOrders.iterator().next();
		
		final Double durationInDays = 42d; // 7(durationUnits)x2(dose)x3(duration)
		
		Assert.assertEquals(Action.NEW, createdOrder.getAction());
		Assert.assertEquals(prescription.getPatient().getUuid(), createdOrder.getPatient().getUuid());
		Assert.assertEquals(durationInDays, ((DrugOrder) createdOrder).getQuantity());
		Assert.assertEquals(prescriptionItem.getDrugOrder().getDrug().getUuid(),
		    ((DrugOrder) createdOrder).getDrug().getUuid());
		
	}
	
	@Test
	public void shouldCancelNotDispensedPrescriptionItem() throws Exception {
		
		final PrescriptionService prescriptionService = Context.getService(PrescriptionService.class);
		
		final DrugOrder drugOrder = new DrugOrder();
		drugOrder.setUuid("921de0a3-05c4-444a-be03-e01b4c4b9142");
		
		final PrescriptionItem prescriptionItem = new PrescriptionItem(drugOrder);
		prescriptionService.cancelPrescriptionItem(prescriptionItem, "cancelation reason");
		
		final Order vodedOrder = Context.getOrderService().getOrderByUuid(drugOrder.getUuid());
		
		Assert.assertNotNull(vodedOrder);
		Assert.assertEquals(true, vodedOrder.isVoided());
	}
	
	@Test
	public void shouldDiscontinueRevisedPrescriptionItem() throws Exception {
		this.executeDataSet("prescriptionservice/shouldCancelNotDispensedPrescriptionItem.xml");
		
		final PrescriptionService prescriptionService = Context.getService(PrescriptionService.class);
		
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setUuid("e1f95924-697a-11e3-bd76-revised");
		
		final PrescriptionItem prescriptionItem = new PrescriptionItem(drugOrder);
		prescriptionService.cancelPrescriptionItem(prescriptionItem, "Discontinuation reason");
		
		drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid(drugOrder.getUuid());
		
		final Order discontinueOrder = Context.getOrderService().getDiscontinuationOrder(drugOrder);
		
		Assert.assertNotNull(discontinueOrder);
		Assert.assertEquals(Action.DISCONTINUE, discontinueOrder.getAction());
		Assert.assertEquals(drugOrder.getUuid(), discontinueOrder.getPreviousOrder().getUuid());
	}
	
	@Test
	public void shouldCancelNonArvActivePrescriptionItem() throws Exception {
		this.executeDataSet("prescriptionservice/shouldCancelNonArvActivePrescriptionItem-dataset.xml");
		
		final PrescriptionService prescriptionService = Context.getService(PrescriptionService.class);
		final String orderUuid = "921de0a3-05c4-444a-be03-0001";
		
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 2017);
		calendar.set(Calendar.MONTH, 11);
		calendar.set(Calendar.DAY_OF_MONTH, 31);
		
		final Order order = Context.getOrderService().getOrderByUuid(orderUuid);
		Assert.assertFalse(order.isVoided());
		
		final PrescriptionItem prescriptionItem = Fixture.from(PrescriptionItem.class)
		        .gimme(PrescriptionItemTemplate.VALID_PROFLAXIA_ASPIRIN);
		prescriptionItem.getDrugOrder().setUuid(orderUuid);
		
		prescriptionService.cancelPrescriptionItem(prescriptionItem, "web service call");
		Assert.assertTrue(order.isVoided());
	}
	
	@Test
	public void testCalculateDrugQuantityShouldRound() throws Exception {
		Concept monthDurationUnits = new Concept();
		monthDurationUnits.setUuid("9d96d012-10e8-11e5-9009-0242ac110012");
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setFrequencyPerDay(0.14286);
		DrugOrder drugOrder = new DrugOrder();
		drugOrder.setDurationUnits(monthDurationUnits);
		drugOrder.setDose(2.0);
		drugOrder.setDuration(1);
		drugOrder.setFrequency(orderFrequency);
		Double quantity = new PrescriptionUtils().calculateDrugQuantity(drugOrder);
		Assert.assertEquals(Double.valueOf(9.0), quantity);
	}
}
