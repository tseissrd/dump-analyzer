import React from 'react';
import IhsHttpAccess from './Views/IhsHttpAccess.jsx';
import Text from './Views/Text.jsx';

export default function View({
    data = {
      data: null,
      type: 'text',
      mode: 'normal'
    },
    style,
    ...props}) {
  
  function resolveType(type) {
    if (type === 'ihs_http_access')
      return IhsHttpAccess;
    else
      return Text;
  }
  
  const ViewType = resolveType(data.type);
    
  return (<ViewType
    data={data.data}
    mode={data.mode}
    style={style}
    {...props}
  />);
}