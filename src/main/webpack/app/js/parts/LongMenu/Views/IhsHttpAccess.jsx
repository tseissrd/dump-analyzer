import React from 'react';

export default function IhsHttpAccess({
    data = '',
    mode = 'normal',
    style,
    ...props}) {
  
  console.log(`mode is ${mode}`);
  
  console.log(data);
  
  const tableStyle = {
    border: 'thick solid black',
    borderCollapse: 'collapse'
  };
  
  const cellStyle = {
    border: 'thin solid black'
  };
  
  function timeMode(input) {
    const headers = ["время"];
    
    const rows = [];
    
    for (const record of input) {
      const time = Object.keys(record)[0];
      const rowData = {"время": time};
      
      const sources = record[time];
      
      for (const source of Object.keys(sources)) {
        const sourceCodes = sources[source];
        
        for (const code of Object.keys(sourceCodes)) {
          if (!headers.includes(code))
            headers.push(code);
          
          rowData[code] = sourceCodes[code];
        }
      }
      
      rows.push(rowData);
    }
    
    return {
      headers,
      rows
    };
  }
  
  function ipMode(input) {
    return {
      headers: [],
      rows: []
    };
  }
  
  let viewData = {
    headers: [],
    rows: []
  };
  
  if (data) {
    if (mode === 'time')
      viewData = timeMode(data);
    else if (mode === 'ip')
      viewData = ipMode(data);
    else
      viewData = timeMode(data);
  }
  
  const view = (<table style={tableStyle}>
    <thead>
      <tr>
        {
          viewData.headers
            .map((header, i) => (
              <th 
                key={i}
                style={cellStyle}
              >
                {header}
              </th>))
        }
      </tr>
    </thead>
    <tbody>
      {
        viewData.rows
          .map((row, i) => (<tr key={i}>
            {
              viewData.headers
                .map((header, i) => (<td key={i} style={cellStyle}>
                  {row[header]}
                </td>))
            }
          </tr>))
      }
    </tbody>
  </table>);
  
  console.log(viewData);
  
  return (<div style={style} {...props} >
    <div style={{padding: '4px'}}>{
      view
    }</div>
  </div>);
}