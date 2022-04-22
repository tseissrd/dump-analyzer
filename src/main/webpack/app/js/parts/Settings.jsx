import React from 'react';

export default function Settings({
    data = [],
    useContext = () => ({}),
    style,
    ...props}) {
    
  const {setValue} = useContext();
    
  const onChange = (event, setting) => {
    setValue(
        setting,
        event
          .target
          .value
      );
  };
  
  return (<div style={style} {...props} >
    {data.map(({title, value}, num) => <div style={{
      width: '298px',
      height: '98px',
      border: 'thin solid black',
      float: 'left'
    }} key={num} >
        <div style={{
          padding: '4px'
        }}>
          <h3>{title}</h3>
          <input style={{
              
            }}
            type='text'
            placeholder={title}
            onChange={ev => onChange(ev, value)}
          />
        </div>
      </div>)}
  </div>);
}