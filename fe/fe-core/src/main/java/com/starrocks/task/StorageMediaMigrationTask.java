// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.starrocks.task;

import com.starrocks.thrift.TStorageMedium;
import com.starrocks.thrift.TStorageMediumMigrateReq;
import com.starrocks.thrift.TTaskType;

public class StorageMediaMigrationTask extends AgentTask {

    private int schemaHash;
    private TStorageMedium toStorageMedium;

    // Rebuild persistent index at the end of clone task
    private boolean needRebuildPkIndex = false;

    public StorageMediaMigrationTask(long backendId, long tabletId, int schemaHash,
                                     TStorageMedium toStorageMedium, boolean needRebuildPkIndex) {
        super(null, backendId, TTaskType.STORAGE_MEDIUM_MIGRATE, -1L, -1L, -1L, -1L, tabletId);

        this.schemaHash = schemaHash;
        this.toStorageMedium = toStorageMedium;
        this.needRebuildPkIndex = needRebuildPkIndex;
    }

    public TStorageMediumMigrateReq toThrift() {
        TStorageMediumMigrateReq request = new TStorageMediumMigrateReq(tabletId, schemaHash, toStorageMedium);
        request.setNeed_rebuild_pk_index(needRebuildPkIndex);
        return request;
    }

    public int getSchemaHash() {
        return schemaHash;
    }

    public TStorageMedium getToStorageMedium() {
        return toStorageMedium;
    }
}
