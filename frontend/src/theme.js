/**
 * Naive UI 主題覆寫 - 你儂我農 配色
 * 主色：農場綠 #4a7c2a
 * 輔色：土黃 #c89b3c
 */
export const themeOverrides = {
  common: {
    primaryColor: '#4a7c2a',
    primaryColorHover: '#5a9438',
    primaryColorPressed: '#3d6b22',
    primaryColorSuppl: '#5a9438',

    successColor: '#4a7c2a',
    successColorHover: '#5a9438',

    infoColor: '#c89b3c',
    infoColorHover: '#e0b75c',
    infoColorPressed: '#9b7826',

    warningColor: '#e0b75c',
    errorColor: '#c1502e',

    bodyColor: '#fafaf7',
    fontFamily: '"Noto Sans TC", system-ui, "Microsoft JhengHei", sans-serif',

    borderRadius: '6px',
  },
  Button: {
    fontWeight: '500',
  },
  Card: {
    borderRadius: '10px',
  },
}
