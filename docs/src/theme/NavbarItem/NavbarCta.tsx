import useBaseUrl from '@docusaurus/useBaseUrl';
import clsx from 'clsx';
import React from 'react';

export default function NavbarCta({ text, href, ...props }) {
  const passableProps = { ...props } as Partial<typeof props>;
  delete passableProps.mobile;

  return (
    <a {...passableProps} href={useBaseUrl(href)} className={clsx(props.className, 'cta')}>
      {text}
      <svg xmlns="http://www.w3.org/2000/svg" className="ionicon" viewBox="0 0 512 512" width="12" height="12">
        <title>Arrow Forward</title>
        <path
          fill="none"
          stroke="currentColor"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="48"
          d="M268 112l144 144-144 144M392 256H100"
        />
      </svg>
    </a>
  );
}
