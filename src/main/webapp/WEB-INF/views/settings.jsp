<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Cài đặt tài khoản | Celine Closset" scope="request" />
<%@ include file="common/header.jsp" %>
<section class="page-hero small">
    <h1>Cài đặt tài khoản</h1>
    <p>Cập nhật avatar, tên khách hàng, số điện thoại, địa chỉ mặc định và mật khẩu đăng nhập.</p>
</section>
<c:if test="${sessionScope.auth.vaiTro == 'CUSTOMER'}">
<section class="account-shortcuts" aria-label="Tiện ích tài khoản">
    <a href="${ctx}/orders"><strong>Lịch sử mua hàng</strong><span>Xem đơn hàng và trạng thái giao</span></a>
    <a href="${ctx}/wishlist"><strong>Sản phẩm yêu thích</strong><span>Xem lại những sản phẩm đã lưu</span></a>
    <a href="${ctx}/loyalty"><strong>Điểm thưởng &amp; voucher</strong><span>Quản lý ưu đãi thành viên</span></a>
</section>
</c:if>
<section class="section settings-grid customer-settings-grid">
    <div class="form-card profile-setting-card">
        <h2>Thông tin khách hàng</h2>
        <c:if test="${param.success == 'profile'}"><div class="alert success">Đã cập nhật thông tin tài khoản.</div></c:if>
        <c:if test="${param.error == 'email'}"><div class="alert danger">Email này đã được tài khoản khác sử dụng.</div></c:if>
        <c:if test="${param.error == 'emailFormat'}"><div class="alert danger">Email không đúng định dạng, ví dụ ten@gmail.com.</div></c:if>
        <form action="${ctx}/settings" method="post" enctype="multipart/form-data" class="checkout-form compact-form profile-form">
            <input type="hidden" name="action" value="profile">
            <div class="avatar-setting-box">
                <div class="avatar-preview">
                    <c:choose>
                        <c:when test="${not empty profile.hinhDaiDien}">
                            <img src="${ctx}/${profile.hinhDaiDien}?v=${profile.maTK}" alt="Avatar">
                        </c:when>
                        <c:otherwise>
                            <span>👤</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <label class="file-field avatar-file-field">
                    <span>Chọn avatar mới</span>
                    <input type="file" name="avatarFile" accept="image/*">
                    <small>Không chọn ảnh thì hệ thống giữ avatar hiện tại. Hỗ trợ JPG, PNG, WEBP, GIF, SVG.</small>
                </label>
            </div>

            <label>Tên khách hàng</label>
            <input name="hoTen" value="${profile.hoTen}" required>

            <label>Email đăng nhập</label>
            <input type="email" name="email" value="${profile.email}" required
                   pattern="[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}">

            <label>Số điện thoại</label>
            <input name="soDienThoai" value="${profile.soDienThoai}" maxlength="10" placeholder="Ví dụ: 0900000003">

            <label>Địa chỉ mặc định</label>
            <textarea name="diaChiMacDinh" rows="4" placeholder="Nhập địa chỉ nhận hàng mặc định">${profile.diaChiMacDinh}</textarea>

            <button class="btn btn-dark">Lưu cài đặt</button>
        </form>
    </div>

    <div class="form-card password-setting-card">
        <h2>Đổi mật khẩu</h2>
        <c:if test="${param.success == 'password'}"><div class="alert success">Đã đổi mật khẩu.</div></c:if>
        <c:if test="${param.error == 'password'}"><div class="alert danger">Mật khẩu xác nhận không khớp.</div></c:if>
        <c:if test="${param.error == 'passwordRule'}"><div class="alert danger">Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.</div></c:if>
        <form action="${ctx}/settings" method="post" class="checkout-form compact-form">
            <input type="hidden" name="action" value="password">
            <label>Mật khẩu mới</label>
            <div class="password-field"><input id="settingsNewPassword" type="password" name="newPassword" minlength="8" maxlength="32" required><button type="button" class="password-eye" data-password-toggle="settingsNewPassword" aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button></div>
            <label>Nhập lại mật khẩu mới</label>
            <div class="password-field"><input id="settingsConfirmPassword" type="password" name="confirmPassword" minlength="8" maxlength="32" required><button type="button" class="password-eye" data-password-toggle="settingsConfirmPassword" aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button></div>
            <button class="btn btn-dark">Đổi mật khẩu</button>
        </form>
        <div class="setting-note">
            <b>Lưu ý:</b> mật khẩu mới cần 8-32 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt ! @ # $ % ^ &amp; *. Sau khi đổi mật khẩu, bạn vẫn có thể tiếp tục dùng tài khoản hiện tại.
        </div>
    </div>
</section>
<%@ include file="common/footer.jsp" %>
