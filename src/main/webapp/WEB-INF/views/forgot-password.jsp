<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quên mật khẩu | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<section class="auth-page refined-auth-page">
    <form class="auth-card refined-auth-card" action="${ctx}/forgot-password" method="post">
        <p class="eyebrow">Khôi phục tài khoản khách hàng</p><h1>Quên mật khẩu</h1>
        <p class="auth-intro">Chức năng này dành cho CUSTOMER. Nhập email đã đăng ký; liên kết đặt lại chỉ dùng một lần và có hiệu lực trong ${empty passwordResetMinutes ? 15 : passwordResetMinutes} phút.</p>
        <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
        <c:if test="${not empty success}"><div class="alert success">${success}</div></c:if>
        <c:if test="${not empty mailNotice}"><div class="alert">${mailNotice}</div></c:if>
        <label>Email</label><input type="email" name="email" required placeholder="ten@email.com">
        <button class="btn btn-dark auth-main-button">Gửi liên kết đặt lại</button>
        <c:if test="${not empty devResetLink}"><div class="dev-reset-box"><b>Chế độ demo:</b> <a href="${devResetLink}">Mở liên kết reset CUSTOMER để thử ngay</a>. Khi điền Gmail + App Password, link này sẽ được gửi qua email.</div></c:if>
        <p class="auth-switch"><a href="${ctx}/login">← Quay lại đăng nhập</a></p>
    </form>
</section>
<%@ include file="common/footer.jsp" %>
