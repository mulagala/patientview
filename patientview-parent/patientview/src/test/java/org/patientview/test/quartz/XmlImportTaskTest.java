package org.patientview.test.quartz;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.common.test.BaseTestPvDbSchema;
import org.patientview.ibd.model.Allergy;
import org.patientview.ibd.model.MyIbd;
import org.patientview.ibd.model.Procedure;
import org.patientview.model.Patient;
import org.patientview.patientview.FindXmlFiles;
import org.patientview.patientview.model.Centre;
import org.patientview.patientview.model.Diagnostic;
import org.patientview.patientview.model.Letter;
import org.patientview.patientview.model.Medicine;
import org.patientview.patientview.model.Specialty;
import org.patientview.patientview.model.TestResult;
import org.patientview.patientview.model.Unit;
import org.patientview.quartz.XmlImportTask;
import org.patientview.quartz.exception.ProcessException;
import org.patientview.repository.PatientDao;
import org.patientview.repository.UnitDao;
import org.patientview.service.CentreManager;
import org.patientview.service.DiagnosticManager;
import org.patientview.service.ImportManager;
import org.patientview.service.LetterManager;
import org.patientview.service.MedicineManager;
import org.patientview.service.TestResultManager;
import org.patientview.service.ibd.IbdManager;
import org.patientview.test.helpers.ServiceHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml", "classpath*:test-context.xml"})
@Transactional
public class XmlImportTaskTest extends BaseTestPvDbSchema {

    @Autowired
    private XmlImportTask xmlImport;

    private String xmlDirectory;

    private String[] fileEndings = {".xml", };

    @Inject
    private PatientDao patientDao;

    @Inject
    private CentreManager centreManager;

    @Inject
    private MedicineManager medicineManager;

    @Inject
    private LetterManager letterManager;

    @Inject
    private TestResultManager testResultManager;

    @Inject
    private IbdManager ibdManager;

    @Inject
    private DiagnosticManager diagnosticManager;

    @Inject
    private ImportManager importManager;

    @Inject
    private ServiceHelpers serviceHelpers;

    @Inject
    private UnitDao unitDao;

    private Specialty specialty;

    @Test
    public void testRead() throws Exception {

        int xmlFilesSize = 0;

        String parentDir = ResourceUtils.getFile("classpath:schedule/rm301_1244_9876543210.xml").getParent();

        setXmlDirectory(parentDir);

        File[] xmlFiles = FindXmlFiles.findXmlFiles(xmlDirectory, fileEndings);
        if (xmlFiles != null) {
            xmlFilesSize = xmlFiles.length;
        }

        assertTrue("Can not read XML files", xmlFilesSize != 0);

        xmlImport.setXmlDirectory(parentDir);
        xmlImport.execute();

        List<Centre> centres = centreManager.getAll();

        assertEquals("Incorrect number of centres", 1, centres.size());
        assertEquals("Incorrect centre", "RM301", centres.get(0).getCentreCode());

        List<Patient> patients = patientDao.get("RM301");

        assertEquals("Incorrect number of patients", 1, patients.size());
        assertEquals("Incorrect patient", "9876543210", patients.get(0).getNhsno());

        List<Letter> letters = letterManager.getAll();

        assertEquals("Incorrect number of letters", 2, letters.size());

        MyIbd myIbd = ibdManager.getMyIbd("9876543210");
        assertNotNull("No MyIbd information was parsed", myIbd);

        Diagnostic diagnostic = diagnosticManager.get("9876543210");
        assertNotNull("No diagnostic information was parsed", diagnostic);

        Procedure procedure = ibdManager.getProcedure("9876543210");
        assertNotNull("No procedure information was parsed", procedure);

        Allergy allergy = ibdManager.getAllergy("9876543210");
        assertNotNull("No allergy information was parsed", allergy);

        List<TestResult> results = testResultManager.get("9876543210", "RM301");

        assertEquals("Incorrect number of results", 3, results.size());

        List<Medicine> medicines = medicineManager.getAll();

        assertEquals("Incorrect number of medicines", 0, medicines.size());

    }

