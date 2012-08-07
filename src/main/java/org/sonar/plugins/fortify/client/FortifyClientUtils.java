/*
 * Sonar Fortify Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.fortify.client;

import com.fortify.schema.fws.MeasurementHistoryListResponse;
import com.fortify.schema.fws.VariableHistoryListResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import xmlns.www_fortifysoftware_com.schema.wstypes.MeasurementHistory;
import xmlns.www_fortifysoftware_com.schema.wstypes.VariableHistory;

import java.util.List;
import java.util.Map;

final class FortifyClientUtils {
  private FortifyClientUtils() {
  }

  static List<MeasurementHistory> keepMoreRecent(MeasurementHistoryListResponse response) {
    Map<String, MeasurementHistory> recents = Maps.newHashMap();
    for (MeasurementHistory mh : response.getMeasurementHistories()) {
      putIfRecent(recents, mh);
    }
    return Lists.newArrayList(recents.values());
  }

  static List<VariableHistory> keepMoreRecent(VariableHistoryListResponse response) {
    Map<String, VariableHistory> recents = Maps.newHashMap();
    for (VariableHistory vh : response.getVariableHistories()) {
      putIfRecent(recents, vh);
    }
    return Lists.newArrayList(recents.values());
  }

  private static void putIfRecent(Map<String, MeasurementHistory> recents, MeasurementHistory mh) {
    MeasurementHistory existing = recents.get(mh.getMeasurementGuid());
    if (existing == null || date(existing) < date(mh)) {
      recents.put(mh.getMeasurementGuid(), mh);
    }
  }

  private static void putIfRecent(Map<String, VariableHistory> recents, VariableHistory vh) {
    VariableHistory existing = recents.get(vh.getVariable().getVariable());
    if (existing == null || date(existing) < date(vh)) {
      recents.put(vh.getVariable().getVariable(), vh);
    }
  }

  private static long date(MeasurementHistory mh) {
    return mh.getSnapshot().getDate().toGregorianCalendar().getTimeInMillis();
  }

  private static long date(VariableHistory mh) {
    return mh.getSnapshot().getDate().toGregorianCalendar().getTimeInMillis();
  }
}
