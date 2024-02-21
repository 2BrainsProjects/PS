import React, { useEffect } from 'react';
import './Pagination.css';
import { useSearchParams } from 'react-router-dom';

type PaginationProps = {
  onPageChange: (page: number) => void;
  hasPrevious: boolean;
  hasNext: boolean;
};

function Pagination({ onPageChange, hasPrevious, hasNext }: PaginationProps) {
  const [page, setPage] = React.useState(1);
  const [searchParams, setSearchParams] = useSearchParams();

  async function updatePage(pageNumber: number) {
    await onPageChange(pageNumber);
    setPage(pageNumber);
    setSearchParams({ page: pageNumber.toString() });
  }

  useEffect(() => {
    const pageParam = searchParams.get('page');
    const currentPage = pageParam ? +pageParam : undefined;
    if (currentPage) {
      updatePage(currentPage);
    }
  }, []);

  return (
    <div className="Pagination">
      {hasPrevious && page > 1 && (
        <button className="previous" onClick={() => updatePage(page - 1)}>
          Previous
        </button>
      )}
      {hasNext && (
        <button className="next" onClick={() => updatePage(page + 1)}>
          Next
        </button>
      )}
    </div>
  );
}

export default Pagination;
