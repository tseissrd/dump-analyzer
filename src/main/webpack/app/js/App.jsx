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
    file: null,
    mode: 'default'
  });
  
  const [files, setFiles] = useState([]);
  
  const [viewData, setViewData] = useState('');
  
  async function updateDirectory(option) {
    const response = await fetch('list', {
      method: 'POST',
      body: option? option : settings.type
    });
    
    const fileList = await response.json();
    
    if (!fileList.includes(settings.file))
      chooseFile(null);
    
    setFiles(fileList);
  }
  
  async function updateView(targetSettings) {
    const usedSettings = targetSettings?
      targetSettings
      : settings;
    
    if (!(usedSettings.file)) {
      return;
    }
    
    const response = await fetch('view', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(
        usedSettings
      )
    });
    
    let data = null;
    
    try {
      data = await response.text();
    } catch (ex) {}
        
    setViewData(data);
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
    
    if (
      setting === 'mode'
      &&  newSettings['mode'] !== settings['mode']
    )
      updateView(newSettings);
    
    setSettings(newSettings);
  }
  
  const useSettings = () => ({setOption, setValue});
  
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
    for (const file of files) {
      await uploadFile(file);
    }
    
    updateDirectory();
  }
  
  async function deleteFile() {
    if (!settings.file || !settings.type)
      return;
    
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
    
    chooseFile(null);
    updateDirectory();
  }
  
  const blockStyle = {
      display: 'inline-block',
      width: '300px',
      height: '300px',
      overflowY: 'auto',
      border: 'thick double black'
    };
    
  const longMenuStyle = {
      display: 'block',
      width: 'fit-content',
      height: 'fit-content',
      border: 'thick double black'
    };
  
  const viewSettings = {
    data: viewData,
    ...settings
  };
  
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
      <LongMenu
        data={viewSettings}
        style={longMenuStyle}
        title='просмотр'
        useContext={() => ({
          chosenTab: settings.mode,
          ...useSettings()
        })}
      />
    </div>);
}
