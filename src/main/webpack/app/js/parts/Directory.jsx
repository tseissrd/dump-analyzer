import React from 'react';

export default function Directory({
  title,
  data = [],
  chosen,
  useContext = () => ({}),
  style,
  ...props}) {
    
  const {setValue} = useContext();
  
  const onClick = (value) => {
    setValue(
      "file",
      value
    );
  };
  
  return (<div style={style} {...props} >
    <div style={{padding: '4px'}}>
      <h3>{title}</h3>
      {data.map((file, num) => <div style={{
        width: '100%',
        height: '20px',
        border: 'thin solid black'
      }} key={num} >
        <button style={{
          width: '100%',
          height: '20px',
          backgroundColor: chosen === file?
            'gold'
            : 'white'
        }} onClick={() => onClick(file)}>
          {file}
        </button>
      </div>)}
    </div>
  </div>);
}