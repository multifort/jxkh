import React, { useEffect, useState } from 'react';
import { Layout, Menu, theme } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  DashboardOutlined,
  TeamOutlined,
  UserOutlined,
  SecurityScanOutlined,
  KeyOutlined,
  LogoutOutlined,
  CalendarOutlined,
  AppstoreOutlined,
  SettingOutlined,
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

  // 定义父级菜单及其子路径映射
  const parentMap: Record<string, string[]> = {
    '/performance': ['/performance/cycles', '/performance/indicators', '/performance/weight-schemes'],
    '/settings': ['/settings/org-manage', '/settings/user-manage', '/settings/roles', '/settings/permissions'],
  };

  // 获取当前路径对应的父级菜单key
  const getDefaultOpenKeys = (pathname: string): string[] => {
    const parentKey = Object.keys(parentMap).find(parent => 
      parentMap[parent].includes(pathname)
    );
    return parentKey ? [parentKey] : [];
  };

  // 使用函数式初始化，确保首次渲染时就有正确的openKeys
  const [openKeys, setOpenKeys] = useState<string[]>(() => getDefaultOpenKeys(location.pathname));

  // 监听路由变化，更新展开状态
  useEffect(() => {
    const newOpenKeys = getDefaultOpenKeys(location.pathname);
    setOpenKeys(newOpenKeys);
  }, [location.pathname]);

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
      key: '/performance',
      icon: <CalendarOutlined />,
      label: '绩效管理',
      children: [
        {
          key: '/performance/cycles',
          icon: <CalendarOutlined />,
          label: '周期管理',
        },
        {
          key: '/performance/indicators',
          icon: <AppstoreOutlined />,
          label: '指标库',
        },
        {
          key: '/performance/weight-schemes',
          icon: <SettingOutlined />,
          label: '权重配置',
        },
      ],
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
          key: '/settings/user-manage',
          icon: <UserOutlined />,
          label: '用户管理',
        },
        {
          key: '/settings/roles',
          icon: <SecurityScanOutlined />,
          label: '角色管理',
        },
        {
          key: '/settings/permissions',
          icon: <KeyOutlined />,
          label: '权限管理',
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
          openKeys={openKeys}
          onOpenChange={setOpenKeys}
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
