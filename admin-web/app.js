const SESSION_KEY = "topbiz-session-id";
const API_BASE_KEY = "topbiz-api-base";
const DEFAULT_API_BASE = "http://127.0.0.1:8080";
const PHONE_PATTERN = /^1\d{10}$/;
const EMAIL_PATTERN = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;

const state = {
    user: null,
    menus: [],
    currentPage: "dashboard",
    notificationAdmin: {
        tab: "channels",
        channels: [],
        templates: [],
        messages: [],
        policies: []
    },
    logOps: {
        tab: "query",
        logs: [],
        metrics: [],
        alertRules: [],
        alertTriggers: []
    },
    messageCenter: {
        page: 1,
        pageSize: 10,
        readStatus: null,
        total: 0,
        unreadCount: 0,
        items: []
    }
};

const countdownTimers = new Map();

const menuMappings = {
    "menu:dashboard": { id: "dashboard", label: "工作台", page: "page-dashboard" },
    "menu:userManagement": { id: "users", label: "用户管理", page: "page-users" },
    "menu:groupManagement": { id: "groups", label: "组织管理", page: "page-groups" },
    "menu:permissionManagement": { id: "permissions", label: "权限管理", page: "page-permissions" },
    "menu:systemMonitor": { id: "flow", label: "流程中心", page: "page-flow" },
    "menu:notificationManagement": { id: "notifications", label: "通知管理", page: "page-notifications" }
};

menuMappings["menu:logManagement"] = { id: "logs", label: "日志运维", page: "page-logs" };

const pageMenuRules = [
    { ...menuMappings["menu:dashboard"], permissions: ["menu:dashboard"] },
    {
        ...menuMappings["menu:userManagement"],
        permissions: [
            "menu:userManagement",
            "menu:user:list",
            "menu:user:create",
            "menu:user:update",
            "menu:user:delete",
            "menu:user:resetPassword",
            "menu:user:attribute"
        ]
    },
    {
        ...menuMappings["menu:groupManagement"],
        permissions: [
            "menu:groupManagement",
            "menu:group:list",
            "menu:group:create",
            "menu:group:update",
            "menu:group:delete",
            "menu:group:member",
            "menu:group:permission",
            "menu:group:tempPassword"
        ]
    },
    {
        ...menuMappings["menu:permissionManagement"],
        permissions: [
            "menu:permissionManagement",
            "menu:permission:list",
            "menu:permission:create",
            "menu:permission:update",
            "menu:permission:delete",
            "menu:permission:grant"
        ]
    },
    {
        ...menuMappings["menu:systemMonitor"],
        permissions: [
            "menu:systemMonitor",
            "menu:flow:register",
            "menu:device:sessions"
        ]
    },
    { ...menuMappings["menu:notificationManagement"], permissions: ["menu:notificationManagement"] },
    { ...menuMappings["menu:logManagement"], permissions: ["menu:logManagement"] }
];

const actionPermissions = {
    user: {
        list: "menu:user:list",
        create: "menu:user:create",
        update: "menu:user:update",
        delete: "menu:user:delete",
        resetPassword: "menu:user:resetPassword",
        attribute: "menu:user:attribute"
    },
    group: {
        list: "menu:group:list",
        create: "menu:group:create",
        update: "menu:group:update",
        delete: "menu:group:delete",
        member: "menu:group:member",
        permission: "menu:group:permission",
        tempPassword: "menu:group:tempPassword"
    },
    permission: {
        list: "menu:permission:list",
        create: "menu:permission:create",
        update: "menu:permission:update",
        delete: "menu:permission:delete",
        grant: "menu:permission:grant"
    }
};

const dom = {
    authApp: document.getElementById("authApp"),
    consoleApp: document.getElementById("consoleApp"),
    loginMethodSelector: document.getElementById("loginMethodSelector"),
    phonePasswordView: document.getElementById("phonePasswordView"),
    emailCodeView: document.getElementById("emailCodeView"),
    registerView: document.getElementById("registerView"),
    authTitle: document.getElementById("authTitle"),
    authFeedback: document.getElementById("authFeedback"),
    apiBaseInput: document.getElementById("apiBaseInput"),
    sidebarMenu: document.getElementById("sidebarMenu"),
    pageTitle: document.getElementById("pageTitle"),
    currentUserCard: document.getElementById("currentUserCard"),
    sessionBadge: document.getElementById("sessionBadge"),
    usersTable: document.getElementById("usersTable"),
    groupsTable: document.getElementById("groupsTable"),
    permissionsTree: document.getElementById("permissionsTree"),
    flowCanvas: document.getElementById("flowCanvas"),
    flowEditor: document.getElementById("flowEditor"),
    devicesList: document.getElementById("devicesList"),
    messagesList: document.getElementById("messagesList"),
    messageSummaryBar: document.getElementById("messageSummaryBar"),
    messagesPagination: document.getElementById("messagesPagination"),
    dashboardMessagePreview: document.getElementById("dashboardMessagePreview"),
    channelsTable: document.getElementById("channelsTable"),
    templatesTable: document.getElementById("templatesTable"),
    reusableMessagesTable: document.getElementById("reusableMessagesTable"),
    policiesTable: document.getElementById("policiesTable"),
    logsTable: document.getElementById("logsTable"),
    logMetricsTable: document.getElementById("logMetricsTable"),
    logMetricSummary: document.getElementById("logMetricSummary"),
    logMetricChart: document.getElementById("logMetricChart"),
    logMetricChartPanel: document.getElementById("logMetricChartPanel"),
    logMetricChartTitle: document.getElementById("logMetricChartTitle"),
    logMetricChartMeta: document.getElementById("logMetricChartMeta"),
    logMetricChartLegend: document.getElementById("logMetricChartLegend"),
    logMetricChartEmpty: document.getElementById("logMetricChartEmpty"),
    logMetricChartTooltip: document.getElementById("logMetricChartTooltip"),
    alertRulesTable: document.getElementById("alertRulesTable"),
    alertTriggersTable: document.getElementById("alertTriggersTable"),
    modalRoot: document.getElementById("modalRoot")
};

let metricChartState = {
    series: null,
    rows: [],
    points: []
};

function getSessionId() {
    return localStorage.getItem(SESSION_KEY);
}

function setSessionId(sessionId) {
    if (sessionId) {
        localStorage.setItem(SESSION_KEY, sessionId);
        if (dom.sessionBadge) dom.sessionBadge.textContent = "已登录";
        return;
    }
    localStorage.removeItem(SESSION_KEY);
    if (dom.sessionBadge) dom.sessionBadge.textContent = "未登录";
}

function getApiBase() {
    return localStorage.getItem(API_BASE_KEY) || DEFAULT_API_BASE;
}

function setApiBase(value) {
    localStorage.setItem(API_BASE_KEY, value);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}

function formatDateTime(value) {
    if (!value) {
        return "-";
    }
    const text = String(value);
    if (/[zZ]$|[+-]\d{2}:\d{2}$/.test(text)) {
        const parsed = new Date(text);
        if (!Number.isNaN(parsed.getTime())) {
            return escapeHtml(parsed.toLocaleString("zh-CN", {
                hour12: false,
                year: "numeric",
                month: "2-digit",
                day: "2-digit",
                hour: "2-digit",
                minute: "2-digit",
                second: "2-digit"
            }));
        }
    }
    return escapeHtml(text.replace("T", " "));
}

function joinTextList(values) {
    return escapeHtml((values || []).filter(Boolean).join(" / ") || "-");
}

function resolveFeedbackMessage(title, payload) {
    if (payload && typeof payload === "object") {
        if (typeof payload.message === "string" && payload.message.trim()) {
            return payload.message.trim();
        }
        if (typeof payload.error === "string" && payload.error.trim()) {
            return payload.error.trim();
        }
    }
    return title;
}

function inferFeedbackType(message) {
    return /失败|错误|异常|error|invalid/i.test(message)
        ? "error"
        : /成功|已发送|已复制|已更新|已保存/i.test(message)
            ? "success"
            : "info";
}

function showToast(message, type = "info") {
    const toast = document.createElement("div");
    toast.className = `toast-message is-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    requestAnimationFrame(() => toast.classList.add("visible"));
    window.setTimeout(() => {
        toast.classList.remove("visible");
        window.setTimeout(() => toast.remove(), 220);
    }, 2600);
}

function hideAuthFeedback() {
    dom.authFeedback.className = "auth-feedback hidden";
    dom.authFeedback.textContent = "";
}

function showAuthFeedback(message, type = "info") {
    dom.authFeedback.className = `auth-feedback is-${type}`;
    dom.authFeedback.textContent = message;
}

function writeResponse(title, payload) {
    const message = resolveFeedbackMessage(title, payload);
    if (!dom.authApp.classList.contains("hidden")) {
        showAuthFeedback(message, inferFeedbackType(message));
    } else {
        showToast(message, inferFeedbackType(message));
    }
}

function setSendCodeButtonState(button, seconds = 0) {
    if (!button) {
        return;
    }
    if (!button.dataset.defaultText) {
        button.dataset.defaultText = button.textContent.trim();
    }
    if (seconds > 0) {
        button.disabled = true;
        button.textContent = `${seconds}s 后重试`;
        return;
    }
    button.disabled = false;
    button.textContent = button.dataset.defaultText;
}

function startSendCodeCountdown(button, seconds) {
    if (!button) {
        return;
    }
    const duration = Math.max(1, Number(seconds) || 60);
    const previousTimer = countdownTimers.get(button);
    if (previousTimer) {
        clearInterval(previousTimer);
    }
    let remaining = duration;
    setSendCodeButtonState(button, remaining);
    const timerId = window.setInterval(() => {
        remaining -= 1;
        if (remaining <= 0) {
            clearInterval(timerId);
            countdownTimers.delete(button);
            setSendCodeButtonState(button, 0);
            return;
        }
        setSendCodeButtonState(button, remaining);
    }, 1000);
    countdownTimers.set(button, timerId);
}

function detectAccountType(account) {
    const normalized = (account || "").trim();
    if (PHONE_PATTERN.test(normalized)) {
        return "phone";
    }
    if (EMAIL_PATTERN.test(normalized)) {
        return "email";
    }
    throw new Error("请输入正确的手机号或邮箱");
}

async function apiFetch(path, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };
    const sessionId = getSessionId();
    if (sessionId) {
        headers.SessionId = sessionId;
    }

    const response = await fetch(`${getApiBase()}${path}`, {
        ...options,
        headers
    });

    let data;
    try {
        data = await response.json();
    } catch {
        throw { code: response.status, message: "服务响应解析失败" };
    }

    if (!response.ok || (data.code && data.code !== 200)) {
        if (data.code === 401) {
            setSessionId(null);
            showAuthApp();
        }
        throw data;
    }
    return data;
}

function formToObject(form) {
    const formData = new FormData(form);
    const result = {};
    for (const [key, value] of formData.entries()) {
        if (value !== "") {
            result[key] = value;
        }
    }
    return result;
}

function stringifyAttributeValue(value) {
    if (value == null || value === "") {
        return "-";
    }
    if (Array.isArray(value)) {
        return value.join(" / ");
    }
    if (typeof value === "object") {
        return JSON.stringify(value);
    }
    return String(value);
}

function serializeAttributeInput(rawValue, fieldType) {
    const value = rawValue.trim();
    if (!value) {
        return null;
    }
    if (fieldType === "number") {
        const numberValue = Number(value);
        return Number.isNaN(numberValue) ? value : numberValue;
    }
    if (fieldType === "boolean") {
        return value === "true";
    }
    if (fieldType === "json") {
        try {
            return JSON.parse(value);
        } catch {
            return value;
        }
    }
    if (fieldType === "multi_select") {
        return value.split(",").map((item) => item.trim()).filter(Boolean);
    }
    return value;
}

function parseOptionsInput(rawValue) {
    return rawValue
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean);
}

function getSelectedValues(selectElement) {
    if (!selectElement) {
        return [];
    }
    return Array.from(selectElement.selectedOptions || [])
        .map((option) => option.value)
        .filter(Boolean);
}

function getCheckedValues(containerOrSelector) {
    const container = typeof containerOrSelector === "string"
        ? document.querySelector(containerOrSelector)
        : containerOrSelector;
    if (!container) {
        return [];
    }
    return Array.from(container.querySelectorAll('input[type="checkbox"]:checked'))
        .map((input) => input.value)
        .filter(Boolean);
}

function formatUserStatus(status) {
    const normalized = String(status ?? "");
    return {
        "1": "正常",
        "2": "禁用",
        "3": "锁定",
        "4": "已注销",
        active: "正常",
        disabled: "禁用",
        locked: "锁定",
        cancelled: "已注销"
    }[normalized] || normalized || "-";
}

function formatGroupStatus(status) {
    const normalized = String(status ?? "");
    return {
        "1": "正常",
        "2": "禁用",
        active: "正常",
        disabled: "禁用"
    }[normalized] || normalized || "-";
}

function showAuthApp() {
    dom.authApp.classList.remove("hidden");
    dom.consoleApp.classList.add("hidden");
}

function showConsoleApp() {
    hideAuthFeedback();
    dom.authApp.classList.add("hidden");
    dom.consoleApp.classList.remove("hidden");
}

function showLoginMethodSelector() {
    dom.loginMethodSelector.classList.remove("hidden");
    dom.phonePasswordView.classList.remove("active");
    dom.emailCodeView.classList.remove("active");
    dom.registerView.classList.remove("active");
    dom.authTitle.textContent = "登录系统";
    hideAuthFeedback();
}

function showLoginView(method) {
    dom.loginMethodSelector.classList.add("hidden");
    dom.phonePasswordView.classList.remove("active");
    dom.emailCodeView.classList.remove("active");
    dom.registerView.classList.remove("active");

    if (method === "phone-password") {
        dom.phonePasswordView.classList.add("active");
        dom.authTitle.textContent = "手机号登录";
        const firstInput = document.querySelector('#phonePasswordForm input');
        if (firstInput) setTimeout(() => firstInput.focus(), 100);
    } else if (method === "email-code") {
        dom.emailCodeView.classList.add("active");
        dom.authTitle.textContent = "邮箱验证码登录";
        const firstInput = document.querySelector('#emailCodeForm input');
        if (firstInput) setTimeout(() => firstInput.focus(), 100);
    }
    hideAuthFeedback();
    clearAuthForms();
}

function clearAuthForms() {
    document.querySelectorAll('.auth-panel form').forEach(f => {
        const btn = f.querySelector('button[type="submit"]');
        if (btn) { btn.disabled = false; btn.textContent = btn.dataset.originalText || btn.textContent; }
    });
}

function showRegisterView() {
    dom.loginMethodSelector.classList.add("hidden");
    dom.phonePasswordView.classList.remove("active");
    dom.emailCodeView.classList.remove("active");
    dom.registerView.classList.add("active");
    dom.authTitle.textContent = "注册账号";
    hideAuthFeedback();
    clearAuthForms();
    const firstInput = document.querySelector('#registerForm input');
    if (firstInput) setTimeout(() => firstInput.focus(), 100);
}

function buildMenuConfig() {
    const result = [];
    const seen = new Set();
    const granted = new Set(state.menus || []);
    for (const item of pageMenuRules) {
        if (!seen.has(item.id) && item.permissions.some((code) => granted.has(code))) {
            seen.add(item.id);
            result.push({
                id: item.id,
                label: item.label,
                page: item.page
            });
        }
    }
    if (!result.length) {
        result.push(menuMappings["menu:dashboard"]);
    }
    return result;
}

function hasMenuPermission(...codes) {
    const granted = new Set(state.menus || []);
    return codes.some((code) => granted.has(code));
}

function setElementVisible(id, visible) {
    const element = document.getElementById(id);
    if (element) {
        element.classList.toggle("hidden", !visible);
    }
}

function formatUserGroups(user = state.user) {
    return (user?.groupNames || []).filter(Boolean).join(" / ") || "未加入分组";
}

function renderCurrentUserCard() {
    if (!dom.currentUserCard) {
        return;
    }
    dom.currentUserCard.innerHTML = `
        <strong>${escapeHtml(state.user?.nickname || "用户")} · ${escapeHtml(state.user?.role || "user")}</strong>
        <span>${escapeHtml(formatUserGroups())}</span>
    `;
}

function buildQuickActions() {
    const actions = buildMenuConfig().map((item) => ({
        id: item.id,
        label: item.label,
        description: `进入 ${item.label}`,
        type: "page"
    }));
    const extraActions = [
        { id: "send-message", label: "发送消息", description: "选择模板发送站内消息", type: "command", handler: openSendMessageModal },
        { id: "profile", label: "个人中心", description: "查看分组并维护个人资料", type: "command", handler: openProfileCenter },
        { id: "devices", label: "设备会话", description: "查看和管理当前登录设备", type: "command", handler: () => {
            document.getElementById("deviceDrawer").classList.remove("hidden");
            loadDevices();
        } },
        { id: "messages", label: "站内消息", description: "查看个人站内消息", type: "command", handler: openMessageDrawer }
    ];
    for (const action of extraActions) {
        if (!actions.some((item) => item.id === action.id)) {
            actions.push(action);
        }
    }
    return actions;
}

function renderSidebar() {
    const menus = buildMenuConfig();
    dom.sidebarMenu.innerHTML = menus.map((item) => `
        <div class="menu-item ${state.currentPage === item.id ? "active" : ""}" data-page="${item.id}">
            ${item.label}
        </div>
    `).join("");
    dom.sidebarMenu.querySelectorAll(".menu-item").forEach((item) => {
        item.addEventListener("click", () => {
            state.currentPage = item.dataset.page;
            renderSidebar();
            renderPage();
        });
    });
}

function renderPage() {
    const menus = buildMenuConfig();
    const active = menus.find((item) => item.id === state.currentPage) || menus[0];
    state.currentPage = active.id;
    dom.pageTitle.textContent = active.label;

    document.querySelectorAll(".page").forEach((page) => page.classList.remove("active"));
    const target = document.getElementById(active.page);
    if (target) {
        target.classList.add("active");
    }

    if (active.id === "dashboard") {
        renderDashboard();
    }
    if (active.id === "users") {
        configureUserPageAccess();
        if (hasMenuPermission(actionPermissions.user.list)) {
            loadUsers();
        }
    }
    if (active.id === "groups") {
        configureGroupPageAccess();
        if (hasMenuPermission(actionPermissions.group.list)) {
            loadGroups();
        }
    }
    if (active.id === "permissions") {
        configurePermissionPageAccess();
        if (hasMenuPermission(actionPermissions.permission.list)) {
            loadPermissions();
        }
    }
    if (active.id === "flow") {
        loadFlow();
        loadDevices();
    }
    if (active.id === "notifications") {
        renderNotificationAdmin();
    }
    if (active.id === "logs") {
        renderLogOps();
    }
}

function renderDashboard() {
    document.getElementById("statUserName").textContent = state.user?.nickname || "-";
    document.getElementById("statUserRole").textContent = state.user?.role || "-";
    document.getElementById("statMenuCount").textContent = String(state.menus.length || 0);
    const groupStat = document.getElementById("statUserGroups");
    if (groupStat) {
        groupStat.textContent = formatUserGroups();
    }
    const quickActions = document.getElementById("quickActions");
    const actions = buildQuickActions();
    quickActions.innerHTML = actions.map((item) => `
        <button class="quick-action" data-quick-id="${item.id}" data-quick-type="${item.type}">
            <strong>${escapeHtml(item.label)}</strong>
            <span>${escapeHtml(item.description)}</span>
        </button>
    `).join("");
    quickActions.querySelectorAll("[data-quick-id]").forEach((button) => {
        button.addEventListener("click", () => {
            const action = actions.find((item) => item.id === button.dataset.quickId);
            if (!action) {
                return;
            }
            if (action.type === "page") {
                state.currentPage = action.id;
                renderSidebar();
                renderPage();
                return;
            }
            action.handler?.();
        });
    });
    renderDashboardMessages();
    loadFlowOverview();
}

async function loadFlowOverview() {
    try {
        const data = await apiFetch("/api/v1/flows/register");
        const flow = data.data || {};
        document.getElementById("flowOverview").className = "flow-overview";
        document.getElementById("flowOverview").innerHTML = `
            <div class="subtle">当前流程：${escapeHtml(flow.flowCode || "-")}</div>
            <div class="subtle">版本：${escapeHtml(flow.version || "-")}</div>
            <div class="subtle">节点数：${(flow.nodes || []).length}</div>
            <div class="subtle">流转数：${(flow.transitions || []).length}</div>
        `;
    } catch (error) {
        document.getElementById("flowOverview").className = "flow-overview empty-state";
        document.getElementById("flowOverview").textContent = resolveFeedbackMessage("加载流程失败", error);
    }
}

function renderTable(container, columns, rows) {
    if (!rows.length) {
        container.className = "table-wrap empty-state";
        container.textContent = "暂无数据";
        return;
    }
    container.className = "table-wrap";
    container.innerHTML = `
        <table>
            <thead>
                <tr>${columns.map((column) => `<th>${column.label}</th>`).join("")}</tr>
            </thead>
            <tbody>
                ${rows.map((row) => `<tr>${columns.map((column) => `<td>${column.render(row)}</td>`).join("")}</tr>`).join("")}
            </tbody>
        </table>
    `;
}

function actionButtons(actions) {
    return `<div class="table-actions">${actions.filter(Boolean).join("")}</div>`;
}

function configureUserPageAccess() {
    const canList = hasMenuPermission(actionPermissions.user.list);
    setElementVisible("searchUsersBtn", canList);
    setElementVisible("openCreateUserBtn", hasMenuPermission(actionPermissions.user.create));
    if (!canList && dom.usersTable) {
        dom.usersTable.className = "table-wrap empty-state";
        dom.usersTable.textContent = "missing permission: menu:user:list";
    }
}

function configureGroupPageAccess() {
    const canList = hasMenuPermission(actionPermissions.group.list);
    setElementVisible("searchGroupsBtn", canList);
    setElementVisible("openCreateGroupBtn", hasMenuPermission(actionPermissions.group.create));
    if (!canList && dom.groupsTable) {
        dom.groupsTable.className = "table-wrap empty-state";
        dom.groupsTable.textContent = "missing permission: menu:group:list";
    }
}

function configurePermissionPageAccess() {
    const canList = hasMenuPermission(actionPermissions.permission.list);
    setElementVisible("searchPermissionsBtn", canList);
    setElementVisible("openCreatePermissionBtn", hasMenuPermission(actionPermissions.permission.create));
    if (!canList && dom.permissionsTree) {
        dom.permissionsTree.className = "tree-wrap empty-state";
        dom.permissionsTree.textContent = "missing permission: menu:permission:list";
    }
}

function flattenPermissions(nodes = [], result = []) {
    nodes.forEach((node) => {
        result.push(node);
        if (node.children?.length) {
            flattenPermissions(node.children, result);
        }
    });
    return result;
}

async function bootstrapConsole() {
    try {
        const [userRes, menusRes] = await Promise.all([
            apiFetch("/api/v1/users/me"),
            apiFetch("/api/v1/users/me/menus")
        ]);
        state.user = userRes.data || null;
        state.menus = menusRes.data || [];
        renderCurrentUserCard();
        renderSidebar();
        renderPage();
        showConsoleApp();
        updateMessageEntryPoints();
        loadMessages({ page: 1, pageSize: 6, silent: true }).catch(() => undefined);
    } catch (error) {
        setSessionId(null);
        showAuthApp();
        writeResponse("初始化失败", error);
    }
}

function openModal(title, bodyHtml) {
    dom.modalRoot.classList.remove("hidden");
    dom.modalRoot.innerHTML = `
        <div class="modal-card large-modal">
            <div class="section-header modal-header">
                <div>
                    <p class="eyebrow">Modal</p>
                    <h3 class="modal-title">${title}</h3>
                </div>
                <button class="ghost-button" id="modalCloseBtn">关闭</button>
            </div>
            <div class="modal-body">
                ${bodyHtml}
            </div>
        </div>
    `;
    document.getElementById("modalCloseBtn").addEventListener("click", closeModal);
}

function closeModal() {
    dom.modalRoot.classList.add("hidden");
    dom.modalRoot.innerHTML = "";
}

function renderAttributeList(attributes = []) {
    if (!attributes.length) {
        return `<div class="empty-inline">暂无自定义字段</div>`;
    }
    return `
        <div class="attribute-list">
            ${attributes.map((item) => `
                <article class="attribute-card">
                    <div class="section-header">
                        <div>
                            <strong>${escapeHtml(item.name || item.fieldKey || "-")}</strong>
                            <div class="tree-meta">${escapeHtml(item.fieldKey || "-")} · ${escapeHtml(item.fieldType || "-")}</div>
                        </div>
                        <span class="user-chip">${item.required ? "必填" : "可选"}</span>
                    </div>
                    <div class="attribute-meta">
                        <span>当前值：${escapeHtml(stringifyAttributeValue(item.value))}</span>
                        <span>默认值：${escapeHtml(stringifyAttributeValue(item.defaultValue))}</span>
                    </div>
                    <div class="attribute-meta">
                        <span>选项：${joinTextList(item.options)}</span>
                        <span>排序：${escapeHtml(item.sort ?? "-")}</span>
                    </div>
                    <div class="subtle">${escapeHtml(item.description || "无描述")}</div>
                </article>
            `).join("")}
        </div>
    `;
}

function buildAttributeSection(kind, targetId, attributes = [], title) {
    return `
        <section class="detail-section">
            <div class="section-header">
                <div>
                    <strong>${kind === "user" ? "个人自定义字段" : "组织自定义字段"}</strong>
                    <div class="subtle">前端已支持字段定义、字段值、默认值、选项、排序和描述</div>
                </div>
                <button class="secondary-button" data-attribute-manage="${kind}:${targetId}:${escapeHtml(title || "")}">
                    管理字段
                </button>
            </div>
            ${renderAttributeList(attributes)}
        </section>
    `;
}

async function openAttributeManager(kind, targetId, title, options = {}) {
    const basePath = kind === "user" ? `/api/v1/users/${targetId}/attributes` : `/api/v1/groups/${targetId}/attributes`;
    try {
        const data = await apiFetch(basePath);
        const attributes = data.data || [];
        openModal(`${title} · 自定义字段`, `
            <div class="stack-form">
                <div class="section-header">
                    <div>
                        <p class="eyebrow">Attributes</p>
                        <h3 class="modal-title">${escapeHtml(title)}</h3>
                    </div>
                    <button id="createAttributeBtn" class="primary-button">新增字段</button>
                </div>
                ${attributes.length ? `
                    <div class="attribute-list manage-list">
                        ${attributes.map((item) => `
                            <article class="attribute-card">
                                <div class="section-header">
                                    <div>
                                        <strong>${escapeHtml(item.name || item.fieldKey || "-")}</strong>
                                        <div class="tree-meta">${escapeHtml(item.fieldKey || "-")} · ${escapeHtml(item.fieldType || "-")}</div>
                                    </div>
                                    <div class="table-actions">
                                        <button class="mini-button" data-attribute-action="edit" data-attribute-id="${item.attributeId}">编辑</button>
                                        <button class="mini-button danger" data-attribute-action="delete" data-attribute-id="${item.attributeId}">删除</button>
                                    </div>
                                </div>
                                <div class="attribute-meta">
                                    <span>当前值：${escapeHtml(stringifyAttributeValue(item.value))}</span>
                                    <span>默认值：${escapeHtml(stringifyAttributeValue(item.defaultValue))}</span>
                                </div>
                                <div class="attribute-meta">
                                    <span>选项：${joinTextList(item.options)}</span>
                                    <span>${item.required ? "必填字段" : "非必填字段"}</span>
                                </div>
                                <div class="subtle">${escapeHtml(item.description || "无描述")}</div>
                            </article>
                        `).join("")}
                    </div>
                ` : `<div class="empty-state">还没有自定义字段，先新增一个。</div>`}
            </div>
        `);

        document.getElementById("createAttributeBtn").addEventListener("click", () => {
            openAttributeForm(kind, targetId, null, title, options.onSaved);
        });

        document.querySelectorAll("[data-attribute-action]").forEach((button) => {
            const attributeId = button.dataset.attributeId;
            const action = button.dataset.attributeAction;
            const current = attributes.find((item) => item.attributeId === attributeId);
            button.addEventListener("click", async () => {
                if (action === "edit") {
                    openAttributeForm(kind, targetId, current, title, options.onSaved);
                    return;
                }
                if (!window.confirm("确认删除这个自定义字段吗？")) {
                    return;
                }
                try {
                    await apiFetch(`${basePath}/${attributeId}`, { method: "DELETE" });
                    showToast("自定义字段已删除", "success");
                    await openAttributeManager(kind, targetId, title, options);
                    if (typeof options.onSaved === "function") {
                        await options.onSaved();
                    }
                } catch (error) {
                    showToast(resolveFeedbackMessage("删除自定义字段失败", error), "error");
                }
            });
        });
    } catch (error) {
        showToast(resolveFeedbackMessage("加载自定义字段失败", error), "error");
    }
}

function openAttributeForm(kind, targetId, attribute, title, onSaved) {
    const isEdit = Boolean(attribute?.attributeId);
    const basePath = kind === "user" ? `/api/v1/users/${targetId}/attributes` : `/api/v1/groups/${targetId}/attributes`;
    openModal(`${title} · ${isEdit ? "编辑字段" : "新增字段"}`, `
        <form id="attributeForm" class="stack-form">
            <div class="two-field">
                <label><span>字段名称</span><input id="attributeNameInput" value="${escapeHtml(attribute?.name || "")}" required></label>
                <label><span>字段键</span><input id="attributeKeyInput" value="${escapeHtml(attribute?.fieldKey || "")}" ${isEdit ? "disabled" : ""} required></label>
            </div>
            <div class="two-field">
                <label>
                    <span>字段类型</span>
                    <select id="attributeTypeInput">
                        ${["text", "textarea", "number", "boolean", "date", "enum", "multi_select", "json"].map((fieldType) => `
                            <option value="${fieldType}" ${attribute?.fieldType === fieldType ? "selected" : ""}>${fieldType}</option>
                        `).join("")}
                    </select>
                </label>
                <label>
                    <span>排序</span>
                    <input id="attributeSortInput" type="number" value="${escapeHtml(attribute?.sort ?? 0)}">
                </label>
            </div>
            <div class="two-field">
                <label><span>字段值</span><input id="attributeValueInput" value="${escapeHtml(attribute?.value == null ? "" : stringifyAttributeValue(attribute.value))}"></label>
                <label><span>默认值</span><input id="attributeDefaultValueInput" value="${escapeHtml(attribute?.defaultValue == null ? "" : stringifyAttributeValue(attribute.defaultValue))}"></label>
            </div>
            <div class="two-field">
                <label><span>选项（逗号分隔）</span><input id="attributeOptionsInput" value="${escapeHtml((attribute?.options || []).join(", "))}"></label>
                <label class="checkbox-field">
                    <span>是否必填</span>
                    <input id="attributeRequiredInput" type="checkbox" ${attribute?.required ? "checked" : ""}>
                </label>
            </div>
            <label><span>描述</span><textarea id="attributeDescriptionInput">${escapeHtml(attribute?.description || "")}</textarea></label>
            <button type="submit" class="primary-button">${isEdit ? "保存字段" : "创建字段"}</button>
        </form>
    `);

    document.getElementById("attributeForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const fieldType = document.getElementById("attributeTypeInput").value;
        const payload = {
            name: document.getElementById("attributeNameInput").value.trim(),
            fieldKey: isEdit ? attribute.fieldKey : document.getElementById("attributeKeyInput").value.trim(),
            fieldType,
            required: document.getElementById("attributeRequiredInput").checked,
            value: serializeAttributeInput(document.getElementById("attributeValueInput").value, fieldType),
            defaultValue: serializeAttributeInput(document.getElementById("attributeDefaultValueInput").value, fieldType),
            options: parseOptionsInput(document.getElementById("attributeOptionsInput").value),
            sort: Number(document.getElementById("attributeSortInput").value || 0),
            description: document.getElementById("attributeDescriptionInput").value.trim() || null
        };

        try {
            await apiFetch(isEdit ? `${basePath}/${attribute.attributeId}` : basePath, {
                method: isEdit ? "PUT" : "POST",
                body: JSON.stringify(payload)
            });
            showToast(isEdit ? "自定义字段已更新" : "自定义字段已创建", "success");
            await openAttributeManager(kind, targetId, title, { onSaved });
            if (typeof onSaved === "function") {
                await onSaved();
            }
        } catch (error) {
            showToast(resolveFeedbackMessage(isEdit ? "更新自定义字段失败" : "创建自定义字段失败", error), "error");
        }
    });
}

async function fetchGroupOptions() {
    const data = await apiFetch("/api/v1/groups?page=1&pageSize=200");
    return data.data?.list || [];
}

async function fetchGroupDetails(groupIds = []) {
    const uniqueIds = [...new Set((groupIds || []).filter(Boolean))];
    if (!uniqueIds.length) {
        return [];
    }
    const details = await Promise.all(uniqueIds.map(async (groupId) => {
        try {
            const response = await apiFetch(`/api/v1/groups/${groupId}`);
            return response.data || null;
        } catch {
            return null;
        }
    }));
    return details.filter(Boolean);
}

function buildOrganizationAttributeDisplay(groupDetails = [], userAttributes = []) {
    if (!groupDetails.length) {
        return `<section class="detail-section"><div class="empty-inline">当前没有所属组织字段</div></section>`;
    }
    const userAttributeMap = new Map((userAttributes || []).map((item) => [item.fieldKey, item]));
    return `
        <section class="detail-section">
            <div class="section-header">
                <div>
                    <strong>所属组织字段</strong>
                    <div class="subtle">组织定义的字段会直接显示在这里</div>
                </div>
            </div>
            <div class="attribute-list">
                ${groupDetails.map((group) => `
                    <article class="attribute-card">
                        <div class="section-header">
                            <div>
                                <strong>${escapeHtml(group.groupName || "-")}</strong>
                                <div class="tree-meta">${escapeHtml(group.groupId || "-")}</div>
                            </div>
                        </div>
                        ${(group.attributes || []).length ? `
                            <div class="detail-grid compact-grid">
                                ${(group.attributes || []).map((attribute) => {
                                    const userAttribute = userAttributeMap.get(attribute.fieldKey);
                                    const displayValue = userAttribute?.value ?? attribute.value ?? attribute.defaultValue ?? "";
                                    return `
                                        <div>
                                            <strong>${escapeHtml(attribute.name || attribute.fieldKey || "-")}</strong>
                                            <span>${escapeHtml(stringifyAttributeValue(displayValue))}</span>
                                        </div>
                                    `;
                                }).join("")}
                            </div>
                        ` : `<div class="empty-inline">该组织还没有自定义字段</div>`}
                    </article>
                `).join("")}
            </div>
        </section>
    `;
}

function buildOrganizationAttributeEditor(groupDetails = [], userAttributes = []) {
    if (!groupDetails.length) {
        return `<div class="empty-inline">选择所属组织后，这里会显示组织定义字段。</div>`;
    }
    const userAttributeMap = new Map((userAttributes || []).map((item) => [item.fieldKey, item]));
    return groupDetails.map((group) => `
        <section class="detail-section">
            <div class="section-header">
                <div>
                    <strong>${escapeHtml(group.groupName || "-")} 的组织字段</strong>
                    <div class="subtle">这些字段来自组织自定义属性，填写后会直接绑定到用户</div>
                </div>
            </div>
            ${(group.attributes || []).length ? `
                <div class="attribute-editor-grid">
                    ${(group.attributes || []).map((attribute) => {
                        const userAttribute = userAttributeMap.get(attribute.fieldKey);
                        const value = userAttribute?.value ?? attribute.value ?? attribute.defaultValue ?? "";
                        return `
                            <label class="attribute-input-card">
                                <span>${escapeHtml(attribute.name || attribute.fieldKey || "-")}${attribute.required ? " *" : ""}</span>
                                <input
                                    class="org-attribute-input"
                                    data-group-id="${escapeHtml(group.groupId || "")}"
                                    data-group-name="${escapeHtml(group.groupName || "")}"
                                    data-attribute-id="${escapeHtml(attribute.attributeId || "")}"
                                    data-user-attribute-id="${escapeHtml(userAttribute?.attributeId || "")}"
                                    data-field-key="${escapeHtml(attribute.fieldKey || "")}"
                                    data-field-type="${escapeHtml(attribute.fieldType || "text")}"
                                    data-name="${escapeHtml(attribute.name || "")}"
                                    data-required="${attribute.required ? "true" : "false"}"
                                    data-sort="${escapeHtml(attribute.sort ?? 0)}"
                                    data-description="${escapeHtml(attribute.description || "")}"
                                    data-options="${escapeHtml(JSON.stringify(attribute.options || []))}"
                                    value="${escapeHtml(value == null ? "" : stringifyAttributeValue(value))}"
                                    placeholder="${escapeHtml(attribute.description || attribute.fieldType || "")}"
                                >
                                <small class="subtle">${escapeHtml(attribute.fieldKey || "-")} · ${escapeHtml(attribute.fieldType || "-")}</small>
                            </label>
                        `;
                    }).join("")}
                </div>
            ` : `<div class="empty-inline">该组织还没有定义字段</div>`}
        </section>
    `).join("");
}

function renderOrganizationAttributeEditor(container, groupDetails = [], userAttributes = []) {
    if (!container) {
        return;
    }
    container.innerHTML = buildOrganizationAttributeEditor(groupDetails, userAttributes);
}

async function syncOrganizationAttributes(userId, existingUserAttributes = [], root = document) {
    const inputs = Array.from(root.querySelectorAll(".org-attribute-input"));
    if (!inputs.length) {
        return;
    }
    const existingByKey = new Map((existingUserAttributes || []).map((item) => [item.fieldKey, item]));
    for (const input of inputs) {
        const fieldType = input.dataset.fieldType || "text";
        const rawValue = input.value ?? "";
        const payload = {
            name: input.dataset.name || input.dataset.fieldKey,
            fieldKey: input.dataset.fieldKey,
            fieldType,
            required: input.dataset.required === "true",
            value: rawValue.trim() === "" ? "" : serializeAttributeInput(rawValue, fieldType),
            defaultValue: null,
            options: JSON.parse(input.dataset.options || "[]"),
            sort: Number(input.dataset.sort || 0),
            description: input.dataset.description || null
        };
        const existing = existingByKey.get(payload.fieldKey);
        if (!existing && rawValue.trim() === "" && payload.required !== true) {
            continue;
        }
        if (existing?.attributeId) {
            await apiFetch(`/api/v1/users/${userId}/attributes/${existing.attributeId}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });
            continue;
        }
        await apiFetch(`/api/v1/users/${userId}/attributes`, {
            method: "POST",
            body: JSON.stringify(payload)
        });
    }
}

