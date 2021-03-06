/*
 * PatientView
 *
 * Copyright (c) Worth Solutions Limited 2004-2013
 *
 * This file is part of PatientView.
 *
 * PatientView is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with PatientView in a file
 * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * @package PatientView
 * @link http://www.patientview.org
 * @author PatientView <info@patientview.org>
 * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
 * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
 */

package org.patientview.radar.web.pages.admin;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.patientview.model.Patient;
import org.patientview.radar.model.enums.ExportType;
import org.patientview.radar.model.filter.PatientUserFilter;
import org.patientview.radar.model.user.PatientUser;
import org.patientview.radar.service.EmailManager;
import org.patientview.radar.service.ExportManager;
import org.patientview.radar.service.PatientManager;
import org.patientview.radar.service.UserManager;
import org.patientview.radar.web.behaviours.RadarBehaviourFactory;
import org.patientview.radar.web.components.SortLink;
import org.patientview.radar.web.dataproviders.PatientUserDataProvider;
import org.patientview.radar.web.panels.RadarAjaxPagingNavigator;
import org.patientview.radar.web.resources.RadarResourceFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AdminPatientsPage extends AdminsBasePage {

    @SpringBean
    private UserManager userManager;
    @SpringBean
    private PatientManager patientManager;
    @SpringBean
    private EmailManager emailManager;
    @SpringBean
    private ExportManager exportManager;

    private static final int RESULTS_PER_PAGE = 10;

    public AdminPatientsPage() {
        final PatientUserDataProvider patientsDataProvider = new PatientUserDataProvider(userManager);

        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        feedback.setOutputMarkupPlaceholderTag(true);
        add(feedback);

        // TODO: need to hook these up
        add(new ResourceLink("exportPdf", RadarResourceFactory.getExportResource(
                exportManager.getPatientsExportData(ExportType.PDF), "patients-users" +
                AdminsBasePage.EXPORT_FILE_NAME_SUFFIX, ExportType.PDF)));

        add(new ResourceLink("exportExcel", RadarResourceFactory.getExportResource(
                exportManager.getPatientsExportData(ExportType.EXCEL), "patients-users" +
                AdminsBasePage.EXPORT_FILE_NAME_SUFFIX, ExportType.EXCEL)));

        final WebMarkupContainer patientsContainer = new WebMarkupContainer("patientsContainer");
        patientsContainer.setOutputMarkupId(true);
        add(patientsContainer);

        final DataView<PatientUser> patientList = new DataView<PatientUser>("patients",
                patientsDataProvider) {
            @Override
            protected void populateItem(Item<PatientUser> item) {
                builtDataViewRow(item, feedback);
            }
        };
        patientList.setItemsPerPage(RESULTS_PER_PAGE);
        patientsContainer.add(patientList);

        // add paging element
        patientsContainer.add(new RadarAjaxPagingNavigator("navigator", patientList, patientsDataProvider.size()));

        // add sort links to the table column headers
        for (Map.Entry<String, String> entry : getSortFields().entrySet()) {
            add(new SortLink(entry.getKey(), entry.getValue(), patientsDataProvider, patientList,
                    Arrays.asList(patientsContainer)));
        }
    }

    /**
     * Build a row in the dataview from the object
     *
     * @param item     Item<PatientUser>
     * @param feedback
     */
    private void builtDataViewRow(final Item<PatientUser> item, final FeedbackPanel feedback) {
        final PatientUser patientUser = item.getModelObject();
        final Patient patient = patientManager.getPatientByRadarNumber(
                patientUser.getRadarNumber());

        item.add(new BookmarkablePageLink<AdminConsultantPage>("edit", AdminPatientPage.class,
                AdminPatientPage.getPageParameters(patientUser))); //

        item.add(new Label("radarNo", patientUser.getRadarNumber().toString()));
        item.add(new Label("forename", patient.getForename()));
        item.add(new Label("surname", patient.getSurname()));
        item.add(new Label("dob", patientUser.getDateOfBirth().toString()));

        String dateRegistered = "";
        if (patientUser.getDateRegistered() != null) {
            dateRegistered = patientUser.getDateRegistered().toString();
        }

        item.add(new Label("dateRegistered", dateRegistered));
        item.add(new Label("username", patientUser.getUsername()));

        AjaxLink deleteLink = new AjaxLink("deleteLink") {
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                try {
                    userManager.deletePatientUser(patientUser);
                    setResponsePage(AdminPatientsPage.class);
                } catch (Exception e) {
                    error("Could not delete patient " + e);
                    ajaxRequestTarget.add(feedback);
                }
            }
        };
        deleteLink.add(RadarBehaviourFactory.getDeleteConfirmationBehaviour());
        item.add(deleteLink);

        item.add(new AjaxLink("emailLink") {
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                try {
                    emailManager.sendPatientRegistrationReminderEmail(patientUser);
                } catch (Exception e) {
                    error("There was an error sending reminder email to patient " + e);
                    ajaxRequestTarget.add(feedback);
                }
            }
        });
    }

    /**
     * List of columns that can be used to sort the results - will return ID of el to be bound to and the field to sort
     *
     * @return Map<String, PatientUserFilter.UserField>
     */
    private Map<String, String> getSortFields() {
        return new HashMap<String, String>() {
            {
                put("orderByRadarNo", PatientUserFilter.UserField.RADAR_NO.getDatabaseFieldName());
                put("orderByUsername", PatientUserFilter.UserField.USERNAME.getDatabaseFieldName());
                put("orderByDob", PatientUserFilter.UserField.DOB.getDatabaseFieldName());
                put("orderByDateRegistered", PatientUserFilter.UserField.REGISTRATION_DATE.getDatabaseFieldName());
            }
        };
    }
}
