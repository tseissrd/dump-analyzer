import React from 'react';
import Tabs from './LongMenu/Tabs.jsx';
import View from './LongMenu/View.jsx';

export default function LongMenu({
    title,
    data = {
      type: 'none'
    },
    useContext = () => ({}),
    style,
    ...props}) {
    
  const {setOption} = useContext();
    
  const onClick = value => {
    setOption(value);
  };
  
  function constructTabsData() {
    return [
      {title: 'tab1', action: () => console.log('test')},
      {title: 'tab2', action: () => console.log('test 2')}
    ];
  }
  
  const tabsStyle = {
    width: '100%',
    height: '100px'
  };
  
  return (<div style={style} {...props} >
      <div style={{
        padding: '4px'
      }}>
        <h3>{title}</h3>
        <Tabs data={constructTabsData()} style={tabsStyle} />
        <View data={data} />
      </div>
    </div>);
}