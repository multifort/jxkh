import React, { useState, useEffect } from 'react';
import { Card, InputNumber, Input, Button, Table, message, Spin, Divider, Tag } from 'antd';
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { planService } from '../../services/planService';
import { scoreService } from '../../services/scoreService';
import type { PlanDetailDTO } from '../../types/performance';
import dayjs from 'dayjs';

const { TextArea } = Input;

/**
 * 员工自评表单页面
 */
const SelfEvaluationFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { planId } = useParams<{ planId: string }>();
  const [loading, setLoading] = useState(false);
  const [plan, setPlan] = useState<PlanDetailDTO | null>(null);
  const [scores, setScores] = useState<Record<number, { score: number; comment: string }>>({});

  useEffect(() => {
    if (planId) {
      loadPlanDetail(parseInt(planId));
    }
  }, [planId]);

  const loadPlanDetail = async (id: number) => {
    setLoading(true);
    try {
      const response: any = await planService.getPlanById(id);
      const data = response.data?.data || response.data;
      setPlan(data);
      
      // 初始化评分表单
      if (data && data.indicators) {
        const initialScores: Record<number, { score: number; comment: string }> = {};
        data.indicators.forEach((indicator: any) => {
          initialScores[indicator.id] = { score: 0, comment: '' };
        });
        setScores(initialScores);
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || error.message || '加载计划详情失败');
    } finally {
      setLoading(false);
    }
  };

  const handleScoreChange = (indicatorId: number, field: 'score' | 'comment', value: any) => {
    setScores(prev => ({
      ...prev,
      [indicatorId]: {
        ...prev[indicatorId],
        [field]: value
      }
    }));
  };

  const handleSubmit = async () => {
    try {
      // 验证所有指标都已评分
      if (!plan || !plan.indicators) {
        message.warning('计划数据未加载');
        return;
      }
      
      const unscored = plan.indicators.filter(
        ind => !scores[ind.id] || scores[ind.id].score === 0
      );
      
      if (unscored.length > 0) {
        message.warning('请为所有指标评分');
        return;
      }

      setLoading(true);
      
      // 批量提交评分
      const submitPromises = plan.indicators.map(indicator => {
        return scoreService.submitScore({
          planId: parseInt(planId!),
          indicatorInstanceId: indicator.id,
          score: scores[indicator.id].score,
          comment: scores[indicator.id].comment,
          type: 'SELF'
        });
      });

      await Promise.all(submitPromises);
      
      // 检查是否所有评分都已完成，如果是则触发分数计算
      try {
        const progress: any = await scoreService.getScoreProgress(parseInt(planId!));
        const progressData = progress.data?.data || progress.data;
        if (progressData?.allCompleted) {
          await scoreService.calculateScore(parseInt(planId!));
          message.success('自评提交成功，评分已全部完成，系统已自动计算最终得分');
        } else {
          message.success('自评提交成功');
        }
      } catch (calcError: any) {
        // 即使计算失败，也提示自评提交成功
        console.error('分数计算失败:', calcError);
        message.success('自评提交成功');
      }
      
      navigate('/performance/evaluation/self');
    } catch (error: any) {
      message.error(error.response?.data?.message || error.message || '提交失败');
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    {
      title: '指标名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
    },
    {
      title: '指标类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: string) => {
        const typeMap: Record<string, { color: string; text: string }> = {
          QUANTITATIVE: { color: 'blue', text: '定量' },
          QUALITATIVE: { color: 'green', text: '定性' },
        };
        const config = typeMap[type] || { color: 'default', text: type };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '权重',
      dataIndex: 'weight',
      key: 'weight',
      width: 100,
      render: (weight: number) => `${weight}%`,
    },
    {
      title: '目标值',
      dataIndex: 'targetValue',
      key: 'targetValue',
      width: 120,
      render: (value: number, record: any) => `${value}${record.unit || ''}`,
    },
    {
      title: '当前值',
      dataIndex: 'currentValue',
      key: 'currentValue',
      width: 120,
      render: (value: number, record: any) => `${value}${record.unit || ''}`,
    },
    {
      title: '完成进度',
      dataIndex: 'progress',
      key: 'progress',
      width: 120,
      render: (progress: number) => `${progress.toFixed(2)}%`,
    },
    {
      title: '自评分数',
      key: 'selfScore',
      width: 150,
      render: (_: any, record: any) => (
        <InputNumber
          min={0}
          max={100}
          precision={2}
          value={scores[record.id]?.score || 0}
          onChange={(value: number | null) => handleScoreChange(record.id, 'score', value || 0)}
          placeholder="0-100"
          style={{ width: '100%' }}
        />
      ),
    },
    {
      title: '自评说明',
      key: 'selfComment',
      render: (_: any, record: any) => (
        <TextArea
          rows={2}
          value={scores[record.id]?.comment || ''}
          onChange={(e) => handleScoreChange(record.id, 'comment', e.target.value)}
          placeholder="请输入自评说明（选填）"
        />
      ),
    },
  ];

  if (!plan) {
    return (
      <div style={{ padding: 24 }}>
        <Spin spinning={loading}>
          <Card>
            <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
              暂无数据
            </div>
          </Card>
        </Spin>
      </div>
    );
  }

  return (
    <div style={{ padding: 24 }}>
      <Card
        title={
          <div>
            <Button
              type="link"
              icon={<ArrowLeftOutlined />}
              onClick={() => navigate('/performance/evaluation/self')}
              style={{ marginRight: 8 }}
            />
            员工自评 - {plan.cycleName}
          </div>
        }
        extra={
          <div>
            <Tag color="purple">待评估</Tag>
            <span style={{ marginLeft: 8, color: '#999' }}>
              创建时间：{dayjs(plan.createdAt).format('YYYY-MM-DD HH:mm')}
            </span>
          </div>
        }
        bordered={false}
      >
        <Spin spinning={loading}>
          <div style={{ marginBottom: 24 }}>
            <h3>计划信息</h3>
            <p><strong>员工：</strong>{plan.employeeName}</p>
            <p><strong>周期：</strong>{plan.cycleName}</p>
            <p><strong>指标数量：</strong>{plan.indicators?.length || 0} 个</p>
          </div>

          <Divider />

          <div style={{ marginBottom: 16 }}>
            <h3>指标评分</h3>
            <p style={{ color: '#999', fontSize: 12 }}>请为每个指标打分（0-100分），并填写自评说明</p>
          </div>

          <Table
            dataSource={plan.indicators || []}
            columns={columns}
            rowKey="id"
            pagination={false}
            scroll={{ x: 1400 }}
            locale={{ emptyText: '暂无指标数据' }}
          />

          <Divider />

          <div style={{ textAlign: 'center', marginTop: 24 }}>
            <Button
              type="default"
              onClick={() => navigate('/performance/evaluation/self')}
              style={{ marginRight: 16 }}
            >
              取消
            </Button>
            <Button
              type="primary"
              icon={<SaveOutlined />}
              onClick={handleSubmit}
              loading={loading}
              size="large"
            >
              提交自评
            </Button>
          </div>
        </Spin>
      </Card>
    </div>
  );
};

export default SelfEvaluationFormPage;
