import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/auth/LoginPage';
import MainLayout from './components/layout/MainLayout';
import CycleManagePage from './pages/performance/CycleManagePage';
import IndicatorManagePage from './pages/performance/IndicatorManagePage';
import WeightSchemeManagePage from './pages/performance/WeightSchemeManagePage';
import PlanCreatePage from './pages/performance/PlanCreatePage';
import PlanListPage from './pages/performance/PlanListPage';
import PlanDetailPage from './pages/performance/PlanDetailPage';
import ProgressTrackingPage from './pages/performance/ProgressTrackingPage';
import NotificationCenterPage from './pages/notification/NotificationCenterPage';
import OrgManagePage from './pages/settings/OrgManagePage';
import RoleManagePage from './pages/settings/RoleManagePage';
import UserManagePage from './pages/settings/UserManagePage';
import PermissionManagePage from './pages/settings/PermissionManagePage';

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
              <MainLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<HomePage />} />
          <Route path="performance/cycles" element={<CycleManagePage />} />
          <Route path="performance/indicators" element={<IndicatorManagePage />} />
          <Route path="performance/weight-schemes" element={<WeightSchemeManagePage />} />
          <Route path="performance/plans" element={<PlanListPage />} />
          <Route path="performance/plans/:id" element={<PlanDetailPage />} />
          <Route path="performance/plans/create" element={<PlanCreatePage />} />
          <Route path="performance/tracking/:planId" element={<ProgressTrackingPage />} />
          <Route path="notifications" element={<NotificationCenterPage />} />
          <Route path="settings/org-manage" element={<OrgManagePage />} />
          <Route path="settings/user-manage" element={<UserManagePage />} />
          <Route path="settings/roles" element={<RoleManagePage />} />
          <Route path="settings/permissions" element={<PermissionManagePage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
