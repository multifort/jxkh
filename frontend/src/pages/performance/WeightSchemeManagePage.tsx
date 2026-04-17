import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, Form, Input, Select, message, Tag, Popconfirm, Row, Col, Progress, Divider } from 'antd';
const { Compact } = Space;
import { PlusOutlined, EditOutlined, DeleteOutlined, SendOutlined, InboxOutlined, CopyOutlined, LeftOutlined } from '@ant-design/icons';
import { weightSchemeApi } from '../../services/weightSchemeService';
import { indicatorApi } from '../../services/indicatorService';
import type { WeightScheme, WeightSchemeItem, Indicator, WeightSchemeStatus } from '../../types/performance';
import type { ColumnsType } from 'antd/es/table';

const { TextArea } = Input;

const WeightSchemeManagePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [schemes, setSchemes] = useState<WeightScheme[]>([]);
  const [indicators, setIndicators] = useState<Indicator[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();
  const [filters, setFilters] = useState<{
    keyword?: string;
    status?: string;
  }>({});

  // 编辑模式：基本信息 or 权重配置
  const [editMode, setEditMode] = useState<'basic' | 'weight'>('basic');
  const [currentItems, setCurrentItems] = useState<WeightSchemeItem[]>([]);

  // 加载方案列表
  const loadSchemes = async () => {
    setLoading(true);
    try {
      const res = await weightSchemeApi.list(
        filters.keyword,
        undefined,
        filters.status,
        page,
        pageSize
      );
      const pageData = res.data.data || { content: [], totalElements: 0 };
      setSchemes(pageData.content || []);
      setTotal(pageData.totalElements || 0);
    } catch (error) {
      message.error('加载方案列表失败');
      setSchemes([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  };

  // 加载所有启用的指标
  const loadIndicators = async () => {
    try {
      const res = await indicatorApi.list(undefined, undefined, undefined, 'ACTIVE', 0, 1000);
      const pageData = res.data.data || { content: [], totalElements: 0 };
      setIndicators(pageData.content || []);
    } catch (error) {
      message.error('加载指标列表失败');
    }
  };

  useEffect(() => {
    loadSchemes();
    loadIndicators();
  }, [page, pageSize, filters]);

  // 打开新建/编辑弹窗
  const handleOpenModal = async (record?: WeightScheme) => {
    if (record) {
      setEditingId(record.id);
      form.setFieldsValue({
        name: record.name,
        code: record.code,
        cycleId: record.cycleId,
        description: record.description
      });

      // 加载权重明细
      try {
        const res = await weightSchemeApi.getItems(record.id);
        const items = res.data.data || [];
        setCurrentItems(items);
      } catch (error) {
        message.error('加载权重明细失败');
        setCurrentItems([]);
      }

      setEditMode('basic');
    } else {
      setEditingId(null);
      form.resetFields();
      setCurrentItems([]);
      setEditMode('basic');
    }
    setModalVisible(true);
  };

  // 提交基本信息
  const handleSubmitBasic = async () => {
    try {
      const values = await form.validateFields();
      if (editingId) {
        await weightSchemeApi.update(editingId, values);
        message.success('更新成功');
      } else {
        await weightSchemeApi.create(values);
        message.success('创建成功');
      }
      // 切换到权重配置模式
      setEditMode('weight');
      loadSchemes();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 保存权重配置
  const handleSaveWeights = async () => {
    if (!editingId) return;

    try {
      await weightSchemeApi.saveItems(editingId, currentItems);
      message.success('权重保存成功');
      setModalVisible(false);
      loadSchemes();
    } catch (error: any) {
      message.error(error.response?.data?.message || '保存失败');
    }
  };

  // 发布方案
  const handlePublish = async (id: number) => {
    try {
      await weightSchemeApi.publish(id);
      message.success('发布成功');
      loadSchemes();
    } catch (error: any) {
      message.error(error.response?.data?.message || '发布失败');
    }
  };

  // 归档方案
  const handleArchive = async (id: number) => {
    try {
      await weightSchemeApi.archive(id);
      message.success('归档成功');
      loadSchemes();
    } catch (error: any) {
      message.error(error.response?.data?.message || '归档失败');
    }
  };

  // 删除方案
  const handleDelete = async (id: number) => {
    try {
      await weightSchemeApi.delete(id);
      message.success('删除成功');
      loadSchemes();
    } catch (error: any) {
      message.error(error.response?.data?.message || '删除失败');
    }
  };

  // 复制方案
  const handleCopy = async (id: number) => {
    try {
      await weightSchemeApi.copy(id);
      message.success('复制成功');
      loadSchemes();
    } catch (error: any) {
      message.error(error.response?.data?.message || '复制失败');
    }
  };

  // 添加指标到方案
  const handleAddIndicator = (indicatorId: number) => {
    if (currentItems.find(item => item.indicatorId === indicatorId)) {
      message.warning('该指标已添加');
      return;
    }

    const indicator = indicators.find(ind => ind.id === indicatorId);
    if (indicator) {
      setCurrentItems([
        ...currentItems,
        {
          schemeId: editingId!,
          indicatorId: indicator.id,
          weight: 0,
          sortOrder: currentItems.length
        }
      ]);
    }
  };

  // 更新指标权重
  const handleUpdateWeight = (indicatorId: number, weight: number) => {
    setCurrentItems(currentItems.map(item =>
      item.indicatorId === indicatorId ? { ...item, weight } : item
    ));
  };

  // 移除指标
  const handleRemoveIndicator = (indicatorId: number) => {
    setCurrentItems(currentItems.filter(item => item.indicatorId !== indicatorId));
  };

  // 计算总权重
  const totalWeight = currentItems.reduce((sum, item) => sum + (item.weight || 0), 0);
  const weightColor = totalWeight === 100 ? 'success' : totalWeight > 100 ? 'exception' : 'normal';

  // 获取未添加的指标
  const getAvailableIndicators = () => {
    const addedIds = currentItems.map(item => item.indicatorId);
    return indicators.filter(ind => !addedIds.includes(ind.id));
  };

  // 表格列定义
  const columns: ColumnsType<WeightScheme> = [
    {
      title: '方案名称',
      dataIndex: 'name',
      key: 'name',
      width: 150
    },
    {
      title: '方案编码',
      dataIndex: 'code',
      key: 'code',
      width: 120
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 80
    },
    {
      title: '权重总和',
      dataIndex: 'totalWeight',
      key: 'totalWeight',
      width: 100,
      render: (weight: number) => `${weight}%`
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: WeightSchemeStatus) => {
        const colorMap = {
          DRAFT: 'default',
          PUBLISHED: 'success',
          ARCHIVED: 'warning'
        };
        const textMap = {
          DRAFT: '草稿',
          PUBLISHED: '已发布',
          ARCHIVED: '已归档'
        };
        return <Tag color={colorMap[status]}>{textMap[status]}</Tag>;
      }
    },
    {
      title: '说明',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          {record.status === 'DRAFT' && (
            <>
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleOpenModal(record)}
              >
                编辑
              </Button>
              <Button
                type="link"
                size="small"
                icon={<SendOutlined />}
                onClick={() => handlePublish(record.id)}
              >
                发布
              </Button>
              <Popconfirm
                title="确定要删除吗？"
                onConfirm={() => handleDelete(record.id)}
                okText="确定"
                cancelText="取消"
              >
                <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                  删除
                </Button>
              </Popconfirm>
            </>
          )}
          {record.status === 'PUBLISHED' && (
            <Button
              type="link"
              size="small"
              icon={<InboxOutlined />}
              onClick={() => handleArchive(record.id)}
            >
              归档
            </Button>
          )}
          <Button
            type="link"
            size="small"
            icon={<CopyOutlined />}
            onClick={() => handleCopy(record.id)}
          >
            复制
          </Button>
        </Space>
      )
    }
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <Space>
          <Input.Search
            placeholder="搜索方案名称或编码"
            allowClear
            onSearch={(value) => setFilters({ ...filters, keyword: value })}
            style={{ width: 250 }}
          />
          <Select
            placeholder="状态"
            allowClear
            style={{ width: 120 }}
            onChange={(value) => setFilters({ ...filters, status: value })}
            options={[
              { label: '草稿', value: 'DRAFT' },
              { label: '已发布', value: 'PUBLISHED' },
              { label: '已归档', value: 'ARCHIVED' }
            ]}
          />
        </Space>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => handleOpenModal()}>
          新建方案
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={schemes}
        rowKey="id"
        loading={loading}
        pagination={{
          current: page + 1,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
          onChange: (page, pageSize) => {
            setPage(page - 1);
            setPageSize(pageSize);
          }
        }}
        scroll={{ x: 1000 }}
      />

      <Modal
        title={editingId ? '编辑方案' : '新建方案'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        width={800}
        footer={
          editMode === 'basic' ? (
            <Space>
              <Button onClick={() => setModalVisible(false)}>取消</Button>
              <Button type="primary" onClick={handleSubmitBasic}>
                下一步：配置权重
              </Button>
            </Space>
          ) : (
            <Space>
              <Button icon={<LeftOutlined />} onClick={() => setEditMode('basic')}>
                返回基本信息
              </Button>
              <Button onClick={() => setModalVisible(false)}>取消</Button>
              <Button type="primary" onClick={handleSaveWeights}>
                保存方案
              </Button>
            </Space>
          )
        }
      >
        {editMode === 'basic' ? (
          <Form form={form} layout="vertical">
            <Form.Item
              name="name"
              label="方案名称"
              rules={[{ required: true, message: '请输入方案名称' }]}
            >
              <Input placeholder="请输入方案名称" />
            </Form.Item>
            <Form.Item
              name="code"
              label="方案编码"
              rules={[{ required: true, message: '请输入方案编码' }]}
            >
              <Input placeholder="请输入方案编码" />
            </Form.Item>
            <Form.Item name="description" label="方案说明">
              <TextArea rows={3} placeholder="请输入方案说明" />
            </Form.Item>
          </Form>
        ) : (
          <div>
            <Divider orientation="left">权重配置</Divider>
            <Row gutter={16}>
              <Col span={12}>
                <h4>可用指标</h4>
                <Select
                  mode="multiple"
                  style={{ width: '100%', marginBottom: 16 }}
                  placeholder="选择要添加的指标"
                  onChange={(values) => {
                    values.forEach((id: number) => handleAddIndicator(id));
                  }}
                  value={[]}
                >
                  {getAvailableIndicators().map(ind => (
                    <Select.Option key={ind.id} value={ind.id}>
                      {ind.name} ({ind.code})
                    </Select.Option>
                  ))}
                </Select>
              </Col>
              <Col span={12}>
                <h4>已选指标及权重</h4>
                <div style={{ maxHeight: 300, overflowY: 'auto' }}>
                  {currentItems.map(item => {
                    const indicator = indicators.find(ind => ind.id === item.indicatorId);
                    return indicator ? (
                      <div key={item.indicatorId} style={{ marginBottom: 8, padding: 8, background: '#f5f5f5', borderRadius: 4 }}>
                        <Row gutter={8} align="middle">
                          <Col flex="auto">
                            <strong>{indicator.name}</strong>
                          </Col>
                          <Col span={8}>
                            <Compact style={{ width: '100%' }}>
                              <Input
                                type="number"
                                min={0}
                                max={100}
                                value={item.weight}
                                onChange={(e) => handleUpdateWeight(item.indicatorId, parseFloat(e.target.value) || 0)}
                                style={{ width: 'calc(100% - 40px)' }}
                              />
                              <Input style={{ width: 40, textAlign: 'center' }} value="%" disabled />
                            </Compact>
                          </Col>
                          <Col>
                            <Button
                              type="link"
                              danger
                              size="small"
                              onClick={() => handleRemoveIndicator(item.indicatorId)}
                            >
                              移除
                            </Button>
                          </Col>
                        </Row>
                      </div>
                    ) : null;
                  })}
                </div>
              </Col>
            </Row>
            <Divider />
            <div style={{ textAlign: 'center' }}>
              <span style={{ fontSize: 16 }}>权重总和：</span>
              <Progress
                type="circle"
                percent={totalWeight}
                strokeColor={weightColor === 'success' ? '#52c41a' : weightColor === 'exception' ? '#ff4d4f' : '#1890ff'}
                format={() => `${totalWeight}%`}
              />
              {totalWeight !== 100 && (
                <div style={{ marginTop: 8, color: totalWeight > 100 ? '#ff4d4f' : '#faad14' }}>
                  {totalWeight > 100 ? `超出 ${totalWeight - 100}%` : `还需分配 ${100 - totalWeight}%`}
                </div>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default WeightSchemeManagePage;
