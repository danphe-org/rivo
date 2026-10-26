"""Pagination and search filter models matching bazar-common.

Mirrors:
- com.danphe.bazar.common.library.base.common.Page<T>
- com.danphe.bazar.common.library.base.filter.AbstractFilter
"""
from typing import Generic, List, Optional, TypeVar
from pydantic import BaseModel, Field

T = TypeVar("T")


class Page(BaseModel, Generic[T]):
    """Standard pagination envelope matching bazar-backend's Page<T>."""

    totalRecords: int = Field(default=0, description="Total count of records matching the filter criteria")
    limit: int = Field(default=10, description="Maximum records returned per page")
    currentPage: int = Field(default=1, description="Current page number (1-indexed)")
    data: List[T] = Field(default_factory=list, description="Array of records")


class AbstractFilter(BaseModel):
    """Base filter criteria matching bazar-backend's AbstractFilter."""

    id: Optional[str] = Field(default=None, description="Filter by unique ID")
    ids: Optional[List[str]] = Field(default=None, description="Filter by list of unique IDs")
    pageSize: int = Field(default=10, ge=1, le=100, description="Number of items per page")
    pageNumber: int = Field(default=1, ge=1, description="Page number (1-indexed)")
    delete: Optional[bool] = Field(default=False, description="Whether to include soft-deleted records")
    sortField: Optional[str] = Field(default="created_on", description="Field to sort by")
    sortOrder: str = Field(default="DESC", description="Sort order direction ('ASC' or 'DESC')")
    active: Optional[bool] = Field(default=True, description="Filter by active status")
    searchParameter: Optional[str] = Field(default=None, description="Global text search query keyword")

    @property
    def offset(self) -> int:
        """Calculates SQL query offset from pageNumber and pageSize."""
        page = max(1, self.pageNumber)
        return (page - 1) * max(1, self.pageSize)

    @property
    def sort_direction(self) -> str:
        """Normalized sort direction."""
        return "ASC" if self.sortOrder.upper() == "ASC" else "DESC"

    @property
    def like_search_parameter(self) -> Optional[str]:
        """Returns wildcard wrapped parameter for SQL LIKE queries (e.g. '%keyword%')."""
        if self.searchParameter and self.searchParameter.strip():
            return f"%{self.searchParameter.strip()}%"
        return None