function buildGroupAttributeDraftEditor(attributes = []) {
    return `
        <section class="detail-section">
            <div class="section-header">
                <div>
                    <strong>组织自定义字段</strong>
                    <div class="subtle">创建组织时可以直接把字段一并定义好</div>
                </div>
                <button type="button" id="addGroupDraftAttributeBtn" class="secondary-button">新增字段</button>
            </div>
            <div id="groupDraftAttributeList" class="attribute-list">
                ${attributes.map((attribute, index) => buildGroupDraftAttributeItem(attribute, index)).join("") || `<div class="empty-inline" id="groupDraftAttributeEmpty">还没有组织字段，点击“新增字段”开始添加。</div>`}
            </div>
        </section>
    `;
}

function buildGroupDraftAttributeItem(attribute = {}, index = Date.now()) {
    return `
        <article class="attribute-card group-draft-attribute" data-draft-index="${index}">
            <div class="section-header">
                <div>
                    <strong>字段草稿</strong>
                    <div class="tree-meta">创建组织后会自动落到组织属性表</div>
                </div>
                <button type="button" class="mini-button danger" data-remove-draft-attribute="${index}">移除</button>
            </div>
            <div class="two-field">
                <label><span>字段名称</span><input data-draft-name value="${escapeHtml(attribute.name || "")}"></label>
                <label><span>字段键</span><input data-draft-key value="${escapeHtml(attribute.fieldKey || "")}"></label>
            </div>
            <div class="two-field">
                <label>
                    <span>字段类型</span>
                    <select data-draft-type>
                        ${["text", "textarea", "number", "boolean", "date", "enum", "multi_select", "json"].map((fieldType) => `
                            <option value="${fieldType}" ${attribute.fieldType === fieldType ? "selected" : ""}>${fieldType}</option>
                        `).join("")}
                    </select>
                </label>
                <label><span>排序</span><input data-draft-sort type="number" value="${escapeHtml(attribute.sort ?? 0)}"></label>
            </div>
            <div class="two-field">
                <label><span>默认值</span><input data-draft-default value="${escapeHtml(attribute.defaultValue == null ? "" : stringifyAttributeValue(attribute.defaultValue))}"></label>
                <label><span>选项（逗号分隔）</span><input data-draft-options value="${escapeHtml((attribute.options || []).join(", "))}"></label>
            </div>
            <div class="two-field">
                <label class="checkbox-field">
                    <span>是否必填</span>
                    <input data-draft-required type="checkbox" ${attribute.required ? "checked" : ""}>
                </label>
                <div></div>
            </div>
            <label><span>描述</span><textarea data-draft-description>${escapeHtml(attribute.description || "")}</textarea></label>
        </article>
    `;
}

function wireGroupDraftAttributeEditor() {
    const list = document.getElementById("groupDraftAttributeList");
    const addButton = document.getElementById("addGroupDraftAttributeBtn");
    if (!list || !addButton) {
        return;
    }

    const bindRemoveButtons = () => {
        list.querySelectorAll("[data-remove-draft-attribute]").forEach((button) => {
            button.onclick = () => {
                const card = button.closest(".group-draft-attribute");
                card?.remove();
                if (!list.querySelector(".group-draft-attribute")) {
                    list.innerHTML = `<div class="empty-inline" id="groupDraftAttributeEmpty">还没有组织字段，点击“新增字段”开始添加。</div>`;
                }
            };
        });
    };

    addButton.onclick = () => {
        const empty = document.getElementById("groupDraftAttributeEmpty");
        if (empty) {
            empty.remove();
        }
        list.insertAdjacentHTML("beforeend", buildGroupDraftAttributeItem({}, Date.now() + Math.random()));
        bindRemoveButtons();
    };

    bindRemoveButtons();
}

function collectDraftGroupAttributes() {
    return Array.from(document.querySelectorAll(".group-draft-attribute")).map((card) => {
        const fieldType = card.querySelector("[data-draft-type]").value;
        return {
            name: card.querySelector("[data-draft-name]").value.trim(),
            fieldKey: card.querySelector("[data-draft-key]").value.trim(),
            fieldType,
            required: card.querySelector("[data-draft-required]").checked,
            value: null,
            defaultValue: serializeAttributeInput(card.querySelector("[data-draft-default]").value, fieldType),
            options: parseOptionsInput(card.querySelector("[data-draft-options]").value),
            sort: Number(card.querySelector("[data-draft-sort]").value || 0),
            description: card.querySelector("[data-draft-description]").value.trim() || null
        };
    }).filter((item) => item.name && item.fieldKey);
}

async function searchUsersForPicker(keyword) {
    if (!keyword.trim()) {
        return [];
    }
    const data = await apiFetch(`/api/v1/users?page=1&pageSize=20&keyword=${encodeURIComponent(keyword.trim())}`);
    return data.data?.list || [];
}

function renderMemberPickerState(selectedMembers = []) {
    const selectedWrap = document.getElementById("groupMemberSelectedList");
    if (!selectedWrap) {
        return;
    }
    if (!selectedMembers.length) {
        selectedWrap.innerHTML = `<div class="empty-inline">还没有选择成员，可按邮箱、手机号或昵称搜索。</div>`;
        return;
    }
    selectedWrap.innerHTML = selectedMembers.map((member) => `
        <div class="selected-user-chip">
            <div>
                <strong>${escapeHtml(member.nickname || member.userId || "-")}</strong>
                <small>${escapeHtml(member.email || member.phone || member.userId || "-")}</small>
            </div>
            <button type="button" class="mini-button danger" data-remove-member="${member.userId}">移除</button>
        </div>
    `).join("");
    selectedWrap.querySelectorAll("[data-remove-member]").forEach((button) => {
        button.addEventListener("click", () => {
            const next = (window.__groupMemberSelection || []).filter((item) => item.userId !== button.dataset.removeMember);
            window.__groupMemberSelection = next;
            renderMemberPickerState(next);
        });
    });
}

function wireMemberPicker(initialMembers = []) {
    window.__groupMemberSelection = initialMembers.slice();
    renderMemberPickerState(window.__groupMemberSelection);
    const searchInput = document.getElementById("groupMemberSearchInput");
    const resultWrap = document.getElementById("groupMemberSearchResults");
    const searchButton = document.getElementById("searchGroupMembersBtn");
    if (!searchInput || !resultWrap || !searchButton) {
        return;
    }

    const doSearch = async () => {
        const keyword = searchInput.value.trim();
        if (!keyword) {
            resultWrap.innerHTML = `<div class="empty-inline">请输入邮箱、手机号或昵称搜索成员。</div>`;
            return;
        }
        try {
            const users = await searchUsersForPicker(keyword);
            if (!users.length) {
                resultWrap.innerHTML = `<div class="empty-inline">没有找到匹配用户。</div>`;
                return;
            }
            const selectedIds = new Set((window.__groupMemberSelection || []).map((item) => item.userId));
            resultWrap.innerHTML = users.map((user) => `
                <article class="search-result-card">
                    <div>
                        <strong>${escapeHtml(user.nickname || user.userId || "-")}</strong>
                        <div class="subtle">${escapeHtml(user.email || "-")} / ${escapeHtml(user.phone || "-")}</div>
                    </div>
                    <button
                        type="button"
                        class="mini-button"
                        data-pick-member="${user.userId}"
                        ${selectedIds.has(user.userId) ? "disabled" : ""}
                    >
                        ${selectedIds.has(user.userId) ? "已选择" : "添加"}
                    </button>
                </article>
            `).join("");
            resultWrap.querySelectorAll("[data-pick-member]").forEach((button) => {
                button.addEventListener("click", () => {
                    const picked = users.find((item) => item.userId === button.dataset.pickMember);
                    if (!picked) {
                        return;
                    }
                    const current = window.__groupMemberSelection || [];
                    if (!current.some((item) => item.userId === picked.userId)) {
                        window.__groupMemberSelection = current.concat(picked);
                        renderMemberPickerState(window.__groupMemberSelection);
                        doSearch();
                    }
                });
            });
        } catch (error) {
            resultWrap.innerHTML = `<div class="empty-inline">${escapeHtml(resolveFeedbackMessage("成员搜索失败", error))}</div>`;
        }
    };

    searchButton.addEventListener("click", doSearch);
    searchInput.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            doSearch();
        }
    });
}

function getSelectedMemberIds() {
    return (window.__groupMemberSelection || []).map((item) => item.userId).filter(Boolean);
}

function normalizeMessageFilter(filter) {
    if (filter === "read") {
        return true;
    }
    if (filter === "unread") {
        return false;
    }
    return null;
}

function getMessageFilterKey(readStatus = state.messageCenter.readStatus) {
    if (readStatus === true) {
        return "read";
    }
    if (readStatus === false) {
        return "unread";
    }
    return "all";
}

function getMessageCategoryLabel(category) {
    return {
        system: "系统消息",
        security: "安全通知",
        profile: "资料更新",
        organization: "组织消息",
        permission: "权限通知"
    }[(category || "").trim()] || (category || "系统消息");
}

function mergeMessageItems(existingItems, incomingItems) {
    const merged = [...(existingItems || [])];
    const existingIds = new Set(merged.map((item) => item.messageId));
    (incomingItems || []).forEach((item) => {
        if (!existingIds.has(item.messageId)) {
            merged.push(item);
        }
    });
    return merged;
}

async function loadMessageTemplatesForSend(channel = "in_app") {
    try {
        const params = new URLSearchParams({
            page: "1",
            size: "50",
            channel
        });
        const response = await apiFetch(`/api/v1/messages/templates?${params.toString()}`);
        return response.data?.records || [];
    } catch (error) {
        showToast(resolveFeedbackMessage("加载消息模板失败", error), "error");
        return [];
    }
}

