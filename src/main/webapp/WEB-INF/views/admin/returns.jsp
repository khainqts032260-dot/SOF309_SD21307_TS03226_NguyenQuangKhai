<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Trả hàng & hoàn tiền | Celine Closet" scope="request" />
<%@ include file="../common/admin-header.jsp" %>

<div class="admin-top refined-admin-top return-admin-top">
    <div><p class="eyebrow">RETURN &amp; REFUND</p><h1>Trả hàng và hoàn tiền</h1>
        <p>Quy trình gồm bốn mốc: shipper nhận hàng, hàng đến bưu điện, hoàn hàng thành công và trả tiền qua tài khoản ngân hàng.</p></div>
</div>
<c:if test="${param.saved == '1'}"><div class="alert success">Đã cập nhật tiến trình trả hàng.</div></c:if>
<c:if test="${param.error == '1'}"><div class="alert error">${sessionScope.adminReturnError}</div><c:remove var="adminReturnError" scope="session" /></c:if>

<div class="return-admin-layout">
    <aside class="admin-card return-request-list">
        <div class="admin-card-heading"><div><h2>Yêu cầu trả hàng</h2><p>${fn:length(returnRequests)} yêu cầu</p></div></div>
        <div class="return-list-scroll">
            <c:forEach var="r" items="${returnRequests}">
                <a href="${ctx}/admin/returns?id=${r.maYCTH}" class="return-request-card ${not empty selectedReturn && selectedReturn.maYCTH == r.maYCTH ? 'active' : ''}">
                    <span><b>#${r.maDH}</b><em>${r.trangThai}</em></span>
                    <strong>${r.tenKhachHang}</strong>
                    <small>${r.lyDo}</small>
                    <small>Shipper: ${empty r.tenShipper ? 'Chưa phân công' : r.tenShipper}</small>
                </a>
            </c:forEach>
            <c:if test="${empty returnRequests}"><div class="empty-box">Chưa có yêu cầu trả hàng.</div></c:if>
        </div>
    </aside>

    <main class="admin-card return-workspace">
        <c:choose>
            <c:when test="${empty selectedReturn}">
                <div class="order-empty-state"><span>↩</span><h2>Chọn một yêu cầu</h2><p>Thông tin hàng hoàn và công cụ cập nhật sẽ xuất hiện tại đây.</p></div>
            </c:when>
            <c:otherwise>
                <div class="admin-card-heading return-detail-heading">
                    <div><p class="eyebrow">ĐƠN #${selectedReturn.maDH}</p><h2>${selectedReturn.tenKhachHang}</h2><p>${selectedReturn.diaChiNhan}</p></div>
                    <span class="badge return-status-badge">${selectedReturn.trangThai}</span>
                </div>

                <div class="return-progress-admin">
                    <c:forTokens items="Shipper nhận hàng,Hàng đến bưu điện,Hoàn hàng thành công,Trả tiền" delims="," var="label" varStatus="st">
                        <div class="${selectedReturn.buocTraHang >= st.index + 1 ? 'done' : ''}"><i>${st.index + 1}</i><span>${label}</span></div>
                    </c:forTokens>
                </div>
                <c:if test="${selectedReturn.trangThai == 'REFUND_PROCESSING'}"><div class="alert warning"><b>Đang xử lý trả tiền</b><span>Chuyển khoản hoàn tiền thường mất khoảng 3–4 ngày làm việc. Dự kiến hoàn tất trước ${selectedReturn.duKienHoanTien}.</span></div></c:if>
                <c:if test="${selectedReturn.trangThai == 'REFUNDED'}"><div class="alert success"><b>Đã hoàn tiền</b><span>Tiền đã được chuyển vào tài khoản khách đăng ký.</span></div></c:if>
                <c:if test="${selectedReturn.trangThai == 'REJECTED'}"><div class="alert error"><b>Yêu cầu bị từ chối</b><span>${selectedReturn.ghiChuXuLy}</span></div></c:if>

                <div class="return-info-grid">
                    <article><small>Khách hàng</small><b>${selectedReturn.tenKhachHang}</b><span>${selectedReturn.email} · ${selectedReturn.soDienThoaiNhan}</span></article>
                    <article><small>Số tiền hoàn</small><b><fmt:formatNumber value="${selectedReturn.soTienHoan}" groupingUsed="true" />đ</b><span>Hoàn qua tài khoản ngân hàng</span></article>
                    <article><small>Shipper nhận hàng</small><b>${empty selectedReturn.tenShipper ? 'Chưa phân công' : selectedReturn.tenShipper}</b><span>${selectedReturn.emailShipper}</span></article>
                    <article><small>Ngày yêu cầu</small><b>${selectedReturn.ngayYeuCau}</b><span>Khách sửa tài khoản trong 2 ngày đầu</span></article>
                </div>

                <section class="return-reason-box"><small>Lý do trả hàng</small><p>${selectedReturn.lyDo}</p></section>

                <c:if test="${not empty returnImages}">
                    <section class="return-evidence-section"><h3>Ảnh sản phẩm khách gửi</h3><div class="return-image-grid">
                        <c:forEach var="image" items="${returnImages}"><a href="${ctx}/${image.duongDan}" target="_blank" rel="noopener"><img src="${ctx}/${image.duongDan}" alt="Ảnh minh chứng trả hàng"></a></c:forEach>
                    </div></section>
                </c:if>

                <c:if test="${role == 'ADMIN'}">
                    <section class="return-bank-card">
                        <div><small>Tài khoản nhận tiền</small><h3>${selectedReturn.nganHang}</h3></div>
                        <p><span>Số tài khoản</span><b>${selectedReturn.soTaiKhoan}</b></p>
                        <p><span>Chủ tài khoản</span><b>${selectedReturn.chuTaiKhoan}</b></p>
                        <p><span>Chỉnh sửa gần nhất</span><b>${empty selectedReturn.ngaySuaNganHang ? 'Chưa chỉnh sửa' : selectedReturn.ngaySuaNganHang}</b></p>
                    </section>
                    <c:if test="${selectedReturn.trangThai == 'REQUESTED' || selectedReturn.trangThai == 'SHIPPER_RECEIVED'}">
                        <form class="return-assign-form" method="post" action="${ctx}/admin/returns">
                            <input type="hidden" name="action" value="assign"><input type="hidden" name="maYCTH" value="${selectedReturn.maYCTH}">
                            <label>Phân công shipper<select name="deliveryId" required><option value="">Chọn shipper</option><c:forEach var="d" items="${deliveryAccounts}"><option value="${d.maTK}" ${selectedReturn.maNhanVien == d.maTK ? 'selected' : ''}>${d.hoTen} · ${d.email}</option></c:forEach></select></label>
                            <button class="btn btn-outline">Lưu phân công</button>
                        </form>
                    </c:if>
                </c:if>

                <section class="return-action-panel">
                    <h3>Cập nhật bước tiếp theo</h3>
                    <form method="post" action="${ctx}/admin/returns" class="return-action-form">
                        <input type="hidden" name="maYCTH" value="${selectedReturn.maYCTH}">
                        <textarea name="ghiChu" rows="2" placeholder="Ghi chú xử lý, mã vận đơn hoặc nội dung chuyển khoản..."></textarea>
                        <div class="return-action-buttons">
                            <c:if test="${(role == 'DELIVERY' || role == 'ADMIN') && selectedReturn.trangThai == 'REQUESTED'}"><button class="btn btn-dark" name="action" value="receive">1. Xác nhận shipper đã nhận hàng</button></c:if>
                            <c:if test="${(role == 'DELIVERY' || role == 'ADMIN') && selectedReturn.trangThai == 'SHIPPER_RECEIVED'}"><button class="btn btn-dark" name="action" value="postOffice">2. Xác nhận hàng đã đến bưu điện</button></c:if>
                            <c:if test="${role == 'ADMIN' && selectedReturn.trangThai == 'AT_POST_OFFICE'}"><button class="btn btn-dark" name="action" value="completeReturn">3. Xác nhận hoàn hàng thành công</button></c:if>
                            <c:if test="${role == 'ADMIN' && selectedReturn.trangThai == 'RETURN_COMPLETED'}"><button class="btn btn-dark" name="action" value="startRefund">4. Bắt đầu xử lý trả tiền</button></c:if>
                            <c:if test="${role == 'ADMIN' && selectedReturn.trangThai == 'REFUND_PROCESSING'}"><button class="btn btn-dark" name="action" value="finishRefund" onclick="return confirm('Xác nhận đã chuyển tiền vào đúng tài khoản khách?');">Xác nhận đã trả tiền</button></c:if>
                            <c:if test="${role == 'ADMIN' && (selectedReturn.trangThai == 'REQUESTED' || selectedReturn.trangThai == 'SHIPPER_RECEIVED')}"><button class="btn btn-danger" name="action" value="reject" onclick="return confirm('Từ chối yêu cầu trả hàng này?');">Từ chối yêu cầu</button></c:if>
                        </div>
                    </form>
                    <c:if test="${selectedReturn.trangThai == 'REFUNDED' || selectedReturn.trangThai == 'REJECTED'}"><p class="muted">Yêu cầu đã kết thúc, không còn bước cập nhật tiếp theo.</p></c:if>
                </section>
            </c:otherwise>
        </c:choose>
    </main>
</div>

<%@ include file="../common/admin-footer.jsp" %>
