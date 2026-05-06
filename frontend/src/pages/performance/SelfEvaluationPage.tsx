import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Tag, message, Spin } from 'antd';
import { EditOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { planService } from '../../services/planService';
import type { PlanListDTO } from '../../types/performance';
import dayjs from 'dayjs';

/**
 * 员工自评汇总页面
 * 显示当前用户所有待自评的计划列表
 */
const SelfEvaluationPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [plans, setPlans] = useState<PlanListDTO[]>([]);

  useEffect(() => {
    loadPlans();
  }, []);

  const loadPlans = async () => {
    setLoading(true);
    try {
      // 查询所有待评估状态的计划
      const response: any = await planService.listPlans({ 
        page: 0,
        size: 100,
        status: 'PENDING_EVAL' 
      });
      const data = response.data?.data || response.data;
      
      // 过滤出当前用户的计划
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      // Spring Data JPA 返回的是 content 而不是 records
      const plansData = data.content || data.records || [];
      const userPlans = plansData.filter((plan: any) => plan.userId === currentUser.id);
      
      setPlans(userPlans);
    } catch (error: any) {
      message.error(error.message || '加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  const handleEvaluate = (planId: number) => {
    navigate(`/performance/evaluation/self/${planId}`);
  };

  const columns = [
    {
      title: '计划ID',
      dataIndex: 'id',
      key: 'id',
      width: 100,
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
          去自评
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
              <p>暂无待自评的计划</p>
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

export default SelfEvaluationPage;
