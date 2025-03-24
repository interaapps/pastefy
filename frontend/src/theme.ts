import type { Preset } from '@primeuix/themes/types'

export default {
  semantic: {
    primary: {
      50: '#EBF0FF',
      100: '#D6E1FF',
      200: '#ADC3FF',
      300: '#85A5FF',
      400: '#5C87FF',
      500: '#3469FF',
      600: '#0041F5',
      700: '#0031B8',
      800: '#00217A',
      900: '#00103D',
      950: '#00081F',
    },
  },
  primitive: {
    colorScheme: {
      light: {
        surface: {
          0: '#ffffff',
          50: '{zinc.50}',
          100: '{zinc.100}',
          200: '{zinc.200}',
          300: '{zinc.300}',
          400: '{zinc.400}',
          500: '{zinc.500}',
          600: '{zinc.600}',
          700: '{zinc.700}',
          800: '{zinc.800}',
          900: '{zinc.900}',
          950: '{zinc.950}',
        },
      },
      dark: {
        surface: {
          0: '#ffffff',
          50: '{slate.50}',
          100: '{slate.100}',
          200: '{slate.200}',
          300: '{slate.300}',
          400: '{slate.400}',
          500: '{slate.500}',
          600: '{slate.600}',
          700: '{slate.700}',
          800: '{slate.800}',
          900: '{slate.900}',
          950: '{slate.950}',
        },
      },
    },
  },
  components: {
    inputtext: {
      colorScheme: {
        light: {
          shadow: 'none',
          background: 'transparent',
          border: {
            color: '{neutral.400}',
          },
        },
      },
    },
    select: {
      colorScheme: {
        light: {
          shadow: 'none',
          background: 'transparent',
          border: {
            color: '{neutral.400}',
          },
        },
      },
    },
    treeselect: {
      colorScheme: {
        light: {
          shadow: 'none',
          background: 'transparent',
          border: {
            color: '{neutral.400}',
          },
        },
      },
    },
  },
} as Preset
