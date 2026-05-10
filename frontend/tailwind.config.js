/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  // 關閉 reset 避免覆蓋 Naive UI 的 base styles
  corePlugins: { preflight: false },
  theme: {
    extend: {
      colors: {
        // 農場綠
        farm: {
          50:  '#f3f8ed',
          100: '#e3efd2',
          200: '#c8de9f',
          300: '#a8c96b',
          400: '#7caa53',
          DEFAULT: '#4a7c2a',
          600: '#3d6b22',
          700: '#30541a',
          800: '#243f13',
          900: '#1a2e0c',
        },
        // 土黃
        earth: {
          50:  '#fbf6e8',
          100: '#f5ebc6',
          200: '#ecd58c',
          300: '#e0b75c',
          DEFAULT: '#c89b3c',
          600: '#9b7826',
          700: '#735819',
          800: '#4d3a10',
          900: '#2c2008',
        },
      },
      fontFamily: {
        sans: ['"Noto Sans TC"', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
