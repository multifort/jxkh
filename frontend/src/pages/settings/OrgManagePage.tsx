import React, { useEffect, useState } from 'react';
import { 
  Card, Tree, Button, Modal, Form, Input, InputNumber, Switch, 
  message, Space, Popconfirm, Descriptions, Table, Divider, Row, Col, Tag 
} from 'antd';
import { 
  PlusOutlined, EditOutlined, DeleteOutlined, 
  FolderOpenOutlined, UserOutlined, TeamOutlined 
} from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import type { ColumnsType } from 'antd/es/table';
import { orgService } from '../../services/rbacService';
import type { OrgTreeNode, Org, User } from '../../types/auth';

const OrgManagePage: React.FC = () => {
  // 左侧树状态
  const [treeData, setTreeData] = useState<DataNode[]>([]);
  const [orgList, setOrgList] = useState<OrgTreeNode[]>([]);
  
  // 右侧详情状态
  const [selectedOrg, setSelectedOrg] = useState<Org | null>(null);
  const [orgUsers, setOrgUsers] = useState<User[]>([]);
  const [loadingDetail, setLoadingDetail] = useState(false);
  
  // 表单状态
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
      org: org, // 附加完整数据
      children: org.children && org.children.length > 0 ? convertToTreeData(org.children) : undefined,
    }));
  };

  useEffect(() => {
    loadOrgTree();
  }, []);

  // 选择组织，加载详情
  const handleSelectOrg = async (selectedKeys: React.Key[]) => {
    if (selectedKeys.length === 0) return;
    
    const orgId = Number(selectedKeys[0]);
    try {
      setLoadingDetail(true);
      // 获取组织详情
      const orgDetail = await orgService.getOrgById(orgId);
      setSelectedOrg(orgDetail);
      
      // 获取组织下用户
      const users = await orgService.getOrgUsers(orgId);
      setOrgUsers(users);
    } catch (error: any) {
      message.error(error.message || '加载组织详情失败');
    } finally {
      setLoadingDetail(false);
    }
  };

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
      if (selectedOrg?.id === orgId) {
        setSelectedOrg(null);
        setOrgUsers([]);
      }
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
      if (error.errorFields) return;
      message.error(error.message || '操作失败');
    }
  };

  // 渲染树节点操作按钮
  const renderTitle = (node: DataNode & { org?: Org }) => {
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
      title: renderTitle(item as DataNode & { org?: Org }),
      children: item.children ? renderTreeNodes(item.children) : undefined,
    }));
  };

  // 用户表格列定义
  const userColumns: ColumnsType<User> = [
    {
      title: '工号',
      dataIndex: 'employeeNo',
      key: 'employeeNo',
      width: 120,
    },
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 100,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 120,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '手机',
      dataIndex: 'phone',
      key: 'phone',
      width: 130,
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      width: 100,
      render: (role: string) => {
        const roleMap: Record<string, { color: string; text: string }> = {
          ADMIN: { color: 'red', text: '管理员' },
          HR: { color: 'blue', text: '人事' },
          MANAGER: { color: 'green', text: '经理' },
          EMPLOYEE: { color: 'default', text: '员工' },
        };
        const roleInfo = roleMap[role] || { color: 'default', text: role };
        return <Tag color={roleInfo.color}>{roleInfo.text}</Tag>;
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => (
        <Tag color={status === 'ACTIVE' ? 'success' : 'default'}>
          {status === 'ACTIVE' ? '正常' : status}
        </Tag>
      ),
    },
  ];

  // 查找父组织
  const findParentOrg = (orgs: OrgTreeNode[], targetId: number, parent?: OrgTreeNode): OrgTreeNode | null => {
    for (const org of orgs) {
      if (org.id === targetId) return parent || null;
      if (org.children) {
        const found = findParentOrg(org.children, targetId, org);
        if (found) return found;
      }
    }
    return null;
  };

  return (
    <div>
      <Card title="组织架构管理" style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          {/* 左侧：组织树 */}
          <Col span={8}>
            <Card 
              title={<><TeamOutlined /> 组织树</>}
              size="small"
              extra={
                <Button 
                  type="primary" 
                  size="small"
                  icon={<PlusOutlined />} 
                  onClick={() => handleAdd()}
                >
                  新增根组织
                </Button>
              }
            >
              {treeData.length === 0 && !loading ? (
                <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
                  <FolderOpenOutlined style={{ fontSize: 32, marginBottom: 12 }} />
                  <p>暂无组织数据</p>
                </div>
              ) : (
                <Tree
                  treeData={renderTreeNodes(treeData)}
                  showLine
                  defaultExpandAll
                  onSelect={handleSelectOrg}
                />
              )}
            </Card>
          </Col>

          {/* 右侧：组织详情 */}
          <Col span={16}>
            <Card 
              title={<><UserOutlined /> 组织详情</>}
              size="small"
              loading={loadingDetail}
            >
              {!selectedOrg ? (
                <div style={{ textAlign: 'center', padding: '80px 0', color: '#999' }}>
                  <UserOutlined style={{ fontSize: 48, marginBottom: 16 }} />
                  <p>请在左侧选择一个组织查看详情</p>
                </div>
              ) : (
                <>
                  {/* 基本信息 */}
                  <Descriptions title="基本信息" bordered column={2} size="small">
                    <Descriptions.Item label="组织ID">{selectedOrg.id}</Descriptions.Item>
                    <Descriptions.Item label="组织编码">{selectedOrg.code}</Descriptions.Item>
                    <Descriptions.Item label="组织名称" span={2}>{selectedOrg.name}</Descriptions.Item>
                    <Descriptions.Item label="组织类型">{selectedOrg.orgType || '-'}</Descriptions.Item>
                    <Descriptions.Item label="层级">第 {selectedOrg.level} 级</Descriptions.Item>
                    <Descriptions.Item label="负责人ID">{selectedOrg.leaderId || '-'}</Descriptions.Item>
                    <Descriptions.Item label="排序">{selectedOrg.sort}</Descriptions.Item>
                    <Descriptions.Item label="启用状态" span={2}>
                      <Tag color={selectedOrg.enabled ? 'success' : 'default'}>
                        {selectedOrg.enabled ? '启用' : '禁用'}
                      </Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="描述" span={2}>{selectedOrg.description || '-'}</Descriptions.Item>
                    <Descriptions.Item label="创建时间">
                      {new Date(selectedOrg.createdAt).toLocaleString('zh-CN')}
                    </Descriptions.Item>
                    <Descriptions.Item label="更新时间">
                      {new Date(selectedOrg.updatedAt).toLocaleString('zh-CN')}
                    </Descriptions.Item>
                  </Descriptions>

                  <Divider />

                  {/* 组织成员 */}
                  <Descriptions title={`组织成员 (${orgUsers.length}人)`}>
                    <Descriptions.Item label="">
                      <Table
                        dataSource={orgUsers}
                        columns={userColumns}
                        rowKey="id"
                        size="small"
                        pagination={{ pageSize: 5 }}
                        scroll={{ x: 800 }}
                      />
                    </Descriptions.Item>
                  </Descriptions>
                </>
              )}
            </Card>
          </Col>
        </Row>
      </Card>

      {/* 新增/编辑对话框 */}
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

export default OrgManagePage;
