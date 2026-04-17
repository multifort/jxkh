import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, Form, Input, Select, message, Tag, Popconfirm, TreeSelect } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { indicatorApi, indicatorCategoryApi } from '../../services/indicatorService';
import type { Indicator, IndicatorCategory, IndicatorType, IndicatorStatus } from '../../types/performance';
import type { ColumnsType } from 'antd/es/table';

const { TextArea } = Input;

const IndicatorManagePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [indicators, setIndicators] = useState<Indicator[]>([]);
  const [categories, setCategories] = useState<IndicatorCategory[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();
  const [filters, setFilters] = useState<{
    keyword?: string;
    categoryId?: number;
    type?: string;
    status?: string;
  }>({});

  // 加载分类树
  const loadCategories = async () => {
    try {
      const res = await indicatorCategoryApi.getTree();
      // res.data is ApiResponse, res.data.data is the actual array
      const categoriesData = res.data.data;
      if (Array.isArray(categoriesData)) {
        setCategories(categoriesData);
      } else {
        console.warn('Categories is not an array:', categoriesData);
        setCategories([]);
      }
    } catch (error) {
      message.error('加载分类失败');
      setCategories([]);
    }
  };

  // 加载指标列表
  const loadIndicators = async () => {
    setLoading(true);
    try {
      const res = await indicatorApi.list(
        filters.keyword,
        filters.categoryId,
        filters.type,
        filters.status,
        page,
        pageSize
      );
      // res.data is ApiResponse, res.data.data is the actual page data
      const pageData = res.data.data || { content: [], totalElements: 0 };
      setIndicators(pageData.content || []);
      setTotal(pageData.totalElements || 0);
    } catch (error) {
      message.error('加载指标列表失败');
      setIndicators([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
    loadIndicators();
  }, [page, pageSize, filters]);

  // 打开新建/编辑弹窗
  const handleOpenModal = (record?: Indicator) => {
    if (record) {
      setEditingId(record.id);
      form.setFieldsValue({
        name: record.name,
        code: record.code,
        categoryId: record.categoryId,
        type: record.type,
        unit: record.unit,
        description: record.description,
        calculationMethod: record.calculationMethod,
        dataSource: record.dataSource,
        targetType: record.targetType,
        defaultWeight: record.defaultWeight,
        status: record.status
      });
    } else {
      setEditingId(null);
      form.resetFields();
      form.setFieldsValue({ status: 'ACTIVE' });
    }
    setModalVisible(true);
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingId) {
        await indicatorApi.update(editingId, values);
        message.success('更新成功');
      } else {
        await indicatorApi.create(values);
        message.success('创建成功');
      }
      setModalVisible(false);
      loadIndicators();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 删除指标
  const handleDelete = async (id: number) => {
    try {
      await indicatorApi.delete(id);
      message.success('删除成功');
      loadIndicators();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 切换状态
  const handleToggleStatus = async (id: number) => {
    try {
      await indicatorApi.toggleStatus(id);
      message.success('状态更新成功');
      loadIndicators();
    } catch (error) {
      message.error('状态更新失败');
    }
  };

  // 表格列定义
  const columns: ColumnsType<Indicator> = [
    {
      title: '指标名称',
      dataIndex: 'name',
      key: 'name',
      width: 150
    },
    {
      title: '指标编码',
      dataIndex: 'code',
      key: 'code',
      width: 120
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: IndicatorType) => (
        <Tag color={type === 'QUANTITATIVE' ? 'blue' : 'green'}>
          {type === 'QUANTITATIVE' ? '定量' : '定性'}
        </Tag>
      )
    },
    {
      title: '单位',
      dataIndex: 'unit',
      key: 'unit',
      width: 80
    },
    {
      title: '默认权重',
      dataIndex: 'defaultWeight',
      key: 'defaultWeight',
      width: 100,
      render: (weight?: number) => weight ? `${weight}%` : '-'
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: IndicatorStatus) => (
        <Tag color={status === 'ACTIVE' ? 'success' : 'default'}>
          {status === 'ACTIVE' ? '启用' : '禁用'}
        </Tag>
      )
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
      width: 200,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
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
            icon={record.status === 'ACTIVE' ? <CloseOutlined /> : <CheckOutlined />}
            onClick={() => handleToggleStatus(record.id)}
          >
            {record.status === 'ACTIVE' ? '禁用' : '启用'}
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
        </Space>
      )
    }
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <Space>
          <Input.Search
            placeholder="搜索指标名称或编码"
            allowClear
            onSearch={(value) => setFilters({ ...filters, keyword: value })}
            style={{ width: 250 }}
          />
          <TreeSelect
            placeholder="选择分类"
            allowClear
            style={{ width: 200 }}
            treeData={(Array.isArray(categories) ? categories : []).map(c => ({
              title: c.name,
              value: c.id,
              key: c.id
            }))}
            onChange={(value) => setFilters({ ...filters, categoryId: value })}
          />
          <Select
            placeholder="指标类型"
            allowClear
            style={{ width: 120 }}
            onChange={(value) => setFilters({ ...filters, type: value })}
            options={[
              { label: '定量', value: 'QUANTITATIVE' },
              { label: '定性', value: 'QUALITATIVE' }
            ]}
          />
          <Select
            placeholder="状态"
            allowClear
            style={{ width: 100 }}
            onChange={(value) => setFilters({ ...filters, status: value })}
            options={[
              { label: '启用', value: 'ACTIVE' },
              { label: '禁用', value: 'INACTIVE' }
            ]}
          />
        </Space>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => handleOpenModal()}>
          新建指标
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={indicators}
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
        scroll={{ x: 1200 }}
      />

      <Modal
        title={editingId ? '编辑指标' : '新建指标'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="指标名称"
            rules={[{ required: true, message: '请输入指标名称' }]}
          >
            <Input placeholder="请输入指标名称" />
          </Form.Item>
          <Form.Item
            name="code"
            label="指标编码"
            rules={[{ required: true, message: '请输入指标编码' }]}
          >
            <Input placeholder="请输入指标编码" />
          </Form.Item>
          <Form.Item
            name="categoryId"
            label="所属分类"
            rules={[{ required: true, message: '请选择分类' }]}
          >
            <TreeSelect
              placeholder="请选择分类"
              treeData={(Array.isArray(categories) ? categories : []).map(c => ({
                title: c.name,
                value: c.id,
                key: c.id
              }))}
            />
          </Form.Item>
          <Form.Item
            name="type"
            label="指标类型"
            rules={[{ required: true, message: '请选择类型' }]}
          >
            <Select placeholder="请选择类型">
              <Select.Option value="QUANTITATIVE">定量</Select.Option>
              <Select.Option value="QUALITATIVE">定性</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="unit" label="单位">
            <Input placeholder="如：分、%、元" />
          </Form.Item>
          <Form.Item name="defaultWeight" label="默认权重">
            <Input type="number" placeholder="0-100" min={0} max={100} />
          </Form.Item>
          <Form.Item name="description" label="指标说明">
            <TextArea rows={3} placeholder="请输入指标说明" />
          </Form.Item>
          <Form.Item name="calculationMethod" label="计算方法">
            <TextArea rows={3} placeholder="请输入计算方法" />
          </Form.Item>
          <Form.Item name="dataSource" label="数据来源">
            <Input placeholder="请输入数据来源" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select>
              <Select.Option value="ACTIVE">启用</Select.Option>
              <Select.Option value="INACTIVE">禁用</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default IndicatorManagePage;
