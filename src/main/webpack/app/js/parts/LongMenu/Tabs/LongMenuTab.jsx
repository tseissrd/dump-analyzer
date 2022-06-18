import React from 'react';

export default function Menu({
    data = [],
    useContext = () => ({}),
    style,
    ...props}) {
    
  const {setOption} = useContext();
    
  const onClick = value => {
    setOption(value);
  };
  
  return (<div style={style} {...props} >
    {data.map(({title, value}, num) => <div style={{
      width: '98px',
      height: '98px',
      border: 'thin solid black',
      float: 'left'
    }} key={num} >
        <button style={{
          width: '98px',
          height: '98px'
        }} onClick={() => onClick(value)}>{title}</button>
      </div>)}
  </div>);
}