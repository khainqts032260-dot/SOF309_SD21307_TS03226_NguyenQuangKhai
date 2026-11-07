<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Theo dõi giao hàng | Celine Closet" scope="request" />
<c:set var="needsMap" value="true" scope="request" />
<%@ include file="../common/admin-header.jsp" %>

<div class="admin-top refined-admin-top"><div><p class="eyebrow">Delivery tracking</p><h1>Theo dõi giao hàng</h1><p>Khách hàng và nhân viên cùng xem một dữ liệu vị trí. Nhân viên chỉ cập nhật được đơn được phân công cho mình.</p></div></div>
<c:if test="${param.saved == '1'}"><div class="alert success">Đã cập nhật đơn hàng.</div></c:if>
<c:if test="${param.error == 'permission'}"><div class="alert error">Đơn hàng này không thuộc mã nhân viên của bạn.</div></c:if>
<c:if test="${param.error == 'action'}"><div class="alert error">${sessionScope.orderActionError}</div><c:remove var="orderActionError" scope="session" /></c:if>

<c:if test="${role == 'DELIVERY'}">
<section class="admin-card delivery-global-gps-card">
    <form data-staff-location-form data-all-orders="true" data-api-url="${ctx}/api/order-location">
        <div><p class="eyebrow">GPS TOÀN BỘ ĐƠN</p><h2>Một lần cho tất cả đơn đang giao</h2><span>Trên điện thoại, bật chế độ tự động để gửi vị trí hiện tại cho mọi đơn được phân công mỗi 10 phút.</span></div>
        <input name="note" value="GPS tự động từ điện thoại shipper" aria-label="Ghi chú GPS">
        <div class="global-gps-actions"><button type="submit" class="btn btn-outline"><i class="fa-solid fa-location-crosshairs"></i> Gửi vị trí ngay</button><button type="button" class="btn btn-dark" data-auto-gps-toggle><i class="fa-solid fa-satellite-dish"></i> Bật tự động 10 phút/lần</button></div>
        <small data-auto-gps-status>Chưa bật. Hãy cho phép trình duyệt truy cập vị trí và giữ trang này mở khi giao hàng.</small>
    </form>
</section>
</c:if>

<section class="admin-card order-filter-card"><form action="${ctx}/admin/order-tracking" method="get" class="admin-filter-form refined-filter-form"><input name="q" value="${param.q}" placeholder="Mã đơn, tên khách, điện thoại hoặc email"><select name="status"><option value="">Tất cả trạng thái</option><c:forTokens items="Đã xác nhận,Đang chuẩn bị,Đang giao,Đã giao,Hoàn thành" delims="," var="status"><option value="${status}" ${param.status == status ? 'selected' : ''}>${status}</option></c:forTokens></select><button class="btn btn-dark">Lọc đơn</button></form></section>

