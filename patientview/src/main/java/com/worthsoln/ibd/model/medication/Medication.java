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

package com.worthsoln.ibd.model.medication;

import com.worthsoln.patientview.model.BaseModel;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "ibd_medication")
public class Medication extends BaseModel {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "ibd_medication_allowed_dosages",
            joinColumns = {@JoinColumn(name = "medication_id")},
            inverseJoinColumns = {@JoinColumn(name = "dose_id")}
    )
    private List<MedicationDose> allowedDosages;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MedicationDose> getAllowedDosages() {
        // order by MG
        if (allowedDosages != null) {
            Collections.sort(allowedDosages, new Comparator<MedicationDose>() {
                @Override
                public int compare(MedicationDose o1, MedicationDose o2) {
                    if (o1.getId() > o2.getId()) {
                        return 1;
                    } else if (o1.getId() < o2.getId()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        }

        return allowedDosages;
    }

    public void setAllowedDosages(List<MedicationDose> allowedDosages) {
        this.allowedDosages = allowedDosages;
    }
}
