import React, { useEffect, useState } from 'react';
import { 
  Card, List, Button, Tag, Space, message, Empty, Popconfirm, Badge, Modal, Descriptions 
} from 'antd';
import { 
  BellOutlined, CheckOutlined, MailOutlined, 
  WarningOutlined, InfoCircleOutlined, EyeOutlined 
} from '@ant-design/icons';
import { notificationService, Notification } from '../../services/notificationService';
import dayjs from 'dayjs';

const NotificationCenterPage: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [unreadCount, setUnreadCount] = useState(0);
  
  // 详情弹窗状态
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [currentNotification, setCurrentNotification] = useState<Notification | null>(null);

  // 加载通知列表
  const loadNotifications = async () => {
    setLoading(true);
    try {
      const response: any = await notificationService.getNotifications(currentPage - 1, pageSize);
      if (response.data?.data) {
        setNotifications(response.data.data.content || []);
        setTotal(response.data.data.totalElements || 0);
      }
    } catch (error) {
      message.error('加载通知列表失败');
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  // 加载未读数量
  const loadUnreadCount = async () => {
    try {
      const response: any = await notificationService.getUnreadCount();
      let count = 0;
      if (response?.data) {
        count = response.data.data !== undefined ? response.data.data : response.data;
      }
      setUnreadCount(typeof count === 'number' ? count : 0);
    } catch (error) {
      console.error('加载未读数量失败', error);
      setUnreadCount(0);
    }
  };

  useEffect(() => {
    loadNotifications();
    loadUnreadCount();
  }, [currentPage, pageSize]);

  // 标记单条已读
  const handleMarkAsRead = async (id: number) => {
    try {
      await notificationService.markAsRead(id);
      message.success('已标记为已读');
      loadNotifications();
      loadUnreadCount();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 全部标记已读
  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      message.success('已全部标记为已读');
      loadNotifications();
      loadUnreadCount();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 查看详情
  const handleViewDetail = (notification: Notification) => {
    setCurrentNotification(notification);
    setDetailModalVisible(true);
    
    // 如果是未读通知，自动标记为已读
    if (!notification.isRead) {
      handleMarkAsRead(notification.id);
    }
  };

  // 获取通知类型标签
  const getTypeTag = (type: string) => {
    const typeMap: Record<string, { color: string; icon: React.ReactNode; text: string }> = {
      SYSTEM: { color: 'blue', icon: <InfoCircleOutlined />, text: '系统通知' },
      TASK: { color: 'green', icon: <CheckOutlined />, text: '任务提醒' },
      APPROVAL: { color: 'purple', icon: <MailOutlined />, text: '审批通知' },
      RISK_WARNING: { color: 'red', icon: <WarningOutlined />, text: '风险预警' },
    };
    const config = typeMap[type] || { color: 'default', icon: <BellOutlined />, text: type };
    return <Tag color={config.color} icon={config.icon}>{config.text}</Tag>;
  };

  // 获取类型文本
  const getTypeText = (type: string) => {
    const typeMap: Record<string, string> = {
      SYSTEM: '系统通知',
      TASK: '任务提醒',
      APPROVAL: '审批通知',
      RISK_WARNING: '风险预警',
    };
    return typeMap[type] || type;
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
        <Popconfirm
          title="确认全部标记为已读"
          description="是否要将所有未读通知标记为已读？"
          onConfirm={handleMarkAllAsRead}
          okText="确认"
          cancelText="取消"
          disabled={unreadCount === 0}
        >
          <Button 
            type="primary" 
            icon={<CheckOutlined />}
            disabled={unreadCount === 0}
          >
            全部标记已读
            {unreadCount > 0 && (
              <Badge 
                count={unreadCount} 
                overflowCount={99} 
                style={{ marginLeft: 8 }} 
              />
            )}
          </Button>
        </Popconfirm>
      </div>

      <Card>
        <List
          loading={loading}
          dataSource={notifications}
          renderItem={(item) => (
            <List.Item
              style={{
                backgroundColor: item.isRead ? '#fff' : '#f0f7ff',
                borderLeft: item.isRead ? 'none' : '3px solid #1890ff',
                paddingLeft: item.isRead ? 0 : 12,
                cursor: 'pointer',
              }}
              onClick={() => handleViewDetail(item)}
              actions={[
                <Button
                  type="link"
                  size="small"
                  icon={<EyeOutlined />}
                  onClick={(e) => {
                    e.stopPropagation();
                    handleViewDetail(item);
                  }}
                >
                  查看详情
                </Button>,
                !item.isRead && (
                  <Button
                    type="link"
                    size="small"
                    icon={<CheckOutlined />}
                    onClick={(e) => {
                      e.stopPropagation();
                      handleMarkAsRead(item.id);
                    }}
                  >
                    标记已读
                  </Button>
                ),
              ]}
            >
              <List.Item.Meta
                title={
                  <Space>
                    {getTypeTag(item.type)}
                    <span style={{ fontWeight: item.isRead ? 'normal' : 'bold' }}>
                      {item.title}
                    </span>
                  </Space>
                }
                description={
                  <div>
                    <div style={{ marginTop: 8, color: '#666', marginBottom: 4 }}>
                      {item.content.length > 100 ? item.content.substring(0, 100) + '...' : item.content}
                    </div>
                    <div style={{ fontSize: 12, color: '#999' }}>
                      {dayjs(item.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                    </div>
                  </div>
                }
              />
            </List.Item>
          )}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size);
            },
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          locale={{
            emptyText: <Empty description="暂无通知" />,
          }}
        />
      </Card>

      {/* 详情弹窗 */}
      <Modal
        title={
          <Space>
            {currentNotification && getTypeTag(currentNotification.type)}
            <span>{currentNotification?.title}</span>
          </Space>
        }
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            关闭
          </Button>,
        ]}
        width={700}
      >
        {currentNotification && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="通知类型">
              {getTypeText(currentNotification.type)}
            </Descriptions.Item>
            <Descriptions.Item label="标题">
              {currentNotification.title}
            </Descriptions.Item>
            <Descriptions.Item label="内容">
              <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.8 }}>
                {currentNotification.content}
              </div>
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={currentNotification.isRead ? 'default' : 'processing'}>
                {currentNotification.isRead ? '已读' : '未读'}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {dayjs(currentNotification.createdAt).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
            {currentNotification.readAt && (
              <Descriptions.Item label="阅读时间">
                {dayjs(currentNotification.readAt).format('YYYY-MM-DD HH:mm:ss')}
              </Descriptions.Item>
            )}
            {currentNotification.relatedId && (
              <Descriptions.Item label="关联对象">
                {currentNotification.relatedType}: ID {currentNotification.relatedId}
              </Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default NotificationCenterPage;
