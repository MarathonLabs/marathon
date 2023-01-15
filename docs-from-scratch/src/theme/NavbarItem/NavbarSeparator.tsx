import clsx from 'clsx';
import React from 'react';
const omit = require('lodash/omit');

export default function NavbarSeparator(props) {
  const passableProps = { ...props } as Partial<typeof props>;
  delete passableProps.mobile;

  return <div {...passableProps} className={clsx(props.className, 'separator')} />;
}