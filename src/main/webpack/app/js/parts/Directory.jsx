import React from 'react';

export default function Directory({
  title,
  data = [],
  chosen,
  useContext = () => ({}),
  style,
  ...props}) {
    
  const {setValue} = useContext();
  
  const onClick = (value) => {
    setValue(
      "file",
      value
    );
  };
  
  const fileInputUuid = crypto.randomUUID();
  
  function chooseFile() {
    const fileInputEl = document.getElementById(fileInputUuid);
    fileInputEl.click();
  }
  
  async function uploadFile(file) {
    const data = new FormData();
    data.append('name', file.name);
    data.append('file', file);
    
    await fetch('upload', {
      method: 'POST',
      body: data
    });
  }
  
  async function uploadFiles(files) {
    console.log(files);
    for (const file of files) {
      await uploadFile(file);
    }
  }
  
  return (<div style={style} {...props} >
    <input
      id={fileInputUuid}
      style={{display: 'none'}}
      type='file'
      onChange={event => uploadFile(event.target.files)}
    />
    <div style={{padding: '4px'}}>
      <div>
        <h3 style={{
          width: '100px',
          display: 'inline-block'
        }}>{title}</h3>
        <button style={{
          width: '80px',
          height: '40px'
        }}
        onClick={() => {
          chooseFile();
          console.log('file chosen!');
        }} >загрузить</button>
        <button style={{
          marginLeft: '20px',
          width: '80px',
          height: '40px'
        }}
        onClick={() => {
          console.log('delete');
        }} >удалить</button>
      </div>
      {data.map((file, num) => <div style={{
        width: '100%',
        height: '20px',
        border: 'thin solid black'
      }} key={num} >
        <button style={{
          width: '100%',
          height: '20px',
          backgroundColor: chosen === file?
            'gold'
            : 'white'
        }} onClick={() => onClick(file)}>
          {file}
        </button>
      </div>)}
    </div>
  </div>);
}