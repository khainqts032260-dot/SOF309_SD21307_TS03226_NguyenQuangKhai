<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="../common/admin-header.jsp" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<section class="admin-page-head"><div><span class="eyebrow">EMPLOYEE PROFILE</span><h1>Hồ sơ nhân viên</h1><p>Thông tin cá nhân và lịch sử các đơn đã xử lý.</p></div></section>
<c:if test="${not empty sessionScope.profileSuccess}"><div class="admin-alert success">${sessionScope.profileSuccess}</div><c:remove var="profileSuccess" scope="session"/></c:if>
<c:if test="${not empty sessionScope.profileError}"><div class="admin-alert error">${sessionScope.profileError}</div><c:remove var="profileError" scope="session"/></c:if>
<div class="staff-profile-grid">
  <section class="staff-profile-card">
    <c:choose><c:when test="${not empty profile.hinhDaiDien}"><img class="staff-avatar" src="${ctx}/${profile.hinhDaiDien}" alt="Ảnh nhân viên"></c:when><c:otherwise><img class="staff-avatar" src="${ctx}/assets/images/fashion/card-01.jpg" alt="Ảnh nhân viên"></c:otherwise></c:choose>
    <h2 class="staff-profile-name">${profile.hoTen}</h2>
    <div class="staff-profile-meta">
      <span>Mã nhân viên <b>NV${profile.maTK}</b></span>
      <span>Vai trò <b>${profile.vaiTro}</b></span>
    </div>
    <div class="staff-stats" aria-label="Thống kê xử lý đơn hàng">
      <div class="staff-stat"><span class="staff-stat-icon"><i class="fa-solid fa-clipboard-check"></i></span><small>Tổng đơn xử lý</small><strong>${profile.tongDonXuLy}</strong></div>
      <div class="staff-stat"><span class="staff-stat-icon"><i class="fa-solid fa-circle-check"></i></span><small>Hoàn thành</small><strong>${profile.donHoanThanh}</strong></div>
      <div class="staff-stat"><span class="staff-stat-icon"><i class="fa-solid fa-triangle-exclamation"></i></span><small>Đơn lỗi / hủy</small><strong>${profile.donLoi}</strong></div>
    </div>
  </section>
  <section class="staff-profile-card">
    <h2>Cập nhật thông tin</h2>
    <form class="staff-profile-form" method="post" action="${ctx}/admin/profile" enctype="multipart/form-data">
      <label>Họ tên<input name="hoTen" value="${profile.hoTen}" required></label>
      <label>Email<input type="email" name="email" value="${profile.email}" required></label>
      <label>Số điện thoại<input name="soDienThoai" value="${profile.soDienThoai}" maxlength="10"></label>
      <label>Địa chỉ<input name="diaChiMacDinh" value="${profile.diaChiMacDinh}"></label>
      <label>Ảnh nhân viên<input type="file" name="avatar" accept="image/*"></label>
      <button class="btn btn-dark" type="submit">Lưu hồ sơ</button>
    </form>
  </section>
</div>
<section class="admin-panel" style="margin-top:24px"><div class="admin-panel-title"><div><span>LỊCH SỬ XỬ LÝ</span><h2>Các đơn gần nhất</h2></div></div><div class="admin-table-wrap"><table class="admin-table"><thead><tr><th>Mã đơn</th><th>Khách nhận</th><th>Ngày đặt</th><th>Tổng tiền</th><th>Trạng thái</th></tr></thead><tbody><c:forEach var="o" items="${processedOrders}"><tr><td>#${o.maDH}</td><td>${o.hoTenNhan}</td><td><fmt:formatDate value="${o.ngayDat}" pattern="dd/MM/yyyy HH:mm"/></td><td><fmt:formatNumber value="${o.tongTien}" pattern="#,#00"/>đ</td><td>${o.trangThai}</td></tr></c:forEach><c:if test="${empty processedOrders}"><tr><td colspan="5" class="admin-empty">Chưa có đơn đã xử lý.</td></tr></c:if></tbody></table></div></section>
<jsp:include page="../common/admin-footer.jsp" />
