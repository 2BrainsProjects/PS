import React from 'react';
import './ErrorPopup.css';
import { useErrorContext } from '../../../contexts/ErrorContext';

function ErrorPopup() {
  const { error } = useErrorContext();
  if (!error) {
    return null;
  }
  return (
    <div className="ErrorPopup">
      <p className="title">{error?.title}</p>
      <p className="detail">{error?.detail}</p>
    </div>
  );
}

export default ErrorPopup;
