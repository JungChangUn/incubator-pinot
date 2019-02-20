/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.core.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.pinot.common.data.RowEvent;
import org.apache.pinot.common.utils.JsonUtils;


/**
 * A plain implementation of RowEvent based on HashMap. Should be reused as much as possible via
 * {@link GenericRow#createOrReuseRow(GenericRow)}
 */
public class GenericRow implements RowEvent {
  private Map<String, Object> _fieldMap = new HashMap<>();

  @Override
  public void init(Map<String, Object> field) {
    _fieldMap = field;
  }

  public Set<Map.Entry<String, Object>> getEntrySet() {
    return _fieldMap.entrySet();
  }

  @Override
  public String[] getFieldNames() {
    return _fieldMap.keySet().toArray(new String[_fieldMap.size()]);
  }

  @Override
  public Object getValue(String fieldName) {
    return _fieldMap.get(fieldName);
  }

  public void putField(String key, Object value) {
    _fieldMap.put(key, value);
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (String key : _fieldMap.keySet()) {
      Object value = _fieldMap.get(key);
      b.append(key);
      b.append(" : ");
      if (value instanceof Object[]) {
        b.append(Arrays.toString((Object[]) value));
      } else {
        b.append(value);
      }
      b.append(", ");
    }
    return b.toString();
  }

  @Override
  public int hashCode() {
    return _fieldMap.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof GenericRow)) {
      return false;
    }
    GenericRow r = (GenericRow) o;
    return _fieldMap.equals(r._fieldMap);
  }

  /**
   * Empties the values of this generic row, keeping the keys and hash map nodes to minimize object allocation.
   */
  public void clear() {
    for (Map.Entry<String, Object> mapEntry : getEntrySet()) {
      mapEntry.setValue(null);
    }
  }

  public static GenericRow fromBytes(byte[] buffer)
      throws IOException {
    Map<String, Object> fieldMap = JsonUtils.bytesToObject(buffer, Map.class);
    GenericRow genericRow = new GenericRow();
    genericRow.init(fieldMap);
    return genericRow;
  }

  public byte[] toBytes()
      throws IOException {
    return JsonUtils.objectToBytes(_fieldMap);
  }

  /**
   * Creates a new row if the row given is null, otherwise just {@link GenericRow#clear()} the row so that it can be
   * reused.
   *
   * @param row The row to potentially reuse.
   * @return A cleared or new row.
   */
  public static GenericRow createOrReuseRow(GenericRow row) {
    if (row == null) {
      return new GenericRow();
    } else {
      row.clear();
      return row;
    }
  }
}
