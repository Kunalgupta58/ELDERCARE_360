/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      boxShadow: {
        soft: '0 18px 45px rgba(15, 23, 42, 0.08)',
      },
      colors: {
        brand: {
          50: '#eefcf8',
          100: '#d8f6ef',
          500: '#1ba98a',
          600: '#168b73',
          700: '#136f5d',
          900: '#0f3c35',
        },
      },
    },
  },
  plugins: [],
}
