import { useEffect, useState } from 'react';
import { permissionService } from '@/services/rbacService';

/**
 * 权限检查 Hook
 * @param permissionCode 权限代码
 * @returns 是否有权限
 */
export const usePermission = (permissionCode: string): boolean => {
  const [hasPermission, setHasPermission] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const checkPermission = async () => {
      try {
        const result = await permissionService.checkPermission(permissionCode);
        setHasPermission(result);
      } catch (error) {
        console.error('检查权限失败:', error);
        setHasPermission(false);
      } finally {
        setLoading(false);
      }
    };

    checkPermission();
  }, [permissionCode]);

  return hasPermission;
};

/**
 * 多权限检查 Hook（任一权限）
 * @param permissionCodes 权限代码数组
 * @returns 是否有任一权限
 */
export const useAnyPermission = (permissionCodes: string[]): boolean => {
  const [hasPermission, setHasPermission] = useState<boolean>(false);

  useEffect(() => {
    const checkPermissions = async () => {
      try {
        // 获取用户所有权限
        const response = await fetch('/api/v1/permissions/my', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
          },
        });
        
        if (response.ok) {
          const data = await response.json();
          const userPermissions = new Set(data.data || []);
          const hasAny = permissionCodes.some(code => userPermissions.has(code));
          setHasPermission(hasAny);
        } else {
          setHasPermission(false);
        }
      } catch (error) {
        console.error('检查权限失败:', error);
        setHasPermission(false);
      }
    };

    if (permissionCodes.length > 0) {
      checkPermissions();
    }
  }, [permissionCodes]);

  return hasPermission;
};

/**
 * 多权限检查 Hook（所有权限）
 * @param permissionCodes 权限代码数组
 * @returns 是否有所有权限
 */
export const useAllPermissions = (permissionCodes: string[]): boolean => {
  const [hasPermission, setHasPermission] = useState<boolean>(false);

  useEffect(() => {
    const checkPermissions = async () => {
      try {
        const response = await fetch('/api/v1/permissions/my', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
          },
        });
        
        if (response.ok) {
          const data = await response.json();
          const userPermissions = new Set(data.data || []);
          const hasAll = permissionCodes.every(code => userPermissions.has(code));
          setHasPermission(hasAll);
        } else {
          setHasPermission(false);
        }
      } catch (error) {
        console.error('检查权限失败:', error);
        setHasPermission(false);
      }
    };

    if (permissionCodes.length > 0) {
      checkPermissions();
    }
  }, [permissionCodes]);

  return hasPermission;
};
