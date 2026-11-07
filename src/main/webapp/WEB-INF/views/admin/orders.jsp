<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Quản lý đơn hàng | Celine Closet" scope="request" />
<c:set var="needsMap" value="true" scope="request" />
<%@ include file="../common/admin-header.jsp" %>

<c:if test="${not detailOnly}">
<div class="admin-top refined-admin-top">
    <div><p class="eyebrow">Order management</p><h1>Quản lý đơn hàng</h1>
        <p><c:choose><c:when test="${role == 'DELIVERY'}">Chỉ hiển thị những đơn được phân công giao cho mã nhân viên #${sessionScope.auth.maTK}.</c:when><c:when test="${role == 'STAFF'}">Chỉ hiển thị những đơn được phân công xử lý cho chính bạn (mã STAFF #${sessionScope.auth.maTK}); không xem chéo đơn của STAFF khác.</c:when><c:otherwise>ADMIN có thể xem toàn bộ đơn hàng, nhân viên xử lý và nhân viên giao hàng.</c:otherwise></c:choose></p>
    </div>
    <a class="btn btn-light" href="${ctx}/admin/order-tracking">Mở bản đồ giao hàng</a>
</div>

<c:if test="${param.saved == '1'}"><div class="alert success">Đã lưu thay đổi cho đơn hàng.</div></c:if>
<c:if test="${param.error == 'permission'}"><div class="alert error">Bạn không có quyền xử lý đơn hàng này.</div></c:if>
<c:if test="${param.error == 'action'}"><div class="alert error">${sessionScope.orderActionError}</div><c:remove var="orderActionError" scope="session" /></c:if>

