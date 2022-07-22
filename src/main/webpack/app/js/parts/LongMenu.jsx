import React, {useState} from 'react';
import Tabs from './LongMenu/Tabs.jsx';
import Options from './LongMenu/Options.jsx';
import View from './LongMenu/View.jsx';
import tabsData from '../../data/LongMenu/tabs.js';
import optionsData from '../../data/LongMenu/filters.js';

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
    height: '112px',
    display: 'block',
    float: 'left'
  };
  
  const optionsStyle = {
    width: '200px',
    height: '112px',
    display: 'block',
    overflowY: 'scroll',
    marginLeft: 'auto',
    border: 'thin solid black'
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
    getValue,
    chosenTab
  } = useContext();
  
  function setMode(mode) {
    setValue('mode', mode);
  }
  
  function setOption(option, value) {
    setValue(option, value);
  }
  
  function getOption(option) {
    return getValue(option);
  }
  
  return (<div style={style} {...props} >
      <div style={{
        padding: '4px'
      }}>
        <div style={{
          height: 'fit-content'
        }}>
          <h3>{title}</h3>
          <div style={{
            height: '112px',
            width: '100%'
          }}>
            <Tabs
              data={tabsData[data.type]}
              chosen={chosenTab}
              style={tabsStyle}
              useContext={() => ({setMode})}
            />
            <Options 
              data={optionsData[data.type]}
              style={optionsStyle}
              useContext={() => ({
                setOption,
                getOption
              })}
            />
          </div>
        </div>
        <View
          data={data}
          style={viewStyle}
        />
      </div>
    </div>);
}