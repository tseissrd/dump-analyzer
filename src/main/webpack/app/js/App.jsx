import React, {useState, useEffect} from 'react';
import Settings from './parts/Settings.jsx';
import Menu from './parts/Menu.jsx';
import LongMenu from './parts/LongMenu.jsx';
import Directory from './parts/Directory.jsx';
import Status from './parts/Status.jsx';
import settingsData from '../data/settings/entries.json';
import menuData from '../data/menu/entries.json';

export default function App(props) {
  const [status, setStatus] = useState({});
  const [settings, setSettings] = useState({
    type: 'ihs_http_access',
    file: ''
  });
  
  const [files, setFiles] = useState([]);
  
  const [viewData, setViewData] = useState({
    type: 'ihs_http_access',
    data: '',
    mode: 'normal'
  });
  
  async function updateDirectory(option) {
    console.log(settings.type);
    console.log(settings);
    
    const response = await fetch('list', {
      method: 'POST',
      body: option? option : settings.type
    });
    
    setFiles(await response.json());
  }
  
  async function updateView(targetSettings) {
    const usedSettings = targetSettings?
      targetSettings
      : settings;
    
    const response = await fetch('view', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(
        usedSettings
      )
    });
    
    setViewData({
      type: usedSettings.type,
      data: await response.json()
    });
  }
  
  function setOption(option) {
    const newSettings = {
        ...settings
      };
    newSettings['type'] = option;
    updateDirectory(option);
    
    setSettings(newSettings);
  }
  
  function setValue(setting, value) {
    const newSettings = {
        ...settings
      };
    newSettings[setting] = value;
    
    if (
      setting === 'file'
      &&  newSettings['file'] !== settings['file']
    )
      updateView(newSettings);
    
    setSettings(newSettings);
  }
  
  const useSettings = () => ({setOption, setValue});
  
  async function updateStatus() {
//    const controller = new AbortController();
//    let response;
//    let timeout;
//    
//    try {
//      timeout = setTimeout(controller.abort, 5000);
//
//      response = await fetch('status', {signal: controller.signal});
//      clearTimeout(timeout);
//    } catch (ex) {
//      clearTimeout(timeout);
//      return;
//    }
//    const data = await response.json();
//    setStatus(data);
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
    
    return processedStatus
  }
  
  function chooseFile(file) {
    setValue(
      "file",
      file
    );
  }
  
  async function uploadFile(file) {
    const data = new FormData();
    data.append('name', file.name);
    data.append('type', settings.type);
    data.append('file', file);
    
    const request = await fetch('upload', {
      method: 'PUT',
      body: data
    });
  }
  
  async function uploadFiles(files) {
    console.log(files);
    for (const file of files) {
      await uploadFile(file);
    }
    
    updateDirectory();
  }
  
  async function deleteFile() {
    const request = await fetch('delete', {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        file: settings.file,
        type: settings.type
      })
    });
    
    updateDirectory();
  }
  
  const blockStyle = {
      // float: 'left',
      display: 'inline-block',
      width: '300px',
      height: '300px',
      overflowY: 'auto',
      border: 'thick double black'
    };
    
  const longMenuStyle = {
      display: 'block',
      resize: 'both',
      width: '1200px',
      height: '800px',
      border: 'thick double black',
      overflow: 'scroll'
    };
  
  useEffect(() => setInterval(updateStatus, 2000), []);
  
  // <Settings style={blockStyle} data={settingsData} useContext={useSettings} />
  // <Status style={blockStyle} onClick={() => updateStatus()} title='статус приложения' data={processStatus(status)} />
  
  return (
    <div style={{
      display: 'contents'
    }}>
      <Menu style={blockStyle} data={menuData} useContext={useSettings} />
      <Directory
        title="файлы"
        chosen={settings.file}
        style={blockStyle}
        data={files}
        useContext={useSettings}
        onChoice={chooseFile}
        onUpload={uploadFiles}
        onDelete={deleteFile} /> 
      <LongMenu data={viewData} style={longMenuStyle} title='просмотр' />
    </div>);
}
