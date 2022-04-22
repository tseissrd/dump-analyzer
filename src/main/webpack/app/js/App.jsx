import React, {useState, useEffect} from 'react';
import Settings from './parts/Settings.jsx';
import Menu from './parts/Menu.jsx';
import Status from './parts/Status.jsx';
import settingsData from '../data/settings/entries.json';
import menuData from '../data/menu/entries.json';

export default function App(props) {
  const [status, setStatus] = useState({});
  const [settings, setSettings] = useState({
    strategy: 'PRIME',
    threadsNumber: 0
  });
  
  function setOption(option) {
    const newSettings = {
        ...settings
      };
    newSettings['strategy'] = option;
    
    setSettings(newSettings);
  }
  
  function setValue(setting, value) {
    const newSettings = {
        ...settings
      };
    newSettings[setting] = value;
    
    setSettings(newSettings);
  }
  
  const useSettings = () => ({setOption, setValue});
  
  async function updateStatus() {
    const controller = new AbortController();
    let response;
    let timeout;
    
    try {
      timeout = setTimeout(controller.abort, 5000);

      response = await fetch('status', {signal: controller.signal});
      clearTimeout(timeout);
    } catch (ex) {
      clearTimeout(timeout);
      return;
    }
    const data = await response.json();
    setStatus(data);
  }
  
  async function sendControl(data) {
    const control = {...settings};
    
    if (control['threadsNumber'])
      control['threadsNumber'] = parseInt(control['threadsNumber'])
    else
      control['threadsNumber'] = 0;
    
    const response = await fetch('go', {
      method: 'POST',
      headers: {
        'content-type': 'application/json',
      },
      body: JSON.stringify(control)
    });
  }
  
  function processStatus(status) {
    const processedStatus = {...status};
    
    const strategyToString = {
      PRIME: 'генерация простых чисел',
      ARRAY_COPY: 'копирование массивов',
      CRASH_HEAP: 'заполнение кучи',
      DEAD_LOCK: 'блокировка тредов'
    };
    
    if (!processedStatus.threads)
      processedStatus.threads = 0;
    
    if (processedStatus.strategy)
      processedStatus.strategy = strategyToString[processedStatus.strategy];
    
    return processedStatus
  }
  
  const blockStyle = {
      float: 'left',
      width: '300px',
      height: '300px',
      overflowY: 'auto',
      border: 'thick double black'
    };
  
  useEffect(() => setInterval(updateStatus, 2000), []);
  
  return (
    <div style={{
      display: 'contents'
    }}>
      <Settings style={blockStyle} data={settingsData} useContext={useSettings} />
      <Menu style={blockStyle} data={menuData} useContext={useSettings} />
      <Status style={blockStyle} title='настройки' data={processStatus(settings)} applyAction={() => sendControl(settings)} />
      <Status style={blockStyle} onClick={() => updateStatus()} title='статус приложения' data={processStatus(status)} />
    </div>);
}
