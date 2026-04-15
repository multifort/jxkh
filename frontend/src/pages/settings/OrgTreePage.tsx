import React, { useEffect, useState } from 'react';
import { Card, Tree, Button, Modal, Form, Input, InputNumber, Switch, message, Space, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, FolderOpenOutlined } from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import { orgService } from '../../services/rbacService';
import type { OrgTreeNode, Org } from '../../types/auth';

const OrgTreePage: React.FC = () => {
  const [treeData, setTreeData] = useState<DataNode[]>([]);
  const [orgList, setOrgList] = useState<OrgTreeNode[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingOrg, setEditingOrg] = useState<Org | null>(null);
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  // 加载组织树
  const loadOrgTree = async () => {
    try {
      setLoading(true);
      const data = await orgService.getOrgTree();
      setOrgList(data);
      setTreeData(convertToTreeData(data));
    } catch (error: any) {
      message.error(error.message || '加载组织树失败');
    } finally {
      setLoading(false);
    }
  };

  // 转换为 Ant Design Tree 数据格式
  const convertToTreeData = (orgs: OrgTreeNode[]): DataNode[] => {
    return orgs.map(org => ({
      title: `${org.name} (${org.code})`,
      key: org.id.toString(),
      children: org.children && org.children.length > 0 ? convertToTreeData(org.children) : undefined,
    }));
  };

  useEffect(() => {
    loadOrgTree();
  }, []);

  // 打开新增对话框
  const handleAdd = (parentId?: number) => {
    setEditingOrg(null);
    form.resetFields();
    if (parentId) {
      form.setFieldsValue({ parentId });
    }
    setModalVisible(true);
  };

  // 打开编辑对话框
  const handleEdit = (orgId: number) => {
    const findOrg = (orgs: OrgTreeNode[], id: number): Org | null => {
      for (const org of orgs) {
        if (org.id === id) return org;
        if (org.children) {
          const found = findOrg(org.children, id);
          if (found) return found;
        }
      }
      return null;
    };

    const org = findOrg(orgList, orgId);
    if (org) {
      setEditingOrg(org);
      form.setFieldsValue(org);
      setModalVisible(true);
    }
  };

  // 删除组织
  const handleDelete = async (orgId: number) => {
    try {
      await orgService.deleteOrg(orgId);
      message.success('删除成功');
      loadOrgTree();
    } catch (error: any) {
      message.error(error.message || '删除失败');
    }
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (editingOrg) {
        await orgService.updateOrg(editingOrg.id, values);
        message.success('更新成功');
      } else {
        await orgService.createOrg(values);
        message.success('创建成功');
      }
      
      setModalVisible(false);
      loadOrgTree();
    } catch (error: any) {
      if (error.errorFields) return; // 表单校验错误
      message.error(error.message || '操作失败');
    }
  };

  // 渲染树节点操作按钮
  const renderTitle = (node: DataNode) => {
    const orgId = Number(node.key);
    
    return (
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%', paddingRight: 24 }}>
        <span>{String(node.title)}</span>
        <Space size="small" onClick={(e) => e.stopPropagation()}>
          <Button
            type="text"
            size="small"
            icon={<PlusOutlined />}
            onClick={() => handleAdd(orgId)}
          />
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(orgId)}
          />
          <Popconfirm
            title="确定要删除此组织吗？"
            description="删除后无法恢复"
            onConfirm={() => handleDelete(orgId)}
          >
            <Button
              type="text"
              size="small"
              danger
              icon={<DeleteOutlined />}
            />
          </Popconfirm>
        </Space>
      </div>
    );
  };

  // 递归渲染树节点
  const renderTreeNodes = (data: DataNode[]): DataNode[] => {
    return data.map(item => ({
      ...item,
      title: renderTitle(item),
      children: item.children ? renderTreeNodes(item.children) : undefined,
    }));
  };

  return (
    <div>
      <Card
        title="组织架构管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
            新增根组织
          </Button>
        }
      >
        {treeData.length === 0 && !loading ? (
          <div style={{ textAlign: 'center', padding: '60px 0', color: '#999' }}>
            <FolderOpenOutlined style={{ fontSize: 48, marginBottom: 16 }} />
            <p>暂无组织数据，请点击"新增根组织"按钮创建</p>
          </div>
        ) : (
          <Tree
            treeData={renderTreeNodes(treeData)}
            showLine
            defaultExpandAll
          />
        )}
      </Card>

      <Modal
        title={editingOrg ? '编辑组织' : '新增组织'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
        okText="确定"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="组织名称"
            rules={[{ required: true, message: '请输入组织名称' }]}
          >
            <Input placeholder="请输入组织名称" />
          </Form.Item>

          <Form.Item
            name="code"
            label="组织编码"
            rules={[
              { required: true, message: '请输入组织编码' },
              { pattern: /^[A-Z0-9_]+$/, message: '只能包含大写字母、数字和下划线' }
            ]}
          >
            <Input placeholder="例如：DEPT_TECH" disabled={!!editingOrg} />
          </Form.Item>

          <Form.Item name="parentId" label="父组织">
            <InputNumber 
              placeholder="留空表示根组织" 
              style={{ width: '100%' }} 
              disabled={!!editingOrg} 
              min={1}
            />
          </Form.Item>

          <Form.Item name="orgType" label="组织类型">
            <Input placeholder="例如：部门、子公司" />
          </Form.Item>

          <Form.Item name="leaderId" label="负责人ID">
            <InputNumber placeholder="请输入负责人用户ID" style={{ width: '100%' }} min={1} />
          </Form.Item>

          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入组织描述" />
          </Form.Item>

          <Form.Item name="sort" label="排序" initialValue={0}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="enabled" label="启用状态" valuePropName="checked" initialValue={true}>
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default OrgTreePage;