function updateMessageEntryPoints() {
    const unreadCount = Number(state.messageCenter.unreadCount || 0);
    const triggerButton = document.getElementById("messageDrawerBtn");
    if (triggerButton) {
        triggerButton.textContent = unreadCount > 0 ? `站内消息 (${unreadCount})` : "站内消息";
    }

    const sendButton = document.getElementById("openSendMessageBtn");
    if (sendButton) {
        sendButton.classList.remove("hidden");
    }
    document.querySelectorAll("[data-send-message-entry]").forEach((button) => {
        button.classList.remove("hidden");
    });

    if (dom.messageSummaryBar) {
        const total = Number(state.messageCenter.total || 0);
        const shownCount = Number((state.messageCenter.items || []).length);
        const filterLabel = {
            all: "全部消息",
            unread: "未读消息",
            read: "已读消息"
        }[getMessageFilterKey()];
        dom.messageSummaryBar.innerHTML = `
            <article class="message-stat">
                <span>当前列表</span>
                <strong>${escapeHtml(total)}</strong>
                <small>${escapeHtml(filterLabel)}共 ${escapeHtml(total)} 条</small>
            </article>
            <article class="message-stat">
                <span>未读消息</span>
                <strong>${escapeHtml(unreadCount)}</strong>
                <small>这是全局未读数量</small>
            </article>
            <article class="message-stat">
                <span>当前已加载</span>
                <strong>${escapeHtml(shownCount)}</strong>
                <small>支持继续加载更多</small>
            </article>
        `;
    }

    document.querySelectorAll("[data-message-filter]").forEach((button) => {
        button.classList.toggle("active", button.dataset.messageFilter === getMessageFilterKey());
    });

    renderDashboardMessages();
    renderMessagesPagination();
}

function renderDashboardMessages() {
    if (!dom.dashboardMessagePreview) {
        return;
    }
    const previewItems = (state.messageCenter.items || []).slice(0, 4);
    if (!previewItems.length) {
        dom.dashboardMessagePreview.className = "empty-state";
        dom.dashboardMessagePreview.textContent = "暂无站内消息";
        return;
    }

    dom.dashboardMessagePreview.className = "dashboard-message-preview";
    dom.dashboardMessagePreview.innerHTML = previewItems.map((message) => `
        <article class="dashboard-message-item ${message.read ? "is-read" : "is-unread"}" data-open-message="${message.messageId}">
            <strong>${escapeHtml(message.title || "-")}</strong>
            <small>${escapeHtml(getMessageCategoryLabel(message.category))} · ${formatDateTime(message.createdAt)}</small>
            <p>${escapeHtml((message.content || "-").slice(0, 72))}${(message.content || "").length > 72 ? "..." : ""}</p>
        </article>
    `).join("");

    dom.dashboardMessagePreview.querySelectorAll("[data-open-message]").forEach((item) => {
        item.addEventListener("click", () => openMessageDetail(item.dataset.openMessage));
    });
}

function renderMessagesPagination() {
    if (!dom.messagesPagination) {
        return;
    }
    const shownCount = (state.messageCenter.items || []).length;
    const total = Number(state.messageCenter.total || 0);
    if (!shownCount) {
        dom.messagesPagination.classList.add("hidden");
        dom.messagesPagination.innerHTML = "";
        return;
    }

    const hasMore = shownCount < total;
    dom.messagesPagination.classList.remove("hidden");
    dom.messagesPagination.innerHTML = `
        <div class="subtle">已显示 ${escapeHtml(shownCount)} / ${escapeHtml(total)} 条消息</div>
        <button id="loadMoreMessagesBtn" class="secondary-button" ${hasMore ? "" : "disabled"}>${hasMore ? "加载更多" : "已全部加载"}</button>
    `;

    const button = document.getElementById("loadMoreMessagesBtn");
    if (button && hasMore) {
        button.addEventListener("click", () => {
            loadMessages({
                page: state.messageCenter.page + 1,
                pageSize: state.messageCenter.pageSize,
                readStatus: state.messageCenter.readStatus,
                append: true
            });
        });
    }
}

async function loadMessages(options = {}) {
    const page = Number(options.page || 1);
    const pageSize = Number(options.pageSize || state.messageCenter.pageSize || 10);
    const readStatus = Object.prototype.hasOwnProperty.call(options, "readStatus")
        ? options.readStatus
        : state.messageCenter.readStatus;
    const silent = Boolean(options.silent);
    const append = Boolean(options.append);

    if (!silent && page === 1) {
        dom.messagesList.className = "empty-state";
        dom.messagesList.textContent = "加载站内消息中...";
    }

    try {
        const params = new URLSearchParams({
            page: String(page),
            pageSize: String(pageSize)
        });
        if (readStatus !== null) {
            params.set("readStatus", String(readStatus));
        }

        const response = await apiFetch(`/api/v1/messages?${params.toString()}`);
        const payload = response.data || {};
        const list = payload.list || [];
        state.messageCenter = {
            page,
            pageSize,
            readStatus,
            total: Number(payload.total || 0),
            unreadCount: Number(payload.unreadCount || 0),
            items: append ? mergeMessageItems(state.messageCenter.items, list) : list
        };
        renderMessages();
        updateMessageEntryPoints();
        return state.messageCenter;
    } catch (error) {
        if (!silent) {
            dom.messagesList.className = "empty-state";
            dom.messagesList.textContent = resolveFeedbackMessage("加载站内消息失败", error);
        }
        throw error;
    }
}

function renderMessages() {
    const messages = state.messageCenter.items || [];
    if (!messages.length) {
        dom.messagesList.className = "empty-state";
        dom.messagesList.textContent = getMessageFilterKey() === "all" ? "暂无站内消息" : "当前筛选下没有消息";
        renderMessagesPagination();
        return;
    }

    dom.messagesList.className = "message-list";
    dom.messagesList.innerHTML = messages.map((message) => `
        <article class="message-card ${message.read ? "is-read" : "is-unread"}">
            <div class="message-card-head">
                <div>
                    <strong>${escapeHtml(message.title || "-")}</strong>
                    <div class="message-meta">${escapeHtml(getMessageCategoryLabel(message.category))} · ${formatDateTime(message.createdAt)} · ${escapeHtml(message.creatorName || message.creatorId || "system")}</div>
                </div>
                ${message.read ? `<span class="user-chip">已读</span>` : `<span class="user-chip">未读</span>`}
            </div>
            <div class="message-content">${escapeHtml(message.content || "-")}</div>
            <div class="message-action-row">
                <button class="mini-button" data-open-message="${message.messageId}">查看详情</button>
                ${message.read ? "" : `<button class="mini-button" data-read-message="${message.messageId}">标为已读</button>`}
                ${message.link ? `<a class="message-link" href="${escapeHtml(message.link)}" target="_blank" rel="noopener">打开链接</a>` : ""}
            </div>
        </article>
    `).join("");

    dom.messagesList.querySelectorAll("[data-read-message]").forEach((button) => {
        button.addEventListener("click", async () => {
            try {
                await markMessagesRead([button.dataset.readMessage]);
                showToast("消息已标记为已读", "success");
            } catch (error) {
                showToast(resolveFeedbackMessage("更新消息状态失败", error), "error");
            }
        });
    });

    dom.messagesList.querySelectorAll("[data-open-message]").forEach((button) => {
        button.addEventListener("click", () => openMessageDetail(button.dataset.openMessage));
    });

    renderMessagesPagination();
}

function openMessageDetail(messageId) {
    const message = (state.messageCenter.items || []).find((item) => item.messageId === messageId);
    if (!message) {
        showToast("未找到消息详情", "error");
        return;
    }

    openModal("消息详情", `
        <div class="message-detail-grid">
            <div>
                <strong>标题</strong>
                <span>${escapeHtml(message.title || "-")}</span>
            </div>
            <div>
                <strong>分类</strong>
                <span>${escapeHtml(getMessageCategoryLabel(message.category))}</span>
            </div>
            <div>
                <strong>发送人</strong>
                <span>${escapeHtml(message.creatorName || message.creatorId || "system")}</span>
            </div>
            <div>
                <strong>发送时间</strong>
                <span>${formatDateTime(message.createdAt)}</span>
            </div>
            <div>
                <strong>阅读状态</strong>
                <span>${message.read ? "已读" : "未读"}</span>
            </div>
            <div>
                <strong>阅读时间</strong>
                <span>${formatDateTime(message.readAt)}</span>
            </div>
            <div class="full-width">
                <strong>消息内容</strong>
                <div class="message-content">${escapeHtml(message.content || "-")}</div>
            </div>
            ${message.link ? `
                <div class="full-width">
                    <strong>跳转链接</strong>
                    <a class="message-link" href="${escapeHtml(message.link)}" target="_blank" rel="noopener">${escapeHtml(message.link)}</a>
                </div>
            ` : ""}
        </div>
    `);

    if (!message.read) {
        markMessagesRead([messageId], { silent: true }).catch(() => undefined);
    }
}

async function markMessagesRead(messageIds = [], options = {}) {
    const markAll = Boolean(options.markAll);
    const payload = markAll ? { markAll: true } : { messageIds };
    const response = await apiFetch("/api/v1/messages/read", {
        method: "POST",
        body: JSON.stringify(payload)
    });
    const unreadCount = Number(response.data?.unreadCount ?? state.messageCenter.unreadCount ?? 0);
    if (state.messageCenter.readStatus !== null) {
        await loadMessages({
            page: 1,
            pageSize: state.messageCenter.pageSize,
            readStatus: state.messageCenter.readStatus,
            silent: true
        });
    } else if (markAll) {
        state.messageCenter.items = (state.messageCenter.items || []).map((item) => ({
            ...item,
            read: true,
            readAt: item.readAt || new Date().toISOString()
        }));
    } else {
        const messageIdSet = new Set(messageIds);
        state.messageCenter.items = (state.messageCenter.items || []).map((item) => {
            if (!messageIdSet.has(item.messageId)) {
                return item;
            }
            return {
                ...item,
                read: true,
                readAt: item.readAt || new Date().toISOString()
            };
        });
        renderMessages();
    }
    state.messageCenter.unreadCount = unreadCount;
    updateMessageEntryPoints();
    if (!options.silent && options.successMessage) {
        showToast(options.successMessage, "success");
    }
    return response;
}

async function openSendMessageModal() {
    openModal("发送消息", `
        <form id="sendMessageForm" class="stack-form" novalidate>
            <div class="two-field">
                <label>
                    <span>发送方式</span>
                    <select id="messageChannelInput">
                        <option value="in_app">站内信</option>
                        <option value="email">邮件</option>
                        <option value="push">推送</option>
                        <option value="feishu">飞书</option>
                    </select>
                </label>
                <label><span>业务分类</span><input id="messageCategoryInput" value="system"></label>
            </div>
            <div class="inline-field">
                <label><span>接收人搜索</span><input id="messageReceiverSearchInput" placeholder="输入邮箱、手机号或昵称"></label>
                <button type="button" id="searchMessageReceiversBtn" class="secondary-button">搜索</button>
            </div>
            <div id="messageReceiverSelectedList" class="selected-user-list"></div>
            <div id="messageReceiverSearchResults" class="search-result-list empty-inline">请输入邮箱、手机号或昵称搜索接收人。</div>
            <div class="inline-field">
                <label><span>消息模板</span><select id="messageTemplateInput"><option value="">正在加载模板...</option></select></label>
                <button type="button" id="openSelectedTemplateBtn" class="secondary-button">查看模板</button>
            </div>
            <textarea id="messageTemplateParamsInput" class="hidden">{ "title": "通知", "body": "这是一条站内消息" }</textarea>
            <div id="messageTemplateSummary" class="template-summary empty-inline">选择模板后会打开模板窗口，可查看正文并填写变量。</div>
            <div class="two-field">
                <label><span>发送策略</span><select id="messageSendModeInput"><option value="immediate">即时发送</option><option value="scheduled_once">定时发送</option></select></label>
                <label><span>计划发送时间</span><input id="messageScheduleInput" placeholder="2026-06-28T10:00:00"></label>
            </div>
            <div class="two-field">
                <label><span>优先级</span><input id="messagePriorityInput" type="number" min="1" max="9" value="5"></label>
                <label><span>最大重试</span><input id="messageRetryInput" type="number" min="0" max="10" value="3"></label>
            </div>
            <label><span>标题</span><input id="messageTitleInput" maxlength="80" required></label>
            <label><span>内容</span><textarea id="messageContentInput" rows="5" required></textarea></label>
            <label><span>跳转链接</span><input id="messageLinkInput" placeholder="可选"></label>
            <button type="submit" id="sendMessageSubmitBtn" class="primary-button">发送消息</button>
        </form>
        <div id="messageTemplateDialog" class="template-dialog hidden"></div>
    `);

    window.__messageReceiverSelection = [];
    let templates = [];
    const channelInput = document.getElementById("messageChannelInput");
    const templateSelect = document.getElementById("messageTemplateInput");
    const paramsInput = document.getElementById("messageTemplateParamsInput");
    const titleInput = document.getElementById("messageTitleInput");
    const contentInput = document.getElementById("messageContentInput");
    const categoryInput = document.getElementById("messageCategoryInput");
    const sendModeInput = document.getElementById("messageSendModeInput");
    const scheduleInput = document.getElementById("messageScheduleInput");
    const priorityInput = document.getElementById("messagePriorityInput");
    const retryInput = document.getElementById("messageRetryInput");
    const templateSummary = document.getElementById("messageTemplateSummary");
    const templateDialog = document.getElementById("messageTemplateDialog");
    const submitButton = document.getElementById("sendMessageSubmitBtn");
    const channelLabels = {
        in_app: "站内信",
        email: "邮件",
        push: "推送",
        feishu: "飞书"
    };
    const defaultParamsByChannel = {
        in_app: { title: "通知", body: "这是一条站内消息" },
        email: { subject: "通知", body: "这是一封邮件通知", title: "通知" },
        push: { title: "通知", body: "这是一条推送通知" },
        feishu: { title: "通知", body: "这是一条飞书通知" }
    };
    let currentTemplateParams = defaultParamsByChannel.in_app;
    const renderTemplateOptions = () => {
        templateSelect.innerHTML = templates.length
            ? `<option value="">请选择模板</option>${templates.map((template) => `
                <option value="${escapeHtml(template.templateCode || "")}" data-template-id="${escapeHtml(template.templateId || "")}">
                    ${escapeHtml(template.name || template.templateCode || "-")}
                </option>
            `).join("")}`
            : `<option value="">当前发送方式没有可用模板</option>`;
    };
    const parseMaybeJsonObject = (value) => {
        if (!value) {
            return {};
        }
        if (typeof value === "string") {
            try {
                return JSON.parse(value);
            } catch {
                return { body: value };
            }
        }
        return value;
    };
    const resolveTemplateContent = (template) => {
        const rawContent = parseMaybeJsonObject(template?.content || template?.contentJson || template?.templateContent || {});
        const channelContent = rawContent && typeof rawContent === "object" && rawContent[channelInput.value]
            ? rawContent[channelInput.value]
            : rawContent;
        const content = parseMaybeJsonObject(channelContent);
        if (typeof content === "string") {
            return { title: template?.name || "", body: content };
        }
        return {
            title: content?.title || content?.subject || "",
            body: content?.body || content?.content || content?.text || content?.html || ""
        };
    };
    const extractTemplateVariables = (template) => {
        const schema = template?.paramsSchema || template?.variableDefinition || template?.variables || {};
        const names = new Set(Object.keys(schema || {}));
        const content = resolveTemplateContent(template);
        const source = `${content.title || ""}\n${content.body || ""}`;
        const mustachePattern = /\{\{\{?\s*([a-zA-Z_][a-zA-Z0-9_]*(?:\.[a-zA-Z_][a-zA-Z0-9_]*|\[[0-9]+\])*)[^}]*\}\}\}?/g;
        const dollarPattern = /\$\{\s*([a-zA-Z_][a-zA-Z0-9_]*)\s*\}/g;
        let match = mustachePattern.exec(source);
        while (match) {
            names.add(match[1]);
            match = mustachePattern.exec(source);
        }
        match = dollarPattern.exec(source);
        while (match) {
            names.add(match[1]);
            match = dollarPattern.exec(source);
        }
        return Array.from(names);
    };
    const defaultTemplateParamValue = (key) => {
        const normalizedKey = key.toLowerCase();
        if (["title", "subject"].includes(normalizedKey)) {
            return titleInput.value || "通知";
        }
        if (["body", "content", "message"].includes(normalizedKey)) {
            return contentInput.value || "这是一条消息";
        }
        if (normalizedKey === "code") {
            return "123456";
        }
        if (normalizedKey === "number") {
            return "10";
        }
        return currentTemplateParams?.[key] || "";
    };
    const buildTemplateParams = (template) => {
        const keys = extractTemplateVariables(template);
        if (!keys.length) {
            return defaultParamsByChannel[channelInput.value] || {};
        }
        return keys.reduce((result, key) => {
            result[key] = defaultTemplateParamValue(key);
            return result;
        }, {});
    };
    const syncParamsInput = () => {
        paramsInput.value = stringifyPretty(currentTemplateParams, {});
    };
    const syncDefaultParams = () => {
        currentTemplateParams = defaultParamsByChannel[channelInput.value] || defaultParamsByChannel.in_app;
        syncParamsInput();
        templateSummary.className = "template-summary empty-inline";
        templateSummary.textContent = "选择模板后会打开模板窗口，可查看正文并填写变量。";
    };
    const loadTemplatesForCurrentChannel = async () => {
        templateSelect.innerHTML = `<option value="">正在加载模板...</option>`;
        templates = await loadMessageTemplatesForSend(channelInput.value);
        renderTemplateOptions();
        syncDefaultParams();
    };
    const closeTemplateDialog = () => {
        templateDialog.classList.add("hidden");
        templateDialog.innerHTML = "";
    };
    const applyTemplate = async (template) => {
        const params = {};
        templateDialog.querySelectorAll("[data-template-param]").forEach((input) => {
            params[input.dataset.templateParam] = input.value;
        });
        currentTemplateParams = Object.keys(params).length ? params : buildTemplateParams(template);
        syncParamsInput();
        try {
            const response = await apiFetch("/api/v1/messages/templates/preview", {
                method: "POST",
                body: JSON.stringify({
                    templateId: template.templateId,
                    channel: channelInput.value,
                    params: currentTemplateParams
                })
            });
            const preview = response.data || {};
            if (preview.title) {
                titleInput.value = preview.title;
            }
            if (preview.body || preview.rendered) {
                contentInput.value = preview.body || preview.rendered;
            }
            templateSummary.className = "template-summary";
            templateSummary.innerHTML = `
                <strong>已应用模板：${escapeHtml(template.name || template.templateCode || "-")}</strong>
                <span>${escapeHtml(preview.body || preview.rendered || "模板已应用")}</span>
            `;
            closeTemplateDialog();
        } catch (error) {
            showToast(resolveFeedbackMessage("应用模板失败", error), "error");
        }
    };
    const openMessageTemplateDialog = (template) => {
        if (!template?.templateId) {
            showToast("请选择一个模板", "error");
            return;
        }
        const content = resolveTemplateContent(template);
        currentTemplateParams = buildTemplateParams(template);
        syncParamsInput();
        const paramKeys = Object.keys(currentTemplateParams);
        templateDialog.classList.remove("hidden");
        templateDialog.innerHTML = `
            <div class="modal-card template-dialog-card">
                <div class="section-header">
                    <div>
                        <p class="eyebrow">Template</p>
                        <h3 class="modal-title">${escapeHtml(template.name || template.templateCode || "消息模板")}</h3>
                    </div>
                    <button type="button" class="ghost-button" id="closeMessageTemplateDialogBtn">关闭</button>
                </div>
                <div class="message-detail-grid">
                    <div>
                        <strong>模板编码</strong>
                        <span>${escapeHtml(template.templateCode || "-")}</span>
                    </div>
                    <div>
                        <strong>发送方式</strong>
                        <span>${escapeHtml(channelLabels[channelInput.value] || channelInput.value)}</span>
                    </div>
                    <div class="full-width">
                        <strong>模板标题</strong>
                        <div class="template-content-preview">${escapeHtml(content.title || "-")}</div>
                    </div>
                    <div class="full-width">
                        <strong>模板正文</strong>
                        <div class="template-content-preview">${escapeHtml(content.body || "-")}</div>
                    </div>
                </div>
                <div class="detail-section">
                    <div class="section-header">
                        <div>
                            <strong>模板变量</strong>
                            <div class="subtle">填写变量后应用模板，系统会把渲染后的标题和正文回填到发送表单。</div>
                        </div>
                    </div>
                    <div class="template-param-grid">
                        ${paramKeys.length ? paramKeys.map((key) => `
                            <label>
                                <span>${escapeHtml(key)}</span>
                                <input data-template-param="${escapeHtml(key)}" value="${escapeHtml(currentTemplateParams[key] || "")}">
                            </label>
                        `).join("") : `<div class="empty-inline">这个模板没有需要填写的变量。</div>`}
                    </div>
                </div>
                <div class="modal-actions">
                    <button type="button" class="secondary-button" id="cancelMessageTemplateDialogBtn">取消</button>
                    <button type="button" class="primary-button" id="applySelectedTemplateBtn">应用模板</button>
                </div>
            </div>
        `;
        document.getElementById("closeMessageTemplateDialogBtn").addEventListener("click", closeTemplateDialog);
        document.getElementById("cancelMessageTemplateDialogBtn").addEventListener("click", closeTemplateDialog);
        document.getElementById("applySelectedTemplateBtn").addEventListener("click", () => applyTemplate(template));
    };
    const buildRecipient = (user) => {
        return user.userId ? { type: "user_id", value: user.userId } : null;
    };
    const renderReceivers = () => {
        const wrap = document.getElementById("messageReceiverSelectedList");
        const selected = window.__messageReceiverSelection || [];
        if (!selected.length) {
            wrap.innerHTML = `<div class="empty-inline">还没有选择接收人。</div>`;
            return;
        }
        const invalidCount = selected.filter((user) => !buildRecipient(user)).length;
        wrap.innerHTML = `
            <div class="subtle">已选择 ${escapeHtml(selected.length)} 位接收人${invalidCount ? `，其中 ${escapeHtml(invalidCount)} 位缺少用户ID` : ""}</div>
            ${selected.map((user) => `
                <div class="selected-user-chip">
                    <div>
                        <strong>${escapeHtml(user.nickname || user.userId || "-")}</strong>
                        <small>${escapeHtml(user.email || "-")} / ${escapeHtml(user.phone || "-")}</small>
                    </div>
                    <button type="button" class="mini-button danger" data-remove-receiver="${user.userId}">移除</button>
                </div>
            `).join("")}
        `;
        wrap.querySelectorAll("[data-remove-receiver]").forEach((button) => {
            button.addEventListener("click", () => {
                window.__messageReceiverSelection = (window.__messageReceiverSelection || []).filter((item) => item.userId !== button.dataset.removeReceiver);
                renderReceivers();
            });
        });
    };

    const doSearch = async () => {
        const keyword = document.getElementById("messageReceiverSearchInput").value.trim();
        const resultWrap = document.getElementById("messageReceiverSearchResults");
        if (!keyword) {
            resultWrap.innerHTML = `<div class="empty-inline">请输入邮箱、手机号或昵称搜索接收人。</div>`;
            return;
        }
        try {
            const users = await searchUsersForPicker(keyword);
            const selectedIds = new Set((window.__messageReceiverSelection || []).map((item) => item.userId));
            resultWrap.innerHTML = users.length ? users.map((user) => `
                <article class="search-result-card">
                    <div>
                        <strong>${escapeHtml(user.nickname || user.userId || "-")}</strong>
                        <div class="subtle">${escapeHtml(user.email || "-")} / ${escapeHtml(user.phone || "-")}</div>
                    </div>
                    <button type="button" class="mini-button" data-pick-receiver="${user.userId}" ${selectedIds.has(user.userId) ? "disabled" : ""}>
                        ${selectedIds.has(user.userId) ? "已选择" : "添加"}
                    </button>
                </article>
            `).join("") : `<div class="empty-inline">没有找到匹配用户。</div>`;
            resultWrap.querySelectorAll("[data-pick-receiver]").forEach((button) => {
                button.addEventListener("click", () => {
                    const picked = users.find((item) => item.userId === button.dataset.pickReceiver);
                    if (!picked) {
                        return;
                    }
                    const current = window.__messageReceiverSelection || [];
                    if (!current.some((item) => item.userId === picked.userId)) {
                        window.__messageReceiverSelection = current.concat(picked);
                        renderReceivers();
                        doSearch();
                    }
                });
            });
        } catch (error) {
            resultWrap.innerHTML = `<div class="empty-inline">${escapeHtml(resolveFeedbackMessage("搜索接收人失败", error))}</div>`;
        }
    };

    renderReceivers();
    const syncMessageSendMode = () => {
        const scheduled = sendModeInput.value === "scheduled_once";
        scheduleInput.disabled = !scheduled;
        scheduleInput.required = scheduled;
        if (!scheduled) {
            scheduleInput.value = "";
        }
    };
    sendModeInput.addEventListener("change", syncMessageSendMode);
    syncMessageSendMode();
    channelInput.addEventListener("change", () => {
        loadTemplatesForCurrentChannel();
        renderReceivers();
    });
    templateSelect.addEventListener("change", () => {
        const selected = templates.find((item) => item.templateCode === templateSelect.value);
        categoryInput.value = selected?.businessCode || "system";
        if (selected) {
            templateSummary.className = "template-summary empty-inline";
            templateSummary.textContent = "已选择模板，请在模板窗口中查看正文并应用。";
            openMessageTemplateDialog(selected);
        } else {
            syncDefaultParams();
        }
    });
    document.getElementById("openSelectedTemplateBtn").addEventListener("click", () => {
        const templateCode = templateSelect.value;
        const selected = templates.find((item) => item.templateCode === templateCode);
        openMessageTemplateDialog(selected);
    });
    document.getElementById("searchMessageReceiversBtn").addEventListener("click", doSearch);
    document.getElementById("messageReceiverSearchInput").addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            doSearch();
        }
    });
    document.getElementById("sendMessageForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const receivers = window.__messageReceiverSelection || [];
        if (!receivers.length) {
            showToast("请至少选择一个接收人", "error");
            return;
        }
        const title = titleInput.value.trim();
        const content = contentInput.value.trim();
        if (!title) {
            titleInput.focus();
            showToast("请填写消息标题", "error");
            return;
        }
        if (!content) {
            contentInput.focus();
            showToast("请填写消息内容，或先应用消息模板", "error");
            return;
        }
        const recipients = receivers.map(buildRecipient).filter(Boolean);
        if (recipients.length !== receivers.length) {
            showToast(`当前${channelLabels[channelInput.value]}发送方式下，有接收人缺少必要联系方式`, "error");
            return;
        }
        const channel = channelInput.value;
        const selectedTemplateCode = document.getElementById("messageTemplateInput").value;
        if (channel !== "in_app" && !selectedTemplateCode) {
            showToast(`${channelLabels[channel]}发送必须选择模板`, "error");
            return;
        }
        if (sendModeInput.value === "scheduled_once" && !scheduleInput.value.trim()) {
            showToast("定时发送必须填写计划发送时间", "error");
            scheduleInput.focus();
            return;
        }
        submitButton.disabled = true;
        submitButton.textContent = "发送中...";
        try {
            await apiFetch("/api/v1/messages/send", {
                method: "POST",
                body: JSON.stringify({
                    userIds: receivers.map((item) => item.userId),
                    recipients,
                    channel,
                    title,
                    content,
                    category: categoryInput.value.trim() || "system",
                    businessCode: categoryInput.value.trim() || "system",
                    link: document.getElementById("messageLinkInput").value.trim() || null,
                    templateCode: selectedTemplateCode || null,
                    scheduleAt: sendModeInput.value === "scheduled_once" ? scheduleInput.value.trim() : null,
                    strategy: {
                        priority: Number(priorityInput.value || 5),
                        retry: Number(retryInput.value || 3)
                    },
                    params: parseJsonInput("messageTemplateParamsInput", {})
                })
            });
            closeModal();
            showToast(`${channelLabels[channel] || "消息"}已发送`, "success");
            if (channel === "in_app") {
                loadMessages({ page: 1, pageSize: state.messageCenter.pageSize });
            }
        } catch (error) {
            showToast(resolveFeedbackMessage("发送消息失败", error), "error");
            submitButton.disabled = false;
            submitButton.textContent = "发送消息";
        }
    });
    loadTemplatesForCurrentChannel();
}

