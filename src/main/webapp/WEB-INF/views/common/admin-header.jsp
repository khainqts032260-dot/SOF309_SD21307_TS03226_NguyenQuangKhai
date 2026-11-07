<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="role" value="${sessionScope.auth['vaiTro']}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${empty pageTitle ? 'Quản lý | Celine Closet' : pageTitle}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css" referrerpolicy="no-referrer">
    <link rel="stylesheet" href="${ctx}/assets/css/styles.css?v=staff-chat-profile-20260809">
    <c:if test="${needsMap}"><link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin=""></c:if>
</head>
<body class="admin-body ${role == 'ADMIN' ? 'owner-console' : 'staff-console'}">
<aside class="admin-sidebar refined-admin-sidebar role-separated-sidebar">
    <a class="admin-brand" href="${ctx}${role == 'ADMIN' ? '/admin/dashboard' : (role == 'DELIVERY' ? '/admin/order-tracking' : '/admin/orders')}"><b>C&amp;C</b><span>${role == 'ADMIN' ? 'CHỦ CỬA HÀNG' : (role == 'DELIVERY' ? 'NHÂN VIÊN GIAO HÀNG' : 'NHÂN VIÊN STAFF')}</span></a>

    <div class="admin-user-mini">
        <small>${role == 'ADMIN' ? 'Tài khoản chủ cửa hàng' : 'Nhân viên đang đăng nhập'}</small>
        <strong>${sessionScope.auth.hoTen}</strong>
        <c:if test="${role == 'STAFF' || role == 'DELIVERY'}"><span>Mã NV: NV<c:if test="${sessionScope.auth.maTK < 1000}">0</c:if><c:if test="${sessionScope.auth.maTK < 100}">0</c:if><c:if test="${sessionScope.auth.maTK < 10}">0</c:if>${sessionScope.auth.maTK}</span></c:if>
    </div>

    <nav class="admin-role-navigation">
        <c:choose>
            <c:when test="${role == 'ADMIN'}">
                <p class="admin-nav-label">Tổng quan</p>
                <a class="${activePath == '/admin/dashboard' ? 'active' : ''}" href="${ctx}/admin/dashboard"><i class="fa-solid fa-chart-pie"></i>Dashboard</a>

                <p class="admin-nav-label">Kinh doanh</p>
                <a class="${activePath == '/admin/products' ? 'active' : ''}" href="${ctx}/admin/products"><i class="fa-solid fa-shirt"></i>Sản phẩm</a>
                <a class="${activePath == '/admin/inventory' || activePath == '/admin/inventory-history' ? 'active' : ''}" href="${ctx}/admin/inventory"><i class="fa-solid fa-boxes-stacked"></i>Kho hàng</a>
                <a class="${activePath == '/admin/categories' ? 'active' : ''}" href="${ctx}/admin/categories"><i class="fa-solid fa-layer-group"></i>Danh mục</a>
                <a class="${activePath == '/admin/orders' ? 'active' : ''}" href="${ctx}/admin/orders"><i class="fa-solid fa-receipt"></i>Đơn hàng<c:if test="${accountNotificationCount > 0}"><span class="nav-unread-badge" data-account-notification data-api-url="${ctx}/api/notifications/unread">${accountNotificationCount}</span></c:if><c:if test="${accountNotificationCount == 0}"><span class="nav-unread-badge is-hidden" data-account-notification data-api-url="${ctx}/api/notifications/unread">0</span></c:if></a>
                <a class="${activePath == '/admin/order-tracking' ? 'active' : ''}" href="${ctx}/admin/order-tracking"><i class="fa-solid fa-location-dot"></i>Theo dõi giao hàng</a>
                <a class="${activePath == '/admin/vouchers' ? 'active' : ''}" href="${ctx}/admin/vouchers"><i class="fa-solid fa-ticket"></i>Voucher giảm giá</a>

                <p class="admin-nav-label">Khách hàng &amp; đội ngũ</p>
                <a class="${activePath == '/admin/customers' ? 'active' : ''}" href="${ctx}/admin/customers"><i class="fa-solid fa-users"></i>Khách hàng</a>
                <a class="${activePath == '/admin/carts' ? 'active' : ''}" href="${ctx}/admin/carts"><i class="fa-solid fa-cart-shopping"></i>Giỏ hàng</a>
                <a class="${activePath == '/admin/accounts' ? 'active' : ''}" href="${ctx}/admin/accounts"><i class="fa-solid fa-id-badge"></i>Nhân viên</a>
                <a class="${activePath == '/admin/staff-activity' ? 'active' : ''}" href="${ctx}/admin/staff-activity"><i class="fa-solid fa-list-check"></i>Nhật ký nhân viên</a>
                <a class="${activePath == '/admin/support' ? 'active' : ''}" href="${ctx}/admin/support"><i class="fa-solid fa-headset"></i>Hỗ trợ khách hàng<c:if test="${supportUnreadCount > 0}"><span class="nav-unread-badge" data-support-unread data-api-url="${ctx}/api/support/unread">${supportUnreadCount}</span></c:if><c:if test="${supportUnreadCount == 0}"><span class="nav-unread-badge is-hidden" data-support-unread data-api-url="${ctx}/api/support/unread">0</span></c:if></a>
                <a class="${activePath == '/admin/delivery-support' ? 'active' : ''}" href="${ctx}/admin/delivery-support"><i class="fa-solid fa-triangle-exclamation"></i>Duyệt giao thất bại</a>
                <a class="${activePath == '/admin/returns' ? 'active' : ''}" href="${ctx}/admin/returns"><i class="fa-solid fa-arrow-rotate-left"></i>Trả hàng & hoàn tiền</a>

                <p class="admin-nav-label">Nội dung &amp; báo cáo</p>
                <a class="${activePath == '/admin/news' ? 'active' : ''}" href="${ctx}/admin/news"><i class="fa-regular fa-newspaper"></i>Quản lý tin tức</a>
                <a class="${activePath == '/admin/news-categories' ? 'active' : ''}" href="${ctx}/admin/news-categories"><i class="fa-solid fa-tags"></i>Loại tin tức</a>
                <a class="${activePath == '/admin/revenue' ? 'active' : ''}" href="${ctx}/admin/revenue"><i class="fa-solid fa-chart-column"></i>Doanh thu</a>
            </c:when>
            <c:when test="${role == 'STAFF'}">
                <p class="admin-nav-label">Nghiệp vụ Staff</p>
                <a class="${activePath == '/admin/profile' ? 'active' : ''}" href="${ctx}/admin/profile"><i class="fa-solid fa-address-card"></i>Hồ sơ nhân viên</a>
                <a class="${activePath == '/admin/orders' && param.orderStatus == 'Chờ xác nhận' ? 'active' : ''}" href="${ctx}/admin/orders?orderStatus=Chờ xác nhận"><i class="fa-solid fa-bell"></i>Đơn hàng mới<span class="nav-unread-badge ${staffNewOrderCount <= 0 ? 'is-hidden' : ''}" data-staff-new-order-count>${staffNewOrderCount}</span></a>
                <a class="${activePath == '/admin/orders' && param.orderStatus != 'Chờ xác nhận' ? 'active' : ''}" href="${ctx}/admin/orders"><i class="fa-solid fa-clipboard-check"></i>Xử lý đơn & thanh toán<c:if test="${accountNotificationCount > 0}"><span class="nav-unread-badge" data-account-notification data-api-url="${ctx}/api/notifications/unread">${accountNotificationCount}</span></c:if><c:if test="${accountNotificationCount == 0}"><span class="nav-unread-badge is-hidden" data-account-notification data-api-url="${ctx}/api/notifications/unread">0</span></c:if></a>
                <a class="${activePath == '/admin/order-tracking' ? 'active' : ''}" href="${ctx}/admin/order-tracking"><i class="fa-solid fa-map-location-dot"></i>Theo dõi vị trí giao hàng</a>
                <a class="${activePath == '/admin/inventory' || activePath == '/admin/inventory-history' ? 'active' : ''}" href="${ctx}/admin/inventory"><i class="fa-solid fa-boxes-stacked"></i>Quản lý kho hàng</a>
                <a class="${activePath == '/admin/support' ? 'active' : ''}" href="${ctx}/admin/support"><i class="fa-solid fa-comments"></i>Chat hỗ trợ khách hàng<c:if test="${supportUnreadCount > 0}"><span class="nav-unread-badge" data-support-unread data-api-url="${ctx}/api/support/unread">${supportUnreadCount}</span></c:if><c:if test="${supportUnreadCount == 0}"><span class="nav-unread-badge is-hidden" data-support-unread data-api-url="${ctx}/api/support/unread">0</span></c:if></a>
                <div class="staff-permission-note"><i class="fa-solid fa-circle-check"></i><p>Staff được kiểm tra thanh toán, xử lý đơn, theo dõi vị trí giao hàng, nhập hàng, xem tồn kho và chat hỗ trợ khách hàng.</p></div>
            </c:when>
            <c:otherwise>
                <p class="admin-nav-label">Nghiệp vụ Delivery</p>
                <a class="${activePath == '/admin/profile' ? 'active' : ''}" href="${ctx}/admin/profile"><i class="fa-solid fa-address-card"></i>Hồ sơ nhân viên</a>
                <a class="${activePath == '/admin/order-tracking' ? 'active' : ''}" href="${ctx}/admin/order-tracking"><i class="fa-solid fa-truck-fast"></i>Đơn giao của tôi<span class="nav-unread-badge ${deliveryActiveOrderCount <= 0 ? 'is-hidden' : ''}" data-delivery-active-count>${deliveryActiveOrderCount}</span><c:if test="${accountNotificationCount > 0}"><span class="nav-unread-badge secondary-notification-badge" data-account-notification data-api-url="${ctx}/api/notifications/unread">${accountNotificationCount}</span></c:if><c:if test="${accountNotificationCount == 0}"><span class="nav-unread-badge is-hidden" data-account-notification data-api-url="${ctx}/api/notifications/unread">0</span></c:if></a>
                <a class="${activePath == '/admin/delivery-support' ? 'active' : ''}" href="${ctx}/admin/delivery-support"><i class="fa-solid fa-phone-volume"></i>Hỗ trợ giao thất bại</a>
                <a class="${activePath == '/admin/returns' ? 'active' : ''}" href="${ctx}/admin/returns"><i class="fa-solid fa-box-rotate-left"></i>Nhận hàng hoàn</a>
                <div class="staff-permission-note"><i class="fa-solid fa-location-dot"></i><p>Delivery chỉ xem đơn được phân công, nhận giao hàng, cập nhật vị trí, hoàn tất giao hàng và nhận hàng hoàn.</p></div>
            </c:otherwise>
        </c:choose>
    </nav>

    <div class="admin-sidebar-bottom"><a href="${ctx}/home"><i class="fa-solid fa-arrow-up-right-from-square"></i>Xem website</a><a href="${ctx}/logout"><i class="fa-solid fa-right-from-bracket"></i>Đăng xuất</a></div>
</aside>
<main class="admin-main refined-admin-main">
