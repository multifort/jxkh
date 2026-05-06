import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Tag,
  Space,
  message,
  Modal,
  Input,
} from 'antd';
import { CheckOutlined, CloseOutlined, SendOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { planService } from '../../services/planService';
import { PlanStatus } from '../../types/performance';
import type { PlanListDTO } from '../../types/performance';
import dayjs from 'dayjs';

const { TextArea } = Input;

/**
 * 绩效计划列表页面
 */
const PlanListPage: React.FC = () => {
  const navigate = useNavigate();
  const [plans, setPlans] = useState<PlanListDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [statusFilter, setStatusFilter] = useState<PlanStatus | undefined>(undefined);
  
  // 审批对话框状态
  const [approveModalVisible, setApproveModalVisible] = useState(false);
  const [currentPlan, setCurrentPlan] = useState<PlanListDTO | null>(null);
  const [approveComment, setApproveComment] = useState('');
  const [approveLoading, setApproveLoading] = useState(false);

  // 加载计划列表
  useEffect(() => {
    loadPlans();
  }, [currentPage, pageSize, statusFilter]);

  const loadPlans = async () => {
    setLoading(true);
    try {
      const response = await planService.listPlans({
        page: currentPage - 1,
        size: pageSize,
        status: statusFilter,
      });
      
      if (response.data?.data) {
        setPlans(response.data.data.content || []);
        setTotal(response.data.data.totalElements || 0);
      }
    } catch (error) {
      message.error('加载计划列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 提交审批
  const handleSubmit = async (planId: number) => {
    Modal.confirm({
      title: '确认提交审批',
      content: '提交后将进入待审批状态，确定要提交吗？',
      okText: '确认提交',
      cancelText: '取消',
      onOk: async () => {
        try {
          await planService.submitPlan(planId);
          message.success('提交审批成功');
          loadPlans();
        } catch (error: any) {
          const errorCode = error.response?.data?.code;
          const errorMessage = error.response?.data?.message || '提交审批失败';
          
          // 检测并发冲突错误码
          if (errorCode === 'PLAN_CONCURRENT_UPDATE') {
            message.warning({
              content: '计划已被他人修改，正在重新加载最新数据...',
              duration: 2,
            });
            
            // 自动重新加载数据
            await loadPlans();
            
            // 提示用户重新操作
            setTimeout(() => {
              message.info('数据已更新，请重新提交');
            }, 500);
          } else {
            message.error(errorMessage);
          }
        }
      },
    });
  };

  // 打开审批对话框
  const handleOpenApproveModal = (plan: PlanListDTO) => {
    setCurrentPlan(plan);
    setApproveComment('');
    setApproveModalVisible(true);
  };

  // 审批计划
  const handleApprove = async (approved: boolean) => {
    if (!currentPlan) return;

    // 验证审批意见（驳回时必须填写）
    if (!approved && !approveComment.trim()) {
      message.warning('驳回时必须填写审批意见');
      return;
    }

    // 验证审批意见长度
    if (approveComment.length > 500) {
      message.warning('审批意见不能超过500字');
      return;
    }

    setApproveLoading(true);
    try {
      await planService.approvePlan(currentPlan.id, approved, approveComment);
      message.success(approved ? '审批通过' : '已驳回');
      setApproveModalVisible(false);
      loadPlans();
    } catch (error: any) {
      const errorCode = error.response?.data?.code;
      const errorMessage = error.response?.data?.message || '审批失败';
      
      // 检测并发冲突错误码
      if (errorCode === 'PLAN_CONCURRENT_UPDATE') {
        message.warning({
          content: '计划已被他人修改，正在重新加载最新数据...',
          duration: 2,
        });
        
        // 自动重新加载数据
        await loadPlans();
        
        // 关闭对话框
        setApproveModalVisible(false);
        
        // 提示用户重新操作
        setTimeout(() => {
          message.info('数据已更新，请重新审批');
        }, 500);
      } else {
        message.error(errorMessage);
      }
    } finally {
      setApproveLoading(false);
    }
  };

  // 状态标签颜色
  const getStatusColor = (status: PlanStatus) => {
    const colorMap: Record<PlanStatus, string> = {
      DRAFT: 'default',
      PENDING_SUBMIT: 'orange',
      PENDING_APPROVE: 'blue',
      IN_PROGRESS: 'green',
      PENDING_EVAL: 'purple',
      EVALUATED: 'cyan',
      CALIBRATED: 'geekblue',
      ARCHIVED: 'gray',
    };
    return colorMap[status] || 'default';
  };

  // 状态文本
  const getStatusText = (status: PlanStatus) => {
    const textMap: Record<PlanStatus, string> = {
      DRAFT: '草稿',
      PENDING_SUBMIT: '待提交',
      PENDING_APPROVE: '待审批',
      IN_PROGRESS: '执行中',
      PENDING_EVAL: '待评估',
      EVALUATED: '已评估',
      CALIBRATED: '已校准',
      ARCHIVED: '已归档',
    };
    return textMap[status] || status;
  };

  // 表格列定义
  const columns = [
    {
      title: '计划ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '员工姓名',
      dataIndex: 'employeeName',
      key: 'employeeName',
      width: 120,
    },
    {
      title: '周期名称',
      dataIndex: 'cycleName',
      key: 'cycleName',
      width: 150,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: PlanStatus) => (
        <Tag color={getStatusColor(status)}>{getStatusText(status)}</Tag>
      ),
    },
    {
      title: '总分',
      dataIndex: 'totalScore',
      key: 'totalScore',
      width: 100,
      render: (score: number | null) => score?.toFixed(2) || '-',
    },
    {
      title: '最终等级',
      dataIndex: 'finalLevel',
      key: 'finalLevel',
      width: 100,
    },
    {
      title: '提交时间',
      dataIndex: 'submittedAt',
      key: 'submittedAt',
      width: 160,
      render: (time: string | null) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-',
    },
    {
      title: '审批时间',
      dataIndex: 'approvedAt',
      key: 'approvedAt',
      width: 160,
      render: (time: string | null) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right' as const,
      render: (_: any, record: PlanListDTO) => (
        <Space>
          {/* 草稿状态显示提交按钮 */}
          {record.status === 'DRAFT' && (
            <Button
              type="primary"
              size="small"
              icon={<SendOutlined />}
              onClick={() => handleSubmit(record.id)}
            >
              提交审批
            </Button>
          )}
          
          {/* 待审批状态显示审批按钮（ADMIN/HR/MANAGER可见） */}
          {record.status === 'PENDING_APPROVE' && (
            <Button
              type="primary"
              size="small"
              icon={<CheckOutlined />}
              onClick={() => handleOpenApproveModal(record)}
            >
              审批
            </Button>
          )}
          
          {/* 其他状态显示详情按钮 */}
          {record.status !== 'DRAFT' && record.status !== 'PENDING_APPROVE' && (
            <Button 
              size="small"
              onClick={() => navigate(`/performance/plans/${record.id}`)}
            >
              详情
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      {/* 操作栏：左侧筛选器 + 右侧创建按钮 */}
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <Space>
          <Button
            type={!statusFilter ? 'primary' : 'default'}
            onClick={() => {
              setStatusFilter(undefined);
              setCurrentPage(1);
            }}
          >
            全部
          </Button>
          <Button
            type={statusFilter === PlanStatus.DRAFT ? 'primary' : 'default'}
            onClick={() => {
              setStatusFilter(PlanStatus.DRAFT);
              setCurrentPage(1);
            }}
          >
            草稿
          </Button>
          <Button
            type={statusFilter === PlanStatus.PENDING_APPROVE ? 'primary' : 'default'}
            onClick={() => {
              setStatusFilter(PlanStatus.PENDING_APPROVE);
              setCurrentPage(1);
            }}
          >
            待审批
          </Button>
          <Button
            type={statusFilter === PlanStatus.IN_PROGRESS ? 'primary' : 'default'}
            onClick={() => {
              setStatusFilter(PlanStatus.IN_PROGRESS);
              setCurrentPage(1);
            }}
          >
            执行中
          </Button>
          <Button
            type={statusFilter === PlanStatus.PENDING_EVAL ? 'primary' : 'default'}
            onClick={() => {
              setStatusFilter(PlanStatus.PENDING_EVAL);
              setCurrentPage(1);
            }}
          >
            待评估
          </Button>
          <Button
            type={statusFilter === PlanStatus.EVALUATED ? 'primary' : 'default'}
            onClick={() => {
              setStatusFilter(PlanStatus.EVALUATED);
              setCurrentPage(1);
            }}
          >
            已评估
          </Button>
          <Button
            type={statusFilter === PlanStatus.CALIBRATED ? 'primary' : 'default'}
            onClick={() => {
              setStatusFilter(PlanStatus.CALIBRATED);
              setCurrentPage(1);
            }}
          >
            已校准
          </Button>
          <Button
            type={statusFilter === PlanStatus.ARCHIVED ? 'primary' : 'default'}
            onClick={() => {
              setStatusFilter(PlanStatus.ARCHIVED);
              setCurrentPage(1);
            }}
          >
            已归档
          </Button>
        </Space>
        <Space>
          <Button
            type="default"
            icon={<ReloadOutlined />}
            onClick={loadPlans}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/performance/plans/create')}>
            创建计划
          </Button>
        </Space>
      </div>

      {/* 计划列表表格 */}
      <Table
          columns={columns}
          dataSource={plans}
          rowKey="id"
          loading={loading}
          pagination={{
            current: currentPage,
            pageSize,
            total,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size);
            },
          }}
        scroll={{ x: 1200 }}
      />

      {/* 审批对话框 */}
      <Modal
        title="审批绩效计划"
        open={approveModalVisible}
        onCancel={() => setApproveModalVisible(false)}
        footer={[
          <Button
            key="reject"
            danger
            icon={<CloseOutlined />}
            loading={approveLoading}
            onClick={() => handleApprove(false)}
          >
            驳回
          </Button>,
          <Button
            key="approve"
            type="primary"
            icon={<CheckOutlined />}
            loading={approveLoading}
            onClick={() => handleApprove(true)}
          >
            通过
          </Button>,
        ]}
      >
        {currentPlan && (
          <div>
            <p><strong>计划ID：</strong>{currentPlan.id}</p>
            <p><strong>员工姓名：</strong>{currentPlan.employeeName}</p>
            <p><strong>周期名称：</strong>{currentPlan.cycleName}</p>
            <p><strong>当前状态：</strong>{getStatusText(currentPlan.status)}</p>
            
            <div style={{ marginTop: 16 }}>
              <label style={{ display: 'block', marginBottom: 8 }}>
                <strong>审批意见：</strong>
              </label>
              <TextArea
                rows={4}
                placeholder="请输入审批意见（可选）"
                value={approveComment}
                onChange={(e) => setApproveComment(e.target.value)}
              />
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default PlanListPage;
