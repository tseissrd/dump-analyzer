import React from 'react';
import IhsHttpAccess from './Views/IhsHttpAccess.jsx';
import Text from './Views/Text.jsx';

export default function View({
    data = {
      data: '',
      type: 'text'
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
  
  return (<ViewType data={data.data} style={style} {...props} />);
}