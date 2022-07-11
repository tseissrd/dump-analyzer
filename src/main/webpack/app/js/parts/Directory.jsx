import React from 'react';

export default function Directory({
  title,
  data = [],
  chosen,
  useContext = () => ({}),
  onChoice = () => ({}),
  onUpload = () => ({}),
  onDelete = () => ({}),
  style,
  ...props
}) {
    
  const {setValue} = useContext();
  
  const fileInputUuid = crypto.randomUUID();
  
  function chooseFile() {
    const fileInputEl = document.getElementById(`${fileInputUuid}-file`);
    fileInputEl.click();
  }
  
  return (<div style={style} {...props} >
    <form id={`${fileInputUuid}-form`} >
      <input
        id={`${fileInputUuid}-file`}
        style={{display: 'none'}}
        type='file'
        onChange={event => {
          onUpload(event.target.files)
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
        }} >загрузить</button>
        <button style={{
          marginLeft: '20px',
          width: '65px',
          height: '30px'
        }}
        onClick={() => {
          onDelete();
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
        }} onClick={() => onChoice(file)}>
          {file}
        </button>
      </div>)}
    </div>
  </div>);
}