USE `user_management`;

UPDATE `group`
SET `name` = '管理员组',
    `description` = '系统管理员默认分组'
WHERE `id` = 'group-admin';

UPDATE `permission`
SET `name` = CASE `code`
    WHEN 'menu:dashboard' THEN '工作台'
    WHEN 'menu:userManagement' THEN '用户管理'
    WHEN 'menu:user:list' THEN '用户列表'
    WHEN 'menu:user:create' THEN '创建用户'
    WHEN 'menu:user:update' THEN '编辑用户'
    WHEN 'menu:user:delete' THEN '删除用户'
    WHEN 'menu:user:resetPassword' THEN '重置密码'
    WHEN 'menu:user:attribute' THEN '用户属性'
    WHEN 'menu:groupManagement' THEN '组织管理'
    WHEN 'menu:group:list' THEN '组织列表'
    WHEN 'menu:group:create' THEN '创建组织'
    WHEN 'menu:group:update' THEN '编辑组织'
    WHEN 'menu:group:delete' THEN '删除组织'
    WHEN 'menu:group:member' THEN '成员管理'
    WHEN 'menu:group:permission' THEN '组织权限'
    WHEN 'menu:group:tempPassword' THEN '临时密码'
    WHEN 'menu:permissionManagement' THEN '权限管理'
    WHEN 'menu:permission:list' THEN '权限列表'
    WHEN 'menu:permission:create' THEN '创建权限'
    WHEN 'menu:permission:update' THEN '编辑权限'
    WHEN 'menu:permission:delete' THEN '删除权限'
    WHEN 'menu:permission:grant' THEN '授权管理'
    WHEN 'menu:systemMonitor' THEN '系统监控与会话设备'
    WHEN 'menu:flow:register' THEN '注册流程'
    WHEN 'menu:device:sessions' THEN '会话设备'
    WHEN 'api:user:create' THEN '用户创建'
    WHEN 'api:user:list' THEN '用户查询'
    WHEN 'api:user:detail' THEN '用户详情'
    WHEN 'api:user:update' THEN '用户修改'
    WHEN 'api:user:updateMe' THEN '当前用户修改'
    WHEN 'api:user:delete' THEN '用户删除'
    WHEN 'api:user:batchDelete' THEN '用户批量删除'
    WHEN 'api:user:changePassword' THEN '当前用户改密'
    WHEN 'api:user:resetPassword' THEN '用户重置密码'
    WHEN 'api:user:attribute:list' THEN '用户属性查询'
    WHEN 'api:user:attribute:create' THEN '用户属性创建'
    WHEN 'api:user:attribute:update' THEN '用户属性修改'
    WHEN 'api:user:attribute:delete' THEN '用户属性删除'
    WHEN 'api:group:create' THEN '组织创建'
    WHEN 'api:group:list' THEN '组织查询'
    WHEN 'api:group:listMy' THEN '我的组织查询'
    WHEN 'api:group:listPublic' THEN '公开组织查询'
    WHEN 'api:group:detail' THEN '组织详情'
    WHEN 'api:group:detailMy' THEN '我的组织详情'
    WHEN 'api:group:update' THEN '组织修改'
    WHEN 'api:group:delete' THEN '组织删除'
    WHEN 'api:group:member:add' THEN '组织成员添加'
    WHEN 'api:group:member:remove' THEN '组织成员移除'
    WHEN 'api:group:member:upgrade' THEN '组织成员升级'
    WHEN 'api:group:tempPassword:set' THEN '临时密码设置'
    WHEN 'api:group:tempPassword:clear' THEN '临时密码清除'
    WHEN 'api:group:join' THEN '组织加入'
    WHEN 'api:group:permission:list' THEN '组织权限查询'
    WHEN 'api:group:permission:grant' THEN '组织权限授权'
    WHEN 'api:group:permission:revoke' THEN '组织权限回收'
    WHEN 'api:permission:create' THEN '权限创建'
    WHEN 'api:permission:list' THEN '权限查询'
    WHEN 'api:permission:update' THEN '权限修改'
    WHEN 'api:permission:delete' THEN '权限删除'
    WHEN 'api:permission:group:list' THEN '权限下组织查询'
    WHEN 'api:permission:user:list' THEN '权限下用户查询'
    WHEN 'api:user:permission:list' THEN '用户权限查询'
    WHEN 'api:user:permission:effective' THEN '用户有效权限查询'
    WHEN 'api:user:permission:source' THEN '用户权限来源查询'
    WHEN 'api:user:permission:grant' THEN '用户权限授权'
    WHEN 'api:user:permission:revoke' THEN '用户权限回收'
    ELSE `name`
END
WHERE `code` IN (
    'menu:dashboard',
    'menu:userManagement',
    'menu:user:list',
    'menu:user:create',
    'menu:user:update',
    'menu:user:delete',
    'menu:user:resetPassword',
    'menu:user:attribute',
    'menu:groupManagement',
    'menu:group:list',
    'menu:group:create',
    'menu:group:update',
    'menu:group:delete',
    'menu:group:member',
    'menu:group:permission',
    'menu:group:tempPassword',
    'menu:permissionManagement',
    'menu:permission:list',
    'menu:permission:create',
    'menu:permission:update',
    'menu:permission:delete',
    'menu:permission:grant',
    'menu:systemMonitor',
    'menu:flow:register',
    'menu:device:sessions',
    'api:user:create',
    'api:user:list',
    'api:user:detail',
    'api:user:update',
    'api:user:updateMe',
    'api:user:delete',
    'api:user:batchDelete',
    'api:user:changePassword',
    'api:user:resetPassword',
    'api:user:attribute:list',
    'api:user:attribute:create',
    'api:user:attribute:update',
    'api:user:attribute:delete',
    'api:group:create',
    'api:group:list',
    'api:group:listMy',
    'api:group:listPublic',
    'api:group:detail',
    'api:group:detailMy',
    'api:group:update',
    'api:group:delete',
    'api:group:member:add',
    'api:group:member:remove',
    'api:group:member:upgrade',
    'api:group:tempPassword:set',
    'api:group:tempPassword:clear',
    'api:group:join',
    'api:group:permission:list',
    'api:group:permission:grant',
    'api:group:permission:revoke',
    'api:permission:create',
    'api:permission:list',
    'api:permission:update',
    'api:permission:delete',
    'api:permission:group:list',
    'api:permission:user:list',
    'api:user:permission:list',
    'api:user:permission:effective',
    'api:user:permission:source',
    'api:user:permission:grant',
    'api:user:permission:revoke'
);
