import OriginalSidebar from '@theme-original/DocSidebar';
import React from 'react';
import FrameworkSelector from '@theme/FrameworkSelector';
import Logo from '@theme/Logo';

export default function DocSidebar(props) {
  return (
    <>
      <Logo />
      {/* <FrameworkSelector /> */}
      <OriginalSidebar {...props} />
    </>
  );
}
