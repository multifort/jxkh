import React, { useEffect, useState } from 'react';
import { 
  Card, Table, Button, Modal, Form, Input, Select, DatePicker, 
  message, Space, Popconfirm, Tag
} from 'antd';
import { 
  PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined,
  StopOutlined, ReloadOutlined 
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { cycleService } from '../../services/cycleService';
import { PerformanceCycle, CycleStatus, CycleType } from '../../types/performance';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const CycleManagePage: React.FC = () => {
  const [cycles, setCycles] = useState<PerformanceCycle[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingCycle, setEditingCycle] = useState<PerformanceCycle | null>(null);
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [searchParams, setSearchParams] = useState<{
    keyword?: string;
    status?: CycleStatus;
  }>({});

  // 加载周期列表
  const loadCycles = async (page = 1, size = 10) => {
    try {
      setLoading(true);
      const response = await cycleService.getCycles(
        page - 1,
        size,
        searchParams.keyword,
        searchParams.status
      );
      
      setCycles(response.data?.content || []);
      setPagination({
        current: page,
        pageSize: size,
        total: response.data?.totalElements || 0,
      });
    } catch (error: any) {
      message.error(error.response?.data?.message || '加载周期列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCycles();
  }, []);

  // 搜索
  const handleSearch = (values: any) => {
    setSearchParams(values);
    loadCycles(1, pagination.pageSize);
  };

  // 重置搜索
  const handleReset = () => {
    setSearchParams({});
    form.resetFields(['keyword', 'status']);
    loadCycles(1, pagination.pageSize);
  };

  // 分页变化
  const handleTableChange = (newPagination: any) => {
    loadCycles(newPagination.current, newPagination.pageSize);
  };

  // 打开新增对话框
  const handleAdd = () => {
    setEditingCycle(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 打开编辑对话框
  const handleEdit = (cycle: PerformanceCycle) => {
    setEditingCycle(cycle);
    form.setFieldsValue({
      ...cycle,
      startDate: dayjs(cycle.startDate),
      endDate: dayjs(cycle.endDate),
    });
    setModalVisible(true);
  };

  // 删除周期
  const handleDelete = async (id: number) => {
    try {
      await cycleService.deleteCycle(id);
      message.success('删除成功');
      loadCycles(pagination.current, pagination.pageSize);
    } catch (error: any) {
      message.error(error.response?.data?.message || '删除失败');
    }
  };

  // 启动周期
  const handleStart = async (id: number) => {
    try {
      await cycleService.startCycle(id);
      message.success('周期已启动');
      loadCycles(pagination.current, pagination.pageSize);
    } catch (error: any) {
      message.error(error.response?.data?.message || '启动失败');
    }
  };

  // 结束周期
  const handleEnd = async (id: number) => {
    try {
      await cycleService.endCycle(id);
      message.success('周期已结束');
      loadCycles(pagination.current, pagination.pageSize);
    } catch (error: any) {
      message.error(error.response?.data?.message || '结束失败');
    }
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const cycleData = {
        ...values,
        startDate: values.startDate.format('YYYY-MM-DD'),
        endDate: values.endDate.format('YYYY-MM-DD'),
      };

      if (editingCycle) {
        await cycleService.updateCycle(editingCycle.id, cycleData);
        message.success('更新成功');
      } else {
        await cycleService.createCycle(cycleData);
        message.success('创建成功');
      }

      setModalVisible(false);
      loadCycles(pagination.current, pagination.pageSize);
    } catch (error: any) {
      if (error.errorFields) {
        return; // 表单验证错误
      }
      message.error(error.response?.data?.message || '操作失败');
    }
  };

  // 状态标签颜色
  const getStatusColor = (status: CycleStatus) => {
    switch (status) {
      case CycleStatus.DRAFT:
        return 'default';
      case CycleStatus.IN_PROGRESS:
        return 'processing';
      case CycleStatus.ENDED:
        return 'success';
      default:
        return 'default';
    }
  };

  // 状态文本
  const getStatusText = (status: CycleStatus) => {
    switch (status) {
      case CycleStatus.DRAFT:
        return '草稿';
      case CycleStatus.IN_PROGRESS:
        return '进行中';
      case CycleStatus.ENDED:
        return '已结束';
      default:
        return status;
    }
  };

  // 表格列定义
  const columns: ColumnsType<PerformanceCycle> = [
    {
      title: '周期名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: CycleType) => {
        const typeMap = {
          MONTHLY: '月度',
          QUARTERLY: '季度',
          ANNUAL: '年度',
        };
        return typeMap[type] || type;
      },
    },
    {
      title: '开始日期',
      dataIndex: 'startDate',
      key: 'startDate',
      width: 120,
    },
    {
      title: '结束日期',
      dataIndex: 'endDate',
      key: 'endDate',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: CycleStatus) => (
        <Tag color={getStatusColor(status)}>{getStatusText(status)}</Tag>
      ),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          {record.status === CycleStatus.DRAFT && (
            <>
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleEdit(record)}
              >
                编辑
              </Button>
              <Popconfirm
                title="确定删除该周期吗？"
                onConfirm={() => handleDelete(record.id)}
              >
                <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                  删除
                </Button>
              </Popconfirm>
              <Button
                type="link"
                size="small"
                icon={<PlayCircleOutlined />}
                onClick={() => handleStart(record.id)}
              >
                启动
              </Button>
            </>
          )}
          {record.status === CycleStatus.IN_PROGRESS && (
            <Button
              type="link"
              size="small"
              icon={<StopOutlined />}
              onClick={() => handleEnd(record.id)}
            >
              结束
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="绩效周期管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建周期
          </Button>
        }
      >
        {/* 搜索区域 */}
        <Form layout="inline" onFinish={handleSearch} style={{ marginBottom: 16 }}>
          <Form.Item name="keyword">
            <Input placeholder="搜索周期名称" allowClear style={{ width: 200 }} />
          </Form.Item>
          <Form.Item name="status">
            <Select placeholder="选择状态" allowClear style={{ width: 150 }}>
              <Option value={CycleStatus.DRAFT}>草稿</Option>
              <Option value={CycleStatus.IN_PROGRESS}>进行中</Option>
              <Option value={CycleStatus.ENDED}>已结束</Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">搜索</Button>
              <Button onClick={handleReset} icon={<ReloadOutlined />}>重置</Button>
            </Space>
          </Form.Item>
        </Form>

        {/* 表格 */}
        <Table
          columns={columns}
          dataSource={cycles}
          rowKey="id"
          loading={loading}
          pagination={pagination}
          onChange={handleTableChange}
          scroll={{ x: 1200 }}
        />
      </Card>

      {/* 新增/编辑对话框 */}
      <Modal
        title={editingCycle ? '编辑周期' : '新建周期'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="周期名称"
            rules={[{ required: true, message: '请输入周期名称' }]}
          >
            <Input placeholder="例如：2026年Q1" />
          </Form.Item>

          <Form.Item
            name="type"
            label="周期类型"
            rules={[{ required: true, message: '请选择周期类型' }]}
          >
            <Select placeholder="请选择">
              <Option value={CycleType.MONTHLY}>月度</Option>
              <Option value={CycleType.QUARTERLY}>季度</Option>
              <Option value={CycleType.ANNUAL}>年度</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="startDate"
            label="开始日期"
            rules={[{ required: true, message: '请选择开始日期' }]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="endDate"
            label="结束日期"
            rules={[{ required: true, message: '请选择结束日期' }]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="remark" label="备注">
            <TextArea rows={3} placeholder="请输入备注信息" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CycleManagePage;