async function markAllMessagesRead() {
    try {
        await markMessagesRead([], {
            markAll: true,
            successMessage: "全部消息已标为已读"
        });
    } catch (error) {
        showToast(resolveFeedbackMessage("全部已读失败", error), "error");
    }
}

function setMessageFilter(filter) {
    loadMessages({
        page: 1,
        pageSize: state.messageCenter.pageSize,
        readStatus: normalizeMessageFilter(filter)
    }).catch((error) => {
        showToast(resolveFeedbackMessage("切换消息筛选失败", error), "error");
    });
}

function openMessageDrawer() {
    const drawer = document.getElementById("messageDrawer");
    if (!drawer) {
        return;
    }
    drawer.classList.remove("hidden");
    loadMessages({ page: 1, pageSize: 10 }).catch(() => undefined);
}

function closeMessageDrawer() {
    const drawer = document.getElementById("messageDrawer");
    if (!drawer) {
        return;
    }
    drawer.classList.add("hidden");
}

function getNotificationChannelLabel(channel) {
    return {
        in_app: "站内",
        email: "邮件",
        sms: "短信",
        push: "推送",
        wechat: "微信",
        feishu: "飞书"
    }[channel] || channel || "-";
}

function parseJsonInput(elementId, fallback = {}) {
    const rawValue = document.getElementById(elementId)?.value.trim();
    if (!rawValue) {
        return fallback;
    }
    return JSON.parse(rawValue);
}

function stringifyPretty(value, fallback = {}) {
    return JSON.stringify(value ?? fallback, null, 2);
}

function formatDateTimeInput(value) {
    if (!value) {
        return "";
    }
    return String(value).replace(" ", "T");
}

function statusPill(status, extraClass = "") {
    const normalized = String(status ?? "").toLowerCase();
    return `<span class="status-pill is-${escapeHtml(normalized)} ${extraClass}">${escapeHtml(status || "-")}</span>`;
}

function getAdminNotificationPath(path) {
    return `/api/v1/admin/notifications${path}`;
}

function getAdminLogPath(path) {
    return `/api/v1/admin/logs${path}`;
}

function toApiTime(value) {
    if (!value) {
        return "";
    }
    return new Date(value).toISOString();
}

function setDefaultLogTimes() {
    const end = new Date();
    const start = new Date(end.getTime() - 24 * 60 * 60 * 1000);
    const toLocalInput = (date) => {
        const offset = date.getTimezoneOffset() * 60000;
        return new Date(date.getTime() - offset).toISOString().slice(0, 16);
    };
    [
        ["logStartInput", start],
        ["metricStartInput", start],
        ["logEndInput", end],
        ["metricEndInput", end]
    ].forEach(([id, date]) => {
        const input = document.getElementById(id);
        if (input && !input.value) {
            input.value = toLocalInput(date);
        }
    });
}

function compactPayload(payload) {
    return Object.fromEntries(Object.entries(payload).filter(([, value]) => value !== "" && value != null && !Number.isNaN(value)));
}

function optionalNumber(value) {
    const text = String(value ?? "").trim();
    if (!text) {
        return null;
    }
    const numberValue = Number(text);
    return Number.isNaN(numberValue) ? null : numberValue;
}

function switchLogTab(tab) {
    state.logOps.tab = tab;
    document.querySelectorAll("[data-log-tab]").forEach((button) => {
        button.classList.toggle("active", button.dataset.logTab === tab);
    });
    document.querySelectorAll(".log-pane").forEach((pane) => pane.classList.remove("active"));
    const pane = document.getElementById(`log${tab[0].toUpperCase()}${tab.slice(1)}Pane`);
    if (pane) {
        pane.classList.add("active");
    }
    renderLogOps();
}

function renderLogOps() {
    if (!hasMenuPermission("menu:logManagement")) {
        showToast("missing permission: menu:logManagement", "error");
        return;
    }
    setDefaultLogTimes();
    switch (state.logOps.tab) {
        case "metrics":
            if (!state.logOps.metrics.length) {
                loadLogMetric();
            }
            break;
        case "alerts":
            loadAlertRules();
            break;
        case "triggers":
            loadAlertTriggers();
            break;
        default:
            if (!state.logOps.logs.length) {
                loadAccessLogs();
            }
            break;
    }
}

async function loadAccessLogs() {
    if (!dom.logsTable) {
        return;
    }
    dom.logsTable.className = "table-wrap empty-state";
    dom.logsTable.textContent = "正在加载访问日志...";
    const payload = compactPayload({
        start_time: toApiTime(document.getElementById("logStartInput")?.value),
        end_time: toApiTime(document.getElementById("logEndInput")?.value),
        service: document.getElementById("logServiceInput")?.value.trim(),
        api_endpoint: document.getElementById("logApiInput")?.value.trim(),
        http_method: document.getElementById("logMethodInput")?.value,
        status_code: optionalNumber(document.getElementById("logStatusInput")?.value),
        client_ip: document.getElementById("logClientIpInput")?.value.trim(),
        trace_id: document.getElementById("logTraceIdInput")?.value.trim(),
        user_id: document.getElementById("logUserIdInput")?.value.trim(),
        page: 1,
        page_size: Number(document.getElementById("logPageSizeInput")?.value || 50)
    });
    try {
        const response = await apiFetch(getAdminLogPath("/query"), {
            method: "POST",
            body: JSON.stringify(payload)
        });
        const rows = response.data?.list || response.data?.data?.list || [];
        state.logOps.logs = rows;
        renderAccessLogs(rows);
    } catch (error) {
        dom.logsTable.textContent = resolveFeedbackMessage("load access logs failed", error);
    }
}

function renderAccessLogs(rows) {
    renderTable(dom.logsTable, [
        { label: "时间", render: (row) => formatDateTime(row.timestamp) },
        { label: "服务", render: (row) => escapeHtml(row.microservice || row.service || "-") },
        { label: "接口", render: (row) => escapeHtml(row.api_endpoint || row.apiEndpoint || "-") },
        { label: "方法", render: (row) => escapeHtml(row.http_method || row.httpMethod || "-") },
        { label: "状态", render: (row) => statusPill(row.status_code || row.statusCode || "-") },
        { label: "耗时 ms", render: (row) => escapeHtml(row.latency_ms ?? row.latencyMs ?? "-") },
        { label: "Client IP", render: (row) => escapeHtml(row.client_ip || row.clientIp || "-") },
        { label: "User ID", render: (row) => escapeHtml(row.user_id || row.userId || "-") },
        { label: "Trace", render: (row) => escapeHtml(row.trace_id || row.traceId || "-") }
    ], rows);
}

function buildMetricPayload() {
    return compactPayload({
        start_time: toApiTime(document.getElementById("metricStartInput")?.value),
        end_time: toApiTime(document.getElementById("metricEndInput")?.value),
        window_size: document.getElementById("metricWindowInput")?.value.trim() || "1m",
        service: document.getElementById("metricServiceInput")?.value.trim(),
        api_endpoint: document.getElementById("metricApiInput")?.value.trim(),
        downstream_service: document.getElementById("metricDownstreamInput")?.value.trim(),
        error_type: document.getElementById("metricErrorTypeInput")?.value,
        percentile: document.getElementById("metricPercentileInput")?.value,
        client_ip: document.getElementById("metricClientIpInput")?.value.trim(),
        user_id: document.getElementById("metricUserIdInput")?.value.trim(),
        min_qps: optionalNumber(document.getElementById("metricMinQpsInput")?.value),
        timeout_threshold_ms: optionalNumber(document.getElementById("metricTimeoutInput")?.value)
    });
}

function metricProxyPath(metricType) {
    return {
        request_rate: "/metrics/request-rate",
        request_timeout_rate: "/metrics/timeout-rate",
        request_error_rate: "/metrics/error-rate",
        request_latency: "/metrics/latency",
        downstream_error_rate: "/metrics/downstream-error-rate",
        downstream_latency: "/metrics/downstream-latency",
        ip_request_rate: "/metrics/ip-request-rate",
        user_request_rate: "/metrics/user-request-rate"
    }[metricType] || "/metrics/request-rate";
}

async function loadLogMetric() {
    if (!dom.logMetricsTable) {
        return;
    }
    dom.logMetricsTable.className = "table-wrap empty-state";
    dom.logMetricsTable.textContent = "正在加载指标...";
    const metricType = document.getElementById("logMetricTypeInput")?.value || "request_rate";
    try {
        const response = await apiFetch(getAdminLogPath(metricProxyPath(metricType)), {
            method: "POST",
            body: JSON.stringify(buildMetricPayload())
        });
        const series = response.data || {};
        const rows = series.points || [];
        state.logOps.metrics = rows;
        renderMetricSeries(series, rows);
    } catch (error) {
        renderMetricChart({}, []);
        dom.logMetricsTable.textContent = resolveFeedbackMessage("load metric failed", error);
    }
}

function renderMetricSeries(series, rows) {
    const values = rows.map((row) => Number(row.value)).filter((value) => !Number.isNaN(value));
    const max = values.length ? Math.max(...values) : 0;
    const avg = values.length ? values.reduce((sum, value) => sum + value, 0) / values.length : 0;
    if (dom.logMetricSummary) {
        dom.logMetricSummary.innerHTML = `
            <span>metric: ${escapeHtml(series.metric_name || series.metricName || "-")}</span>
            <span>unit: ${escapeHtml(series.unit || "-")}</span>
            <span>points: ${rows.length}</span>
            <span>max: ${escapeHtml(max.toFixed(2))}</span>
            <span>avg: ${escapeHtml(avg.toFixed(2))}</span>
        `;
    }
    renderMetricChart(series, rows);
    renderTable(dom.logMetricsTable, [
        { label: "开始", render: (row) => formatDateTime(row.bucket_start || row.bucketStart) },
        { label: "结束", render: (row) => formatDateTime(row.bucket_end || row.bucketEnd) },
        { label: "分组", render: (row) => escapeHtml(row.group_key || row.groupKey || row.downstream_service || row.downstreamService || row.client_ip || row.clientIp || row.user_id || row.userId || "-") },
        { label: "值", render: (row) => escapeHtml(row.value ?? "-") },
        { label: "总数", render: (row) => escapeHtml(row.total ?? "-") },
        { label: "命中", render: (row) => escapeHtml(row.matched ?? "-") }
    ], rows);
}

function getMetricRowTime(row) {
    const raw = row.bucket_start || row.bucketStart || row.bucket_end || row.bucketEnd;
    const date = raw ? new Date(raw) : null;
    return date && !Number.isNaN(date.getTime()) ? date.getTime() : null;
}

function getMetricGroupKey(row) {
    return String(row.group_key || row.groupKey || row.downstream_service || row.downstreamService || row.client_ip || row.clientIp || row.user_id || row.userId || "total");
}

function getMetricDisplayName(series) {
    return series.metric_name || series.metricName || document.getElementById("logMetricTypeInput")?.selectedOptions?.[0]?.textContent || "Metric";
}

function formatMetricAxisValue(value) {
    const abs = Math.abs(value);
    if (abs >= 1000) {
        return value.toFixed(0);
    }
    if (abs >= 10) {
        return value.toFixed(1);
    }
    return value.toFixed(2);
}