<div class="tracking-admin-layout">
    <aside class="admin-card tracking-order-list">
        <div class="admin-card-heading"><div><h2>Đơn được theo dõi</h2><p>${fn:length(orders)} đơn</p></div></div>
        <div class="tracking-list-scroll">
        <c:forEach var="o" items="${orders}"><a href="${ctx}/admin/order-tracking?id=${o.maDH}" class="tracking-order-card ${not empty selectedOrder && selectedOrder.maDH == o.maDH ? 'active' : ''}"><span><b>#${o.maDH}</b><em>${o.trangThai}</em></span><strong>${o.hoTenNhan}</strong><small>${o.diaChiNhan}</small><small>NV: <c:choose><c:when test="${empty o.tenNhanVien}">Chưa phân công</c:when><c:otherwise>${o.tenNhanVien} #${o.maNhanVien}</c:otherwise></c:choose></small></a></c:forEach>
        <c:if test="${empty orders}"><div class="empty-box">Chưa có đơn phù hợp.</div></c:if>
        </div>
    </aside>

    <main class="admin-card tracking-workspace">
        <c:choose><c:when test="${empty selectedOrder}"><div class="order-empty-state"><span>⌖</span><h2>Chọn một đơn hàng</h2><p>Bản đồ và công cụ cập nhật sẽ xuất hiện ở đây.</p></div></c:when><c:otherwise>
            <div class="admin-card-heading tracking-heading"><div><p class="eyebrow">Order #${selectedOrder.maDH}</p><h2>${selectedOrder.hoTenNhan}</h2><p>${selectedOrder.diaChiNhan}</p></div><span class="badge order-state-badge">${selectedOrder.trangThai}</span></div>

            <div class="delivery-map large-delivery-map" data-order-map data-order-id="${selectedOrder.maDH}" data-api-url="${ctx}/api/order-location"></div>
            <div class="map-control-row"><p class="map-status" data-map-status>Đang tải vị trí…</p><button type="button" class="map-refresh-button" data-map-refresh>Làm mới bản đồ</button></div>

            <c:if test="${role == 'DELIVERY' && (selectedOrder.trangThai == 'Đang chuẩn bị' || selectedOrder.trangThai == 'Đang giao')}"><form class="staff-location-form prominent-location-form" data-staff-location-form data-order-id="${selectedOrder.maDH}" data-api-url="${ctx}/api/order-location"><div><b>Gửi vị trí xe giao hàng</b><span>Trình duyệt sẽ xin quyền sử dụng GPS của thiết bị.</span></div><input name="note" placeholder="Ghi chú vị trí (không bắt buộc)"><button type="submit" class="btn btn-dark">Dùng vị trí hiện tại</button></form></c:if>

            <div class="tracking-detail-grid">
                <article><small>Nhân viên</small><b>${empty selectedOrder.tenNhanVien ? 'Chưa phân công' : selectedOrder.tenNhanVien}</b><span><c:if test="${not empty selectedOrder.maNhanVien}">#${selectedOrder.maNhanVien}</c:if></span></article>
                <article><small>Liên hệ khách</small><b>${selectedOrder.soDienThoaiNhan}</b><span>${selectedOrder.email}</span></article>
                <article><small>Thanh toán</small><b>${selectedOrder.phuongThucThanhToan}</b><span>${selectedOrder.trangThaiThanhToan}</span></article>
            </div>

            <div class="tracking-status-form tracking-forward-only">
                <label>Trạng thái hiện tại</label><input value="${selectedOrder.trangThai}" readonly>
                <c:choose>
                    <c:when test="${not empty nextOrderStatus}">
                        <form action="${ctx}/admin/order-tracking" method="post" data-single-submit onsubmit="return confirm('Xác nhận chuyển đơn #${selectedOrder.maDH} sang ${nextOrderStatus}?');">
                            <input type="hidden" name="action" value="orderStatus"><input type="hidden" name="maDH" value="${selectedOrder.maDH}"><input type="hidden" name="trangThai" value="${nextOrderStatus}">
                            <input name="deliveryNote" placeholder="Ghi chú giao hàng (không bắt buộc)">
                            <button class="btn btn-dark">Chuyển sang ${nextOrderStatus == 'Đã giao' ? 'Đã giao cho khách' : nextOrderStatus}</button>
                        </form>
                    </c:when>
                    <c:otherwise><div class="alert success">Không có bước tiếp theo. Muốn báo không giao được, hãy dùng mục “Hỗ trợ giao thất bại”.</div></c:otherwise>
                </c:choose>
            </div>

            <div class="tracking-history"><h3>Lịch sử vị trí</h3><c:forEach var="point" items="${trackingHistory}"><article><i></i><div><b>${point.tenNhanVien}</b><span>${point.ngayCapNhat}</span><p>${point.ghiChu}</p></div></article></c:forEach><c:if test="${empty trackingHistory}"><p class="muted">Chưa có điểm vị trí nào được gửi.</p></c:if></div>
        </c:otherwise></c:choose>
    </main>
</div>

<%@ include file="../common/admin-footer.jsp" %>
