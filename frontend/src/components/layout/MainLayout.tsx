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

  // 路径到页面标题的映射
  const pageTitleMap: Record<string, string> = {
    '/': '首页',
    '/performance/cycles': '周期管理',
    '/performance/indicators': '指标库',
    '/performance/weight-schemes': '权重配置',
    '/performance/plans': '绩效计划',
    '/settings/org-manage': '组织管理',
    '/settings/user-manage': '用户管理',
    '/settings/roles': '角色管理',
    '/settings/permissions': '权限管理',
  };

  // 根据当前路径获取页面标题
  const getPageTitle = () => {
    return pageTitleMap[location.pathname] || '绩效管理系统';
  };

  // 定义父级菜单及其子路径映射
  const parentMap: Record<string, string[]> = {
    '/performance': ['/performance/cycles', '/performance/indicators', '/performance/weight-schemes', '/performance/plans'],
    '/settings': ['/settings/org-manage', '/settings/user-manage', '/settings/roles', '/settings/permissions'],
  };

  // 获取当前路径对应的父级菜单key
  const getDefaultOpenKeys = (pathname: string): string[] => {
    // 精确匹配
    const parentKey = Object.keys(parentMap).find(parent => 
      parentMap[parent].includes(pathname)
    );
    
    // 如果是子路由（如 /performance/plans/create），查找父级
    if (!parentKey) {
      const parentKey2 = Object.keys(parentMap).find(parent => 
        pathname.startsWith(parent + '/')
      );
      return parentKey2 ? [parentKey2] : [];
    }
    
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

  // 获取当前应该选中的菜单项
  const getSelectedKey = (pathname: string): string => {
    // 精确匹配菜单项
    const allMenuKeys = [
      '/',
      '/performance/cycles',
      '/performance/indicators',
      '/performance/weight-schemes',
      '/performance/plans',
      '/settings/org-manage',
      '/settings/user-manage',
      '/settings/roles',
      '/settings/permissions',
    ];
    
    if (allMenuKeys.includes(pathname)) {
      return pathname;
    }
    
    // 子路由（如 /performance/plans/create）匹配到父级菜单（/performance/plans）
    const parentMatch = allMenuKeys.find(key => 
      pathname.startsWith(key + '/')
    );
    
    return parentMatch || pathname;
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
        {
          key: '/performance/plans',
          icon: <AppstoreOutlined />,
          label: '计划列表',
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
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: 18,
            fontWeight: 'bold',
          }}
        >
          绩效管理系统
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[getSelectedKey(location.pathname)]}
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
            {getPageTitle()}
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
