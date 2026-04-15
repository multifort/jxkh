// 品牌色
export const primary = {
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
}

// 功能色（Ant Design 规范）
export const functional = {
  success: '#52c41a',
  warning: '#fa8c16',
  error: '#f5222d',
  info: '#1677ff',
}

// 中性色
export const gray = {
  50: '#F5F7FA',
  100: '#F5F5F5',
  200: '#EBEEF5',
  300: '#D9D9D9',
  400: '#A0AEC0',
  500: '#8A8F98',
  600: '#606266',
  700: '#404040',
  800: '#333333',
  900: '#000000',
}

// 语义色
export const semantic = {
  white: '#FFFFFF',
  black: '#000000',
}

// 导出所有色值
export const colors = {
  primary,
  functional,
  gray,
  semantic,
  ...primary,
  ...functional,
  ...gray,
  ...semantic,
}

export default colors
