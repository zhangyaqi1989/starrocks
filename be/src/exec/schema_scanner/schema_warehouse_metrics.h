// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#pragma once

#include "exec/schema_scanner.h"
#include "gen_cpp/FrontendService_types.h"

namespace starrocks {

class WarehouseMetricsScanner : public SchemaScanner {
public:
    WarehouseMetricsScanner();
    ~WarehouseMetricsScanner() override = default;

    Status start(RuntimeState* state) override;
    Status get_next(ChunkPtr* chunk, bool* eos) override;

private:
    Status get_new_table();
    Status fill_chunk(ChunkPtr* chunk);

    size_t _idx = 0;
    TGetWarehouseMetricsRespone _response;
    static SchemaScanner::ColumnDesc _s_columns[];
};

} // namespace starrocks