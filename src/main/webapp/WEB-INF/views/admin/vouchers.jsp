<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Voucher | Celine Closet" scope="request" />
<%@ include file="../common/admin-header.jsp" %>

<div class="admin-top refined-admin-top">
    <div>
        <p class="admin-eyebrow">Khuyến mãi</p>
        <h1>Quản lý voucher</h1>
        <p>Tạo mã giảm theo phần trăm hoặc số tiền, đặt thời hạn, số lượng và giá trị đơn tối thiểu.</p>
    </div>
</div>

<c:if test="${param.error == 'code'}"><div class="alert error">Mã voucher chỉ gồm chữ, số, dấu gạch ngang hoặc gạch dưới và dài từ 3 ký tự.</div></c:if>
<c:if test="${param.error == 'name'}"><div class="alert error">Vui lòng nhập tên chương trình voucher.</div></c:if>
<c:if test="${param.error == 'duplicate'}"><div class="alert error">Mã voucher này đã tồn tại.</div></c:if>
<c:if test="${param.error == 'value'}"><div class="alert error">Mức giảm phải lớn hơn 0; giảm theo phần trăm không được vượt quá 100%.</div></c:if>
<c:if test="${param.error == 'number'}"><div class="alert error">Số lượng và điều kiện đơn hàng không được là số âm.</div></c:if>
<c:if test="${param.error == 'date'}"><div class="alert error">Thời gian kết thúc phải sau thời gian bắt đầu.</div></c:if>
<c:if test="${param.success == 'add'}"><div class="alert success">Đã tạo voucher mới.</div></c:if>
<c:if test="${param.success == 'edit'}"><div class="alert success">Đã cập nhật voucher.</div></c:if>
<c:if test="${param.success == 'status'}"><div class="alert success">Đã đổi trạng thái voucher.</div></c:if>

<section class="admin-card voucher-editor-card">
    <div class="admin-card-heading">
        <div>
            <h2>${empty editVoucher ? 'Tạo voucher mới' : 'Chỉnh sửa voucher'}</h2>
            <p>Nhập số lượng bằng 0 nếu muốn voucher không giới hạn lượt sử dụng.</p>
        </div>
    </div>

    <form action="${ctx}/admin/vouchers" method="post" class="voucher-admin-form" data-voucher-admin-form>
        <input type="hidden" name="maVoucher" value="${editVoucher.maVoucher}">

        <label>Mã voucher
            <input name="maCode" maxlength="40" value="${editVoucher.maCode}" placeholder="VD: CELINE20" required>
        </label>
        <label>Tên chương trình
            <input name="tenVoucher" maxlength="150" value="${editVoucher.tenVoucher}" placeholder="VD: Giảm 20% cuối tuần" required>
        </label>

        <label>Hình thức giảm
            <select name="loaiGiam" data-voucher-type>
                <option value="PERCENT" ${(empty editVoucher or editVoucher.loaiGiam == 'PERCENT') ? 'selected' : ''}>Giảm theo phần trăm (%)</option>
                <option value="FIXED" ${editVoucher.loaiGiam == 'FIXED' ? 'selected' : ''}>Giảm số tiền cố định (đ)</option>
            </select>
        </label>
        <label>Mức giảm
            <input type="number" name="giaTri" min="1" step="1" value="${editVoucher.giaTri}" placeholder="VD: 15 hoặc 50000" required>
        </label>

        <label data-max-discount-field>Giảm tối đa khi dùng phần trăm
            <input type="number" name="giamToiDa" min="0" step="1000" value="${editVoucher.giamToiDa}" placeholder="Để trống nếu không giới hạn">
        </label>
        <label>Đơn hàng tối thiểu
            <input type="number" name="donToiThieu" min="0" step="1000" value="${empty editVoucher ? 0 : editVoucher.donToiThieu}" placeholder="VD: 500000" required>
        </label>

        <label>Thời gian bắt đầu
            <input type="datetime-local" name="ngayBatDau" value="${editVoucher.ngayBatDauInput}" required>
        </label>
        <label>Thời gian kết thúc
            <input type="datetime-local" name="ngayKetThuc" value="${editVoucher.ngayKetThucInput}" required>
        </label>

        <label>Số lượng voucher
            <input type="number" name="soLuot" min="0" step="1" value="${empty editVoucher.soLuot ? 0 : editVoucher.soLuot}" required>
            <small>Nhập 0 để không giới hạn số lượt.</small>
        </label>
        <label>Trạng thái
            <select name="trangThai">
                <option value="1" ${(empty editVoucher or editVoucher.trangThai == 1) ? 'selected' : ''}>Đang bật</option>
                <option value="0" ${editVoucher.trangThai == 0 ? 'selected' : ''}>Tạm ngưng</option>
            </select>
        </label>

        <div class="form-actions voucher-form-actions">
            <button class="btn btn-dark" type="submit">${empty editVoucher ? 'Tạo voucher' : 'Lưu thay đổi'}</button>
            <c:if test="${not empty editVoucher}"><a class="btn" href="${ctx}/admin/vouchers">Hủy chỉnh sửa</a></c:if>
        </div>
    </form>
