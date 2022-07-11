import React from 'react';

export default function Tabs({
    data = [],
    useContext = () => ({}),
    style,
    chosen,
    ...props
}) {
  
  const context = useContext();
  
  const tabsBlockStyle = {
    height: '110px',
    width: '162px',
    display: 'inline-block',
    border: 'thin solid black'
  };
  
  const blocks = [];
  
  for (
    let blockNum = 0;
    blockNum < (
      Math.ceil(data.length / 5)
    );
    blockNum += 1
  )
    blocks.push(
      <div
        style={tabsBlockStyle}
        key={blockNum}
      >
        {
          data.slice(
            blockNum * 5,
            (blockNum + 1) * 5
          ).map((
            {
              id,
              title,
              action
            }, num) => <div style={{
              width: '160px',
              height: '20px',
              border: 'thin solid black',
              display: 'block',
              borderCollapse: 'collapse'
            }} key={num} >
                <button style={{
                  width: '160px',
                  height: '20px',
                  backgroundColor: chosen === id?
                    'gold'
                    : 'white'
                }} onClick={() => action(context)}>{title}</button>
              </div>)
        }
      </div>
    );
    
  return (<div style={style} {...props} >
    {blocks}
  </div>);
}