function formatMetricChartTime(time) {
    if (!time) {
        return "-";
    }
    return new Date(time).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

function renderMetricChart(series, rows) {
    if (!dom.logMetricChart) {
        return;
    }
    metricChartState = { series, rows, points: [] };
    if (dom.logMetricChartTitle) {
        dom.logMetricChartTitle.textContent = getMetricDisplayName(series);
    }
    if (dom.logMetricChartMeta) {
        const unit = series.unit || "-";
        const windowSize = series.window_size || series.windowSize || document.getElementById("metricWindowInput")?.value || "-";
        dom.logMetricChartMeta.textContent = `${unit} / ${windowSize}`;
    }
    if (dom.logMetricChartEmpty) {
        dom.logMetricChartEmpty.classList.toggle("hidden", rows.length > 0);
    }
    drawMetricChart();
}

function drawMetricChart() {
    const canvas = dom.logMetricChart;
    if (!canvas) {
        return;
    }
    const rect = canvas.getBoundingClientRect();
    const width = Math.max(1, Math.floor(rect.width || canvas.clientWidth || 0));
    const height = Math.max(1, Math.floor(rect.height || canvas.clientHeight || 0));
    const ratio = window.devicePixelRatio || 1;
    canvas.width = Math.floor(width * ratio);
    canvas.height = Math.floor(height * ratio);
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;
    const ctx = canvas.getContext("2d");
    ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
    ctx.clearRect(0, 0, width, height);

    const rows = metricChartState.rows || [];
    const parsed = rows
        .map((row) => ({
            row,
            time: getMetricRowTime(row),
            value: Number(row.value),
            group: getMetricGroupKey(row)
        }))
        .filter((point) => point.time !== null && !Number.isNaN(point.value))
        .sort((a, b) => a.time - b.time);

    if (!parsed.length) {
        metricChartState.points = [];
        renderMetricLegend([]);
        return;
    }

    const groups = Array.from(parsed.reduce((map, point) => {
        if (!map.has(point.group)) {
            map.set(point.group, []);
        }
        map.get(point.group).push(point);
        return map;
    }, new Map()).entries()).slice(0, 8);
    const colors = ["#1f6f68", "#bc4b51", "#20486e", "#c77d2b", "#6a4c93", "#2f855a", "#b7791f", "#805ad5"];
    const minTime = Math.min(...parsed.map((point) => point.time));
    const maxTime = Math.max(...parsed.map((point) => point.time));
    const minValue = Math.min(0, ...parsed.map((point) => point.value));
    const maxValue = Math.max(...parsed.map((point) => point.value));
    const valueSpan = maxValue === minValue ? 1 : maxValue - minValue;
    const timeSpan = maxTime === minTime ? 1 : maxTime - minTime;
    const padding = { top: 20, right: 24, bottom: 42, left: 58 };
    const chartWidth = Math.max(1, width - padding.left - padding.right);
    const chartHeight = Math.max(1, height - padding.top - padding.bottom);
    const x = (time) => padding.left + ((time - minTime) / timeSpan) * chartWidth;
    const y = (value) => padding.top + chartHeight - ((value - minValue) / valueSpan) * chartHeight;

    ctx.lineWidth = 1;
    ctx.strokeStyle = "rgba(20, 40, 29, 0.08)";
    ctx.fillStyle = "#5b7067";
    ctx.font = "12px Segoe UI, sans-serif";
    ctx.textAlign = "right";
    ctx.textBaseline = "middle";
    for (let i = 0; i <= 4; i += 1) {
        const value = minValue + (valueSpan * i) / 4;
        const yy = y(value);
        ctx.beginPath();
        ctx.moveTo(padding.left, yy);
        ctx.lineTo(width - padding.right, yy);
        ctx.stroke();
        ctx.fillText(formatMetricAxisValue(value), padding.left - 10, yy);
    }

    ctx.textAlign = "center";
    ctx.textBaseline = "top";
    for (let i = 0; i <= 3; i += 1) {
        const time = minTime + (timeSpan * i) / 3;
        const xx = x(time);
        ctx.fillText(formatMetricChartTime(time), xx, height - padding.bottom + 18);
    }

    const hitPoints = [];
    groups.forEach(([group, points], index) => {
        const color = colors[index % colors.length];
        ctx.strokeStyle = color;
        ctx.lineWidth = 2.5;
        ctx.beginPath();
        points.forEach((point, pointIndex) => {
            point.x = x(point.time);
            point.y = y(point.value);
            point.color = color;
            point.group = group;
            if (pointIndex === 0) {
                ctx.moveTo(point.x, point.y);
            } else {
                ctx.lineTo(point.x, point.y);
            }
            hitPoints.push(point);
        });
        ctx.stroke();

        ctx.fillStyle = color;
        points.forEach((point) => {
            ctx.beginPath();
            ctx.arc(point.x, point.y, 3, 0, Math.PI * 2);
            ctx.fill();
        });
    });

    ctx.strokeStyle = "rgba(20, 40, 29, 0.22)";
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(padding.left, padding.top);
    ctx.lineTo(padding.left, height - padding.bottom);
    ctx.lineTo(width - padding.right, height - padding.bottom);
    ctx.stroke();

    metricChartState.points = hitPoints;
    renderMetricLegend(groups.map(([group], index) => ({ group, color: colors[index % colors.length] })));
}

function renderMetricLegend(items) {
    if (!dom.logMetricChartLegend) {
        return;
    }
    dom.logMetricChartLegend.innerHTML = items
        .map((item) => `<span><i style="background:${item.color}"></i>${escapeHtml(item.group)}</span>`)
        .join("");
}

function updateMetricChartTooltip(event) {
    if (!dom.logMetricChart || !dom.logMetricChartTooltip || !metricChartState.points.length) {
        return;
    }
    const rect = dom.logMetricChart.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;
    let nearest = null;
    let nearestDistance = Infinity;
    metricChartState.points.forEach((point) => {
        const distance = Math.hypot(point.x - mouseX, point.y - mouseY);
        if (distance < nearestDistance) {
            nearest = point;
            nearestDistance = distance;
        }
    });
    if (!nearest || nearestDistance > 28) {
        dom.logMetricChartTooltip.classList.add("hidden");
        return;
    }
    const tooltip = dom.logMetricChartTooltip;
    tooltip.innerHTML = `
        <strong>${escapeHtml(nearest.group)}</strong>
        <span><em>time</em><b>${escapeHtml(formatDateTime(nearest.row.bucket_start || nearest.row.bucketStart))}</b></span>
        <span><em>value</em><b>${escapeHtml(nearest.value)}</b></span>
    `;
    const left = Math.min(Math.max(nearest.x + 12, 8), rect.width - 180);
    const top = Math.max(nearest.y - 54, 8);
    tooltip.style.left = `${left}px`;
    tooltip.style.top = `${top}px`;
    tooltip.classList.remove("hidden");
}

async function loadAlertRules() {
    if (!dom.alertRulesTable) {
        return;
    }
    dom.alertRulesTable.className = "table-wrap empty-state";
    dom.alertRulesTable.textContent = "正在加载告警规则...";
    const params = new URLSearchParams({ page: "1", pageSize: "50" });
    const metricType = document.getElementById("alertMetricFilter")?.value;
    const service = document.getElementById("alertServiceFilter")?.value.trim();
    const enabled = document.getElementById("alertEnabledFilter")?.value;
    if (metricType) params.set("metricType", metricType);
    if (service) params.set("service", service);
    if (enabled) params.set("enabled", enabled);
    try {
        const response = await apiFetch(getAdminLogPath(`/alerts/rules?${params.toString()}`));
        const rows = response.data?.list || response.data?.data?.list || [];
        state.logOps.alertRules = rows;
        renderAlertRules(rows);
    } catch (error) {
        dom.alertRulesTable.textContent = resolveFeedbackMessage("load alert rules failed", error);
    }
}

function renderAlertRules(rows) {
    renderTable(dom.alertRulesTable, [
        { label: "规则", render: (row) => `<strong>${escapeHtml(row.rule_name || row.ruleName || "-")}</strong><div class="tree-meta">${escapeHtml(row.id || "-")}</div>` },
        { label: "指标", render: (row) => escapeHtml(row.metric_type || row.metricType || "-") },
        { label: "范围", render: (row) => escapeHtml([row.service, row.api_endpoint || row.apiEndpoint, row.downstream_service || row.downstreamService, row.client_ip || row.clientIp, row.user_id || row.userId].filter(Boolean).join(" / ") || "-") },
        { label: "条件", render: (row) => escapeHtml(`${row.window_size || row.windowSize || "-"} ${row.comparator || ""} ${row.threshold ?? "-"}`) },
        { label: "连续", render: (row) => escapeHtml(row.consecutive_times || row.consecutiveTimes || 1) },
        { label: "状态", render: (row) => statusPill(row.enabled ? "enabled" : "disabled") },
        { label: "最近值", render: (row) => escapeHtml(row.last_value ?? row.lastValue ?? "-") },
        { label: "触发", render: (row) => escapeHtml(row.trigger_count ?? row.triggerCount ?? 0) },
        {
            label: "操作",
            render: (row) => actionButtons([
                `<button class="mini-button" data-edit-alert-rule="${escapeHtml(row.id)}">编辑</button>`,
                `<button class="mini-button danger" data-delete-alert-rule="${escapeHtml(row.id)}">删除</button>`
            ])
        }
    ], rows);
    dom.alertRulesTable.querySelectorAll("[data-edit-alert-rule]").forEach((button) => {
        button.addEventListener("click", () => {
            const rule = state.logOps.alertRules.find((item) => item.id === button.dataset.editAlertRule);
            openAlertRuleModal(rule);
        });
    });
    dom.alertRulesTable.querySelectorAll("[data-delete-alert-rule]").forEach((button) => {
        button.addEventListener("click", () => deleteAlertRule(button.dataset.deleteAlertRule));
    });
}

async function loadAlertTriggers() {
    if (!dom.alertTriggersTable) {
        return;
    }
    dom.alertTriggersTable.className = "table-wrap empty-state";
    dom.alertTriggersTable.textContent = "正在加载触发记录...";
    const params = new URLSearchParams({ page: "1", pageSize: "50" });
    const ruleId = document.getElementById("triggerRuleFilter")?.value.trim();
    const metricType = document.getElementById("triggerMetricFilter")?.value;
    if (ruleId) params.set("ruleId", ruleId);
    if (metricType) params.set("metricType", metricType);
    try {
        const response = await apiFetch(getAdminLogPath(`/alerts/triggers?${params.toString()}`));
        const rows = response.data?.list || response.data?.data?.list || [];
        state.logOps.alertTriggers = rows;
        renderAlertTriggers(rows);
    } catch (error) {
        dom.alertTriggersTable.textContent = resolveFeedbackMessage("load alert triggers failed", error);
    }
}

function renderAlertTriggers(rows) {
    renderTable(dom.alertTriggersTable, [
        { label: "时间", render: (row) => formatDateTime(row.triggered_at || row.triggeredAt) },
        { label: "规则", render: (row) => `<strong>${escapeHtml(row.rule_name || row.ruleName || "-")}</strong><div class="tree-meta">${escapeHtml(row.rule_id || row.ruleId || "-")}</div>` },
        { label: "指标", render: (row) => escapeHtml(row.metric_type || row.metricType || "-") },
        { label: "当前值", render: (row) => escapeHtml(row.observed_value ?? row.observedValue ?? "-") },
        { label: "条件", render: (row) => escapeHtml(`${row.comparator || ""} ${row.threshold ?? "-"}`) },
        { label: "通知", render: (row) => `${statusPill(row.notification_status || row.notificationStatus || "-")}<div class="tree-meta">${escapeHtml(row.notification_message || row.notificationMessage || "")}</div>` },
        { label: "目标", render: (row) => escapeHtml(row.notification_target || row.notificationTarget || "-") }
    ], rows);
}

function openAlertRuleModal(rule = null) {
    const isEdit = Boolean(rule?.id);
    const get = (...keys) => keys.map((key) => rule?.[key]).find((value) => value != null) ?? "";
    openModal(isEdit ? "编辑告警规则" : "新增告警规则", `
        <form id="alertRuleForm" class="stack-form">
            <div class="two-field">
                <label><span>规则名称</span><input id="alertRuleNameInput" value="${escapeHtml(get("rule_name", "ruleName"))}" required></label>
                <label><span>指标</span><select id="alertRuleMetricInput">
                    ${["request_rate", "request_timeout_rate", "request_error_rate", "request_latency", "downstream_error_rate", "downstream_latency", "ip_request_rate", "user_request_rate"].map((metric) => `<option value="${metric}" ${get("metric_type", "metricType") === metric ? "selected" : ""}>${metric}</option>`).join("")}
                </select></label>
            </div>
            <div class="two-field">
                <label><span>服务</span><input id="alertRuleServiceInput" value="${escapeHtml(get("service"))}"></label>
                <label><span>接口</span><input id="alertRuleApiInput" value="${escapeHtml(get("api_endpoint", "apiEndpoint"))}"></label>
            </div>
            <div class="two-field">
                <label><span>下游服务</span><input id="alertRuleDownstreamInput" value="${escapeHtml(get("downstream_service", "downstreamService"))}"></label>
                <label><span>错误类型</span><input id="alertRuleErrorTypeInput" value="${escapeHtml(get("error_type", "errorType"))}" placeholder="5xx / timeout"></label>
            </div>
            <div class="two-field">
                <label><span>分位数</span><select id="alertRulePercentileInput">
                    <option value="">默认</option>
                    ${["p50", "p95", "p99"].map((item) => `<option value="${item}" ${get("percentile") === item ? "selected" : ""}>${item}</option>`).join("")}
                </select></label>
                <label><span>超时阈值 ms</span><input id="alertRuleTimeoutInput" type="number" min="1" value="${escapeHtml(get("timeout_threshold_ms", "timeoutThresholdMs"))}"></label>
            </div>
            <div class="two-field">
                <label><span>Client IP</span><input id="alertRuleClientIpInput" value="${escapeHtml(get("client_ip", "clientIp"))}"></label>
                <label><span>User ID</span><input id="alertRuleUserIdInput" value="${escapeHtml(get("user_id", "userId"))}"></label>
            </div>
            <div class="two-field">
                <label><span>窗口</span><input id="alertRuleWindowInput" value="${escapeHtml(get("window_size", "windowSize") || "1m")}" required></label>
                <label><span>比较</span><select id="alertRuleComparatorInput">
                    ${[">", ">=", "<", "<=", "=="].map((item) => `<option value="${escapeHtml(item)}" ${get("comparator") === item ? "selected" : ""}>${escapeHtml(item)}</option>`).join("")}
                </select></label>
            </div>
            <div class="two-field">
                <label><span>阈值</span><input id="alertRuleThresholdInput" type="number" step="0.01" value="${escapeHtml(get("threshold"))}" required></label>
                <label><span>连续次数</span><input id="alertRuleConsecutiveInput" type="number" min="1" value="${escapeHtml(get("consecutive_times", "consecutiveTimes") || 1)}"></label>
            </div>
            <div class="two-field">
                <label><span>通知渠道</span><select id="alertRuleChannelInput"><option value="in_app">in_app</option></select></label>
                <label><span>通知用户 ID</span><input id="alertRuleTargetInput" value="${escapeHtml(get("notification_target", "notificationTarget"))}" placeholder="user id, comma separated"></label>
            </div>
            <label class="permission-pick-item"><input id="alertRuleEnabledInput" type="checkbox" ${get("enabled") === false ? "" : "checked"}>启用规则</label>
            <button type="submit" class="primary-button">保存规则</button>
        </form>
    `);
    document.getElementById("alertRuleChannelInput").value = get("notification_channel", "notificationChannel") || "in_app";
    document.getElementById("alertRuleForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = compactPayload({
            rule_name: document.getElementById("alertRuleNameInput").value.trim(),
            metric_type: document.getElementById("alertRuleMetricInput").value,
            service: document.getElementById("alertRuleServiceInput").value.trim(),
            api_endpoint: document.getElementById("alertRuleApiInput").value.trim(),
            downstream_service: document.getElementById("alertRuleDownstreamInput").value.trim(),
            error_type: document.getElementById("alertRuleErrorTypeInput").value.trim(),
            percentile: document.getElementById("alertRulePercentileInput").value,
            client_ip: document.getElementById("alertRuleClientIpInput").value.trim(),
            user_id: document.getElementById("alertRuleUserIdInput").value.trim(),
            timeout_threshold_ms: optionalNumber(document.getElementById("alertRuleTimeoutInput").value),
            window_size: document.getElementById("alertRuleWindowInput").value.trim(),
            comparator: document.getElementById("alertRuleComparatorInput").value,
            threshold: Number(document.getElementById("alertRuleThresholdInput").value),
            consecutive_times: Number(document.getElementById("alertRuleConsecutiveInput").value || 1),
            notification_channel: document.getElementById("alertRuleChannelInput").value,
            notification_target: document.getElementById("alertRuleTargetInput").value.trim(),
            enabled: document.getElementById("alertRuleEnabledInput").checked
        });
        try {
            await apiFetch(getAdminLogPath(isEdit ? `/alerts/rules/${rule.id}` : "/alerts/rules"), {
                method: isEdit ? "PUT" : "POST",
                body: JSON.stringify(payload)
            });
            closeModal();
            showToast("alert rule saved", "success");
            loadAlertRules();
        } catch (error) {
            showToast(resolveFeedbackMessage("save alert rule failed", error), "error");
        }
    });
}

async function deleteAlertRule(ruleId) {
    if (!ruleId || !window.confirm("Delete this alert rule?")) {
        return;
    }
    try {
        await apiFetch(getAdminLogPath(`/alerts/rules/${ruleId}`), { method: "DELETE" });
        showToast("alert rule deleted", "success");
        loadAlertRules();
    } catch (error) {
        showToast(resolveFeedbackMessage("delete alert rule failed", error), "error");
    }
}

function switchNotificationTab(tab) {
    state.notificationAdmin.tab = tab;
    document.querySelectorAll("[data-notification-tab]").forEach((button) => {
        button.classList.toggle("active", button.dataset.notificationTab === tab);
    });
    document.querySelectorAll(".notification-pane").forEach((pane) => pane.classList.remove("active"));
    const pane = document.getElementById(`notification${tab[0].toUpperCase()}${tab.slice(1)}Pane`);
    if (pane) {
        pane.classList.add("active");
    }
    renderNotificationAdmin();
}

function renderNotificationAdmin() {
    if (!hasMenuPermission("menu:notificationManagement")) {
        showToast("missing permission: menu:notificationManagement", "error");
        return;
    }
    switch (state.notificationAdmin.tab) {
        case "templates":
            loadNotificationTemplates();
            break;
        case "messages":
            loadReusableMessages();
            break;
        case "policies":
            loadNotificationPolicies();
            break;
        default:
            loadNotificationChannels();
            break;
    }
}

async function loadNotificationChannels() {
    if (!dom.channelsTable) {
        return;
    }
    dom.channelsTable.className = "table-wrap empty-state";
    dom.channelsTable.textContent = "正在加载通道...";
    try {
        const response = await apiFetch(getAdminNotificationPath("/channels/status"));
        const channels = response.data?.carriers || Object.values(response.data?.channels || {});
        state.notificationAdmin.channels = channels;
        renderNotificationChannels(channels);
    } catch (error) {
        dom.channelsTable.textContent = resolveFeedbackMessage("加载通道失败", error);
    }
}

function renderNotificationChannels(channels) {
    renderTable(dom.channelsTable, [
        { label: "通道", render: (row) => escapeHtml(getNotificationChannelLabel(row.channelType)) },
        { label: "Provider", render: (row) => escapeHtml(row.provider || "-") },
        { label: "状态", render: (row) => `${statusPill(row.status)} ${row.defaultCarrier ? statusPill("默认", "is-default") : ""}` },
        { label: "重试", render: (row) => escapeHtml(row.maxRetry ?? "-") },
        { label: "更新时间", render: (row) => formatDateTime(row.updatedAt) },
        {
            label: "操作",
            render: (row) => actionButtons([
                `<button class="mini-button" data-edit-channel="${escapeHtml(row.channelId || "")}">配置</button>`,
                `<button class="mini-button" data-switch-channel="${escapeHtml(row.channelType || "")}" data-switch-provider="${escapeHtml(row.provider || "")}">设为默认</button>`
            ])
        }
    ], channels);
    dom.channelsTable.querySelectorAll("[data-edit-channel]").forEach((button) => {
        button.addEventListener("click", () => {
            const channel = state.notificationAdmin.channels.find((item) => item.channelId === button.dataset.editChannel);
            openChannelConfigModal(channel);
        });
    });
    dom.channelsTable.querySelectorAll("[data-switch-channel]").forEach((button) => {
        button.addEventListener("click", async () => {
            const channel = button.dataset.switchChannel;
            const provider = button.dataset.switchProvider;
            try {
                await apiFetch(getAdminNotificationPath(`/channels/${channel}/switch`), {
                    method: "PUT",
                    body: JSON.stringify({ provider })
                });
                showToast("默认通道已切换", "success");
                loadNotificationChannels();
            } catch (error) {
                showToast(resolveFeedbackMessage("切换默认通道失败", error), "error");
            }
        });
    });
}

function buildDefaultChannelConfig(channel) {
    if (channel === "email") {
        return {
            host: "smtp.163.com",
            port: 465,
            username: "",
            password: "",
            from: "",
            protocol: "smtps",
            defaultEncoding: "UTF-8",
            properties: {
                mail: {
                    smtp: {
                        auth: true,
                        ssl: { enable: true },
                        socketFactory: { class: "javax.net.ssl.SSLSocketFactory" },
                        connectiontimeout: 5000,
                        timeout: 5000,
                        writetimeout: 5000
                    }
                }
            }
        };
    }
    if (channel === "sms") {
        return {
            mode: "template",
            baseUrl: "https://push.spug.cc/send",
            templateId: "",
            targetParam: "targets",
            timeoutMs: 5000
        };
    }
    if (channel === "feishu") {
        return {
            appId: "",
            appSecret: "",
            receiveIdType: "open_id",
            msgType: "text",
            timeoutMs: 5000
        };
    }
    return {};
}

function getDefaultChannelProvider(channelType) {
    if (channelType === "email") {
        return "spring_mail";
    }
    if (channelType === "in_app") {
        return "system";
    }
    if (channelType === "sms") {
        return "spug_sms";
    }
    if (channelType === "feishu") {
        return "feishu_app";
    }
    return channelType;
}

function openChannelConfigModal(channel = {}) {
    const channelType = channel.channelType || "email";
    const defaultConfig = channel.config || buildDefaultChannelConfig(channelType);
    const defaultProvider = channel.provider || getDefaultChannelProvider(channelType);
    openModal("配置通知通道", `
        <form id="channelConfigForm" class="stack-form">
            <div class="two-field">
                <label><span>通道类型</span><input id="channelTypeInput" value="${escapeHtml(channelType)}" required></label>
                <label><span>Provider</span><input id="channelProviderInput" value="${escapeHtml(defaultProvider)}" required></label>
            </div>
            <label><span>通道名称</span><input id="channelNameInput" value="${escapeHtml(channel.channelName || "")}"></label>
            <div class="two-field">
                <label><span>状态</span><select id="channelStatusInput"><option value="active">active</option><option value="disabled">disabled</option></select></label>
                <label><span>最大重试</span><input id="channelMaxRetryInput" type="number" min="0" value="${escapeHtml(channel.maxRetry ?? 3)}"></label>
            </div>
            <label class="permission-pick-item"><input id="channelDefaultInput" type="checkbox" ${channel.defaultCarrier ? "checked" : ""}>设为默认 carrier</label>
            <label><span>配置 JSON</span><textarea id="channelConfigJsonInput" class="code-textarea">${escapeHtml(JSON.stringify(defaultConfig, null, 2))}</textarea></label>
            <label><span>备注</span><input id="channelRemarkInput" value=""></label>
            <button type="submit" class="primary-button">保存通道配置</button>
        </form>
    `);
    document.getElementById("channelStatusInput").value = channel.status || "active";
    document.getElementById("channelConfigForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            const targetChannel = document.getElementById("channelTypeInput").value.trim();
            const payload = {
                channelName: document.getElementById("channelNameInput").value.trim() || undefined,
                provider: document.getElementById("channelProviderInput").value.trim(),
                status: document.getElementById("channelStatusInput").value,
                maxRetry: Number(document.getElementById("channelMaxRetryInput").value || 3),
                defaultCarrier: document.getElementById("channelDefaultInput").checked,
                remark: document.getElementById("channelRemarkInput").value.trim() || undefined,
                config: JSON.parse(document.getElementById("channelConfigJsonInput").value || "{}")
            };
            await apiFetch(getAdminNotificationPath(`/channels/${targetChannel}`), {
                method: "POST",
                body: JSON.stringify(payload)
            });
            closeModal();
            showToast("通道配置已保存", "success");
            loadNotificationChannels();
        } catch (error) {
            showToast(resolveFeedbackMessage("保存通道失败", error), "error");
        }
    });
}

async function loadNotificationTemplates() {
    if (!dom.templatesTable) {
        return;
    }
    dom.templatesTable.className = "table-wrap empty-state";
    dom.templatesTable.textContent = "正在加载模板...";
    const params = new URLSearchParams({ page: "1", size: "20" });
    const businessCode = document.getElementById("templateBusinessFilter")?.value.trim();
    const channel = document.getElementById("templateChannelFilter")?.value;
    const status = document.getElementById("templateStatusFilter")?.value;
    if (businessCode) params.set("business_code", businessCode);
    if (channel) params.set("channel", channel);
    if (status) params.set("status", status);
    try {
        const response = await apiFetch(getAdminNotificationPath(`/templates?${params.toString()}`));
        const records = response.data?.records || [];
        state.notificationAdmin.templates = records;
        renderNotificationTemplates(records);
    } catch (error) {
        dom.templatesTable.textContent = resolveFeedbackMessage("加载模板失败", error);
    }
}

function renderNotificationTemplates(records) {
    renderTable(dom.templatesTable, [
        { label: "模板", render: (row) => `<strong>${escapeHtml(row.name || "-")}</strong><div class="tree-meta">${escapeHtml(row.templateCode || "-")}</div>` },
        { label: "业务", render: (row) => escapeHtml(row.businessCode || "-") },
        { label: "通道", render: (row) => escapeHtml(getNotificationChannelLabel(row.channel)) },
        { label: "状态", render: (row) => statusPill(row.status) },
        { label: "版本", render: (row) => escapeHtml(row.version ?? "-") },
        {
            label: "操作",
            render: (row) => actionButtons([
                `<button class="mini-button" data-edit-template="${escapeHtml(row.templateId)}">编辑</button>`,
                `<button class="mini-button" data-preview-template="${escapeHtml(row.templateId)}">预览</button>`,
                `<button class="mini-button" data-version-template="${escapeHtml(row.templateId)}">版本</button>`,
                `<button class="mini-button" data-publish-template="${escapeHtml(row.templateId)}">发布</button>`,
                `<button class="mini-button danger" data-delete-template="${escapeHtml(row.templateId)}">归档</button>`
            ])
        }
    ], records);
    dom.templatesTable.querySelectorAll("[data-edit-template]").forEach((button) => {
        button.addEventListener("click", () => openTemplateForm(button.dataset.editTemplate));
    });
    dom.templatesTable.querySelectorAll("[data-preview-template]").forEach((button) => {
        button.addEventListener("click", () => openTemplatePreviewModal(button.dataset.previewTemplate));
    });
    dom.templatesTable.querySelectorAll("[data-version-template]").forEach((button) => {
        button.addEventListener("click", () => openTemplateVersionsModal(button.dataset.versionTemplate));
    });
    dom.templatesTable.querySelectorAll("[data-publish-template]").forEach((button) => {
        button.addEventListener("click", async () => {
            try {
                await apiFetch(getAdminNotificationPath(`/templates/${button.dataset.publishTemplate}/publish`), {
                    method: "POST",
                    body: JSON.stringify({})
                });
                showToast("模板已发布", "success");
                loadNotificationTemplates();
            } catch (error) {
                showToast(resolveFeedbackMessage("发布模板失败", error), "error");
            }
        });
    });
    dom.templatesTable.querySelectorAll("[data-delete-template]").forEach((button) => {
        button.addEventListener("click", async () => {
            if (!confirm("确认归档这个模板？")) return;
            try {
                await apiFetch(getAdminNotificationPath(`/templates/${button.dataset.deleteTemplate}`), { method: "DELETE" });
                showToast("模板已归档", "success");
                loadNotificationTemplates();
            } catch (error) {
                showToast(resolveFeedbackMessage("归档模板失败", error), "error");
            }
        });
    });
}

async function openTemplateForm(templateId = null) {
    let record = null;
    if (templateId) {
        const response = await apiFetch(getAdminNotificationPath(`/templates/${templateId}`));
        record = response.data || {};
    }
    openModal(templateId ? "编辑通知模板" : "新建通知模板", `
        <form id="templateForm" class="stack-form">
            <div class="two-field">
                <label><span>业务编码</span><input id="tplBusinessInput" value="${escapeHtml(record?.businessCode || "")}" required></label>
                <label><span>模板编码</span><input id="tplCodeInput" value="${escapeHtml(record?.templateCode || "")}"></label>
            </div>
            <label><span>模板名称</span><input id="tplNameInput" value="${escapeHtml(record?.name || "")}" required></label>
            <div class="two-field">
                <label><span>通道</span><select id="tplChannelInput"><option value="in_app">站内</option><option value="email">邮件</option><option value="push">推送</option><option value="feishu">飞书</option></select></label>
                <label><span>状态</span><select id="tplStatusInput"><option value="draft">draft</option><option value="active">active</option></select></label>
            </div>
            <label><span>内容 JSON</span><textarea id="tplContentInput" class="code-textarea" required>${escapeHtml(JSON.stringify(record?.content || { title: "{{ title }}", body: "{{ body }}" }, null, 2))}</textarea></label>
            <label><span>变量定义 JSON</span><textarea id="tplSchemaInput" class="code-textarea">${escapeHtml(JSON.stringify(record?.paramsSchema || { title: "string", body: "string" }, null, 2))}</textarea></label>
            <button type="submit" class="primary-button">保存模板</button>
        </form>
    `);
    document.getElementById("tplChannelInput").value = record?.channel || "in_app";
    document.getElementById("tplStatusInput").value = record?.status || "draft";
    document.getElementById("templateForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            const payload = {
                businessCode: document.getElementById("tplBusinessInput").value.trim(),
                templateCode: document.getElementById("tplCodeInput").value.trim() || undefined,
                name: document.getElementById("tplNameInput").value.trim(),
                channel: document.getElementById("tplChannelInput").value,
                status: document.getElementById("tplStatusInput").value,
                content: JSON.parse(document.getElementById("tplContentInput").value),
                paramsSchema: JSON.parse(document.getElementById("tplSchemaInput").value || "null"),
                createdBy: "admin"
            };
            await apiFetch(getAdminNotificationPath(templateId ? `/templates/${templateId}` : "/templates"), {
                method: templateId ? "PUT" : "POST",
                body: JSON.stringify(payload)
            });
            closeModal();
            showToast("模板已保存", "success");
            loadNotificationTemplates();
        } catch (error) {
            showToast(resolveFeedbackMessage("保存模板失败", error), "error");
        }
    });
}

function openTemplatePreviewModal(templateId) {
    openModal("模板预览", `
        <form id="templatePreviewForm" class="stack-form">
            <label><span>参数 JSON</span><textarea id="previewParamsInput" class="code-textarea">{ "title": "Preview", "body": "Hello" }</textarea></label>
            <button type="submit" class="primary-button">生成预览</button>
            <div id="templatePreviewResult" class="message-detail-grid"></div>
        </form>
    `);
    document.getElementById("templatePreviewForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            const response = await apiFetch(getAdminNotificationPath("/templates/preview"), {
                method: "POST",
                body: JSON.stringify({
                    templateId,
                    params: JSON.parse(document.getElementById("previewParamsInput").value || "{}")
                })
            });
            const preview = response.data || {};
            document.getElementById("templatePreviewResult").innerHTML = `
                <div><strong>标题</strong><span>${escapeHtml(preview.title || "-")}</span></div>
                <div><strong>内容</strong><span>${escapeHtml(preview.body || preview.rendered || "-")}</span></div>
            `;
        } catch (error) {
            showToast(resolveFeedbackMessage("预览模板失败", error), "error");
        }
    });
}

