import React from 'react';

export default function Text({
    data = '',
    style,
    ...props}) {
  
  console.log(data);
  
  return (<div style={style} {...props} >
    <div style={{padding: '4px'}}>{
      typeof data === 'object'?
      JSON.stringify(data)
      : data
    }</div>
  </div>);
}