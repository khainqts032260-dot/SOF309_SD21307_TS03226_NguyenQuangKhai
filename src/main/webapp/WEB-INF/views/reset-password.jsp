<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đặt lại mật khẩu | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<section class="auth-page refined-auth-page">
    <div class="auth-card refined-auth-card">
        <p class="eyebrow">Bảo mật tài khoản khách hàng</p><h1>Đặt lại mật khẩu</h1>
        <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
        <c:choose>
            <c:when test="${validReset}">
                <form action="${ctx}/reset-password" method="post" class="reset-password-form">
                    <input type="hidden" name="token" value="${token}">
                    <p class="auth-intro">Tạo mật khẩu mới cho tài khoản CUSTOMER. Liên kết chỉ dùng được một lần.</p>
                    <label>Mật khẩu mới</label>
                    <div class="password-field">
                        <input id="resetPassword" type="password" name="password" required placeholder="Ít nhất 8 ký tự">
                        <button type="button" class="password-eye" data-password-toggle="resetPassword" aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button>
                    </div>
                    <label>Nhập lại mật khẩu</label>
                    <div class="password-field">
                        <input id="resetConfirmPassword" type="password" name="confirmPassword" required placeholder="Nhập lại mật khẩu">
                        <button type="button" class="password-eye" data-password-toggle="resetConfirmPassword" aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button>
                    </div>
                    <button class="btn btn-dark auth-main-button">Lưu mật khẩu mới</button>
                </form>
            </c:when>
            <c:otherwise>
                <div class="empty-box">Liên kết không hợp lệ hoặc đã hết hạn. <a href="${ctx}/forgot-password">Gửi yêu cầu mới</a></div>
            </c:otherwise>
        </c:choose>
    </div>
</section>
<%@ include file="common/footer.jsp" %>
