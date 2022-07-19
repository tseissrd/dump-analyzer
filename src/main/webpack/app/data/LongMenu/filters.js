export default {
  'ihs_http_access': [
    {
      id: 'full',
      title: 'весь документ',
      action: (context) => {
        context.setMode("full");
      }
    },
    {
      id: 'time',
      title: 'время',
      action: (context) => {
        context.setMode("time");
      }
    },
    {
      id: 'lines',
      title: 'строки',
      action: (context) => {
        context.setMode("lines");
      }
    },
    {
      id: 'percent',
      title: 'проценты',
      action: (context) => {
        context.setMode("percent");
      }
    }
  ]
}