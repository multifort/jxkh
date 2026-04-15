import React, { useEffect, useState, useRef } from 'react';
import { 
  Card, Table, Button, Modal, Form, Input, Select, message, Space, 
  Popconfirm, Tag, Tooltip 
} from 'antd';
import { 
  PlusOutlined, EditOutlined, DeleteOutlined, LockOutlined, 
  UnlockOutlined, ReloadOutlined, KeyOutlined 
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { userService, roleService, orgService } from '../../services/rbacService';
import type { User, Role, Org } from '../../types/auth';

const { Option } = Select;

const UserManagePage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [orgs, setOrgs] = useState<Org[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [passwordModalVisible, setPasswordModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [form] = Form.useForm();
  const [passwordForm] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [searchParams, setSearchParams] = useState<{
    keyword?: string;
    orgId?: number;
    role?: string;
  }>({});
  
  // 防止 StrictMode 下的重复请求
  const isMounted = useRef(true);

  // 加载用户列表
  const loadUsers = async (page = 1, size = 10) => {
    if (!isMounted.current) return; // 如果组件已卸载，不执行请求
    
    try {
      setLoading(true);
      const data = await userService.getUsers({
        page: page - 1,
        size,
        ...searchParams,
      });
      console.log('用户列表数据:', data);
      if (isMounted.current) { // 再次检查，防止竞态条件
        setUsers(data.content || []);
        setPagination({
          current: page,
          pageSize: size,
          total: data.totalElements || 0,
        });
      }
    } catch (error: any) {
      console.error('加载用户列表失败:', error);
      if (isMounted.current) {
        message.error(error.message || '加载用户列表失败');
      }
    } finally {
      if (isMounted.current) {
        setLoading(false);
      }
    }
  };

  // 加载角色列表
  const loadRoles = async () => {
    try {
      const data = await roleService.getAllActiveRoles();
      setRoles(data);
    } catch (error: any) {
      console.error('加载角色列表失败', error);
    }
  };

  // 加载组织列表
  const loadOrgs = async () => {
    try {
      const data = await orgService.getAllActiveOrgs?.() || [];
      setOrgs(data);
    } catch (error: any) {
      console.error('加载组织列表失败', error);
    }
  };

  useEffect(() => {
    isMounted.current = true;
    loadUsers();
    loadRoles();
    loadOrgs();
    
    return () => {
      isMounted.current = false;
    };
  }, []);

  // 搜索
  const handleSearch = (values: any) => {
    setSearchParams(values);
    loadUsers(1, pagination.pageSize);
  };

  // 重置搜索
  const handleReset = () => {
    setSearchParams({});
    loadUsers(1, pagination.pageSize);
  };

  // 分页变化
  const handleTableChange = (newPagination: any) => {
    loadUsers(newPagination.current, newPagination.pageSize);
  };

  // 打开新增对话框
  const handleAdd = () => {
    setEditingUser(null);
    form.resetFields();
    form.setFieldsValue({
      status: 'ACTIVE',
      role: 'EMPLOYEE',
    });
    setModalVisible(true);
  };

  // 打开编辑对话框
  const handleEdit = async (user: User) => {
    setEditingUser(user);
    form.setFieldsValue(user);
    
    // 加载用户的角色
    try {
      const roleIds = await userService.getUserRoles(user.id);
      form.setFieldsValue({ roleIds });
    } catch (error) {
      console.error('加载用户角色失败', error);
    }
    
    setModalVisible(true);
  };

  // 删除用户
  const handleDelete = async (userId: number) => {
    try {
      await userService.deleteUser(userId);
      message.success('删除成功');
      loadUsers(pagination.current, pagination.pageSize);
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 启用/禁用用户
  const handleToggleStatus = async (userId: number) => {
    try {
      await userService.toggleUserStatus(userId);
      message.success('状态更新成功');
      loadUsers(pagination.current, pagination.pageSize);
    } catch (error: any) {
      message.error(error.message || '操作失败');
    }
  };

  // 重置密码
  const handleResetPassword = (user: User) => {
    setEditingUser(user);
    passwordForm.resetFields();
    setPasswordModalVisible(true);
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const { roleIds, ...userData } = values;
      
      if (editingUser) {
        // 更新用户基本信息
        await userService.updateUser(editingUser.id, userData);
        // 分配角色
        if (roleIds && roleIds.length > 0) {
          await userService.assignRoles(editingUser.id, roleIds);
        }
        message.success('更新成功');
      } else {
        // 创建用户
        const newUser = await userService.createUser(userData);
        // 分配角色
        if (roleIds && roleIds.length > 0) {
          await userService.assignRoles(newUser.id, roleIds);
        }
        message.success('创建成功');
      }
      
      setModalVisible(false);
      loadUsers(pagination.current, pagination.pageSize);
    } catch (error: any) {
      if (error.errorFields) return; // 表单校验错误
      message.error(error.message || '操作失败');
    }
  };

  // 提交密码重置
  const handlePasswordSubmit = async () => {
    try {
      const values = await passwordForm.validateFields();
      if (editingUser) {
        await userService.resetPassword(editingUser.id, values.newPassword);
        message.success('密码重置成功');
        setPasswordModalVisible(false);
      }
    } catch (error: any) {
      if (error.errorFields) return;
      message.error(error.message || '操作失败');
    }
  };

  // 表格列定义
  const columns: ColumnsType<User> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60,
    },
    {
      title: '工号',
      dataIndex: 'employeeNo',
      key: 'employeeNo',
      width: 120,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 120,
    },
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 100,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 180,
      ellipsis: true,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      width: 130,
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      width: 120,
      render: (role: string) => {
        const roleMap: Record<string, { text: string; color: string }> = {
          ADMIN: { text: '管理员', color: 'red' },
          HR: { text: 'HR', color: 'orange' },
          MANAGER: { text: '主管', color: 'blue' },
          EMPLOYEE: { text: '员工', color: 'green' },
        };
        const config = roleMap[role] || { text: role, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const statusMap: Record<string, { text: string; color: string }> = {
          ACTIVE: { text: '正常', color: 'green' },
          INACTIVE: { text: '禁用', color: 'default' },
          LOCKED: { text: '锁定', color: 'red' },
        };
        const config = statusMap[status] || { text: status, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '最后登录',
      dataIndex: 'lastLoginAt',
      key: 'lastLoginAt',
      width: 160,
      render: (text: string) => text ? new Date(text).toLocaleString('zh-CN') : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="编辑">
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Tooltip title="重置密码">
            <Button
              type="link"
              size="small"
              icon={<KeyOutlined />}
              onClick={() => handleResetPassword(record)}
            />
          </Tooltip>
          {record.status === 'LOCKED' ? (
            <Tooltip title="解锁">
              <Button
                type="link"
                size="small"
                icon={<UnlockOutlined />}
                onClick={() => handleToggleStatus(record.id)}
              />
            </Tooltip>
          ) : (
            <Tooltip title={record.status === 'ACTIVE' ? '禁用' : '启用'}>
              <Button
                type="link"
                size="small"
                icon={record.status === 'ACTIVE' ? <LockOutlined /> : <UnlockOutlined />}
                onClick={() => handleToggleStatus(record.id)}
              />
            </Tooltip>
          )}
          <Popconfirm
            title="确定要删除此用户吗？"
            description="删除后无法恢复"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Card title="用户管理">
      {/* 搜索栏 */}
      <Form layout="inline" onFinish={handleSearch} style={{ marginBottom: 16 }}>
        <Form.Item name="keyword">
          <Input placeholder="搜索姓名或用户名" allowClear style={{ width: 200 }} />
        </Form.Item>
        <Form.Item name="orgId">
          <Select placeholder="选择组织" allowClear style={{ width: 200 }}>
            {orgs.map(org => (
              <Option key={org.id} value={org.id}>{org.name}</Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item name="role">
          <Select placeholder="选择角色" allowClear style={{ width: 150 }}>
            {roles.map(role => (
              <Option key={role.code} value={role.code}>{role.name}</Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">搜索</Button>
            <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增用户
            </Button>
          </Space>
        </Form.Item>
      </Form>

      {/* 表格 */}
      <Table
        columns={columns}
        dataSource={users}
        rowKey="id"
        loading={loading}
        pagination={pagination}
        onChange={handleTableChange}
        scroll={{ x: 1400 }}
      />

      {/* 新增/编辑对话框 */}
      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={700}
        okText="确定"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="请输入用户名" disabled={!!editingUser} />
          </Form.Item>

          {!editingUser && (
            <Form.Item
              name="password"
              label="密码"
              rules={[{ required: !editingUser, message: '请输入密码' }]}
            >
              <Input.Password placeholder="请输入密码（默认：123456）" />
            </Form.Item>
          )}

          <Form.Item
            name="employeeNo"
            label="工号"
            rules={[{ required: true, message: '请输入工号' }]}
          >
            <Input placeholder="请输入工号" disabled={!!editingUser} />
          </Form.Item>

          <Form.Item
            name="name"
            label="姓名"
            rules={[{ required: true, message: '请输入姓名' }]}
          >
            <Input placeholder="请输入姓名" />
          </Form.Item>

          <Form.Item name="email" label="邮箱" rules={[{ type: 'email', message: '请输入有效的邮箱' }]}>
            <Input placeholder="请输入邮箱" />
          </Form.Item>

          <Form.Item name="phone" label="手机号">
            <Input placeholder="请输入手机号" />
          </Form.Item>

          <Form.Item name="orgId" label="所属组织">
            <Select placeholder="请选择组织">
              {orgs.map(org => (
                <Option key={org.id} value={org.id}>{org.name}</Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="roleIds" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Select mode="multiple" placeholder="请选择角色（可多选）">
              {roles.map(role => (
                <Option key={role.id} value={role.id}>{role.name}</Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="status" label="状态" valuePropName="checked">
            <Select>
              <Option value="ACTIVE">正常</Option>
              <Option value="INACTIVE">禁用</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      {/* 重置密码对话框 */}
      <Modal
        title="重置密码"
        open={passwordModalVisible}
        onOk={handlePasswordSubmit}
        onCancel={() => setPasswordModalVisible(false)}
        okText="确定"
        cancelText="取消"
      >
        <Form form={passwordForm} layout="vertical">
          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '密码长度至少6位' }
            ]}
          >
            <Input.Password placeholder="请输入新密码" />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label="确认密码"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'));
                },
              }),
            ]}
          >
            <Input.Password placeholder="请再次输入新密码" />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default UserManagePage;
