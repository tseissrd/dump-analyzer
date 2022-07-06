import React from 'react';

export default function Directory({
  title,
  data = [],
  folder,
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
    const fileInputEl = document.getElementById(`${fileInputUuid}-file`);
    fileInputEl.click();
  }
  
  async function uploadFile(file) {
    const data = new FormData();
    data.append('name', file.name);
    data.append('type', folder);
    data.append('file', file);
    
    const request = fetch('upload', {
      method: 'PUT',
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
    <form id={`${fileInputUuid}-form`} >
      <input
        id={`${fileInputUuid}-file`}
        style={{display: 'none'}}
        type='file'
        onChange={event => {
          uploadFiles(event.target.files)
          event.target
            .parentNode
            .reset();
        }}
      />
    </form>
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