<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/admin-header.jsp" %>
<div class="admin-top"><h1>Quản lý khách hàng</h1><p>Thêm khách hàng mới, tìm kiếm, khóa/mở tài khoản và xem lịch sử mua hàng.</p></div>

<c:if test="${param.error == 'duplicateEmail'}">
    <div class="alert error">Email này đã tồn tại trong hệ thống. Vui lòng dùng email khác.</div>
</c:if>
<c:if test="${param.error == 'emailFormat'}">
    <div class="alert error">Email không đúng định dạng, ví dụ ten@gmail.com.</div>
</c:if>
<c:if test="${param.error == 'passwordRule'}">
    <div class="alert error">Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.</div>
</c:if>
<c:if test="${param.success == 'add'}">
    <div class="alert success">Đã thêm khách hàng thành công.</div>
</c:if>
<c:if test="${param.success == 'status'}">
    <div class="alert success">Đã cập nhật trạng thái khách hàng.</div>
</c:if>

<section class="admin-card form-card">
<h2>Thêm khách hàng</h2>
<form action="${ctx}/admin/customers" method="post" class="admin-form grid-form">
    <input name="hoTen" placeholder="Họ tên khách hàng" required>
    <input type="email" name="email" placeholder="Email" required
           pattern="[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}">
    <input name="soDienThoai" placeholder="Số điện thoại">
    <input type="password" name="password" minlength="8" maxlength="32" required placeholder="Mật khẩu">
    <small>Mật khẩu cần chữ hoa, chữ thường, số và ký tự đặc biệt.</small>
    <button class="btn btn-dark">Thêm khách hàng</button>
</form>
</section>

<section class="admin-card">
<div class="admin-toolbar">
    <h2>Danh sách khách hàng</h2>
    <form action="${ctx}/admin/customers" method="get" class="search-form">
        <input name="q" value="${param.q}" placeholder="Tìm tên, email, SĐT">
        <button class="btn btn-dark">Tìm</button>
    </form>
</div>
<table class="data-table">
<thead><tr><th>ID</th><th>Khách hàng</th><th>Email</th><th>SĐT</th><th>Đơn hàng</th><th>Chi tiêu</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
<tbody>
<c:forEach var="c" items="${customers}">
<tr>
    <td>#${c.maTK}</td>
    <td><a href="${ctx}/admin/customers?id=${c.maTK}">${c.hoTen}</a></td>
    <td>${c.email}</td>
    <td>${c.soDienThoai}</td>
    <td>${c.soDonHang}</td>
    <td><fmt:formatNumber value="${c.tongChiTieu}" type="number" groupingUsed="true" />đ</td>
    <td><span class="badge">${c.trangThai == 1 ? 'Hoạt động' : 'Khóa'}</span></td>
    <td>
        <form action="${ctx}/admin/customers" method="post" class="inline-form">
            <input type="hidden" name="action" value="status">
            <input type="hidden" name="id" value="${c.maTK}">
            <input type="hidden" name="status" value="${c.trangThai == 1 ? 0 : 1}">
            <button class="link-btn">${c.trangThai == 1 ? 'Khóa' : 'Mở'}</button>
        </form>
    </td>
</tr>
</c:forEach>
</tbody>
</table>
</section>

<c:if test="${not empty customer}">
<section class="admin-card">
<h2>Hồ sơ khách hàng #${customer.maTK}</h2>
<div class="detail-grid">
    <p><b>Họ tên:</b> ${customer.hoTen}</p>
    <p><b>Email:</b> ${customer.email}</p>
    <p><b>SĐT:</b> ${customer.soDienThoai}</p>
    <p><b>Ngày tạo:</b> ${customer.ngayTao}</p>
</div>
<h3>Lịch sử đơn hàng</h3>
<table class="data-table">
<thead><tr><th>Mã đơn</th><th>Ngày đặt</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th><th>Theo dõi</th></tr></thead>
<tbody>
<c:forEach var="o" items="${orders}">
<tr>
    <td>#${o.maDH}</td>
    <td>${o.ngayDat}</td>
    <td><fmt:formatNumber value="${o.tongTien}" type="number" groupingUsed="true" />đ</td>
    <td>${o.phuongThucThanhToan}</td>
    <td><span class="badge">${o.trangThai}</span></td>
    <td><a class="link-btn" href="${ctx}/admin/order-tracking?id=${o.maDH}">Xem tiến trình</a></td>
</tr>
</c:forEach>
</tbody>
</table>
</section>
</c:if>

<%@ include file="../common/admin-footer.jsp" %>
