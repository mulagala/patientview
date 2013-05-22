<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.worthsoln.ibd.model.symptoms.SymptomsData" %>
<%@ page import="com.worthsoln.ibd.model.enums.Severity" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>

<%--
  ~ PatientView
  ~
  ~ Copyright (c) Worth Solutions Limited 2004-2013
  ~
  ~ This file is part of PatientView.
  ~
  ~ PatientView is free software: you can redistribute it and/or modify it under the terms of the
  ~ GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  ~ or (at your option) any later version.
  ~ PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  ~ the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU General Public License for more details.
  ~ You should have received a copy of the GNU General Public License along with PatientView in a file
  ~ titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  ~
  ~ @package PatientView
  ~ @link http://www.patientview.org
  ~ @author PatientView <info@patientview.org>
  ~ @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  ~ @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  --%>

<div id="symptomsContainer">
    <div class="page-header">
        <h1>Colitis</h1>
    </div>
    <ul class="padded-list">
        <li>
            The symptom checker can be used to keep a personal record of your colitis symptoms over time.
        </li>
        <li>
            The scores generated from the questions are based on previous validated research studies.
        </li>
        <li>
            If you have 3 days of worsening symptoms from your bowels, record your symptoms every day on the chart for 2
            weeks.
        </li>
        <li>
            Please note the symptom checker is not reviewed by healthcare professionals and is for your personal use
            only.
        </li>
        <li>
            Guidance generated by the chart is advice only. If you have any concerns about your condition, please do not
            hesitate to <html:link action="/patient/contact">Contact us by about your health</html:link> or via the IBD
            helpline on 0161 20 60423.
        </li>
        <li>
             For more information about flare-ups <a target="_blank" href="http://www.myibdportal.org/about-flare-ups">click here.</a>
        </li>
    </ul>
    <hr/>

    <div class="row">
        <div class="span5">
            <html:form action="/colitis-update" styleClass="form-horizontal" styleId="symptomsForm">
                <html:errors/>

                <input type="hidden" name="fromDate" class="fromDate" value="" />
                <input type="hidden" name="toDate" class="toDate" value="" />

                <div class="control-group">
                    <label class="control-label">Date</label>

                    <div class="controls">
                        <div class="input-append date datePicker"
                             data-date="<bean:write name="colitisSymptomsForm" property="symptomDate"/>">
                            <input name="symptomDate" class="span2" size="16" type="text"
                                   value="<bean:write name="colitisSymptomsForm" property="symptomDate"/>" readonly>
                            <span class="add-on"><i class="icon-th"></i></span>
                        </div>
                    </div>
                </div>

                <logic:present name="stoolsDayList" scope="session">
                    <div class="control-group">
                        <label class="control-label">Number of Stools (Day)</label>

                        <div class="controls">
                            <html:select property="numberOfStoolsDaytimeId">
                                <html:options collection="stoolsDayList" property="id"
                                              labelProperty="displayText"/>
                            </html:select>
                        </div>
                    </div>
                </logic:present>

                <logic:present name="stoolsNightList" scope="session">
                    <div class="control-group">
                        <label class="control-label">Number of Stools (Night)</label>

                        <div class="controls">
                            <html:select property="numberOfStoolsNighttimeId">
                                <html:options collection="stoolsNightList" property="id"
                                              labelProperty="displayText"/>
                            </html:select>
                        </div>
                    </div>
                </logic:present>

                <logic:present name="toiletTimingList" scope="session">
                    <div class="control-group">
                        <label class="control-label">When I go to the toilet?</label>

                        <div class="controls">
                            <html:select property="toiletTimingId">
                                <html:options collection="toiletTimingList" property="id" labelProperty="displayText"/>
                            </html:select>
                        </div>
                    </div>
                </logic:present>

                <logic:present name="presentBloodList" scope="session">
                    <div class="control-group">
                        <label class="control-label">Is there blood present mixed in the stool?</label>

                        <div class="controls">
                            <html:select property="presentBloodId">
                                <html:options collection="presentBloodList" property="id"
                                              labelProperty="displayText"/>
                            </html:select>
                        </div>
                    </div>
                </logic:present>

                <logic:present name="feelingList" scope="session">
                    <div class="control-group">
                        <label class="control-label">How do I feel?</label>

                        <div class="controls">
                            <html:select property="feelingId">
                                <html:options collection="feelingList" property="id"
                                              labelProperty="displayText"/>
                            </html:select>
                        </div>
                    </div>
                </logic:present>

                <logic:present name="furtherComplicationList" scope="session">
                    <div class="control-group">
                        <label class="control-label">
                            Do I have any further complications?

                            <logic:present name="bodyPartAffectedLink">
                                <a href="<bean:write name="bodyPartAffectedLink"/>" target="_blank"><i class="icon-info-sign"></i></a>
                            </logic:present>
                        </label>

                        <div class="controls">
                            <html:select property="complicationId">
                                <html:options collection="furtherComplicationList" property="id"
                                              labelProperty="displayText"/>
                            </html:select>
                        </div>
                    </div>
                </logic:present>

                <div class="form-actions">
                    <html:submit value="Save" styleClass="btn btn-primary"/>
                </div>
            </html:form>
        </div>

        <jsp:include page="graph.jsp" />

        <div class="span7">
            <logic:present name="myIbdSeverityLevel">
                <div>
                    <bean:define id="myIbdSeverityLevel" name="myIbdSeverityLevel"
                                 type="com.worthsoln.ibd.model.MyIbdSeverityLevel"/>



                    <%
                        if (myIbdSeverityLevel.getSeverity().equals(Severity.SEVERE)) {
                    %>

                    <h3 class="<%=myIbdSeverityLevel.getSeverity().name().toLowerCase()%>">
                        Very Active
                    </h3>

                    <p>
                        Your symptom score suggests ulcerative colitis is very active and you are experiencing a severe
                        flare-up. We advise you to monitor your symptoms daily on the chart and contact the IBD team or
                        speak to your GP within the next 24 hours.
                    </p>
                    <p>
                        If this is not possible and you are concerned then call
                        <a target="_blank" href="http://www.nhsdirect.nhs.uk/">NHS Direct</a> on 0845 46 47, your local
                        GP out-of-hours service or attend your local A&E department.
                    </p>

                    <p>With a severe flare-up the following symptoms are described:</p>

                    <ul>
                        <li>Blood or clots mixed in with the stools</li>
                        <li>Needing to open your bowels at night</li>
                        <li>High fever (more than 37.8 &deg;C)</li>
                        <li>Rapid heart beat (Pulse greater than 90 beats per minute)</li>
                        <li>Constant abdominal pain and tenderness </li>
                        <li>Significant weight loss (Greater than 5%)</li>
                    </ul>

                    <p>Please <strong>do not</strong> take anti-diarrhoeal treatment as it could worsen your condition.</p>

                    <p><strong>Contact Details</strong></p>

                    <p>
                        To contact the IBD nurses from Monday to Friday 0900-1700 call the IBD helpline on 0161 206 4023
                        or email via the <html:link action="/patient/contact">About Your Health box on the Contact page</html:link>.  Please leave your:
                    </p>

                    <ul>
                        <li>Name</li>
                        <li>Hospital number</li>
                        <li>Contact details</li>
                        <li>A short message</li>
                    </ul>
                    <%
                    } else if (myIbdSeverityLevel.getSeverity().equals(Severity.MODERATE)) {
                    %>

                    <h3 class="<%=myIbdSeverityLevel.getSeverity().name().toLowerCase()%>">
                        Active
                    </h3>

                    <p>
                        Your symptom score suggests your ulcerative colitis is active and you are experiencing a flare-up.
                    </p>

                    <p>
                        Please login and monitor your symptoms daily for at least 14 days. If your symptoms deteriorate
                        or are no better within 5 days you need to contact us (see below).
                    </p>

                    <p>
                        In carefully selected individuals experiencing a flare-up, the IBD team may have made a
                        self-management plan, so you do not have to wait for a hospital or GP visit to start treatment.
                        If so, the following medicines has been recommended:
                    </p>

                     <%
                        if (myIbdSeverityLevel.getTreatment() != null && myIbdSeverityLevel.getTreatment().length() > 0) {
                    %>
                    <p>
                        <%=myIbdSeverityLevel.getTreatment()%>
                    </p>

                    <%
                        }
                    %>

                    <p>Please <strong>do not</strong> take anti-diarrhoeal treatment as it could worsen your condition.</p>

                    <p>You should definitely contact us in the following circumstances:</p>
                    <ul>
                        <li>If your flare up comes back as soon as you stop or reduce treatment.</li>
                        <li>If you need to use more than two courses of oral steroids tablets a year.</li>
                        <li>If you are losing weight without dieting.</li>
                        <li>If you are losing bloods from your bowels between flare-ups.</li>
                        <li>If you have any worrying symptoms</li>
                    </ul>

                    <p><strong>Contact Details</strong></p>

                    <p>
                        To contact the IBD nurses from Monday to Friday 0900-1700 call the IBD helpline on 0161 206 4023
                        or email via the <html:link action="/patient/contact">About Your Health box on the Contact page</html:link>.  Please leave your:
                    </p>

                    <ul>
                        <li>Name</li>
                        <li>Hospital number</li>
                        <li>Contact details</li>
                        <li>A short message</li>
                    </ul>
                    <%
                    } else if (myIbdSeverityLevel.getSeverity().equals(Severity.MILD)) {
                    %>

                    <h3 class="<%=myIbdSeverityLevel.getSeverity().name().toLowerCase()%>">
                        Inactive (Remission)
                    </h3>

                    <p>
                        Excellent. Your ulcerative colitis seems under good control. Please carry on taking your current
                        medicines.  If you want to learn more about maintaining remission and
                        <a href="http://www.myibdportal.org/staying-well-with-ibd">staying well with IBD, click here.</a>
                    </p>
                    <%
                        }
                    %>
                </div>
            </logic:present>
        </div>
    </div>
</div>
