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
      id: 'filterFrom',
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
        context.setOption('filterFrom', value);
      }
    },
    {
      id: 'filterTo',
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
        context.setOption('filterTo', value);
      }
    },
    {
      id: 'applyFilter',
      title: 'применить',
      type: 'button',
      isShown: () => true,
      action: (context) => {
        context.apply();
      }
    }
  ]
}