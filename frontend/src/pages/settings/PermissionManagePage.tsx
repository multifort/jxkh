import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Modal, Form, Input, Select, message, Space, Tag, TreeSelect, Popconfirm } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import { permissionService } from '@/services/rbacService';

const { Option } = Select;

interface Permission {
  id: number;
  code: string;
  name: string;
  type: string;
  resource?: string;
  parentId?: number;
  sort: number;
  icon?: string;
  path?: string;
  createdAt: string;
  updatedAt: string;
  isDeleted: boolean;
}

interface PermissionTreeNode {
  title: string;
  value: number;
  key: string;
  children?: PermissionTreeNode[];
}

const PermissionManagePage: React.FC = () => {
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [permissionTree, setPermissionTree] = useState<PermissionTreeNode[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingPermission, setEditingPermission] = useState<Permission | null>(null);
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  // 加载权限列表
  const loadPermissions = async () => {
    try {
      setLoading(true);
      const data = await permissionService.getAllActivePermissions();
      setPermissions(data);
      
      // 构建树形结构
      const tree = buildPermissionTree(data);
      setPermissionTree(tree);
    } catch (error: any) {
      message.error(error.message || '加载权限列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 构建权限树
  const buildPermissionTree = (perms: Permission[]): PermissionTreeNode[] => {
    const map = new Map<number, PermissionTreeNode>();
    const roots: PermissionTreeNode[] = [];

    perms.forEach(p => {
      map.set(p.id, {
        title: `${p.name} (${p.code})`,
        value: p.id,
        key: String(p.id),
        children: [],
      });
    });

    perms.forEach(p => {
      const node = map.get(p.id)!;
      if (p.parentId && map.has(p.parentId)) {
        map.get(p.parentId)!.children!.push(node);
      } else {
        roots.push(node);
      }
    });

    return roots;
  };

  useEffect(() => {
    loadPermissions();
  }, []);

  // 打开新增对话框
  const handleAdd = () => {
    setEditingPermission(null);
    form.resetFields();
    form.setFieldsValue({
      type: 'MENU',
      sort: 0,
    });
    setModalVisible(true);
  };

  // 打开编辑对话框
  const handleEdit = (permission: Permission) => {
    setEditingPermission(permission);
    form.setFieldsValue(permission);
    setModalVisible(true);
  };

  // 删除权限
  const handleDelete = async (permissionId: number) => {
    try {
      await permissionService.deletePermission(permissionId);
      message.success('删除成功');
      loadPermissions();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (editingPermission) {
        await permissionService.updatePermission(editingPermission.id, values);
        message.success('更新成功');
      } else {
        await permissionService.createPermission(values);
        message.success('创建成功');
      }
      
      setModalVisible(false);
      loadPermissions();
    } catch (error: any) {
      if (error.errorFields) return; // 表单校验错误
      message.error(error.message || '操作失败');
    }
  };

  // 表格列定义
  const columns: ColumnsType<Permission> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60,
    },
    {
      title: '权限代码',
      dataIndex: 'code',
      key: 'code',
      width: 200,
    },
    {
      title: '权限名称',
      dataIndex: 'name',
      key: 'name',
      width: 150,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: string) => {
        const typeMap: Record<string, { text: string; color: string }> = {
          MENU: { text: '菜单', color: 'blue' },
          BUTTON: { text: '按钮', color: 'green' },
          DATA: { text: '数据', color: 'orange' },
        };
        const config = typeMap[type] || { text: type, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '资源标识',
      dataIndex: 'resource',
      key: 'resource',
      width: 150,
      ellipsis: true,
    },
    {
      title: '路由路径',
      dataIndex: 'path',
      key: 'path',
      width: 180,
      ellipsis: true,
    },
    {
      title: '排序',
      dataIndex: 'sort',
      key: 'sort',
      width: 80,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
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
          <Popconfirm
            title="确定要删除此权限吗？"
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
    <Card title="权限管理">
      {/* 工具栏 */}
      <div style={{ marginBottom: 16 }}>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增权限
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadPermissions}>
            刷新
          </Button>
        </Space>
      </div>

      {/* 表格 */}
      <Table
        columns={columns}
        dataSource={permissions}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 20,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
        }}
        scroll={{ x: 1200 }}
      />

      {/* 新增/编辑对话框 */}
      <Modal
        title={editingPermission ? '编辑权限' : '新增权限'}
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
            label="权限代码"
            rules={[{ required: true, message: '请输入权限代码' }]}
          >
            <Input placeholder="例如：user:create" disabled={!!editingPermission} />
          </Form.Item>

          <Form.Item
            name="name"
            label="权限名称"
            rules={[{ required: true, message: '请输入权限名称' }]}
          >
            <Input placeholder="例如：创建用户" />
          </Form.Item>

          <Form.Item
            name="type"
            label="权限类型"
            rules={[{ required: true, message: '请选择权限类型' }]}
          >
            <Select>
              <Option value="MENU">菜单</Option>
              <Option value="BUTTON">按钮</Option>
              <Option value="DATA">数据</Option>
            </Select>
          </Form.Item>

          <Form.Item name="parentId" label="父权限">
            <TreeSelect
              placeholder="选择父权限（可选）"
              allowClear
              treeData={permissionTree}
              fieldNames={{ label: 'title', value: 'value', children: 'children' }}
            />
          </Form.Item>

          <Form.Item name="resource" label="资源标识">
            <Input placeholder="例如：user:create" />
          </Form.Item>

          <Form.Item name="path" label="路由路径">
            <Input placeholder="例如：/users" />
          </Form.Item>

          <Form.Item name="icon" label="图标">
            <Input placeholder="例如：UserOutlined" />
          </Form.Item>

          <Form.Item name="sort" label="排序">
            <Input type="number" />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default PermissionManagePage;

