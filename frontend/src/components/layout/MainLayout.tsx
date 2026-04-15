import React from 'react';
import { Layout, Menu, theme } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  DashboardOutlined,
  TeamOutlined,
  SecurityScanOutlined,
  LogoutOutlined,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';

const { Header, Sider, Content } = Layout;

const MainLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const {
    token: { colorBgContainer },
  } = theme.useToken();

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    navigate(e.key);
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const menuItems: MenuProps['items'] = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: '首页',
    },
    {
      key: '/settings',
      icon: <TeamOutlined />,
      label: '系统设置',
      children: [
        {
          key: '/settings/org-manage',
          icon: <TeamOutlined />,
          label: '组织架构',
        },
        {
          key: '/settings/roles',
          icon: <SecurityScanOutlined />,
          label: '角色管理',
        },
      ],
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible>
        <div
          style={{
            height: 32,
            margin: 16,
            background: 'rgba(255, 255, 255, 0.2)',
            borderRadius: 6,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontWeight: 'bold',
          }}
        >
          JXKH 系统
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={['/settings']}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: '0 24px',
            background: colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <div style={{ fontSize: 18, fontWeight: 500 }}>
            绩效管理系统
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <span>欢迎，{user.realName || user.username || '用户'}</span>
            <LogoutOutlined
              style={{ cursor: 'pointer', fontSize: 18 }}
              onClick={handleLogout}
              title="退出登录"
            />
          </div>
        </Header>
        <Content style={{ margin: '16px' }}>
          <div
            style={{
              padding: 24,
              minHeight: 360,
              background: colorBgContainer,
              borderRadius: 8,
            }}
          >
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
