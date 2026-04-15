import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Modal, Form, Input, InputNumber, Switch, message, Space, Popconfirm, Tag, Transfer } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, KeyOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { roleService, permissionService } from '../../services/rbacService';
import type { Role, Permission } from '../../types/auth';

const RoleManagePage: React.FC = () => {
  const [roles, setRoles] = useState<Role[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [permissionModalVisible, setPermissionModalVisible] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [targetKeys, setTargetKeys] = useState<number[]>([]); // 已分配的权限ID

  // 加载角色列表
  const loadRoles = async () => {
    try {
      setLoading(true);
      const data = await roleService.getAllActiveRoles();
      setRoles(data);
    } catch (error: any) {
      message.error(error.message || '加载角色列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载所有权限
  const loadPermissions = async () => {
    try {
      const data = await permissionService.getAllActivePermissions();
      setPermissions(data);
    } catch (error: any) {
      console.error('加载权限列表失败', error);
    }
  };

  useEffect(() => {
    loadRoles();
    loadPermissions();
  }, []);

  // 打开新增对话框
  const handleAdd = () => {
    setEditingRole(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 打开编辑对话框
  const handleEdit = (role: Role) => {
    setEditingRole(role);
    form.setFieldsValue(role);
    setModalVisible(true);
  };

  // 删除角色
  const handleDelete = async (roleId: number) => {
    try {
      await roleService.deleteRole(roleId);
      message.success('删除成功');
      loadRoles();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 打开权限分配对话框
  const handleAssignPermissions = async (role: Role) => {
    setEditingRole(role);
    try {
      // 获取角色当前的权限
      const permissionIds = await roleService.getRolePermissions(role.id);
      setTargetKeys(permissionIds);
      setPermissionModalVisible(true);
    } catch (error: any) {
      message.error(error.message || '加载角色权限失败');
    }
  };

  // 提交权限分配
  const handlePermissionSubmit = async () => {
    if (!editingRole) return;
    
    try {
      await roleService.assignPermissions(editingRole.id, targetKeys);
      message.success('权限分配成功');
      setPermissionModalVisible(false);
      setTargetKeys([]);
    } catch (error: any) {
      message.error(error.message || '权限分配失败');
    }
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (editingRole) {
        await roleService.updateRole(editingRole.id, values);
        message.success('更新成功');
      } else {
        await roleService.createRole(values);
        message.success('创建成功');
      }
      
      setModalVisible(false);
      loadRoles();
    } catch (error: any) {
      if (error.errorFields) return; // 表单校验错误
      message.error(error.message || '操作失败');
    }
  };

  // 表格列定义
  const columns: ColumnsType<Role> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '角色编码',
      dataIndex: 'code',
      key: 'code',
      width: 150,
    },
    {
      title: '角色名称',
      dataIndex: 'name',
      key: 'name',
      width: 150,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '排序',
      dataIndex: 'sort',
      key: 'sort',
      width: 100,
      sorter: (a, b) => a.sort - b.sort,
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      render: (enabled: boolean) => (
        <Tag color={enabled ? 'green' : 'red'}>
          {enabled ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (text: string) => new Date(text).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            icon={<KeyOutlined />}
            onClick={() => handleAssignPermissions(record)}
          >
            分配权限
          </Button>
          <Popconfirm
            title="确定要删除此角色吗？"
            description="删除后无法恢复"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Card
      title="角色管理"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增角色
        </Button>
      }
    >
      <Table
        columns={columns}
        dataSource={roles}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 10,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
        scroll={{ x: 1200 }}
      />

      <Modal
        title={editingRole ? '编辑角色' : '新增角色'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
        okText="确定"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="code"
            label="角色编码"
            rules={[
              { required: true, message: '请输入角色编码' },
              { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线' }
            ]}
          >
            <Input placeholder="例如：ADMIN" disabled={!!editingRole} />
          </Form.Item>

          <Form.Item
            name="name"
            label="角色名称"
            rules={[{ required: true, message: '请输入角色名称' }]}
          >
            <Input placeholder="例如：系统管理员" />
          </Form.Item>

          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入角色描述" />
          </Form.Item>

          <Form.Item name="sort" label="排序" initialValue={0}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="enabled" label="启用状态" valuePropName="checked" initialValue={true}>
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 权限分配对话框 */}
      <Modal
        title={`分配权限 - ${editingRole?.name || ''}`}
        open={permissionModalVisible}
        onOk={handlePermissionSubmit}
        onCancel={() => {
          setPermissionModalVisible(false);
          setTargetKeys([]);
        }}
        width={800}
        okText="确定"
        cancelText="取消"
      >
        <Transfer
          dataSource={permissions.map(p => ({
            key: p.id,
            title: `${p.name} (${p.code})`,
            description: p.type === 'MENU' ? '菜单' : p.type === 'BUTTON' ? '按钮' : '数据',
          }))}
          titles={['可选权限', '已选权限']}
          targetKeys={targetKeys}
          onChange={(newTargetKeys) => setTargetKeys(newTargetKeys as number[])}
          render={(item) => (
            <div>
              <div>{item.title}</div>
              {item.description && <div style={{ fontSize: 12, color: '#999' }}>{item.description}</div>}
            </div>
          )}
          listStyle={{
            width: 350,
            height: 400,
          }}
        />
      </Modal>
    </Card>
  );
};

export default RoleManagePage;