async function openTemplateVersionsModal(templateId) {
    try {
        const response = await apiFetch(getAdminNotificationPath(`/templates/${templateId}/versions`));
        const versions = Array.isArray(response.data) ? response.data : [];
        openModal("模板版本", `
            <div id="templateVersionsTable" class="table-wrap empty-state">暂无版本</div>
            <div id="templateVersionDetail" class="message-detail-grid"></div>
        `);
        const table = document.getElementById("templateVersionsTable");
        renderTable(table, [
            { label: "版本", render: (row) => escapeHtml(row.version ?? "-") },
            { label: "模板", render: (row) => `<strong>${escapeHtml(row.name || "-")}</strong><div class="tree-meta">${escapeHtml(row.templateCode || "-")}</div>` },
            { label: "状态", render: (row) => statusPill(row.status) },
            { label: "创建时间", render: (row) => formatDateTime(row.createdAt) },
            {
                label: "操作",
                render: (row) => actionButtons([
                    `<button class="mini-button" data-open-template-version="${escapeHtml(row.version)}">查看</button>`
                ])
            }
        ], versions);
        table.querySelectorAll("[data-open-template-version]").forEach((button) => {
            button.addEventListener("click", async () => {
                try {
                    const versionResponse = await apiFetch(getAdminNotificationPath(`/templates/${templateId}/versions/${button.dataset.openTemplateVersion}`));
                    const version = versionResponse.data || {};
                    document.getElementById("templateVersionDetail").innerHTML = `
                        <div><strong>版本</strong><span>${escapeHtml(version.version ?? "-")}</span></div>
                        <div><strong>通道</strong><span>${escapeHtml(getNotificationChannelLabel(version.channel))}</span></div>
                        <div class="full-width"><strong>内容</strong><span><pre class="inline-json">${escapeHtml(stringifyPretty(version.content, {}))}</pre></span></div>
                        <div class="full-width"><strong>变量定义</strong><span><pre class="inline-json">${escapeHtml(stringifyPretty(version.paramsSchema, {}))}</pre></span></div>
                    `;
                } catch (error) {
                    showToast(resolveFeedbackMessage("加载模板版本失败", error), "error");
                }
            });
        });
    } catch (error) {
        showToast(resolveFeedbackMessage("加载模板版本失败", error), "error");
    }
}

async function loadReusableMessages() {
    if (!dom.reusableMessagesTable) {
        return;
    }
    dom.reusableMessagesTable.className = "table-wrap empty-state";
    dom.reusableMessagesTable.textContent = "正在加载发送记录...";
    const params = new URLSearchParams({ page: "1", size: "20" });
    const businessCode = document.getElementById("messageBusinessFilter")?.value.trim();
    const status = document.getElementById("messageStatusFilter")?.value;
    const recipient = document.getElementById("messageRecipientFilter")?.value.trim();
    if (businessCode) params.set("business_code", businessCode);
    if (status) params.set("status", status);
    if (recipient) params.set("recipient", recipient);
    try {
        const response = await apiFetch(getAdminNotificationPath(`/messages?${params.toString()}`));
        const records = response.data?.records || [];
        state.notificationAdmin.messages = records;
        renderReusableMessages(records);
    } catch (error) {
        dom.reusableMessagesTable.textContent = resolveFeedbackMessage("加载发送记录失败", error);
    }
}

function renderReusableMessages(records) {
    renderTable(dom.reusableMessagesTable, [
        { label: "消息", render: (row) => `<strong>${escapeHtml(row.title || "-")}</strong><div class="tree-meta">${escapeHtml(row.messageId || "-")}</div>` },
        { label: "通道", render: (row) => escapeHtml(getNotificationChannelLabel(row.channel)) },
        { label: "接收人", render: (row) => escapeHtml(row.recipient || "-") },
        { label: "状态", render: (row) => statusPill(row.status) },
        { label: "批次", render: (row) => row.batchId ? `<button class="link-button" data-open-reusable-batch="${escapeHtml(row.batchId)}">${escapeHtml(row.batchId)}</button>` : "-" },
        { label: "发送时间", render: (row) => formatDateTime(row.sentAt || row.plannedAt) },
        {
            label: "操作",
            render: (row) => actionButtons([
                `<button class="mini-button" data-open-reusable-message="${escapeHtml(row.messageId)}">详情</button>`,
                row.status === "pending" || row.status === "retrying" ? `<button class="mini-button danger" data-cancel-reusable-message="${escapeHtml(row.messageId)}">取消</button>` : ""
            ])
        }
    ], records);
    dom.reusableMessagesTable.querySelectorAll("[data-open-reusable-message]").forEach((button) => {
        button.addEventListener("click", () => openReusableMessageDetail(button.dataset.openReusableMessage));
    });
    dom.reusableMessagesTable.querySelectorAll("[data-open-reusable-batch]").forEach((button) => {
        button.addEventListener("click", () => openReusableBatchDetail(button.dataset.openReusableBatch));
    });
    dom.reusableMessagesTable.querySelectorAll("[data-cancel-reusable-message]").forEach((button) => {
        button.addEventListener("click", async () => {
            try {
                await apiFetch(getAdminNotificationPath(`/messages/${button.dataset.cancelReusableMessage}`), { method: "DELETE" });
                showToast("消息已取消", "success");
                loadReusableMessages();
            } catch (error) {
                showToast(resolveFeedbackMessage("取消消息失败", error), "error");
            }
        });
    });
}

async function openReusableBatchDetail(batchId) {
    try {
        const response = await apiFetch(getAdminNotificationPath(`/messages/batch/${batchId}`));
        const batch = response.data || {};
        const records = batch.records || [];
        openModal("批次详情", `
            <div class="message-detail-grid">
                <div><strong>批次号</strong><span>${escapeHtml(batch.batchId || batchId)}</span></div>
                <div><strong>状态</strong><span>${statusPill(batch.status)}</span></div>
                <div><strong>总数</strong><span>${escapeHtml(batch.total ?? records.length)}</span></div>
                <div><strong>成功/失败</strong><span>${escapeHtml(batch.success ?? 0)} / ${escapeHtml(batch.failed ?? 0)}</span></div>
            </div>
            <div id="batchMessagesTable" class="table-wrap empty-state">暂无批次消息</div>
        `);
        renderTable(document.getElementById("batchMessagesTable"), [
            { label: "消息", render: (row) => `<strong>${escapeHtml(row.title || "-")}</strong><div class="tree-meta">${escapeHtml(row.messageId || "-")}</div>` },
            { label: "接收人", render: (row) => escapeHtml(row.recipient || "-") },
            { label: "状态", render: (row) => statusPill(row.status) },
            { label: "失败原因", render: (row) => escapeHtml(row.failureReason || "-") }
        ], records);
    } catch (error) {
        showToast(resolveFeedbackMessage("加载批次详情失败", error), "error");
    }
}

async function openReusableMessageDetail(messageId) {
    try {
        const response = await apiFetch(getAdminNotificationPath(`/messages/${messageId}`));
        const row = response.data || {};
        openModal("通知详情", `
            <div class="message-detail-grid">
                <div><strong>状态</strong><span>${statusPill(row.status)}</span></div>
                <div><strong>通道</strong><span>${escapeHtml(getNotificationChannelLabel(row.channel))} / ${escapeHtml(row.provider || "-")}</span></div>
                <div><strong>接收人</strong><span>${escapeHtml(row.recipient || "-")}</span></div>
                <div><strong>业务编码</strong><span>${escapeHtml(row.businessCode || "-")}</span></div>
                <div class="full-width"><strong>标题</strong><span>${escapeHtml(row.title || "-")}</span></div>
                <div class="full-width"><strong>内容</strong><span>${escapeHtml(row.content || "-")}</span></div>
                <div class="full-width"><strong>失败原因</strong><span>${escapeHtml(row.failureReason || "-")}</span></div>
            </div>
        `);
    } catch (error) {
        showToast(resolveFeedbackMessage("加载通知详情失败", error), "error");
    }
}

function openReusableSendModal() {
    openModal("发送通知", `
        <form id="reusableSendForm" class="stack-form">
            <div class="two-field">
                <label><span>业务编码</span><input id="sendBusinessInput" value="manual_notice" required></label>
                <label><span>模板编码</span><input id="sendTemplateCodeInput" placeholder="例如 SMTP_DB_TEST_EMAIL" required></label>
            </div>
            <div class="two-field">
                <label><span>通道</span><select id="sendChannelInput"><option value="in_app">站内</option><option value="email">邮件</option><option value="push">推送</option><option value="feishu">飞书</option></select></label>
                <label><span>发送方式</span><select id="sendModeInput"><option value="single">单条</option><option value="batch">批量</option></select></label>
            </div>
            <div id="singleRecipientFields">
                <div class="two-field">
                    <label><span>接收类型</span><select id="sendRecipientTypeInput"><option value="user_id">用户ID</option><option value="email">邮箱</option><option value="phone">手机号</option><option value="feishu_open_id">飞书 open_id</option><option value="feishu_user_id">飞书 user_id</option><option value="feishu_union_id">飞书 union_id</option></select></label>
                    <label><span>接收值</span><input id="sendRecipientValueInput"></label>
                </div>
            </div>
            <div id="batchRecipientFields" class="hidden">
                <label><span>批量接收人 JSON</span><textarea id="sendRecipientsInput" class="code-textarea">[
  { "type": "user_id", "value": "系统用户ID" }
]</textarea></label>
            </div>
            <label><span>参数 JSON</span><textarea id="sendParamsInput" class="code-textarea">{ "title": "通知", "body": "这是一条通知" }</textarea></label>
            <div class="two-field">
                <label><span>发送策略</span><select id="sendPolicyModeInput"><option value="immediate">即时发送</option><option value="scheduled_once">定时发送</option></select></label>
                <label><span>计划发送时间</span><input id="sendScheduleInput" placeholder="2026-06-28T10:00:00"></label>
            </div>
            <div class="two-field">
                <label><span>优先级</span><input id="sendPriorityInput" type="number" min="1" max="9" value="5"></label>
                <label><span>最大重试</span><input id="sendRetryInput" type="number" min="0" max="10" value="3"></label>
            </div>
            <div class="two-field">
                <label><span>请求号</span><input id="sendRequestIdInput" placeholder="留空自动生成"></label>
            </div>
            <button type="submit" class="primary-button">发送</button>
        </form>
    `);
    const sendBusinessInput = document.getElementById("sendBusinessInput");
    const sendTemplateCodeInput = document.getElementById("sendTemplateCodeInput");
    const sendChannelInput = document.getElementById("sendChannelInput");
    const sendPolicyModeInput = document.getElementById("sendPolicyModeInput");
    const sendScheduleInput = document.getElementById("sendScheduleInput");
    const syncReusableSendPolicy = () => {
        const scheduled = sendPolicyModeInput.value === "scheduled_once";
        sendScheduleInput.disabled = !scheduled;
        sendScheduleInput.required = scheduled;
        if (!scheduled) {
            sendScheduleInput.value = "";
        }
    };
    sendPolicyModeInput.addEventListener("change", syncReusableSendPolicy);
    syncReusableSendPolicy();
    const sendModeInput = document.getElementById("sendModeInput");
    const syncSendMode = () => {
        const batchMode = sendModeInput.value === "batch";
        document.getElementById("singleRecipientFields").classList.toggle("hidden", batchMode);
        document.getElementById("batchRecipientFields").classList.toggle("hidden", !batchMode);
    };
    sendModeInput.addEventListener("change", syncSendMode);
    syncSendMode();
    document.getElementById("reusableSendForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            const batchMode = document.getElementById("sendModeInput").value === "batch";
            const payload = {
                requestId: document.getElementById("sendRequestIdInput").value.trim() || undefined,
                businessCode: sendBusinessInput.value.trim(),
                templateCode: sendTemplateCodeInput.value.trim() || undefined,
                channel: sendChannelInput.value,
                scheduleAt: sendPolicyModeInput.value === "scheduled_once" ? sendScheduleInput.value.trim() : undefined,
                strategy: {
                    priority: Number(document.getElementById("sendPriorityInput").value || 5),
                    retry: Number(document.getElementById("sendRetryInput").value || 3)
                },
                params: parseJsonInput("sendParamsInput", {})
            };
            if (batchMode) {
                payload.recipients = parseJsonInput("sendRecipientsInput", []);
            } else {
                payload.recipient = {
                    type: document.getElementById("sendRecipientTypeInput").value,
                    value: document.getElementById("sendRecipientValueInput").value.trim()
                };
            }
            const response = await apiFetch(getAdminNotificationPath(batchMode ? "/messages/batch" : "/messages/send"), {
                method: "POST",
                body: JSON.stringify(payload)
            });
            closeModal();
            const batchId = response.data?.batchId;
            showToast(batchId ? `通知已发送，批次 ${batchId}` : "通知已发送", "success");
            switchNotificationTab("messages");
        } catch (error) {
            showToast(resolveFeedbackMessage("发送通知失败", error), "error");
        }
    });
}

async function loadNotificationPolicies() {
    if (!dom.policiesTable) {
        return;
    }
    dom.policiesTable.className = "table-wrap empty-state";
    dom.policiesTable.textContent = "正在加载策略...";
    const params = new URLSearchParams({ page: "1", size: "20" });
    const businessCode = document.getElementById("policyBusinessFilter")?.value.trim();
    const channel = document.getElementById("policyChannelFilter")?.value;
    const status = document.getElementById("policyStatusFilter")?.value;
    if (businessCode) params.set("business_code", businessCode);
    if (channel) params.set("channel_type", channel);
    if (status) params.set("status", status);
    try {
        const response = await apiFetch(getAdminNotificationPath(`/policies?${params.toString()}`));
        const records = response.data?.records || [];
        state.notificationAdmin.policies = records;
        renderNotificationPolicies(records);
    } catch (error) {
        dom.policiesTable.textContent = resolveFeedbackMessage("加载策略失败", error);
    }
}

function renderNotificationPolicies(records) {
    renderTable(dom.policiesTable, [
        { label: "策略", render: (row) => `<strong>${escapeHtml(row.policyName || "-")}</strong><div class="tree-meta">${escapeHtml(row.policyId || "-")}</div>` },
        { label: "业务", render: (row) => escapeHtml(row.businessCode || "-") },
        { label: "通道", render: (row) => escapeHtml(getNotificationChannelLabel(row.channelType)) },
        { label: "模式", render: (row) => escapeHtml(row.sendMode || "-") },
        { label: "状态", render: (row) => statusPill(row.status) },
        {
            label: "操作",
            render: (row) => actionButtons([
                `<button class="mini-button" data-open-policy="${escapeHtml(row.policyId)}">详情</button>`,
                `<button class="mini-button" data-edit-policy="${escapeHtml(row.policyId)}">编辑</button>`,
                `<button class="mini-button danger" data-delete-policy="${escapeHtml(row.policyId)}">删除</button>`
            ])
        }
    ], records);
    dom.policiesTable.querySelectorAll("[data-open-policy]").forEach((button) => {
        button.addEventListener("click", () => openPolicyDetail(button.dataset.openPolicy));
    });
    dom.policiesTable.querySelectorAll("[data-edit-policy]").forEach((button) => {
        button.addEventListener("click", () => openPolicyForm(button.dataset.editPolicy));
    });
    dom.policiesTable.querySelectorAll("[data-delete-policy]").forEach((button) => {
        button.addEventListener("click", async () => {
            if (!confirm("确认删除这个策略？")) return;
            try {
                await apiFetch(getAdminNotificationPath(`/policies/${button.dataset.deletePolicy}`), { method: "DELETE" });
                showToast("策略已删除", "success");
                loadNotificationPolicies();
            } catch (error) {
                showToast(resolveFeedbackMessage("删除策略失败", error), "error");
            }
        });
    });
}

async function openPolicyDetail(policyId) {
    try {
        const response = await apiFetch(getAdminNotificationPath(`/policies/${policyId}`));
        const row = response.data || {};
        openModal("策略详情", `
            <div class="message-detail-grid">
                <div><strong>策略名称</strong><span>${escapeHtml(row.policyName || "-")}</span></div>
                <div><strong>状态</strong><span>${statusPill(row.status)}</span></div>
                <div><strong>业务编码</strong><span>${escapeHtml(row.businessCode || "-")}</span></div>
                <div><strong>通道</strong><span>${escapeHtml(getNotificationChannelLabel(row.channelType))}</span></div>
                <div><strong>发送模式</strong><span>${escapeHtml(row.sendMode || "-")}</span></div>
                <div><strong>下次触发</strong><span>${formatDateTime(row.nextFireTime)}</span></div>
                <div class="full-width"><strong>动态条件</strong><span>${escapeHtml(row.dynamicCondition || "-")}</span></div>
                <div class="full-width"><strong>备注</strong><span>${escapeHtml(row.remark || "-")}</span></div>
            </div>
        `);
    } catch (error) {
        showToast(resolveFeedbackMessage("加载策略详情失败", error), "error");
    }
}

async function openPolicyForm(policyId = null) {
    let record = null;
    if (policyId) {
        const response = await apiFetch(getAdminNotificationPath(`/policies/${policyId}`));
        record = response.data || {};
    }
    openModal(policyId ? "编辑通知策略" : "新建通知策略", `
        <form id="policyForm" class="stack-form">
            <div class="two-field">
                <label><span>策略名称</span><input id="policyNameInput" value="${escapeHtml(record?.policyName || "")}" required></label>
                <label><span>业务编码</span><input id="policyBusinessInput" value="${escapeHtml(record?.businessCode || "")}" required></label>
            </div>
            <div class="two-field">
                <label><span>模板编码</span><input id="policyTemplateCodeInput" value="${escapeHtml(record?.templateCode || "")}" required></label>
                <label><span>通道</span><select id="policyChannelInput"><option value="in_app">站内</option><option value="email">邮件</option><option value="push">推送</option><option value="feishu">飞书</option></select></label>
            </div>
            <div class="two-field">
                <label><span>发送模式</span><select id="policySendModeInput"><option value="immediate">立即</option><option value="scheduled_once">定时一次</option><option value="scheduled_recurring">周期</option></select></label>
                <label><span>发送时间</span><input id="policySendTimeInput" value="${escapeHtml(formatDateTimeInput(record?.sendTime))}" placeholder="2026-06-26T10:00:00"></label>
            </div>
            <div class="two-field">
                <label><span>周期规则</span><input id="policyRecurringRuleInput" value="${escapeHtml(record?.recurringRule || "")}" placeholder="0 0/5 * * * *"></label>
                <label><span>周期结束时间</span><input id="policyRecurringEndInput" value="${escapeHtml(formatDateTimeInput(record?.recurringEndTime))}" placeholder="2026-06-30T18:00:00"></label>
            </div>
            <div class="two-field">
                <label><span>优先级</span><input id="policyPriorityInput" type="number" min="0" value="${escapeHtml(record?.priority ?? 5)}"></label>
                <label><span>最大重试</span><input id="policyMaxRetryInput" type="number" min="0" value="${escapeHtml(record?.maxRetry ?? 3)}"></label>
            </div>
            <label><span>状态</span><select id="policyStatusInput"><option value="active">active</option><option value="disabled">disabled</option><option value="draft">draft</option></select></label>
            <label><span>动态条件 JSON</span><textarea id="policyConditionInput" class="code-textarea">${escapeHtml(record?.dynamicCondition || '{ "api": "" }')}</textarea></label>
            <label><span>备注</span><input id="policyRemarkInput" value="${escapeHtml(record?.remark || "")}"></label>
            <button type="submit" class="primary-button">保存策略</button>
        </form>
    `);
    document.getElementById("policyChannelInput").value = record?.channelType || "in_app";
    document.getElementById("policySendModeInput").value = record?.sendMode || "immediate";
    document.getElementById("policyStatusInput").value = record?.status || "active";
    document.getElementById("policyForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            const payload = {
                policyName: document.getElementById("policyNameInput").value.trim(),
                businessCode: document.getElementById("policyBusinessInput").value.trim(),
                templateCode: document.getElementById("policyTemplateCodeInput").value.trim(),
                channelType: document.getElementById("policyChannelInput").value,
                sendMode: document.getElementById("policySendModeInput").value,
                sendTime: document.getElementById("policySendTimeInput").value.trim() || undefined,
                recurringRule: document.getElementById("policyRecurringRuleInput").value.trim() || undefined,
                recurringEndTime: document.getElementById("policyRecurringEndInput").value.trim() || undefined,
                priority: Number(document.getElementById("policyPriorityInput").value || 5),
                maxRetry: Number(document.getElementById("policyMaxRetryInput").value || 3),
                dynamicCondition: document.getElementById("policyConditionInput").value.trim() || "{}",
                remark: document.getElementById("policyRemarkInput").value.trim() || undefined,
                status: document.getElementById("policyStatusInput").value,
                createdBy: "admin"
            };
            await apiFetch(getAdminNotificationPath(policyId ? `/policies/${policyId}` : "/policies"), {
                method: policyId ? "PUT" : "POST",
                body: JSON.stringify(payload)
            });
            closeModal();
            showToast("策略已保存", "success");
            loadNotificationPolicies();
        } catch (error) {
            showToast(resolveFeedbackMessage("保存策略失败", error), "error");
        }
    });
}

async function loadUsers() {
    if (!hasMenuPermission(actionPermissions.user.list)) {
        configureUserPageAccess();
        return;
    }
    try {
        const keyword = document.getElementById("userKeyword").value.trim();
        const params = new URLSearchParams({ page: "1", pageSize: "20" });
        if (keyword) {
            params.set("keyword", keyword);
        }
        const data = await apiFetch(`/api/v1/users?${params.toString()}`);
        renderTable(dom.usersTable, [
            { label: "用户", render: (row) => `${escapeHtml(row.nickname || "-")}<br><small>${escapeHtml(row.userId || "-")}</small>` },
            { label: "联系方式", render: (row) => `${escapeHtml(row.phone || "-")}<br>${escapeHtml(row.email || "-")}` },
            { label: "角色", render: (row) => escapeHtml(row.role || "-") },
            { label: "所属组织", render: (row) => joinTextList(row.groupNames) },
            { label: "状态", render: (row) => formatUserStatus(row.status) },
            { label: "最近登录", render: (row) => formatDateTime(row.lastLoginAt) },
            { label: "创建时间", render: (row) => formatDateTime(row.createdAt) },
            {
                label: "操作",
                render: (row) => actionButtons([
                    `<button class="mini-button" data-user-action="detail" data-user-id="${row.userId}">详情</button>`,
                    hasMenuPermission(actionPermissions.user.update) ? `<button class="mini-button" data-user-action="edit" data-user-id="${row.userId}">编辑</button>` : "",
                    hasMenuPermission(actionPermissions.user.attribute) ? `<button class="mini-button" data-user-action="attributes" data-user-id="${row.userId}" data-user-name="${escapeHtml(row.nickname || row.userId || "")}">字段</button>` : "",
                    hasMenuPermission(actionPermissions.permission.grant) ? `<button class="mini-button" data-user-action="grant" data-user-id="${row.userId}" data-user-name="${escapeHtml(row.nickname || row.userId || "")}">授权</button>` : "",
                    hasMenuPermission(actionPermissions.user.resetPassword) ? `<button class="mini-button" data-user-action="reset" data-user-id="${row.userId}" data-user-name="${escapeHtml(row.nickname || row.userId || "")}">重置密码</button>` : "",
                    hasMenuPermission(actionPermissions.user.delete) ? `<button class="mini-button danger" data-user-action="delete" data-user-id="${row.userId}">删除</button>` : ""
                ])
            }
        ], data.data?.list || []);

        dom.usersTable.querySelectorAll("[data-user-action]").forEach((button) => {
            const userId = button.dataset.userId;
            const action = button.dataset.userAction;
            const userName = button.dataset.userName || userId;
            button.addEventListener("click", () => {
                if (action === "detail") {
                    openUserDetail(userId);
                }
                if (action === "edit") {
                    openUserForm("edit", userId);
                }
                if (action === "attributes") {
                    openAttributeManager("user", userId, `${userName} 的自定义字段`, { onSaved: () => openUserDetail(userId) });
                }
                if (action === "grant") {
                    openPermissionGrantModal("user", userId, userName);
                }
                if (action === "reset") {
                    openResetUserPasswordModal(userId, userName);
                }
                if (action === "delete") {
                    deleteUser(userId);
                }
            });
        });
    } catch (error) {
        dom.usersTable.className = "table-wrap empty-state";
        dom.usersTable.textContent = "加载用户失败";
        showToast(resolveFeedbackMessage("加载用户失败", error), "error");
    }
}

