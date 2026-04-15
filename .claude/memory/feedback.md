---
name: feedback
description: 反馈记忆
type: feedback
---

## 架构规范反馈

- 分层访问规则必须严格遵守
- 横切能力必须集中在 middleware/common
- 外部调用必须通过 integration 层

## 开发流程反馈

- 每次修改必须评估风险等级
- 新增代码必须有测试覆盖
- 单次修改文件不超过 5 个

## 代码质量反馈

- 单文件不超过 500 行
- 单函数不超过 50 行
- 嵌套不超过 3 层