<section class="admin-card order-filter-card">
    <form action="${ctx}/admin/orders" method="get" class="admin-filter-form refined-filter-form">
        <input name="q" value="${param.q}" placeholder="Mã đơn, khách hàng, điện thoại, email hoặc nhân viên">
        <select name="orderStatus">
            <option value="">Tất cả trạng thái</option>
            <c:forTokens items="Chờ xác nhận,Đã xác nhận,Đang chuẩn bị,Đang giao,Đã giao,Hoàn thành,Trả hàng,Bom hàng,Báo lỗi,Đã hủy" delims="," var="status">
                <option value="${status}" ${param.orderStatus == status ? 'selected' : ''}>${status}</option>
            </c:forTokens>
        </select>
        <select name="paymentStatus">
            <option value="">Tất cả thanh toán</option>
            <option value="PENDING" ${param.paymentStatus == 'PENDING' ? 'selected' : ''}>Chờ thanh toán</option>
            <option value="PAID" ${param.paymentStatus == 'PAID' ? 'selected' : ''}>Đã thanh toán</option>
            <option value="FAILED" ${param.paymentStatus == 'FAILED' ? 'selected' : ''}>Thanh toán lỗi</option>
            <option value="CANCELLED" ${param.paymentStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
        </select>
        <button class="btn btn-dark">Lọc đơn</button>
    </form>
</section>

<section class="admin-card admin-table-card">
    <div class="admin-card-heading"><div><h2>Danh sách đơn hàng</h2><p>${fn:length(orders)} đơn phù hợp</p></div></div>
    <div class="table-scroll">
        <table class="data-table refined-data-table">
            <thead><tr><th>Đơn hàng</th><th>Khách hàng</th><th>Nhân viên xử lý</th><th>Nhân viên giao hàng</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="o" items="${orders}">
                <tr class="${not empty selectedOrder && selectedOrder.maDH == o.maDH ? 'selected-row' : ''}">
                    <td><a class="order-number-link" href="${ctx}/admin/order-detail?id=${o.maDH}">#${o.maDH}</a><small>${o.ngayDat}</small></td>
                    <td><b>${o.hoTenNhan}</b><small>${o.soDienThoaiNhan}<br>${o.email}</small></td>
                    <td><c:choose><c:when test="${empty o.maNhanVienXuLy}"><span class="badge warning-badge">Chưa xác nhận</span></c:when><c:otherwise><b>${o.tenNhanVienXuLy}</b><small>Mã STAFF #${o.maNhanVienXuLy}</small></c:otherwise></c:choose></td>
                    <td><c:choose><c:when test="${empty o.maNhanVien}"><span class="badge warning-badge">Chưa có người giao</span></c:when><c:otherwise><b>${o.tenNhanVien}</b><small>Mã DELIVERY #${o.maNhanVien}</small></c:otherwise></c:choose></td>
                    <td><strong><fmt:formatNumber value="${o.tongTien}" type="number" groupingUsed="true" />đ</strong><c:if test="${o.tienGiam > 0}"><small>Đã giảm <fmt:formatNumber value="${o.tienGiam}" type="number" groupingUsed="true" />đ</small></c:if></td>
                    <td><span class="badge status-${fn:toLowerCase(o.trangThaiThanhToan)}">${o.trangThaiThanhToan}</span><small>${o.phuongThucThanhToan}</small></td>
                    <td><span class="badge order-state-badge">${o.trangThai}</span></td>
                    <td><a class="link-btn" href="${ctx}/admin/order-detail?id=${o.maDH}">Chi tiết →</a></td>
                </tr>
            </c:forEach>
            <c:if test="${empty orders}"><tr><td colspan="8" class="empty-cell">Không có đơn hàng phù hợp.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</section>
</c:if>

<c:if test="${not empty selectedOrder}">
<a class="btn btn-light" href="${ctx}/admin/orders">← Quay lại danh sách đơn</a>
<section class="admin-card order-admin-detail" id="order-detail">
    <div class="admin-card-heading order-detail-heading">
        <div><p class="eyebrow">Order #${selectedOrder.maDH}</p><h2>${selectedOrder.hoTenNhan}</h2><p>${selectedOrder.diaChiNhan}</p></div>
        <div class="order-detail-badges"><span class="badge order-state-badge">${selectedOrder.trangThai}</span><span class="badge status-${fn:toLowerCase(selectedOrder.trangThaiThanhToan)}">${selectedOrder.trangThaiThanhToan}</span></div>
    </div>

    <div class="order-admin-summary">
        <article><small>Nhân viên xử lý đơn</small><b>${empty selectedOrder.tenNhanVienXuLy ? 'Chưa có STAFF xác nhận' : selectedOrder.tenNhanVienXuLy}</b><span><c:if test="${not empty selectedOrder.maNhanVienXuLy}">Mã STAFF #${selectedOrder.maNhanVienXuLy} · ${selectedOrder.ngayXacNhan}</c:if></span></article>
        <article><small>Nhân viên giao hàng</small><b>${empty selectedOrder.tenNhanVien ? 'Chưa phân công' : selectedOrder.tenNhanVien}</b><span><c:if test="${not empty selectedOrder.maNhanVien}">Mã nhân viên #${selectedOrder.maNhanVien}</c:if></span></article>
        <article><small>Người nhận</small><b>${selectedOrder.hoTenNhan}</b><span>${selectedOrder.soDienThoaiNhan}</span></article>
        <article><small>Thanh toán</small><b>${selectedOrder.phuongThucThanhToan}</b><span><c:choose><c:when test="${selectedOrder.trangThaiThanhToan == 'PAID'}">Đã thanh toán</c:when><c:when test="${selectedOrder.trangThaiThanhToan == 'FAILED'}">Thanh toán lỗi</c:when><c:when test="${selectedOrder.trangThaiThanhToan == 'CANCELLED'}">Đã hủy thanh toán</c:when><c:otherwise>Chưa thanh toán / chờ kiểm tra</c:otherwise></c:choose></span></article>
        <article><small>Tổng thanh toán</small><b><fmt:formatNumber value="${selectedOrder.tongTien}" type="number" groupingUsed="true" />đ</b><span>${empty selectedOrder.voucherCode ? 'Không dùng voucher' : selectedOrder.voucherCode}</span></article>
    </div>

    <div class="admin-action-grid order-actions-grid">
        <div class="admin-action-box order-progress-lock">
            <h3>Cập nhật tiến trình</h3>
            <p>STAFF và DELIVERY chỉ được đi đúng một bước. Khi chuyển sang “Đang giao”, hệ thống tự phân công DELIVERY; shipper phải chuyển tiếp sang “Đã giao”, sau đó CUSTOMER mới xác nhận nhận hàng để hoàn tất.</p>
            <label>Trạng thái hiện tại<input value="${selectedOrder.trangThai}" readonly></label>
            <c:choose>
                <c:when test="${not empty nextOrderStatus}">
                    <form action="${ctx}/admin/orders" method="post" data-single-submit onsubmit="return confirm('Xác nhận chuyển đơn #${selectedOrder.maDH} sang ${nextOrderStatus}?');">
                        <input type="hidden" name="action" value="orderStatus"><input type="hidden" name="maDH" value="${selectedOrder.maDH}"><input type="hidden" name="trangThai" value="${nextOrderStatus}">
                        <button class="btn btn-dark">Chuyển sang ${nextOrderStatus == 'Đã giao' ? 'Đã giao cho khách' : nextOrderStatus}</button>
                    </form>
                </c:when>
                <c:otherwise><div class="alert success">Không còn bước tiến trình nào khả dụng cho vai trò hiện tại.</div></c:otherwise>
            </c:choose>
        </div>
        <c:if test="${role == 'ADMIN' || role == 'STAFF'}">
        <div class="admin-action-box payment-update-box">
            <h3>Kiểm tra và cập nhật thanh toán</h3>
            <c:choose>
                <c:when test="${selectedOrder.hasReturnRequest == 1}">
                    <p>Đơn đang có quy trình trả hàng/hoàn tiền #${selectedOrder.maYeuCauTraHang}. Thanh toán đã khóa để hai luồng không đè lên nhau.</p>
                    <a class="btn btn-light" href="${ctx}/admin/returns?id=${selectedOrder.maYeuCauTraHang}">Mở yêu cầu trả hàng</a>
                </c:when>
                <c:when test="${selectedOrder.phuongThucThanhToan == 'COD' && selectedOrder.trangThai == 'Hoàn thành'}">
                    <p>Tiền mặt được tự động chuyển sang PAID ngay khi shipper xác nhận giao thành công.</p>
                    <div class="alert success">Đã thanh toán tiền mặt khi nhận hàng.</div>
                </c:when>
                <c:when test="${selectedOrder.trangThai == 'Hoàn thành'}">
                    <p>Đơn đã hoàn tất nên không thể chuyển thanh toán sang lỗi hoặc hủy.</p>
                    <div class="alert success">Trạng thái thanh toán đã khóa: ${selectedOrder.trangThaiThanhToan}</div>
                </c:when>
                <c:otherwise>
                    <form action="${ctx}/admin/orders" method="post" data-single-submit>
                        <input type="hidden" name="action" value="payment"><input type="hidden" name="maDH" value="${selectedOrder.maDH}">
                        <p>Đối chiếu tiền mặt, QR hoặc chuyển khoản rồi cập nhật trạng thái thực tế.</p>
                        <label>Trạng thái thanh toán hiện tại</label><select name="paymentStatus" required><option value="PENDING" ${selectedOrder.trangThaiThanhToan == 'PENDING' ? 'selected' : ''}>Chưa thanh toán / chờ kiểm tra</option><option value="PAID" ${selectedOrder.trangThaiThanhToan == 'PAID' ? 'selected' : ''}>Đã thanh toán</option><option value="FAILED" ${selectedOrder.trangThaiThanhToan == 'FAILED' ? 'selected' : ''}>Thanh toán lỗi</option><option value="CANCELLED" ${selectedOrder.trangThaiThanhToan == 'CANCELLED' ? 'selected' : ''}>Đã hủy thanh toán</option></select>
                        <button class="btn btn-dark">Cập nhật thanh toán</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
        </c:if>
        <c:if test="${role == 'ADMIN'}">
        <form action="${ctx}/admin/orders" method="post" class="admin-action-box admin-status-override" data-single-submit onsubmit="return confirm('Chỉ dùng khi nhân viên cập nhật nhầm. Xác nhận sửa lùi trạng thái?');">
            <input type="hidden" name="action" value="adminOverride"><input type="hidden" name="maDH" value="${selectedOrder.maDH}">
            <h3>ADMIN sửa lùi trạng thái</h3><p>Dùng khi nhân viên bấm nhầm. Không áp dụng cho đơn hoàn thành, đã hủy hoặc đang trả hàng.</p>
            <label>Đưa đơn về<select name="trangThaiSuaLui" required><option value="">Chọn trạng thái trước</option><c:if test="${selectedOrder.trangThai == 'Đã xác nhận' || selectedOrder.trangThai == 'Đang chuẩn bị' || selectedOrder.trangThai == 'Đang giao' || selectedOrder.trangThai == 'Đã giao'}"><option value="Chờ xác nhận">Chờ xác nhận</option></c:if><c:if test="${selectedOrder.trangThai == 'Đang chuẩn bị' || selectedOrder.trangThai == 'Đang giao' || selectedOrder.trangThai == 'Đã giao'}"><option value="Đã xác nhận">Đã xác nhận</option></c:if><c:if test="${selectedOrder.trangThai == 'Đang giao' || selectedOrder.trangThai == 'Đã giao'}"><option value="Đang chuẩn bị">Đang chuẩn bị</option></c:if><c:if test="${selectedOrder.trangThai == 'Đã giao'}"><option value="Đang giao">Đang giao</option></c:if></select></label>
            <label>Lý do sửa<textarea name="overrideReason" maxlength="500" placeholder="Ví dụ: STAFF bấm nhầm sang Đang giao" required></textarea></label>
            <button class="btn btn-light">Sửa lùi và ghi nhật ký</button>
        </form>
        </c:if>
    </div>

    <c:if test="${selectedOrder.trangThai == 'Đang giao' || selectedOrder.trangThai == 'Đã giao' || selectedOrder.trangThai == 'Hoàn thành'}">
    <section class="tracking-map-section admin-tracking-map">
        <div class="section-head"><div><p class="eyebrow">Live delivery</p><h2>Vị trí giao hàng</h2></div><button type="button" class="map-refresh-button" data-map-refresh>Làm mới</button></div>
        <div class="delivery-map" data-order-map data-order-id="${selectedOrder.maDH}" data-api-url="${ctx}/api/order-location"></div>
        <p class="map-status" data-map-status>Đang tải bản đồ…</p>
        <c:if test="${role == 'DELIVERY'}">
            <form class="staff-location-form" data-staff-location-form data-order-id="${selectedOrder.maDH}" data-api-url="${ctx}/api/order-location">
                <input name="note" placeholder="Ghi chú vị trí, ví dụ: Đang ở cổng khu phố"><button type="submit" class="btn btn-dark">Gửi vị trí hiện tại</button>
            </form>
        </c:if>
    </section>
    </c:if>

    <div class="order-detail-columns">
        <div class="order-products-clean"><h3>Sản phẩm trong đơn</h3><c:forEach var="i" items="${items}"><c:set var="adminOrderFallback" value="${ctx}/assets/images/fashion/card-01.jpg" /><article><c:choose><c:when test="${not empty i.hinhAnh}"><img class="js-fashion-image" src="${ctx}/${i.hinhAnh}" data-fallback="${adminOrderFallback}" alt="${i.tenSP}"></c:when><c:otherwise><img class="js-fashion-image" src="${adminOrderFallback}" alt="${i.tenSP}"></c:otherwise></c:choose><div><b>${i.tenSP}</b><span>${i.tenDM} · SL ${i.soLuong}</span></div><strong><fmt:formatNumber value="${i.thanhTien}" type="number" groupingUsed="true" />đ</strong></article></c:forEach></div>
        <div class="order-note-panel"><h3>Thông tin giao nhận</h3><p><b>Email:</b> ${selectedOrder.email}</p><p><b>Điện thoại:</b> ${selectedOrder.soDienThoaiNhan}</p><p><b>Địa chỉ:</b> ${selectedOrder.diaChiNhan}</p><p><b>Ghi chú:</b> ${empty selectedOrder.ghiChu ? 'Không có' : selectedOrder.ghiChu}</p>
            <c:if test="${role == 'ADMIN'}"><div class="order-incident-actions"><a class="btn btn-light" href="${ctx}/admin/delivery-support"><i class="fa-solid fa-file-circle-check"></i> Xem hồ sơ giao thất bại</a><a class="btn btn-light" href="${ctx}/admin/returns"><i class="fa-solid fa-box-rotate-left"></i> Quản lý trả hàng & hoàn tiền</a></div></c:if>
            <c:if test="${role == 'ADMIN' && selectedOrder.trangThai != 'Hoàn thành' && selectedOrder.hasReturnRequest != 1}"><div class="danger-actions"><form action="${ctx}/admin/orders" method="post" data-single-submit><input type="hidden" name="action" value="error"><input type="hidden" name="maDH" value="${selectedOrder.maDH}"><textarea name="errorNote" placeholder="Nội dung báo lỗi" required></textarea><button class="btn btn-light">Báo lỗi</button></form><form action="${ctx}/admin/orders" method="post" data-single-submit><input type="hidden" name="action" value="cancel"><input type="hidden" name="maDH" value="${selectedOrder.maDH}"><textarea name="cancelReason" placeholder="Lý do hủy đơn" required></textarea><button class="btn btn-danger">Hủy đơn</button></form></div></c:if>
        </div>
    </div>
</section>
</c:if>

<%@ include file="../common/admin-footer.jsp" %>