    @Test
    public void testImportSetsUnitLastUpdated() throws Exception {

        setup("schedule/TestUnitA_1244_9876543210/TestUnitA_1244_9876543210.xml");
        xmlImport.execute();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        Unit checkUnit = unitDao.get("TestUnitA", specialty);
        assertNotNull("Unit last import date is null", checkUnit.getLastImportDate());
        assertEquals("Unit last import date updated not correct",
                simpleDateFormat.format(date), simpleDateFormat.format(checkUnit.getLastImportDate()));

    }

    /**
     * Import a new patient with test results and show the mostRecentTestResultDateRangeStopDate is taken from
     * the last stop date in the xml file.
     *
     * Then run a second import with test results previous to the first import.  Show that this does not affect the
     * mostRecentTestResultDateRangeStopDate set on the patient.
     *
     * Then run a third import with test results after the first import.  Show that this does update the
     * mostRecentTestResultDateRangeStopDate.
     * @throws Exception
     */
    @Test
    public void testImportSetsPatientMostRecentTestResultDateRangeStopDate() throws Exception {

        setup("schedule/testrun1/first-results-for-patient.xml");
        xmlImport.execute();
        Patient patient = patientDao.get("9876543210", "TestUnitA");
        assertEquals("Date not set correctly on patient from first import",
                getDateRangeDate("2012-08-17").toString(),
                patient.getMostRecentTestResultDateRangeStopDate().toString());

        xmlImport.setXmlDirectory(
                ResourceUtils.getFile("classpath:schedule/testrun2/second-results-for-patient.xml").getParent());
        xmlImport.execute();

        patient = patientDao.get("9876543210", "TestUnitA");
        assertEquals("Date should not have changed, stop date was before current value",
                getDateRangeDate("2012-08-17").toString(),
                patient.getMostRecentTestResultDateRangeStopDate().toString());

        xmlImport.setXmlDirectory(
                ResourceUtils.getFile("classpath:schedule/testrun3/third-results-for-patient.xml").getParent());
        xmlImport.execute();

        patient = patientDao.get("9876543210", "TestUnitA");
        assertEquals("Date should have updated",
                getDateRangeDate("2013-01-17").toString(),
                patient.getMostRecentTestResultDateRangeStopDate().toString());
    }

    /**
     * Get a date object that to the spec of that which ends up in the database.
     * If Fri Aug 17 00:00:00 BST 2012 is used as a stop date it will end up as Fri Aug 17 23:59:59 BST 2012
     * @param dateStr in the format of that found in the xml file
     * @return the date
     */
    private Date getDateRangeDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = formatter.parseDateTime(dateStr);
        return dt.plusDays(1).minusSeconds(1).toDate();
    }

    private void setup(String directoryAndFilename) throws FileNotFoundException {
        int xmlFilesSize = 0;

        String parentDir
                = ResourceUtils.getFile("classpath:" + directoryAndFilename).getParent();

        setXmlDirectory(parentDir);

        File[] xmlFiles = FindXmlFiles.findXmlFiles(xmlDirectory, fileEndings);
        if (xmlFiles != null) {
            xmlFilesSize = xmlFiles.length;
        }

        assertTrue("Can not read XML files", xmlFilesSize != 0);

        specialty = serviceHelpers.createSpecialty("Specialty 1", "Specialty1", "Test description");
        Unit unit = new Unit();
        unit.setUnitcode("TestUnitA");
        unit.setName("Test Data For Unit");
        unit.setShortname("Test Data");
        unit.setSpecialty(specialty);

        unitDao.save(unit);
        assertNull("Unit last import date is not null", unit.getLastImportDate());

        xmlImport.setXmlDirectory(parentDir);
    }

    /**
     * This test is to create a data exception on the patient record. The exception is the lack of a
     * nhs no.
     *
     * @throws Exception
     */
    @Test (expected = ProcessException.class)
    public void testLoadXmlFileContainingNoNhsNumber() throws ProcessException, IOException {

        File  file = ResourceUtils.getFile("classpath:RENALB_nhsnomissing_TEST111111.gpg.xml");

        importManager.process(file);

    }

    public void setXmlDirectory(String xmlDirectory) {
        this.xmlDirectory = xmlDirectory;
    }

}