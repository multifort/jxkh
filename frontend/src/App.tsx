import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/auth/LoginPage';
import OrgTreePage from './pages/settings/OrgTreePage';
import RoleManagePage from './pages/settings/RoleManagePage';

// 简单的路由保护组件
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const token = localStorage.getItem('accessToken');
  
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
};

// 首页组件（临时）
const HomePage: React.FC = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  
  return (
    <div style={{ padding: 24 }}>
      <h1>欢迎，{user.realName || user.name || '用户'}</h1>
      <p>用户名：{user.username}</p>
      <p>角色：{user.roles?.map((r: any) => r.name).join(', ') || '未分配'}</p>
      <button onClick={() => {
        localStorage.clear();
        window.location.href = '/login';
      }}>退出登录</button>
    </div>
  );
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings/org-tree"
          element={
            <ProtectedRoute>
              <OrgTreePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings/roles"
          element={
            <ProtectedRoute>
              <RoleManagePage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
