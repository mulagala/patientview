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

package org.patientview.patientview.parser;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.patientview.ibd.model.Allergy;
import org.patientview.ibd.model.MyIbd;
import org.patientview.ibd.model.Procedure;
import org.patientview.ibd.model.enums.DiseaseExtent;
import org.patientview.model.Patient;
import org.patientview.patientview.TestResultDateRange;
import org.patientview.patientview.model.Centre;
import org.patientview.patientview.model.CorruptNode;
import org.patientview.patientview.model.Diagnosis;
import org.patientview.patientview.model.Diagnostic;
import org.patientview.patientview.model.Letter;
import org.patientview.patientview.model.Medicine;
import org.patientview.patientview.model.TestResult;
import org.patientview.patientview.model.enums.DiagnosticType;
import org.patientview.patientview.model.enums.NodeError;
import org.patientview.patientview.utils.TimestampUtils;
import org.patientview.quartz.exception.ResultParserException;
import org.patientview.utils.LegacySpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultParser.class);
    private static final SimpleDateFormat IMPORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final int HOURS_IN_DAY = 24;

    private String filename;

    private List<TestResult> testResults = new ArrayList<TestResult>();
    private List<TestResultDateRange> dateRanges = new ArrayList<TestResultDateRange>();
    private List<Letter> letters = new ArrayList<Letter>();
    private List<Diagnosis> otherDiagnoses = new ArrayList<Diagnosis>();
    private List<Medicine> medicines = new ArrayList<Medicine>();
    private List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
    private List<Procedure> procedures = new ArrayList<Procedure>();
    private List<Allergy> allergies = new ArrayList<Allergy>();
    private List<CorruptNode> corruptNodes = new ArrayList<CorruptNode>();

    private Map xmlData = new HashMap();
    private Document document;
    private String[] topLevelElements = new String[]{"flag", "centrecode", "centrename", "centreaddress1",
            "centreaddress2", "centreaddress3", "centreaddress4", "centrepostcode", "centretelephone", "centreemail",
            "gpname", "gpaddress1", "gpaddress2", "gpaddress3", "gppostcode", "gptelephone", "gpemail", "diagnosisedta",
            "diagnosisdate", "rrtstatus", "tpstatus", "surname", "forename", "dateofbirth", "sex", "nhsno",
            "hospitalnumber", "address1", "address2", "address3", "address4", "postcode", "telephone1", "telephone2",
            "mobile", "diagnosisyear", "ibddiseaseextent", "ibddiseasecomplications", "ibdeimanifestations",
            "bodypartsaffected", "familyhistory", "smokinghistory", "surgicalhistory", "vaccinationrecord", "bmdexam",
            "namedconsultant", "ibdnurse", "bloodgroup", "diagnosis", "colonoscopysurveillance"};



    public ResultParser(File xmlFile) throws ResultParserException {
        filename = xmlFile.getName();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(xmlFile);
        } catch (SAXException se) {
            throw new ResultParserException("There has been a parse exception from the XML file", se);
        } catch (ParserConfigurationException pe) {
            throw new ResultParserException("There has been a configuration exception with the parser", pe);
        } catch (IOException io) {
            throw new ResultParserException("There has been an IO exception", io);
        }
    }

    public boolean parse() {

        for (int i = 0; i < topLevelElements.length; i++) {
            collectTopLevelData(topLevelElements[i], document);
        }

        collectTestResults(document);
        if (corruptNodes.size() == 0) {
            collectDateRanges(document);
            collectLetters(document);
            collectOtherDiagnosis(document);
            collectMedicines(document);
            collectDiagnostics(document);
            collectProcedures(document);
            collectAllergies(document);
            return true;
        }  else  {
            return false;
        }
    }

    private void collectDateRanges(Document doc) {
        NodeList testNodeList = doc.getElementsByTagName("test");
        for (int i = 0; i < testNodeList.getLength(); i++) {
            Node testNode = testNodeList.item(i);
            NodeList testResultNodes = testNode.getChildNodes();
            String testCode = "";
            String startDate = "";
            String stopDate = "";
            for (int j = 0; j < testResultNodes.getLength(); j++) {
                Node testResultNode = testResultNodes.item(j);
                if ((testResultNode.getNodeType() == Node.ELEMENT_NODE)
                        && (testResultNode.getNodeName().equals("daterange"))) {
                    NamedNodeMap attributes = testResultNode.getAttributes();
                    startDate = attributes.getNamedItem("start").getNodeValue();
                    stopDate = attributes.getNamedItem("stop").getNodeValue();
                } else if ((testResultNode.getNodeType() == Node.ELEMENT_NODE)
                        && (testResultNode.getNodeName().equals("testcode"))) {
                    testCode = testResultNode.getFirstChild().getNodeValue();
                }
            }
            TestResultDateRange dateRange =
                    new TestResultDateRange(getData("nhsno"), getData("centrecode"), testCode, startDate, stopDate);
            dateRanges.add(dateRange);
        }
    }

    private void collectTestResults(Document doc) {

        NodeList testNodeList = doc.getElementsByTagName("test");

        for (int i = 0; i < testNodeList.getLength(); i++) {
            Node testNode = testNodeList.item(i);
            NodeList testResultNodes = testNode.getChildNodes();
            String testCode = "";
            String dateRangeStartString;
            String dateRangeStopString;
            Calendar dateRangeStart = null;
            Calendar dateRangeStop = null;
            for (int j = 0; j < testResultNodes.getLength(); j++) {
                Node testResultNode = testResultNodes.item(j);
                if ((testResultNode.getNodeType() == Node.ELEMENT_NODE)
                        && (testResultNode.getNodeName().equals("testcode"))) {
                    testCode = testResultNode.getFirstChild().getNodeValue();
                } else

                    if (testResultNode.getNodeName().equals("daterange")) {
                        NamedNodeMap namedNodeMap = testResultNode.getAttributes();

                        Node startNode = namedNodeMap.getNamedItem("start");
                        Node stopNode = namedNodeMap.getNamedItem("stop");
                        if (startNode != null && startNode.getNodeValue() != null
                                && stopNode != null && stopNode.getNodeValue() != null) {
                            dateRangeStartString = startNode.getNodeValue();
                            dateRangeStopString = stopNode.getNodeValue();

                            dateRangeStart = TimestampUtils.createTimestamp(dateRangeStartString);
                            dateRangeStop = TimestampUtils.createTimestamp(dateRangeStopString);
                            dateRangeStop.add(Calendar.HOUR, HOURS_IN_DAY); // set it to end of day instead of beginning
                        }
                    } else if ((testResultNode.getNodeType() == Node.ELEMENT_NODE)
                            && (testResultNode.getNodeName().equals("result"))) {
                        TestResult testResult = new TestResult(getData("nhsno"), getData("centrecode"), null,
                                testCode, "");
                        NodeList resultDataNodes = testResultNode.getChildNodes();
                        for (int k = 0; k < resultDataNodes.getLength(); k++) {
                            Node resultDataNode = resultDataNodes.item(k);
                            if ((resultDataNode.getNodeType() == Node.ELEMENT_NODE)
                                    && (resultDataNode.getNodeName().equals("datestamp"))) {
                                parseDatestamp(testResult, resultDataNode);

                                if (testResult.getDatestamped() != null) {
                                    DateTime testResultDate = new DateTime(testResult.getDatestamped());

                                    /**
                                     * make sure the result doesn't have a future date. if it does not, make sure
                                     *  result date is between date range specified in the xml
                                     */
                                    if (testResultDate.isAfter(
                                            LegacySpringUtils.getTimeManager().getCurrentDate().getTime())) {
                                        // add this test to corrupt tests list
                                        corruptNodes.add(new CorruptNode(testNode, NodeError.FUTURE_RESULT));
                                    } else if (dateRangeStart != null && dateRangeStop != null && !(new Interval(
                                            new DateTime(dateRangeStart), new DateTime(dateRangeStop)).contains(
                                            new DateTime(testResultDate)))) {
                                        // add this test to corrupt tests list
                                        corruptNodes.add(new CorruptNode(testNode, NodeError.WRONG_DATE_RANGE));
                                    }
                                }
                            } else if ((resultDataNode.getNodeType() == Node.ELEMENT_NODE)
                                    && (resultDataNode.getNodeName().equals("prepost"))) {
                                testResult.setPrepost(resultDataNode.getFirstChild().getNodeValue());
                            } else if ((resultDataNode.getNodeType() == Node.ELEMENT_NODE)
                                    && (resultDataNode.getNodeName().equals("value"))) {
                                if (resultDataNode.getFirstChild() == null) {
                                    // we should not import this xml. continue execution and throw the corrupt nodes
                                    corruptNodes.add(new CorruptNode(testNode, NodeError.MISSING_VALUE));
                                } else {
                                    testResult.setValue(resultDataNode.getFirstChild().getNodeValue().trim());
                                }
                            }
                        }
                        testResults.add(testResult);
                    }
            }
        }

    }

    private void collectLetters(Document doc) {
        NodeList letterNodes = doc.getElementsByTagName("letter");
        for (int i = 0; i < letterNodes.getLength(); i++) {
            Node letterNode = letterNodes.item(i);
            Letter letter = new Letter();
            letter.setNhsno(getData("nhsno"));
            letter.setUnitcode(getData("centrecode"));
            NodeList letterDetailNodes = letterNode.getChildNodes();
            for (int j = 0; j < letterDetailNodes.getLength(); j++) {

                Node letterDetailNode = letterDetailNodes.item(j);

                // Avoid the npe when the node has no children
                if (letterDetailNode.getFirstChild() == null) {
                    continue;
                }

                if ((letterDetailNode.getNodeType() == Node.ELEMENT_NODE)
                        && (letterDetailNode.getNodeName().equals("letterdate"))) {

                    letter.setStringDate(letterDetailNode.getFirstChild().getNodeValue());

                } else if ((letterDetailNode.getNodeType() == Node.ELEMENT_NODE)
                        && (letterDetailNode.getNodeName().equals("lettertype"))) {

                    letter.setType(letterDetailNode.getFirstChild().getNodeValue());

                } else if ((letterDetailNode.getNodeType() == Node.ELEMENT_NODE)
                        && (letterDetailNode.getNodeName().equals("lettercontent"))) {

                    NodeList nodes = letterDetailNode.getChildNodes();
                    for (int k = 0; k < nodes.getLength(); k++) {
                        Node node = nodes.item(k);
                        if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
                            CDATASection cdata = (CDATASection) node;
                            letter.setContent(cdata.getData());
                        }
                    }
                }
            }

            letters.add(letter);
        }
    }

    private void collectOtherDiagnosis(Document doc) {
        NodeList diagnosisNodes = doc.getElementsByTagName("diagnosis");
        for (int i = 0; i < diagnosisNodes.getLength(); i++) {
            Node diagnosisNode = diagnosisNodes.item(i);
            Node diagnosisNodeChild = diagnosisNode.getFirstChild();
            if (diagnosisNodeChild != null) {
                Diagnosis diagnosis = new Diagnosis();
                diagnosis.setNhsno(getData("nhsno"));
                diagnosis.setUnitcode(getData("centrecode"));
                diagnosis.setDiagnosis(diagnosisNode.getFirstChild().getNodeValue());
                diagnosis.setDisplayorder(i + "");
                otherDiagnoses.add(diagnosis);
            }
        }
    }

    private void collectProcedures(Document doc) {
        NodeList procedureNodes = doc.getElementsByTagName("procedure");
        for (int i = 0; i < procedureNodes.getLength(); i++) {
            Node procedureNode = procedureNodes.item(i);
            Procedure procedure = new Procedure();
            procedure.setNhsno(getData("nhsno"));
            procedure.setUnitcode(getData("centrecode"));
            NodeList procedureDetailNodes = procedureNode.getChildNodes();
            for (int j = 0; j < procedureDetailNodes.getLength(); j++) {
                try {
                    Node procedureDetailNode = procedureDetailNodes.item(j);
                    if ((procedureDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (procedureDetailNode.getNodeName().equals("procedurename"))) {
                        procedure.setProcedure(procedureNode.getFirstChild().getNodeValue());
                    } else if ((procedureDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (procedureDetailNode.getNodeName().equals("proceduredate"))) {
                        procedure.setDate(procedureDetailNode.getFirstChild().getNodeValue());
                    }
                } catch (NullPointerException e) {
                    LOGGER.error(e.getMessage());
                    LOGGER.debug(e.getMessage(), e);
                }
            }
            procedures.add(procedure);
        }
    }

    private void collectDiagnostics(Document doc) {
        NodeList diagnosticsNodes = doc.getElementsByTagName("diagnostic");
        for (int i = 0; i < diagnosticsNodes.getLength(); i++) {
            Node diagnosticNode = diagnosticsNodes.item(i);

            Diagnostic diagnostic = new Diagnostic();
            diagnostic.setNhsno(getData("nhsno"));
            diagnostic.setUnitcode(getData("centrecode"));

            NodeList diagnosticsDetailNodes = diagnosticNode.getChildNodes();
            for (int j = 0; j < diagnosticsDetailNodes.getLength(); j++) {
                try {
                    Node diagnosticDetailNode = diagnosticsDetailNodes.item(j);
                    if ((diagnosticDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (diagnosticDetailNode.getNodeName().equals("diagnosticname"))) {
                        diagnostic.setDescription(diagnosticDetailNode.getFirstChild().getNodeValue());
                    } else if ((diagnosticDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (diagnosticDetailNode.getNodeName().equals("diagnosticdate"))) {
                        diagnostic.setDatestamp(TimestampUtils.createTimestamp(
                                diagnosticDetailNode.getFirstChild().getNodeValue()));
                    } else if ((diagnosticDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (diagnosticDetailNode.getNodeName().equals("diagnostictype"))) {
                        diagnostic.setDiagnosticType(DiagnosticType.getDiagnosticType(
                                diagnosticDetailNode.getFirstChild().getNodeValue()));
                    }
                } catch (NullPointerException e) {
                    LOGGER.error(e.getMessage());
                    LOGGER.debug(e.getMessage(), e);
                }
            }

            if (diagnostic.getDiagnosticType() != null) {
                diagnostics.add(diagnostic);
            }
        }
    }

    private void collectAllergies(Document doc) {
        NodeList allergyNodes = doc.getElementsByTagName("allergy");
        for (int i = 0; i < allergyNodes.getLength(); i++) {
            Node allergyNode = allergyNodes.item(i);
            Allergy allergy = new Allergy();
            allergy.setNhsno(getData("nhsno"));
            allergy.setUnitcode(getData("centrecode"));
            NodeList allergyDetailNodes = allergyNode.getChildNodes();
            for (int j = 0; j < allergyDetailNodes.getLength(); j++) {
                try {
                    Node allergyDetailNode = allergyDetailNodes.item(j);
                    if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (allergyDetailNode.getNodeName().equals("allergysubstance"))) {
                        allergy.setSubstance(allergyNode.getFirstChild().getNodeValue());
                    } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (allergyDetailNode.getNodeName().equals("allergytypecode"))) {
                        allergy.setTypeCode(allergyDetailNode.getFirstChild().getNodeValue());
                    } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (allergyDetailNode.getNodeName().equals("allergyreaction"))) {
                        allergy.setReaction(allergyDetailNode.getFirstChild().getNodeValue());
                    } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (allergyDetailNode.getNodeName().equals("allergyconfidencelevel"))) {
                        allergy.setConfidenceLevel(allergyDetailNode.getFirstChild().getNodeValue());
                    } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (allergyDetailNode.getNodeName().equals("allergyinfosource"))) {
                        allergy.setInfoSource(allergyDetailNode.getFirstChild().getNodeValue());
                    } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (allergyDetailNode.getNodeName().equals("allergystatus"))) {
                        allergy.setStatus(allergyDetailNode.getFirstChild().getNodeValue());
                    } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (allergyDetailNode.getNodeName().equals("allergydescription"))) {
                        allergy.setDescription(allergyDetailNode.getFirstChild().getNodeValue());
                    } else if ((allergyDetailNode.getNodeType() == Node.ELEMENT_NODE)
                            && (allergyDetailNode.getNodeName().equals("allergyrecordeddate"))) {
                        allergy.setRecordedDate(allergyDetailNode.getFirstChild().getNodeValue());
                    }
                } catch (NullPointerException e) {
                    LOGGER.error(e.getMessage());
                    LOGGER.debug(e.getMessage(), e);
                }
            }
            allergies.add(allergy);
        }
    }

    private void collectMedicines(Document doc) {
        NodeList medicineNodes = doc.getElementsByTagName("drug");

        if (medicineNodes != null) {
            for (int i = 0; i < medicineNodes.getLength(); i++) {
                Node medicineNode = medicineNodes.item(i);
                Medicine medicine = new Medicine();
                NodeList medicineDetailNodes = medicineNode.getChildNodes();
                if (medicineDetailNodes != null) {
                    for (int j = 0; j < medicineDetailNodes.getLength(); j++) {
                        Node medicineDetailNode = medicineDetailNodes.item(j);
                        Node medicineDetailNodeFirstChild = medicineDetailNode.getFirstChild();
                        if (medicineDetailNodeFirstChild != null) {
                            if ((medicineDetailNode.getNodeType() == Node.ELEMENT_NODE)
                                    && (medicineDetailNode.getNodeName().equals("drugstartdate"))) {
                                medicine.setStartdate(medicineDetailNodeFirstChild.getNodeValue());
                            } else if ((medicineDetailNode.getNodeType() == Node.ELEMENT_NODE)
                                    && (medicineDetailNode.getNodeName().equals("drugname"))) {
                                medicine.setName(medicineDetailNodeFirstChild.getNodeValue());
                            } else if ((medicineDetailNode.getNodeType() == Node.ELEMENT_NODE)
                                    && (medicineDetailNode.getNodeName().equals("drugdose"))) {
                                medicine.setDose(medicineDetailNodeFirstChild.getNodeValue());
                            }
                        }
                    }
                }
                if (medicine.getStartdate() != null && medicine.getName() != null) {
                    // TODO: add error email sent to unit admin here
                    medicine.setNhsno(getData("nhsno"));
                    medicine.setUnitcode(getData("centrecode"));
                    medicines.add(medicine);
                }
            }
        }
    }

    private void parseDatestamp(TestResult testResult, Node resultDataNode) {
        String dateStampString = resultDataNode.getFirstChild().getNodeValue();
        testResult.setDatestampString(dateStampString);
    }

    private void collectTopLevelData(String dataId, Document doc) {
        NodeList nhsNoNodeList = doc.getElementsByTagName(dataId);
        if (nhsNoNodeList.getLength() != 0) {
            Node nhsNoNode = nhsNoNodeList.item(0);
            if (nhsNoNode.getFirstChild() == null) {
                topLevelDataSpecialCases(xmlData, dataId);
            } else {
                xmlData.put(dataId, nhsNoNode.getFirstChild().getNodeValue());
            }
        } else {
            topLevelDataSpecialCases(xmlData, dataId);
        }
    }

    private void topLevelDataSpecialCases(Map xmlData, String dataId) {
        if (dataId.equals("rrtstatus")) {
            xmlData.put(dataId, "GEN");
        } else if (dataId.equals("diagnosisedta")) {
            xmlData.put(dataId, "DEF");
        }
    }


    public String getData(String dataId) {
        return (String) xmlData.get(dataId);
    }

    public Patient getPatient() {
        Patient patient = new Patient();
        patient.setNhsno((String) xmlData.get("nhsno"));
        patient.setSurname((String) xmlData.get("surname"));
        patient.setForename((String) xmlData.get("forename"));
        String dateofbirth = (String) xmlData.get("dateofbirth");
        if (dateofbirth != null) {
            try {
                patient.setDateofbirth(IMPORT_DATE_FORMAT.parse(dateofbirth));
            } catch (ParseException e) {
                LOGGER.error("Could not parse diagnosisDate {} {}", dateofbirth, e);
            }
        }
        patient.setSex((String) xmlData.get("sex"));
        patient.setAddress1((String) xmlData.get("address1"));
        patient.setAddress2((String) xmlData.get("address2"));
        patient.setAddress3((String) xmlData.get("address3"));
        patient.setAddress4((String) xmlData.get("address4"));
        patient.setPostcode((String) xmlData.get("postcode"));
        patient.setTelephone1((String) xmlData.get("telephone1"));
        patient.setTelephone2((String) xmlData.get("telephone2"));
        patient.setMobile((String) xmlData.get("mobile"));
        patient.setUnitcode((String) xmlData.get("centrecode"));
        patient.setDiagnosis((String) xmlData.get("diagnosisedta"));
        String diagnosisdate = (String) xmlData.get("diagnosisdate");
        if (diagnosisdate != null) {
            try {
                patient.setDiagnosisDate(IMPORT_DATE_FORMAT.parse(diagnosisdate));
            } catch (ParseException e) {
                LOGGER.error("Could not parse diagnosisDate {} {}", diagnosisdate, e);
            }
        }
        patient.setTreatment((String) xmlData.get("rrtstatus"));
        patient.setTransplantstatus((String) xmlData.get("tpstatus"));
        patient.setHospitalnumber((String) xmlData.get("hospitalnumber"));
        patient.setGpname((String) xmlData.get("gpname"));
        patient.setGpaddress1((String) xmlData.get("gpaddress1"));
        patient.setGpaddress2((String) xmlData.get("gpaddress2"));
        patient.setGpaddress3((String) xmlData.get("gpaddress3"));
        patient.setGppostcode((String) xmlData.get("gppostcode"));
        patient.setGptelephone((String) xmlData.get("gptelephone"));
        patient.setGpemail((String) xmlData.get("gpemail"));
        String bmdexam = (String) xmlData.get("bmdexam");
        if (bmdexam != null) {
            try {
                patient.setBmdexam(IMPORT_DATE_FORMAT.parse(bmdexam));
            } catch (ParseException e) {
                LOGGER.error("Could not parse bmdexam {} {}", bmdexam, e);
            }
        }
        patient.setBloodgroup((String) xmlData.get("bloodgroup"));

        return patient;
    }

    public MyIbd getMyIbd() {
        MyIbd myIbd = new MyIbd();
        myIbd.setNhsno((String) xmlData.get("nhsno"));
        myIbd.setUnitcode((String) xmlData.get("centrecode"));
        myIbd.setDiagnosis(org.patientview.ibd.model.enums.Diagnosis.getDiagnosisByXmlName(
                (String) xmlData.get("diagnosis")));

        Object diagnosisDate = xmlData.get("diagnosisdate");
        // this is an ibd only field so can be null
        if (diagnosisDate != null) {
            try {
                myIbd.setYearOfDiagnosis(
                        IMPORT_DATE_FORMAT.parse((String) diagnosisDate));
            } catch (Exception e) {
                LOGGER.error("Could not parse diagnosisyear for MyIbd with NHS No {}", myIbd.getNhsno(), e);
            }
        }

        // if we don't have the required fields for an myibd object just return null, it's an rpv file
        if (myIbd.getNhsno() != null && myIbd.getUnitcode() != null && myIbd.getDiagnosis() != null
                && myIbd.getYearOfDiagnosis() != null) {

            myIbd.setDiseaseExtent(DiseaseExtent.getDiseaseExtentByXmlName((String) xmlData.get("ibddiseaseextent")));
            myIbd.setEiManifestations((String) xmlData.get("ibdeimanifestations"));
            myIbd.setComplications((String) xmlData.get("ibddiseasecomplications"));
            myIbd.setBodyPartAffected((String) xmlData.get("bodypartsaffected"));
            myIbd.setFamilyHistory((String) xmlData.get("familyhistory"));
            myIbd.setSmoking((String) xmlData.get("smokinghistory"));
            myIbd.setSurgery((String) xmlData.get("surgicalhistory"));
            myIbd.setVaccinationRecord((String) xmlData.get("vaccinationrecord"));
            myIbd.setNamedConsultant((String) xmlData.get("namedconsultant"));
            myIbd.setNurses((String) xmlData.get("ibdnurse"));
            if (xmlData.get("colonoscopysurveillance") != null) {
                try {
                    myIbd.setYearForSurveillanceColonoscopy(
                            IMPORT_DATE_FORMAT.parse((String) xmlData.get("colonoscopysurveillance")));
                } catch (ParseException e) {
                    LOGGER.error("Could not parse colonoscopysurveillance for MyIbd with NHS No {}",
                            myIbd.getNhsno(), e);
                }
            }
            return myIbd;
        } else {
            return null;
        }
    }

    public Centre getCentre() {
        Centre centre = new Centre();
        centre.setCentreCode((String) xmlData.get("centrecode"));
        centre.setCentreName((String) xmlData.get("centrename"));
        centre.setCentreAddress1((String) xmlData.get("centreaddress1"));
        centre.setCentreAddress2((String) xmlData.get("centreaddress2"));
        centre.setCentreAddress3((String) xmlData.get("centreaddress3"));
        centre.setCentreAddress4((String) xmlData.get("centreaddress4"));
        centre.setCentrePostCode((String) xmlData.get("centrepostcode"));
        centre.setCentreTelephone((String) xmlData.get("centretelephone"));
        centre.setCentreEmail((String) xmlData.get("centreemail"));

        return centre;
    }

    public String getFlag() {
        return (String) xmlData.get("flag");
    }

    public List<TestResult> getTestResults() {
        return testResults;
    }

    public List<TestResultDateRange> getDateRanges() {
        return dateRanges;
    }

    public List<Letter> getLetters() {
        return letters;
    }

    public List<Diagnosis> getOtherDiagnoses() {
        return otherDiagnoses;
    }

    public List<Medicine> getMedicines() {
        return medicines;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public List<Procedure> getProcedures() {
        return procedures;
    }

    public List<Allergy> getAllergies() {
        return allergies;
    }

    public List<CorruptNode> getCorruptNodes() {
        return corruptNodes;
    }

    public String getFilename() {
        return filename;
    }
}