async function openUserDetail(userId) {
    try {
        const data = await apiFetch(`/api/v1/users/${userId}/detail`);
        const user = data.data || {};
        const groupDetails = await fetchGroupDetails(user.groupIds || []);
        openModal("用户详情", `
            <div class="detail-grid">
                <div><strong>用户 ID</strong><span>${escapeHtml(user.userId || "-")}</span></div>
                <div><strong>昵称</strong><span>${escapeHtml(user.nickname || "-")}</span></div>
                <div><strong>邮箱</strong><span>${escapeHtml(user.email || "-")}</span></div>
                <div><strong>手机号</strong><span>${escapeHtml(user.phone || "-")}</span></div>
                <div><strong>角色</strong><span>${escapeHtml(user.role || "-")}</span></div>
                <div><strong>状态</strong><span>${formatUserStatus(user.status)}</span></div>
                <div><strong>所属组织</strong><span>${joinTextList(user.groupNames)}</span></div>
                <div><strong>头像地址</strong><span>${escapeHtml(user.avatar || "-")}</span></div>
                <div><strong>最近登录时间</strong><span>${formatDateTime(user.lastLoginAt)}</span></div>
                <div><strong>最近登录 IP</strong><span>${escapeHtml(user.lastLoginIp || "-")}</span></div>
                <div><strong>创建时间</strong><span>${formatDateTime(user.createdAt)}</span></div>
                <div><strong>更新时间</strong><span>${formatDateTime(user.updatedAt)}</span></div>
                <div class="full-width"><strong>备注</strong><span>${escapeHtml(user.remark || "-")}</span></div>
            </div>
            ${buildOrganizationAttributeDisplay(groupDetails, user.attributes || [])}
            ${buildAttributeSection("user", user.userId, user.attributes || [], user.nickname || user.userId || "当前用户")}
        `);

        const managerButton = document.querySelector("[data-attribute-manage]");
        if (managerButton) {
            managerButton.addEventListener("click", () => {
                openAttributeManager("user", user.userId, `${user.nickname || user.userId} 的自定义字段`, { onSaved: () => openUserDetail(userId) });
            });
        }
    } catch (error) {
        showToast(resolveFeedbackMessage("加载用户详情失败", error), "error");
    }
}

async function openUserForm(mode, userId = null) {
    const isEdit = mode === "edit";
    if (!hasMenuPermission(isEdit ? actionPermissions.user.update : actionPermissions.user.create)) {
        showToast(`missing permission: ${isEdit ? actionPermissions.user.update : actionPermissions.user.create}`, "error");
        return;
    }
    let user = {};
    let groupOptions = [];
    let selectedGroupIds = [];
    let currentGroupDetails = [];
    if (isEdit) {
        const [userData, groups] = await Promise.all([
            apiFetch(`/api/v1/users/${userId}/detail`),
            fetchGroupOptions()
        ]);
        user = userData.data || {};
        groupOptions = groups;
        selectedGroupIds = user.groupIds || [];
        currentGroupDetails = await fetchGroupDetails(selectedGroupIds);
    } else {
        groupOptions = await fetchGroupOptions();
    }

    openModal(isEdit ? "编辑用户" : "新建用户", `
        <form id="userForm" class="stack-form">
            <div class="two-field">
                <label><span>昵称</span><input id="userNicknameInput" value="${escapeHtml(user.nickname || "")}" required></label>
                <label><span>角色</span><input id="userRoleInput" value="${escapeHtml(user.role || "")}" placeholder="如：admin / user"></label>
            </div>
            <div class="two-field">
                <label><span>邮箱</span><input id="userEmailInput" value="${escapeHtml(user.email || "")}"></label>
                <label><span>手机号</span><input id="userPhoneInput" value="${escapeHtml(user.phone || "")}"></label>
            </div>
            <div class="two-field">
                <label><span>头像地址</span><input id="userAvatarInput" value="${escapeHtml(user.avatar || "")}"></label>
                <label>
                    <span>状态</span>
                    <select id="userStatusInput">
                        <option value="1" ${String(user.status ?? "1") === "1" || user.status === "active" ? "selected" : ""}>正常</option>
                        <option value="2" ${String(user.status) === "2" || user.status === "disabled" ? "selected" : ""}>禁用</option>
                        <option value="3" ${String(user.status) === "3" || user.status === "locked" ? "selected" : ""}>锁定</option>
                    </select>
                </label>
            </div>
            <label>
                <span>所属组织</span>
                <div id="userGroupChecklist" class="checklist-panel">
                    ${groupOptions.length
                        ? groupOptions.map((group) => `
                            <label class="checklist-item">
                                <input type="checkbox" value="${escapeHtml(group.groupId)}" ${selectedGroupIds.includes(group.groupId) ? "checked" : ""}>
                                <span>${escapeHtml(group.groupName || group.groupId)}</span>
                            </label>
                        `).join("")
                        : `<div class="empty-inline">暂无可选组织</div>`}
                </div>
                <small class="subtle">可勾选多个组织，保存后会同步成员关系</small>
            </label>
            <label><span>备注</span><textarea id="userRemarkInput">${escapeHtml(user.remark || "")}</textarea></label>
            <div id="userOrganizationAttributeSection">
                ${buildOrganizationAttributeEditor(currentGroupDetails, user.attributes || [])}
            </div>
            ${isEdit ? "" : `<label><span>初始密码</span><input id="userPasswordInput" type="password" required></label>`}
            <button type="submit" class="primary-button">${isEdit ? "保存修改" : "创建用户"}</button>
        </form>
    `);

    const groupChecklist = document.getElementById("userGroupChecklist");
    const orgAttributeSection = document.getElementById("userOrganizationAttributeSection");
    if (groupChecklist) {
        groupChecklist.addEventListener("change", async () => {
            const groupIds = getCheckedValues(groupChecklist);
            const details = groupIds.length ? await fetchGroupDetails(groupIds) : [];
            renderOrganizationAttributeEditor(orgAttributeSection, details, user.attributes || []);
        });
    }

    document.getElementById("userForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = {
            nickname: document.getElementById("userNicknameInput").value.trim(),
            role: document.getElementById("userRoleInput").value.trim() || undefined,
            email: document.getElementById("userEmailInput").value.trim() || undefined,
            phone: document.getElementById("userPhoneInput").value.trim() || undefined,
            avatar: document.getElementById("userAvatarInput").value.trim() || undefined,
            status: Number(document.getElementById("userStatusInput").value),
            remark: document.getElementById("userRemarkInput").value.trim() || undefined
        };
        const desiredGroupIds = getCheckedValues(document.getElementById("userGroupChecklist"));
        try {
            if (isEdit) {
                await apiFetch("/api/v1/users", {
                    method: "PUT",
                    body: JSON.stringify({
                        userIds: [userId],
                        ...payload
                    })
                });
                const currentGroupIds = user.groupIds || [];
                const groupsToAdd = desiredGroupIds.filter((groupId) => !currentGroupIds.includes(groupId));
                const groupsToRemove = currentGroupIds.filter((groupId) => !desiredGroupIds.includes(groupId));
                for (const groupId of groupsToAdd) {
                    await apiFetch(`/api/v1/groups/${groupId}/members`, {
                        method: "POST",
                        body: JSON.stringify({
                            userIds: [userId],
                            role: "member"
                        })
                    });
                }
                for (const groupId of groupsToRemove) {
                    await apiFetch(`/api/v1/groups/${groupId}/members`, {
                        method: "DELETE",
                        body: JSON.stringify({
                            userIds: [userId]
                        })
                    });
                }
                await syncOrganizationAttributes(userId, user.attributes || [], document.getElementById("userForm"));
            } else {
                const created = await apiFetch("/api/v1/users", {
                    method: "POST",
                    body: JSON.stringify({
                        ...payload,
                        password: document.getElementById("userPasswordInput").value
                    })
                });
                const createdUserId = created.data?.userId;
                if (desiredGroupIds.length && createdUserId) {
                    for (const groupId of desiredGroupIds) {
                        await apiFetch(`/api/v1/groups/${groupId}/members`, {
                            method: "POST",
                            body: JSON.stringify({
                                userIds: [createdUserId],
                                role: "member"
                            })
                        });
                    }
                    await syncOrganizationAttributes(createdUserId, [], document.getElementById("userForm"));
                }
            }
            closeModal();
            showToast(isEdit ? "用户已更新" : "用户已创建", "success");
            loadUsers();
        } catch (error) {
            showToast(resolveFeedbackMessage(isEdit ? "更新用户失败" : "创建用户失败", error), "error");
        }
    });
}

async function deleteUser(userId) {
    if (!hasMenuPermission(actionPermissions.user.delete)) {
        showToast(`missing permission: ${actionPermissions.user.delete}`, "error");
        return;
    }
    if (!window.confirm("确认删除这个用户吗？")) {
        return;
    }
    try {
        await apiFetch(`/api/v1/users/${userId}?permanent=false`, { method: "DELETE" });
        showToast("用户已删除", "success");
        loadUsers();
    } catch (error) {
        showToast(resolveFeedbackMessage("删除用户失败", error), "error");
    }
}

function openResetUserPasswordModal(userId, nickname) {
    if (!hasMenuPermission(actionPermissions.user.resetPassword)) {
        showToast(`missing permission: ${actionPermissions.user.resetPassword}`, "error");
        return;
    }
    openModal("重置用户密码", `
        <form id="resetUserPasswordForm" class="stack-form">
            <div class="subtle">正在为 ${escapeHtml(nickname)} 重置密码</div>
            <label><span>新密码</span><input id="resetUserNewPasswordInput" type="password" required></label>
            <button type="submit" class="primary-button">确认重置</button>
        </form>
    `);

    document.getElementById("resetUserPasswordForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            await apiFetch(`/api/v1/users/${userId}/password/reset`, {
                method: "POST",
                body: JSON.stringify({ newPassword: document.getElementById("resetUserNewPasswordInput").value })
            });
            closeModal();
            showToast("用户密码已重置", "success");
        } catch (error) {
            showToast(resolveFeedbackMessage("重置用户密码失败", error), "error");
        }
    });
}

async function loadGroups() {
    if (!hasMenuPermission(actionPermissions.group.list)) {
        configureGroupPageAccess();
        return;
    }
    try {
        const keyword = document.getElementById("groupKeyword").value.trim();
        const params = new URLSearchParams({ page: "1", pageSize: "20" });
        if (keyword) {
            params.set("keyword", keyword);
        }
        const data = await apiFetch(`/api/v1/groups?${params.toString()}`);
        renderTable(dom.groupsTable, [
            { label: "组织", render: (row) => `${escapeHtml(row.groupName || "-")}<br><small>${escapeHtml(row.groupId || "-")}</small>` },
            { label: "描述", render: (row) => escapeHtml(row.description || "-") },
            { label: "成员数", render: (row) => escapeHtml(row.memberCount ?? "-") },
            { label: "成员名称", render: (row) => joinTextList(row.memberNames) },
            { label: "状态", render: (row) => formatGroupStatus(row.status) },
            { label: "临时字段", render: (row) => row.tempExpireAt ? formatDateTime(row.tempExpireAt) : "-" },
            {
                label: "操作",
                render: (row) => actionButtons([
                    `<button class="mini-button" data-group-action="detail" data-group-id="${row.groupId}">详情</button>`,
                    hasMenuPermission(actionPermissions.group.update) ? `<button class="mini-button" data-group-action="edit" data-group-id="${row.groupId}">编辑</button>` : "",
                    hasMenuPermission(actionPermissions.group.update) ? `<button class="mini-button" data-group-action="attributes" data-group-id="${row.groupId}" data-group-name="${escapeHtml(row.groupName || row.groupId || "")}">字段</button>` : "",
                    hasMenuPermission(actionPermissions.permission.grant, actionPermissions.group.permission) ? `<button class="mini-button" data-group-action="grant" data-group-id="${row.groupId}" data-group-name="${escapeHtml(row.groupName || row.groupId || "")}">授权</button>` : "",
                    hasMenuPermission(actionPermissions.group.delete) ? `<button class="mini-button danger" data-group-action="delete" data-group-id="${row.groupId}">删除</button>` : ""
                ])
            }
        ], data.data?.list || []);

        dom.groupsTable.querySelectorAll("[data-group-action]").forEach((button) => {
            const groupId = button.dataset.groupId;
            const action = button.dataset.groupAction;
            const groupName = button.dataset.groupName || groupId;
            button.addEventListener("click", () => {
                if (action === "detail") {
                    openGroupDetail(groupId);
                }
                if (action === "edit") {
                    openGroupForm("edit", groupId);
                }
                if (action === "attributes") {
                    openAttributeManager("group", groupId, `${groupName} 的自定义字段`, { onSaved: () => openGroupDetail(groupId) });
                }
                if (action === "grant") {
                    openPermissionGrantModal("group", groupId, groupName);
                }
                if (action === "delete") {
                    deleteGroup(groupId);
                }
            });
        });
    } catch (error) {
        dom.groupsTable.className = "table-wrap empty-state";
        dom.groupsTable.textContent = "加载组织失败";
        showToast(resolveFeedbackMessage("加载组织失败", error), "error");
    }
}

async function openGroupDetail(groupId) {
    try {
        const data = await apiFetch(`/api/v1/groups/${groupId}`);
        const group = data.data || {};
        openModal("组织详情", `
            <div class="detail-grid">
                <div><strong>组织 ID</strong><span>${escapeHtml(group.groupId || "-")}</span></div>
                <div><strong>组织名称</strong><span>${escapeHtml(group.groupName || "-")}</span></div>
                <div><strong>状态</strong><span>${formatGroupStatus(group.status)}</span></div>
                <div><strong>我的角色</strong><span>${escapeHtml(group.myRole || "-")}</span></div>
                <div><strong>成员数</strong><span>${escapeHtml(group.memberCount ?? 0)}</span></div>
                <div><strong>成员名称</strong><span>${joinTextList(group.memberNames)}</span></div>
                <div><strong>临时密码</strong><span>${group.hasTempPassword ? "已启用" : "未启用"}</span></div>
                <div><strong>临时密码过期</strong><span>${formatDateTime(group.tempPasswordExpireAt || group.tempExpireAt)}</span></div>
                <div><strong>创建时间</strong><span>${formatDateTime(group.createdAt)}</span></div>
                <div><strong>更新时间</strong><span>${formatDateTime(group.updatedAt)}</span></div>
                <div class="full-width"><strong>描述</strong><span>${escapeHtml(group.description || "-")}</span></div>
                <div class="full-width"><strong>权限</strong><span>${joinTextList((group.permissions || []).map((item) => item.permCode || item.permName))}</span></div>
                <div class="full-width"><strong>成员明细</strong><span>${joinTextList((group.members || []).map((item) => item.nickname || item.userId))}</span></div>
            </div>
            ${buildAttributeSection("group", group.groupId, group.attributes || [], group.groupName || group.groupId || "当前组织")}
        `);

        const managerButton = document.querySelector("[data-attribute-manage]");
        if (managerButton) {
            managerButton.addEventListener("click", () => {
                openAttributeManager("group", group.groupId, `${group.groupName || group.groupId} 的自定义字段`, { onSaved: () => openGroupDetail(groupId) });
            });
        }
    } catch (error) {
        showToast(resolveFeedbackMessage("加载组织详情失败", error), "error");
    }
}

async function openGroupForm(mode, groupId = null) {
    const isEdit = mode === "edit";
    if (!hasMenuPermission(isEdit ? actionPermissions.group.update : actionPermissions.group.create)) {
        showToast(`missing permission: ${isEdit ? actionPermissions.group.update : actionPermissions.group.create}`, "error");
        return;
    }
    let group = {};
    if (isEdit) {
        const data = await apiFetch(`/api/v1/groups/${groupId}`);
        group = data.data || {};
    }

    openModal(isEdit ? "编辑组织" : "新建组织", `
        <form id="groupForm" class="stack-form">
            <label><span>组织名称</span><input id="groupNameInput" value="${escapeHtml(group.groupName || "")}" required></label>
            <label><span>描述</span><textarea id="groupDescriptionInput">${escapeHtml(group.description || "")}</textarea></label>
            ${isEdit ? `
                <label>
                    <span>状态</span>
                    <select id="groupStatusInput">
                        <option value="1" ${String(group.status ?? "1") === "1" || group.status === "active" ? "selected" : ""}>正常</option>
                        <option value="2" ${String(group.status) === "2" || group.status === "disabled" ? "selected" : ""}>禁用</option>
                    </select>
                </label>
            ` : `
                <div class="two-field">
                    <label><span>初始成员（用户 ID，逗号分隔）</span><input id="groupMembersInput" placeholder="可选"></label>
                    <label><span>临时密码</span><input id="groupTempPasswordInput" placeholder="可选"></label>
                </div>
                <label><span>临时密码有效小时数</span><input id="groupExpireHoursInput" type="number" value="24" min="1"></label>
                ${buildGroupAttributeDraftEditor()}
            `}
            <button type="submit" class="primary-button">${isEdit ? "保存修改" : "创建组织"}</button>
        </form>
    `);

    if (!isEdit) {
        wireGroupDraftAttributeEditor();
    }

    document.getElementById("groupForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            if (isEdit) {
                await apiFetch("/api/v1/groups", {
                    method: "PUT",
                    body: JSON.stringify({
                        groupIds: [groupId],
                        groupName: document.getElementById("groupNameInput").value.trim(),
                        description: document.getElementById("groupDescriptionInput").value.trim(),
                        status: Number(document.getElementById("groupStatusInput").value)
                    })
                });
            } else {
                const created = await apiFetch("/api/v1/groups", {
                    method: "POST",
                    body: JSON.stringify({
                        groupName: document.getElementById("groupNameInput").value.trim(),
                        description: document.getElementById("groupDescriptionInput").value.trim(),
                        initialMembers: document.getElementById("groupMembersInput").value
                            .split(",")
                            .map((item) => item.trim())
                            .filter(Boolean),
                        tempPassword: document.getElementById("groupTempPasswordInput").value.trim() || undefined,
                        tempPasswordExpireHours: Number(document.getElementById("groupExpireHoursInput").value || 24)
                    })
                });
                const createdGroupId = created.data?.groupId;
                const draftAttributes = collectDraftGroupAttributes();
                for (const attribute of draftAttributes) {
                    if (!createdGroupId) {
                        break;
                    }
                    await apiFetch(`/api/v1/groups/${createdGroupId}/attributes`, {
                        method: "POST",
                        body: JSON.stringify(attribute)
                    });
                }
            }
            closeModal();
            showToast(isEdit ? "组织已更新" : "组织已创建", "success");
            loadGroups();
        } catch (error) {
            showToast(resolveFeedbackMessage(isEdit ? "更新组织失败" : "创建组织失败", error), "error");
        }
    });
}

async function deleteGroup(groupId) {
    if (!hasMenuPermission(actionPermissions.group.delete)) {
        showToast(`missing permission: ${actionPermissions.group.delete}`, "error");
        return;
    }
    if (!window.confirm("确认删除这个组织吗？")) {
        return;
    }
    try {
        await apiFetch("/api/v1/groups", {
            method: "DELETE",
            body: JSON.stringify({ ids: [groupId], force: true })
        });
        showToast("组织已删除", "success");
        loadGroups();
    } catch (error) {
        showToast(resolveFeedbackMessage("删除组织失败", error), "error");
    }
}

function renderPermissionTree(nodes) {
    if (!nodes.length) {
        dom.permissionsTree.className = "tree-wrap empty-state";
        dom.permissionsTree.textContent = "暂无权限数据";
        return;
    }
    dom.permissionsTree.className = "tree-wrap";
    dom.permissionsTree.innerHTML = nodes.map((node) => renderPermissionNode(node)).join("");
    dom.permissionsTree.querySelectorAll("[data-perm-action]").forEach((button) => {
        const permId = button.dataset.permId;
        const action = button.dataset.permAction;
        button.addEventListener("click", () => {
            if (action === "edit") {
                openPermissionForm("edit", permId);
            }
            if (action === "delete") {
                deletePermission(permId);
            }
        });
    });
}

function renderPermissionNode(node) {
    return `
        <div class="tree-node">
            <div class="section-header">
                <div>
                    <div class="tree-title">${escapeHtml(node.permCode || "-")}</div>
                    <div class="tree-meta">${escapeHtml(node.path || "-")}${node.permName ? ` · ${escapeHtml(node.permName)}` : ""}</div>
                </div>
                <div class="table-actions">
                    ${hasMenuPermission(actionPermissions.permission.update) ? `<button class="mini-button" data-perm-action="edit" data-perm-id="${node.permId}">编辑</button>` : ""}
                    ${hasMenuPermission(actionPermissions.permission.delete) ? `<button class="mini-button danger" data-perm-action="delete" data-perm-id="${node.permId}">删除</button>` : ""}
                </div>
            </div>
            ${(node.children || []).map((child) => renderPermissionNode(child)).join("")}
        </div>
    `;
}

async function loadPermissions() {
    if (!hasMenuPermission(actionPermissions.permission.list)) {
        configurePermissionPageAccess();
        return;
    }
    try {
        const keyword = document.getElementById("permissionKeyword").value.trim();
        const suffix = keyword ? `?keyword=${encodeURIComponent(keyword)}` : "";
        const data = await apiFetch(`/api/v1/permissions${suffix}`);
        renderPermissionTree(data.data || []);
    } catch (error) {
        dom.permissionsTree.className = "tree-wrap empty-state";
        dom.permissionsTree.textContent = "加载权限失败";
        showToast(resolveFeedbackMessage("加载权限失败", error), "error");
    }
}

async function openPermissionForm(mode, permId = null) {
    const isEdit = mode === "edit";
    if (!hasMenuPermission(isEdit ? actionPermissions.permission.update : actionPermissions.permission.create)) {
        showToast(`missing permission: ${isEdit ? actionPermissions.permission.update : actionPermissions.permission.create}`, "error");
        return;
    }
    let current = {};
    let allPermissions = [];
    try {
        const listData = await apiFetch("/api/v1/permissions");
        allPermissions = flattenPermissions(listData.data || []);
        if (isEdit) {
            current = allPermissions.find((item) => item.permId === permId) || {};
        }
    } catch (error) {
        showToast(resolveFeedbackMessage("加载权限表单失败", error), "error");
        return;
    }

    const parentOptions = [`<option value="">顶级权限</option>`].concat(
        allPermissions
            .filter((item) => !isEdit || item.permId !== permId)
            .map((item) => `<option value="${item.permId}" ${item.permId === current.parentId ? "selected" : ""}>${escapeHtml(item.permCode)}</option>`)
    ).join("");

    openModal(isEdit ? "编辑权限" : "新建权限", `
        <form id="permissionForm" class="stack-form">
            <label><span>权限编码</span><input id="permCodeInput" value="${escapeHtml(current.permCode || "")}" ${isEdit ? "disabled" : ""} required></label>
            <label><span>权限名称</span><input id="permNameInput" value="${escapeHtml(current.permName || "")}" required></label>
            <label><span>父级权限</span><select id="permParentInput">${parentOptions}</select></label>
            <div class="two-field">
                <label><span>路径</span><input id="permPathInput" value="${escapeHtml(current.path || "")}"></label>
                <label><span>图标</span><input id="permIconInput" value="${escapeHtml(current.icon || "")}"></label>
            </div>
            <div class="two-field">
                <label><span>排序</span><input id="permSortInput" type="number" value="${Number(current.sort ?? 0)}"></label>
                ${isEdit ? `<div></div>` : `
                    <label>
                        <span>权限类型</span>
                        <select id="permTypeInput">
                            <option value="menu">menu</option>
                            <option value="api">api</option>
                        </select>
                    </label>
                `}
            </div>
            <label><span>描述</span><textarea id="permDescInput">${escapeHtml(current.description || "")}</textarea></label>
            <button type="submit" class="primary-button">${isEdit ? "保存修改" : "创建权限"}</button>
        </form>
    `);

    document.getElementById("permissionForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = {
            permName: document.getElementById("permNameInput").value.trim(),
            parentId: document.getElementById("permParentInput").value || null,
            path: document.getElementById("permPathInput").value.trim() || null,
            icon: document.getElementById("permIconInput").value.trim() || null,
            sort: Number(document.getElementById("permSortInput").value || 0),
            description: document.getElementById("permDescInput").value.trim() || null
        };
        try {
            if (isEdit) {
                await apiFetch(`/api/v1/permissions/${permId}`, {
                    method: "PUT",
                    body: JSON.stringify(payload)
                });
            } else {
                await apiFetch("/api/v1/permissions", {
                    method: "POST",
                    body: JSON.stringify({
                        ...payload,
                        permCode: document.getElementById("permCodeInput").value.trim(),
                        methodType: document.getElementById("permTypeInput").value
                    })
                });
            }
            closeModal();
            showToast(isEdit ? "权限已更新" : "权限已创建", "success");
            loadPermissions();
        } catch (error) {
            showToast(resolveFeedbackMessage(isEdit ? "更新权限失败" : "创建权限失败", error), "error");
        }
    });
}

