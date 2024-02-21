import React from 'react';
import './Switch.css';

type SwitchButtonsProps = {
  onClick?: () => void;
};

function SwitchButton({ onClick }: SwitchButtonsProps) {
  return (
    <label className="switch" onClick={onClick}>
      <input type="checkbox" />
      <span className="slider" />
    </label>
  );
}

export default SwitchButton;
