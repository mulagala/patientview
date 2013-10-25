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

package org.patientview.repository;

import org.patientview.model.patientview.Conversation;
import org.patientview.model.patientview.enums.GroupEnum;

import java.util.List;

public interface ConversationDao {

    /**
     * Get specific conversation by ID
     * @param id Long
     * @return Conversation
     */
    Conversation get(Long id);

    /**
     * Get all conversations a user is part of
     * @param participantId Long
     * @return List<Conversation>
     */
    List<Conversation> getConversations(Long participantId);

    List<Conversation> getConversations(Long participantId, GroupEnum groupEnum);

    void save(Conversation conversation);

    void delete(Conversation conversation);
}
