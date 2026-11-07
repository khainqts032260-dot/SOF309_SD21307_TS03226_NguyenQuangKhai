<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Hỗ trợ giao hàng | Celine Closet" scope="request" />
<%@ include file="../common/admin-header.jsp" %>

<div class="admin-top refined-admin-top">
    <div>
        <p class="eyebrow">Delivery support</p>
        <h1>${role == 'ADMIN' ? 'Duyệt hồ sơ giao không thành công' : 'Hỗ trợ giao không thành công'}</h1>
        <p>${role == 'ADMIN' ? 'Kiểm tra đủ 3 ngày giao, tối thiểu 3 lần gọi và ảnh minh chứng trước khi duyệt hủy đơn.' : 'Mỗi ngày ghi nhận một lần giao, số lần gọi và ảnh chụp lịch sử cuộc gọi. Đủ 3 ngày mới gửi ADMIN duyệt.'}</p>
    </div>
</div>

<c:if test="${param.saved == 'attempt'}"><div class="alert success">Đã lưu minh chứng của ngày giao.</div></c:if>
<c:if test="${param.saved == 'submitted'}"><div class="alert success">Đã gửi hồ sơ cho ADMIN duyệt.</div></c:if>
<c:if test="${param.saved == 'approved'}"><div class="alert success">Đã duyệt. Đơn được hủy và khách đã nhận thông báo không giao được hàng.</div></c:if>
<c:if test="${param.saved == 'rejected'}"><div class="alert success">Đã từ chối hồ sơ và gửi ghi chú cho shipper.</div></c:if>
<c:if test="${param.error == '1'}"><div class="alert error">${sessionScope.deliverySupportError}</div><c:remove var="deliverySupportError" scope="session" /></c:if>

