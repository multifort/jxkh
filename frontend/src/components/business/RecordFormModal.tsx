import React, { useState } from 'react';
import {
  Modal,
  Form,
  Input,
  Select,
  DatePicker,
  Upload,
  Button,
  Space,
  message,
  Card,
  Collapse,
} from 'antd';
import {
  UploadOutlined,
  RobotOutlined,
  LoadingOutlined,
} from '@ant-design/icons';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import dayjs from 'dayjs';
import { recordService, fileService, aiService } from '../../services/recordService';

const { Option } = Select;
const { Panel } = Collapse;

interface RecordFormModalProps {
  visible: boolean;
  editingRecord?: any | null;
  planId: number;
  onSuccess: () => void;
  onCancel: () => void;
}

const RecordFormModal: React.FC<RecordFormModalProps> = ({
  visible,
  editingRecord,
  planId,
  onSuccess,
  onCancel,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [content, setContent] = useState('');
  const [fileList, setFileList] = useState<any[]>([]);
  const [uploading, setUploading] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiSummary, setAiSummary] = useState<any>(null);

  React.useEffect(() => {
    if (editingRecord) {
      form.setFieldsValue({
        type: editingRecord.type,
        recordDate: dayjs(editingRecord.recordDate),
        progress: editingRecord.progress,
      });
      setContent(editingRecord.content || '');
      
      // 解析附件
      if (editingRecord.attachments) {
        try {
          const urls = JSON.parse(editingRecord.attachments);
          setFileList(
            urls.map((url: string, index: number) => ({
              uid: `-${index}`,
              name: url.split('/').pop(),
              status: 'done',
              url,
            }))
          );
        } catch (e) {
          setFileList([]);
        }
      } else {
        setFileList([]);
      }
      
      // 解析 AI 总结
      if (editingRecord.aiSuggestions) {
        try {
          setAiSummary(JSON.parse(editingRecord.aiSuggestions));
        } catch (e) {
          setAiSummary(null);
        }
      }
    } else {
      form.resetFields();
      setContent('');
      setFileList([]);
      setAiSummary(null);
    }
  }, [editingRecord, form, visible]);

  const handleGenerateSummary = async () => {
    if (!content) {
      message.warning('请先输入内容');
      return;
    }

    setAiLoading(true);
    try {
      // 提取纯文本内容
      const tempDiv = document.createElement('div');
      tempDiv.innerHTML = content;
      const plainText = tempDiv.textContent || tempDiv.innerText || '';

      const response = await aiService.generateWeeklySummary(plainText);
      setAiSummary(response.data);
      message.success('AI 总结生成成功');
    } catch (error) {
      message.error('AI 总结生成失败');
    } finally {
      setAiLoading(false);
    }
  };

  const handleUpload = async (file: File) => {
    setUploading(true);
    try {
      const response = await fileService.uploadFile(file);
      const newFile = {
        uid: Date.now().toString(),
        name: file.name,
        status: 'done',
        url: response.data,
      };
      setFileList([...fileList, newFile]);
      message.success(`${file.name} 上传成功`);
    } catch (error) {
      message.error(`${file.name} 上传失败`);
    } finally {
      setUploading(false);
    }
    return false; // 阻止默认上传行为
  };

  const handleRemoveFile = async (file: any) => {
    try {
      if (file.url) {
        await fileService.deleteFile(file.url);
      }
      setFileList(fileList.filter((f) => f.uid !== file.uid));
      message.success('文件删除成功');
    } catch (error) {
      message.error('文件删除失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const formData = new FormData();
      formData.append('type', values.type);
      formData.append('planId', planId.toString());
      formData.append('content', content);
      
      if (values.progress) {
        formData.append('progress', values.progress.toString());
      }
      
      if (values.recordDate) {
        formData.append('recordDate', values.recordDate.format('YYYY-MM-DD'));
      }

      // 添加新上传的文件
      fileList.forEach((file) => {
        if (file.originFileObj) {
          formData.append('files', file.originFileObj);
        }
      });

      if (editingRecord) {
        await recordService.updateRecord(editingRecord.id, formData);
        message.success('更新成功');
      } else {
        await recordService.createRecord(formData);
        message.success('创建成功');
      }

      onSuccess();
    } catch (error) {
      message.error('操作失败');
    } finally {
      setLoading(false);
    }
  };

  const modules = {
    toolbar: [
      [{ header: [1, 2, 3, false] }],
      ['bold', 'italic', 'underline', 'strike'],
      [{ list: 'ordered' }, { list: 'bullet' }],
      ['link', 'image'],
      ['clean'],
    ],
  };

  return (
    <Modal
      title={editingRecord ? '编辑记录' : '新建记录'}
      open={visible}
      onCancel={onCancel}
      width={900}
      footer={[
        <Button key="cancel" onClick={onCancel}>
          取消
        </Button>,
        <Button
          key="submit"
          type="primary"
          loading={loading}
          onClick={handleSubmit}
        >
          {editingRecord ? '更新' : '创建'}
        </Button>,
      ]}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          label="记录类型"
          name="type"
          rules={[{ required: true, message: '请选择记录类型' }]}
        >
          <Select placeholder="请选择">
            <Option value="WEEKLY_REPORT">周报</Option>
            <Option value="MONTHLY_REPORT">月报</Option>
            <Option value="MILESTONE">里程碑</Option>
            <Option value="ACHIEVEMENT">成果</Option>
          </Select>
        </Form.Item>

        <Form.Item
          label="记录日期"
          name="recordDate"
          rules={[{ required: true, message: '请选择记录日期' }]}
        >
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item label="进度 (%)" name="progress">
          <Input type="number" min={0} max={100} placeholder="0-100" />
        </Form.Item>

        <Form.Item
          label="内容"
          rules={[{ required: true, message: '请输入内容' }]}
        >
          <ReactQuill
            theme="snow"
            value={content}
            onChange={setContent}
            modules={modules}
            style={{ height: 200, marginBottom: 50 }}
          />
        </Form.Item>

        <Form.Item label="附件">
          <Upload
            fileList={fileList}
            customRequest={({ file }) => handleUpload(file as File)}
            onRemove={handleRemoveFile}
            multiple
          >
            <Button icon={<UploadOutlined />} loading={uploading}>
              上传文件
            </Button>
          </Upload>
          <div style={{ marginTop: 8, color: '#999', fontSize: 12 }}>
            支持 PDF、DOC、XLS、图片等格式，单个文件不超过 10MB
          </div>
        </Form.Item>

        <Card
          size="small"
          title={
            <Space>
              <RobotOutlined />
              AI 智能总结
              <Button
                size="small"
                icon={aiLoading ? <LoadingOutlined /> : <RobotOutlined />}
                loading={aiLoading}
                onClick={handleGenerateSummary}
              >
                生成总结
              </Button>
            </Space>
          }
          style={{ marginTop: 16 }}
        >
          {aiSummary ? (
            <Collapse defaultActiveKey={['1', '2', '3']}>
              <Panel header="关键成果" key="1">
                <ul>
                  {aiSummary.keyAchievements?.map((item: string, index: number) => (
                    <li key={index}>{item}</li>
                  ))}
                </ul>
              </Panel>
              <Panel header="风险点" key="2">
                {aiSummary.risks?.length > 0 ? (
                  <ul>
                    {aiSummary.risks.map((item: string, index: number) => (
                      <li key={index} style={{ color: '#ff4d4f' }}>
                        {item}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <div style={{ color: '#52c41a' }}>暂无风险</div>
                )}
              </Panel>
              <Panel header="建议" key="3">
                <ul>
                  {aiSummary.suggestions?.map((item: string, index: number) => (
                    <li key={index}>{item}</li>
                  ))}
                </ul>
              </Panel>
            </Collapse>
          ) : (
            <div style={{ textAlign: 'center', color: '#999', padding: 20 }}>
              点击"生成总结"按钮，AI 将自动分析内容
            </div>
          )}
        </Card>
      </Form>
    </Modal>
  );
};

export default RecordFormModal;
