/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.component.reducer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.merger.component.ReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.row.OrderByRow;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

/**
 * 流式排序.
 *
 * @author gaohongtao
 */
@Slf4j
public class StreamingOrderByReducerResultSet extends AbstractResultSetAdapter implements ReducerResultSet {
    
    private final List<OrderByColumn> orderByColumns;
    
    private List<ResultSet> effectiveResultSets;
    
    private boolean initial;
    
    private Map<ResultSet, Integer> innerStateMap = new HashMap<>();
    
    public StreamingOrderByReducerResultSet(final List<OrderByColumn> orderByColumns) {
        this.orderByColumns = orderByColumns;
    }
    
    @Override
    public void inject(final List<ResultSet> preResultSet) throws SQLException {
        setResultSets(preResultSet);
        setCurrentResultSet(preResultSet.get(0));
        effectiveResultSets = Lists.newArrayList(preResultSet);
        if (log.isDebugEnabled()) {
        
        }
    }
    
    @Override
    public boolean next() throws SQLException {
        if (initial) {
            nextEffectiveResultSets();
        } else {
            initial = true;
        }
        OrderByRow chosenOrderByValue = null;
        for (ResultSet each : effectiveResultSets) {
            OrderByRow eachOrderByValue = new OrderByRow(orderByColumns, each);
            if (null == chosenOrderByValue || chosenOrderByValue.compareTo(eachOrderByValue) > 0) {
                chosenOrderByValue = eachOrderByValue;
                setCurrentResultSet(each);
            }
        }
        return !effectiveResultSets.isEmpty();
    }
    
    private void nextEffectiveResultSets() throws SQLException {
        boolean next = getCurrentResultSet().next();
        if (!next) {
            effectiveResultSets.remove(getCurrentResultSet());
        }
    }
}