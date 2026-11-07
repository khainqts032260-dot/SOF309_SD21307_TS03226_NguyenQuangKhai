<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="C&C Rewards | Thành viên" scope="request" />
<%@ include file="common/header.jsp" %>
<main class="loyalty-page">
    <section class="loyalty-hero">
        <div><p>C&amp;C Rewards</p><h1>Đặc quyền<br>của riêng bạn</h1><span>Mỗi 10.000đ thanh toán thành công được tích 1 điểm.</span></div>
        <aside>
            <small>Hạng hiện tại</small><strong>${loyalty.hangThanhVien}</strong>
            <b>${loyalty.diemTichLuy} điểm</b>
            <c:if test="${loyalty.hangThanhVien != 'DIAMOND'}"><p>Còn ${loyalty.mocTiepTheo - loyalty.diemTichLuy} điểm để lên hạng tiếp theo.</p></c:if>
        </aside>
    </section>
    <section class="fashion-section fashion-container">
        <c:if test="${not empty param.success}"><div class="alert success">Đổi thưởng thành công: ${param.success}</div></c:if>
        <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
        <div class="fashion-heading"><div><span>Redeem points</span><h2>Đổi điểm lấy voucher hoặc quà</h2></div></div>
        <div class="reward-grid">
            <c:forEach var="r" items="${rewards}">
                <article class="reward-card">
                    <img src="${ctx}/${r.hinhAnh}" alt="${r.tenPhanThuong}">
                    <div><small>${r.loai == 'VOUCHER' ? 'Voucher' : 'Quà tặng'}</small><h3>${r.tenPhanThuong}</h3><p>${r.moTa}</p><b>${r.diemCan} điểm</b>
                    <form action="${ctx}/loyalty" method="post"><input type="hidden" name="rewardId" value="${r.maPhanThuong}"><button class="btn btn-dark" ${loyalty.diemTichLuy < r.diemCan ? 'disabled' : ''}>Đổi ngay</button></form></div>
                </article>
            </c:forEach>
        </div>
    </section>
    <section class="fashion-section soft-section">
        <div class="fashion-container">
            <div class="fashion-heading"><div><span>My wallet</span><h2>Ví voucher của tôi</h2></div></div>
            <div class="voucher-wallet-grid">
                <c:forEach var="v" items="${myVouchers}"><article class="voucher-ticket ${v.trangThaiCaNhan != 'AVAILABLE' ? 'used' : ''}"><small>${v.trangThaiCaNhan}</small><h3>${v.maCode}</h3><p>${v.tenVoucher}</p><span>Đơn tối thiểu <fmt:formatNumber value="${v.donToiThieu}" type="number" />đ</span></article></c:forEach>
                <c:if test="${empty myVouchers}"><div class="empty-box">Bạn chưa có voucher cá nhân.</div></c:if>
            </div>
        </div>
    </section>
    <section class="fashion-section fashion-container loyalty-history-grid">
        <div><h2>Lịch sử điểm</h2><div class="simple-history"><c:forEach var="h" items="${pointHistory}"><p><span>${h.noiDung}<small>${h.ngayTao}</small></span><b class="${h.soDiem < 0 ? 'negative' : ''}">${h.soDiem > 0 ? '+' : ''}${h.soDiem}</b></p></c:forEach></div></div>
        <div><h2>Yêu cầu đổi quà</h2><div class="simple-history"><c:forEach var="d" items="${redemptions}"><p><span>${d.tenPhanThuong}<small>${d.ngayDoi}</small></span><b>${d.trangThai}</b></p></c:forEach><c:if test="${empty redemptions}"><p>Chưa có yêu cầu đổi quà.</p></c:if></div></div>
    </section>
</main>
<%@ include file="common/footer.jsp" %>
