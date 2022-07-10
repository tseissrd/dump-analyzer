import React, {useState} from 'react';
import Tabs from './LongMenu/Tabs.jsx';
import View from './LongMenu/View.jsx';
import tabsData from '../../data/LongMenu/tabs.js';

export default function LongMenu({
    title,
    data = {
      type: 'none'
    },
    useContext = () => ({}),
    style,
    ...props}) {
    
  // const {setOption} = useContext();
  
  const tabsStyle = {
    width: '100%',
    height: '110px'
  };
  
  const viewStyle = {
    marginTop: '10px'
  };
  
  const [mode, setMode] = useState("default");
  
  const viewData = {
    mode,
    ...data
  };
  
  return (<div style={style} {...props} >
      <div style={{
        padding: '4px'
      }}>
        <h3>{title}</h3>
        <Tabs
          data={tabsData}
          style={tabsStyle}
          useContext={() => ({setMode})}
        />
        <View
          data={viewData}
          style={viewStyle}
          mode={mode}
        />
      </div>
    </div>);
}