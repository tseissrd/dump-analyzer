export default [
  {
    title: 'коды по адресу источника',
    action: (context) => {
      context.setMode("ip");
    }
  },
  {
    title: 'коды по времени',
    action: (context) => {
      context.setMode("time");
    }
  }
]