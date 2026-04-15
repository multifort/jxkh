/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#E3F2FD',
          100: '#BBDEFB',
          200: '#90CAF9',
          300: '#64B5F6',
          400: '#42A5F5',
          500: '#2196F3',
          600: '#1E88E5',
          700: '#1976D2',
          800: '#1565C0',
          900: '#0D47A1',
        },
        success: '#52c41a',
        warning: '#fa8c16',
        error: '#f5222d',
        info: '#1677ff',
      },
      spacing: {
        '1_5': '0.375rem',
        '2_5': '0.625rem',
        '3_5': '0.875rem',
        '7': '1.75rem',
        '9': '2.25rem',
        '11': '2.75rem',
        '14': '3.5rem',
      },
      borderRadius: {
        'default': '6px',
        'md': '8px',
      },
    },
  },
  plugins: [],
}
