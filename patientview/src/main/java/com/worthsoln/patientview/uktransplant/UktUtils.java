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

package com.worthsoln.patientview.uktransplant;

import javax.servlet.http.HttpServletRequest;
import com.worthsoln.patientview.model.UktStatus;
import com.worthsoln.utils.LegacySpringUtils;

public class UktUtils {

    public static void addUktStatusToRequest(String nhsno, HttpServletRequest request)
            throws Exception {
        UktStatusForPatient readableStatus = retreiveUktStatus(nhsno);
        request.setAttribute("uktstatus", readableStatus);
    }

    public static UktStatusForPatient retreiveUktStatus(String nhsno) {
        UktStatusForPatient readableStatus = null;
        UktStatus status = LegacySpringUtils.getUkTransplantManager().getUktStatus(nhsno);
        if (status != null) {
            readableStatus = new UktStatusForPatient(status.getNhsno(), makeRawStatusReadable(status.getKidney()),
                    makeRawStatusReadable(status.getPancreas()));
        } else {
            readableStatus = new UktStatusForPatient("", "", "");
        }
        return readableStatus;
    }

    static String makeRawStatusReadable(String rawStaus) {
        if ("A".equalsIgnoreCase(rawStaus)) {
            return "Active";
        }
        if ("S".equalsIgnoreCase(rawStaus)) {
            return "Suspended";
        }
        if ("T".equalsIgnoreCase(rawStaus)) {
            return "Transplanted";
        }
        return "Not on list";
    }

}
