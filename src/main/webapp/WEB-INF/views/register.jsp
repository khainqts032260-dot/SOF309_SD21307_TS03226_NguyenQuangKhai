<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đăng ký | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<main class="auth-page refined-auth-page centered-auth-page register-auth-page">
    <section class="auth-shell register-shell">
        <nav class="auth-tabs" aria-label="Đăng nhập hoặc đăng ký"><a href="${ctx}/login">Đăng nhập</a><a class="active" href="${ctx}/register">Đăng ký</a></nav>
        <form class="auth-card refined-auth-card compact-auth-card register-card" action="${ctx}/register" method="post">
            <p class="eyebrow">Thành viên mới</p><h1>Tạo tài khoản</h1>
            <p class="auth-intro">Tích điểm theo đơn hàng, nhận ưu đãi hạng thành viên và lưu thông tin mua sắm.</p>
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
            <div class="register-grid">
                <label>Họ tên<input name="hoTen" value="${param.hoTen}" required placeholder="Họ và tên"></label>
                <label>Số điện thoại<input name="soDienThoai" value="${param.soDienThoai}" required inputmode="numeric" minlength="10" maxlength="10" pattern="0[0-9]{9}" title="Số điện thoại phải đủ 10 số và bắt đầu bằng 0" placeholder="0xxxxxxxxx"></label>
                <label class="full">Email<input type="email" name="email" value="${param.email}" required pattern="[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}" placeholder="name@email.com"></label>
                <label>Mật khẩu<div class="password-field"><input id="registerPassword" type="password" name="password" minlength="8" maxlength="32" required placeholder="Tối thiểu 8 ký tự"><button type="button" class="password-eye" data-password-toggle="registerPassword" aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button></div></label>
                <label>Nhập lại mật khẩu<div class="password-field"><input id="confirmPassword" type="password" name="confirmPassword" minlength="8" maxlength="32" required placeholder="Nhập lại mật khẩu"><button type="button" class="password-eye" data-password-toggle="confirmPassword" aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button></div></label>
            </div>
            <small class="password-hint">Mật khẩu nên có chữ hoa, chữ thường, số và ký tự đặc biệt.</small>
            <button class="btn btn-dark auth-main-button" type="submit">Tạo tài khoản</button>
            <p class="auth-switch">Đã có tài khoản? <a class="auth-switch-link" href="${ctx}/login">Đăng nhập</a></p>
        </form>
    </section>
</main>
<%@ include file="common/footer.jsp" %>