<div class="delivery-support-layout">
    <aside class="admin-card delivery-support-list">
        <c:choose>
            <c:when test="${role == 'DELIVERY'}">
                <div class="admin-card-heading"><div><h2>Đơn đang giao</h2><p>Chọn đơn cần ghi nhận hỗ trợ</p></div></div>
                <div class="delivery-support-scroll">
                    <c:forEach var="o" items="${deliveryOrders}">
                        <a class="delivery-support-card ${selectedOrderId == o.maDH ? 'active' : ''}" href="${ctx}/admin/delivery-support?orderId=${o.maDH}">
                            <span><b>#${o.maDH}</b><em>${o.trangThai}</em></span>
                            <strong>${o.hoTenNhan}</strong>
                            <small>${o.soDienThoaiNhan}</small>
                            <small>${o.soNgayDaGiao} ngày · ${o.tongLanGoi} lần gọi</small>
                            <c:if test="${not empty o.trangThaiYeuCau}"><i>${o.trangThaiYeuCau}</i></c:if>
                        </a>
                    </c:forEach>
                    <c:if test="${empty deliveryOrders}"><div class="empty-box">Không có đơn đang giao phù hợp.</div></c:if>
                </div>
            </c:when>
            <c:otherwise>
                <div class="admin-card-heading"><div><h2>Hồ sơ shipper</h2><p>${fn:length(failureRequests)} hồ sơ</p></div></div>
                <div class="delivery-support-scroll">
                    <c:forEach var="r" items="${failureRequests}">
                        <a class="delivery-support-card ${not empty selectedFailure && selectedFailure.maYCGTB == r.maYCGTB ? 'active' : ''}" href="${ctx}/admin/delivery-support?id=${r.maYCGTB}">
                            <span><b>Đơn #${r.maDH}</b><em>${r.trangThai}</em></span>
                            <strong>${r.hoTenNhan}</strong>
                            <small>${r.tenNhanVien}</small>
                            <small>${r.soNgayDaGiao} ngày · ${r.tongLanGoi} lần gọi</small>
                        </a>
                    </c:forEach>
                    <c:if test="${empty failureRequests}"><div class="empty-box">Chưa có hồ sơ giao thất bại.</div></c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </aside>

    <main class="admin-card delivery-support-workspace">
        <c:choose>
            <c:when test="${empty selectedOrderId && empty selectedFailure}">
                <div class="order-empty-state"><span>☎</span><h2>Chọn một hồ sơ</h2><p>Minh chứng và công cụ xử lý sẽ xuất hiện ở đây.</p></div>
            </c:when>
            <c:otherwise>
                <c:if test="${not empty selectedFailure}">
                    <div class="admin-card-heading">
                        <div><p class="eyebrow">Order #${selectedFailure.maDH}</p><h2>${selectedFailure.hoTenNhan}</h2><p>${selectedFailure.soDienThoaiNhan} · ${selectedFailure.diaChiNhan}</p></div>
                        <span class="badge order-state-badge">${selectedFailure.trangThai}</span>
                    </div>
                    <div class="delivery-proof-summary">
                        <article><small>Số ngày đã giao</small><b>${selectedFailure.soNgayDaGiao}/3</b></article>
                        <article><small>Tổng lần gọi</small><b>${selectedFailure.tongLanGoi}/3</b></article>
                        <article><small>Shipper</small><b>${selectedFailure.tenNhanVien}</b></article>
                        <article><small>Trạng thái đơn</small><b>${selectedFailure.trangThaiDon}</b></article>
                    </div>
                    <c:if test="${not empty selectedFailure.ghiChuAdmin}"><div class="alert ${selectedFailure.trangThai == 'REJECTED' ? 'error' : 'success'}">Ghi chú ADMIN: ${selectedFailure.ghiChuAdmin}</div></c:if>
                    <div class="delivery-proof-grid">
                        <c:forEach var="a" items="${failureAttempts}" varStatus="st">
                            <article class="delivery-proof-card">
                                <a href="${ctx}/${a.hinhAnh}" target="_blank"><img src="${ctx}/${a.hinhAnh}" alt="Minh chứng cuộc gọi ngày ${a.ngayGiao}"></a>
                                <div><b>Lần giao ${st.index + 1} · ${a.ngayGiao}</b><span>${a.soLanGoi} lần gọi</span><p>${a.ghiChu}</p></div>
                            </article>
                        </c:forEach>
                        <c:if test="${empty failureAttempts}"><div class="empty-box wide">Chưa có minh chứng.</div></c:if>
                    </div>
                </c:if>

                <c:if test="${role == 'DELIVERY'}">
                    <c:set var="requestState" value="${empty selectedFailure ? 'DRAFT' : selectedFailure.trangThai}" />
                    <c:if test="${requestState == 'DRAFT' || requestState == 'REJECTED'}">
                        <form action="${ctx}/admin/delivery-support" method="post" enctype="multipart/form-data" class="admin-action-box delivery-attempt-form">
                            <input type="hidden" name="action" value="addAttempt"><input type="hidden" name="maDH" value="${selectedOrderId}">
                            <h3>Thêm minh chứng một ngày giao</h3>
                            <p>Mỗi ngày chỉ lưu một minh chứng. Ảnh phải thấy lịch sử đã gọi khách.</p>
                            <label>Ngày giao<input type="date" name="ngayGiao" max="<%= java.time.LocalDate.now() %>" required></label>
                            <label>Số lần gọi trong ngày<input type="number" name="soLanGoi" min="1" max="20" value="1" required></label>
                            <label class="full-field">Ảnh lịch sử cuộc gọi<input type="file" name="callEvidence" accept="image/jpeg,image/png,image/webp,image/gif" required></label>
                            <label class="full-field">Ghi chú<textarea name="ghiChu" rows="3" placeholder="Khách không nghe máy, dời giờ giao, không có nhà..."></textarea></label>
                            <button class="btn btn-dark">Lưu minh chứng</button>
                        </form>
                        <c:if test="${not empty selectedFailure && selectedFailure.soNgayDaGiao >= 3 && selectedFailure.tongLanGoi >= 3}">
                            <form action="${ctx}/admin/delivery-support" method="post" class="admin-action-box delivery-submit-review">
                                <input type="hidden" name="action" value="submit"><input type="hidden" name="maDH" value="${selectedOrderId}">
                                <h3>Gửi ADMIN duyệt</h3><p>Hồ sơ đã đủ điều kiện 3 ngày và ít nhất 3 lần gọi.</p>
                                <textarea name="lyDo" rows="3" required placeholder="Tóm tắt lý do không giao được hàng"></textarea>
                                <button class="btn btn-danger">Gửi hồ sơ duyệt</button>
                            </form>
                        </c:if>
                    </c:if>
                    <c:if test="${requestState == 'PENDING'}"><div class="alert success">Hồ sơ đang chờ ADMIN duyệt. Bạn không thể sửa minh chứng lúc này.</div></c:if>
                    <c:if test="${requestState == 'APPROVED'}"><div class="alert success">ADMIN đã duyệt. Đơn đã được hủy vì không giao được hàng.</div></c:if>
                </c:if>

                <c:if test="${role == 'ADMIN' && not empty selectedFailure && selectedFailure.trangThai == 'PENDING'}">
                    <form action="${ctx}/admin/delivery-support" method="post" class="admin-action-box delivery-review-form">
                        <input type="hidden" name="maYCGTB" value="${selectedFailure.maYCGTB}">
                        <h3>Duyệt hồ sơ</h3><p>Chỉ duyệt khi ảnh minh chứng hợp lệ, đủ 3 ngày khác nhau và tổng ít nhất 3 lần gọi.</p>
                        <textarea name="ghiChuAdmin" rows="4" placeholder="Ghi chú duyệt hoặc lý do từ chối" required></textarea>
                        <div><button class="btn btn-dark" name="action" value="approve">Duyệt và hủy đơn</button><button class="btn btn-danger" name="action" value="reject">Từ chối hồ sơ</button></div>
                    </form>
                </c:if>
            </c:otherwise>
        </c:choose>
    </main>
</div>

<%@ include file="../common/admin-footer.jsp" %>
