import React, { useState } from 'react';

type OptionButtonProps = {
  options: string[];
  setOption: (option: string) => void;
};

function OptionButton({ options, setOption }: OptionButtonProps) {
  const [index, setIndex] = useState(0);

  async function handleOptionChange() {
    const newIndex = (index + 1) % options.length;
    setIndex(newIndex);
    setOption(options[newIndex]);
  }

  return <button onClick={handleOptionChange}>{options[index].replace('_', ' ')}</button>;
}

export default OptionButton;
