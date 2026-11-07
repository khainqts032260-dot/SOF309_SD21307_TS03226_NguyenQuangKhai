<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../common/admin-header.jsp" %>

<div class="admin-top refined-admin-top">
    <div><p class="admin-eyebrow">Responsibility log</p><h1>Nhật ký nhân viên</h1><p>Mọi lần nhận đơn, cập nhật trạng thái, gửi vị trí và hỗ trợ khách đều được lưu để đối chiếu khi có vấn đề.</p></div>
</div>

<section class="admin-card order-filter-card">
    <form action="${ctx}/admin/staff-activity" method="get" class="admin-filter-form refined-filter-form activity-filter-form">
        <select name="staffId"><option value="">Tất cả nhân viên</option><c:forEach var="s" items="${staffList}"><option value="${s.maTK}" ${param.staffId == s.maTK ? 'selected' : ''}>NV${s.maTK} · ${s.hoTen}</option></c:forEach></select>
        <input type="text" name="orderId" value="${fn:escapeXml(param.orderId)}" placeholder="Mã đơn: 45, #45 hoặc DH00045" inputmode="numeric">
        <button class="btn btn-dark">Lọc nhật ký</button>
        <a class="btn" href="${ctx}/admin/staff-activity">Xóa lọc</a>
    </form>
    <c:if test="${not empty filterError}"><div class="alert error">${filterError}</div></c:if>
</section>

<section class="admin-card">
    <div class="admin-card-heading"><div><h2>Công việc đã ghi nhận</h2><p>Dữ liệu được sắp xếp từ mới đến cũ.</p></div><span>${fn:length(activities)} hoạt động</span></div>
    <div class="table-scroll">
        <table class="data-table refined-data-table staff-activity-table">
            <thead><tr><th>Thời gian</th><th>Nhân viên chịu trách nhiệm</th><th>Đơn hàng</th><th>Hành động</th><th>Nội dung</th></tr></thead>
            <tbody>
                <c:forEach var="a" items="${activities}">
                    <tr><td>${a.ngayTao}</td><td><strong>${a.tenNhanVien}</strong><small>NV${a.maNhanVien} · ${a.emailNhanVien}</small></td><td><c:choose><c:when test="${not empty a.maDH}"><a class="order-number-link" href="${ctx}/admin/order-detail?id=${a.maDH}">#${a.maDH}</a></c:when><c:otherwise>—</c:otherwise></c:choose></td><td>${a.hanhDong}</td><td>${a.noiDung}</td></tr>
                </c:forEach>
                <c:if test="${empty activities}"><tr><td colspan="5" class="empty-cell">Chưa có hoạt động phù hợp.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</section>

<%@ include file="../common/admin-footer.jsp" %>
