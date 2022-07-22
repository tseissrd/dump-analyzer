export default {
  'ihs_http_access': [
    {
      id: 'filters',
      type: 'radio',
      options: [
        {
          id: 'full',
          title: 'весь документ',
          action: (context) => {
            console.log('full');
            context.setOption('filter', 'full');
          }
        },
        {
          id: 'time',
          title: 'время',
          action: (context) => {
            context.setOption('filter', 'time');
          }
        },
        {
          id: 'lines',
          title: 'строки',
          action: (context) => {
            context.setOption('filter', 'lines');
          }
        },
        {
          id: 'percent',
          title: 'проценты',
          action: (context) => {
            context.setOption('filter', 'percent');
          }
        }
      ]
    },
    {
      id: 'from',
      title: 'от',
      type: 'number',
      isShown: context => {
        const relatedFilters = [
          'time',
          'lines',
          'percent'
        ];
        
        return relatedFilters.includes(
          context.getOption('filter')
        );
      },
      action: (context, value) => {
        console.log(value);
        console.log('from is ' + value)
        context.setOption('from', value);
      }
    },
    {
      id: 'to',
      title: 'до',
      type: 'number',
      isShown: context => {
        const relatedFilters = [
          'time',
          'lines',
          'percent'
        ];
        
        return relatedFilters.includes(
          context.getOption('filter')
        );
      },
      action: (context, value) => {
        console.log(value);
        console.log('to is ' + value)
        context.setOption('to', value);
      }
    }
  ]
}