</section>

<section class="admin-card">
    <div class="admin-card-heading">
        <div><h2>Danh sách voucher</h2><p>${fn:length(vouchers)} voucher đang được lưu trong hệ thống.</p></div>
        <form class="search-form" action="${ctx}/admin/vouchers" method="get">
            <input name="q" value="${param.q}" placeholder="Tìm theo mã hoặc tên">
            <button class="btn btn-dark">Tìm</button>
        </form>
    </div>

    <div class="table-scroll">
        <table class="data-table voucher-admin-table">
            <thead><tr><th>Mã</th><th>Ưu đãi</th><th>Điều kiện</th><th>Thời hạn</th><th>Lượt dùng</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
            <c:forEach var="v" items="${vouchers}">
                <tr>
                    <td><strong class="voucher-code-cell">${v.maCode}</strong><small>${v.tenVoucher}</small></td>
                    <td>
                        <c:choose>
                            <c:when test="${v.loaiGiam == 'PERCENT'}"><b><fmt:formatNumber value="${v.giaTri}" maxFractionDigits="0"/>%</b><c:if test="${not empty v.giamToiDa}"><small>Tối đa <fmt:formatNumber value="${v.giamToiDa}" type="number" groupingUsed="true"/>đ</small></c:if></c:when>
                            <c:otherwise><b>-<fmt:formatNumber value="${v.giaTri}" type="number" groupingUsed="true"/>đ</b></c:otherwise>
                        </c:choose>
                    </td>
                    <td><span>Đơn từ</span><b><fmt:formatNumber value="${v.donToiThieu}" type="number" groupingUsed="true"/>đ</b></td>
                    <td><small>${v.ngayBatDauText}</small><span>đến</span><small>${empty v.ngayKetThucText ? 'Không giới hạn' : v.ngayKetThucText}</small></td>
                    <td><b>${v.daDung}</b> / <span>${empty v.soLuot ? '∞' : v.soLuot}</span></td>
                    <td><span class="status-pill ${v.tinhTrang == 'Đang hoạt động' ? 'active' : 'inactive'}">${v.tinhTrang}</span></td>
                    <td class="table-actions">
                        <a class="link-btn" href="${ctx}/admin/vouchers?edit=${v.maVoucher}">Sửa</a>
                        <form action="${ctx}/admin/vouchers" method="post" class="inline-action-form">
                            <input type="hidden" name="action" value="status">
                            <input type="hidden" name="id" value="${v.maVoucher}">
                            <input type="hidden" name="status" value="${v.trangThai == 1 ? 0 : 1}">
                            <button class="link-btn" type="submit">${v.trangThai == 1 ? 'Tạm ngưng' : 'Bật lại'}</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty vouchers}"><tr><td colspan="7">Chưa có voucher phù hợp.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</section>

<%@ include file="../common/admin-footer.jsp" %>
