package com.relatosdepapel.catalogueservice.dto;

import java.util.List;

public class PaginatedResponse<T> {

    private List<T> data;
    private Meta meta;
    private List<AggregationDetails> aggregations;

    public PaginatedResponse() {
    }

    public PaginatedResponse(
            List<T> data,
            long count,
            int page,
            int limit,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
    ) {
        this.data = data;
        this.meta = new Meta(
                count,
                page,
                limit,
                totalPages,
                hasNext,
                hasPrevious
        );
    }

    public PaginatedResponse(
            List<T> data,
            long count,
            int page,
            int limit,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            List<AggregationDetails> aggregations
    ) {
        this.data = data;
        this.meta = new Meta(
                count,
                page,
                limit,
                totalPages,
                hasNext,
                hasPrevious
        );
        this.aggregations = aggregations;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<AggregationDetails> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<AggregationDetails> aggregations) {
        this.aggregations = aggregations;
    }

    public static class Meta {
        private long count;
        private int page;
        private int limit;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public Meta() {
        }

        public Meta(
                long count,
                int page,
                int limit,
                int totalPages,
                boolean hasNext,
                boolean hasPrevious
        ) {
            this.count = count;
            this.page = page;
            this.limit = limit;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public boolean isHasPrevious() {
            return hasPrevious;
        }

        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
    }
}