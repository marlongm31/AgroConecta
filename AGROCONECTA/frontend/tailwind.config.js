/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        agro: {
          50: '#f2f9f1',
          100: '#e0f0dd',
          200: '#c2e1bd',
          300: '#93c98c',
          400: '#5faa56',
          500: '#3d8b34',
          600: '#2d6e26',
          700: '#25581f',
          800: '#20461d',
          900: '#1b3a1a',
          950: '#0c200c',
        },
        earth: {
          50: '#faf6f1',
          100: '#f2e8da',
          200: '#e5d0b4',
          300: '#d3b088',
          400: '#c08d5c',
          500: '#a97040',
          600: '#8a5734',
          700: '#6f442d',
          800: '#5c3929',
          900: '#4d3124',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'Avenir', 'Helvetica', 'Arial', 'sans-serif'],
      },
      boxShadow: {
        card: '0 1px 3px 0 rgba(16, 24, 40, 0.08), 0 1px 2px -1px rgba(16, 24, 40, 0.06)',
        'card-hover': '0 8px 24px -6px rgba(16, 24, 40, 0.16)',
      },
    },
  },
  plugins: [],
};
