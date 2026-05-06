import React, { useState, useEffect } from 'react';
import {
  Card,
  Descriptions,
  Table,
  Tag,
  Button,
  message,
  Spin,
} from 'antd';
import { ArrowLeftOutlined, LineChartOutlined, EditOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { planService } from '../../services/planService';
import type { PlanDetailDTO, PlanStatus } from '../../types/performance';
import dayjs from 'dayjs';

/**
 * 绩效计划详情页面
 */
const PlanDetailPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [plan, setPlan] = useState<PlanDetailDTO | null>(null);
  const [loading, setLoading] = useState(false);

  // 获取当前用户信息
  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
  const userRole = currentUser.roles?.[0]?.code || '';

  useEffect(() => {
    if (id) {
      loadPlanDetail(Number(id));
    }
  }, [id]);

  const loadPlanDetail = async (planId: number) => {
    setLoading(true);
    try {
      const response: any = await planService.getPlanById(planId);
      // 修复：ApiResponse 结构是 { code, message, data }
      const planData = response.data?.data || response.data;
      if (planData) {
        setPlan(planData);
        console.log('计划详情数据:', planData); // 调试用
      }
    } catch (error) {
      message.error('加载计划详情失败');
    } finally {
      setLoading(false);
    }
  };

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

  if (loading || !plan) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        <Spin size="large" />
      </div>
    );
  }

  // 指标表格列定义
  const indicatorColumns = [
    {
      title: '指标名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '指标类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => {
        const typeMap: Record<string, string> = {
          QUANTITATIVE: '定量',
          QUALITATIVE: '定性',
        };
        return typeMap[type] || type;
      },
    },
    {
      title: '权重',
      dataIndex: 'weight',
      key: 'weight',
      render: (weight: number) => `${weight}%`,
    },
    {
      title: '目标值',
      dataIndex: 'targetValue',
      key: 'targetValue',
      render: (value: number | null) => value?.toFixed(2) || '-',
    },
    {
      title: '当前值',
      dataIndex: 'currentValue',
      key: 'currentValue',
      render: (value: number | null) => value?.toFixed(2) || '-',
    },
    {
      title: '进度',
      dataIndex: 'progress',
      key: 'progress',
      render: (progress: number | null) => progress ? `${progress}%` : '-',
    },
    {
      title: '单位',
      dataIndex: 'unit',
      key: 'unit',
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      {/* 返回按钮 */}
      <Button
        type="link"
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/performance/plans')}
        style={{ marginBottom: 16 }}
      >
        返回列表
      </Button>

      {/* 基本信息 */}
      <Card 
        title="计划基本信息" 
        style={{ marginBottom: 16 }}
        extra={
          <div style={{ display: 'flex', gap: 8 }}>
            {/* 进度跟踪按钮 - 执行中状态 */}
            {plan.status === 'IN_PROGRESS' && (
              <Button 
                type="primary" 
                icon={<LineChartOutlined />}
                onClick={() => navigate(`/performance/tracking/${plan.id}`)}
              >
                进度跟踪
              </Button>
            )}
            
            {/* 自评按钮 - 待评估状态且当前用户是员工本人 */}
            {plan.status === 'PENDING_EVAL' && currentUser.id === plan.userId && (
              <Button 
                type="primary" 
                icon={<EditOutlined />}
                onClick={() => navigate(`/performance/evaluation/self/${plan.id}`)}
              >
                去自评
              </Button>
            )}
            
            {/* 上级评分按钮 - 待评估或已评估状态且当前用户是主管 */}
            {(plan.status === 'PENDING_EVAL' || plan.status === 'EVALUATED') && 
             (userRole === 'MANAGER' || userRole === 'ADMIN') && (
              <Button 
                type="primary" 
                icon={<UserOutlined />}
                onClick={() => navigate(`/performance/evaluation/manager/${plan.id}`)}
              >
                去评分
              </Button>
            )}
          </div>
        }
      >
        <Descriptions column={2} bordered>
          <Descriptions.Item label="计划ID">{plan.id}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={getStatusColor(plan.status)}>{getStatusText(plan.status)}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="员工姓名">{plan.employeeName}</Descriptions.Item>
          <Descriptions.Item label="周期名称">{plan.cycleName}</Descriptions.Item>
          <Descriptions.Item label="所属组织">{plan.orgName}</Descriptions.Item>
          <Descriptions.Item label="总分">
            {plan.totalScore?.toFixed(2) || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="最终等级">
            {plan.finalLevel || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">
            {dayjs(plan.createdAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
          <Descriptions.Item label="更新时间">
            {dayjs(plan.updatedAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
          <Descriptions.Item label="提交时间" span={2}>
            {plan.submittedAt ? dayjs(plan.submittedAt).format('YYYY-MM-DD HH:mm') : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="审批时间" span={2}>
            {plan.approvedAt ? dayjs(plan.approvedAt).format('YYYY-MM-DD HH:mm') : '-'}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 审批意见 */}
      {plan.comment && (
        <Card title="审批意见" style={{ marginBottom: 16 }}>
          <p>{plan.comment}</p>
        </Card>
      )}

      {/* 指标列表 */}
      <Card title="指标明细">
        <Table
          columns={indicatorColumns}
          dataSource={plan.indicators || []}
          rowKey="id"
          pagination={false}
          size="middle"
        />
      </Card>
    </div>
  );
};

export default PlanDetailPage;
