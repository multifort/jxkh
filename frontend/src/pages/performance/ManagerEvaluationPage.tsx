import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Tag, Progress, message, Spin } from 'antd';
import { EditOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { planService } from '../../services/planService';
import type { PlanListDTO } from '../../types/performance';
import dayjs from 'dayjs';

/**
 * 上级评分汇总页面
 * 显示当前主管所有待评分的下属计划列表
 */
const ManagerEvaluationPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [plans, setPlans] = useState<PlanListDTO[]>([]);

  useEffect(() => {
    loadPlans();
  }, []);

  const loadPlans = async () => {
    setLoading(true);
    try {
      // 查询所有待评估和已评估状态的计划
      const response: any = await planService.listPlans({ 
        status: 'PENDING_EVAL' 
      });
      const data = response.data?.data || response.data;
      
      // 过滤出当前主管下属的计划（这里简化处理，实际应该根据 manager_id 查询）
      const managerPlans = data.records || [];
      
      setPlans(managerPlans);
    } catch (error: any) {
      message.error(error.message || '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  const handleEvaluate = (planId: number) => {
    navigate(`/performance/evaluation/manager/${planId}`);
  };

  const columns = [
    {
      title: '计划ID',
      dataIndex: 'id',
      key: 'id',
      width: 100,
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
      title: '所属组织',
      dataIndex: 'orgName',
      key: 'orgName',
      width: 150,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: string) => {
        const statusMap: Record<string, { color: string; text: string }> = {
          PENDING_EVAL: { color: 'purple', text: '待评估' },
          EVALUATED: { color: 'cyan', text: '已评估' },
        };
        const config = statusMap[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '自评进度',
      key: 'selfProgress',
      width: 200,
      render: (_: any, record: PlanListDTO) => {
        const total = record.totalIndicators || 0;
        const scored = record.selfScoredCount || 0;
        const percent = total > 0 ? Math.round((scored / total) * 100) : 0;
        
        return (
          <div>
            <Progress percent={percent} size="small" status={percent === 100 ? 'success' : 'active'} />
            <span style={{ fontSize: 12, color: '#999' }}>{scored}/{total} 指标</span>
          </div>
        );
      },
    },
    {
      title: '上级评进度',
      key: 'managerProgress',
      width: 200,
      render: (_: any, record: PlanListDTO) => {
        const total = record.totalIndicators || 0;
        const scored = record.managerScoredCount || 0;
        const percent = total > 0 ? Math.round((scored / total) * 100) : 0;
        
        return (
          <div>
            <Progress percent={percent} size="small" status={percent === 100 ? 'success' : 'active'} />
            <span style={{ fontSize: 12, color: '#999' }}>{scored}/{total} 指标</span>
          </div>
        );
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: any, record: PlanListDTO) => (
        <Button
          type="primary"
          icon={<EditOutlined />}
          onClick={() => handleEvaluate(record.id)}
        >
          去评分
        </Button>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card bordered={false}>
        <Spin spinning={loading}>
          {plans.length === 0 ? (
            <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
              <p>暂无待评分的计划</p>
            </div>
          ) : (
            <Table
              dataSource={plans}
              columns={columns}
              rowKey="id"
              pagination={{ pageSize: 10 }}
            />
          )}
        </Spin>
      </Card>
    </div>
  );
};

export default ManagerEvaluationPage;
