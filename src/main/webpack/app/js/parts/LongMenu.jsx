import React, {useState} from 'react';
import Tabs from './LongMenu/Tabs.jsx';
import View from './LongMenu/View.jsx';
import tabsData from '../../data/LongMenu/tabs.js';

export default function LongMenu({
  title,
  data = {
    type: 'ihs_http_access',
    mode: 'text',
    data: null
  },
  useContext = () => ({}),
  style,
  ...props
}) {
  
  const tabsStyle = {
    width: '100%',
    height: '110px'
  };
  
  const viewStyle = {
    marginTop: '10px'
  };
  
  const {
    setValue,
    chosenTab
  } = useContext();
  
  function setMode(mode) {
    setValue('mode', mode);
  }
  
  return (<div style={style} {...props} >
      <div style={{
        padding: '4px'
      }}>
        <h3>{title}</h3>
        <Tabs
          data={tabsData}
          chosen={chosenTab}
          style={tabsStyle}
          useContext={() => ({setMode})}
        />
        <View
          data={data}
          style={viewStyle}
        />
      </div>
    </div>);
}