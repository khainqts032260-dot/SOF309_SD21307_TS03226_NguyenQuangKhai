<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="../common/admin-header.jsp" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<section class="admin-page-head inventory-page-head">
    <div><span class="eyebrow">WAREHOUSE RECEIPTS</span><h1>Lịch sử nhập hàng</h1><p>Tìm nhanh theo sản phẩm, mã biên lai, nhà cung cấp, nhân viên và khoảng ngày.</p></div>
    <a class="inventory-history-jump" href="${ctx}/admin/inventory"><i class="fa-solid fa-arrow-left"></i> Trở về kho hàng</a>
</section>

<form class="inventory-history-filter" method="get" action="${ctx}/admin/inventory-history">
    <label><span>Từ khóa</span><input type="search" name="q" value="${param.q}" placeholder="Tên/mã SP, biên lai, nhà cung cấp..."></label>
    <label><span>Nhân viên nhập</span><select name="staffId"><option value="">Tất cả nhân viên</option><c:forEach var="staff" items="${inventoryStaff}"><option value="${staff.maTK}" ${param.staffId == staff.maTK ? 'selected' : ''}>${staff.hoTen} (#${staff.maTK})</option></c:forEach></select></label>
    <label><span>Từ ngày</span><input type="date" name="from" value="${param.from}"></label>
    <label><span>Đến ngày</span><input type="date" name="to" value="${param.to}"></label>
    <div class="inventory-history-filter-actions"><button class="btn btn-dark" type="submit"><i class="fa-solid fa-filter"></i> Lọc lịch sử</button><a class="btn btn-light" href="${ctx}/admin/inventory-history">Xóa lọc</a></div>
</form>

<section class="admin-panel inventory-history-page-panel">
    <div class="admin-panel-title"><div><span>BIÊN LAI NHẬP KHO</span><h2>${history.size()} lần nhập gần nhất</h2><p>Hiển thị tối đa 500 bản ghi phù hợp với bộ lọc.</p></div></div>
    <div class="admin-table-wrap">
        <table class="admin-table inventory-history-table">
            <thead><tr><th>Thời gian</th><th>Sản phẩm</th><th>Số lượng</th><th>Tồn kho</th><th>Biên lai / nguồn hàng</th><th>Nhân viên</th><th>Ghi chú</th></tr></thead>
            <tbody>
            <c:forEach var="h" items="${history}">
                <tr>
                    <td><strong><fmt:formatDate value="${h.ngayNhap}" pattern="dd/MM/yyyy"/></strong><small><fmt:formatDate value="${h.ngayNhap}" pattern="HH:mm:ss"/></small></td>
                    <td><strong>${h.tenSP}</strong><small>Mã SP #${h.maSP} · Phiếu #${h.maNhapKho}</small></td>
                    <td><span class="inventory-history-plus">+${h.soLuongNhap}</span></td>
                    <td><span>${h.tonTruoc}</span> <i class="fa-solid fa-arrow-right"></i> <strong>${h.tonSau}</strong></td>
                    <td><strong>${empty h.soBienLai ? 'Chưa ghi biên lai' : h.soBienLai}</strong><small>${empty h.nhaCungCap ? 'Chưa ghi nhà cung cấp' : h.nhaCungCap}<c:if test="${not empty h.xuatXu}"> · ${h.xuatXu}</c:if></small></td>
                    <td><strong>${h.tenNhanVien}</strong><small>${h.vaiTro} · #${h.maNhanVien}</small></td>
                    <td>${empty h.ghiChu ? '—' : h.ghiChu}</td>
                </tr>
            </c:forEach>
            <c:if test="${empty history}"><tr><td colspan="7" class="admin-empty">Không có lịch sử nhập hàng phù hợp với bộ lọc.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</section>

<jsp:include page="../common/admin-footer.jsp" />
