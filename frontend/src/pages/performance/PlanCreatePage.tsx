import React, { useState, useEffect } from 'react';
import {
  Steps,
  Form,
  Select,
  InputNumber,
  Button,
  Card,
  message,
  Space,
  Table,
  Alert,
} from 'antd';
import { DeleteOutlined, SaveOutlined } from '@ant-design/icons';
import { planService } from '../../services/planService';
import { cycleService } from '../../services/cycleService';
import { indicatorApi } from '../../services/indicatorService';
import type { PerformanceCycle } from '../../types/performance';
import type { Indicator } from '../../types/performance';
import type { IndicatorItemRequest } from '../../types/performance';

const { Step } = Steps;
const { Option } = Select;

/**
 * 绩效计划创建页面
 */
const PlanCreatePage: React.FC = () => {
  const [form] = Form.useForm();
  const [currentStep, setCurrentStep] = useState(0);
  const [cycles, setCycles] = useState<PerformanceCycle[]>([]);
  const [indicators, setIndicators] = useState<Indicator[]>([]);
  const [selectedIndicators, setSelectedIndicators] = useState<IndicatorItemRequest[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalWeight, setTotalWeight] = useState(0);

  // 加载周期列表
  useEffect(() => {
    loadCycles();
  }, []);

  // 加载指标列表
  useEffect(() => {
    if (currentStep === 1) {
      loadIndicators();
    }
  }, [currentStep]);

  // 计算权重总和
  useEffect(() => {
    const total = selectedIndicators.reduce((sum, item) => sum + (item.weight || 0), 0);
    setTotalWeight(total);
  }, [selectedIndicators]);

  // 草稿自动保存
  useEffect(() => {
    if (selectedIndicators.length > 0) {
      const timer = setTimeout(() => {
        saveDraft();
      }, 2000);
      return () => clearTimeout(timer);
    }
  }, [selectedIndicators]);

  const loadCycles = async () => {
    try {
      const response = await cycleService.getCycles(0, 100);
      console.log('周期API响应:', response);
      
      // cycleService返回response.data (ApiResponse)，所以response.data.data.content是周期数组
      const cyclesData = response.data?.data?.content || [];
      console.log('原始周期数据:', cyclesData);
      
      // 只显示进行中的周期（过滤掉DRAFT和ENDED）
      const activeCycles = cyclesData.filter((cycle: any) => cycle.status === 'IN_PROGRESS');
      console.log('过滤后的周期数据:', activeCycles);
      
      setCycles(activeCycles);
    } catch (error) {
      console.error('加载周期列表失败', error);
      message.error('加载周期列表失败');
    }
  };

  const loadIndicators = async () => {
    try {
      const response = await indicatorApi.list(undefined, undefined, undefined, undefined, 0, 100);
      // indicatorApi returns full AxiosResponse<ApiResponse<T>>, so response.data is ApiResponse
      if (response.data?.data?.content) {
        setIndicators(response.data.data.content);
      }
    } catch (error) {
      message.error('加载指标列表失败');
    }
  };

  // 恢复草稿
  useEffect(() => {
    const draft = localStorage.getItem('plan_draft');
    if (draft) {
      try {
        const draftData = JSON.parse(draft);
        setSelectedIndicators(draftData.indicators || []);
        form.setFieldsValue({
          cycleId: draftData.cycleId,
        });
        message.info('已恢复草稿');
      } catch (e) {
        console.error('恢复草稿失败', e);
      }
    }
  }, []);

  // 保存草稿
  const saveDraft = () => {
    const cycleId = form.getFieldValue('cycleId');
    if (cycleId && selectedIndicators.length > 0) {
      const draftData = {
        cycleId,
        indicators: selectedIndicators,
      };
      localStorage.setItem('plan_draft', JSON.stringify(draftData));
      message.success('草稿已自动保存', 1);
    }
  };

  // 下一步
  const nextStep = async () => {
    try {
      if (currentStep === 0) {
        await form.validateFields(['cycleId']);
      }
      setCurrentStep(currentStep + 1);
    } catch (error) {
      message.error('请填写必填项');
    }
  };

  // 上一步
  const prevStep = () => {
    setCurrentStep(currentStep - 1);
  };

  // 添加指标
  const handleAddIndicator = (indicatorId: number) => {
    const indicator = indicators.find((i) => i.id === indicatorId);
    if (!indicator) return;

    // 检查是否已添加
    if (selectedIndicators.some((item) => item.indicatorId === indicatorId)) {
      message.warning('该指标已添加');
      return;
    }

    const newItem: IndicatorItemRequest = {
      indicatorId: indicator.id,
      ownerId: 1, // TODO: 从当前用户获取
      name: indicator.name,
      type: indicator.type,
      weight: 0,
      targetValue: undefined,
      unit: indicator.unit,
      remark: '',
    };

    setSelectedIndicators([...selectedIndicators, newItem]);
  };

  // 删除指标
  const handleRemoveIndicator = (indicatorId: number) => {
    setSelectedIndicators(selectedIndicators.filter((item) => item.indicatorId !== indicatorId));
  };

  // 更新指标权重
  const handleWeightChange = (indicatorId: number, weight: number | null) => {
    setSelectedIndicators(
      selectedIndicators.map((item) =>
        item.indicatorId === indicatorId ? { ...item, weight: weight || 0 } : item
      )
    );
  };

  // 更新目标值
  const handleTargetValueChange = (indicatorId: number, value: number | null) => {
    setSelectedIndicators(
      selectedIndicators.map((item) =>
        item.indicatorId === indicatorId ? { ...item, targetValue: value || undefined } : item
      )
    );
  };

  // 提交计划
  const handleSubmit = async () => {
    const cycleId = form.getFieldValue('cycleId');

    // 校验权重
    if (Math.abs(totalWeight - 100) > 0.01) {
      message.error(`权重总和必须等于100%，当前为${totalWeight.toFixed(2)}%`);
      return;
    }

    if (selectedIndicators.length === 0) {
      message.error('请至少添加一个指标');
      return;
    }

    setLoading(true);
    try {
      await planService.createPlan({
        userId: 1, // TODO: 从当前用户获取
        cycleId,
        indicators: selectedIndicators,
      });

      message.success('计划创建成功');
      localStorage.removeItem('plan_draft');
      // TODO: 跳转到计划详情页
    } catch (error: any) {
      message.error(error.response?.data?.message || '创建失败');
    } finally {
      setLoading(false);
    }
  };

  // 指标表格列定义
  const columns = [
    {
      title: '指标名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => (type === 'QUANTITATIVE' ? '定量' : '定性'),
    },
    {
      title: '单位',
      dataIndex: 'unit',
      key: 'unit',
      render: (unit: string) => unit || '-',
    },
    {
      title: '权重 (%)',
      key: 'weight',
      render: (_: any, record: IndicatorItemRequest) => (
        <InputNumber
          min={0}
          max={100}
          precision={2}
          value={record.weight}
          onChange={(value) => handleWeightChange(record.indicatorId, value)}
          style={{ width: 100 }}
        />
      ),
    },
    {
      title: '目标值',
      key: 'targetValue',
      render: (_: any, record: IndicatorItemRequest) => (
        <InputNumber
          min={0}
          precision={2}
          value={record.targetValue}
          onChange={(value) => handleTargetValueChange(record.indicatorId, value)}
          placeholder="请输入"
          style={{ width: 120 }}
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: IndicatorItemRequest) => (
        <Button
          type="link"
          danger
          icon={<DeleteOutlined />}
          onClick={() => handleRemoveIndicator(record.indicatorId)}
        >
          删除
        </Button>
      ),
    },
  ];

  // 权重校验提示
  const isWeightValid = Math.abs(totalWeight - 100) < 0.01;
  const weightAlert = !isWeightValid && selectedIndicators.length > 0 ? (
    <Alert
      message={`权重总和: ${totalWeight.toFixed(2)}%`}
      description="权重总和必须等于100%"
      type="error"
      showIcon
      style={{ marginBottom: 16 }}
    />
  ) : null;

  return (
    <div style={{ padding: 24 }}>
      <Card title="创建绩效计划">
        <Steps current={currentStep} style={{ marginBottom: 32 }}>
          <Step title="选择周期" />
          <Step title="选择指标" />
          <Step title="设置目标" />
          <Step title="确认提交" />
        </Steps>

        {/* Step 1: 选择周期 */}
        {currentStep === 0 && (
          <Form form={form} layout="vertical">
            <Form.Item
              label="绩效周期"
              name="cycleId"
              rules={[{ required: true, message: '请选择绩效周期' }]}
            >
              <Select placeholder="请选择绩效周期" style={{ width: '100%' }}>
                {cycles.map((cycle) => (
                  <Option key={cycle.id} value={cycle.id}>
                    {cycle.name} ({cycle.startDate} ~ {cycle.endDate})
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Form>
        )}

        {/* Step 2: 选择指标 */}
        {currentStep === 1 && (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            <div>
              <label style={{ marginRight: 8 }}>添加指标：</label>
              <Select
                placeholder="搜索并选择指标"
                style={{ width: 300 }}
                showSearch
                filterOption={(input, option) =>
                  (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())
                }
                onChange={handleAddIndicator}
                value={null}
              >
                {indicators
                  .filter((i) => !selectedIndicators.some((s) => s.indicatorId === i.id))
                  .map((indicator) => (
                    <Option key={indicator.id} value={indicator.id}>
                      {indicator.name}
                    </Option>
                  ))}
              </Select>
            </div>

            <Table
              dataSource={selectedIndicators}
              columns={columns}
              rowKey="indicatorId"
              pagination={false}
              locale={{ emptyText: '请添加指标' }}
            />
          </Space>
        )}

        {/* Step 3: 设置目标（已在Step 2的表格中完成） */}
        {currentStep === 2 && (
          <div>
            {weightAlert}
            <Table
              dataSource={selectedIndicators}
              columns={columns}
              rowKey="indicatorId"
              pagination={false}
            />
            <div style={{ marginTop: 16, textAlign: 'right', fontSize: 16 }}>
              <strong>权重总和: {totalWeight.toFixed(2)}%</strong>
              {isWeightValid ? (
                <span style={{ color: '#52c41a', marginLeft: 8 }}>✓</span>
              ) : (
                <span style={{ color: '#ff4d4f', marginLeft: 8 }}>✗</span>
              )}
            </div>
          </div>
        )}

        {/* Step 4: 确认提交 */}
        {currentStep === 3 && (
          <div>
            {weightAlert}
            <h3>计划概览</h3>
            <p>
              <strong>周期：</strong>
              {cycles.find((c) => c.id === form.getFieldValue('cycleId'))?.name}
            </p>
            <p>
              <strong>指标数量：</strong>
              {selectedIndicators.length}
            </p>
            <p>
              <strong>权重总和：</strong>
              {totalWeight.toFixed(2)}%
            </p>
            <Table
              dataSource={selectedIndicators}
              columns={columns}
              rowKey="indicatorId"
              pagination={false}
            />
          </div>
        )}

        {/* 操作按钮 */}
        <div style={{ marginTop: 32, textAlign: 'center' }}>
          <Space>
            {currentStep > 0 && (
              <Button onClick={prevStep}>上一步</Button>
            )}
            {currentStep < 3 && (
              <Button type="primary" onClick={nextStep}>
                下一步
              </Button>
            )}
            {currentStep === 3 && (
              <Button
                type="primary"
                loading={loading}
                onClick={handleSubmit}
                disabled={!isWeightValid || selectedIndicators.length === 0}
              >
                提交计划
              </Button>
            )}
            <Button icon={<SaveOutlined />} onClick={saveDraft}>
              保存草稿
            </Button>
          </Space>
        </div>
      </Card>
    </div>
  );
};

export default PlanCreatePage;
