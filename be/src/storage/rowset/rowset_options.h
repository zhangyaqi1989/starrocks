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

#include <memory>
#include <unordered_map>
#include <vector>

#include "column/column_access_path.h"
#include "runtime/global_dict/types.h"
#include "storage/olap_common.h"
#include "storage/options.h"
#include "storage/predicate_tree/predicate_tree.hpp"
#include "storage/runtime_filter_predicate.h"
#include "storage/runtime_range_pruner.h"
#include "storage/seek_range.h"
#include "storage/tablet_schema.h"

namespace starrocks {
class Conditions;
class KVStore;
struct OlapReaderStatistics;
class RuntimeProfile;
class RowCursor;
class RuntimeState;
class TabletSchema;

class ColumnPredicate;
class DeletePredicates;
class ChunkPredicate;
struct RowidRangeOption;
struct ShortKeyRangesOption;
struct VectorSearchOption;
using VectorSearchOptionPtr = std::shared_ptr<VectorSearchOption>;

class RowsetReadOptions {
    using RowidRangeOptionPtr = std::shared_ptr<RowidRangeOption>;
    using ShortKeyRangesOptionPtr = std::shared_ptr<ShortKeyRangesOption>;
    using PredicateList = std::vector<const ColumnPredicate*>;

public:
    ReaderType reader_type = READER_QUERY;
    int chunk_size = DEFAULT_CHUNK_SIZE;

    std::vector<SeekRange> ranges;

    PredicateTree pred_tree;
    PredicateTree pred_tree_for_zone_map;
    RuntimeFilterPredicates runtime_filter_preds;

    // whether rowset should return rows in sorted order.
    bool sorted = true;

    const DeletePredicates* delete_predicates = nullptr;

    TabletSchemaCSPtr tablet_schema = nullptr;

    bool is_primary_keys = false;
    int64_t version = 0;
    KVStore* meta = nullptr;

    OlapReaderStatistics* stats = nullptr;
    RuntimeState* runtime_state = nullptr;
    RuntimeProfile* profile = nullptr;
    bool use_page_cache = false;
    LakeIOOptions lake_io_opts;

    ColumnIdToGlobalDictMap* global_dictmaps = &EMPTY_GLOBAL_DICTMAPS;
    const std::unordered_set<uint32_t>* unused_output_column_ids = nullptr;

    RowidRangeOptionPtr rowid_range_option = nullptr;
    ShortKeyRangesOptionPtr short_key_ranges_option = nullptr;

    RuntimeScanRangePruner runtime_range_pruner;

    std::vector<ColumnAccessPathPtr>* column_access_paths = nullptr;

    bool asc_hint = true;

    bool prune_column_after_index_filter = false;
    bool enable_gin_filter = false;
    bool has_preaggregation = true;

    bool use_vector_index = false;

    VectorSearchOptionPtr vector_search_option = nullptr;

    TTableSampleOptions sample_options;
    bool enable_join_runtime_filter_pushdown = false;
};

} // namespace starrocks
