import React, { useState } from 'react';
import { Modal, Form, InputNumber, Input, message } from 'antd';
import { ScoreSubmitRequest } from '../../types/score';
import { scoreService } from '../../services/scoreService';

interface ScoreFormModalProps {
  open: boolean;
  planId: number;
  indicatorInstanceId: number;
  indicatorName: string;
  type: 'SELF' | 'MANAGER';
  onSuccess?: () => void;
  onCancel?: () => void;
}

const ScoreFormModal: React.FC<ScoreFormModalProps> = ({
  open,
  planId,
  indicatorInstanceId,
  indicatorName,
  type,
  onSuccess,
  onCancel,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data: ScoreSubmitRequest = {
        planId,
        indicatorInstanceId,
        score: values.score,
        comment: values.comment,
        type,
      };

      await scoreService.submitScore(data);
      message.success('评分提交成功');
      form.resetFields();
      onSuccess?.();
    } catch (error: any) {
      if (error.errorFields) {
        return; // 表单验证失败
      }
      message.error(error.message || '评分提交失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    form.resetFields();
    onCancel?.();
  };

  return (
    <Modal
      title={`${type === 'SELF' ? '自评' : '上级评'} - ${indicatorName}`}
      open={open}
      onOk={handleSubmit}
      onCancel={handleCancel}
      confirmLoading={loading}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="分数"
          name="score"
          rules={[
            { required: true, message: '请输入分数' },
            {
              validator: (_, value) => {
                if (value === undefined || value === null) {
                  return Promise.reject(new Error('请输入分数'));
                }
                if (value < 0 || value > 100) {
                  return Promise.reject(new Error('分数必须在0-100之间'));
                }
                return Promise.resolve();
              },
            },
          ]}
        >
          <InputNumber
            min={0}
            max={100}
            precision={2}
            style={{ width: '100%' }}
            placeholder="请输入0-100的分数"
          />
        </Form.Item>

        <Form.Item label="评语" name="comment">
          <Input.TextArea
            rows={4}
            placeholder="请输入评语（可选）"
            maxLength={500}
            showCount
          />
        </Form.Item>

        <div style={{ marginTop: 16, color: '#999', fontSize: 12 }}>
          <p>说明：</p>
          <ul style={{ margin: 0, paddingLeft: 20 }}>
            <li>分数范围：0-100分</li>
            <li>最终得分 = 自评分 × 30% + 上级评分 × 70%</li>
            <li>请根据实际表现客观评分</li>
          </ul>
        </div>
      </Form>
    </Modal>
  );
};

export default ScoreFormModal;
