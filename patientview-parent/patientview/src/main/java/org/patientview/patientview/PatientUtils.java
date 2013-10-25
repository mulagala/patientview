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

package org.patientview.patientview;

import org.patientview.model.Patient;
import org.patientview.model.patientview.User;
import org.patientview.model.patientview.UserMapping;
import org.patientview.utils.LegacySpringUtils;

import javax.servlet.http.HttpServletRequest;

public final class PatientUtils {

    private PatientUtils() {
    }

    public static String retrieveNhsNo(HttpServletRequest request) {
        User user = LegacySpringUtils.getUserManager().retrieveUser(request);
        UserMapping userMapping = LegacySpringUtils.getUserManager().getUserMappingPatientEntered(user);
        return userMapping.getNhsno();
    }

    public static Patient retrievePatient(HttpServletRequest request) {
        String nhsno = PatientUtils.retrieveNhsNo(request);

        User user = LegacySpringUtils.getUserManager().retrieveUser(request);
        UserMapping userMapping = LegacySpringUtils.getUserManager().getUserMappingPatientEntered(user);

        return retrievePatient(nhsno, userMapping.getUnitcode());
    }

    public static Patient retrievePatient(String nhsno, String unitcode) {
        return LegacySpringUtils.getPatientManager().get(nhsno, unitcode);
    }
}
