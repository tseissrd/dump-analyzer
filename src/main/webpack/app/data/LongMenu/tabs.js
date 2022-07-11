export default [
  {
    id: 'ip',
    title: 'коды по адресу источника',
    action: (context) => {
      context.setMode("ip");
    }
  },
  {
    id: 'time',
    title: 'коды по времени',
    action: (context) => {
      context.setMode("time");
    }
  },
  {
    id: 'text',
    title: 'текст',
    action: (context) => {
      context.setMode("text");
    }
  }
]