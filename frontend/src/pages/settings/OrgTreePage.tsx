import React, { useEffect, useState } from 'react';
import { Card, Tree, Button, Modal, Form, Input, InputNumber, Switch, message, Space, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import { orgService } from '../../services/rbacService';
import type { OrgTreeNode, Org } from '../../types/auth';

const OrgTreePage: React.FC = () => {
  const [treeData, setTreeData] = useState<DataNode[]>([]);
  const [orgList, setOrgList] = useState<OrgTreeNode[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingOrg, setEditingOrg] = useState<Org | null>(null);
  const [form] = Form.useForm();

  // 加载组织树
  const loadOrgTree = async () => {
    try {
      const response = await orgService.getOrgTree();
      const data = response.data;
      setOrgList(data);
      setTreeData(convertToTreeData(data));
    } catch (error) {
      message.error('加载组织树失败');
    }
  };

  // 转换为 Ant Design Tree 数据格式
  const convertToTreeData = (orgs: OrgTreeNode[]): DataNode[] => {
    return orgs.map(org => ({
      title: `${org.name} (${org.code})`,
      key: org.id.toString(),
      children: org.children ? convertToTreeData(org.children) : undefined,
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
    } catch (error) {
      message.error('删除失败');
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
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 渲染树节点操作按钮
  const renderTitle = (node: DataNode & { org?: Org }) => {
    const orgId = Number(node.key);
    
    return (
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
        <span>{String(node.title)}</span>
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<PlusOutlined />}
            onClick={(e) => {
              e.stopPropagation();
              handleAdd(orgId);
            }}
          />
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={(e) => {
              e.stopPropagation();
              handleEdit(orgId);
            }}
          />
          <Popconfirm
            title="确定要删除此组织吗？"
            onConfirm={(e) => {
              e?.stopPropagation();
              handleDelete(orgId);
            }}
            onCancel={(e) => e?.stopPropagation()}
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
              onClick={(e) => e.stopPropagation()}
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
    <Card
      title="组织架构管理"
      extra={
        <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd()}>
          新增根组织
        </Button>
      }
    >
      <Tree
        treeData={renderTreeNodes(treeData)}
        showLine
        defaultExpandAll
      />

      <Modal
        title={editingOrg ? '编辑组织' : '新增组织'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
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

          <Form.Item name="parentId" label="父组织ID">
            <InputNumber placeholder="留空表示根组织" style={{ width: '100%' }} disabled={!!editingOrg} />
          </Form.Item>

          <Form.Item name="orgType" label="组织类型">
            <Input placeholder="例如：部门、子公司" />
          </Form.Item>

          <Form.Item name="leaderId" label="负责人ID">
            <InputNumber placeholder="请输入负责人用户ID" style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入组织描述" />
          </Form.Item>

          <Form.Item name="sort" label="排序" initialValue={0}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="enabled" label="启用状态" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
};

export default OrgTreePage;