async function deletePermission(permId) {
    if (!hasMenuPermission(actionPermissions.permission.delete)) {
        showToast(`missing permission: ${actionPermissions.permission.delete}`, "error");
        return;
    }
    if (!window.confirm("确认删除这个权限吗？")) {
        return;
    }
    try {
        await apiFetch(`/api/v1/permissions/${permId}?force=true`, { method: "DELETE" });
        showToast("权限已删除", "success");
        loadPermissions();
    } catch (error) {
        showToast(resolveFeedbackMessage("删除权限失败", error), "error");
    }
}

async function openPermissionGrantModal(targetType, targetId, targetName) {
    if (!hasMenuPermission(actionPermissions.permission.grant)) {
        showToast(`missing permission: ${actionPermissions.permission.grant}`, "error");
        return;
    }
    try {
        const [allRes, currentRes] = await Promise.all([
            apiFetch("/api/v1/permissions"),
            apiFetch(targetType === "user" ? `/api/v1/users/${targetId}/permissions` : `/api/v1/groups/${targetId}/permissions`)
        ]);
        const allPermissions = flattenPermissions(allRes.data || []);
        const currentPermissions = flattenPermissions(currentRes.data || []);
        const selected = new Set(currentPermissions.map((item) => item.permId));

        openModal(`${targetType === "user" ? "用户" : "组织"}授权管理`, `
            <form id="grantPermissionForm" class="stack-form">
                <div class="subtle">当前对象：${escapeHtml(targetName)}</div>
                <div class="permission-pick-list">
                    ${allPermissions.map((item) => `
                        <label class="permission-pick-item">
                            <input type="checkbox" value="${item.permId}" ${selected.has(item.permId) ? "checked" : ""}>
                            <span>${escapeHtml(item.permCode)}${item.permName ? ` · ${escapeHtml(item.permName)}` : ""}</span>
                        </label>
                    `).join("")}
                </div>
                <button type="submit" class="primary-button">保存授权</button>
            </form>
        `);

        document.getElementById("grantPermissionForm").addEventListener("submit", async (event) => {
            event.preventDefault();
            const checkedIds = Array.from(document.querySelectorAll('#grantPermissionForm input[type="checkbox"]:checked')).map((input) => input.value);
            const checkedSet = new Set(checkedIds);
            const currentSet = new Set(currentPermissions.map((item) => item.permId));
            const toGrant = checkedIds.filter((id) => !currentSet.has(id));
            const toRevoke = Array.from(currentSet).filter((id) => !checkedSet.has(id));

            try {
                if (toGrant.length) {
                    await apiFetch(targetType === "user" ? `/api/v1/users/${targetId}/permissions` : `/api/v1/groups/${targetId}/permissions`, {
                        method: "POST",
                        body: JSON.stringify({
                            permIds: toGrant,
                            overwrite: false
                        })
                    });
                }
                if (toRevoke.length) {
                    await apiFetch(targetType === "user" ? `/api/v1/users/${targetId}/permissions` : `/api/v1/groups/${targetId}/permissions`, {
                        method: "DELETE",
                        body: JSON.stringify({
                            permIds: toRevoke
                        })
                    });
                }
                if (!toGrant.length && !toRevoke.length) {
                    showToast("权限没有变化", "info");
                    return;
                }
                closeModal();
                showToast("授权设置已保存", "success");
            } catch (error) {
                showToast(resolveFeedbackMessage("保存授权失败", error), "error");
            }
        });
    } catch (error) {
        showToast(resolveFeedbackMessage("加载授权信息失败", error), "error");
    }
}

async function loadDevices() {
    try {
        const data = await apiFetch("/api/v1/devices");
        renderDevices(data.data || []);
    } catch (error) {
        dom.devicesList.className = "empty-state";
        dom.devicesList.textContent = resolveFeedbackMessage("加载设备失败", error);
    }
}

function renderDevices(devices) {
    if (!devices.length) {
        dom.devicesList.className = "empty-state";
        dom.devicesList.textContent = "暂无设备会话";
        return;
    }
    dom.devicesList.className = "";
    dom.devicesList.innerHTML = devices.map((device) => `
        <article class="flow-node">
            <div class="section-header">
                <div>
                    <strong>${escapeHtml(device.deviceName || "未知设备")}</strong>
                    <div class="tree-meta">${escapeHtml(device.loginIp || "-")} · ${escapeHtml(device.browser || "Unknown")} / ${escapeHtml(device.os || "Unknown")}</div>
                </div>
                ${device.current ? `<span class="user-chip">当前设备</span>` : `<button class="secondary-button danger-button" data-kick="${device.sessionId}">下线</button>`}
            </div>
            <div class="tree-meta">登录时间：${formatDateTime(device.loginAt)} | 最近活跃：${formatDateTime(device.lastActiveAt)}</div>
        </article>
    `).join("");
    dom.devicesList.querySelectorAll("[data-kick]").forEach((button) => {
        button.addEventListener("click", async () => {
            try {
                await apiFetch(`/api/v1/devices/${button.dataset.kick}`, { method: "DELETE" });
                showToast("设备已下线", "success");
                loadDevices();
            } catch (error) {
                showToast(resolveFeedbackMessage("设备下线失败", error), "error");
            }
        });
    });
}

async function loadFlow() {
    try {
        const data = await apiFetch("/api/v1/flows/register");
        const flow = data.data;
        if (!flow || !flow.nodes) {
            dom.flowCanvas.className = "flow-canvas empty-state";
            dom.flowCanvas.textContent = "暂无流程定义";
            dom.flowEditor.value = "";
            return;
        }
        dom.flowCanvas.className = "flow-canvas";
        dom.flowCanvas.innerHTML = flow.nodes.map((node) => {
            const transitions = (flow.transitions || []).filter((item) => item.from === node.id);
            return `
                <article class="flow-node">
                    <small>${escapeHtml(node.type || "unknown")}</small>
                    <strong>${escapeHtml(node.id)}</strong>
                    <div class="tree-meta">${node.action ? `action: ${escapeHtml(node.action)}` : ""}${node.condition ? ` condition: ${escapeHtml(node.condition)}` : ""}</div>
                    <ul>${transitions.map((transition) => `<li>→ ${escapeHtml(transition.to)}${transition.on === true ? " [true]" : transition.on === false ? " [false]" : ""}</li>`).join("")}</ul>
                </article>
            `;
        }).join("");
        dom.flowEditor.value = flow.yaml || "";
    } catch (error) {
        dom.flowCanvas.className = "flow-canvas empty-state";
        dom.flowCanvas.textContent = resolveFeedbackMessage("加载流程失败", error);
    }
}

async function openProfileCenter() {
    try {
        const data = await apiFetch("/api/v1/users/me");
        const user = data.data || {};
        const groupDetails = await fetchGroupDetails(user.groupIds || []);
        openModal("个人中心", `
            <div class="profile-grid">
                <section class="stack-form">
                    <h4>个人资料</h4>
                    <label><span>昵称</span><input id="profileNicknameInput" value="${escapeHtml(user.nickname || "")}"></label>
                    <label><span>头像地址</span><input id="profileAvatarInput" value="${escapeHtml(user.avatar || "")}"></label>
                    <label><span>邮箱</span><input value="${escapeHtml(user.email || "-")}" disabled></label>
                    <label><span>手机号</span><input value="${escapeHtml(user.phone || "-")}" disabled></label>
                    <label><span>角色</span><input value="${escapeHtml(user.role || "-")}" disabled></label>
                    <label><span>最近登录时间</span><input value="${escapeHtml(user.lastLoginAt || "-")}" disabled></label>
                    <label><span>最近登录 IP</span><input value="${escapeHtml(user.lastLoginIp || "-")}" disabled></label>
                    <button id="saveProfileBtn" class="primary-button">保存资料</button>
                    <button id="manageMyAttributesBtn" type="button" class="secondary-button">管理个人自定义字段</button>
                </section>
                <section class="stack-form">
                    <h4>修改密码</h4>
                    <label><span>旧密码</span><input id="profileOldPasswordInput" type="password"></label>
                    <label><span>新密码</span><input id="profileNewPasswordInput" type="password"></label>
                    <label><span>确认新密码</span><input id="profileConfirmPasswordInput" type="password"></label>
                    <button id="changeMyPasswordBtn" class="secondary-button">修改密码</button>
                </section>
            </div>
            ${buildOrganizationAttributeDisplay(groupDetails, user.attributes || [])}
            <section class="detail-section">
                <div class="section-header">
                    <div>
                        <strong>当前个人自定义字段</strong>
                        <div class="subtle">这里展示的是已保存到后端的个人属性</div>
                    </div>
                </div>
                ${renderAttributeList(user.attributes || [])}
            </section>
            <section class="detail-section">
                <div class="section-header">
                    <div>
                        <strong>账号注销</strong>
                        <div class="subtle">注销后账号将不可恢复，请谨慎操作</div>
                    </div>
                </div>
                <button id="deactivateAccountBtn" class="secondary-button" style="background:rgba(188,75,81,0.12);color:#8d2b31;">注销当前账号</button>
            </section>
        `);

        document.getElementById("saveProfileBtn").addEventListener("click", async () => {
            try {
                const payload = {
                    nickname: document.getElementById("profileNicknameInput").value.trim(),
                    avatar: document.getElementById("profileAvatarInput").value.trim()
                };
                const updated = await apiFetch("/api/v1/users/me", {
                    method: "PUT",
                    body: JSON.stringify(payload)
                });
                state.user = updated.data || state.user;
                renderCurrentUserCard();
                renderDashboard();
                showToast("资料已保存", "success");
            } catch (error) {
                showToast(resolveFeedbackMessage("保存资料失败", error), "error");
            }
        });

        document.getElementById("manageMyAttributesBtn").addEventListener("click", () => {
            openAttributeManager("user", user.userId, "我的自定义字段", { onSaved: openProfileCenter });
        });

        document.getElementById("changeMyPasswordBtn").addEventListener("click", async () => {
            const oldPassword = document.getElementById("profileOldPasswordInput").value;
            const newPassword = document.getElementById("profileNewPasswordInput").value;
            const confirmPassword = document.getElementById("profileConfirmPasswordInput").value;
            if (!oldPassword || !newPassword) {
                showToast("请完整填写密码信息", "error");
                return;
            }
            if (newPassword !== confirmPassword) {
                showToast("两次输入的新密码不一致", "error");
                return;
            }
            try {
                await apiFetch("/api/v1/users/me/password", {
                    method: "POST",
                    body: JSON.stringify({ oldPassword, newPassword })
                });
                showToast("密码修改成功", "success");
                document.getElementById("profileOldPasswordInput").value = "";
                document.getElementById("profileNewPasswordInput").value = "";
                document.getElementById("profileConfirmPasswordInput").value = "";
            } catch (error) {
                showToast(resolveFeedbackMessage("密码修改失败", error), "error");
            }
        });

        const deactivateBtn = document.getElementById("deactivateAccountBtn");
        if (deactivateBtn) {
            deactivateBtn.addEventListener("click", async () => {
                if (!confirm("确定要注销当前账号吗？此操作不可恢复。")) return;
                try {
                    await apiFetch("/api/v1/cancel", { method: "POST" });
                    showToast("账号已注销", "success");
                    logout();
                } catch (error) {
                    showToast(resolveFeedbackMessage("注销失败", error), "error");
                }
            });
        }
    } catch (error) {
        showToast(resolveFeedbackMessage("加载个人中心失败", error), "error");
    }
}

function openForgotPasswordModal() {
    openModal("忘记密码", `
        <form id="resetPasswordForm" class="stack-form">
            <label><span>账号</span><input id="resetAccountInput" placeholder="请输入手机号或邮箱" required></label>
            <div class="inline-field">
                <label><span>验证码</span><input id="resetCodeInput" placeholder="请输入验证码" required></label>
                <button type="button" id="sendResetCodeBtn" class="secondary-button">发送验证码</button>
            </div>
            <label><span>新密码</span><input id="resetNewPasswordInput" type="password" placeholder="请输入新密码" required></label>
            <label><span>确认新密码</span><input id="resetConfirmPasswordInput" type="password" placeholder="请再次输入新密码" required></label>
            <button type="submit" class="primary-button">重置密码</button>
        </form>
    `);

    const sendButton = document.getElementById("sendResetCodeBtn");
    setSendCodeButtonState(sendButton, 0);
    sendButton.addEventListener("click", async () => {
        const account = document.getElementById("resetAccountInput").value.trim();
        if (!account) {
            showToast("请先输入账号", "error");
            return;
        }
        try {
            const data = await apiFetch("/api/v1/captcha/send", {
                method: "POST",
                body: JSON.stringify({
                    captchaType: detectAccountType(account) === "email" ? "email" : "sms",
                    account,
                    scene: "reset_password"
                })
            });
            showToast("验证码已发送", "success");
            startSendCodeCountdown(sendButton, data?.data?.resendWaitSeconds || 60);
        } catch (error) {
            showToast(resolveFeedbackMessage("发送验证码失败", error), "error");
        }
    });

    document.getElementById("resetPasswordForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        const account = document.getElementById("resetAccountInput").value.trim();
        const code = document.getElementById("resetCodeInput").value.trim();
        const newPassword = document.getElementById("resetNewPasswordInput").value;
        const confirmPassword = document.getElementById("resetConfirmPasswordInput").value;
        if (!account || !code || !newPassword) {
            showToast("请完整填写重置密码信息", "error");
            return;
        }
        if (newPassword !== confirmPassword) {
            showToast("两次输入的新密码不一致", "error");
            return;
        }
        try {
            await apiFetch("/api/v1/password/reset", {
                method: "POST",
                body: JSON.stringify({ account, code, newPassword })
            });
            showToast("密码重置成功，请使用新密码登录", "success");
            closeModal();
            showLoginView("phone-password");
            document.querySelector('#phonePasswordForm input[name="phone"]').value = account;
        } catch (error) {
            showToast(resolveFeedbackMessage("重置密码失败", error), "error");
        }
    });
}

function installSendCodeHandler(buttonId, getAccount, scene, emptyMessage) {
    const button = document.getElementById(buttonId);
    if (!button) {
        return;
    }
    setSendCodeButtonState(button, 0);
    button.addEventListener("click", async () => {
        const account = getAccount().trim();
        if (!account) {
            writeResponse("发送验证码失败", { message: emptyMessage });
            return;
        }
        try {
            const data = await apiFetch("/api/v1/captcha/send", {
                method: "POST",
                body: JSON.stringify({
                    captchaType: detectAccountType(account) === "email" ? "email" : "sms",
                    account,
                    scene
                })
            });
            showToast("验证码已发送", "success");
            startSendCodeCountdown(button, data?.data?.resendWaitSeconds || 60);
        } catch (error) {
            writeResponse("发送验证码失败", error);
        }
    });
}

async function logout() {
    try {
        await apiFetch("/api/v1/logout", { method: "POST" });
    } catch (error) {
        console.debug("logout failed", error);
    } finally {
        setSessionId(null);
        state.user = null;
        state.menus = [];
        showAuthApp();
    }
}

function bindEvents() {
    document.querySelectorAll("[data-method]").forEach((card) => {
        card.addEventListener("click", () => {
            const method = card.dataset.method;
            if (method === "feishu") {
                triggerFeishuLogin();
            } else {
                showLoginView(method);
            }
        });
    });

    document.querySelectorAll("[data-back]").forEach((btn) => {
        btn.addEventListener("click", () => showLoginMethodSelector());
    });

    document.querySelectorAll("[data-goto]").forEach((btn) => {
        btn.addEventListener("click", () => {
            if (btn.dataset.goto === "register") {
                showRegisterView();
            } else {
                showLoginMethodSelector();
            }
        });
    });

    if (dom.apiBaseInput) {
        dom.apiBaseInput.value = getApiBase();
        dom.apiBaseInput.addEventListener("change", () => {
            setApiBase(dom.apiBaseInput.value.trim() || DEFAULT_API_BASE);
            showToast(`API Base 已更新为 ${getApiBase()}`, "info");
        });
    }

    function setFormSubmitting(form, submitting) {
        const btn = form.querySelector('button[type="submit"]');
        if (!btn) return;
        if (!btn.dataset.originalText) btn.dataset.originalText = btn.textContent;
        btn.disabled = submitting;
        btn.textContent = submitting ? "处理中..." : btn.dataset.originalText;
    }

    document.getElementById("phonePasswordForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        setFormSubmitting(event.target, true);
        try {
            const payload = formToObject(event.target);
            payload.account = (payload.phone || "").trim();
            payload.loginType = "password";
            const data = await apiFetch("/api/v1/login", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            setSessionId(data.data.sessionId);
            await bootstrapConsole();
        } catch (error) {
            writeResponse("登录失败", error);
            setFormSubmitting(event.target, false);
        }
    });

    document.getElementById("emailCodeForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        setFormSubmitting(event.target, true);
        try {
            const payload = formToObject(event.target);
            payload.account = (payload.email || "").trim();
            payload.loginType = "code";
            const data = await apiFetch("/api/v1/login", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            setSessionId(data.data.sessionId);
            await bootstrapConsole();
        } catch (error) {
            writeResponse("验证码登录失败", error);
            setFormSubmitting(event.target, false);
        }
    });

    document.getElementById("registerForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        setFormSubmitting(event.target, true);
        try {
            const payload = formToObject(event.target);
            payload.account = (payload.account || "").trim();
            detectAccountType(payload.account);
            const data = await apiFetch("/api/v1/register", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            setSessionId(data.data.sessionId);
            await bootstrapConsole();
        } catch (error) {
            writeResponse("注册失败", error);
            setFormSubmitting(event.target, false);
        }
    });

    document.getElementById("profileCenterBtn").addEventListener("click", openProfileCenter);
    document.getElementById("forgotPasswordBtn").addEventListener("click", openForgotPasswordModal);

    async function triggerFeishuLogin() {
        const redirectUri = `${window.location.origin}${window.location.pathname}`;
        try {
            const data = await apiFetch(`/api/v1/oauth/feishu/authorize?redirectUri=${encodeURIComponent(redirectUri)}`);
            window.open(data.data.authorizeUrl, "_blank", "noopener");
        } catch (error) {
            showToast(resolveFeedbackMessage("飞书授权失败", error), "error");
        }
    }

    document.getElementById("logoutSidebarBtn").addEventListener("click", logout);
    document.getElementById("openDashboardMessagesBtn").addEventListener("click", openMessageDrawer);
    document.getElementById("dashboardSendMessageBtn").addEventListener("click", openSendMessageModal);
    document.getElementById("messageDrawerBtn").addEventListener("click", openMessageDrawer);
    document.getElementById("topbarSendMessageBtn").addEventListener("click", openSendMessageModal);
    document.getElementById("closeMessageDrawerBtn").addEventListener("click", closeMessageDrawer);
    document.getElementById("refreshMessagesBtn").addEventListener("click", () => {
        loadMessages({ page: 1, pageSize: state.messageCenter.pageSize }).catch((error) => {
            showToast(resolveFeedbackMessage("刷新站内消息失败", error), "error");
        });
    });
    document.getElementById("markAllMessagesReadBtn").addEventListener("click", markAllMessagesRead);
    document.getElementById("openSendMessageBtn").addEventListener("click", openSendMessageModal);
    document.querySelectorAll("[data-message-filter]").forEach((button) => {
        button.addEventListener("click", () => setMessageFilter(button.dataset.messageFilter));
    });
    document.getElementById("deviceDrawerBtn").addEventListener("click", () => {
        document.getElementById("deviceDrawer").classList.remove("hidden");
        loadDevices();
    });
    document.getElementById("closeDeviceDrawerBtn").addEventListener("click", () => {
        document.getElementById("deviceDrawer").classList.add("hidden");
    });

    document.getElementById("searchUsersBtn").addEventListener("click", loadUsers);
    document.getElementById("searchGroupsBtn").addEventListener("click", loadGroups);
    document.getElementById("searchPermissionsBtn").addEventListener("click", loadPermissions);
    document.getElementById("refreshFlowBtn").addEventListener("click", loadFlow);
    document.getElementById("openCreateUserBtn").addEventListener("click", () => openUserForm("create"));
    document.getElementById("openCreateGroupBtn").addEventListener("click", () => openGroupForm("create"));
    document.getElementById("openCreatePermissionBtn").addEventListener("click", () => openPermissionForm("create"));
    document.querySelectorAll("[data-notification-tab]").forEach((button) => {
        button.addEventListener("click", () => switchNotificationTab(button.dataset.notificationTab));
    });
    document.getElementById("refreshChannelsBtn")?.addEventListener("click", loadNotificationChannels);
    document.getElementById("openCreateChannelBtn")?.addEventListener("click", () => openChannelConfigModal());
    document.getElementById("searchTemplatesBtn")?.addEventListener("click", loadNotificationTemplates);
    document.getElementById("openCreateTemplateBtn")?.addEventListener("click", () => openTemplateForm());
    document.getElementById("searchReusableMessagesBtn")?.addEventListener("click", loadReusableMessages);
    document.getElementById("openReusableSendBtn")?.addEventListener("click", openReusableSendModal);
    document.getElementById("searchPoliciesBtn")?.addEventListener("click", loadNotificationPolicies);
    document.getElementById("openCreatePolicyBtn")?.addEventListener("click", () => openPolicyForm());
    document.querySelectorAll("[data-log-tab]").forEach((button) => {
        button.addEventListener("click", () => switchLogTab(button.dataset.logTab));
    });
    document.getElementById("searchLogsBtn")?.addEventListener("click", loadAccessLogs);
    document.getElementById("loadMetricBtn")?.addEventListener("click", loadLogMetric);
    dom.logMetricChart?.addEventListener("mousemove", updateMetricChartTooltip);
    dom.logMetricChart?.addEventListener("mouseleave", () => dom.logMetricChartTooltip?.classList.add("hidden"));
    window.addEventListener("resize", () => {
        if (state.currentPage === "logs" && state.logOps.tab === "metrics") {
            window.requestAnimationFrame(drawMetricChart);
        }
    });
    document.getElementById("searchAlertRulesBtn")?.addEventListener("click", loadAlertRules);
    document.getElementById("openCreateAlertRuleBtn")?.addEventListener("click", () => openAlertRuleModal());
    document.getElementById("searchAlertTriggersBtn")?.addEventListener("click", loadAlertTriggers);

    document.getElementById("prettyFlowBtn").addEventListener("click", () => {
        dom.flowEditor.value = dom.flowEditor.value.trim();
        showToast("流程草稿已整理", "success");
    });

    document.getElementById("copyFlowBtn").addEventListener("click", async () => {
        await navigator.clipboard.writeText(dom.flowEditor.value);
        showToast("流程草稿已复制", "success");
    });
}

function bootstrap() {
    bindEvents();
    installSendCodeHandler("sendEmailCodeBtn", () => document.querySelector('#emailCodeForm input[name="email"]').value, "login", "请先输入邮箱");
    installSendCodeHandler("sendRegisterCodeBtn", () => document.getElementById("registerAccountInput").value, "register", "请先输入手机号或邮箱");
    if (getSessionId()) {
        bootstrapConsole();
    } else {
        showAuthApp();
        showLoginMethodSelector();
    }
}

bootstrap();
