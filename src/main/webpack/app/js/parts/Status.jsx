import React from 'react';

export default function Status({data, title, style, applyAction, ...props}) {
    
  return (<div style={style}
    {...props} >
      <div style={{
        padding: '4px'
      }}>
        <h3>{title}</h3>
        <div>тредов: {data.threadsNumber}</div>
        <div>стратегия: {data.strategy}</div>
        {applyAction? (<div style={{
            marginLeft: 'auto',
            marginRight: 'auto',
            width: 'fit-content',
            paddingTop: '10px'
          }}>
            <button style={{
                width: '90px',
                height: '40px'
              }}  onClick={() => applyAction()}>
                Применить
            </button>
          </div>)
          : ('')}
      </div>
    </div>);
}
