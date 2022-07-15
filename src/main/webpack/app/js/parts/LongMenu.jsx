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
    marginTop: '10px',
    height: '600px',
    width: '800px',
    border: 'thin solid black',
    overflow: 'scroll',
    resize: 'both'
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
        <div style={{
          height: 'fit-content'
        }}>
          <h3>{title}</h3>
          <Tabs
            data={tabsData[data.type]}
            chosen={chosenTab}
            style={tabsStyle}
            useContext={() => ({setMode})}
          />
        </div>
        <View
          data={data}
          style={viewStyle}
        />
      </div>
    </div>);
}