<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../common/admin-header.jsp" %>
<div class="admin-top refined-admin-top">
    <div><p class="admin-eyebrow">Đội ngũ cửa hàng</p><h1>Quản lý nhân viên</h1><p>Thêm, chỉnh sửa và khóa/mở tài khoản nhân viên. Mã nhân viên được hệ thống tạo tự động.</p></div>
</div>

<c:if test="${param.error == 'duplicateEmail'}"><div class="alert error">Email này đã tồn tại trong hệ thống.</div></c:if>
<c:if test="${param.error == 'missingPassword'}"><div class="alert error">Khi thêm nhân viên mới, vui lòng nhập mật khẩu.</div></c:if>
<c:if test="${param.error == 'emailFormat'}"><div class="alert error">Email không đúng định dạng, ví dụ ten@gmail.com.</div></c:if>
<c:if test="${param.error == 'passwordRule'}"><div class="alert error">Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.</div></c:if>
<c:if test="${param.success == 'add'}"><div class="alert success">Đã thêm nhân viên và tạo mã tự động.</div></c:if>
<c:if test="${param.success == 'edit'}"><div class="alert success">Đã cập nhật nhân viên thành công.</div></c:if>
<c:if test="${param.success == 'status'}"><div class="alert success">Đã cập nhật trạng thái tài khoản.</div></c:if>

<section class="admin-card form-card staff-form-card">
    <div class="admin-card-heading"><div><h2>${empty editAccount ? 'Thêm nhân viên' : 'Sửa thông tin nhân viên'}</h2><p>Vai trò của tài khoản mới mặc định là Nhân viên.</p></div></div>
    <form action="${ctx}/admin/accounts" method="post" class="admin-form grid-form">
        <input type="hidden" name="maTK" value="${editAccount.maTK}">
        <label>Loại nhân viên<select name="vaiTro" required><option value="STAFF" ${empty editAccount || editAccount.vaiTro == 'STAFF' ? 'selected' : ''}>Staff — xử lý đơn, thanh toán, hỗ trợ</option><option value="DELIVERY" ${editAccount.vaiTro == 'DELIVERY' ? 'selected' : ''}>Delivery — giao hàng, cập nhật vị trí</option></select></label>
        <label>Họ và tên<input name="hoTen" placeholder="Nguyễn Văn A" value="${editAccount.hoTen}" required></label>
        <label>Email đăng nhập<input type="email" name="email" placeholder="nhanvien@celine.vn" value="${editAccount.email}" required pattern="[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}"></label>
        <label>Mật khẩu<input type="password" name="password" minlength="8" maxlength="32" placeholder="${empty editAccount ? 'Mật khẩu ban đầu' : 'Bỏ trống nếu không đổi'}" ${empty editAccount ? 'required' : ''}></label>
        <label>Số điện thoại<input name="soDienThoai" placeholder="0xxxxxxxxx" value="${editAccount.soDienThoai}"></label>
        <small class="span-2">Mật khẩu cần chữ hoa, chữ thường, số và ký tự đặc biệt. Mã NV sẽ tự động tạo theo số tài khoản.</small>
        <div class="form-actions span-2"><button class="btn btn-dark">${empty editAccount ? 'Thêm nhân viên' : 'Lưu thay đổi'}</button><c:if test="${not empty editAccount}"><a class="btn" href="${ctx}/admin/accounts">Hủy chỉnh sửa</a></c:if></div>
    </form>
</section>

<section class="admin-card">
    <div class="admin-card-heading"><div><h2>Danh sách nhân viên</h2><p>Chỉ tài khoản Nhân viên xuất hiện tại đây; tài khoản Chủ cửa hàng được tách riêng.</p></div><span>${fn:length(accounts)} nhân viên</span></div>
    <div class="table-scroll"><table class="data-table">
        <thead><tr><th>Mã nhân viên</th><th>Họ tên</th><th>Email</th><th>Vai trò</th><th>Công việc đã lưu</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
        <tbody>
        <c:forEach var="a" items="${accounts}">
            <tr><td><strong class="staff-code">NV<c:if test="${a.maTK < 1000}">0</c:if><c:if test="${a.maTK < 100}">0</c:if><c:if test="${a.maTK < 10}">0</c:if>${a.maTK}</strong><small>${empty a.soDienThoai ? 'Chưa có SĐT' : a.soDienThoai}</small></td><td>${a.hoTen}</td><td>${a.email}</td><td><span class="status-pill active">${a.vaiTro == 'DELIVERY' ? 'Delivery' : 'Staff'}</span></td><td><b>${a.soDonPhuTrach} đơn</b><small>${a.soHoatDong} hoạt động đã ghi</small><a class="link-btn" href="${ctx}/admin/staff-activity?staffId=${a.maTK}">Xem nhật ký</a></td><td><span class="status-pill ${a.trangThai == 1 ? 'active' : 'inactive'}">${a.trangThai == 1 ? 'Hoạt động' : 'Đã khóa'}</span></td><td class="table-actions"><a class="link-btn" href="${ctx}/admin/accounts?edit=${a.maTK}">Sửa</a><form action="${ctx}/admin/accounts" method="post" style="display:inline"><input type="hidden" name="action" value="status"><input type="hidden" name="id" value="${a.maTK}"><input type="hidden" name="status" value="${a.trangThai == 1 ? 0 : 1}"><button class="link-btn">${a.trangThai == 1 ? 'Khóa' : 'Mở'}</button></form></td></tr>
        </c:forEach>
        <c:if test="${empty accounts}"><tr><td colspan="7">Chưa có nhân viên.</td></tr></c:if>
        </tbody>
    </table></div>
</section>
<%@ include file="../common/admin-footer.jsp" %>
