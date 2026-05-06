import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Table,
  Button,
  Tag,
  Space,
  Modal,
  message,
  Tabs,
  Badge,
  Spin,
  Drawer,
  List,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  FileTextOutlined,
  CalendarOutlined,
  TrophyOutlined,
  ArrowLeftOutlined,
  BellOutlined,
} from '@ant-design/icons';
import { recordService, riskService } from '../../services/recordService';
import RecordFormModal from '../../components/business/RecordFormModal';
import type { ColumnsType } from 'antd/es/table';

interface PerformanceRecord {
  id: number;
  planId: number;
  userId: number;
  type: string;
  content: string;
  progress?: number;
  attachments?: string;
  recordDate: string;
  aiSummary?: string;
  createdAt: string;
}

interface RiskAssessment {
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  expectedProgress: number;
  actualProgress: number;
  progressDelay: number;
  staleIndicatorCount: number;
}

const ProgressTrackingPage: React.FC = () => {
  const { planId: planIdParam } = useParams<{ planId: string }>();
  const navigate = useNavigate();
  
  const [records, setRecords] = useState<PerformanceRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<PerformanceRecord | null>(null);
  const [activeTab, setActiveTab] = useState('WEEKLY_REPORT');
  const [riskInfo, setRiskInfo] = useState<RiskAssessment | null>(null);
  const [riskDrawerVisible, setRiskDrawerVisible] = useState(false);
  const [riskIndicators, setRiskIndicators] = useState<any[]>([]);
  const [riskLoading, setRiskLoading] = useState(false);

  // 从路由参数获取 planId
  const planId = planIdParam ? parseInt(planIdParam) : 0;

  // 加载数据
  useEffect(() => {
    if (!planId || planId <= 0) {
      navigate('/performance/plans');
      return;
    }
    
    loadRecords();
    loadRiskInfo();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // tab 切换时重新加载
  useEffect(() => {
    if (planId && planId > 0) {
      loadRecords();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  const loadRecords = async () => {
    setLoading(true);
    try {
      const response = await recordService.getRecords({
        planId,
        type: activeTab,
        page: 0,
        size: 100,
      });
      // 修复：ApiResponse 结构是 { code, message, data }
      const pageData = response.data?.data || response.data;
      setRecords(pageData?.content || []);
    } catch (error: any) {
      console.error('加载记录失败:', error);
      message.error(error.response?.data?.message || '加载记录失败');
    } finally {
      setLoading(false);
    }
  };

  const loadRiskInfo = async () => {
    try {
      const response = await riskService.getPlanRisks(planId);
      const riskData = response.data?.data || response.data;
      setRiskInfo(riskData);
    } catch (error) {
      // 风险信息加载失败不影响主功能
      console.warn('加载风险信息失败:', error);
    }
  };

  const handleBack = () => {
    navigate(`/performance/plans/${planId}`);
  };

  const loadRiskIndicators = async () => {
    setRiskLoading(true);
    try {
      const response = await riskService.getRiskIndicators(planId);
      const data = response.data?.data || response.data;
      setRiskIndicators(data || []);
    } catch (error) {
      console.error('加载风险预警失败:', error);
    } finally {
      setRiskLoading(false);
    }
  };

  const handleOpenRiskDrawer = () => {
    loadRiskIndicators();
    setRiskDrawerVisible(true);
  };

  const handleCreate = () => {
    setEditingRecord(null);
    setModalVisible(true);
  };

  const handleEdit = (record: PerformanceRecord) => {
    setEditingRecord(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这条记录吗？',
      onOk: async () => {
        try {
          await recordService.deleteRecord(id);
          message.success('删除成功');
          loadRecords();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  const handleModalSuccess = () => {
    setModalVisible(false);
    loadRecords();
  };

  const getTypeTag = (type: string) => {
    const typeMap: Record<string, { color: string; text: string }> = {
      WEEKLY_REPORT: { color: 'blue', text: '周报' },
      MONTHLY_REPORT: { color: 'green', text: '月报' },
      MILESTONE: { color: 'purple', text: '里程碑' },
      ACHIEVEMENT: { color: 'gold', text: '成果' },
    };
    const config = typeMap[type] || { color: 'default', text: type };
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  const getRiskBadge = () => {
    if (!riskInfo) return null;

    const riskConfig = {
      LOW: { color: 'success', text: '低风险' },
      MEDIUM: { color: 'warning', text: '中风险' },
      HIGH: { color: 'error', text: '高风险' },
    };

    const config = riskConfig[riskInfo.riskLevel];
    return (
      <Badge status={config.color as any} text={`${config.text}（滞后 ${riskInfo.progressDelay.toFixed(1)}%）`} />
    );
  };

  const columns: ColumnsType<PerformanceRecord> = [
    {
      title: '记录类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: string) => getTypeTag(type),
    },
    {
      title: '记录日期',
      dataIndex: 'recordDate',
      key: 'recordDate',
      width: 120,
    },
    {
      title: '进度',
      dataIndex: 'progress',
      key: 'progress',
      width: 100,
      render: (progress?: number) => (progress ? `${progress}%` : '-'),
    },
    {
      title: '内容摘要',
      dataIndex: 'content',
      key: 'content',
      ellipsis: true,
      render: (content: string) => (
        <div dangerouslySetInnerHTML={{ __html: content.substring(0, 100) + '...' }} />
      ),
    },
    {
      title: 'AI 总结',
      dataIndex: 'aiSummary',
      key: 'aiSummary',
      width: 200,
      ellipsis: true,
      render: (summary?: string) => summary || '-',
    },
    {
      title: '附件',
      dataIndex: 'attachments',
      key: 'attachments',
      width: 100,
      render: (attachments?: string) => {
        if (!attachments) return '-';
        try {
          const urls = JSON.parse(attachments);
          return <Tag color="cyan">{urls.length} 个文件</Tag>;
        } catch {
          return '-';
        }
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  const tabItems = [
    {
      key: 'WEEKLY_REPORT',
      label: (
        <span>
          <FileTextOutlined />
          周报
        </span>
      ),
    },
    {
      key: 'MONTHLY_REPORT',
      label: (
        <span>
          <CalendarOutlined />
          月报
        </span>
      ),
    },
    {
      key: 'MILESTONE',
      label: (
        <span>
          <TrophyOutlined />
          里程碑
        </span>
      ),
    },
    {
      key: 'ACHIEVEMENT',
      label: (
        <span>
          <TrophyOutlined />
          成果
        </span>
      ),
    },
  ];

  return (
    <div>
      {/* 返回按钮 */}
      <Button
        type="link"
        icon={<ArrowLeftOutlined />}
        onClick={handleBack}
        style={{ marginBottom: 16, padding: 0 }}
      >
        返回计划详情
      </Button>

      {loading && records.length === 0 ? (
        <div style={{ padding: 24, textAlign: 'center' }}>
          <Spin size="large" tip="加载中..." />
        </div>
      ) : (
        <Card
        title={
          <Space>
            <span>进度跟踪</span>
            {getRiskBadge()}
            {/* 风险预警标识 */}
            <Badge 
              count={riskInfo?.staleIndicatorCount || 0} 
              overflowCount={99}
              style={{ marginLeft: 8 }}
            >
              <Button
                type="text"
                icon={<BellOutlined style={{ fontSize: 18, color: riskInfo?.riskLevel === 'HIGH' ? '#ff4d4f' : '#faad14' }} />}
                onClick={handleOpenRiskDrawer}
              />
            </Badge>
          </Space>
        }
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新建记录
          </Button>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={tabItems}
          style={{ marginBottom: 16 }}
        />

        <Table
          columns={columns}
          dataSource={records}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>
      )}

      <RecordFormModal
        visible={modalVisible}
        editingRecord={editingRecord}
        planId={planId}
        onSuccess={handleModalSuccess}
        onCancel={() => setModalVisible(false)}
      />
      
      {/* 风险预警列表 */}
      <Drawer
        title="风险预警"
        placement="left"
        onClose={() => setRiskDrawerVisible(false)}
        open={riskDrawerVisible}
        width={400}
      >
        <Spin spinning={riskLoading}>
          {riskIndicators.length > 0 ? (
            <List
              dataSource={riskIndicators}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Space>
                        <Tag color="red">延期风险</Tag>
                        <span>{item.name}</span>
                      </Space>
                    }
                    description={
                      <div>
                        <div>当前进度: {item.progress?.toFixed(2) || 0}%</div>
                        <div>目标值: {item.targetValue?.toFixed(2) || '-'}</div>
                        <div>当前值: {item.currentValue?.toFixed(2) || '-'}</div>
                        {item.remark && <div style={{ marginTop: 4 }}>备注: {item.remark}</div>}
                      </div>
                    }
                  />
                </List.Item>
              )}
            />
          ) : (
            <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
              <BellOutlined style={{ fontSize: 48, marginBottom: 16 }} />
              <div>暂无风险预警</div>
              <div style={{ fontSize: 12, marginTop: 8 }}>所有指标进度正常</div>
            </div>
          )}
        </Spin>
      </Drawer>
    </div>
  );
};

export default ProgressTrackingPage;
