import React from 'react';

export default function IhsHttpAccess({
    data = '',
    style,
    ...props}) {
  
  return (<div style={style} {...props} >
    <div style={{padding: '4px'}}>{
      typeof data === 'object'?
      JSON.stringify(data)
      : data
    }</div>
  </div>);
}