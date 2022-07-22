import React from 'react';

const optionStyle = {
  width: '100%',
  height: 'fit-content',
  marginBottom: '4px'
};

export default function Options({
    data = [],
    useContext = () => ({}),
    style,
    ...props
}) {
  
  const context = useContext();
  
  function constructRadio(definition, key) {
    return (<div
      key={key}
      style={optionStyle}
    >
      {
        definition.options
          .map((option, innerKey) => (
            <div key={innerKey}
              style={{
                height: '20px'
              }}
            >
              <div style={{
                float: 'left',
                marginLeft: '15px'
              }}>
                {option.title}
              </div>
              <div style={{
                width: 'fit-content',
                height: 'fit-content',
                marginLeft: 'auto',
                marginRight: '5px'
              }}>
                <input
                  key={innerKey}
                  type='radio'
                  name={definition.id}
                  onInput={() => option.action(context)}
                />
              </div>
            </div>)
          )
      }
    </div>);
  }
  
  function constructNumber(definition, key) {
    return (<div
      key={key}
      style={optionStyle}
    >
      <div style={{
        float: 'left',
        marginLeft: '15px'
      }}>
        {definition.title}
      </div>
      <div style={{
        width: 'fit-content',
        height: 'fit-content',
        marginLeft: 'auto',
        marginRight: '5px'
      }}>
        <input style={{
          width: '70px'
        }} placeholder={definition.title}
          onChange={(event) => definition.action(
            context,
            event.target
              .value
          )}
        />
      </div>
    </div>);
  }
  
  function constructOption(definition, key) {
    if (
      definition.isShown
      && !definition.isShown(context)
    )
      return '';
      
    
    if (definition.type === 'radio')
      return constructRadio(definition, key);
    else if (definition.type === 'number')
      return constructNumber(definition, key);
  }
    
  return (<div style={style} {...props} >
    {
      data.map((option, key) => constructOption(option, key))
    }
  </div>);
}