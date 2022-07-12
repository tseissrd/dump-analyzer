import React from 'react';

export default function IhsHttpAccess({
    data = '',
    mode = 'normal',
    style,
    ...props}) {
  
  const tableStyle = {
    border: 'thick solid black',
    borderCollapse: 'collapse'
  };
  
  const cellStyle = {
    border: 'thin solid black',
    textAlign: 'center'
  };
  
  function timeMode(input) {
    return input;
  }
  
  function ipMode(input) {
    return input;
  }
  
  function translate(string) {
    const translations = {
      'time': 'время',
      'source': 'источник',
      'total': 'всего'
    };
    
    if (translations[string])
      return translations[string];
    else
      return string;
  }
  
  let viewData = {
    headers: [],
    rows: []
  };
  
  let view;
  
  if (data) {
    try {
      if (mode === 'time') {
        viewData = timeMode(
          JSON.parse(data)
        );
        view = constructTable(viewData);
      } else if (mode === 'ip') {
        viewData = ipMode(
          JSON.parse(data)
        );
        view = constructTable(viewData);
      } else
        view = data;
    } catch (ex) {
      view = data;
    }
  }
  
  function constructTable(data) {
    return (<table style={tableStyle}>
      <thead>
        <tr>
          {
            data.headers
              .map((header, i) => (
                <th 
                  key={i}
                  style={cellStyle}
                >
                  {translate(header)}
                </th>))
          }
        </tr>
      </thead>
      <tbody>
        {
          data.rows
            .map((row, i) => (<tr key={i}>
              {
                data.headers
                  .map((header, i) => (
                  <td
                    key={i}
                    style={cellStyle}
                  >
                    {row[header]}
                  </td>))
              }
            </tr>))
        }
      </tbody>
    </table>);
  }
  
  return (<div style={style} {...props} >
    <div style={{
      padding: '4px',
      whiteSpace: 'pre-wrap'
    }}>{
      view
    }</div>
  </div>);
}